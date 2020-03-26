package io.nofrills.multimodule

import kotlin.test.Test

@Suppress("FunctionName")
class JavaActionTests : BaseActionTest() {
    @Test
    fun `applies default java config to jar`() {
        val (_, subProjects) = makeTestProject(setOf(jar))

        subProjects.forEach { (_, p) ->
            p.resolve(BuildFile).appendText(
                """
                project.tasks.register("testCase") {
                    val javaExt = project.extensions.getByType(org.gradle.api.plugins.JavaPluginExtension::class.java)
                    println("sourceCompatibility=" + javaExt.sourceCompatibility)
                    println("targetCompatibility=" + javaExt.targetCompatibility)
                }
            """.trimIndent()
            )
            p.runGradle("testCase")
                .assertLine("^sourceCompatibility=1.8$")
                .assertLine("^targetCompatibility=1.8$")
        }
    }

    @Test
    fun `applies custom java config to jar`() {
        val (_, subProjects) = makeTestProject(
            setOf(jar), multimoduleContent = """
            java {
                sourceCompatibility = JavaVersion.VERSION_1_1
                targetCompatibility = JavaVersion.VERSION_1_2
            }
        """.trimIndent()
        )

        subProjects.forEach { (_, p) ->
            p.resolve(BuildFile).appendText(
                """
                project.tasks.register("testCase") {
                    val javaExt = project.extensions.getByType(org.gradle.api.plugins.JavaPluginExtension::class.java)
                    println("sourceCompatibility=" + javaExt.sourceCompatibility)
                    println("targetCompatibility=" + javaExt.targetCompatibility)
                }
            """.trimIndent()
            )
            p.runGradle("testCase")
                .assertLine("^sourceCompatibility=1.1$")
                .assertLine("^targetCompatibility=1.2$")
        }
    }

    @Test
    fun `applies default java config to android projects`() {
        val (_, subProjects) = makeTestProject(
            androidTypes,
            multimoduleContent = baseAndroidConfig
        )

        subProjects.forEach { (_, p) ->
            p.resolve(BuildFile).appendText(
                """
                project.tasks.register("testCase") {
                    val ext = project.extensions.getByType(com.android.build.gradle.TestedExtension::class.java)
                    println("sourceCompatibility=" + ext.compileOptions.sourceCompatibility)
                    println("targetCompatibility=" + ext.compileOptions.targetCompatibility)
                }
            """.trimIndent()
            )
            p.runGradle("testCase")
                .assertLine("^sourceCompatibility=1.8$")
                .assertLine("^targetCompatibility=1.8$")
        }
    }

    @Test
    fun `applies custom java config to android projects`() {
        val (_, subProjects) = makeTestProject(
            androidTypes,
            multimoduleContent = """
                $baseAndroidConfig
                java {
                    sourceCompatibility = JavaVersion.VERSION_1_1
                    targetCompatibility = JavaVersion.VERSION_1_2
                }
            """.trimIndent()
        )

        subProjects.forEach { (_, p) ->
            p.resolve(BuildFile).appendText(
                """
                project.tasks.register("testCase") {
                    val ext = project.extensions.getByType(com.android.build.gradle.TestedExtension::class.java)
                    println("sourceCompatibility=" + ext.compileOptions.sourceCompatibility)
                    println("targetCompatibility=" + ext.compileOptions.targetCompatibility)
                }
            """.trimIndent()
            )
            p.runGradle("testCase")
                .assertLine("^sourceCompatibility=1.1$")
                .assertLine("^targetCompatibility=1.2$")
        }
    }
}