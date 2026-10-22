# VanillaTeams

A lightweight, vanilla-friendly team plugin for Paper and Purpur servers.

VanillaTeams is built for survival servers that want basic teamwork without gameplay bloat—no team homes, no teleport commands, and no virtual team vaults. Just simple teams, private channels, alliances, and a vote-driven friendly fire system.

## Supported Software

<table border="0" align="center" cellpadding="8">
  <tr align="center">
    <td><img src="https://cdn.modrinth.com/data/cached_images/d1bd722e40b096e10c20037ffc9785f90961cb00.png" alt="Paper" height="60" /></td>
    <td><img src="https://cdn.modrinth.com/data/cached_images/422664306c62bca7fecd6b0a656868c5f3357763.png" alt="Purpur" height="60" /></td>
    <td><img src="https://cdn.modrinth.com/data/cached_images/009932291d8200b524fb67a6516b2434c0e16b9f_0.webp" alt="Spigot" height="60" /></td>
  </tr>
  <tr align="center" valign="top">
    <td><sub><b>Paper</b></sub></td>
    <td><sub><b>Purpur</b></sub></td>
    <td><sub><b>Spigot</b></sub></td>
  </tr>
</table>

<br>

## Features

- **Vanilla-Friendly Gameplay:** Keeps survival fair and true to vanilla. No team homes, warps, or virtual chests.
- **Vote-Based Friendly Fire:** Friendly fire is on by default. Any member can start a vote via `/team pvp on` or `/team pvp off`, letting online teammates vote with clickable **[Yes]** / **[No]** chat buttons.
- **Combat Protection:** Team damage is intercepted at `LOWEST` event priority, cleanly canceling friendly fire before combat taggers or other plugins process damage.
- **Team & Ally Chat:**
  - `/tc [message]` - Send a team message or toggle team chat mode.
  - `/ac [message]` - Send an ally message or toggle ally chat mode.
- **DiscordSRV Integration:** Team and ally chat messages are kept private and suppressed from public DiscordSRV broadcast channels.
- **Alliances:** Form a pact with an allied team for cross-team coordination.
- **SQLite Persistence:** All team data, memberships, and roles persist across restarts in `database.db`.

---

## Commands & Showcase

