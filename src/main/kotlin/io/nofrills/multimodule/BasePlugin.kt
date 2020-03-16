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
import org.gradle.api.provider.Property
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

abstract class BasePlugin : Plugin<Project> {
    companion object {
        internal const val PLUGIN_ID_ANDROID_APP = "com.android.application"
        internal const val PLUGIN_ID_ANDROID_LIBRARY = "com.android.library"
        internal const val PLUGIN_ID_DOKKA = "org.jetbrains.dokka"
        internal const val PLUGIN_ID_JACOCO = "jacoco"
        internal const val PLUGIN_ID_JAVA_LIBRARY = "java-library"
        internal const val PLUGIN_ID_KOTLIN_ANDROID = "org.jetbrains.kotlin.android"
        internal const val PLUGIN_ID_KOTLIN_JVM = "org.jetbrains.kotlin.jvm"
        private const val PLUGIN_ID_MAVEN_PUBLISH = "maven-publish"

        private const val SUBMODULE_EXT_NAME = "submodule"

        internal const val DOKKA_FORMAT = "html"
        internal const val TASK_NAME_DOKKA = "dokka"

        internal fun getSubmoduleExtension(project: Project): SubmoduleExtension? {
            return project.extensions.findByType(SubmoduleExtension::class.java)
        }
    }

    /** Apply the jacoco plugin. */
    protected abstract fun applyJacoco(project: Project, jacocoAction: Action<JacocoReport>)

    /** Apply the kotlin plugin appropriate for the module. */
    protected abstract fun applyKotlin(project: Project, kotlinConfigAction: Action<KotlinConfig>)

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

        multimoduleExtension.kotlinAction?.let {
            applyKotlin(project, it)
        }

        multimoduleExtension.jacocoAction?.let { jacocoAction ->
            project.afterEvaluate { project ->
                if (submoduleExtension.jacocoAllowed.get()) {
                    applyJacoco(project, jacocoAction)
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
        docsJarTask: Lazy<TaskProvider<Jar>>,
        sourcesJarTask: Lazy<TaskProvider<Jar>>,
        componentName: String,
        publicationName: String
    ) {
        publications.create(publicationName, MavenPublication::class.java) { mavenPublication ->
            mavenPublication.from(project.components.getByName(componentName))
            if (publishConfig.withDocs) {
                mavenPublication.artifact(docsJarTask.value.get())
            }
            if (publishConfig.withSources) {
                mavenPublication.artifact(sourcesJarTask.value.get())
            }
            publishConfig.mavenPomAction?.let { mavenPublication.pom(it) }
        }
    }

    protected fun Project.configureKotlinTasks(kotlinConfigAction: Action<KotlinConfig>) {
        project.tasks.withType(KotlinCompile::class.java).configureEach {
            val kotlinConfig = KotlinConfig(it.kotlinOptions)
            kotlinConfigAction.execute(kotlinConfig)
        }
    }
}

abstract class SubmoduleExtension(project: Project) {
    var dokkaAllowed: Property<Boolean> = project.objects.property(Boolean::class.java).convention(true)
    var jacocoAllowed: Property<Boolean> = project.objects.property(Boolean::class.java).convention(true)
    var publishAllowed: Property<Boolean> = project.objects.property(Boolean::class.java).convention(true)
}