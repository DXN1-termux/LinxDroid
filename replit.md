# LinxDroid

## Project Overview
LinxDroid is an Android application that runs a full Linux environment on Android devices using PRoot. It manages the extraction of a Linux Root File System (RootFS) and executes it within the Android environment, providing access via a VNC interface.

## Tech Stack
- **Language**: Kotlin (JVM Target 17)
- **Platform**: Android (minSdk 26, targetSdk 34)
- **UI**: Jetpack Compose
- **Dependency Injection**: Hilt (Dagger)
- **Build System**: Gradle with Kotlin DSL (`build.gradle.kts`)
- **Key Libraries**:
  - PRoot: Run Linux environments without root privileges
  - Apache Commons Compress: Handle `.tar.gz` rootfs archives
  - Timber: Logging
  - Navigation Compose, Lifecycle Runtime KTX

## Project Layout
```
app/
  build.gradle.kts           # Module-level build config
  src/main/
    AndroidManifest.xml
    java/com/linxdroid/app/
      di/                    # Dependency Injection (AppModule.kt)
      ui/                    # UI components (MainScreen.kt, MainViewModel.kt)
      utils/                 # RootFSManager.kt - tar extraction
      vnc/                   # VNCView.kt - VNC interface stub
      LinxDroidApplication.kt
      MainActivity.kt        # Entry point, triggers PRootManager
      PRootManager.kt        # Executes proot binary with bind mounts
      VNCService.kt          # Foreground service for Linux session
```

## Key Architecture Notes
- **PRootManager**: Extracts the `proot` binary from assets and executes it with `/dev`, `/proc`, `/sys` bind mounts
- **RootFSManager**: Installs a Linux distro by extracting a gzipped tar archive to app internal storage
- **VNCService**: Keeps the Linux environment alive as a foreground Android service

## Environment Notes
- This is a pure Android application — it cannot be run or previewed in the Replit browser environment
- Building requires Android SDK tools (not available in the Replit container by default)
- To build: use Android Studio or `./gradlew assembleDebug` with a properly configured Android SDK
