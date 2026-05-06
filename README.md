<div align="center">

<img src="src/desktopMain/resources/logo.png" alt="Discord RPC GUI Logo" width="96" height="96" />

# Discord RPC GUI

**A polished, cross-platform desktop app for setting Discord Rich Presence — no coding required.**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Compose Desktop](https://img.shields.io/badge/Compose_Desktop-1.10.3-4285F4?logo=jetpackcompose&logoColor=white)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![License](https://img.shields.io/github/license/Yuichiro1019/discord-rpc-gui)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Linux%20%7C%20Windows-blue)](#)

</div>

---

## ✨ Overview

Discord RPC GUI lets you craft a fully customised **Rich Presence** (the "Playing …" status shown on your Discord profile) for any running application — games, creative tools, media players, or anything else.

It works with the **official Discord client**, [**Vesktop**](https://github.com/Vencord/Vesktop), and even **without Discord installed at all** (Token/Gateway mode). A searchable live process list lets you pick any running app in seconds, and every configuration is saved per-app so it's ready the next time you launch.

---

## 🖼️ Features at a Glance

| Category | Details |
|---|---|
| 🔌 **Connection modes** | arRPC (Vesktop), Discord Gateway (token), Native IPC (official client) |
| 🖥️ **Process detection** | Auto-detects all GUI apps; picks up app icons from `.desktop` entries (Linux) and window titles (Windows) |
| 🎨 **Theming** | 5 hand-crafted dark presets + fully custom hex colour with live preview |
| 💾 **Settings persistence** | Per-app JSON profiles saved automatically, loaded on next selection |
| 🃏 **Full RPC fields** | Name, type, details, state, large/small images, timestamps, party secrets, 2 buttons |
| 📦 **Installable packages** | `.deb`, `.rpm` (Linux) and `.exe`/`.msi` (Windows) via Gradle packaging |

---

## 🔌 Connection Modes

### ♙ arRPC — Local WebSocket
The **default** mode. No Discord token needed.  
Scans ports **6463–6472** for a running [arRPC](https://github.com/OpenAsar/arrpc) instance (built into Vesktop). If none is found, it automatically spawns its own via `npx -y arrpc`.

> **Best for:** Vesktop users or anyone already running arRPC.

### 🔑 Token — Discord Gateway
Connects directly to the **Discord WebSocket Gateway** using your user token. Works completely standalone — no Discord client or Vesktop required. This mode supports **dynamically changing the activity name** (title) on the fly.

> **Best for:** headless setups or users who want maximum control.  
> ⚠️ Self-botting/user tokens are against Discord ToS. Use at your own risk.

### 🔌 Native IPC — Official Client
Uses a bundled Python sidecar (`pypresence_sidecar`) that talks to Discord's **native IPC socket** via [pypresence](https://github.com/qwertyquerty/pypresence). Requires the official Discord desktop client to be running.

> **Best for:** users of the standard Discord client who don't run Vesktop.

---

## 🎨 Themes

Five carefully hand-crafted dark themes, plus a **custom hex colour** option that auto-generates a full palette using HSL colour theory (complementary + analogous hues):

| Theme | Primary | Accent |
|---|---|---|
| **Plush Amber** | `#FFC174` warm amber | `#5BA8F5` cool blue |
| **Obsidian Violet** | `#A78BFA` soft violet | `#34D399` emerald |
| **Discord Blurple** | `#5865F2` Discord blue | `#F2A858` warm orange |
| **Neon Cyber** | `#00E5FF` cyan | `#FF6090` hot pink |
| **Crimson Forge** | `#DC2626` deep red | `#26DCC8` teal |
| **Custom** | Any `#RRGGBB` hex | Auto-generated |

Switch themes at any time from the **🎨 Theme** picker in the header — the choice is saved globally.

---

## 📋 Rich Presence Fields

| Field | Description |
|---|---|
| **Activity Name** | Title shown on your profile (e.g. "Playing Heroic") |
| **Activity Type** | Playing · Streaming · Listening · Watching · Competing |
| **Details** | First line of detail text |
| **State** | Second line of detail text |
| **Application ID** | Discord Dev Portal app ID for custom assets |
| **Large / Small Image** | Image key + hover text for artwork |
| **Timestamps** | Start and/or end epoch timestamps |
| **Party Secrets** | Match, join, and spectate secrets |
| **Buttons** | Up to 2 clickable buttons with labels and URLs |

---

## 🗂️ Settings Storage

Settings are saved as JSON on disk:

| OS | Location |
|---|---|
| **Linux** | `~/.config/discord-rpc-gui/app-settings.json` |
| **Windows** | `%APPDATA%\discord-rpc-gui\app-settings.json` |

Global preferences (active theme) are stored in `global-settings.json` alongside the app settings.

---

## 🚀 Getting Started

### Prerequisites

| Requirement | Notes |
|---|---|
| **JDK 21+** | Required to run and build |
| **Node.js + npx** | Only needed for arRPC mode without Vesktop |
| **Python 3 + pypresence** | Only needed if you rebuild the sidecar from source |

### Run from source

```bash
git clone https://github.com/Yuichiro1019/discord-rpc-gui.git
cd discord-rpc-gui
./gradlew run
```

Or use the convenience script (adjust `JAVA_HOME` in `run.sh` first):

```bash
./run.sh
```

### Build an installer

```bash
./gradlew packageReleaseDistributionForCurrentOS
```

Output is placed in `build/compose/binaries/main/`.  
Supported formats: **`.deb`**, **`.rpm`** (Linux) · **`.exe`**, **`.msi`** (Windows).

> See `build-production.sh` for an example with a full JDK path set explicitly.

---

## 🏗️ Architecture

```
src/desktopMain/kotlin/com/discordrpc/gui/
├── Main.kt                   # Entry point — window setup & theme selection
├── ui/
│   ├── MainScreen.kt         # Root layout (header, sidebar, config pane)
│   ├── Sidebar.kt            # Connection card + process list
│   ├── ConfigPane.kt         # Rich presence form (all RPC fields)
│   └── AppTheme.kt           # Compose theme provider
├── state/
│   ├── AppState.kt           # Immutable UI state data class
│   └── MainViewModel.kt      # Business logic + coroutine orchestration
├── rpc/
│   ├── DiscordRpcService.kt  # Interface + RpcMode enum
│   ├── LocalRpcServiceImpl.kt    # arRPC WebSocket backend
│   ├── GatewayRpcServiceImpl.kt  # Discord Gateway backend
│   ├── NativeIpcRpcServiceImpl.kt # Python sidecar backend
│   └── gateway/              # Gateway models & WebSocket logic
├── process/
│   ├── ProcessDetector.kt    # Cross-platform process scanner
│   └── ProcessInfo.kt        # Process data model
├── settings/
│   ├── AppRpcSettings.kt     # Per-app RPC profile model
│   ├── AppGlobalSettings.kt  # Global preferences model
│   └── SettingsRepository.kt # JSON persistence layer
└── theme/
    └── ThemeEngine.kt        # Palette presets + HSL generator
```

---

## 🛠️ Tech Stack

| Library | Purpose |
|---|---|
| [Kotlin Multiplatform 2.3.20](https://kotlinlang.org/) | Language & build target |
| [Compose Multiplatform 1.10.3](https://www.jetbrains.com/lp/compose-multiplatform/) | Declarative desktop UI |
| [Material 3](https://m3.material.io/) | UI component library |
| [Ktor 3.1.3](https://ktor.io/) | WebSocket client (arRPC & Gateway) |
| [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) | JSON parsing & settings storage |
| [kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines) | Async / coroutine runtime |
| [pypresence](https://github.com/qwertyquerty/pypresence) | Native IPC sidecar (Python) |

---

## 🤝 Contributing

Pull requests and issues are welcome! Please open an issue first if you plan a large change so we can discuss the approach.

---

## 📄 License

This project is open source. See [LICENSE](LICENSE) for details.
