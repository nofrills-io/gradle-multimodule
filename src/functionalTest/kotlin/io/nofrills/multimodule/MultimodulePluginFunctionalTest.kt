/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package io.nofrills.multimodule

import java.io.File
import org.gradle.testkit.runner.GradleRunner
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * A simple functional test for the 'io.nofrills.multimodule' plugin.
 */
class MultimodulePluginFunctionalTest {
    @Test
    fun `can run task`() {
        // Setup the test build
        val projectDir = File("build/functionalTest")
        val libDir = File(projectDir, "lib")
        libDir.mkdirs()

        projectDir.resolve("settings.gradle").writeText("""
            include(":lib")
        """.trimIndent())
        projectDir.resolve("build.gradle").writeText("""
            plugins {
                id("io.nofrills.multimodule")
            }
            multimodule {
                android {
                    compileSdkVersion(28)

                    buildTypes {
                        create("mock") {
                        }
                        getByName("release") {
                            minifyEnabled = false
                        }
                    }
                }
            }
        """)
        libDir.resolve("build.gradle").writeText("""
            plugins {
                id("io.nofrills.multimodule.aar")
            }
        """.trimIndent())

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments(":lib:tasks")
        runner.withProjectDir(projectDir)
        val result = runner.build();

        // Verify the result
//        assertTrue(result.output.contains("Hello from plugin 'io.nofrills.multimodule'"))
    }
}
