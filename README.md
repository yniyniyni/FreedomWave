# FreedomWave

**English** | [Русский](README.ru.md)

A native Android admin client for [Remnawave](https://remna.st) — a self-hosted VPN/proxy management panel. Built with Kotlin Multiplatform and Compose Multiplatform, with the entire UI and business logic in shared code (an iOS target compiles as a framework stub).

## Features

- **Dashboard** — live system stats, bandwidth, online users, and node-derived counts
- **Users** — full CRUD, search, filters and sorting, traffic limits and expiry, squad membership, subscription link with QR code, HWID devices
- **Nodes** — status and uptime monitoring, enable/disable/restart, traffic stats
- **Hosts** — host management with VLESS/security-layer configuration
- **Squads** — internal squad management, bulk add/remove users
- **Bandwidth** — per-node bandwidth charts
- **Security** — biometric app lock
- **Theming** — Material 3, dynamic color on Android

## Getting started

1. In your Remnawave panel, create an API token (**API Tokens** section).
2. Install the app and enter:
   - your panel URL (e.g. `https://panel.example.com`),
   - the API token.
3. The app verifies the token and connects. The token is stored locally and is effectively permanent; on a 401 response the app returns to the login screen.

## Building

```bash
# Debug APK
./gradlew :app:assembleDebug

# Release APK
./gradlew :app:assembleRelease

# Unit tests
./gradlew :composeApp:testDebugUnitTest

# iOS framework (requires full Xcode)
./gradlew :composeApp:linkDebugFrameworkIosArm64
```

Requirements: JDK 17+, Android SDK 36. Minimum Android version: 12 (API 31).

## Architecture

Two-module layout (AGP 9+ forbids `com.android.application` + `kotlin.multiplatform` in one module):

| Module | Purpose |
|---|---|
| `:app` | Thin Android launcher — `MainActivity`, `Application`, manifest, resources |
| `:composeApp` | All shared code: Compose UI, domain, data, DI. Produces an Android AAR and an iOS framework |

Inside `:composeApp/commonMain`: MVVM with `StateFlow`, Ktor client with Bearer auth for the Remnawave REST API, repositories mapping DTOs to domain models, Koin for DI, DataStore for preferences.

### Stack

| Role | Library |
|---|---|
| Language / UI | Kotlin Multiplatform, Compose Multiplatform (Material 3) |
| Networking | Ktor Client + kotlinx.serialization |
| DI | Koin |
| Storage | AndroidX DataStore (multiplatform) |
| ViewModel | JetBrains lifecycle-viewmodel-compose |
| Images / QR | Coil 3, qrose |
| Logging | Kermit |

## License

Licensed under the [European Union Public Licence v. 1.2](LICENSE) (EUPL-1.2).
