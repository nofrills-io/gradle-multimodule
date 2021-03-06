package io.nofrills.multimodule

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import javax.annotation.RegEx
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal const val BuildFile = "build.gradle.kts"
internal const val FunctionalTestPath = "build/functionalTest"
internal const val aar = "aar"
internal const val apk = "apk"
internal const val jar = "jar"

/** Creates a test project with a number of submodules.
 * @param include The sub-projects to be included ("aar", "apk" and/or "jar").
 * @param multimoduleContent The content to put in root project, inside the `multimodule` block.
 * @param submoduleContent The content to put in sub-projects, inside the `submodule` block.
 * @return A pair, where first element is the root project directory,
 *  and the second element is a map of sub-projects (sub_project_name; sub_project_dir).
 */
internal fun makeTestProject(
    include: Set<String> = setOf(aar, apk, jar),
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
            buildscript {
                dependencies {
                    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.20")
                }
            }
            plugins {
                id("io.nofrills.multimodule")
            }
            multimodule {
                $multimoduleContent
            }

        """.trimIndent()
    )
    val localPropFile = File("local.properties")
    localPropFile.copyTo(rootDir.resolve("local.properties"))

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
    assertTrue(output.contains(other), "Result should contain `${other}`")
    return this
}

internal fun BuildResult.assertNotContains(other: String): BuildResult {
    assertFalse(output.contains(other))
    return this
}

internal fun BuildResult.assertContains(other: Regex): BuildResult {
    assertTrue(output.contains(other))
    return this
}

internal fun BuildResult.assertLine(@RegEx lineRegex: String): BuildResult {
    return assertContains(Regex(lineRegex, RegexOption.MULTILINE))
}
