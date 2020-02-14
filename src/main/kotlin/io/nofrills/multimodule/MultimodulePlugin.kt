package io.nofrills.multimodule

import com.android.build.gradle.TestedExtension
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

private const val MULTIMODULE_EXT_NAME = "multimodule"

class MultimodulePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        if (project.rootProject == project) {
            project.extensions.create<MultimoduleExtension>(MULTIMODULE_EXT_NAME, MultimoduleExtension::class.java, project)
        } else {
            error("Multimodule plugin should be applied to root project.")
        }

        // kotlin version + compiler config
        // publishing via maven, gradle modules
        // include kotlin std lib t(default)/f
        // include common libraries (dagger, retrofit etc)
        // manage version numbers

        // jacoco, detekt etc.

        // Write it as a plugin, which will be basically the only plugin to apply.
        // Then, it can be configured so that all modules have the same basic config.
        // So the config should be a one-time, global for the whole multi-module project.
        // So the idea is, you apply the plugin, and then you may only need to add the dependencies that are needed by a specific module.
        // In the end, creating a new module is very simple.

        // configuration should be global (e.g. in the root build.gradle)
        // but the plugin should be applied to each submodule separately
    }
}

abstract class MultimoduleExtension(project: Project) {
    internal var androidAction: Action<TestedExtension>? = null
    internal val javaConfig: SimpleJavaConfig = project.objects.newInstance(SimpleJavaConfig::class.java)
    internal var kotlinAction: Action<KotlinJvmOptions>? = null

    fun android(action: Action<TestedExtension>) {
        androidAction = action
    }

    fun java(action: Action<SimpleJavaConfig>) {
        action.execute(javaConfig)
    }

    fun kotlin(action: Action<KotlinJvmOptions>) {
        kotlinAction = action
    }
}

open class SimpleJavaConfig {
    var sourceCompatibility = JavaVersion.VERSION_1_8
    var targetCompatibility = JavaVersion.VERSION_1_8
}
