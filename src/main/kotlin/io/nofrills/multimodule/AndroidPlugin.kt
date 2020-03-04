package io.nofrills.multimodule

import com.android.build.gradle.TestedExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.jetbrains.dokka.gradle.DokkaTask
import java.io.File

abstract class AndroidPlugin : BasePlugin() {
    protected abstract val androidPluginId: String

    protected abstract fun getComponentNameForVariant(variant: BaseVariant): String
    protected abstract fun getDefaultPublishVariant(project: Project): BaseVariant?
    protected abstract fun getPublicationNameForVariant(variant: BaseVariant): String

    final override fun applyKotlin(project: Project, kotlinConfigAction: Action<KotlinConfig>) {
        project.plugins.apply(PLUGIN_ID_KOTLIN_ANDROID)
        project.configureKotlinTasks(kotlinConfigAction)
        project.extensions.getByType(TestedExtension::class.java).apply {
            sourceSets {
                it.getByName("androidTest").java.srcDir("src/androidTest/kotlin")
                it.getByName("main").java.srcDir("src/main/kotlin")
                it.getByName("test").java.srcDir("src/test/kotlin")
            }
        }
    }

    final override fun applyPlugin(project: Project, multimoduleExtension: MultimoduleExtension) {
        project.plugins.apply(androidPluginId)

        project.extensions.getByType(TestedExtension::class.java).apply {
            compileOptions {
                it.sourceCompatibility = multimoduleExtension.javaConfig.sourceCompatibility
                it.targetCompatibility = multimoduleExtension.javaConfig.targetCompatibility
            }
            multimoduleExtension.androidAction?.execute(this)
        }
    }

    final override fun applyPublications(
        project: Project,
        publishConfig: PublishConfig,
        publications: PublicationContainer
    ) {
        project.afterEvaluate {
            val variant = getDefaultPublishVariant(project)
            if (variant != null) {
                val docsJarTaskProvider = lazy { getDocsJarTaskProvider(project, variant) }
                val sourcesJarTaskProvider = lazy { getSourcesJarTaskProvider(project, variant) }

                createPublication(
                    project, publishConfig, publications,
                    docsJarTaskProvider = docsJarTaskProvider,
                    sourcesJarTaskProvider = sourcesJarTaskProvider,
                    componentName = getComponentNameForVariant(variant),
                    publicationName = getPublicationNameForVariant(variant)
                )
            } else {
                project.logger.warn("Cannot create publication for ${project}: default publish config not found (check 'android.defaultPublishConfig' setting).")
            }
        }
    }

    private fun getDocsJarTaskProvider(project: Project, variant: BaseVariant): TaskProvider<Jar> {
        return if (project.plugins.hasPlugin(PLUGIN_ID_KOTLIN_ANDROID)) {
            getDokkaJarTaskProvider(project, variant)
        } else {
            getJavadocJarTaskProvider(project, variant)
        }
    }

    private fun getDokkaJarTaskProvider(project: Project, variant: BaseVariant): TaskProvider<Jar> {
        project.plugins.apply(PLUGIN_ID_DOKKA)

        val dokkaTaskProvider = project.tasks.named(TASK_NAME_DOKKA, DokkaTask::class.java) {
            it.configuration.androidVariants = listOf(variant.name)
            it.outputDirectory = File(project.buildDir, "dokka").path
            it.outputFormat = "javadoc"
        }

        return project.tasks.register("${variant.name}DokkaJar", Jar::class.java) { jar ->
            jar.from(dokkaTaskProvider.get())
            jar.archiveClassifier.set("javadoc")
        }
    }

    private fun getJavadocJarTaskProvider(project: Project, variant: BaseVariant): TaskProvider<Jar> {
        val javadocProvider = project.tasks.register("${variant.name}Javadoc", Javadoc::class.java) { javadoc ->
            javadoc.source(variant.sourceSets.map { it.javaDirectories })
            javadoc.classpath += project.files(project.extensions.getByType(TestedExtension::class.java).bootClasspath)
            javadoc.classpath += variant.javaCompileProvider.get().classpath
        }
        return project.tasks.register("${variant.name}JavadocJar", Jar::class.java).apply {
            configure { jar ->
                jar.dependsOn(javadocProvider)
                jar.archiveClassifier.set("javadoc")
                jar.from(javadocProvider.get().destinationDir)
            }
        }
    }

    private fun getSourcesJarTaskProvider(project: Project, variant: BaseVariant): TaskProvider<Jar> {
        return project.tasks.register("${variant.name}SourcesJar", Jar::class.java) { jar ->
            jar.from(variant.sourceSets.map { it.javaDirectories })
            jar.archiveClassifier.set("sources")
        }
    }
}
