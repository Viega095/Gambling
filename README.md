# 🎰 VieGambling - Enterprise Casino & 3D Gambling Plugin

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-brightgreen.svg)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://openjdk.org)
[![Build](https://img.shields.io/badge/Build-Maven-orange.svg)](https://maven.apache.org)

VieGambling is a next-generation Casino and Gambling plugin for Paper/Spigot 1.20.1+ featuring custom **3D Display Entities**, smooth 60 FPS in-world mechanics, synchronized multiplayer tables, VIP Tiering & Rakeback, Progressive Mega Jackpots, and full Vault economy integration.

---

## 🌟 Key Features

* **3D In-World Drop Machines**:
  * **Plinko 3D**: Real visual pin drops, multi-ball cascades, and realistic bounce sound pitching.
  * **Rocket Crash**: Exponential curve multiplayer crash simulator with live particle contrails and real-time cashout.
  * **Physical Blackjack Table**: 4-player physical felt table with smooth card dealing animations using ItemDisplays.
  * **Texas Hold'em Poker**: 6-player physical poker table with Blinds, Flop, Turn, River, Pot management, and Hand evaluator.
  * **Community Synchronized Roulette**: Global shared table with 30s countdown and automated payouts.
* **Economic Progression**:
  * **VIP Rakeback & Cashback**: 6 tiers (Bronze to Whale) with daily volume rakeback claims (`/gamble vip`).
  * **Mega Progressive Jackpot**: Global cross-game jackpot fund with server-wide celebration events.
  * **$CHIPS Crypto Exchange**: Volatile casino token with fluctuating market value.
* **Mini-events**:
  * **Casino Heist**: Infiltrate the high-security VIP vault, evade armed guards, and escape with the grand prize.

---

## 📜 Commands & Permissions

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/gamble` | Open main casino game menu | `gambling.play` |
| `/gamble vip` | Open VIP Rakeback GUI | `gambling.play` |
| `/gamble token <buy\|sell>` | Trade $CHIPS on the crypto exchange | `gambling.play` |
| `/gamble heist` | Initiate a Casino Vault Heist | `gambling.play` |
| `/gamble plinko3d` | Drop a physical 3D Plinko ball | `gambling.play` |
| `/gamble admin` | Open Casino Admin Dashboard | `gambling.admin` |

---

## 🔨 Building from Source

```bash
git clone <REPO_URL>
cd Gambling
mvn clean package
```
Output JAR will be in `target/GamblingPlugin-1.0-SNAPSHOT.jar`.
