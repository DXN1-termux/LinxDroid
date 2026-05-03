<div align="center">

# 🐧 LinxDroid

**Run a complete Linux environment on Android — no root required.**

[![Build Status](https://github.com/DXN1-termux/LinxDroid/actions/workflows/build.yml/badge.svg)](https://github.com/DXN1-termux/LinxDroid/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/license-Apache%202.0-green.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-8.0%2B-brightgreen.svg?logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-blueviolet.svg?logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.02-blue.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)

LinxDroid uses **PRoot** to run real Linux distributions inside an Android app — no superuser access needed. Pick a distro, download it, and get a live terminal session in seconds.

</div>

---

## ✨ Features

| Feature | Details |
|---|---|
| 🚀 No Root Required | Powered by PRoot — runs entirely in userspace |
| 🖥️ Live Terminal | Full stdin/stdout terminal with keyboard input |
| 📦 Multiple Distros | Alpine Linux, Ubuntu 22.04, Debian 12 |
| 🔄 Download & Install | Streams rootfs directly from official mirrors |
| 📡 VNC Client | Built-in RFB 3.8 VNC client with touch input |
| ⚙️ Settings | VNC port, display number, custom PRoot args |
| 🔋 Background Session | WakeLock + foreground service keeps session alive |
| 🌙 Dark Theme | Material 3 dark design with hacker-green accents |
| 🏗️ Modern Architecture | MVVM · Hilt DI · StateFlow · Coroutines |

---

## 📱 Screens

| Welcome | Distro Selection | Terminal | Settings |
|---|---|---|---|
| Animated onboarding | Choose Alpine / Ubuntu / Debian | Live PRoot session | VNC, args, uninstall |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                        UI Layer                         │
│  WelcomeScreen  │  DistroSelection  │  Terminal  │  Settings │
│                    Jetpack Compose                       │
└────────────────────────┬────────────────────────────────┘
                         │ StateFlow / collectAsStateWithLifecycle
┌────────────────────────▼────────────────────────────────┐
│                    MainViewModel                        │
│  AppState sealed class  ·  Coroutines  ·  Hilt @HVM    │
└──────┬──────────────┬───────────────┬───────────────────┘
       │              │               │
┌──────▼──────┐ ┌─────▼──────┐ ┌────▼──────────┐
│PRootManager │ │RootFSMgr   │ │DownloadManager│
│  proot bin  │ │tar.gz / xz │ │ OkHttp stream │
│  arch detect│ │extraction  │ │ live progress │
└──────┬──────┘ └────────────┘ └───────────────┘
       │
┌──────▼──────────┐      ┌──────────────────┐
│TerminalSession  │      │   VNCClient      │
│ Process I/O     │      │ RFB 3.8 protocol │
│ SharedFlow      │      │ RAW + RRE decode │
└─────────────────┘      └──────────────────┘
```

### Key files

```
app/src/main/java/com/linxdroid/app/
├── MainActivity.kt              # Compose host, @AndroidEntryPoint
├── LinxDroidApplication.kt      # @HiltAndroidApp, Timber setup
├── PRootManager.kt              # Binary extraction, session lifecycle
├── VNCService.kt                # Foreground service, WakeLock
├── model/
│   ├── AppState.kt              # Sealed state machine
│   ├── Distribution.kt          # Distro model + download URLs
│   └── TerminalLine.kt          # Terminal output model
├── terminal/TerminalSession.kt  # PRoot process I/O via coroutines
├── vnc/
│   ├── VNCClient.kt             # Full RFB 3.8 protocol client
│   └── VNCView.kt               # SurfaceView + pinch/pan/touch
├── ui/
│   ├── theme/                   # Color, Type, Material3 dark theme
│   ├── main/MainViewModel.kt    # State orchestration
│   └── screens/                 # One Composable per screen
└── utils/
    ├── ArchDetector.kt          # ABI → proot arch string
    ├── DownloadManager.kt       # OkHttp streaming + progress
    ├── RootFSManager.kt         # Tar extraction, safe path check
    └── PreferencesManager.kt    # DataStore preferences
```

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer  
- JDK 17  
- Android SDK 34  

### Build

```bash
git clone https://github.com/DXN1-termux/LinxDroid.git
cd LinxDroid
chmod +x gradlew
./gradlew assembleDebug
```

The APK will be at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### PRoot Binaries ⚠️

Before the app can start a Linux session you must place **statically compiled PRoot binaries** in `app/src/main/assets/`:

| Filename | Android ABI | Architecture |
|---|---|---|
| `proot-aarch64` | arm64-v8a | ARM 64-bit |
| `proot-x86_64` | x86_64 | x86 64-bit |
| `proot-armhf` | armeabi-v7a | ARM 32-bit |
| `proot-x86` | x86 | x86 32-bit |

Pre-built static binaries are available from:
- [Termux packages](https://github.com/termux/termux-packages)
- [PRoot releases](https://github.com/proot-me/proot/releases)
- [proot-distro](https://github.com/termux/proot-distro)

---

## 🔐 Signed Releases

To produce a signed APK via CI, add these repository secrets:

| Secret | Description |
|---|---|
| `KEYSTORE_BASE64` | Base64-encoded `.jks` keystore |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |

Then push a version tag:
```bash
git tag v1.0.0
git push origin v1.0.0
```

The CI will build, sign, and publish a GitHub Release automatically.

---

## 📦 Supported Distributions

| Distro | Version | Size | Shell |
|---|---|---|---|
| 🏔️ Alpine Linux | 3.19.1 | ~8 MB | /bin/sh |
| 🐧 Ubuntu | 22.04 LTS | ~75 MB | /bin/bash |
| 🌀 Debian | 12 | ~120 MB | /bin/bash |

All rootfs images are downloaded directly from official mirrors at install time.

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 1.9.22 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Hilt + StateFlow |
| Async | Kotlin Coroutines |
| Networking | OkHttp 4 |
| Storage | DataStore Preferences |
| Compression | Apache Commons Compress (gzip + xz) |
| Linux runtime | PRoot (userspace chroot) |
| VNC | Custom RFB 3.8 Kotlin client |
| Logging | Timber |

---

## 🤝 Contributing

Pull requests are welcome! Please open an issue first to discuss what you'd like to change.

1. Fork the repository  
2. Create your feature branch (`git checkout -b feature/my-feature`)  
3. Commit your changes  
4. Push and open a pull request  

---

## 📄 License

```
Copyright 2024 LinxDroid Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
