package com.linxdroid.app.model

data class Distribution(
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val iconEmoji: String,
    val downloadUrls: Map<String, String>,
    val defaultShell: String = "/bin/sh",
    val estimatedSizeMb: Int
) {
    fun urlForArch(arch: String): String? = downloadUrls[arch]
}

object Distributions {

    private const val ALPINE_VERSION = "3.19.1"
    private const val UBUNTU_VERSION = "22.04"
    private const val DEBIAN_VERSION = "12"
    private const val KALI_VERSION = "current"
    private const val ARCH_VERSION = "current"

    val all: List<Distribution> = listOf(

        Distribution(
            id = "alpine",
            name = "Alpine Linux",
            version = ALPINE_VERSION,
            description = "Minimal, security-oriented Linux. Great for lightweight containers.",
            iconEmoji = "🏔️",
            downloadUrls = mapOf(
                "aarch64" to "https://dl-cdn.alpinelinux.org/alpine/v3.19/releases/aarch64/alpine-minirootfs-$ALPINE_VERSION-aarch64.tar.gz",
                "x86_64"  to "https://dl-cdn.alpinelinux.org/alpine/v3.19/releases/x86_64/alpine-minirootfs-$ALPINE_VERSION-x86_64.tar.gz",
                "armhf"   to "https://dl-cdn.alpinelinux.org/alpine/v3.19/releases/armhf/alpine-minirootfs-$ALPINE_VERSION-armhf.tar.gz",
                "x86"     to "https://dl-cdn.alpinelinux.org/alpine/v3.19/releases/x86/alpine-minirootfs-$ALPINE_VERSION-x86.tar.gz"
            ),
            defaultShell = "/bin/sh",
            estimatedSizeMb = 8
        ),

        Distribution(
            id = "ubuntu",
            name = "Ubuntu",
            version = UBUNTU_VERSION,
            description = "Popular Debian-based Linux with a large software ecosystem.",
            iconEmoji = "🐧",
            downloadUrls = mapOf(
                "aarch64" to "https://cdimage.ubuntu.com/ubuntu-base/releases/22.04/release/ubuntu-base-22.04-base-arm64.tar.gz",
                "x86_64"  to "https://cdimage.ubuntu.com/ubuntu-base/releases/22.04/release/ubuntu-base-22.04-base-amd64.tar.gz",
                "armhf"   to "https://cdimage.ubuntu.com/ubuntu-base/releases/22.04/release/ubuntu-base-22.04-base-armhf.tar.gz"
            ),
            defaultShell = "/bin/bash",
            estimatedSizeMb = 75
        ),

        Distribution(
            id = "debian",
            name = "Debian",
            version = DEBIAN_VERSION,
            description = "Stable, universal operating system powering countless servers.",
            iconEmoji = "🌀",
            downloadUrls = mapOf(
                "aarch64" to "https://github.com/termux/proot-distro/releases/download/v4.7.0/rootfs-debian-aarch64.tar.xz",
                "x86_64"  to "https://github.com/termux/proot-distro/releases/download/v4.7.0/rootfs-debian-x86_64.tar.xz"
            ),
            defaultShell = "/bin/bash",
            estimatedSizeMb = 120
        ),

        Distribution(
            id = "kali",
            name = "Kali Linux",
            version = KALI_VERSION,
            description = "The industry-standard penetration testing and security auditing platform.",
            iconEmoji = "🐉",
            downloadUrls = mapOf(
                "aarch64" to "https://github.com/termux/proot-distro/releases/download/v4.7.0/rootfs-kali-aarch64.tar.xz",
                "x86_64"  to "https://github.com/termux/proot-distro/releases/download/v4.7.0/rootfs-kali-x86_64.tar.xz"
            ),
            defaultShell = "/bin/bash",
            estimatedSizeMb = 180
        ),

        Distribution(
            id = "arch",
            name = "Arch Linux",
            version = ARCH_VERSION,
            description = "Rolling release Linux focused on simplicity and modernity.",
            iconEmoji = "🦅",
            downloadUrls = mapOf(
                "aarch64" to "https://github.com/termux/proot-distro/releases/download/v4.7.0/rootfs-archlinux-aarch64.tar.xz",
                "x86_64"  to "https://github.com/termux/proot-distro/releases/download/v4.7.0/rootfs-archlinux-x86_64.tar.xz"
            ),
            defaultShell = "/bin/bash",
            estimatedSizeMb = 150
        )
    )

    fun findById(id: String): Distribution? = all.find { it.id == id }
}
