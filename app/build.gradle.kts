import java.util.Base64

import java.util.Base64

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "ro.ipnet.hosting"
    compileSdk = 35

    defaultConfig {
        applicationId = "ro.ipnet.hosting"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.1.0"
    }

    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.11.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
}


// Decode the approved IP-NET mark into an Android drawable before resource processing.
val decodeIpnetLogo by tasks.registering {
    doLast {
        val encoded = layout.projectDirectory.file("src/main/ipnet_logo.b64").asFile.readText().trim()
        val output = layout.projectDirectory.file("src/main/res/drawable-nodpi/ipnet_logo.png").asFile
        output.parentFile.mkdirs()
        output.writeBytes(Base64.getDecoder().decode(encoded))
    }
}
tasks.named("preBuild").configure { dependsOn(decodeIpnetLogo) }