| Command | Description | Preview *(Click to zoom)* |
|:---|:---|:---:|
| `/team create <name>` | Create a new team | [<img src="https://cdn.modrinth.com/data/cached_images/08ee1e479c739cb3fcabdf029193798e2ca69970.png" width="230" alt="Create 1"/>](https://cdn.modrinth.com/data/cached_images/08ee1e479c739cb3fcabdf029193798e2ca69970.png) [<img src="https://cdn.modrinth.com/data/cached_images/635d94726fe3e7ba2ef2049e397c88461ee69865.png" width="230" alt="Create 2"/>](https://cdn.modrinth.com/data/cached_images/635d94726fe3e7ba2ef2049e397c88461ee69865.png) |
| `/team invite <player>` | Send invite to an online player *(Leader only)* | [<img src="https://cdn.modrinth.com/data/cached_images/698e3b3e213b2c65a0b73c4f74d06a4457ffad04.png" width="230" alt="Invite 1"/>](https://cdn.modrinth.com/data/cached_images/698e3b3e213b2c65a0b73c4f74d06a4457ffad04.png) [<img src="https://cdn.modrinth.com/data/cached_images/7542981ce818c7d67499d1469e774ea0217ec23f.png" width="230" alt="Invite 2"/>](https://cdn.modrinth.com/data/cached_images/7542981ce818c7d67499d1469e774ea0217ec23f.png) |
| `/team accept [team]` | Accept pending team invitation | [<img src="https://cdn.modrinth.com/data/cached_images/a5ceb1e2a90ee9281a1795c64dcfd3886be51df2.png" width="230" alt="Accept 1"/>](https://cdn.modrinth.com/data/cached_images/a5ceb1e2a90ee9281a1795c64dcfd3886be51df2.png) [<img src="https://cdn.modrinth.com/data/cached_images/936a28db9405d6e27988ee591d5754f98246f45b.png" width="230" alt="Accept 2"/>](https://cdn.modrinth.com/data/cached_images/936a28db9405d6e27988ee591d5754f98246f45b.png) |
| `/team deny [team]` | Decline pending team invitation | [<img src="https://cdn.modrinth.com/data/cached_images/8c34fbc875e52c8b74681aa3e284a7e9377ae84a.png" width="230" alt="Deny 1"/>](https://cdn.modrinth.com/data/cached_images/8c34fbc875e52c8b74681aa3e284a7e9377ae84a.png) [<img src="https://cdn.modrinth.com/data/cached_images/96ba7a0503c03a05e7aceb16e03a0306374ff573.png" width="230" alt="Deny 2"/>](https://cdn.modrinth.com/data/cached_images/96ba7a0503c03a05e7aceb16e03a0306374ff573.png) |
| `/team leave` | Leave your current team | [<img src="https://cdn.modrinth.com/data/cached_images/75e5f69b9712c60771418edaa1abb39fef5f912a.png" width="230" alt="Leave 1"/>](https://cdn.modrinth.com/data/cached_images/75e5f69b9712c60771418edaa1abb39fef5f912a.png) [<img src="https://cdn.modrinth.com/data/cached_images/b0689fcffb0d9de84bbfe17638c4d485f08668dd.png" width="230" alt="Leave 2"/>](https://cdn.modrinth.com/data/cached_images/b0689fcffb0d9de84bbfe17638c4d485f08668dd.png) |
| `/team kick <player>` | Remove a member from your team *(Leader only)* | [<img src="https://cdn.modrinth.com/data/cached_images/5b2506b512875017b8920c3083e6c48cf72e2e6a.png" width="230" alt="Kick 1"/>](https://cdn.modrinth.com/data/cached_images/5b2506b512875017b8920c3083e6c48cf72e2e6a.png) [<img src="https://cdn.modrinth.com/data/cached_images/eb4b34a7d851391a6bf6bf1626f66c9a534bf01f.png" width="230" alt="Kick 2"/>](https://cdn.modrinth.com/data/cached_images/eb4b34a7d851391a6bf6bf1626f66c9a534bf01f.png) |
| `/team transfer <player>`<br><sub>*(or `/team leader`)*</sub> | Transfer team leadership *(Leader only)* | [<img src="https://cdn.modrinth.com/data/cached_images/150e77c2f04e2deba24018093919abc3b3da9c49.png" width="230" alt="Transfer 1"/>](https://cdn.modrinth.com/data/cached_images/150e77c2f04e2deba24018093919abc3b3da9c49.png) [<img src="https://cdn.modrinth.com/data/cached_images/1230fc251c55db75851006e6384c01c5021a8a8a.png" width="230" alt="Transfer 2"/>](https://cdn.modrinth.com/data/cached_images/1230fc251c55db75851006e6384c01c5021a8a8a.png) |
| `/team info [team]` | View team details and member list | [<img src="https://cdn.modrinth.com/data/cached_images/69c4ecc6992ca3aeb5a9f79bec66b074b2022dcb.png" width="280" alt="Team Info"/>](https://cdn.modrinth.com/data/cached_images/69c4ecc6992ca3aeb5a9f79bec66b074b2022dcb.png) |
| `/team disband` | Disband your team completely *(Leader only)* | [<img src="https://cdn.modrinth.com/data/cached_images/6b661613f360dc7332576aa3a6cf8751068bfe3f.png" width="230" alt="Disband 1"/>](https://cdn.modrinth.com/data/cached_images/6b661613f360dc7332576aa3a6cf8751068bfe3f.png) [<img src="https://cdn.modrinth.com/data/cached_images/54fdd04b98249d67009379de9ef331febab72587.png" width="230" alt="Disband 2"/>](https://cdn.modrinth.com/data/cached_images/54fdd04b98249d67009379de9ef331febab72587.png) |
| `/teams` | List all teams on the server | - |
| `/team pvp [on\|off]` | Start a vote to enable or disable friendly fire | [<img src="https://cdn.modrinth.com/data/cached_images/1b43a342133b6ef041d135a58dde922c9270e4e7.png" width="280" alt="PvP Vote"/>](https://cdn.modrinth.com/data/cached_images/1b43a342133b6ef041d135a58dde922c9270e4e7.png) |
| `/team pvp [yes\|no]` | Cast your vote on an active friendly fire proposal | - |
| `/tc [message]` | Send team message or toggle team chat mode | [<img src="https://cdn.modrinth.com/data/cached_images/b3a0613adae31513c95a25519b06cdc3ec0e7cd7.png" width="230" alt="TC Toggle"/>](https://cdn.modrinth.com/data/cached_images/b3a0613adae31513c95a25519b06cdc3ec0e7cd7.png) [<img src="https://cdn.modrinth.com/data/cached_images/299e4df43a7effb74c9cb8266ca4d97c6298af27.png" width="230" alt="TC Msg"/>](https://cdn.modrinth.com/data/cached_images/299e4df43a7effb74c9cb8266ca4d97c6298af27.png) |
| `/ac [message]` | Send ally message or toggle ally chat mode | [<img src="https://cdn.modrinth.com/data/cached_images/338e7c32ad00e83354bf6e8a54cb1c13e9ca3c0a.png" width="280" alt="Ally Chat"/>](https://cdn.modrinth.com/data/cached_images/338e7c32ad00e83354bf6e8a54cb1c13e9ca3c0a.png) |
| `/team ally <team>`<br><sub>*(or `/ally <team>`)*</sub> | Send alliance request to another team *(Leader only)* | [<img src="https://cdn.modrinth.com/data/cached_images/f439ebbfbdf315c46547b53900514e344bd2f8b4.png" width="280" alt="Ally Request"/>](https://cdn.modrinth.com/data/cached_images/f439ebbfbdf315c46547b53900514e344bd2f8b4.png) |
| `/team ally accept\|deny` | Respond to an incoming alliance request *(Leader only)* | - |
| `/team ally break` | Break your current team alliance *(Leader only)* | [<img src="https://cdn.modrinth.com/data/cached_images/1ac0d9407b71ede51cdecd466df3d0c6f5dd19f3.png" width="280" alt="Break Ally"/>](https://cdn.modrinth.com/data/cached_images/1ac0d9407b71ede51cdecd466df3d0c6f5dd19f3.png) |
| `/team disband <team>` | Force-disband any team *(Admin only)* | - |
| `/team reload` | Reload configuration *(Admin only)* | - |

---

## Configuration

<details>
<summary><strong>Default config.yml (Click to expand)</strong></summary>

```yaml
settings:
  min-name-length: 2
  max-name-length: 16
  max-members: 10
  invite-expire-seconds: 60
  ally-request-expire-seconds: 60
  default-team-pvp: true
  pvp-vote-timeout-seconds: 30

messages:
  prefix: "&8[&7Team&8] "
  ally-prefix: "&8[&7Ally&8] "
  chat-format: "&8[&7TC&8] &f<player>&8: &7<message>"
  ally-chat-format: "&8[&7AC&8] &8[&7<team>&8] &f<player>&8: &7<message>"

  player-only: "&7This command is for players only."
  no-permission: "&7You don't have permission to do that."
  reloaded: "&7Config reloaded."

  team-created: "&7Created &f<team>&7."
  team-disbanded: "&f<team> &7has been disbanded."
  already-in-team: "&7You're already in a team."
  not-in-team: "&7You're not in a team."
  not-leader: "&7Only the leader can do that."
  team-exists: "&f<team> &7already exists."
  team-not-found: "&7Couldn't find team &f<team>&7."
  invalid-name: "&7Name must be &f<min>-<max> &7characters (letters, numbers, underscores)."
  team-full: "&7Team is full (&f<max>&7)."

  invite-sent: "&7Invited &f<player>&7."
  invite-received: "&7You've been invited to &f<team>&7. "
  already-invited: "&f<player> &7already has a pending invite."
  target-in-team: "&f<player> &7is already in a team."
  player-not-found: "&f<player> &7is not online."
  no-pending-invite: "&7You don't have a pending invite."
  invite-denied: "&7Denied the invite from &f<team>&7."
  invite-denied-notify: "&f<player> &7declined your invite."
  member-joined: "&f<player> &7joined."
  member-left: "&f<player> &7left."
  leader-cannot-leave: "&7Transfer leadership first (&f/team transfer <player>&7) or &f/team disband&7."
  member-kicked: "&f<player> &7was kicked."
  kicked-notify: "&7You were kicked from &f<team>&7."
  not-in-your-team: "&f<player> &7isn't in your team."
  cannot-kick-self: "&7You can't kick yourself."
  leader-transferred: "&f<player> &7is the new leader."

  pvp-status: "&7Team PvP: &f<state>"
  pvp-changed: "&7Team PvP is now <state>&7."
  pvp-vote-started: "&f<player> &7wants to set Team PvP to <state>&7. "
  pvp-vote-cast: "&f<player> &7voted <vote>&7."
  pvp-vote-passed: "&7Vote passed &8(&f<yes> &7yes, &f<no> &7no&8)&7. Team PvP is now <state>&7."
  pvp-vote-failed: "&7Vote didn't pass &8(&f<yes> &7yes, &f<no> &7no&8)&7. Team PvP stays <state>&7."
  pvp-vote-already-active: "&7There's already a vote going on."
  pvp-already-voted: "&7You already voted."
  pvp-no-active-vote: "&7No vote is happening right now."
  pvp-not-eligible: "&7You can't vote on this."
  pvp-already-set: "&7Team PvP is already <state>&7."
  friendly-fire-blocked: "&7Friendly fire is off."

  chat-toggled-on: "&7Team chat: &fON"
  chat-toggled-off: "&7Team chat: &fOFF"
  ally-chat-toggled-on: "&7Ally chat: &fON"
  ally-chat-toggled-off: "&7Ally chat: &fOFF"

  ally-request-sent: "&7Sent ally request to &f<team>&7."
  ally-request-received: "&f<team> &7wants to ally. "
  ally-accepted: "&7Now allied with &f<team>&7."
  ally-denied: "&7Denied ally request from &f<team>&7."
  ally-denied-notify: "&f<team> &7turned down your ally request."
  ally-broken: "&7Alliance with &f<team> &7is over."
  already-have-ally: "&7You already have an ally (&f<ally>&7)."
  target-has-ally: "&f<team> &7already has an ally."
  no-ally: "&7Your team doesn't have an ally."
  no-ally-request: "&7No pending ally request."
  cannot-ally-self: "&7You can't ally with your own team."
```

</details>

---

## Requirements

- Paper / Purpur 1.21.4+
- Java 21+

## Support

If you enjoy the plugin and would like to support its development:

<p align="center">
  <a href="https://ko-fi.com/I6C527B4XK" target="_blank">
    <img src="https://storage.ko-fi.com/cdn/kofi2.png?v=3" height="38" alt="Support me on Ko-fi" />
  </a>
</p>
