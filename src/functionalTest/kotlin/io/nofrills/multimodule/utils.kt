package io.nofrills.multimodule

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import kotlin.test.assertTrue

internal const val BuildFile = "build.gradle.kts"
internal const val FunctionalTestPath = "build/functionalTest"

internal fun makeTestProject(
    include: List<String> = listOf("aar", "apk", "jar"),
    multimoduleContent: String = "",
    submoduleContent: String = ""
): Pair<File, Map<String, File>> {
    val rootDir = File(FunctionalTestPath)
    rootDir.deleteRecursively()
    rootDir.mkdirs()

    val settingsContents = include.joinToString("\n") { "include(':$it')" }
    rootDir.resolve("settings.gradle").writeText(settingsContents)
    rootDir.resolve(BuildFile).writeText(
        """
            plugins {
                id("io.nofrills.multimodule")
            }
            multimodule {
                $multimoduleContent
            }

        """.trimIndent()
    )

    val subProjects = include.map { it to File(rootDir, it) }.toMap()
    for ((name, dir) in subProjects) {
        dir.mkdirs()
        dir.resolve(BuildFile).writeText(
            """
        plugins { id("io.nofrills.multimodule.${name}") }
        submodule {
            $submoduleContent
        }

    """.trimIndent()
        )
    }

    return rootDir to subProjects
}

internal fun File.runGradle(vararg runnerArgs: String): BuildResult {
    val runner = GradleRunner.create()
    runner.forwardOutput()
    runner.withPluginClasspath()
    runner.withArguments(*runnerArgs)
    runner.withProjectDir(this)
    return runner.build()
}

internal fun File.failGradle(vararg runnerArgs: String): BuildResult {
    val runner = GradleRunner.create()
    runner.forwardOutput()
    runner.withPluginClasspath()
    runner.withArguments(*runnerArgs)
    runner.withProjectDir(this)
    return runner.buildAndFail()
}

internal fun BuildResult.assertContains(other: String): BuildResult {
    assertTrue(output.contains(other))
    return this
}

internal fun BuildResult.assertContains(other: Regex): BuildResult {
    assertTrue(output.contains(other))
    return this
}

internal enum class ProjectType {
    Aar, Apk, Jar
}