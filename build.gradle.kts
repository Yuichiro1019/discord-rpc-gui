plugins {
    kotlin("multiplatform") version "2.3.20"
    id("org.jetbrains.compose") version "1.10.3"
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.20"
}

group = "com.discordrpc.gui"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(25)

    jvm("desktop")

    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                @Suppress("DEPRECATION")
                implementation(compose.material3)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.0")
                implementation("com.github.caoimhebyrne:KDiscordIPC:0.2.6")
                implementation("org.slf4j:slf4j-simple:2.0.12")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.discordrpc.gui.MainKt"

        jvmArgs += listOf(
            "-Dawt.toolkit.name=GTK",
            "-Djava.awt.headless=false",
            "-Dskiko.renderApi=SOFTWARE"
        )

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
            )
            packageName = "DiscordRPCGUI"
            packageVersion = "1.0.0"
        }
    }
}
