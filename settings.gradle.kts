pluginManagement {
    plugins {
        val kotlinVersion: String by settings
        val gradleVersionsPluginVersion: String by settings
        val fulladleVersion: String by settings

        kotlin("jvm") version(kotlinVersion)
        id("com.github.ben-manes.versions") version(gradleVersionsPluginVersion)
        id("com.osacky.fulladle") version(fulladleVersion)
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        maven {
            // For the Locale Plug-in SDK artifacts
            // GitHub Package Registry does not allow anonymous access
            val localePluginMavenUrl: String by settings
            val localePluginMavenUser: String by settings
            val localePluginMavenPassword: String by settings
            url = java.net.URI(localePluginMavenUrl)
            credentials  {
                username = localePluginMavenUser
                password = localePluginMavenPassword
            }
        }
    }
}

include(":toastPluginSettingApp")
