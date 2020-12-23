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

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.plugins.JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.provider.Property
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import java.io.File

abstract class BasePlugin : Plugin<Project> {
    companion object {
        internal const val LIBRARY_COROUTINES_ANDROID = "org.jetbrains.kotlinx:kotlinx-coroutines-android"
        private const val LIBRARY_COROUTINES_CORE = "org.jetbrains.kotlinx:kotlinx-coroutines-core"
        private const val LIBRARY_KOTLIN_REFLECT = "org.jetbrains.kotlin:kotlin-reflect"

        internal const val PLUGIN_ID_ANDROID_APP = "com.android.application"
        internal const val PLUGIN_ID_ANDROID_LIBRARY = "com.android.library"
        internal const val PLUGIN_ID_DOKKA = "org.jetbrains.dokka"
        internal const val PLUGIN_ID_JACOCO = "jacoco"
        internal const val PLUGIN_ID_JAVA_LIBRARY = "java-library"
        internal const val PLUGIN_ID_KOTLIN_ANDROID = "org.jetbrains.kotlin.android"
        internal const val PLUGIN_ID_KOTLIN_JVM = "org.jetbrains.kotlin.jvm"
        internal const val PLUGIN_ID_KOTLIN_KAPT = "org.jetbrains.kotlin.kapt"
        internal const val PLUGIN_ID_KOTLIN_PARCELIZE = "org.jetbrains.kotlin.plugin.parcelize"
        private const val PLUGIN_ID_MAVEN_PUBLISH = "maven-publish"

        private const val SUBMODULE_EXT_NAME = "submodule"

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

        multimoduleExtension.kotlinAction?.let { kotlinAction ->
            performKotlinAction(project, kotlinAction)
        }

        multimoduleExtension.jacocoAction?.let { jacocoAction ->
            performJacocoAction(project, jacocoAction, submoduleExtension)
        }

        if (multimoduleExtension.kotlinAction != null) {
            multimoduleExtension.dokkaAction?.let { dokkaAction ->
                val isDokkaAllowed = getSubmoduleExtension(project)?.dokkaAllowed?.get() ?: true
                if (isDokkaAllowed) {
                    performDokkaAction(project, dokkaAction)
                }
            }
        }

        multimoduleExtension.publishAction?.let { publishAction ->
            performPublishAction(project, publishAction, submoduleExtension)
        }
    }

    private fun performDokkaAction(project: Project, dokkaAction: Action<DokkaTask>) {
        project.pluginManager.apply(PLUGIN_ID_DOKKA)

        project.tasks.withType(DokkaTask::class.java) { dokka ->
            val javaVersion = project.convention.getPlugin(JavaPluginConvention::class.java).sourceCompatibility
            dokka.dokkaSourceSets.configureEach {
                it.jdkVersion.set(javaVersion.ordinal + 1)
                if (project.plugins.hasPlugin(PLUGIN_ID_ANDROID_APP)
                    || project.plugins.hasPlugin(PLUGIN_ID_ANDROID_LIBRARY)
                ) {
                    it.noAndroidSdkLink.set(false)
                }
            }
            dokka.group = "documentation"
            dokka.outputDirectory.set(File(project.buildDir, "dokka"))
            dokkaAction.execute(dokka)
        }
    }

    private fun performKotlinAction(project: Project, kotlinAction: Action<KotlinConfig>) {
        val kotlinConfig = KotlinConfig(project)
        kotlinAction.execute(kotlinConfig)

        applyKotlin(project, kotlinConfig)

        if (kotlinConfig.kapt) {
            project.pluginManager.apply(PLUGIN_ID_KOTLIN_KAPT)
        }

        configureKotlinTasks(project, kotlinConfig)

        if (kotlinConfig.coroutines) {
            addCoroutinesLibrary(project, kotlinConfig)
        }

        val kotlinVersion by lazy { project.getKotlinPluginVersion() }
        if (kotlinConfig.reflect) {
            addKotlinReflectLibrary(project, kotlinVersion)
        }
    }

