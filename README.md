# multimodule

The multimodule plugin allows you to write a common configuration in a single place,
and apply that configuration to your sub-projects.

### Step 1/2
In the top-level `build.gradle` file, put the following:

```kotlin
// Top-level build.gradle

plugins {
    id("io.nofrills.multimodule") version "0.7.0"
}

multimodule {
    // You can include or skip any of the following configuration blocks.

    // Common configuration for android sub-projects.
    android {
        compileSdkVersion(28) // Usually, at least this setting is required    
    }

    // If `dokka` block is present, the plugin will generate merged
    // documentation for all sub-projects (with Kotlin).
    // `./gradlew :dokka`
    dokka {}
    
    // If `jacoco` block is present, the plugin will include
    // tasks for code coverage reports.
    // `./gradlew jacoco<optional_variant_name>TestReport`
    jacoco {}
    
    // Options for Java source and target compatibility.
    // Defaults to Java 8.
    java {}

    // Options for Kotlin.
    // By default, `jvmTarget` is set to "1.8".
    kotlin {}

    // If present, the plugin will include tasks for publishing
    // the artifacts to a maven repository. 
    publish {}
}
```

### Step 2/2 
In each of you sub-projects where the common configuration should be applied:

```kotlin
// build.gradle in a sub-project

plugins {
    // Use ONE OF the following plugins:
    id("io.nofrills.multimodule.aar") // For Android library
    id("io.nofrills.multimodule.apk") // For Android application
    id("io.nofrills.multimodule.jar") // For JAR library

    // Appropriate plugins will be applied automatically.
    // For example, if you applied "io.nofrills.multimodule.aar",
    // the "com.android.library" plugin will be applied for you.
}

// Optional configuration.
// Allows to disable specific feature, even though it is present
// in the `multimodule` block, in top-level build.gradle.
submodule {
    dokkaAllowed.set(false) 
    jacocoAllowed.set(false)
    publishAllowed.set(false)
}
```

---------
### Plugin versions

By default, `multimodule` plugin specifies a preferred version numbers for the plugins it uses.
You can overwrite them by including an explicit dependency:

```kotlin
// Top-level build.gradle
 
buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:<version>")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:<version>")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:<version>")
    }
}
```

## License

This project is published under Apache License, Version 2.0 (see the [LICENSE](LICENSE) file for details).
