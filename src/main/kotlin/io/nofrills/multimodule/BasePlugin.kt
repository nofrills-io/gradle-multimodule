package io.nofrills.multimodule

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.PublishingExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

abstract class BasePlugin : Plugin<Project> {
    protected abstract fun applyKotlin(project: Project, kotlinConfigAction: Action<KotlinConfig>)
    protected abstract fun applyPlugin(project: Project, multimoduleExtension: MultimoduleExtension)
    protected abstract fun applyPublications(project: Project, publishConfig: PublishConfig, publications: PublicationContainer)

    final override fun apply(project: Project) {
        val multimoduleExtension = project.rootProject.extensions.getByType(MultimoduleExtension::class.java)
        applyPlugin(project, multimoduleExtension)
        multimoduleExtension.kotlinAction?.let { kotlinConfigAction ->
            applyKotlin(project, kotlinConfigAction)
        }
        multimoduleExtension.publishConfig?.let { publishConfig ->
            project.plugins.apply("maven-publish")
            val publishing = project.extensions.getByType(PublishingExtension::class.java)
            publishConfig.repositories?.let { publishing.repositories(it) }
            applyPublications(project, publishConfig, publishing.publications)
        }
    }

    internal fun Project.configureKotlinTasks(kotlinConfigAction: Action<KotlinConfig>) {
        project.tasks.withType(KotlinCompile::class.java).configureEach {
            val kotlinConfig = KotlinConfig(it.kotlinOptions)
            kotlinConfigAction.execute(kotlinConfig)
        }
    }
}
