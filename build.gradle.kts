plugins {
    `java-gradle-plugin`
    `maven-publish`
    id("org.jetbrains.kotlin.jvm") version "1.3.61"
    id("com.gradle.plugin-publish") version "0.10.1"
}

repositories {
    jcenter()
    google()
}

dependencies {
    implementation("com.android.tools.build:gradle:3.6.0")
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:0.10.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

gradlePlugin {
    val multimodule by plugins.creating {
        id = "io.nofrills.multimodule"
        implementationClass = "io.nofrills.multimodule.MultimodulePlugin"
        displayName = "Multimodule plugin"
        description = "Create your submodules quickly by applying a common configuration."
    }
    val aar by plugins.creating {
        id = "io.nofrills.multimodule.aar"
        implementationClass = "io.nofrills.multimodule.AarPlugin"
        displayName = "Multimodule AAR plugin"
        description = "Create an AAR (Android library) submodule."
    }
    val apk by plugins.creating {
        id = "io.nofrills.multimodule.apk"
        implementationClass = "io.nofrills.multimodule.ApkPlugin"
        displayName = "Multimodule APK plugin"
        description = "Create an APK (Android application) submodule."
    }
    val jar by plugins.creating {
        id = "io.nofrills.multimodule.jar"
        implementationClass = "io.nofrills.multimodule.JarPlugin"
        displayName = "Multimodule JAR plugin"
        description = "Create an JAR (Java library) submodule."
    }
}

// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

gradlePlugin.testSourceSets(functionalTestSourceSet)
configurations.getByName("functionalTestImplementation").extendsFrom(configurations.getByName("testImplementation"))

// Add a task to run the functional tests
val functionalTest by tasks.creating(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
}

val check by tasks.getting(Task::class) {
    // Run the functional tests as part of `check`
    dependsOn(functionalTest)
}


group = "io.nofrills"
version = "0.2.0"

publishing {
    repositories {
        maven {
            name = "dist"
            url = uri("file://${buildDir}/dist")
        }
    }
}

pluginBundle {
    website = "https://github.com/nofrills-io/gradle-multimodule"
    vcsUrl = "https://github.com/nofrills-io/gradle-multimodule"
    tags = listOf("android", "aar", "apk", "jar", "submodule")
}
