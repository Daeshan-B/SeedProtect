# SeedProtect

<p align="center">
  <b>Don't let your crops go to waste.</b><br>
  SeedProtect auto-replants fully grown crops, protects young seedlings,<br>
  and keeps your farmland from turning to dirt.
</p>

---

## ✦ Features

| Feature | Description |
|---|---|
| **Auto-Replant** | Break a mature crop and it instantly replants — 1 seed is consumed, the rest drop as normal. |
| **Immature Protection** | Forgot you planted there? Normal breaking is cancelled. Sneak to intentionally break young crops. |
| **Trample Prevention** | Walking on farmland with a crop above it won't turn it to dirt anymore. |
| **Water/Lava Protection** | Crops are protected from being destroyed by flowing water or lava. |
| **XP Harvest Bonus** | Small chance (5 %) to pop out 1–3 XP orbs with a happy-particle burst. |
| **Growth Particles** | Watch crops grow with subtle happy-villager particles. |

---

## ✦ Supported Crops

```
WHEAT • CARROTS • POTATOES • BEETROOTS • NETHER_WART
MELON_STEM • PUMPKIN_STEM • TORCHFLOWER_CROP • PITCHER_CROP
```

---

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

### Right-Click Harvest

- **Fully-grown crops**: Right-click to harvest and replant automatically (no sneaking required).
- **Immature crops**: Right-click passes through so bonemeal works normally.

### Physical Interactions

- Walking on farmland with a crop above it is cancelled to prevent trampling.
- Water and lava flows into crops are blocked.

---

## ✦ Commands

```
/toggle [enable|disable]
```

Toggle SeedProtect on or off.

---

## ✦ Permissions

None. Every player on the server benefits from crop protection.

---

## ✦ Building

```bash
git clone https://github.com/Daeshan-B/SeedProtect.git
cd SeedProtect
chmod +x gradlew
./gradlew build
```

The compiled jar lands in `build/libs/SeedProtect-2.0.0.jar`.

---

## ✦ Installation

1. Build the project using Gradle.
2. Copy `SeedProtect-2.0.0.jar` to your server's `plugins/` folder.
3. Restart your server or use `/reload`.

---

<p align="center">
  <sub>Built with Java 21 • Requires Minecraft 1.21+</sub>
</p>