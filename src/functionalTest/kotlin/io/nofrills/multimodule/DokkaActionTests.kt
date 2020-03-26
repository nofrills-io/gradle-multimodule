package io.nofrills.multimodule

import kotlin.test.Test

@Suppress("FunctionName")
class DokkaActionTests : BaseActionTest() {
    @Test
    fun `applies dokka but skips sub-projects`() {
        val (root, _) = makeTestProject(
            multimoduleContent = """
            $baseAndroidConfig
            dokka {}
        """.trimIndent()
        )
        root.resolve(BuildFile).appendText(
            """
            tasks.register("testCase") {
                val dokkaTask = project.tasks.getByName("dokka") as org.jetbrains.dokka.gradle.DokkaTask
                println(dokkaTask.subProjects)
            }
        """.trimIndent()
        )
        root.runGradle("testCase").assertLine("^\\[]$") // sub-project is included only if it has a kotlin plugin
        root.runGradle("tasks").assertLine("^dokka ")
    }

    @Test
    fun `applies dokka`() {
        val (root, _) = makeTestProject(
            multimoduleContent = """
            $baseAndroidConfig
            dokka {}
            kotlin {}
        """.trimIndent()
        )
        root.resolve(BuildFile).appendText(
            """
            tasks.register("testCase") {
                val dokkaTask = project.tasks.getByName("dokka") as org.jetbrains.dokka.gradle.DokkaTask
                println(dokkaTask.subProjects)
            }
        """.trimIndent()
        )
        root.runGradle("testCase").assertContains("[aar, apk, jar]")
        root.runGradle("tasks").assertLine("^dokka ")
    }

    @Test
    fun `dokka config applied`() {
        val (root, _) = makeTestProject(
            multimoduleContent = """
            $baseAndroidConfig
            dokka {
                configuration {
                    moduleName = "hello"
                }
            }
        """.trimIndent()
        )
        root.resolve(BuildFile).appendText(
            """
            tasks.register("testCase") {
                val dokkaTask = project.tasks.getByName("dokka") as org.jetbrains.dokka.gradle.DokkaTask
                println(dokkaTask.configuration.moduleName)
            }
        """.trimIndent()
        )
        root.runGradle("testCase").assertLine("^hello$")
    }

    @Test
    fun `dokka not allowed`() {
        val (root, _) = makeTestProject(
            multimoduleContent = """
            $baseAndroidConfig
            dokka {}
            kotlin {}
        """.trimIndent(),
            submoduleContent = "dokkaAllowed.set(false)"
        )
        root.resolve(BuildFile).appendText(
            """
            tasks.register("testCase") {
                val dokkaTask = project.tasks.getByName("dokka") as org.jetbrains.dokka.gradle.DokkaTask
                println(dokkaTask.subProjects)
            }
        """.trimIndent()
        )
        root.runGradle("testCase").assertLine("^\\[]$")
    }
}