import com.android.build.api.dsl.ApplicationDefaultConfig

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

        if (launchAsApplication) {
            (this as ApplicationDefaultConfig).apply {
                applicationId = "com.github.foodiestudio.sugar.app"
                versionCode = 1
                versionName = "1.0.0"
                targetSdk = 33
            }
        } else {
            defaultConfig.consumerProguardFiles("consumer-rules.pro")
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
    api(sharedLibs.okio)
    implementation(libs.documentfile)

    if (launchAsApplication) {
        // debugOnly
        debugImplementation(sharedLibs.activity.compose)
        debugImplementation(sharedLibs.compose.material)
    }
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
