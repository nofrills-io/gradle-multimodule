package io.nofrills.multimodule

import com.android.build.gradle.TestedExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar

abstract class AndroidPlugin : BasePlugin() {
    protected abstract val androidPluginId: String

    override fun applyKotlin(project: Project, kotlinConfigAction: Action<KotlinConfig>) {
        project.plugins.apply("org.jetbrains.kotlin.android")
        project.configureKotlinTasks(kotlinConfigAction)
        project.extensions.getByType(TestedExtension::class.java).apply {
            sourceSets {
                it.getByName("androidTest").java.srcDir("src/androidTest/kotlin")
                it.getByName("main").java.srcDir("src/main/kotlin")
                it.getByName("test").java.srcDir("src/test/kotlin")
            }
        }
    }

    override fun applyPlugin(project: Project, multimoduleExtension: MultimoduleExtension) {
        project.plugins.apply(androidPluginId)

        project.extensions.getByType(TestedExtension::class.java).apply {
            compileOptions {
                it.sourceCompatibility = multimoduleExtension.javaConfig.sourceCompatibility
                it.targetCompatibility = multimoduleExtension.javaConfig.targetCompatibility
            }
            multimoduleExtension.androidAction?.execute(this)
        }
    }

    protected fun createPublicationForVariant(project: Project, publishConfig: PublishConfig, publications: PublicationContainer, variant: BaseVariant) {
        val sourcesJarTaskProvider by lazy {
            project.tasks.register("${variant.name}SourcesJar", Jar::class.java) { jar ->
                jar.from(variant.sourceSets.map { it.javaDirectories })
                jar.archiveClassifier.set("sources")
            }
        }

        publications.create(variant.name, MavenPublication::class.java) { mavenPublication ->
            mavenPublication.from(project.components.getByName(variant.name))
            if (publishConfig.withSources) {
                mavenPublication.artifact(sourcesJarTaskProvider.get())
            }
            publishConfig.mavenPom?.let { mavenPublication.pom(it) }
        }
    }
}
