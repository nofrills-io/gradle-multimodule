package io.nofrills.multimodule

import org.junit.After
import java.io.File
import kotlin.test.Test

@Suppress("FunctionName")
class AndroidActionTests {
    companion object {
        private const val missingCompileSdkVersion = "compileSdkVersion is not specified. Please add it to build.gradle"
        private val androidTypes = setOf(aar, apk)
    }

    @After
    fun tearDown() {
        File(FunctionalTestPath).deleteRecursively()
    }

    @Test
    fun `missing compileSdkVersion in aar`() {
        val (root, _) = makeTestProject(setOf(aar))
        root.failGradle(":aar:check").assertContains(missingCompileSdkVersion)
    }

    @Test
    fun `missing compileSdkVersion in apk`() {
        val (root, _) = makeTestProject(setOf(apk))
        root.failGradle(":aar:check").assertContains(missingCompileSdkVersion)
    }

    @Test
    fun `applies android plugins`() {
        val (_, subProjects) = makeTestProject(
            androidTypes,
            multimoduleContent = "android { compileSdkVersion(28) }"
        )

        arrayOf(aar to "library", apk to "application").forEach { (projectType, pluginName) ->
            val dir = subProjects.getValue(projectType)
            dir.resolve(BuildFile).appendText(
                """
                tasks.register("testCase") {
                    doLast {
                        val plugin = project.pluginManager.findPlugin("com.android.$pluginName")
                        val ext = project.extensions.getByName("android") as com.android.build.gradle.TestedExtension
                        println(plugin?.name)
                        println(ext)
                    }
                }
            """.trimIndent()
            )

            dir.runGradle("testCase")
                .assertContains(Regex("^$pluginName$", RegexOption.MULTILINE))
                .assertContains(Regex("^extension 'android'$", RegexOption.MULTILINE))
        }
    }

    @Test
    fun `android configuration is applied`() {
        val (_, subProjects) = makeTestProject(
            androidTypes,
            multimoduleContent = """
                android {
                    compileSdkVersion(28)
                    buildTypes { create("mock") }
                }
            """.trimIndent()
        )

        androidTypes.forEach { projectType ->
            val dir = subProjects.getValue(projectType)
            dir.runGradle("tasks", "--all").assertContains(Regex("^assembleMock ", RegexOption.MULTILINE))
        }
    }
}