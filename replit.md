# LinxDroid

## Project Overview
LinxDroid is a fully professional Android application that runs a complete Linux environment on Android devices using PRoot — no root access required. Users can pick a Linux distribution (Alpine, Ubuntu, Debian), download and install it, then interact with a live terminal session.

## Tech Stack
- **Language**: Kotlin 1.9.22 (JVM Target 17)
- **Platform**: Android (minSdk 26, targetSdk 34)
- **UI**: Jetpack Compose (Material 3, compose-bom 2024.02.00)
- **Architecture**: MVVM + Hilt DI + StateFlow
- **Build System**: Gradle 8.4 with Kotlin DSL
- **Key Libraries**:
  - Hilt (Dagger): Dependency injection
  - OkHttp 4.12: Distribution download
  - Apache Commons Compress 1.26: Tar/GZip/XZ extraction
  - DataStore Preferences: Persistent settings
  - Coroutines: Async download, extraction, terminal I/O
  - Timber: Logging
  - PRoot: Run Linux without root (binaries in `app/src/main/assets/`)

## Project Structure
```
LinxDroid/
├── build.gradle.kts              # Root gradle (plugin versions)
├── settings.gradle.kts           # Module includes
├── gradle.properties             # JVM args, AndroidX flags
├── gradlew                       # Gradle wrapper script
├── gradle/wrapper/               # Gradle wrapper config
├── .github/workflows/build.yml   # GitHub Actions CI
└── app/
    ├── build.gradle.kts          # App module: deps, plugins, compile options
    ├── proguard-rules.pro        # Release shrinking rules
    └── src/main/
        ├── AndroidManifest.xml   # Permissions, activity, service
        ├── assets/               # PRoot binaries go here (see assets/README.md)
        ├── res/
        │   ├── drawable/         # Adaptive launcher icon vectors
        │   ├── mipmap-anydpi-v26/# Adaptive icon XML
        │   └── values/           # strings, colors, themes
        └── java/com/linxdroid/app/
            ├── LinxDroidApplication.kt  # @HiltAndroidApp, Timber setup
            ├── MainActivity.kt          # Compose entry point, @AndroidEntryPoint
            ├── PRootManager.kt          # Extracts proot binary, starts/stops sessions
            ├── VNCService.kt            # Foreground service, WakeLock, notification
            ├── di/AppModule.kt          # Hilt @Singleton providers
            ├── model/
            │   ├── AppState.kt          # Sealed state machine
            │   ├── Distribution.kt      # Distro model + download URLs
            │   └── TerminalLine.kt      # Terminal output model
            ├── terminal/
            │   └── TerminalSession.kt   # PRoot process I/O wrapper (coroutines)
            ├── ui/
            │   ├── theme/               # Color, Type, Theme (dark green hacker aesthetic)
            │   ├── components/
            │   │   └── TerminalView.kt  # Compose terminal with LazyColumn + input
            │   ├── main/
            │   │   ├── MainViewModel.kt # State machine, install/session orchestration
            │   │   └── MainScreen.kt    # AnimatedContent router
            │   └── screens/
            │       ├── WelcomeScreen.kt       # First-launch onboarding
            │       ├── DistroSelectionScreen.kt # Pick & install distro
            │       ├── ProgressScreen.kt       # Download + extraction progress
            │       ├── ReadyScreen.kt          # Installed distro dashboard
            │       ├── TerminalScreen.kt       # Live terminal session
            │       ├── SettingsScreen.kt       # VNC, custom args, uninstall
            │       └── ErrorScreen.kt          # Error + retry
            ├── utils/
            │   ├── ArchDetector.kt      # Maps Android ABI -> PRoot arch string
            │   ├── DownloadManager.kt   # OkHttp streaming download w/ progress
            │   ├── PreferencesManager.kt# DataStore: installed distro, VNC port, etc.
            │   └── RootFSManager.kt     # Tar extraction, install, uninstall, size
            └── vnc/
                ├── VNCClient.kt         # Full RFB 3.8 client: handshake, RAW/RRE decode
                └── VNCView.kt           # SurfaceView with pinch-zoom + touch input
```

## App Flow
1. **Welcome** → first launch splash with feature overview
2. **Distribution Selection** → choose Alpine / Ubuntu / Debian
3. **Downloading** → streaming download with byte progress (OkHttp)
4. **Extracting** → tar.gz/xz extraction with progress (Commons Compress)
5. **Ready** → dashboard showing distro name, size, shell
6. **Terminal Session** → live PRoot session, stdin/stdout via coroutines
7. **Settings** → VNC port, custom proot args, uninstall

## Building
```bash
chmod +x gradlew
./gradlew assembleDebug
```
APK output: `app/build/outputs/apk/debug/app-debug.apk`

### PRoot Binaries Required
Before building a fully functional app, place statically-compiled PRoot binaries in `app/src/main/assets/`:
- `proot-aarch64` → arm64-v8a devices
- `proot-x86_64`  → x86_64 devices
- `proot-armhf`   → armeabi-v7a devices
- `proot-x86`     → x86 devices

Get from: https://github.com/termux/proot-distro or https://github.com/proot-me/proot/releases

## Permissions Used
- `INTERNET` — download rootfs
- `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_DATA_SYNC` — keep session alive
- `WAKE_LOCK` — prevent CPU sleep during active session
- `POST_NOTIFICATIONS` — show "session running" notification (Android 13+)

## Architecture Notes
- State is a sealed `AppState` class managed by `MainViewModel` using `StateFlow`
- VNCClient implements the full RFB 3.8 protocol (handshake, RAW, RRE encodings, pointer/key events)
- `TerminalSession` wraps the PRoot `Process` and streams stdout/stderr as `SharedFlow<TerminalLine>`
- All long-running work runs on `Dispatchers.IO` with `viewModelScope`
- `PreferencesManager` uses DataStore for persistent settings (installed distro ID, VNC port, custom args)
