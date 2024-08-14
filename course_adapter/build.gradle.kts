@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm()
    js()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization)
            implementation(libs.kotlinx.datetime)
            implementation(libs.ksoup)
            implementation(libs.ksoup.network)
        }

        jvmTest.dependencies {
            implementation(libs.gson)
            implementation(libs.kotlin.csv.jvm)
            implementation(libs.jsoup)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.kotlin.test)
        }
    }
}