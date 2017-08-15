# Overview
[Locale X](https://play.google.com/store/apps/details?id=com.twofortyfouram.locale.x) allows developers to create plug-in settings.  The android-toast-setting-plugin-for-locale implements an example plug-in setting.  This project is the final layer of the [Locale Developer Platform](http://www.twofortyfouram.com/developer).

Although there are multiple ways to approach building a plug-in setting, we recommend forking this project as the starting point.


# Compatibility
The application is compatible and optimized for Android API Level 24 and above, which matches the compatibility of Locale X.


# Setup
To build the project as-is, the following steps are required

1. Generate a GitHub Personal Access token: The plug-in SDK dependencies are currently published to GitHub Package Registry which does not allow for anonymous access.  Until we get these packages published to Maven Central, use the following steps 
    1. Generate a new [Personal Access Token](https://github.com/settings/tokens/new)
    1. Put in a name for the token, such as `package registry`
    1. Check the box for `read:packages` only
    1. Click Generate Token
    1. Leave the page open—you'll come back to it in the next step
1. Set up GitHub authentication on your machine:
    1. If it doesn't exist, create a Gradle properties file for your user (on macOS and Linux, this will be `~/.gradle/gradle.properties`).  _This is a different file than the one in the git repo, and it acts as an override._
    1. Add the line `localePluginMavenUser=` with your GitHub username after the equals (with no space)
    1. Add the line `localePluginMavenPassword=` with the personal access token from the first step after the equals (with no space)

# Forking
To fork the project, in order to develop your own plug-in, the following changes are recommended:

1. Rename the package name in AndroidManifest.xml
1. Rename the package name in proguard-project.txt
