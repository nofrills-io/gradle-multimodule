package io.nofrills.multimodule

import org.gradle.api.Project

class AarPlugin : BasePlugin() {
    override fun apply(project: Project) {
        super.apply(project)
        project.applyAndroidPlugin("com.android.library", shouldApplyKotlin)
    }
}
