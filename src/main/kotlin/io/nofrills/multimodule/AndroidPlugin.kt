package io.nofrills.multimodule

import com.android.build.gradle.TestedExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.tasks.bundling.Jar

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
                val sourcesJarTaskProvider by lazy {
                    project.tasks.register("${variant.name}SourcesJar", Jar::class.java) { jar ->
                        jar.from(variant.sourceSets.map { it.javaDirectories })
                        jar.archiveClassifier.set("sources")
                    }
                }

                createPublication(
                    project, publishConfig, publications, sourcesJarTaskProvider,
                    componentName = getComponentNameForVariant(variant),
                    publicationName = getPublicationNameForVariant(variant)
                )
            } else {
                project.logger.warn("Cannot create publication for ${project}: default publish config not found (check 'android.defaultPublishConfig' setting).")
            }
        }
    }
}
