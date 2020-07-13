/*
 *    Copyright 2020 Mateusz Armatys
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.nofrills.multimodule

import com.android.build.gradle.TestedExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.CompileOptions
import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.plugins.JavaBasePlugin.CHECK_TASK_NAME
import org.gradle.api.plugins.JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.dokka.gradle.DokkaTask
import java.io.File
import java.util.concurrent.Callable

abstract class AndroidPlugin : BasePlugin() {
    protected abstract val androidPluginId: String

    protected abstract fun getBaseVariants(project: Project): Collection<BaseVariant>
    protected abstract fun getComponentNameForVariant(variant: BaseVariant): String
    protected abstract fun getPublicationNameForVariant(variant: BaseVariant): String

    override fun applyJacoco(project: Project, jacocoConfig: JacocoConfig) {
        project.pluginManager.apply(PLUGIN_ID_JACOCO)
        project.tasks.withType(Test::class.java) { test ->
            test.extensions.configure(JacocoTaskExtension::class.java) {
                it.excludes = listOf("jdk.internal.*")
                it.isIncludeNoLocationClasses = true
            }
        }

        getBaseVariants(project).forEach { variant ->
            val jacocoReportTask = project.tasks.register(
                    "jacoco${variant.name.capitalize()}TestReport",
                    JacocoReport::class.java
            ) { jacoco ->
                jacoco.dependsOn(project.tasks.withType(Test::class.java))
                jacoco.executionData.setFrom(project.fileTree(project.buildDir) {
                    it.include(setOf("jacoco/test${variant.name.capitalize()}UnitTest.exec"))
                })
                jacoco.reports {
                    it.html.isEnabled = true
                    it.xml.isEnabled = true
                }

                jacoco.sourceDirectories.setFrom(variant.sourceSets.map { it.javaDirectories })
                jacoco.classDirectories.setFrom(variant.getCompileClasspath(null).filter { it.extension != "jar" })

                jacocoConfig.jacocoTaskAction?.execute(jacoco)
            }
            jacocoConfig.jacocoPluginAction?.execute(project.extensions.getByType(JacocoPluginExtension::class.java))
            project.tasks.named(CHECK_TASK_NAME).dependsOn(jacocoReportTask)
        }
    }

    final override fun applyKotlin(project: Project, kotlinConfig: KotlinConfig) {
        project.pluginManager.apply(PLUGIN_ID_KOTLIN_ANDROID)
        if (kotlinConfig.androidExtensions) {
            project.pluginManager.apply(PLUGIN_ID_KOTLIN_ANDROID_EXTENSIONS)
        }
        project.extensions.getByType(TestedExtension::class.java).apply {
            sourceSets.getByName("androidTest").java.srcDir("src/androidTest/kotlin")
            sourceSets.getByName("main").java.srcDir("src/main/kotlin")
            sourceSets.getByName("test").java.srcDir("src/test/kotlin")
        }
        if (kotlinConfig.coroutines) {
            project.configurations.getByName(IMPLEMENTATION_CONFIGURATION_NAME) { config ->
                config.withDependencies {
                    val dep = project.dependencies.create(LIBRARY_COROUTINES_ANDROID) as ExternalModuleDependency
                    dep.version(kotlinConfig.coroutinesVersion)
                    it.add(dep)
                }
            }
        }
    }

    final override fun applyPlugin(project: Project, multimoduleExtension: MultimoduleExtension) {
        project.pluginManager.apply(androidPluginId)

        val javaConfig = JavaConfig(project)
        multimoduleExtension.javaAction?.execute(javaConfig)

        project.extensions.getByType(TestedExtension::class.java).apply {
            compileOptions(Action<CompileOptions> {
                it.sourceCompatibility = javaConfig.sourceCompatibility
                it.targetCompatibility = javaConfig.targetCompatibility
            })
            multimoduleExtension.activeProject = ThreadLocal.withInitial { project }
            multimoduleExtension.androidAction?.execute(this)
        }
    }

    final override fun applyPublications(
            project: Project,
            publishConfig: PublishConfig,
            publications: PublicationContainer
    ) {
        val variant = getDefaultPublishVariant(project)
        if (variant != null) {
            val docsJarTaskProvider = lazy { getDocsJarTaskProvider(project, variant) }
            val sourcesJarTaskProvider = lazy { getSourcesJarTaskProvider(project, variant) }

            registerPublication(
                    project, publishConfig, publications,
                    lazyDocsJarTask = docsJarTaskProvider,
                    lazySourcesJarTask = sourcesJarTaskProvider,
                    componentName = getComponentNameForVariant(variant),
                    publicationName = getPublicationNameForVariant(variant)
            )
        } else {
            project.logger.warn("Cannot create publication for ${project}: default publish config not found (check 'android.defaultPublishConfig' setting).")
        }
    }

    private fun getDefaultPublishVariant(project: Project): BaseVariant? {
        val testedExtension = project.extensions.getByType(TestedExtension::class.java)
        return getBaseVariants(project).find { it.name == testedExtension.defaultPublishConfig }
    }

    private fun getDocsJarTaskProvider(project: Project, variant: BaseVariant): TaskProvider<Jar> {
        return if (project.plugins.hasPlugin(PLUGIN_ID_KOTLIN_ANDROID)) {
            getDokkaJarTaskProvider(project, variant)
        } else {
            getJavadocJarTaskProvider(project, variant)
        }
    }

    private fun getDokkaJarTaskProvider(project: Project, variant: BaseVariant): TaskProvider<Jar> {
        project.pluginManager.apply(PLUGIN_ID_DOKKA)

        val dokkaTaskProvider = project.tasks.named(TASK_NAME_DOKKA, DokkaTask::class.java) { dokka ->
            dokka.group = "documentation"
            dokka.configuration.kotlinTasks(Callable { emptyList<Any>() })
            dokka.outputDirectory = File(project.buildDir, "dokka").path
            dokka.outputFormat = DOKKA_FORMAT
            dokka.disableAutoconfiguration = true
            dokka.dependsOn(dokka.defaultKotlinTasks())

            dokka.doFirst {
                dokka.configuration.classpath = variant.javaCompileProvider.get().classpath.map { it.path }
                variant.sourceSets.map { it.javaDirectories }
                        .flatten()
                        .forEach { src ->
                            dokka.configuration.sourceRoot(Action {
                                it.path = src.path
                            })
                        }
            }
        }

        return project.tasks.register("${variant.name}DokkaJar", Jar::class.java) { jar ->
            jar.group = "documentation"
            jar.from(dokkaTaskProvider.get())
            jar.archiveClassifier.set("javadoc")
        }
    }

    private fun getJavadocJarTaskProvider(project: Project, variant: BaseVariant): TaskProvider<Jar> {
        val javadocProvider = project.tasks.register("${variant.name}Javadoc", Javadoc::class.java) { javadoc ->
            javadoc.group = "documentation"
            javadoc.doFirst {
                javadoc.source(variant.sourceSets.map { it.javaDirectories })
                javadoc.classpath += project.files(project.extensions.getByType(TestedExtension::class.java).bootClasspath)
                javadoc.classpath += variant.javaCompileProvider.get().classpath
            }
        }
        return project.tasks.register("${variant.name}JavadocJar", Jar::class.java).apply {
            configure { jar ->
                jar.group = "documentation"
                jar.dependsOn(javadocProvider)
                jar.archiveClassifier.set("javadoc")
                jar.from(javadocProvider.get().destinationDir)
            }
        }
    }

    private fun getSourcesJarTaskProvider(project: Project, variant: BaseVariant): TaskProvider<Jar> {
        return project.tasks.register("${variant.name}SourcesJar", Jar::class.java) { jar ->
            jar.from(variant.sourceSets.map { it.javaDirectories })
            jar.archiveClassifier.set("sources")
        }
    }
}
