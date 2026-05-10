<p align="center">
  <img src="https://img.shields.io/badge/MC-1.21.4-00b813?style=flat-square" alt="MC Version"/>
  <img src="https://img.shields.io/badge/Paper-1.21.4-00b813?style=flat-square" alt="Paper API"/>
  <img src="https://img.shields.io/badge/Java-21-fcba03?style=flat-square" alt="Java Version"/>
  <img src="https://img.shields.io/badge/license-MIT-808080?style=flat-square" alt="License"/>
</p>

<p align="center">
  <b>Don't let your crops go to waste.</b><br>
  SeedProtect auto-replants fully grown crops, protects young seedlings,<br>
  and keeps your farmland from turning to dirt.
</p>

---

## ✦ Features

| | |
|---|---|
| **Auto-Replant** | Break a mature crop and it instantly replants — 1 seed is consumed, the rest drop as normal. |
| **Immature Protection** | Forgot you planted there? Normal breaking is cancelled. Sneak to intentionally break young crops. |
| **Trample Prevention** | Walking on farmland with a crop above it won't turn it to dirt anymore. |
| **XP Harvest Bonus** | Small chance (5 %) to pop out 1–3 XP orbs with a happy-particle burst. |

## ✦ Supported Crops

```
WHEET  •  CARROTS  •  POTATOES  •  BEETROOTS  •  NETHER_WART
MELON_STEM  •  PUMPKIN_STEM  •  TORCHFLOWER_CROP  •  PITCHER_CROP
```

## ✦ How It Works

```
    ┌──────────────────────────────────────────────────────┐
    │                     1.  Player breaks a crop          │
    │                        │                              │
    │          ┌─────────────┴─────────────┐                │
    │          ▼                           ▼                │
    │   Fully grown?                 Still growing?        │
    │          │                           │                │
    │          ▼                           ▼                │
    │   Auto-harvest               ❌ Cancelled             │
    │   Drop items - 1 seed        "Sneak to break         │
    │   Replant (age 0)            baby seeds."            │
    │   ✨ 5% XP + particles                               │
    └──────────────────────────────────────────────────────┘
```

## ✦ Commands

None. Plug-and-play — just drop the jar in your `plugins/` folder.

## ✦ Permissions

None. Every player on the server benefits from crop protection.

## ✦ Building

```bash
git clone https://github.com/yourname/SeedProtect.git
cd SeedProtect
chmod +x gradlew
./gradlew build
```

The compiled jar lands in `build/libs/SeedProtect-2.0.0.jar`.

---

<p align="center">
  <sub>Built with <a href="https://papermc.io">Paper API</a> • Requires Minecraft 1.21.4+</sub>
</p>
