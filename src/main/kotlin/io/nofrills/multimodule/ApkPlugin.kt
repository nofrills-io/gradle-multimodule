package io.nofrills.multimodule

import com.android.build.gradle.AppExtension
import org.gradle.api.Project
import org.gradle.api.publish.PublicationContainer

class ApkPlugin : AndroidPlugin() {
    override val androidPluginId: String = "com.android.application"

    override fun applyPublications(project: Project, publishConfig: PublishConfig, publications: PublicationContainer) {
        project.afterEvaluate {
            val appExtension = project.extensions.getByType(AppExtension::class.java)
            appExtension.applicationVariants.all { appVariant ->
                createPublicationForVariant(project, publishConfig, publications, appVariant)
            }
        }
    }
}