    private fun addCoroutinesLibrary(project: Project, kotlinConfig: KotlinConfig) {
        project.configurations.getByName(IMPLEMENTATION_CONFIGURATION_NAME) { config ->
            config.withDependencies {
                val dep = project.dependencies.create(LIBRARY_COROUTINES_CORE) as ExternalModuleDependency
                dep.version(kotlinConfig.coroutinesVersion)
                it.add(dep)
            }
        }
    }

    private fun addKotlinReflectLibrary(project: Project, kotlinVersion: String?) {
        project.configurations.getByName(IMPLEMENTATION_CONFIGURATION_NAME) { config ->
            config.withDependencies {
                it.add(project.dependencies.create("$LIBRARY_KOTLIN_REFLECT:$kotlinVersion"))
            }
        }
    }

    private fun performJacocoAction(
        project: Project,
        jacocoAction: Action<JacocoConfig>,
        submoduleExtension: SubmoduleExtension
    ) {
        project.afterEvaluate {
            if (submoduleExtension.jacocoAllowed.get()) {
                val jacocoConfig = JacocoConfig(project)
                jacocoAction.execute(jacocoConfig)
                applyJacoco(project, jacocoConfig)
            }
        }
    }

    private fun performPublishAction(
        project: Project,
        publishAction: Action<PublishConfig>,
        submoduleExtension: SubmoduleExtension
    ) {
        val publishConfig = PublishConfig(project)
        publishAction.execute(publishConfig)

        project.afterEvaluate { _ ->
            if (submoduleExtension.publishAllowed.get()) {
                project.pluginManager.apply(PLUGIN_ID_MAVEN_PUBLISH)
                val publishing = project.extensions.getByType(PublishingExtension::class.java)
                publishConfig.repositoriesAction?.let { publishing.repositories(it) }
                applyPublications(project, publishConfig, publishing.publications)
            }
        }
    }

    protected fun registerPublication(
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
        publications.register(publicationName, MavenPublication::class.java) { mavenPublication ->
            mavenPublication.from(project.components.getByName(componentName))
            docsProvider?.let { mavenPublication.artifact(it.get()) }
            sourcesProvider?.let { mavenPublication.artifact(it.get()) }
            publishConfig.mavenPomAction?.let { mavenPublication.pom(it) }
        }
    }

    private fun configureKotlinTasks(project: Project, kotlinConfig: KotlinConfig) {
        val kotlinAndroidExt = project.extensions.findByType(KotlinAndroidProjectExtension::class.java)
        val kotlinJvmExt = project.extensions.findByType(KotlinJvmProjectExtension::class.java)

        val kotlinOptionsAction = Action<KotlinJvmOptions> { kotlinOptions ->
            kotlinConfig.allWarningsAsErrors?.let { kotlinOptions.allWarningsAsErrors = it }
            kotlinOptions.apiVersion = kotlinConfig.apiVersion
            kotlinConfig.freeCompilerArgs?.let { kotlinOptions.freeCompilerArgs = it }
            kotlinOptions.jvmTarget = kotlinConfig.jvmTarget
            kotlinOptions.languageVersion = kotlinConfig.languageVersion
            kotlinConfig.suppressWarnings?.let { kotlinOptions.suppressWarnings = it }
            kotlinConfig.useIR?.let { kotlinOptions.useIR = it }
            kotlinConfig.verbose?.let { kotlinOptions.verbose = it }
        }
        kotlinAndroidExt?.target?.compilations?.all {
            it.kotlinOptions {
                kotlinOptionsAction.execute(this)
            }
        }
        kotlinJvmExt?.target?.compilations?.all {
            it.kotlinOptions {
                kotlinOptionsAction.execute(this)
            }
        }
    }
}

abstract class SubmoduleExtension(project: Project) {
    var dokkaAllowed: Property<Boolean> = project.objects.property(Boolean::class.java).convention(true)
    var jacocoAllowed: Property<Boolean> = project.objects.property(Boolean::class.java).convention(true)
    var publishAllowed: Property<Boolean> = project.objects.property(Boolean::class.java).convention(true)
}
