package io.nofrills.multimodule

import com.android.build.gradle.TestedExtension
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.publish.maven.MavenPom
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

private const val MULTIMODULE_EXT_NAME = "multimodule"

class MultimodulePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        if (project.rootProject == project) {
            applyToRootProject(project)
        } else {
            error("Multimodule plugin should be applied to root project.")
        }
    }

    private fun applyToRootProject(project: Project) {
        project.extensions.create<MultimoduleExtension>(MULTIMODULE_EXT_NAME, MultimoduleExtension::class.java, project)
    }
}

abstract class MultimoduleExtension(project: Project) {
    internal var androidAction: Action<TestedExtension>? = null
    internal val javaConfig: JavaConfig = project.objects.newInstance(JavaConfig::class.java)
    internal var kotlinAction: Action<KotlinConfig>? = null
    internal var publishConfig: PublishConfig? = null

    fun android(action: Action<TestedExtension>) {
        androidAction = action
    }

    fun java(action: Action<JavaConfig>) {
        action.execute(javaConfig)
    }

    fun kotlin(action: Action<KotlinConfig>) {
        kotlinAction = action
    }

    fun publish(action: Action<PublishConfig>) {
        val config = publishConfig ?: PublishConfig().also {
            publishConfig = it
        }
        action.execute(config)
    }
}

open class KotlinConfig(private val kotlinJvmOptions: KotlinJvmOptions) : KotlinJvmOptions by kotlinJvmOptions {
    override var jvmTarget: String = "1.8"
}

open class JavaConfig {
    var sourceCompatibility = JavaVersion.VERSION_1_8
    var targetCompatibility = JavaVersion.VERSION_1_8
}

open class PublishConfig {
    internal var mavenPomAction: Action<MavenPom>? = null
    internal var repositoriesAction: Action<RepositoryHandler>? = null

    var withDocs: Boolean = false
    var withSources: Boolean = false

    fun mavenPom(action: Action<MavenPom>) {
        mavenPomAction = action
    }

    fun repositories(action: Action<RepositoryHandler>) {
        repositoriesAction = action
    }
}
