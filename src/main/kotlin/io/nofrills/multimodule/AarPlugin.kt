package io.nofrills.multimodule

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.publish.PublicationContainer

class AarPlugin : AndroidPlugin() {
    override val androidPluginId: String = "com.android.library"

    override fun applyPublications(project: Project, publishConfig: PublishConfig, publications: PublicationContainer) {
        project.afterEvaluate {
            val libraryExtension = project.extensions.getByType(LibraryExtension::class.java)
            libraryExtension.libraryVariants.all { libraryVariant ->
                createPublicationForVariant(project, publishConfig, publications, libraryVariant)
            }
        }
    }
}
