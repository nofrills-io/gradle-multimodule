plugins {
    `java-gradle-plugin`
    `maven-publish`
    id("org.jetbrains.kotlin.jvm") version "1.3.61"
}

repositories {
    jcenter()
    google()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
    implementation("com.android.tools.build:gradle:3.6.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

gradlePlugin {
    val multimodule by plugins.creating {
        id = "io.nofrills.multimodule"
        implementationClass = "io.nofrills.multimodule.MultimodulePlugin"
    }
    val aar by plugins.creating {
        id = "io.nofrills.multimodule.aar"
        implementationClass = "io.nofrills.multimodule.AarPlugin"
    }
    val apk by plugins.creating {
        id = "io.nofrills.multimodule.apk"
        implementationClass = "io.nofrills.multimodule.ApkPlugin"
    }
    val jar by plugins.creating {
        id = "io.nofrills.multimodule.jar"
        implementationClass = "io.nofrills.multimodule.JarPlugin"
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
version = "0.1.0"

publishing {
    repositories {
        maven {
            name = "dist"
            url = uri("file://${buildDir}/dist")
        }
    }
}
