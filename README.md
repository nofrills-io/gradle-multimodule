# multimodule

The multimodule plugin allows you to write a common configuration
in the top-level `build.gradle` file, and apply that configuration to your sub-projects.

```kotlin
// Top-level build.gradle

plugins {
    id("io.nofrills.multimodule") version "0.2.0"
}

multimodule {
    // You can include or skip any of the following configuration blocks.

    // Common configuration for android sub-projects.
    android {
        compileSdkVersion(28) // Usually, at least this setting is required    
    }

    // If `dokka` block is present, the plugin will generate dokka docs
    // for all sub-projects.
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
