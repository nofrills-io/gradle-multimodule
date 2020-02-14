package io.nofrills.multimodule

import org.gradle.api.Plugin
import org.gradle.api.Project

class AarPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        applyAndroidPlugin("com.android.library", target)
    }
}
