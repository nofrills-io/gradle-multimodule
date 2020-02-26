package io.nofrills.multimodule

import com.android.build.gradle.TestedExtension
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.configureKotlinTasks(commonConfig: MultimoduleExtension) {
    project.tasks.withType(KotlinCompile::class.java).configureEach {
        val kotlinConfig = KotlinConfig(it.kotlinOptions)
        commonConfig.kotlinAction?.execute(kotlinConfig)
    }
}

internal fun Project.applyAndroidPlugin(androidPluginId: String, shouldApplyKotlin: Boolean) {
    val commonConfig = project.rootProject.extensions.getByType(MultimoduleExtension::class.java)

    project.plugins.apply(androidPluginId)

    project.extensions.getByType(TestedExtension::class.java).apply {
        compileOptions {
            it.sourceCompatibility = commonConfig.javaConfig.sourceCompatibility
            it.targetCompatibility = commonConfig.javaConfig.targetCompatibility
        }
        commonConfig.androidAction?.execute(this)
    }

    if (shouldApplyKotlin) {
        project.plugins.apply("org.jetbrains.kotlin.android")
        project.configureKotlinTasks(commonConfig)
        project.extensions.getByType(TestedExtension::class.java).apply {
            sourceSets {
                it.getByName("androidTest").java.srcDir("src/androidTest/kotlin")
                it.getByName("main").java.srcDir("src/main/kotlin")
                it.getByName("test").java.srcDir("src/test/kotlin")
            }
        }
    }
}
