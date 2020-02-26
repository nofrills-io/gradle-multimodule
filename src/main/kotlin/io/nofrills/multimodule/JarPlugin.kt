package io.nofrills.multimodule

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension

class JarPlugin : BasePlugin() {
    override fun apply(project: Project) {
        super.apply(project)

        project.plugins.apply("java-library")
        project.extensions.getByType(JavaPluginExtension::class.java).apply {
            sourceCompatibility = multimoduleExtensionInstance.javaConfig.sourceCompatibility
            targetCompatibility = multimoduleExtensionInstance.javaConfig.targetCompatibility
        }

        if (shouldApplyKotlin) {
            project.plugins.apply("org.jetbrains.kotlin.jvm")
            project.configureKotlinTasks(multimoduleExtensionInstance)
        }
    }
}
