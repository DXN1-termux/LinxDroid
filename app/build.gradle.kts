// app/build.gradle.kts
plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    namespace = "com.linxdroid.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.linxdroid.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
}
