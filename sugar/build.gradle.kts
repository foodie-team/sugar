@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(sharedLibs.plugins.android.library)
    alias(sharedLibs.plugins.kotlin.android)
    alias(libs.plugins.compatibility.validator)
}

android {
    namespace = "com.github.foodiestudio.sugar"
    compileSdk = 33

    defaultConfig {
        minSdk = 26
        targetSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
    buildFeatures {
        buildConfig = false
        aidl = false
    }
}

dependencies {
    implementation(sharedLibs.androidx.lifecycle.service)
    implementation(platform(sharedLibs.compose.bom))
    implementation(sharedLibs.compose.ui)
    implementation(libs.modernstorage.storage)
    api(libs.modernstorage.permissions)
    api(sharedLibs.okio)
}
