// Top-level build file
plugins {
    // AGP 8.3.2 (Stable for Gradle 8.4+)
    id("com.android.application") version "8.13.2" apply false
    id("com.android.library") version "8.13.2" apply false

    // Kotlin 1.9.24 (Compatible with Compose Compiler 1.5.14)
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false

    // Google Services

}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}