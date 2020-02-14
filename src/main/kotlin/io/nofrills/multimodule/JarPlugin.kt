package io.nofrills.multimodule

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension

class JarPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val commonConfig = target.rootProject.extensions.getByType(MultimoduleExtension::class.java)

        target.plugins.apply("java-library")
        target.plugins.apply("org.jetbrains.kotlin.jvm")

        target.extensions.getByType(JavaPluginExtension::class.java).apply {
            sourceCompatibility = commonConfig.javaConfig.sourceCompatibility
            targetCompatibility = commonConfig.javaConfig.targetCompatibility
        }

        configureKotlinTasks(target, commonConfig)
    }
}
