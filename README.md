# VanillaTeams

A lightweight vanilla-friendly team plugin for Paper and Purpur servers. 

No `/team home`, no warps, and no virtual team chests. Just basic teams, team chat, alliances, and vote-based friendly fire.

---

<!-- Showcase / Screenshots (will add photos here later) -->
<!-- ![Team Info](path/to/screenshot1.png) -->
<!-- ![Team Chat & Buttons](path/to/screenshot2.png) -->

---

## Features

- **Vanilla Friendly**: Keeps the game vanilla. No teleporting, no free homes, and no virtual storage.
- **Team Friendly Fire Voting**: Friendly fire is on by default. Any team member can start a vote with `/team pvp on` or `/team pvp off`, and online members vote using clickable `[Yes]` and `[No]` buttons.
- **Low-Level Friendly Fire Cancellation**: Team friendly fire runs at the lowest event priority, ensuring teammates don't take damage from each other before other plugins even touch the damage event.
- **Private Team & Ally Chat**:
  - `/tc <message>` or `/tc` to toggle team chat.
  - `/ac <message>` or `/ac` to toggle ally chat.
- **DiscordSRV Support**: Team and ally chat are completely private and will never leak into public DiscordSRV channels.
- **One Ally Per Team**: Teams can form a 1-to-1 alliance via `/ally <team>` with clickable `[Accept]` and `[Deny]` buttons.
- **SQLite Database**: Automatically saves all teams, leaders, members, and usernames in `database.db`.

---

## Commands

| Command | Description | Permission |
|---|---|---|
| `/teams` | List all teams on the server | `vanillateams.use` |
| `/team create <name>` | Create a team | `vanillateams.use` |
| `/team invite <player>` | Invite someone to your team | `vanillateams.use` |
| `/team accept <team>` | Accept an invite | `vanillateams.use` |
| `/team deny <team>` | Decline an invite | `vanillateams.use` |
| `/team leave` | Leave your current team | `vanillateams.use` |
| `/team kick <player>` | Kick a member (leader only) | `vanillateams.use` |
| `/team leader <player>` | Transfer leadership (leader only) | `vanillateams.use` |
| `/team pvp [on\|off]` | Start a vote to toggle friendly fire | `vanillateams.use` |
| `/team pvp [yes\|no]` | Vote on an active PvP vote | `vanillateams.use` |
| `/team info [team]` | View team info and members | `vanillateams.use` |
| `/team disband` | Disband your team (leader only) | `vanillateams.use` |
| `/team disband <team>` | Force disband a team | `vanillateams.admin` |
| `/team reload` | Reload configuration | `vanillateams.admin` |
| `/tc [message]` | Send a message to team chat or toggle mode | `vanillateams.use` |
| `/ac [message]` | Send a message to ally chat or toggle mode | `vanillateams.use` |
| `/ally <team>` | Send an ally request to another team | `vanillateams.use` |
| `/ally accept` | Accept an ally request | `vanillateams.use` |
| `/ally deny` | Decline an ally request | `vanillateams.use` |
| `/ally break` | Break your current alliance | `vanillateams.use` |

---

## Requirements

- Paper / Purpur 1.21.4+ (or Paper 26.x)
- Java 21+ (Java 25 if running 26.1+)
