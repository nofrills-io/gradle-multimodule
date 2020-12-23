package io.nofrills.multimodule

import kotlin.test.Test

@Suppress("FunctionName")
class KotlinActionTests : BaseActionTest() {
    @Test
    fun `applies default config`() {
        val (_, subProjects) = makeTestProject(
            multimoduleContent = """
            $baseAndroidConfig
            kotlin {}
        """.trimIndent()
        )

        subProjects.forEach { (projectType, p) ->
            p.resolve(BuildFile).appendText(
                """
                val kotlinTasks = project.tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java)
                project.tasks.register("testCase") {
                    kotlinTasks.forEach { 
                        println(it.name + "=" + it.kotlinOptions.jvmTarget)
                    }
                }
            """.trimIndent()
            )

            val result = p.runGradle("testCase")
            if (projectType == jar) {
                result.assertLine("^compileKotlin=1.8$")
                result.assertLine("^compileTestKotlin=1.8$")
            } else {
                result.assertLine("^compileDebugAndroidTestKotlin=1.8$")
                result.assertLine("^compileDebugKotlin=1.8$")
                result.assertLine("^compileDebugUnitTestKotlin=1.8$")
                result.assertLine("^compileReleaseKotlin=1.8$")
                result.assertLine("^compileReleaseUnitTestKotlin=1.8$")
            }
        }
    }

    @Test
    fun `applies custom kotlin options`() {
        val (_, subProjects) = makeTestProject(
            multimoduleContent = """
            $baseAndroidConfig
            kotlin {
                allWarningsAsErrors = true
                apiVersion = "1.1"
                freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn", project.name)
                jvmTarget = "1.6"
                languageVersion = "1.0"
                suppressWarnings = true
                useIR = true
                verbose = true
            }
        """.trimIndent()
        )

        subProjects.forEach { (projectType, p) ->
            p.resolve(BuildFile).appendText(
                """
                val kotlinTasks = project.tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java)
                project.tasks.register("testCase") {
                    kotlinTasks.forEach {
                        check(it.kotlinOptions.allWarningsAsErrors == true)
                        check(it.kotlinOptions.apiVersion == "1.1")
                        check(it.kotlinOptions.freeCompilerArgs == listOf("-Xopt-in=kotlin.RequiresOptIn", "${p.name}"))
                        check(it.kotlinOptions.jvmTarget == "1.6")
                        check(it.kotlinOptions.languageVersion == "1.0")
                        check(it.kotlinOptions.suppressWarnings == true)
                        check(it.kotlinOptions.useIR == true)
                        check(it.kotlinOptions.verbose == true)
                        println(it.name)
                    }
                }
            """.trimIndent()
            )

            val result = p.runGradle("testCase")
            if (projectType == jar) {
                result.assertLine("^compileKotlin$")
                result.assertLine("^compileTestKotlin$")
            } else {
                result.assertLine("^compileDebugAndroidTestKotlin$")
                result.assertLine("^compileDebugKotlin$")
                result.assertLine("^compileDebugUnitTestKotlin$")
                result.assertLine("^compileReleaseKotlin$")
                result.assertLine("^compileReleaseUnitTestKotlin$")
            }
        }
    }

    @Test
    fun `applies kotlin parcelize extensions`() {
        val (_, subProjects) = makeTestProject(
            multimoduleContent = """
            $baseAndroidConfig
            kotlin {
                parcelizePlugin = true
            }
        """.trimIndent()
        )

        subProjects.forEach { (projectType, p) ->
            p.resolve(BuildFile).appendText(
                """
                project.tasks.register("testCase") {
                    val plugin = project.pluginManager.findPlugin("org.jetbrains.kotlin.plugin.parcelize")
                    println("plugin=" + plugin?.name)
                }
            """.trimIndent()
            )

            val result = p.runGradle("testCase")
            if (projectType == jar) {
                result.assertLine("^plugin=null$")
            } else {
                result.assertLine("^plugin=parcelize$")
            }
        }
    }

    @Test
    fun `applies coroutines`() {
        val (_, subProjects) = makeTestProject(
            multimoduleContent = """
            $baseAndroidConfig
            kotlin {
                coroutines = true
            }
        """.trimIndent()
        )

        subProjects.forEach { (projectType, p) ->
            val result = p.runGradle("dependencies")
            result.assertContains("org.jetbrains.kotlinx:kotlinx-coroutines-core:{prefer 1.4.2}")
            if (projectType == aar || projectType == apk) {
                result.assertContains("org.jetbrains.kotlinx:kotlinx-coroutines-android:{prefer 1.4.2}")
            }
        }
    }

    @Test
    fun `applies coroutines with required version`() {
        val (_, subProjects) = makeTestProject(
            multimoduleContent = """
            $baseAndroidConfig
            kotlin {
                coroutines = true
                coroutinesVersion = Action { require("1.2.3") }
            }
        """.trimIndent()
        )

        subProjects.forEach { (projectType, p) ->
            val result = p.runGradle("dependencies")
            result.assertContains("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.2.3")
            if (projectType == aar || projectType == apk) {
                result.assertContains("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.2.3")
            }
        }
    }

    @Test
    fun `applies kapt`() {
        val (_, subProjects) = makeTestProject(
            multimoduleContent = """
            $baseAndroidConfig
            kotlin {
                kapt = true
            }
        """.trimIndent()
        )

        subProjects.forEach { (_, p) ->
            p.resolve(BuildFile).appendText(
                """
                project.tasks.register("testCase") {
                    val plugin = project.pluginManager.findPlugin("org.jetbrains.kotlin.kapt")
                    println("plugin=" + plugin?.name)
                }
                
                dependencies { kapt("some:project:1.0") }
            """.trimIndent()
            )

            p.runGradle("testCase").assertLine("^plugin=kapt$")
        }
    }

    @Test
    fun `applies kotlin reflect`() {
        val (_, subProjects) = makeTestProject(
            multimoduleContent = """
            $baseAndroidConfig
            kotlin {
                reflect = true
            }
        """.trimIndent()
        )

        subProjects.forEach { (_, p) ->
            p.runGradle("dependencies").assertLine("org.jetbrains.kotlin:kotlin-reflect:[0-9\\.]+")
        }
    }

    @Test
    fun `applies kotlin stdlib library jvm target 1_6`() {
        val (rootProject, subProjects) = makeTestProject(
            multimoduleContent = """
            $baseAndroidConfig
            kotlin {
                jvmTarget = "1.6"
            }
        """.trimIndent()
        )

        subProjects.forEach { (_, p) ->
            p.runGradle("dependencies")
                .assertLine("org.jetbrains.kotlin:kotlin-stdlib:[0-9\\.]+")
                .assertNotContains("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        }
    }

    @Test
    fun `applies kotlin stdlib library jvm target 1_8`() {
        val (_, subProjects) = makeTestProject(
            multimoduleContent = """
            $baseAndroidConfig
            kotlin {
                jvmTarget = "1.8"
            }
        """.trimIndent()
        )

        subProjects.forEach { (_, p) ->
            p.runGradle("dependencies")
                .assertLine("org.jetbrains.kotlin:kotlin-stdlib-jdk8:[0-9\\.]+")
        }
    }
}
