pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            name = "Github Packages"
            url = uri("https://maven.pkg.github.com/foodiestudio/public")
            credentials {
                // your GitHub username
                username = extra["github.username"] as String
                // https://github.com/settings/tokens
                password = extra["github.token"] as String
            }
        }
    }
    versionCatalogs {
        create("sharedLibs") {
            from("com.github.foodiestudio:libs-versions:2023.01.00")
            version("lifecycle", "2.6.1")
        }
    }
}

rootProject.name = "Sugar"
include(":sugar")
