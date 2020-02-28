package io.nofrills.multimodule

import com.android.build.gradle.TestedExtension
import org.gradle.api.Action
import org.gradle.api.Project

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
}
