package io.nofrills.multimodule

import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class BasePlugin : Plugin<Project> {
    protected lateinit var multimoduleExtensionInstance: MultimoduleExtension
    protected var shouldApplyKotlin: Boolean = false

    override fun apply(project: Project) {
        multimoduleExtensionInstance = project.rootProject.extensions.getByType(MultimoduleExtension::class.java)
        shouldApplyKotlin = multimoduleExtensionInstance.kotlinAction != null
    }
}