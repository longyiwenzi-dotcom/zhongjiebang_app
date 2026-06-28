// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.22")
        // 删除 KSP 这一行
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
}