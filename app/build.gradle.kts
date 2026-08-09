import java.io.File

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val envVersionCode = System.getenv("VERSION_CODE")?.toIntOrNull() ?: 1
val envVersionName = System.getenv("VERSION_NAME") ?: "0.1.0"

android {
    namespace = "com.leminno.partygames"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.leminno.partygames"
        minSdk = 26
        targetSdk = 35
        versionCode = envVersionCode
        versionName = envVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("PARTYGAMES_KEYSTORE_PATH") ?: "release.jks"
            val storeFileFile = File(keystorePath).let { if (it.isAbsolute) it else file(it) }
            if (storeFileFile.exists()) {
                storeFile = storeFileFile
                storePassword = System.getenv("PARTYGAMES_KEYSTORE_PASSWORD") ?: "PartyGamesRelease2026!"
                keyAlias = System.getenv("PARTYGAMES_KEY_ALIAS") ?: "partygames-key"
                keyPassword = System.getenv("PARTYGAMES_KEY_PASSWORD") ?: "PartyGamesRelease2026!"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val releaseConfig = signingConfigs.findByName("release")
            if (releaseConfig?.storeFile?.exists() == true) {
                signingConfig = releaseConfig
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.okhttp)
}
