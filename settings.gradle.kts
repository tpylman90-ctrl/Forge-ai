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
    }
}
rootProject.name = "ForgeAI"
include(":app")
// G1 uses the pinned official llama.cpp Android library. Run scripts/setup-llama.sh
// before any local Gradle build (CI runs this automatically).
include(":llamaAndroid")
project(":llamaAndroid").projectDir = file("vendor/llama.cpp/examples/llama.android/lib")
