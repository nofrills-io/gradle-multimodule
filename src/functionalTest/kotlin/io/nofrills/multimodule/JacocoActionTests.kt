package io.nofrills.multimodule

import kotlin.test.Test
import kotlin.test.assertFalse

@Suppress("FunctionName")
class JacocoActionTests : BaseActionTest() {
    @Test
    fun `applies jacoco plugin for jar`() {
        val (_, subProjects) = makeTestProject(
            setOf(jar),
            multimoduleContent = "jacoco {}"
        )

        subProjects.forEach { (_, p) ->
            p.runGradle("tasks", "--all").assertLine("^jacocoTestReport ")
        }
    }

    @Test
    fun `custom configuration applied`() {
        val (_, subProjects) = makeTestProject(
            multimoduleContent = """
                $baseAndroidConfig
                jacoco {
                    jacocoPlugin {
                        toolVersion = "0.9.99"
                    }
                    jacocoTask {
                        reports.html.destination = file(project.name + "-test-destination.html")
                    }
                }
            """.trimIndent()
        )

        subProjects.forEach { (_, p) ->
            p.resolve(BuildFile).appendText(
                """
                tasks.register("testCase") {
                    val jacocoPluginExt = project.extensions.getByType(org.gradle.testing.jacoco.plugins.JacocoPluginExtension::class.java)
                    println(jacocoPluginExt.toolVersion)
                }
                project.afterEvaluate {
                    project.tasks.withType(JacocoReport::class.java).all { println(reports.html.destination) }
                }
            """.trimIndent()
            )
            p.runGradle("testCase")
                .assertLine("^0.9.99$")
                .assertContains("${p.name}-test-destination.html")
        }
    }

    @Test
    fun `applies jacoco plugin for android`() {
        val (_, subProjects) = makeTestProject(
            androidTypes,
            multimoduleContent = """
                $baseAndroidConfig
                jacoco {}
            """.trimIndent()
        )

        subProjects.forEach { (_, p) ->
            p.runGradle("tasks", "--all")
                .assertLine("^jacocoDebugTestReport$")
                .assertLine("^jacocoReleaseTestReport$")
        }
    }

    @Test
    fun `applies jacoco plugin with android flavors`() {
        val (_, subProjects) = makeTestProject(
            androidTypes,
            multimoduleContent = """
                android {
                    compileSdkVersion(28)
                    flavorDimensions("api")
                    productFlavors {
                        create("staging") { dimension = "api" }
                        create("prod") { dimension = "api" }
                    }
                }
                jacoco {}
            """.trimIndent()
        )

        subProjects.forEach { (_, p) ->
            p.runGradle("tasks", "--all")
                .assertLine("^jacocoStagingDebugTestReport$")
                .assertLine("^jacocoProdDebugTestReport$")
                .assertLine("^jacocoStagingReleaseTestReport$")
                .assertLine("^jacocoProdReleaseTestReport$")
        }
    }

    @Test
    fun `jacoco not allowed`() {
        val (_, subProjects) = makeTestProject(
            multimoduleContent = """
                $baseAndroidConfig
                jacoco {}
            """.trimIndent(),
            submoduleContent = "jacocoAllowed.set(false)"
        )

        subProjects.forEach { (_, p) ->
            assertFalse(p.runGradle("tasks", "--all").output.contains("jacoco"))
        }
    }
}