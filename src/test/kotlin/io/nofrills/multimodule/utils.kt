package io.nofrills.multimodule

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

/** Returns a pair of (root project; library project),
 * @param libProjectType One of "aar", "apk" or "jar"
 */
internal fun testProjects(libProjectType: String): Pair<Project, Project> {
    val rootProject = ProjectBuilder.builder().withName("root").build()
    val project = ProjectBuilder.builder().withName("lib").withParent(rootProject).build()
    rootProject.plugins.apply("io.nofrills.multimodule")
    project.plugins.apply("io.nofrills.multimodule.$libProjectType")
    return rootProject to project
}
