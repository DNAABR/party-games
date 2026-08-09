################################################################################
#  new-android-app.ps1
#  Leminno Android App Bootstrap Script
#
#  Usage:
#    .\new-android-app.ps1
#
#  What it does:
#    1. Prompts for app identity details
#    2. Generates a release keystore (.jks) using Android Studio's bundled keytool
#    3. Creates KEYSTORE.md  (credentials, fingerprints, Play Store checklist)
#    4. Creates techstack.md (architecture & tech decisions template)
#    5. Creates AGENTS.md   (AI coding agent context file)
#    6. Creates .gitignore
#    7. Creates .github/workflows/android.yml  (CI/CD pipeline)
#    8. Prints a summary of next steps
#
#  Requirements:
#    - Android Studio installed (for keytool)
#    - PowerShell 5.1+
#
################################################################################

param (
    [string]$AppName,
    [string]$PackageName,
    [string]$OrgName,
    [string]$KeyAlias,
    [string]$KeyPassword,
    [string]$TargetDir,
    [string]$VersionName,
    [string]$GithubRepo,
    [string]$KeytoolPath
)

# ─── Colour helpers ───────────────────────────────────────────────────────────
function Write-Step  { param($msg) Write-Host "`n>  $msg" -ForegroundColor Cyan }
function Write-Ok    { param($msg) Write-Host "   OK  $msg" -ForegroundColor Green }
function Write-Warn  { param($msg) Write-Host "   !!  $msg" -ForegroundColor Yellow }
function Write-Title { param($msg) Write-Host "`n======================================================`n   $msg`n======================================================" -ForegroundColor Magenta }

Write-Title "Leminno Android App Bootstrap"

# ─── Collect inputs ───────────────────────────────────────────────────────────
Write-Host ""

$APP_NAME = $AppName
if (-not $APP_NAME) {
    if ([Environment]::UserInteractive) { $APP_NAME = Read-Host "App Name (e.g. Mirror)" }
}
if (-not $APP_NAME) { $APP_NAME = "Party Games" }

$APP_SLUG       = ($APP_NAME.ToLower() -replace '\s+', '-')         # mirror
$APP_ID_SUFFIX  = ($APP_NAME.ToLower() -replace '[^a-z0-9]', '')    # mirror
$APP_SLUG_UPPER = $APP_NAME.ToUpper() -replace '[^A-Z0-9]', ''      # MIRROR

$PACKAGE_NAME = $PackageName
if (-not $PACKAGE_NAME) {
    if ([Environment]::UserInteractive) { $PACKAGE_NAME = Read-Host "Package Name (e.g. com.leminno.$APP_ID_SUFFIX)" }
}
if (-not $PACKAGE_NAME) { $PACKAGE_NAME = "com.leminno.$APP_ID_SUFFIX" }

$ORG_NAME = $OrgName
if (-not $ORG_NAME) {
    if ([Environment]::UserInteractive) { $ORG_NAME = Read-Host "Organisation Name (e.g. Leminno)" }
}
if (-not $ORG_NAME) { $ORG_NAME = "Leminno" }

$KEY_ALIAS_DEF  = "$APP_ID_SUFFIX-key"
$KEY_ALIAS = $KeyAlias
if (-not $KEY_ALIAS) {
    if ([Environment]::UserInteractive) { $KEY_ALIAS = Read-Host "Key Alias (default: $KEY_ALIAS_DEF)" }
}
if (-not $KEY_ALIAS) { $KEY_ALIAS = $KEY_ALIAS_DEF }

$KEY_PASS_DEF   = "${APP_NAME}Release$(Get-Date -Format 'yyyy')!"
$KEY_PASSWORD = $KeyPassword
if (-not $KEY_PASSWORD) {
    if ([Environment]::UserInteractive) { $KEY_PASSWORD = Read-Host "Keystore & Key Password (default: $KEY_PASS_DEF)" }
}
if (-not $KEY_PASSWORD) { $KEY_PASSWORD = $KEY_PASS_DEF }

$TARGET_DIR = $TargetDir
if (-not $TARGET_DIR) {
    if ([Environment]::UserInteractive) { $TARGET_DIR = Read-Host "Target Directory (full path, e.g. E:\Codes\${APP_NAME}-App)" }
}
if (-not $TARGET_DIR) { $TARGET_DIR = $PSScriptRoot }

