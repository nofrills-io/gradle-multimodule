package io.nofrills.multimodule

import org.gradle.api.Project
import org.gradle.api.publish.PublicationContainer

class ApkPlugin : AndroidPlugin() {
    override val androidPluginId: String = "com.android.application"

    override fun applyPublications(project: Project, publishConfig: PublishConfig, publications: PublicationContainer) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
