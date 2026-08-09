# Android Release Keystore & App Publishing Guide

This document contains full details for the Android Release App Signing KeyStore used for **Party Games**, instructions for Gradle and CI setup, and a complete pre-launch checklist required before publishing the app to the Google Play Store.

---

## 1. Release Keystore Specifications

| Property | Value |
| :--- | :--- |
| **Keystore File** | `app/release.jks` |
| **Relative Path (from app module)** | `release.jks` |
| **Key Alias** | `partygames-key` |
| **Keystore Password** | `PartyGamesRelease2026!` |
| **Key Password** | `PartyGamesRelease2026!` |
| **Key Algorithm** | RSA 2048-bit |
| **Signature Algorithm** | SHA384withRSA |
| **Validity Period** | 10,000 days (~27 years from August 09, 2026) |
| **Distinguished Name** | `CN=Party Games, OU=Development, O=Leminno, L=City, ST=State, C=US` |

### Certificate Fingerprints
- **SHA-1**: `22:2B:CD:4E:70:28:22:2B:56:DE:B5:A4:56:F2:1D:64:71:93:17:B7`
- **SHA-256**: `B7:DC:D2:0D:50:A2:9A:1A:F8:F9:73:96:37:2A:89:CC:39:1A:BF:0A:F6:E1:AB:AC:95:8E:91:A8:3D:DD:66:84`

> [!WARNING]
> Keep a secure offline backup of `app/release.jks`. If lost, publishing updates to Google Play under `com.leminno.partygames` will not be possible.

---

## 2. Build & CI Configuration

### Local Builds
Gradle auto-detects `app/release.jks`. Override via environment variables:
- `PARTYGAMES_KEYSTORE_PATH`
- `PARTYGAMES_KEYSTORE_PASSWORD`
- `PARTYGAMES_KEY_ALIAS`
- `PARTYGAMES_KEY_PASSWORD`

### CI (GitHub Actions)
| Branch | Debug APK | Release APK | Release AAB | Versioning |
| :--- | :--- | :--- | :--- | :--- |
| `dev` | Yes | No | No | Local fallback |
| `main` | Yes | Yes | Yes | Auto (github.run_number) |

---

## 3. Pre-Upload Checklist

### A. Google Play Console
- [ ] Active developer account (\ registration fee)
- [ ] Identity verification complete

### B. App Identity & Metadata
- **Package Name**: `com.leminno.partygames`
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

```bash
# View certificate details
keytool -list -v -keystore app/release.jks -alias partygames-key

# Export public certificate
keytool -exportcert -alias partygames-key -keystore app/release.jks -file party-games-release.crt
```
