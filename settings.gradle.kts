pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("foodiestudio")) {
                useModule("com.github.foodiestudio:convention-plugins:0.3.0")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("sharedLibs") {
            from("io.github.foodiestudio:libs-versions:2023.04.00")
            library("okio", "com.squareup.okio:okio:3.4.0")
        }
    }
}

rootProject.name = "Sugar"
include(":sugar")
