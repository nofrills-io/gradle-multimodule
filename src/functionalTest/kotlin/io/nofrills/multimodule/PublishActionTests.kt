package io.nofrills.multimodule

import kotlin.test.Test

@Suppress("FunctionName")
class PublishActionTests : BaseActionTest() {
    @Test
    fun `applies publish plugin`() {
        val (_, subProjects) = makeTestProject(
            multimoduleContent = """
                $baseAndroidConfig
                publish {}
            """.trimIndent()
        )

        subProjects.forEach { (projectType, p) ->
            val result = p.runGradle("tasks", "--group=publishing")
            result.assertLine("^publish -.*$")
            result.assertLine("^publishToMavenLocal -.*$")
            when (projectType) {
                aar -> result.assertLine("^publishReleaseAarPublicationToMavenLocal -.*$")
                apk -> result.assertLine("^publishReleaseApkPublicationToMavenLocal -.*$")
                jar -> result.assertLine("^publishJarPublicationToMavenLocal -.*$")
            }
        }
    }

    @Test
    fun `applies publish plugin with custom mavenPom`() {
        val (_, subProjects) = makeTestProject(
            multimoduleContent = """
                $baseAndroidConfig
                publish {
                    mavenPom {
                        name.set("testCase-" + project.name)
                    }
                }
            """.trimIndent()
        )

        subProjects.forEach { (_, p) ->
            p.resolve(BuildFile).appendText(
                """
                project.tasks.register("testCase") {
                    doLast {
                        val ext = project.extensions.getByType(org.gradle.api.publish.PublishingExtension::class.java)
                        val publications = ext.publications.withType(org.gradle.api.publish.maven.MavenPublication::class.java)
                        val pub = publications.first()
                        println("pomName=" + pub.pom.name.get())
                    }
                }
            """.trimIndent()
            )
            val result = p.runGradle("testCase")
            result.assertContains("pomName=testCase-${p.name}")
        }
    }

    @Test
    fun `applies publish plugin with custom repositories`() {
        val (_, subProjects) = makeTestProject(
            multimoduleContent = """
                $baseAndroidConfig
                publish {
                    repositories {
                        maven { name = "testRepo" }
                    }
                }
            """.trimIndent()
        )

        subProjects.forEach { (_, p) ->
            val result = p.runGradle("tasks", "--group=publishing")
            result.assertLine("^publish.*PublicationToTestRepoRepository -.*$")
        }
    }

    @Test
    fun `applies publish plugin with javadocs`() {
        val (_, subProjects) = makeTestProject(
            multimoduleContent = """
                $baseAndroidConfig
                publish {
                    withDocs = true
                }
            """.trimIndent()
        )

        subProjects.forEach { (projectType, p) ->
            val result = p.runGradle("tasks", "--group=documentation")
            if (projectType == jar) {
                result.assertLine("^javadoc -.*$")
                result.assertLine("^javadocJar$")
            } else {
                result.assertLine("^releaseJavadoc$")
                result.assertLine("^releaseJavadocJar$")
            }
        }
    }

    @Test
    fun `applies publish plugin with dokka`() {
        val (_, subProjects) = makeTestProject(
            multimoduleContent = """
                $baseAndroidConfig
                kotlin {}
                publish {
                    withDocs = true
                }
            """.trimIndent()
        )

        subProjects.forEach { (projectType, p) ->
            val result = p.runGradle("tasks", "--group=documentation")
            if (projectType == jar) {
                result.assertLine("^dokka -.*$")
                result.assertLine("^dokkaJar$")
            } else {
                result.assertLine("^dokka -.*$")
                result.assertLine("^releaseDokkaJar$")
            }
        }
    }

    @Test
    fun `applies publish plugin with sources`() {
        val (_, subProjects) = makeTestProject(
            multimoduleContent = """
                $baseAndroidConfig
                publish {
                    withSources = true
                }
            """.trimIndent()
        )

        subProjects.forEach { (projectType, p) ->
            val result = p.runGradle("tasks", "--all", "--group=other")
            if (projectType == jar) {
                result.assertLine("^sourcesJar$")
            } else {
                result.assertLine("^releaseSourcesJar$")
            }
        }
    }
}