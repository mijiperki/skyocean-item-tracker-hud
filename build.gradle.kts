plugins {
    // Version + Loom-variant selection is configured in settings.gradle.kts and
    // gradle.properties (loomx.*). This applies the correct fabric-loom under the hood.
    id("dev.kikugie.loom-back-compat")
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
    // Minecraft 26.1+ is shipped DEOBFUSCATED by Mojang, so there is no
    // obfuscation/intermediary layer and NO `mappings(...)` declaration: Loom
    // uses the real names directly. (See gradle.properties for why.)
    minecraft("com.mojang:minecraft:$minecraftVersion")
    // No-op on deobfuscated 26.1+ (there are no Mojang mappings to apply);
    // applies them automatically on older obfuscated versions.
    loomx.applyMojangMappings()

    // `mod*` configs work on 26.1+ too — loom-back-compat aliases them to the
    // plain configs since the deobfuscated game needs no dependency remapping.
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
    modRuntimeOnly("net.fabricmc:fabric-language-kotlin:$fabricLanguageKotlinVersion")

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
    // Minecraft 26.1.2 targets Java 25.
    jvmToolchain(25)
}

tasks.processResources {
    inputs.property("version", modVersion)
    filesMatching("fabric.mod.json") {
        expand("version" to modVersion)
    }
}
