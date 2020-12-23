package io.nofrills.multimodule

import kotlin.test.Test

@Suppress("FunctionName")
class DokkaActionTests : BaseActionTest() {
    @Test
    fun `applies dokka but skips sub-projects`() {
        val (root, subProjects) = makeTestProject(
            multimoduleContent = """
            $baseAndroidConfig
            dokka {}
        """.trimIndent()
        )

        root.runGradle("tasks").assertLine("^dokka[a-zA-Z]+ -")

        for ((_, project) in subProjects) {
            project.runGradle("tasks").assertNotContains("^dokka[a-zA-Z]+ -")
        }
    }

    @Test
    fun `applies dokka`() {
        val (root, subProjects) = makeTestProject(
            multimoduleContent = """
            $baseAndroidConfig
            dokka {}
            kotlin {}
        """.trimIndent()
        )

        root.runGradle("tasks").assertLine("^dokka[a-zA-Z]+ -")

        for ((_, project) in subProjects) {
            project.runGradle("tasks").assertLine("^dokka[a-zA-Z]+ -")
        }
    }

    @Test
    fun `dokka config applied`() {
        val (_, subProjects) = makeTestProject(
            multimoduleContent = """
            $baseAndroidConfig
            dokka {
                moduleName.set("hello-" + project.name)
            }
            kotlin {}
        """.trimIndent()
        )

        for ((name, project) in subProjects) {
            project.resolve(BuildFile).appendText(
                """
            tasks.register("testCase") {
                doLast {
                    val dokkaTask = project.tasks.getByName("dokkaHtml") as org.jetbrains.dokka.gradle.DokkaTask
                    println(dokkaTask.moduleName.get())
                }
            }
        """.trimIndent()
            )
            project.runGradle("testCase").assertContains("hello-$name")
        }
    }

    @Test
    fun `dokka not allowed`() {
        val (root, subProjects) = makeTestProject(
            multimoduleContent = """
            $baseAndroidConfig
            dokka {}
            kotlin {}
        """.trimIndent(),
            submoduleContent = "dokkaAllowed.set(false)"
        )

        for ((_, project) in subProjects) {
            project.runGradle("tasks").assertNotContains("^dokka[a-zA-Z]+ -")
        }
    }
}