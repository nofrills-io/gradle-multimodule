package io.nofrills.multimodule

import org.junit.After
import java.io.File
import kotlin.test.Test

@Suppress("FunctionName")
class AndroidTests {
    companion object {
        const val missingCompileSdkVersion = "compileSdkVersion is not specified. Please add it to build.gradle"
    }

    @After
    fun tearDown() {
        File(FunctionalTestPath).deleteRecursively()
    }

    @Test
    fun `missing compileSdkVersion in aar`() {
        val (root, _) = makeTestProject(listOf("aar"))
        root.failGradle(":aar:check").assertContains(missingCompileSdkVersion)
    }

    @Test
    fun `missing compileSdkVersion in apk`() {
        val (root, _) = makeTestProject(listOf("apk"))
        root.failGradle(":aar:check").assertContains(missingCompileSdkVersion)
    }

    @Test
    fun `applies android plugins`() {
        val (_, subProjects) = makeTestProject(multimoduleContent = "android { compileSdkVersion(28) }")

        arrayOf("aar" to "library", "apk" to "application").forEach { (projectType, pluginName) ->
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
}