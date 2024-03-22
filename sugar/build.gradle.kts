import com.android.build.api.dsl.LibraryDefaultConfig

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    // debug only
    val launchAsApplication = false

    if (launchAsApplication) {
        id("foodiestudio.android.application.compose")
    } else {
        id("foodiestudio.android.library.compose")
    }
    alias(libs.plugins.compatibility.validator)
    id("maven-publish")
}

val launchAsApplication = project.plugins.findPlugin("foodiestudio.android.library.compose") == null

android {
    namespace = "com.github.foodiestudio.sugar"

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        if (!launchAsApplication) {
            (this as LibraryDefaultConfig).consumerProguardFiles("consumer-rules.pro")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            )
        }
    }
}

dependencies {
    implementation(sharedLibs.androidx.lifecycle.service)
    implementation(sharedLibs.compose.ui)
    implementation(sharedLibs.compose.foundation)
    api(sharedLibs.okio)
    implementation(libs.documentfile)

    // debugOnly
    debugImplementation(sharedLibs.activity.compose)
    debugImplementation(libs.viewmodel.compose)
    debugImplementation(sharedLibs.compose.material)
    debugImplementation(sharedLibs.coil)
    debugImplementation(libs.coil.video)
}

if (!launchAsApplication) {
    group = "com.github.foodiestudio"
    version = extra["publish.version"].toString()

    publishing {
        publications {
            register<MavenPublication>("release") {
                afterEvaluate {
                    from(components["release"])
                }
            }
        }
    }
}
