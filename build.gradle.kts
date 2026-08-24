plugins {
    id("net.fabricmc.fabric-loom") version "1.8.10"
    `maven-publish`
    kotlin("jvm") version "2.2.0"
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
    minecraft("com.mojang:minecraft:26.1.2")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.16.7")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.100.5+26.1.2")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.12.5+kotlin.2.2.0")
    
    // SkyOcean dependency
    modImplementation("maven.modrinth:skyocean:1.17.2+26.1.2")
}

loom {
    mixin {
        defaultRefmapName.set("skyocean_item_tracker.refmap.json")
    }
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filteringCharacterEncoding = "UTF-8"
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
