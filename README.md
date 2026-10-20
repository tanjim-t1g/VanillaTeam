# VanillaTeams

A lightweight vanilla-friendly team plugin for Paper and Purpur servers. 

No `/team home`, no warps, and no virtual team chests. Just basic teams, team chat, alliances, and vote-based friendly fire.

---

<!-- Showcase / Screenshots (will add photos here later) -->
<!-- ![Team Info](path/to/screenshot1.png) -->
<!-- ![Team Chat & Buttons](path/to/screenshot2.png) -->

---

## Features

<details>
<summary><strong>Vanilla-friendly gameplay</strong></summary>

Keeps survival simple and fair. No team homes, teleport commands, warps, or virtual team storage.

</details>

<details>
<summary><strong>Friendly fire voting</strong></summary>

Friendly fire is enabled by default. Any team member can start a vote using `/team pvp on` or `/team pvp off`. Online members can vote using clickable **[Yes]** and **[No]** buttons.

</details>

<details>
<summary><strong>Friendly fire protection</strong></summary>

Team damage is handled at the lowest event priority, allowing VanillaTeams to cancel friendly fire before other plugins process the damage event.

</details>

<details>
<summary><strong>Private team and ally chat</strong></summary>

- `/tc <message>` — Send a private team message.
- `/tc` — Toggle team chat mode.
- `/ac <message>` — Send a private ally message.
- `/ac` — Toggle ally chat mode.

</details>

<details>
<summary><strong>DiscordSRV support</strong></summary>

Team and ally messages stay private and are not sent to public DiscordSRV channels.

</details>

<details>
<summary><strong>Team alliances</strong></summary>

Each team can have one allied team. Send an alliance request using `/team ally <team>` and accept or deny it using clickable buttons.

</details>

<details>
<summary><strong>SQLite database</strong></summary>

Teams, leaders, members, and usernames are saved automatically in `database.db`, preserving team data across server restarts.

</details>

---

## Commands

| Command | Description | Permission |
|---|---|---|
| `/teams` | List all teams on the server | `vanillateams.use` |
| `/team create <name>` | Create a team | `vanillateams.use` |
| `/team invite <player>` | Invite someone to your team (leader only) | `vanillateams.use` |
| `/team accept [team]` | Accept an invite | `vanillateams.use` |
| `/team deny [team]` | Decline an invite | `vanillateams.use` |
| `/team leave` | Leave your current team | `vanillateams.use` |
| `/team kick <player>` | Kick a member (leader only) | `vanillateams.use` |
| `/team transfer <player>` | Transfer leadership (leader only) | `vanillateams.use` |
| `/team ally <team>` | Send an ally request to another team (leader only) | `vanillateams.use` |
| `/team ally accept` | Accept an ally request (leader only) | `vanillateams.use` |
| `/team ally deny` | Decline an ally request (leader only) | `vanillateams.use` |
| `/team ally break` | Break your current alliance (leader only) | `vanillateams.use` |
| `/team pvp [on\|off]` | Start a vote to toggle friendly fire | `vanillateams.use` |
| `/team pvp [yes\|no]` | Vote on an active PvP vote | `vanillateams.use` |
| `/team info [team]` | View team info and members | `vanillateams.use` |
| `/team chat [on\|off]` | Toggle or send team chat message | `vanillateams.use` |
| `/team disband` | Disband your team (leader only) | `vanillateams.use` |
| `/team disband <team>` | Force disband a team | `vanillateams.admin` |
| `/team reload` | Reload configuration | `vanillateams.admin` |
| `/tc [message]` | Send a message to team chat or toggle mode | `vanillateams.use` |
| `/ac [message]` | Send a message to ally chat or toggle mode | `vanillateams.use` |

---

## Requirements

- Paper / Purpur 1.21.4+ (or Paper 26.x)
- Java 21+ (Java 25 if running 26.1+)
