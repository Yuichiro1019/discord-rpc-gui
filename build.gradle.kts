plugins {
    kotlin("multiplatform") version "2.3.20"
    kotlin("plugin.serialization") version "2.3.20"
    id("org.jetbrains.compose") version "1.10.3"
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.20"
}

group = "com.discordrpc.gui"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(21)

    jvm("desktop")

    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                @Suppress("DEPRECATION")
                implementation(compose.material3)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
                implementation("org.slf4j:slf4j-simple:2.0.12")

                // Ktor WebSocket client for Discord Gateway connection
                val ktorVersion = "3.1.3"
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
                implementation("io.ktor:ktor-client-websockets:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.discordrpc.gui.MainKt"

        buildTypes.release.proguard {
            isEnabled.set(false)
        }

        jvmArgs += listOf(
            "-Dawt.toolkit.name=GTK",
            "-Djava.awt.headless=false",
            "-Dskiko.renderApi=SOFTWARE"
        )

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Rpm,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi
            )
            packageName = "DiscordRPCGUI"
            packageVersion = "1.0.0"

            windows {
                iconFile.set(project.file("src/desktopMain/resources/logo.ico"))
            }
            linux {
                iconFile.set(project.file("src/desktopMain/resources/logo.png"))
            }
            macOS {
                iconFile.set(project.file("src/desktopMain/resources/logo.png"))
            }
        }
    }
}
