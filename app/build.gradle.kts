plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.tdt.musicplayer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tdt.musicplayer"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // ViewPager2 + TabLayout
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.google.android.material:material:1.10.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.github.TeamNewPipe:NewPipeExtractor:v0.24.5")

    // Thay thế dependency
    implementation("com.arthenica:ffmpeg-kit-full:6.0-2.LTS")
    implementation("com.google.code.gson:gson:2.10.1")





    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}