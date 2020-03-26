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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.plugins.JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME
import org.gradle.api.provider.Property
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

abstract class BasePlugin : Plugin<Project> {
    companion object {
        internal const val LIBRARY_COROUTINES_ANDROID = "org.jetbrains.kotlinx:kotlinx-coroutines-android"
        private const val LIBRARY_COROUTINES_CORE = "org.jetbrains.kotlinx:kotlinx-coroutines-core"
        private const val LIBRARY_KOTLIN_REFLECT = "org.jetbrains.kotlin:kotlin-reflect"
        private const val LIBRARY_KOTLIN_STDLIB = "org.jetbrains.kotlin:kotlin-stdlib"
        private const val LIBRARY_KOTLIN_STDLIB_JDK8 = "org.jetbrains.kotlin:kotlin-stdlib-jdk8"

        internal const val PLUGIN_ID_ANDROID_APP = "com.android.application"
        internal const val PLUGIN_ID_ANDROID_LIBRARY = "com.android.library"
        internal const val PLUGIN_ID_DOKKA = "org.jetbrains.dokka"
        internal const val PLUGIN_ID_JACOCO = "jacoco"
        internal const val PLUGIN_ID_JAVA_LIBRARY = "java-library"
        internal const val PLUGIN_ID_KOTLIN_ANDROID = "org.jetbrains.kotlin.android"
        internal const val PLUGIN_ID_KOTLIN_ANDROID_EXTENSIONS = "org.jetbrains.kotlin.android.extensions"
        internal const val PLUGIN_ID_KOTLIN_JVM = "org.jetbrains.kotlin.jvm"
        internal const val PLUGIN_ID_KOTLIN_KAPT = "org.jetbrains.kotlin.kapt"
        private const val PLUGIN_ID_MAVEN_PUBLISH = "maven-publish"

        private const val SUBMODULE_EXT_NAME = "submodule"

        internal const val DOKKA_FORMAT = "html"
        internal const val TASK_NAME_DOKKA = "dokka"

        internal fun getSubmoduleExtension(project: Project): SubmoduleExtension? {
            return project.extensions.findByType(SubmoduleExtension::class.java)
        }
    }

    /** Apply the jacoco plugin. */
    protected abstract fun applyJacoco(project: Project, jacocoConfig: JacocoConfig)

    /** Apply the kotlin plugin appropriate for the module. */
    protected abstract fun applyKotlin(project: Project, kotlinConfig: KotlinConfig)

    /** Apply the main plugin for the module. */
    protected abstract fun applyPlugin(project: Project, multimoduleExtension: MultimoduleExtension)

    /** Create and configure Maven publications for the module. */
    protected abstract fun applyPublications(
            project: Project,
            publishConfig: PublishConfig,
            publications: PublicationContainer
    )

