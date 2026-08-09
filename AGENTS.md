# Party Games â€” AI Agent Context

This file provides context for AI coding agents (e.g. Antigravity / Gemini) working in this repository.

---

## Project Identity

| Property | Value |
| :--- | :--- |
| **App Name** | Party Games |
| **Package Name** | com.leminno.partygames |
| **Organisation** | Leminno |
| **Platform** | Native Android (Kotlin) |
| **Min SDK** | 26 (Android 8.0) |
| **Build System** | Gradle Kotlin DSL |

---

## Repository Structure

```
.
â”œâ”€â”€ app/
â”‚   â”œâ”€â”€ src/main/             # Source code & resources
â”‚   â”œâ”€â”€ build.gradle.kts      # App-level build (signing, versioning, deps)
â”‚   â””â”€â”€ release.jks           # Release signing keystore (committed â€” private repo)
â”œâ”€â”€ .github/
â”‚   â””â”€â”€ workflows/
â”‚       â””â”€â”€ android.yml       # CI/CD pipeline (GitHub Actions)
â”œâ”€â”€ gradle/                   # Gradle wrapper files
â”œâ”€â”€ build.gradle.kts          # Root Gradle config
â”œâ”€â”€ settings.gradle.kts       # Module settings
â”œâ”€â”€ KEYSTORE.md               # Keystore credentials & Play Store checklist
â”œâ”€â”€ techstack.md              # Tech stack & architecture decisions
â”œâ”€â”€ AGENTS.md                 # This file â€” AI agent context
â”œâ”€â”€ CHANGELOG.md              # Version history
â””â”€â”€ README.md                 # Public documentation
```

---

## CI/CD Summary

| Branch | Debug APK | Release APK | Release AAB | Versioning |
| :--- | :--- | :--- | :--- | :--- |
| `dev` | Yes | No | No | Local fallback (versionCode=1) |
| `main` | Yes | Yes | Yes | Auto: VERSION_CODE=github.run_number |

---

## Signing Configuration

- Keystore: `app/release.jks` (committed â€” private repo only)
- Alias: `partygames-key`
- Env vars: `PARTYGAMES_KEYSTORE_PATH`, `PARTYGAMES_KEYSTORE_PASSWORD`, `PARTYGAMES_KEY_ALIAS`, `PARTYGAMES_KEY_PASSWORD`
- Full details: see `KEYSTORE.md`

---

## Coding Conventions

- Kotlin only â€” no Java source in `app/src/main`
- Gradle Kotlin DSL only â€” no Groovy
- Keep `AndroidManifest.xml` permissions minimal and well-commented
- `versionCode` and `versionName` driven by CI env vars â€” do not hard-code

---

## Notes for AI Agents

- `release.jks` is intentionally committed because this is a **private repository**. Do not suggest moving it to GitHub Secrets only.
- Do not hard-code `versionCode` or `versionName` in `build.gradle.kts` â€” they are controlled by CI environment variables.
- Always preserve existing comments in `AndroidManifest.xml` and `build.gradle.kts`.

---

## Leminno Social Links

These links should appear in the app's settings, about screen, or footer where appropriate.

| Platform | URL |
| :--- | :--- |
| **LinkedIn** | https://www.linkedin.com/company/leminno/ |
| **Discord** | https://discord.gg/uTmQnkMVkA |

When adding to a settings screen, use `Intent(Intent.ACTION_VIEW, Uri.parse(url))` to open in the default browser.

---

## AI Provider Gateway Integration

Party Games is connected to the Leminno Protected AI Gateway hosted at `https://ai.leminno.com/`. AI coding agents and app features can easily leverage AI capabilities using the pre-configured `AiGateway` utility (`com.leminno.partygames.data.remote.AiGateway`).

| Property | Value |
| :--- | :--- |
| **Gateway URL** | `https://ai.leminno.com/api/chat` |
| **Default Secret Key** | `leminno_apps_Key` (passed in `x-api-key` header) |
| **App Identity** | `Party Games` (passed in `x-app-id` header) |
| **Utility Location** | `app/src/main/java/com/leminno/partygames/data/remote/AiGateway.kt` |

### Usage Examples

#### Callback Style
``kotlin
AiGateway.askAi("Your prompt message here") { response, error ->
    if (response != null) {
        val text = response.text
    }
}
``

#### Suspend Function (ViewModel / Coroutines)
``kotlin
viewModelScope.launch {
    AiGateway.askAiSuspend("Your prompt message here")
        .onSuccess { response ->
            val text = response.text
        }
}
``
