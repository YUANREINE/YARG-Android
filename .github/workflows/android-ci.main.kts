#!/usr/bin/env kotlin

name = "Build and Release APK"

on {
    push {
        branches = listOf("main") // Change if needed
    }
    pull_request {
        branches = listOf("main")
    }
    workflow_dispatch()
}

jobs {
    "build" {
        runsOn = "ubuntu-latest"
        
        steps {
            - uses("actions/checkout@v4")

            - uses("actions/setup-java@v3") {
                with(
                    "distribution" to "temurin",
                    "java-version" to "17"
                )
            }

            - run("chmod +x gradlew")

            - run("./gradlew assembleDebug")

            - uses("actions/upload-artifact@v4") {
                with(
                    "name" to "YARG-Android-APK",
                    "path" to "app/build/outputs/apk/debug/app-debug.apk"
                )
            }
        }
    }

    "release" {
        needs = listOf("build")
        runsOn = "ubuntu-latest"

        steps {
            - uses("actions/download-artifact@v4") {
                with(
                    "name" to "YARG-Android-APK",
                    "path" to "app/build/outputs/apk/debug/"
                )
            }

            - uses("softprops/action-gh-release@v2") {
                with(
                    "tag_name" to "v1.0.${{ github.run_number }}",
                    "release_name" to "YARG-Android Release v1.0.${{ github.run_number }}",
                    "body" to "Automated release of YARG-Android APK.",
                    "draft" to false,
                    "prerelease" to false,
                    "files" to "app/build/outputs/apk/debug/app-debug.apk"
                )
                env("GITHUB_TOKEN", "${{ secrets.GITHUB_TOKEN }}")
            }
        }
    }
}
