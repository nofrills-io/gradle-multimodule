package io.nofrills.multimodule

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.tasks.bundling.Jar

class JarPlugin : BasePlugin() {
    override fun applyKotlin(project: Project, kotlinConfigAction: Action<KotlinConfig>) {
        project.plugins.apply(PLUGIN_ID_KOTLIN_JVM)
        project.configureKotlinTasks(kotlinConfigAction)
    }

    override fun applyPlugin(project: Project, multimoduleExtension: MultimoduleExtension) {
        project.plugins.apply(PLUGIN_ID_JAVA_LIBRARY)
        project.extensions.getByType(JavaPluginExtension::class.java).apply {
            sourceCompatibility = multimoduleExtension.javaConfig.sourceCompatibility
            targetCompatibility = multimoduleExtension.javaConfig.targetCompatibility
        }
    }

    override fun applyPublications(project: Project, publishConfig: PublishConfig, publications: PublicationContainer) {
        val sourcesJarTaskProvider by lazy {
            project.tasks.register("sourcesJar", Jar::class.java) { jar ->
                val sourceSets = project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets
                jar.from(sourceSets.getByName("main").allJava)
                jar.archiveClassifier.set("sources")
            }
        }

        createPublication(
            project, publishConfig, publications, sourcesJarTaskProvider,
            componentName = "java",
            publicationName = "jar"
        )
    }
}