$VER_DEF        = "0.1.0"
$VERSION_NAME = $VersionName
if (-not $VERSION_NAME) {
    if ([Environment]::UserInteractive) { $VERSION_NAME = Read-Host "Initial Version Name (default: $VER_DEF)" }
}
if (-not $VERSION_NAME) { $VERSION_NAME = $VER_DEF }

$KEYSTORE_REL   = "app\release.jks"
$KEYSTORE_PATH  = Join-Path $TARGET_DIR $KEYSTORE_REL

# ─── Validate / locate keytool ────────────────────────────────────────────────
Write-Step "Locating keytool"
if (-not $KeytoolPath -or -not (Test-Path $KeytoolPath)) {
    $fallbacks = @(
        "D:\New folder\New folder\jbr\bin\keytool.exe",
        (& { $reg = Get-ItemProperty 'HKLM:\Software\Android Studio' -ErrorAction SilentlyContinue; if ($reg -and $reg.Path) { Join-Path $reg.Path "jbr\bin\keytool.exe" } }),
        "$env:JAVA_HOME\bin\keytool.exe",
        (& { $c = Get-Command keytool -ErrorAction SilentlyContinue; if ($c) { $c.Source } }),
        "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe",
        "F:\AndroidStudio\jbr\bin\keytool.exe"
    )
    foreach ($f in $fallbacks) {
        if ($f -and (Test-Path $f)) { $KeytoolPath = $f; break }
    }
}
$SKIP_KEYSTORE = $false
if (-not $KeytoolPath -or -not (Test-Path $KeytoolPath)) {
    Write-Warn "keytool.exe not found. Skipping keystore generation."
    Write-Warn "Set -KeytoolPath to your Android Studio JBR keytool and re-run."
    $SKIP_KEYSTORE = $true
    $sha1   = "PENDING - run: keytool -list -v -keystore app/release.jks"
    $sha256 = "PENDING - run: keytool -list -v -keystore app/release.jks"
} else {
    Write-Ok "Found keytool: $KeytoolPath"
}

# ─── Create directories ───────────────────────────────────────────────────────
Write-Step "Creating directory structure"
@(
    "$TARGET_DIR\app",
    "$TARGET_DIR\.github\workflows",
    "$TARGET_DIR\docs"
) | ForEach-Object { New-Item -ItemType Directory -Force -Path $_ | Out-Null }
Write-Ok "Directories ready under $TARGET_DIR"

