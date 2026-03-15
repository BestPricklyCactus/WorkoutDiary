pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "WorkoutDiary"
include(":app")
include(":core:mvi")
include(":data")
include(":feature:common")
include(":feature:editor:api")
include(":feature:editor:impl")
include(":feature:main:api")
include(":feature:main:impl")
include(":feature:history:api")
include(":feature:history:impl")
include(":feature:workout:api")
include(":feature:workout:impl")
