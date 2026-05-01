plugins {
 kotlin("jvm") version "2.1.10"
 id("org.jetbrains.compose") version "1.8.0"
 kotlin("plugin.compose") version "2.1.10"
 kotlin("plugin.serialization") version "2.1.10"
 application
}

group = "com.discordrpc"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
 implementation(compose.desktop.currentOs)
 implementation(compose.material3)
 implementation("com.github.caoimhebyrne:KDiscordIPC:0.2.5")
 implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
 testImplementation(kotlin("test"))
}

application {
    mainClass.set("com.discordrpc.gui.MainKt")
}

kotlin {
    jvmToolchain(21)
}
