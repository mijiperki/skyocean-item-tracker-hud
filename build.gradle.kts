plugins {
    id("net.fabricmc.fabric-loom") version "1.17.19"
    `maven-publish`
    kotlin("jvm") version "2.4.10"
}

version = "1.0.0"
group = "com.example"

repositories {
    mavenCentral()
    maven { url = uri("https://maven.fabricmc.net/") }
    maven { url = uri("https://maven.teamresourceful.com/repository/maven-public/") }
    maven { url = uri("https://maven.parchmentmc.org/") }
    maven { url = uri("https://api.modrinth.com/maven") }
}

dependencies {
    // Minecraft 26.x ships non-obfuscated, so Loom does not create the
    // remapping configurations (mappings/modImplementation). We depend on the
    // game directly and use plain implementation for mods.
    "minecraft"("com.mojang:minecraft:26.1.2")
    implementation("net.fabricmc:fabric-loader:0.19.3")
    implementation("net.fabricmc.fabric-api:fabric-api:0.155.2+26.1.2")
    implementation("net.fabricmc:fabric-language-kotlin:1.13.13+kotlin.2.4.10")

    // SkyOcean dependency — pinned to the exact Modrinth version id of the
    // 1.17.2 build for MC 26.1.2 (the plain "1.17.2" number is ambiguous, it
    // also has a 26.2 build).
    implementation("maven.modrinth:skyocean:wwN6ghcO")
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filteringCharset = "UTF-8"
        filesMatching("fabric.mod.json") {
            expand(
                "version" to project.version,
                "group" to project.group
            )
        }
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(25)
    }
}

kotlin {
    jvmToolchain(25)
}

loom {
    // AbstractContainerScreen#leftPos / #topPos are protected; widen them so
    // our helper objects (which are not subclasses) can read slot positions.
    accessWidenerPath.set(file("src/main/resources/skyocean_item_tracker.accesswidener"))
}
