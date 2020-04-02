/*
 *    Copyright 2020 Mateusz Armatys
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.nofrills.multimodule

import com.android.build.gradle.TestedExtension
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.MutableVersionConstraint
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.publish.maven.MavenPom
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import java.io.File

class MultimodulePlugin : Plugin<Project> {
    companion object {
        private const val DOKKA_FORMAT = "html"
        private const val MULTIMODULE_EXT_NAME = "multimodule"
    }

    override fun apply(project: Project) {
        if (project.rootProject == project) {
            applyToRootProject(project)
        } else {
            error("Multimodule plugin should be applied to root project.")
        }
    }

    private fun applyToRootProject(project: Project) {
        project.extensions.create(MULTIMODULE_EXT_NAME, MultimoduleExtension::class.java)
        applyGlobalDokka(project)
    }

    private fun applyGlobalDokka(project: Project) {
        project.gradle.projectsEvaluated {
            val ext = project.extensions.getByType(MultimoduleExtension::class.java)

            val action = ext.dokkaAction
            if (action != null) {
                project.pluginManager.apply(BasePlugin.PLUGIN_ID_DOKKA)

                val jdkVersion by lazy {
                    val javaConfig = JavaConfig(project)
                    ext.javaAction?.execute(javaConfig)
                    javaConfig.sourceCompatibility.ordinal + 1
                }

                project.tasks.named("dokka", DokkaTask::class.java) { dokka ->
                    dokka.outputDirectory = File(project.buildDir, "dokka").path
                    dokka.outputFormat = DOKKA_FORMAT
                    dokka.configuration.jdkVersion = jdkVersion

                    val kotlinProjects = project.subprojects.filter {
                        val submoduleExtension = BasePlugin.getSubmoduleExtension(it)
                        it.getKotlinPluginVersion() != null && (submoduleExtension?.dokkaAllowed?.get() ?: false)
                    }
                    dokka.subProjects = kotlinProjects.map { it.name }

                    action.execute(dokka)
                }
            }
        }
    }
}

open class MultimoduleExtension {
    internal var androidAction: Action<TestedExtension>? = null
    internal var dokkaAction: Action<DokkaTask>? = null
    internal var jacocoAction: Action<JacocoConfig>? = null
    internal var javaAction: Action<JavaConfig>? = null
    internal var kotlinAction: Action<KotlinConfig>? = null
    internal var publishAction: Action<PublishConfig>? = null

    internal lateinit var activeProject: ThreadLocal<Project>

    val TestedExtension.project: Project
        get() = activeProject.get()

    fun android(action: Action<TestedExtension>) {
        androidAction = action
    }

    fun dokka(action: Action<DokkaTask>) {
        dokkaAction = action
    }

    fun jacoco(action: Action<JacocoConfig>) {
        jacocoAction = action
    }

    fun java(action: Action<JavaConfig>) {
        javaAction = action
    }

    fun kotlin(action: Action<KotlinConfig>) {
        kotlinAction = action
    }

    fun publish(action: Action<PublishConfig>) {
        publishAction = action
    }
}

class JacocoConfig(val project: Project) {
    internal var jacocoPluginAction: Action<JacocoPluginExtension>? = null
    internal var jacocoTaskAction: Action<JacocoReport>? = null

    fun jacocoPlugin(action: Action<JacocoPluginExtension>) {
        jacocoPluginAction = action
    }

    fun jacocoTask(action: Action<JacocoReport>) {
        jacocoTaskAction = action
    }
}

class JavaConfig(val project: Project) {
    var sourceCompatibility = JavaVersion.VERSION_1_8
    var targetCompatibility = JavaVersion.VERSION_1_8
}

class KotlinConfig(val project: Project) {
    var androidExtensions: Boolean = false
    var coroutines: Boolean = false
    var coroutinesVersion: Action<MutableVersionConstraint> = Action { it.prefer("1.3.5") }
    var kapt: Boolean = false
    var reflect: Boolean = false
    var stdLib: Boolean = true

    // options corresponding to [org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions]
    var allWarningsAsErrors: Boolean? = null
    var apiVersion: String? = null
    var freeCompilerArgs: List<String>? = null
    var jvmTarget: String = "1.8"
    var languageVersion: String? = null
    var suppressWarnings: Boolean? = null
    var useIR: Boolean? = null
    var verbose: Boolean? = null
}

class PublishConfig(val project: Project) {
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
