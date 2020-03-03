package io.nofrills.multimodule

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

abstract class BasePlugin : Plugin<Project> {
    companion object {
        internal const val PLUGIN_ID_ANDROID_APP = "com.android.application"
        internal const val PLUGIN_ID_ANDROID_LIBRARY = "com.android.library"
        internal const val PLUGIN_ID_JAVA_LIBRARY = "java-library"
        internal const val PLUGIN_ID_KOTLIN_ANDROID = "org.jetbrains.kotlin.android"
        internal const val PLUGIN_ID_KOTLIN_JVM = "org.jetbrains.kotlin.jvm"
        private const val PLUGIN_ID_MAVEN_PUBLISH = "maven-publish"
    }

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
        val multimoduleExtension = project.rootProject.extensions.getByType(MultimoduleExtension::class.java)

        applyPlugin(project, multimoduleExtension)

        multimoduleExtension.kotlinAction?.let { kotlinConfigAction ->
            applyKotlin(project, kotlinConfigAction)
        }

        multimoduleExtension.publishConfig?.let { publishConfig ->
            project.plugins.apply(PLUGIN_ID_MAVEN_PUBLISH)
            val publishing = project.extensions.getByType(PublishingExtension::class.java)
            publishConfig.repositoriesAction?.let { publishing.repositories(it) }
            applyPublications(project, publishConfig, publishing.publications)
        }
    }

    protected fun createPublication(
        project: Project,
        publishConfig: PublishConfig,
        publications: PublicationContainer,
        sourcesJarTaskProvider: TaskProvider<Jar>,
        componentName: String,
        publicationName: String
    ) {
        publications.create(publicationName, MavenPublication::class.java) { mavenPublication ->
            mavenPublication.from(project.components.getByName(componentName))
            if (publishConfig.withSources) {
                mavenPublication.artifact(sourcesJarTaskProvider.get())
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
