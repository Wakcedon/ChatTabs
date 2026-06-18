# ChatTabs Reloaded

A modern Minecraft mod that adds tabbed chat organization. Filter messages into separate tabs per server, with per-tab send modifiers, color rules, and drag-and-drop management.

## Features

- **Chat tabs** — messages are filtered into separate tabs based on regex rules
- **Per-server profiles** — different tab sets for different servers, configured in `config/chattabs-profiles.json`
- **Send modifiers** — auto-prepend/suffix when chatting from a specific tab (e.g. `!` for global chat)
- **Drag & drop** — reorder tabs by dragging
- **Unread counter** — shows number of unread messages per tab
- **Fade animation** — tabs fade when chat closes
- **Right-click context menu** — create, edit, delete, reorder tabs
- **Tab editor** — in-game screen to configure name, filter regex, hex color, send modifier
- **In-game commands** — `/chattabs` (alias `/ct`)
- **NeoForge config screen** — accessible from Mods menu

## Commands

| Command | Description |
|---|---|
| `/chattabs help` | Show command list |
| `/chattabs reload` | Reload config and profiles from disk |
| `/chattabs save` | Save current config to disk |
| `/chattabs list` | List all configured tabs |
| `/chattabs select <name>` | Switch to a tab by name |
| `/chattabs tab add <name>` | Create a new tab |
| `/chattabs tab remove <name>` | Delete a tab |
| `/chattabs toggle <feature>` | Toggle unreadcounter / dragdrop / animation |
| `/chattabs filter <tab> <regex>` | Set a tab's filter regex |
| `/chattabs profile list` | List all server profiles |
| `/chattabs profile current` | Show active profile for current server |

`/ct` is an alias for all `/chattabs` commands.

## Configuration

### `config/chattabs.json`
Main mod settings: enabled state, tab colors, chat dimensions, animation toggle.

### `config/chattabs-profiles.json`
Per-server tab profiles. Structure:

```json
{
  "profiles": [
    {
      "serverIp": "mc.hypixel.net",
      "name": "Hypixel",
      "tabs": [
        {
          "id": "all",
          "name": "All",
          "visibleByDefault": true,
          "filter": { "regex": ".*", "hexColor": -1 },
          "sendModifier": { "prefix": "", "suffix": "" }
        }
      ]
    }
  ],
  "defaultProfile": {
    "name": "Default",
    "tabs": [ ... ]
  }
}
```

- `profiles[]` — list of server-specific tab sets (matched by IP suffix)
- `defaultProfile` — fallback tabs when no server matches
- Each tab supports: `id`, `name`, `visibleByDefault`, `filter` (regex + hexColor), `sendModifier` (prefix + suffix)

Share `chattabs-profiles.json` with friends — drop it in your `config` folder and it just works.

## Languages

- English (default), Russian, Spanish, Chinese (Simplified)

## Building

Requirements: JDK 21, Gradle wrapper

```bash
./gradlew :neoforge:build
```

Output: `neoforge/build/libs/`
