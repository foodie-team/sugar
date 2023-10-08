@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("foodiestudio.android.library.compose")
    alias(libs.plugins.compatibility.validator)
}

android {
    namespace = "com.github.foodiestudio.sugar"

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
        }
    }
}

dependencies {
    implementation(sharedLibs.androidx.lifecycle.service)
    implementation(sharedLibs.compose.ui)
    implementation(libs.modernstorage.storage)
    api(libs.modernstorage.permissions)
    api(sharedLibs.okio)
    implementation(libs.documentfile)
}
