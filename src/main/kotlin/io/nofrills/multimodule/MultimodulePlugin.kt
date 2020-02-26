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
            applyToRootProject(project)
        } else {
            error("Multimodule plugin should be applied to root project.")
        }

        // configure dependencies
        // publishing via maven, gradle modules
        // jacoco, detekt etc.
    }

    private fun applyToRootProject(project: Project) {
        project.extensions.create<MultimoduleExtension>(MULTIMODULE_EXT_NAME, MultimoduleExtension::class.java, project)
    }
}

abstract class MultimoduleExtension(project: Project) {
    internal var androidAction: Action<TestedExtension>? = null
    internal val javaConfig: JavaConfig = project.objects.newInstance(JavaConfig::class.java)
    internal var kotlinAction: Action<KotlinConfig>? = null

    fun android(action: Action<TestedExtension>) {
        androidAction = action
    }

    fun java(action: Action<JavaConfig>) {
        action.execute(javaConfig)
    }

    fun kotlin(action: Action<KotlinConfig>) {
        kotlinAction = action
    }
}

open class KotlinConfig(private val kotlinJvmOptions: KotlinJvmOptions) : KotlinJvmOptions by kotlinJvmOptions {
    override var jvmTarget: String = "1.8"
}

open class JavaConfig {
    var sourceCompatibility = JavaVersion.VERSION_1_8
    var targetCompatibility = JavaVersion.VERSION_1_8
}
