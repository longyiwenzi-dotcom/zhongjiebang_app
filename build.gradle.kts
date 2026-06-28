// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.22")
        classpath("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:1.8.22-1.0.11")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
}