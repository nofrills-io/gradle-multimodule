package io.nofrills.multimodule

import org.gradle.api.Project

class ApkPlugin : BasePlugin() {
    override fun apply(project: Project) {
        super.apply(project)
        project.applyAndroidPlugin("com.android.application", shouldApplyKotlin)
    }
}
