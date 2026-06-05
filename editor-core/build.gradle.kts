plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.diffplug.spotless")
}

android {
    namespace = "com.example.glimmerseed.editorcore"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.24")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("org.joml:joml:1.10.5")
    // ViewModel
    api("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    api("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    // Coroutines Flow
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

// Spotless 代码格式化配置
spotless {
    kotlin {
        target("src/**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktlint("1.0.1")
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint("1.0.1")
    }
}
