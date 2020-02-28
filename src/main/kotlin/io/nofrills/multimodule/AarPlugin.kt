package io.nofrills.multimodule

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.maven.MavenPublication

class AarPlugin : AndroidPlugin() {
    override val androidPluginId: String = "com.android.library"

    override fun applyPublications(project: Project, publishConfig: PublishConfig, publications: PublicationContainer) {
        project.afterEvaluate { _ ->
            val libraryExtension = project.extensions.getByType(LibraryExtension::class.java)
            libraryExtension.libraryVariants.all { libraryVariant ->
                publications.create("${project.name}${libraryVariant.name.capitalize()}", MavenPublication::class.java) { mavenPublication ->
                    // TODO
                    mavenPublication.from(project.components.getByName(libraryVariant.name))
//                    mavenPublication.artifact(project.tasks.getByName("bundle${libraryVariant.name.capitalize()}Aar"))
                    // TODO
//                if (publishConfig.withDocs) {
//                    mavenPublication.artifact(javadocJarTaskProvider.get())
//                }
                    // TODO
//                if (publishConfig.withSources) {
//                    mavenPublication.artifact(sourcesJarTaskProvider.get())
//                }
                    publishConfig.mavenPom?.let { mavenPublication.pom(it) }
                }
            }
        }
    }
}
