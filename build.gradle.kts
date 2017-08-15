buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }

    dependencies {
        val androidGradlePluginVersion: String by project

        classpath("com.android.tools.build:gradle:${androidGradlePluginVersion}")
    }
}

plugins {
    kotlin("jvm")
    id("com.github.ben-manes.versions")
    id("com.osacky.fulladle")
}

fladle {
    val toastPluginFirebaseTestLabServiceAccountKeyPath: String by project

    serviceAccountCredentials.set(File(toastPluginFirebaseTestLabServiceAccountKeyPath))
    devices.addAll(mapOf("model" to "NexusLowRes", "version" to "30"))
}
