pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    // Selects the right Fabric Loom variant for the Minecraft version. For 26.1+
    // (deobfuscated Minecraft) it uses plain fabric-loom with no remapping; on
    // older obfuscated versions it would use fabric-loom-remap. See gradle.properties
    // for the `loomx.*` flags that drive the selection.
    // https://codeberg.org/KikuGie/loom-back-compat
    id("dev.kikugie.loom-back-compat") version "0.4.2"
}

rootProject.name = "multicraft-helper"
