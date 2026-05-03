# Assets

Place proot binaries here before building. These must be statically compiled PRoot binaries for each Android ABI:

| File              | Architecture      | Android ABI    |
|-------------------|-------------------|----------------|
| `proot-aarch64`   | ARM 64-bit        | arm64-v8a      |
| `proot-x86_64`    | x86 64-bit        | x86_64         |
| `proot-armhf`     | ARM 32-bit        | armeabi-v7a    |
| `proot-x86`       | x86 32-bit        | x86            |

## Where to get PRoot binaries

Pre-built static PRoot binaries can be obtained from:
- **Termux packages**: https://github.com/termux/termux-packages
- **PRoot-distro**: https://github.com/termux/proot-distro
- **Static PRoot releases**: https://github.com/proot-me/proot/releases

Download the appropriate `.tar.gz`, extract the `proot` binary, and rename it as shown above.

## Optional: Loader

Some PRoot versions require a loader binary (`proot-loader-<arch>`) alongside the main binary.
