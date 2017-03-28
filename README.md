[![CircleCI](https://circleci.com/gh/twofortyfouram/android-toast-setting-plugin-for-locale.svg?style=svg)](https://circleci.com/gh/twofortyfouram/android-toast-setting-plugin-for-locale)

# Overview
[Locale](https://play.google.com/store/apps/details?id=com.twofortyfouram.locale) allows developers to create plug-in conditions and settings.  The android-toast-setting-plugin-for-locale implements an example plug-in setting.  This project is the final layer of the [Locale Developer Platform](http://www.twofortyfouram.com/developer).

Although there are multiple ways to approach building a plug-in setting, we recommend forking this project as the starting point.


# Compatibility
The application is compatible and optimized for Android API Level 14 and above.


## Forking this Project
The following steps are necessary to fork this project

1. Rename the package name in AndroidManifest.xml
1. Rename the package name in proguard-project.txt
1. Optional: For CircleCI continous integration (CI)
    1. Create a [Firebase](https://firebase.google.com) project
    1. Configure the Firebase project with a [service account](https://firebase.google.com/docs/test-lab/continuous)
    1. On CircleCI, add environment variables for `GCLOUD_SERVICE_KEY_BASE_64` which is the base64 encoded JSON service key and `GCLOUD_PROJECT_ID` which is the project ID.