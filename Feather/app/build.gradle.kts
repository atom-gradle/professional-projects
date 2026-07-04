plugins {
    id("com.android.application")
}

android {
    namespace = "com.qian.feather"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.qian.feather"
        minSdk = 28
        targetSdk = 33
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.1.0")
    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.activity:activity-ktx:1.6.0")
    implementation("com.github.bumptech.glide:glide:3.7.0")
    implementation("jp.wasabeef:glide-transformations:4.3.0")
    implementation("com.hankcs:hanlp:portable-1.7.8")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
}