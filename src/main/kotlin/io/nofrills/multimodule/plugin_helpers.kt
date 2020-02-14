package io.nofrills.multimodule

import com.android.build.gradle.TestedExtension
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun applyAndroidPlugin(pluginId: String, target: Project) {
    val commonConfig = target.rootProject.extensions.getByType(MultimoduleExtension::class.java)

    target.plugins.apply(pluginId)

    configureCommonAndroid(target, commonConfig)
    configureKotlinTasks(target, commonConfig)
}

internal fun configureCommonAndroid(project: Project, commonConfig: MultimoduleExtension) {
    project.plugins.apply("org.jetbrains.kotlin.android")

    project.extensions.getByType(TestedExtension::class.java).apply {
        compileOptions {
            it.sourceCompatibility = commonConfig.javaConfig.sourceCompatibility
            it.targetCompatibility = commonConfig.javaConfig.targetCompatibility
        }

        sourceSets {
            it.getByName("androidTest").java.srcDir("src/androidTest/kotlin")
            it.getByName("main").java.srcDir("src/main/kotlin")
            it.getByName("test").java.srcDir("src/test/kotlin")
        }

        commonConfig.androidAction?.execute(this)
    }
}

internal fun configureKotlinTasks(project: Project, commonConfig: MultimoduleExtension) {
    project.tasks.withType(KotlinCompile::class.java).configureEach {
        commonConfig.kotlinAction?.execute(it.kotlinOptions)
    }
}