# ─── Generate release keystore ────────────────────────────────────────────────
if (-not $SKIP_KEYSTORE) {
    Write-Step "Generating release keystore"
    $dname = "CN=$APP_NAME, OU=Development, O=$ORG_NAME, L=City, ST=State, C=US"
    & $KeytoolPath -genkeypair -v `
        -keystore $KEYSTORE_PATH `
        -alias    $KEY_ALIAS `
        -keyalg   RSA -keysize 2048 -validity 10000 `
        -dname    $dname `
        -storepass $KEY_PASSWORD `
        -keypass   $KEY_PASSWORD 2>&1 | Out-Null
    Write-Ok "Keystore created: $KEYSTORE_REL"

    # Extract fingerprints
    $ks_info = & $KeytoolPath -list -v `
        -keystore $KEYSTORE_PATH -alias $KEY_ALIAS -storepass $KEY_PASSWORD 2>&1
    $sha1   = ($ks_info | Select-String "SHA1:")   | ForEach-Object { ($_.Line.Trim() -replace 'SHA1:\s*','').Trim() } | Select-Object -First 1
    $sha256 = ($ks_info | Select-String "SHA256:") | ForEach-Object { ($_.Line.Trim() -replace 'SHA256:\s*','').Trim() } | Select-Object -First 1
    Write-Ok "SHA-1:   $sha1"
    Write-Ok "SHA-256: $sha256"
}

# ─── .gitignore ───────────────────────────────────────────────────────────────
Write-Step "Writing .gitignore"
Set-Content "$TARGET_DIR\.gitignore" -Encoding UTF8 -Value @"
*.iml
.gradle/
.idea/
.kotlin/
build/
local.properties
captures/
.externalNativeBuild/
.cxx/
*.apk
*.aab
*.ap_
*.dex
!gradle/wrapper/gradle-wrapper.jar
"@
Write-Ok ".gitignore written"

# ─── GitHub Actions CI/CD workflow ────────────────────────────────────────────
Write-Step "Writing .github/workflows/android.yml"
Set-Content "$TARGET_DIR\.github\workflows\android.yml" -Encoding UTF8 -Value @"
name: Android build

on:
  push:
    branches: [main, dev]
  pull_request:
    branches: [main, dev]
  workflow_dispatch:

concurrency:
  group: `${{ github.workflow }}-`${{ github.ref }}
  cancel-in-progress: true

env:
  GRADLE_VERSION: "8.13"
  ${APP_SLUG_UPPER}_KEYSTORE_PATH: "release.jks"
  ${APP_SLUG_UPPER}_KEYSTORE_PASSWORD: `${{ secrets.${APP_SLUG_UPPER}_KEYSTORE_PASSWORD || '$KEY_PASSWORD' }}
  ${APP_SLUG_UPPER}_KEY_ALIAS: `${{ secrets.${APP_SLUG_UPPER}_KEY_ALIAS || '$KEY_ALIAS' }}
  ${APP_SLUG_UPPER}_KEY_PASSWORD: `${{ secrets.${APP_SLUG_UPPER}_KEY_PASSWORD || '$KEY_PASSWORD' }}
  VERSION_CODE: `${{ github.run_number }}
  VERSION_NAME: "0.1.`${{ github.run_number }}"

jobs:
  build:
    name: Assemble, test and lint
    runs-on: ubuntu-latest
    timeout-minutes: 45

    steps:
      - name: Check out repository
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "17"

      - name: Set up Android SDK
        uses: android-actions/setup-android@v3
        with:
          packages: "platforms;android-36 build-tools;36.0.0"

      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: `${{ env.GRADLE_VERSION }}

      - name: Materialise Gradle wrapper
        run: |
          if [ ! -f gradlew ]; then
            echo "No wrapper found. Generating one with Gradle `${GRADLE_VERSION}."
            gradle wrapper --gradle-version "`${GRADLE_VERSION}" --no-daemon
            chmod +x gradlew
          else
            echo "Wrapper already committed."
            chmod +x gradlew
          fi

      - name: Upload Gradle wrapper for committing
        uses: actions/upload-artifact@v4
        with:
          name: gradle-wrapper
          path: |
            gradlew
            gradlew.bat
            gradle/wrapper/
          if-no-files-found: warn

      - name: Assemble debug
        run: ./gradlew :app:assembleDebug --stacktrace

      - name: Unit tests
        run: ./gradlew :app:testDebugUnitTest --stacktrace

      - name: Lint
        run: ./gradlew :app:lintDebug --stacktrace

      - name: Assemble release APK
        if: github.ref == 'refs/heads/main' || github.event_name == 'workflow_dispatch'
        run: ./gradlew :app:assembleRelease --stacktrace

      - name: Bundle release AAB
        if: github.ref == 'refs/heads/main' || github.event_name == 'workflow_dispatch'
        run: ./gradlew :app:bundleRelease --stacktrace

      - name: Upload debug APK
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: $APP_SLUG-debug-apk
          path: app/build/outputs/apk/debug/*.apk
          if-no-files-found: ignore

      - name: Upload release APK
        if: (github.ref == 'refs/heads/main' || github.event_name == 'workflow_dispatch') && success()
        uses: actions/upload-artifact@v4
        with:
          name: $APP_SLUG-release-apk-v0.1.`${{ github.run_number }}
          path: app/build/outputs/apk/release/*.apk
          if-no-files-found: ignore

      - name: Upload release AAB
        if: (github.ref == 'refs/heads/main' || github.event_name == 'workflow_dispatch') && success()
        uses: actions/upload-artifact@v4
        with:
          name: $APP_SLUG-release-aab-v0.1.`${{ github.run_number }}
          path: app/build/outputs/bundle/release/*.aab
          if-no-files-found: ignore

      - name: Upload reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: $APP_SLUG-reports
          path: |
            app/build/reports/
            app/build/outputs/logs/
          if-no-files-found: ignore
"@
Write-Ok "android.yml written"

# ─── KEYSTORE.md ──────────────────────────────────────────────────────────────
Write-Step "Writing KEYSTORE.md"
$TODAY = Get-Date -Format "MMMM dd, yyyy"
Set-Content "$TARGET_DIR\KEYSTORE.md" -Encoding UTF8 -Value @"
# Android Release Keystore & App Publishing Guide

This document contains full details for the Android Release App Signing KeyStore used for **$APP_NAME**, instructions for Gradle and CI setup, and a complete pre-launch checklist required before publishing the app to the Google Play Store.

---

## 1. Release Keystore Specifications

| Property | Value |
| :--- | :--- |
| **Keystore File** | ``app/release.jks`` |
| **Relative Path (from app module)** | ``release.jks`` |
| **Key Alias** | ``$KEY_ALIAS`` |
| **Keystore Password** | ``$KEY_PASSWORD`` |
| **Key Password** | ``$KEY_PASSWORD`` |
| **Key Algorithm** | RSA 2048-bit |
| **Signature Algorithm** | SHA384withRSA |
| **Validity Period** | 10,000 days (~27 years from $TODAY) |
| **Distinguished Name** | ``CN=$APP_NAME, OU=Development, O=$ORG_NAME, L=City, ST=State, C=US`` |

### Certificate Fingerprints
- **SHA-1**: ``$sha1``
- **SHA-256**: ``$sha256``

> [!WARNING]
> Keep a secure offline backup of ``app/release.jks``. If lost, publishing updates to Google Play under ``$PACKAGE_NAME`` will not be possible.

---

## 2. Build & CI Configuration

### Local Builds
Gradle auto-detects ``app/release.jks``. Override via environment variables:
- ``${APP_SLUG_UPPER}_KEYSTORE_PATH``
- ``${APP_SLUG_UPPER}_KEYSTORE_PASSWORD``
- ``${APP_SLUG_UPPER}_KEY_ALIAS``
- ``${APP_SLUG_UPPER}_KEY_PASSWORD``

### CI (GitHub Actions)
| Branch | Debug APK | Release APK | Release AAB | Versioning |
| :--- | :--- | :--- | :--- | :--- |
| ``dev`` | Yes | No | No | Local fallback |
| ``main`` | Yes | Yes | Yes | Auto (github.run_number) |

---

## 3. Pre-Upload Checklist

### A. Google Play Console
- [ ] Active developer account (\$25 registration fee)
- [ ] Identity verification complete

### B. App Identity & Metadata
- **Package Name**: ``$PACKAGE_NAME``
- **App Title**: Max 30 characters
- **Short Description**: Max 80 characters
- **Full Description**: Max 4,000 characters

### C. Graphic Assets
- [ ] App Icon: 512 x 512 px PNG (32-bit with alpha)
- [ ] Feature Graphic: 1024 x 500 px PNG or JPEG
- [ ] Phone Screenshots: minimum 2 (16:9 or 9:16 aspect)
- [ ] Tablet Screenshots (optional): 7-inch, 10-inch

### D. Legal & Privacy
- [ ] Hosted Privacy Policy URL
- [ ] Data Safety Form completed in Play Console
- [ ] Content Rating questionnaire (IARC) complete
- [ ] Target Audience declared

---

## 4. Useful Keytool Commands

``````bash
# View certificate details
keytool -list -v -keystore app/release.jks -alias $KEY_ALIAS

# Export public certificate
keytool -exportcert -alias $KEY_ALIAS -keystore app/release.jks -file $APP_SLUG-release.crt
``````
"@
Write-Ok "KEYSTORE.md written"

# ─── techstack.md ─────────────────────────────────────────────────────────────
Write-Step "Writing techstack.md"
Set-Content "$TARGET_DIR\techstack.md" -Encoding UTF8 -Value @"
# $APP_NAME

<!-- TODO: one-line app description -->

---

## Tech Stack Overview

| Category | Technology |
| :--- | :--- |
| **Language** | [Kotlin](https://kotlinlang.org/) |
| **Platform** | Native Android |
| **Build System** | Gradle with Kotlin DSL (``.gradle.kts``) |
| **Min Android Version** | Android 8.0 (API Level 26+) |
| **Backend** | None (Fully Local / On-Device) |

---

## Architecture & Design

* **Architecture**: <!-- e.g. Single Activity, MVVM, MVI -->
* **UI Framework**: <!-- e.g. Android Views / Jetpack Compose -->
* **State Management**: <!-- e.g. ViewModel + StateFlow -->
* **Storage**: <!-- e.g. DataStore, Room -->

---

## Key Features

<!-- List primary features here -->

---

## Getting Started

### Prerequisites
* **Android Studio** (Latest Stable)
* **JDK 17** or higher
* **Package Name**: ``$PACKAGE_NAME``
"@
Write-Ok "techstack.md written"

# ─── AGENTS.md ────────────────────────────────────────────────────────────────
Write-Step "Writing AGENTS.md"
Set-Content "$TARGET_DIR\AGENTS.md" -Encoding UTF8 -Value @"
# $APP_NAME — AI Agent Context

This file provides context for AI coding agents (e.g. Antigravity / Gemini) working in this repository.

---

## Project Identity

| Property | Value |
| :--- | :--- |
| **App Name** | $APP_NAME |
| **Package Name** | $PACKAGE_NAME |
| **Organisation** | $ORG_NAME |
| **Platform** | Native Android (Kotlin) |
| **Min SDK** | 26 (Android 8.0) |
| **Build System** | Gradle Kotlin DSL |

---

## Repository Structure

``````
.
├── app/
│   ├── src/main/             # Source code & resources
│   ├── build.gradle.kts      # App-level build (signing, versioning, deps)
│   └── release.jks           # Release signing keystore (committed — private repo)
├── .github/
│   └── workflows/
│       └── android.yml       # CI/CD pipeline (GitHub Actions)
├── gradle/                   # Gradle wrapper files
├── build.gradle.kts          # Root Gradle config
├── settings.gradle.kts       # Module settings
├── KEYSTORE.md               # Keystore credentials & Play Store checklist
├── techstack.md              # Tech stack & architecture decisions
├── AGENTS.md                 # This file — AI agent context
├── CHANGELOG.md              # Version history
└── README.md                 # Public documentation
``````

---

## CI/CD Summary

| Branch | Debug APK | Release APK | Release AAB | Versioning |
| :--- | :--- | :--- | :--- | :--- |
| ``dev`` | Yes | No | No | Local fallback (versionCode=1) |
| ``main`` | Yes | Yes | Yes | Auto: VERSION_CODE=github.run_number |

---

## Signing Configuration

- Keystore: ``app/release.jks`` (committed — private repo only)
- Alias: ``$KEY_ALIAS``
- Env vars: ``${APP_SLUG_UPPER}_KEYSTORE_PATH``, ``${APP_SLUG_UPPER}_KEYSTORE_PASSWORD``, ``${APP_SLUG_UPPER}_KEY_ALIAS``, ``${APP_SLUG_UPPER}_KEY_PASSWORD``
- Full details: see ``KEYSTORE.md``

---

## Coding Conventions

- Kotlin only — no Java source in ``app/src/main``
- Gradle Kotlin DSL only — no Groovy
- Keep ``AndroidManifest.xml`` permissions minimal and well-commented
- ``versionCode`` and ``versionName`` driven by CI env vars — do not hard-code

---

## Notes for AI Agents

- ``release.jks`` is intentionally committed because this is a **private repository**. Do not suggest moving it to GitHub Secrets only.
- Do not hard-code ``versionCode`` or ``versionName`` in ``build.gradle.kts`` — they are controlled by CI environment variables.
- Always preserve existing comments in ``AndroidManifest.xml`` and ``build.gradle.kts``.

---

## Leminno Social Links

These links should appear in the app's settings, about screen, or footer where appropriate.

| Platform | URL |
| :--- | :--- |
| **LinkedIn** | https://www.linkedin.com/company/leminno/ |
| **Discord** | https://discord.gg/uTmQnkMVkA |

When adding to a settings screen, use ``Intent(Intent.ACTION_VIEW, Uri.parse(url))`` to open in the default browser.

---

## AI Provider Gateway Integration

$APP_NAME is connected to the Leminno Protected AI Gateway hosted at ``https://ai.leminno.com/``. AI coding agents and app features can easily leverage AI capabilities using the pre-configured ``AiGateway`` utility (``$PACKAGE_NAME.data.remote.AiGateway``).

| Property | Value |
| :--- | :--- |
| **Gateway URL** | ``https://ai.leminno.com/api/chat`` |
| **Default Secret Key** | ``leminno_apps_Key`` (passed in ``x-api-key`` header) |
| **App Identity** | ``$APP_NAME`` (passed in ``x-app-id`` header) |
| **Utility Location** | ``app/src/main/java/$($PACKAGE_NAME -replace '\.', '/')/data/remote/AiGateway.kt`` |

### Usage Examples

#### Callback Style
````kotlin
AiGateway.askAi("Your prompt message here") { response, error ->
    if (response != null) {
        val text = response.text
    }
}
````

#### Suspend Function (ViewModel / Coroutines)
````kotlin
viewModelScope.launch {
    AiGateway.askAiSuspend("Your prompt message here")
        .onSuccess { response ->
            val text = response.text
        }
}
````
"@
# ─── Android App Codebase Template ───────────────────────────────────────────
Write-Step "Scaffolding Android App Template codebase"

$PACKAGE_DIR = Join-Path $TARGET_DIR ("app\src\main\java\" + ($PACKAGE_NAME -replace '\.', '\'))
$REMOTE_DIR = Join-Path $PACKAGE_DIR "data\remote"
$RES_LAYOUT_DIR = Join-Path $TARGET_DIR "app\src\main\res\layout"
$RES_VALUES_DIR = Join-Path $TARGET_DIR "app\src\main\res\values"
$RES_DRAWABLE_DIR = Join-Path $TARGET_DIR "app\src\main\res\drawable"
$RES_MIPMAP_DIR = Join-Path $TARGET_DIR "app\src\main\res\mipmap-anydpi-v26"
$GRADLE_WRAPPER_DIR = Join-Path $TARGET_DIR "gradle\wrapper"

@(
    $PACKAGE_DIR,
    $REMOTE_DIR,
    $RES_LAYOUT_DIR,
    $RES_VALUES_DIR,
    $RES_DRAWABLE_DIR,
    $RES_MIPMAP_DIR,
    $GRADLE_WRAPPER_DIR
) | ForEach-Object { New-Item -ItemType Directory -Force -Path $_ | Out-Null }

Set-Content "$TARGET_DIR\gradle.properties" -Encoding UTF8 -Value @"
# Project-wide Gradle settings.

# Enable AndroidX support
android.useAndroidX=true

# Enable Non-transitive R classes for faster builds
android.nonTransitiveRClass=true

# JVM args for Gradle daemon
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
"@

Set-Content "$TARGET_DIR\settings.gradle.kts" -Encoding UTF8 -Value @"
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

rootProject.name = "$APP_NAME"
include(":app")
"@

Set-Content "$TARGET_DIR\build.gradle.kts" -Encoding UTF8 -Value @"
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}
"@

New-Item -ItemType Directory -Force -Path "$TARGET_DIR\gradle" | Out-Null
Set-Content "$TARGET_DIR\gradle\libs.versions.toml" -Encoding UTF8 -Value @"
[versions]
agp = "8.8.0"
kotlin = "2.1.0"
okhttp = "4.12.0"
coreKtx = "1.15.0"
appcompat = "1.7.0"
material = "1.12.0"
activity = "1.10.0"
constraintlayout = "2.2.0"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "appcompat" }
material = { group = "com.google.android.material", name = "material", version.ref = "material" }
androidx-activity = { group = "androidx.activity", name = "activity", version.ref = "activity" }
androidx-constraintlayout = { group = "androidx.constraintlayout", name = "constraintlayout", version.ref = "constraintlayout" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
"@

Set-Content "$TARGET_DIR\gradle\wrapper\gradle-wrapper.properties" -Encoding UTF8 -Value @"
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.13-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
"@

Set-Content "$TARGET_DIR\app\build.gradle.kts" -Encoding UTF8 -Value @"
import java.io.File

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val envVersionCode = System.getenv("VERSION_CODE")?.toIntOrNull() ?: 1
val envVersionName = System.getenv("VERSION_NAME") ?: "$VERSION_NAME"

android {
    namespace = "$PACKAGE_NAME"
    compileSdk = 35

    defaultConfig {
        applicationId = "$PACKAGE_NAME"
        minSdk = 26
        targetSdk = 35
        versionCode = envVersionCode
        versionName = envVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("${APP_SLUG_UPPER}_KEYSTORE_PATH") ?: "release.jks"
            val storeFileFile = File(keystorePath).let { if (it.isAbsolute) it else file(it) }
            if (storeFileFile.exists()) {
                storeFile = storeFileFile
                storePassword = System.getenv("${APP_SLUG_UPPER}_KEYSTORE_PASSWORD") ?: "$KEY_PASSWORD"
                keyAlias = System.getenv("${APP_SLUG_UPPER}_KEY_ALIAS") ?: "$KEY_ALIAS"
                keyPassword = System.getenv("${APP_SLUG_UPPER}_KEY_PASSWORD") ?: "$KEY_PASSWORD"
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
"@

$APP_THEME_NAME = "Theme." + ($APP_NAME -replace '[^a-zA-Z0-9]', '')

Set-Content "$TARGET_DIR\app\src\main\AndroidManifest.xml" -Encoding UTF8 -Value @"
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/$APP_THEME_NAME">

        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

    </application>

</manifest>
"@

Set-Content "$REMOTE_DIR\AiGateway.kt" -Encoding UTF8 -Value @"
package $PACKAGE_NAME.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

/**
 * Data class representing the response returned from Leminno AI Gateway.
 */
data class AiResponse(
    val text: String,
    val provider: String? = null,
    val model: String? = null,
    val rawJson: String? = null
)

/**
 * Client service for interacting with the Leminno Protected AI Gateway (https://ai.leminno.com).
 */
object AiGateway {
    private const val BASE_URL = "https://ai.leminno.com/api/chat"
    private const val DEFAULT_SECRET_KEY = "leminno_apps_Key"
    private const val APP_ID = "$APP_NAME"

    private val client: OkHttpClient by lazy { OkHttpClient() }

    /**
     * Asynchronously calls Leminno AI Gateway using standard OkHttp enqueue callback.
     */
    fun askAi(
        promptText: String,
        apiKey: String = DEFAULT_SECRET_KEY,
        appId: String = APP_ID,
        onResult: (response: AiResponse?, error: String?) -> Unit
    ) {
        val jsonPayload = JSONObject().apply {
            put("prompt", promptText)
        }.toString()

        val body = jsonPayload.toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(BASE_URL)
            .addHeader("Content-Type", "application/json")
            .addHeader("x-api-key", apiKey)
            .addHeader("x-app-id", appId)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult(null, e.message ?: "Network error")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    try {
                        val jsonObj = JSONObject(responseBody)
                        val aiAnswer = jsonObj.optString("response")
                        val providerName = jsonObj.optString("provider").ifEmpty { null }
                        val modelName = jsonObj.optString("model").ifEmpty { null }
                        onResult(
                            AiResponse(
                                text = aiAnswer,
                                provider = providerName,
                                model = modelName,
                                rawJson = responseBody
                            ),
                            null
                        )
                    } catch (e: Exception) {
                        onResult(null, "JSON parsing error: `${e.message}")
                    }
                } else {
                    onResult(null, "HTTP `${response.code}: `$responseBody")
                }
            }
        })
    }

    /**
     * Coroutine suspend version of askAi for convenient usage within ViewModel / CoroutineScope.
     */
    suspend fun askAiSuspend(
        promptText: String,
        apiKey: String = DEFAULT_SECRET_KEY,
        appId: String = APP_ID
    ): Result<AiResponse> = withContext(Dispatchers.IO) {
        val jsonPayload = JSONObject().apply {
            put("prompt", promptText)
        }.toString()

        val body = jsonPayload.toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(BASE_URL)
            .addHeader("Content-Type", "application/json")
            .addHeader("x-api-key", apiKey)
            .addHeader("x-app-id", appId)
            .post(body)
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            if (response.isSuccessful && responseBody != null) {
                val jsonObj = JSONObject(responseBody)
                val aiAnswer = jsonObj.optString("response")
                val providerName = jsonObj.optString("provider").ifEmpty { null }
                val modelName = jsonObj.optString("model").ifEmpty { null }
                Result.success(
                    AiResponse(
                        text = aiAnswer,
                        provider = providerName,
                        model = modelName,
                        rawJson = responseBody
                    )
                )
            } else {
                Result.failure(IOException("HTTP `${response.code}: `$responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
"@

Set-Content "$PACKAGE_DIR\MainActivity.kt" -Encoding UTF8 -Value @"
package $PACKAGE_NAME

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
"@

Set-Content "$RES_LAYOUT_DIR\activity_main.xml" -Encoding UTF8 -Value @"
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="24dp"
    tools:context=".MainActivity">

    <com.google.android.material.card.MaterialCardView
        android:id="@+id/cardWelcome"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        app:cardCornerRadius="16dp"
        app:cardElevation="4dp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="24dp"
            android:gravity="center">

            <TextView
                android:id="@+id/tvAppName"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/app_name"
                android:textSize="28sp"
                android:textStyle="bold"
                android:textColor="?attr/colorPrimary"/>

            <TextView
                android:id="@+id/tvWelcomeMessage"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="8dp"
                android:text="@string/welcome_message"
                android:textSize="16sp"
                android:gravity="center"
                android:textColor="?attr/colorOnSurfaceVariant"/>

        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>

</androidx.constraintlayout.widget.ConstraintLayout>
"@

Set-Content "$RES_VALUES_DIR\strings.xml" -Encoding UTF8 -Value @"
<resources>
    <string name="app_name">$APP_NAME</string>
    <string name="welcome_message">Welcome to $APP_NAME</string>
</resources>
"@

Set-Content "$RES_VALUES_DIR\colors.xml" -Encoding UTF8 -Value @"
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="purple_500">#6200EE</color>
    <color name="purple_700">#3700B3</color>
    <color name="teal_200">#03DAC5</color>
    <color name="teal_700">#018786</color>
    <color name="black">#FF000000</color>
    <color name="white">#FFFFFFFF</color>
</resources>
"@

Set-Content "$RES_VALUES_DIR\themes.xml" -Encoding UTF8 -Value @"
<resources>
    <style name="$APP_THEME_NAME" parent="Theme.Material3.DayNight.NoActionBar">
        <item name="colorPrimary">@color/purple_500</item>
    </style>
</resources>
"@

Set-Content "$RES_MIPMAP_DIR\ic_launcher.xml" -Encoding UTF8 -Value @"
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
"@

Set-Content "$RES_MIPMAP_DIR\ic_launcher_round.xml" -Encoding UTF8 -Value @"
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
"@

Set-Content "$RES_DRAWABLE_DIR\ic_launcher_background.xml" -Encoding UTF8 -Value @"
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#6200EE"
        android:pathData="M0,0h108v108h-108z" />
</vector>
"@

Set-Content "$RES_DRAWABLE_DIR\ic_launcher_foreground.xml" -Encoding UTF8 -Value @"
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M54,30L24,54h9v24h18V63h6v15h18V54h9L54,30z" />
</vector>
"@

Write-Ok "Android App Codebase Template generated"

# ─── Git repository setup ─────────────────────────────────────────────────────
Write-Step "Setting up git repository"
$GITHUB_REPO = $GithubRepo
if (-not $GITHUB_REPO -and [Environment]::UserInteractive) {
    $GITHUB_REPO = Read-Host "GitHub remote URL (leave blank to skip, e.g. git@github.com:LRxDarkDevil/$APP_NAME-App.git)"
}

push-location $TARGET_DIR

# Initialise repo if not already done
if (-not (Test-Path ".git")) {
    git init | Out-Null
    Write-Ok "git init complete"
} else {
    Write-Ok "git repo already initialised"
}

# Set remote if provided
if ($GITHUB_REPO) {
    $existing = git remote get-url origin 2>$null
    if ($existing) {
        git remote set-url origin $GITHUB_REPO | Out-Null
        Write-Ok "Updated remote origin -> $GITHUB_REPO"
    } else {
        git remote add origin $GITHUB_REPO | Out-Null
        Write-Ok "Added remote origin -> $GITHUB_REPO"
    }
}

# Stage all generated files
git add . | Out-Null
Write-Ok "git add . complete"

# Initial commit on main
git commit -m "chore: bootstrap $APP_NAME" | Out-Null
Write-Ok "Initial commit on main"

# Rename default branch to main (in case git defaulted to master)
$currentBranch = git rev-parse --abbrev-ref HEAD 2>$null
if ($currentBranch -ne "main") {
    git branch -M main | Out-Null
    Write-Ok "Renamed branch to main"
}

# Create dev branch if it does not exist
$devExists = git branch --list dev
if (-not $devExists) {
    git checkout -b dev | Out-Null
    Write-Ok "Created and switched to dev branch"
} else {
    Write-Ok "dev branch already exists"
}

# Switch back to main so the repo starts on main
git checkout main | Out-Null
Write-Ok "Switched back to main"

pop-location

# ─── Final summary ────────────────────────────────────────────────────────────
Write-Title "Bootstrap Complete!"
Write-Host ""
Write-Host "Files created in: $TARGET_DIR" -ForegroundColor White
Write-Host ""
Write-Host "Branches created:" -ForegroundColor Cyan
Write-Host "  main  (current)"
Write-Host "  dev"
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "  1. Open Android Studio -> New Project -> point to $TARGET_DIR"
Write-Host "     Use package name: $PACKAGE_NAME"
Write-Host "  2. Add signing config to app/build.gradle.kts"
Write-Host "     (see Mirror-App as reference)"
if (-not $GITHUB_REPO) {
    Write-Host "  3. git remote add origin <github-repo-url>" -ForegroundColor Yellow
    Write-Host "  4. git push -u origin main"
    Write-Host "  5. git push -u origin dev"
} else {
    Write-Host "  3. git push -u origin main"
    Write-Host "  4. git push -u origin dev"
}
Write-Host "  -> Verify GitHub Actions runs on first push"
Write-Host ""
Write-Host "Keystore:  $KEYSTORE_REL"
Write-Host "Alias:     $KEY_ALIAS"
Write-Host "Password:  $KEY_PASSWORD"
Write-Host ""
