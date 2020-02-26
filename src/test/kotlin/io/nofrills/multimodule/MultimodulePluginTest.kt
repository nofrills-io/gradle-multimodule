/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package io.nofrills.multimodule

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MultimodulePluginTest {
    @Test
    fun `plugin registers extension`() {
        val rootProject = ProjectBuilder.builder().withName("root").build()
        rootProject.plugins.apply("io.nofrills.multimodule")
        assertNotNull(rootProject.extensions.findByName("multimodule"))
    }

    @Test
    fun `apply aar plugin`() {
        val rootProject = ProjectBuilder.builder().withName("root").build()
        val project = ProjectBuilder.builder().withName("sub-project").withParent(rootProject).build()
        rootProject.plugins.apply("io.nofrills.multimodule")
        project.plugins.apply("io.nofrills.multimodule.aar")

        assertNotNull(project.plugins.findPlugin("com.android.library"))
    }

    @Test
    fun `apply apk plugin`() {
        val rootProject = ProjectBuilder.builder().withName("root").build()
        val project = ProjectBuilder.builder().withName("sub-project").withParent(rootProject).build()
        rootProject.plugins.apply("io.nofrills.multimodule")
        project.plugins.apply("io.nofrills.multimodule.apk")

        assertNotNull(project.plugins.findPlugin("com.android.application"))
    }

    @Test
    fun `apply jar plugin`() {
        val rootProject = ProjectBuilder.builder().withName("root").build()
        val project = ProjectBuilder.builder().withName("sub-project").withParent(rootProject).build()
        rootProject.plugins.apply("io.nofrills.multimodule")
        project.plugins.apply("io.nofrills.multimodule.jar")

        assertNull(project.plugins.findPlugin("com.android.application"))
        assertNull(project.plugins.findPlugin("com.android.library"))
        assertNotNull(project.plugins.findPlugin("java-library"))
    }
}