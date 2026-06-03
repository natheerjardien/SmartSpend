// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Firebase (2026), demonstrates how to add the Google services Gradle plugins
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.gms.google.services) apply false

}

// Firebase, 2026.  Add Firebase to your Android project. (Version 2.0) [Source code]
// Available at: < https://firebase.google.com/docs/android/setup > [Accessed 28 May 2026].