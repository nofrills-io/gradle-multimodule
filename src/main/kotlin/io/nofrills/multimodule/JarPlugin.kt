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

import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin.CHECK_TASK_NAME
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.dokka.gradle.DokkaTask
import java.io.File

class JarPlugin : BasePlugin() {
    override fun applyJacoco(project: Project, jacocoConfig: JacocoConfig) {
        project.pluginManager.apply(PLUGIN_ID_JACOCO)
        project.tasks.withType(JacocoReport::class.java) { jacoco ->
            jacoco.dependsOn(project.tasks.withType(Test::class.java))
            jacoco.reports {
                it.html.isEnabled = true
                it.xml.isEnabled = true
            }
            jacocoConfig.jacocoTaskAction?.execute(jacoco)
        }
        jacocoConfig.jacocoPluginAction?.execute(project.extensions.getByType(JacocoPluginExtension::class.java))
        project.tasks.named(CHECK_TASK_NAME) {
            it.dependsOn(project.tasks.withType(JacocoReport::class.java))
        }
    }

    override fun applyKotlin(project: Project, kotlinConfig: KotlinConfig) {
        project.pluginManager.apply(PLUGIN_ID_KOTLIN_JVM)
    }

    override fun applyPlugin(project: Project, multimoduleExtension: MultimoduleExtension) {
        project.pluginManager.apply(PLUGIN_ID_JAVA_LIBRARY)

        val javaConfig = JavaConfig(project)
        multimoduleExtension.javaAction?.execute(javaConfig)

        project.extensions.getByType(JavaPluginExtension::class.java).apply {
            sourceCompatibility = javaConfig.sourceCompatibility
            targetCompatibility = javaConfig.targetCompatibility
        }
    }

    override fun applyPublications(project: Project, publishConfig: PublishConfig, publications: PublicationContainer) {
        val docsJarTaskProvider = lazy { getDocsJarTaskProvider(project) }
        val sourcesJarTaskProvider = lazy { getSourcesJarTaskProvider(project) }
        registerPublication(
            project, publishConfig, publications,
            lazyDocsJarTask = docsJarTaskProvider,
            lazySourcesJarTask = sourcesJarTaskProvider,
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
            dokka.group = "documentation"
            dokka.outputDirectory = File(project.buildDir, "dokka").path
            dokka.outputFormat = DOKKA_FORMAT
        }

        return project.tasks.register("dokkaJar", Jar::class.java) { jar ->
            jar.group = "documentation"
            jar.from(dokkaTaskProvider.get())
            jar.archiveClassifier.set("javadoc")
        }
    }

    private fun getJavadocJarTaskProvider(project: Project): TaskProvider<Jar> {
        return project.tasks.register("javadocJar", Jar::class.java) { jar ->
            val javadocTaskProvider = project.tasks.withType(Javadoc::class.java)
            jar.group = "documentation"
            jar.from(javadocTaskProvider)
            jar.archiveClassifier.set("javadoc")
        }
    }

    private fun getSourcesJarTaskProvider(project: Project): TaskProvider<Jar> {
        return project.tasks.register("sourcesJar", Jar::class.java) { jar ->
            val sourceSets = project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets
            jar.from(sourceSets.getByName(MAIN_SOURCE_SET_NAME).allSource)
            jar.archiveClassifier.set("sources")
        }
    }
}
