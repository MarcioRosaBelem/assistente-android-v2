// build.gradle.kts (root)
plugins {
    id("com.android.application") version "8.1.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
