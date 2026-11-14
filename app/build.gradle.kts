import java.util.Base64

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "ro.ipnet.hosting"
    compileSdk = 35

    signingConfigs {
        create("release") {
            val storeFilePath = System.getenv("RELEASE_STORE_FILE")
            if (!storeFilePath.isNullOrBlank()) {
                storeFile = file(storeFilePath)
                storePassword = System.getenv("RELEASE_STORE_PASSWORD")
                keyAlias = System.getenv("RELEASE_KEY_ALIAS")
                keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
            }
        }
    }

    defaultConfig {
        applicationId = "ro.ipnet.hosting"
        minSdk = 26
        targetSdk = 35
        versionCode = providers.gradleProperty("appVersionCode").orElse("1").get().toInt()
        versionName = providers.gradleProperty("appVersionName").orElse("1.0.0").get()
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.11.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
}

val decodeIpNetLogo by tasks.registering {
    val source = layout.projectDirectory.file("src/main/ipnet_logo.b64")
    val output = layout.projectDirectory.file("src/main/res/drawable-nodpi/ipnet_logo.png")
    inputs.file(source)
    outputs.file(output)
    doLast {
        val encoded = source.asFile.readText().filterNot(Char::isWhitespace)
        val bytes = Base64.getDecoder().decode(encoded)
        output.asFile.parentFile.mkdirs()
        output.asFile.writeBytes(bytes)
    }
}

tasks.named("preBuild").configure {
    dependsOn(decodeIpNetLogo)
}
