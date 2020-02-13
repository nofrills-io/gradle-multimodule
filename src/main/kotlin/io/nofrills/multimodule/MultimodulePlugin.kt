package io.nofrills.multimodule

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Property
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

enum class ModuleType {
    AAR, APK, JAR
}

abstract class MulticonfigExtension(project: Project) {
    internal val androidConfig: SimpleAndroidConfig = project.objects.newInstance(SimpleAndroidConfig::class.java)
    internal val javaConfig: SimpleJavaConfig = project.objects.newInstance(SimpleJavaConfig::class.java)
    internal val kotlinConfig: SimpleKotlinConfig = project.objects.newInstance(SimpleKotlinConfig::class.java)

    fun android(action: Action<SimpleAndroidConfig>) {
        action.execute(androidConfig)
    }

    fun java(action: Action<SimpleJavaConfig>) {
        action.execute(javaConfig)
    }

    fun kotlin(action: Action<SimpleKotlinConfig>) {
        action.execute(kotlinConfig)
    }
}

abstract class MultimoduleExtension(project: Project) {
    val moduleType: Property<ModuleType> = project.objects.property(ModuleType::class.java)
}

/**
 * A simple 'hello world' plugin.
 */
class MultimodulePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        if (project.rootProject != project) {
            error("Plugin should be applied to the root project.")
        }

        applyToRootProject(project)
        project.subprojects {
            applyToSubProject(it)
        }

        // project type: jar, aar, apk
        // android:
        //  - compileSdkVersion, compileOptions, defaultConfig
        //  - buildTypes (default): debug, release
        //  - sourceSets for kotlin
        // java version
        // kotlin version + compiler config
        // publishing via maven, gradle modules
        // include kotlin std lib t(default)/f
        // include common libraries (dagger, retrofit etc)
        // manage version numbers

        // jacoco, detekt etc.

        // Write it as a plugin, which will be basically the only plugin to apply.
        // Then, it can be configured so that all modules have the same basic config.
        // So the config should be a one-time, global for the whole multi-module project.
        // So the idea is, you apply the plugin, and then you may only need to add the dependencies that are needed by a specific module.
        // In the end, creating a new module is very simple.

        // configuration should be global (e.g. in the root build.gradle)
        // but the plugin should be applied to each submodule separately

        project.tasks.register("mminfo") { task ->
            task.doLast {
                val configExtension = it.project.extensions.getByType(MulticonfigExtension::class.java)
                println("Config: ${configExtension.androidConfig.versionName}")
                it.project.subprojects.forEach {
                    val mmExtension = it.extensions.getByType(MultimoduleExtension::class.java)
                    println("Subproject ${it.name}")
                    println("MM: ${mmExtension.moduleType.orNull}")
                }
            }
        }
    }

    private fun applyToRootProject(project: Project) {
        project.extensions.create<MulticonfigExtension>("multiconfig", MulticonfigExtension::class.java, project)
    }

    private fun applyToSubProject(project: Project) {
        project.extensions.create<MultimoduleExtension>("multimodule", MultimoduleExtension::class.java, project)
        project.afterEvaluate { p ->
            val commonConfig = p.rootProject.extensions.getByType(MulticonfigExtension::class.java)
            val moduleExtension = p.extensions.getByType(MultimoduleExtension::class.java)
            when (val moduleType = moduleExtension.moduleType.orNull) {
                ModuleType.AAR -> {
                    configureCommonAndroid(p, commonConfig)
                }
                ModuleType.APK -> {
                    configureCommonAndroid(p, commonConfig)
                }
                ModuleType.JAR -> {
                    configureJar(p, commonConfig)
                }
                else -> error("Unsupported module type for project ${project.name}: $moduleType")
            }
        }
    }

    private fun configureCommonAndroid(project: Project, commonConfig: MulticonfigExtension) {
        project.plugins.apply("com.android.library")
        project.plugins.apply("org.jetbrains.kotlin.android")
        project.extensions.getByType(LibraryExtension::class.java).apply {
            compileSdkVersion(commonConfig.androidConfig.compileSdkVersion)
            compileOptions {
                it.sourceCompatibility = commonConfig.javaConfig.sourceCompatibility
                it.targetCompatibility = commonConfig.javaConfig.targetCompatibility
            }
            defaultConfig {
                it.minSdkVersion(commonConfig.androidConfig.minSdkVersion)
                it.targetSdkVersion(commonConfig.androidConfig.targetSdkVersion)
                it.versionCode = commonConfig.androidConfig.versionCode
                it.versionName = commonConfig.androidConfig.versionName
            }

//            buildTypes {
//            }

//            sourceSets["androidTest"].java.srcDir("src/androidTest/kotlin")
//            sourceSets["main"].java.srcDir("src/main/kotlin")
//            sourceSets["test"].java.srcDir("src/test/kotlin")
        }
    }

    private fun configureJar(project: Project, commonConfig: MulticonfigExtension) {
        project.plugins.apply("java-library")
        project.plugins.apply("org.jetbrains.kotlin.jvm")

        project.extensions.getByType(JavaPluginExtension::class.java).apply {
            sourceCompatibility = commonConfig.javaConfig.sourceCompatibility
            targetCompatibility = commonConfig.javaConfig.targetCompatibility
        }
    }

    private fun configureKotlinTasks(project: Project, commonConfig: MulticonfigExtension) {
        project.tasks.withType(KotlinCompile::class.java).configureEach {
            it.kotlinOptions {
                allWarningsAsErrors = commonConfig.kotlinConfig.allWarningsAsErrors
                if (commonConfig.kotlinConfig.useExperimental) {
                    freeCompilerArgs = freeCompilerArgs + listOf(
                            "-Xuse-experimental=kotlin.Experimental"
                    )
                }
                jvmTarget = commonConfig.kotlinConfig.jvmTarget
            }
        }
    }
}

interface SimpleAndroidConfig {
    var compileSdkVersion: Int
    var minSdkVersion: Int
    var targetSdkVersion: Int
    var versionCode: Int
    var versionName: String
}

open class SimpleJavaConfig {
    var sourceCompatibility = JavaVersion.VERSION_1_8
    var targetCompatibility = JavaVersion.VERSION_1_8
}

open class SimpleKotlinConfig {
    var allWarningsAsErrors = false
    var jvmTarget = "1.8"
    var useExperimental = false
}