    final override fun apply(project: Project) {
        val submoduleExtension = project.extensions.create(SUBMODULE_EXT_NAME, SubmoduleExtension::class.java, project)
        val multimoduleExtension = project.rootProject.extensions.getByType(MultimoduleExtension::class.java)

        applyPlugin(project, multimoduleExtension)

        multimoduleExtension.kotlinConfig?.let { kotlinConfig ->
            applyKotlin(project, kotlinConfig)
            if (kotlinConfig.kapt) {
                project.pluginManager.apply(PLUGIN_ID_KOTLIN_KAPT)
            }
            project.configureKotlinTasks(kotlinConfig)
            if (kotlinConfig.coroutines) {
                project.configurations.getByName(IMPLEMENTATION_CONFIGURATION_NAME) { config ->
                    config.withDependencies {
                        val dep = project.dependencies.create(LIBRARY_COROUTINES_CORE) as ExternalModuleDependency
                        dep.version(kotlinConfig.coroutinesVersion)
                        it.add(dep)
                    }
                }
            }

            val kotlinVersion by lazy { project.getKotlinPluginVersion() }
            if (kotlinConfig.stdLib) {
                val stdLib = if (kotlinConfig.jvmTarget == "1.6") {
                    LIBRARY_KOTLIN_STDLIB
                } else {
                    LIBRARY_KOTLIN_STDLIB_JDK8
                }
                project.configurations.getByName(IMPLEMENTATION_CONFIGURATION_NAME) { config ->
                    config.withDependencies { dependencySet ->
                        val dep = project.dependencies.create(stdLib) as ExternalModuleDependency
                        kotlinVersion?.let { ver -> dep.version { it.require(ver) } }
                        dependencySet.add(dep)
                    }
                }
            }
            if (kotlinConfig.reflect) {
                project.configurations.getByName(IMPLEMENTATION_CONFIGURATION_NAME) { config ->
                    config.withDependencies {
                        it.add(project.dependencies.create("$LIBRARY_KOTLIN_REFLECT:$kotlinVersion"))
                    }
                }
            }
        }

        multimoduleExtension.jacocoAction?.let { jacocoAction ->
            project.afterEvaluate { project ->
                if (submoduleExtension.jacocoAllowed.get()) {
                    val jacocoConfig = JacocoConfig()
                    jacocoAction.execute(jacocoConfig)
                    applyJacoco(project, jacocoConfig)
                }
            }
        }

        multimoduleExtension.publishConfig?.let { publishConfig ->
            project.afterEvaluate { project ->
                if (submoduleExtension.publishAllowed.get()) {
                    project.pluginManager.apply(PLUGIN_ID_MAVEN_PUBLISH)
                    val publishing = project.extensions.getByType(PublishingExtension::class.java)
                    publishConfig.repositoriesAction?.let { publishing.repositories(it) }
                    applyPublications(project, publishConfig, publishing.publications)
                }
            }
        }
    }

    protected fun createPublication(
            project: Project,
            publishConfig: PublishConfig,
            publications: PublicationContainer,
            lazyDocsJarTask: Lazy<TaskProvider<Jar>>,
            lazySourcesJarTask: Lazy<TaskProvider<Jar>>,
            componentName: String,
            publicationName: String
    ) {
        val docsProvider = if (publishConfig.withDocs) lazyDocsJarTask.value else null
        val sourcesProvider = if (publishConfig.withSources) lazySourcesJarTask.value else null
        publications.create(publicationName, MavenPublication::class.java) { mavenPublication ->
            mavenPublication.from(project.components.getByName(componentName))
            docsProvider?.let { mavenPublication.artifact(it.get()) }
            sourcesProvider?.let { mavenPublication.artifact(it.get()) }
            publishConfig.mavenPomAction?.let { mavenPublication.pom(it) }
        }
    }

    private fun Project.configureKotlinTasks(kotlinConfig: KotlinConfig) {
        project.tasks.withType(KotlinCompile::class.java).configureEach { kotlinCompile ->
            kotlinCompile.kotlinOptions.apply {
                kotlinConfig.allWarningsAsErrors?.let { allWarningsAsErrors = it }
                apiVersion = kotlinConfig.apiVersion
                kotlinConfig.freeCompilerArgs?.let { freeCompilerArgs = it }
                jvmTarget = kotlinConfig.jvmTarget
                languageVersion = kotlinConfig.languageVersion
                kotlinConfig.suppressWarnings?.let { suppressWarnings = it }
                kotlinConfig.useIR?.let { useIR = it }
                kotlinConfig.verbose?.let { verbose = it }
            }
        }
    }
}

abstract class SubmoduleExtension(project: Project) {
    var dokkaAllowed: Property<Boolean> = project.objects.property(Boolean::class.java).convention(true)
    var jacocoAllowed: Property<Boolean> = project.objects.property(Boolean::class.java).convention(true)
    var publishAllowed: Property<Boolean> = project.objects.property(Boolean::class.java).convention(true)
}
