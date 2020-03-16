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

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.dokka.gradle.DokkaTask
import java.io.File

class JarPlugin : BasePlugin() {
    override fun applyJacoco(project: Project, jacocoAction: Action<JacocoReport>) {
        project.pluginManager.apply(PLUGIN_ID_JACOCO)
        project.tasks.withType(JacocoReport::class.java) { jacoco ->
            jacoco.dependsOn(project.tasks.withType(Test::class.java))
            jacoco.reports {
                it.html.isEnabled = true
                it.xml.isEnabled = true
            }
            jacocoAction.execute(jacoco)
        }
        project.tasks.named("check") {
            it.dependsOn(project.tasks.withType(JacocoReport::class.java))
        }
    }

    override fun applyKotlin(project: Project, kotlinConfigAction: Action<KotlinConfig>) {
        project.pluginManager.apply(PLUGIN_ID_KOTLIN_JVM)
        project.configureKotlinTasks(kotlinConfigAction)
    }

    override fun applyPlugin(project: Project, multimoduleExtension: MultimoduleExtension) {
        project.pluginManager.apply(PLUGIN_ID_JAVA_LIBRARY)
        project.extensions.getByType(JavaPluginExtension::class.java).apply {
            sourceCompatibility = multimoduleExtension.javaConfig.sourceCompatibility
            targetCompatibility = multimoduleExtension.javaConfig.targetCompatibility
        }
    }

    override fun applyPublications(project: Project, publishConfig: PublishConfig, publications: PublicationContainer) {
        val docsJarTaskProvider = lazy { getDocsJarTaskProvider(project) }
        val sourcesJarTaskProvider = lazy { getSourcesJarTaskProvider(project) }
        createPublication(
            project, publishConfig, publications,
            docsJarTask = docsJarTaskProvider,
            sourcesJarTask = sourcesJarTaskProvider,
            componentName = "java",
            publicationName = "jar"
        )
    }

    private fun getDocsJarTaskProvider(project: Project): TaskProvider<Jar> {
        return if (project.plugins.hasPlugin(PLUGIN_ID_KOTLIN_JVM)) {
            getDokkaJarTaskProvider(project)
        } else {
            getJavadocJarTaskProvider(project)
        }
    }

    private fun getDokkaJarTaskProvider(project: Project): TaskProvider<Jar> {
        project.pluginManager.apply(PLUGIN_ID_DOKKA)

        val dokkaTaskProvider = project.tasks.named(TASK_NAME_DOKKA, DokkaTask::class.java) { dokka ->
            dokka.outputDirectory = File(project.buildDir, "dokka").path
            dokka.outputFormat = DOKKA_FORMAT
        }

        return project.tasks.register("dokkaJar", Jar::class.java) { jar ->
            jar.from(dokkaTaskProvider.get())
            jar.archiveClassifier.set("javadoc")
        }
    }

    private fun getJavadocJarTaskProvider(project: Project): TaskProvider<Jar> {
        return project.tasks.register("javadocJar", Jar::class.java) { jar ->
            val javadocTaskProvider = project.tasks.withType(Javadoc::class.java)
            jar.from(javadocTaskProvider)
            jar.archiveClassifier.set("javadoc")
        }
    }

    private fun getSourcesJarTaskProvider(project: Project): TaskProvider<Jar> {
        return project.tasks.register("sourcesJar", Jar::class.java) { jar ->
            val sourceSets = project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets
            jar.from(sourceSets.getByName("main").allSource)
            jar.archiveClassifier.set("sources")
        }
    }
}
