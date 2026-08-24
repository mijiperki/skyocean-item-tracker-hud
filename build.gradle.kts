plugins {
    id("fabric-loom") version "1.15-SNAPSHOT"
    kotlin("jvm") version "2.4.0"
}

val modVersion: String by project
val mavenGroup: String by project
val archivesBaseName: String by project

version = modVersion
group = mavenGroup
base.archivesName.set(archivesBaseName)

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/") { name = "Fabric" }
}

val minecraftVersion: String by project
val fabricLoaderVersion: String by project
val fabricApiVersion: String by project
val fabricLanguageKotlinVersion: String by project

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    // A fresh mod: use official Mojang mappings so we don't depend on a Yarn
    // build being published for 26.1.2. (SkyOcean uses its own mappings setup.)
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
    modImplementation("net.fabricmc:fabric-language-kotlin:$fabricLanguageKotlinVersion")

    // --- Pure-JVM core deps (no Minecraft) ---
    // gson is already on the classpath transitively via Minecraft, but declare it
    // so the core module compiles independently of the game.
    implementation("com.google.code.gson:gson:2.12.1")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
}

loom {
    runs {
        named("client") {
            // Hypixel SkyBlock is client-side; there is no dev server run of interest.
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    // Minecraft 1.21.11 targets Java 21.
    jvmToolchain(21)
}

tasks.processResources {
    inputs.property("version", modVersion)
    filesMatching("fabric.mod.json") {
        expand("version" to modVersion)
    }
}
