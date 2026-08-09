import java.io.File

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val envVersionCode = System.getenv("VERSION_CODE")?.toIntOrNull() ?: 1
val envVersionName = System.getenv("VERSION_NAME") ?: "0.1.0"

android {
    namespace = "com.leminno.partygames"
    compileSdk = 35

    buildFeatures {
        compose = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.leminno.partygames"
        minSdk = 26
        targetSdk = 35
        versionCode = envVersionCode
        versionName = envVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val aiKey = System.getenv("AI_GATEWAY_KEY") ?: "leminno_apps_Key"
        buildConfigField("String", "AI_GATEWAY_KEY", "\"$aiKey\"")
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("PARTYGAMES_KEYSTORE_PATH") ?: "release.jks"
            val storeFileFile = File(keystorePath).let { if (it.isAbsolute) it else file(it) }
            val envStorePassword = System.getenv("PARTYGAMES_KEYSTORE_PASSWORD")
            val envKeyAlias = System.getenv("PARTYGAMES_KEY_ALIAS")
            val envKeyPassword = System.getenv("PARTYGAMES_KEY_PASSWORD")

            if (storeFileFile.exists() && !envStorePassword.isNullOrEmpty() && !envKeyAlias.isNullOrEmpty() && !envKeyPassword.isNullOrEmpty()) {
                storeFile = storeFileFile
                storePassword = envStorePassword
                keyAlias = envKeyAlias
                keyPassword = envKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val releaseConfig = signingConfigs.findByName("release")
            if (releaseConfig?.storeFile?.exists() == true && !releaseConfig.storePassword.isNullOrEmpty()) {
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

    // Jetpack Compose BOM & Libraries
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
}

