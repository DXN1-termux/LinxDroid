# 👀 COMING SOON 👀
< :) >
# 🐧 LinxDroid

**Run a complete Linux environment on Android — no root required.**

LinxDroid is a modern, high-performance Android application that allows you to install and run various Linux distributions (Alpine, Ubuntu, Debian) directly on your device. It utilizes **PRoot** to provide a secure, sandboxed environment without needing root access.

---

## 🚀 Features

- **No Root Required**: Runs entirely in userspace using PRoot.
- **Multiple Distributions**: One-tap installation for Alpine, Ubuntu, and Debian.
- **Modern UI**: Built with Jetpack Compose and Material 3 with a "hacker-green" aesthetic.
- **Terminal Session**: Live interactive terminal with full stdout/stderr streaming.
- **Background Support**: Foreground service with WakeLock ensures your Linux session stays alive.
- **VNC Support**: Built-in VNC client for graphical desktop environments (XFCE4, etc.).

---

## 🛠️ Tech Stack

- **Language**: Kotlin 1.9.22 (JVM Target 17)
- **UI Framework**: Jetpack Compose
- **Dependency Injection**: Hilt (Dagger)
- **Build System**: Gradle 8.4
- **Networking/IO**: OkHttp, Apache Commons Compress, Coroutines, DataStore

---

## 📦 Building from Source

### 1. Prerequisites
- Android SDK (API 34)
- JDK 17 or 21
- [PRoot Binaries](https://github.com/proot-me/proot/releases): Place architecture-specific binaries in `app/src/main/assets/`:
  - `proot-aarch64`
  - `proot-x86_64`
  - `proot-armhf`
  - `proot-x86`

### 2. Build the APK
Clone the repository and run the Gradle wrapper:

```bash
chmod +x gradlew
./gradlew assembleDebug
```

The APK will be located at:  
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🤖 GitHub Actions CI

This repository includes a comprehensive `build.yml` workflow that:
1.  **Validates** the code on every push/PR.
2.  **Builds** both Debug and Unsigned Release APKs.
3.  **Produces Artifacts**: Download the latest build directly from the "Actions" tab.
4.  **Auto-Release**: Automatically creates a GitHub Release when a version tag (e.g., `v1.0.0`) is pushed.

---

## 📜 License

Licensed under the Apache License, Version 2.0. See `LICENSE` for details.

---

## ✨ Credits

Developed and maintained by [DXN1-termux](https://github.com/DXN1-termux).
Special thanks to the **PRoot** and **Termux** communities.
