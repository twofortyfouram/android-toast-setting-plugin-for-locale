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

tasks {
    withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
        gradleReleaseChannel = "current"

        resolutionStrategy {
            componentSelection {
                all {
                    if (isNonStable(candidate.version) && !isNonStable(currentVersion)) {
                        reject("Unstable")
                    }
                }
            }
        }
    }
}

val unstableKeywords = listOf("alpha", "beta", "rc", "m", "ea", "build")

fun isNonStable(version: String): Boolean {
    val versionLowerCase = version.toLowerCase()

    return unstableKeywords.any { versionLowerCase.contains(it) }
}