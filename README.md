# Suspicious Chunk Scanner

A lightweight, client-side Fabric mod designed for intelligent base and farm detection on heavily anti-xray protected servers (especially DonutSMP).

Unlike simple block ESP or basic density scanners, this mod uses multi-signal scoring, permanent peak memory, spatial clustering, and smart filters to find real player-built structures while staying quiet on natural terrain.

---

## Features

- **Multi-signal scoring system**
  - Storage density (chests, barrels, shulkers, hoppers, furnaces, dispensers)
  - Deep underground storage bonus
  - Structural / player-built blocks (concrete, deepslate bricks, wool, glass, etc.)
  - Entity heuristics (armor stands, item frames, chest minecarts)
  - Controlled tunnel detection
  - Light anomalies (heavily limited so normal caves don’t trigger)

- **Permanent peak memory**
  - Highest score ever seen for a chunk is saved to disk
  - Survives relogs, restarts, and chunk unloads
  - Extremely useful with the classic relog method

- **Spatial clustering**
  - Nearby high-score chunks are automatically grouped
  - Clusters are highlighted differently and treated as higher priority

- **Smart rejection filters**
  - Trial chambers
  - Mineshafts
  - Dungeons
  - Pure ender chest setups
  - Low-activity natural caves

- **Clean feedback**
  - Purple/blue themed chat notifications
  - Optional sound for nearby finds
  - Dense but limited particle highlights (ground plane + pillars)
  - Maximum 12 chunks rendered at once for performance

- **Simple controls**
  - Toggle the entire mod on/off
  - List nearest peaks
  - Clear saved data
  - Works even when server commands are blocked

---

## Commands

| Command       | Description                    |
|---------------|--------------------------------|
| `/scf` or `.scf` | Toggle the mod on/off         |
| `/scf list`   | Show nearest saved peaks       |
| `/scf clear`  | Delete all saved peaks         |
| `/scf help`   | Show help                      |

The `.scf` versions work as chat messages and are never blocked by servers.

---

## How Detection Works

Every chunk the client receives is scored using multiple independent signals:

1. **Tile entities** (strongest signal) – chests, shulkers, hoppers, etc.
2. **Structural density** – large amounts of player-placed blocks
3. **Entities** – armor stands, item frames, chest minecarts
4. **Deep storage** – storage below Y level 0
5. **Tunnels** – only counted when other player activity is already present
6. **Light** – very weak bonus, heavily restricted

Chunks that pass the threshold are stored as permanent “peaks”.  
Nearby peaks are clustered together.  
The mod never forgets a high score, even if the server later obfuscates the blocks.

This combination is what makes it effective on servers with strong anti-xray.

---

## Why This Mod Is Different

- Most public finders only count chests/hoppers in currently loaded chunks and forget everything on unload.
- This mod keeps a permanent memory of the best score ever seen for every chunk.
- It uses multiple signal types + clustering instead of a single density check.
- It actively rejects common false positives (mineshafts, trial chambers, natural caves).
- It is fully client-side, lightweight, and designed to be used with the [OpSec Mod](https://github.com/aurickk/OpSec/releases/)

---

## Installation

1. Install Fabric Loader for Minecraft 1.21.11
2. Install Fabric API
3. Place the mod jar in your `mods` folder
4. (Recommended) Use with OpSec if the server fingerprints mods

---

## Config & Data

- Saved peaks are stored in: `.minecraft/config/suschunkfinder/flagged.json`
- You can delete this file or use `/scf clear` to reset.

---

## Notes

- This is a detection/assistance tool. It does not give xray vision or force the server to send hidden blocks.
- Effectiveness depends on how the server’s anti-xray is configured and whether you use techniques like the known relog method similar to DonutSMP.
- The mod is intentionally kept clean and minimal to reduce detection surface and keep performance as consistent as possible.

---

## Credits

Developed as a focused, high-signal base finder for modern anti-xray environments.
For questions, problems, or suggestions, please use the repository's issue tracker.
