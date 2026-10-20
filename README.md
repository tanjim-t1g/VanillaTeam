# VanillaTeams

A lightweight vanilla-friendly team plugin for Paper and Purpur servers. 

No `/team home`, no warps, and no virtual team chests. Just basic teams, team chat, alliances, and vote-based friendly fire.

---

## Supported server software

<table border="0" align="center" cellpadding="8">
  <tr align="center">
    <td><img src="assets/software/paper_logo.png" alt="Paper" height="60" /></td>
    <td><img src="assets/software/purpur_logo.png" alt="Purpur" height="60" /></td>
    <td><img src="assets/software/spigot_logo.png" alt="Spigot" height="60" /></td>
  </tr>
  <tr align="center" valign="top">
    <td><sub><b>Paper</b></sub></td>
    <td><sub><b>Purpur</b></sub></td>
    <td><sub><b>Spigot</b></sub></td>
  </tr>
</table>

<br>

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

<details open>
<summary><strong>Team Management</strong></summary>
<br>

| Command | Description | In-Game Preview |
|---|---|:---:|
| `/team create <name>` | Creates a new team.<br><br><sub><b>Permission:</b> `vanillateams.use`</sub> | <img src="assets/images/TeamCreate1.png" width="250" alt="Create Team 1"/> <img src="assets/images/TeamCreate2.png" width="250" alt="Create Team 2"/> |
| `/team invite <player>` | Sends an invitation to join your team.<br><br><sub><b>Role:</b> Leader only<br><b>Permission:</b> `vanillateams.use`</sub> | <sub><b>Leader perspective:</b></sub><br><img src="assets/images/TeamInvite1.png" width="250" alt="Invite 1"/><br><sub><b>Target perspective:</b></sub><br><img src="assets/images/TeamInvite2-P2.png" width="250" alt="Invite 2"/> |
| `/team accept <team>` | Accepts a pending team invitation.<br><br><sub><b>Permission:</b> `vanillateams.use`</sub> | <img src="assets/images/TeamAccept-P2.png" width="250" alt="Accept 1"/> <img src="assets/images/TeamAccept2-P2.png" width="250" alt="Accept 2"/> |
| `/team deny <team>` | Declines a pending team invitation.<br><br><sub><b>Permission:</b> `vanillateams.use`</sub> | <img src="assets/images/TeamDeny1-P2.png" width="250" alt="Deny 1"/> <img src="assets/images/Teamdeny2-P2.png" width="250" alt="Deny 2"/> |
| `/team leave` | Leaves your current team.<br><br><sub><b>Permission:</b> `vanillateams.use`</sub> | <img src="assets/images/TeamLeave1-P2.png" width="250" alt="Leave 1"/> <img src="assets/images/TeamLeave2-P2.png" width="250" alt="Leave 2"/> |
| `/team kick <player>` | Removes a member from your team.<br><br><sub><b>Role:</b> Leader only<br><b>Permission:</b> `vanillateams.use`</sub> | <img src="assets/images/TeamKick-P1.png" width="250" alt="Kick 1"/> <img src="assets/images/TeamKick2-P1.png" width="250" alt="Kick 2"/> |
| `/team transfer <player>`<br>*(or `/team leader <player>`)* | Transfers team leadership to another member.<br><br><sub><b>Role:</b> Leader only<br><b>Permission:</b> `vanillateams.use`</sub> | <img src="assets/images/TeamTransfer1-P1.png" width="250" alt="Transfer 1"/> <img src="assets/images/TeamTransfer2-P1.png" width="250" alt="Transfer 2"/> |
| `/team info [team]` | Displays team stats, leader, and members.<br><br><sub><b>Permission:</b> `vanillateams.use`</sub> | <img src="assets/images/TeamInfo.png" width="280" alt="Team Info"/> |
| `/team disband` | Disbands your team completely.<br><br><sub><b>Role:</b> Leader only<br><b>Permission:</b> `vanillateams.use`</sub> | <img src="assets/images/TeamDisband1.png" width="250" alt="Disband 1"/> <img src="assets/images/TeamDisband2.png" width="250" alt="Disband 2"/> |
| `/teams` | Lists all existing teams on the server.<br><br><sub><b>Permission:</b> `vanillateams.use`</sub> | <sub>*(List view)*</sub> |

</details>

<details>
<summary><strong>Friendly Fire Voting</strong></summary>
<br>

| Command | Description | In-Game Preview |
|---|---|:---:|
| `/team pvp on`<br>`/team pvp off` | Starts a team-wide vote to enable or disable friendly fire.<br><br><sub><b>Permission:</b> `vanillateams.use`</sub> | <img src="assets/images/TeamVoting1.png" width="280" alt="Friendly Fire Voting"/> |
| `/team pvp yes`<br>`/team pvp no` | Casts your vote on an active friendly fire proposal.<br><br><sub><b>Permission:</b> `vanillateams.use`</sub> | <sub>*(Clickable buttons in chat)*</sub> |

</details>

<details>
<summary><strong>Team and Ally Chat</strong></summary>
<br>

| Command | Description | In-Game Preview |
|---|---|:---:|
| `/tc <message>`<br>`/tc` | Sends a message to team chat or toggles team chat mode.<br><br><sub><b>Permission:</b> `vanillateams.use`</sub> | <img src="assets/images/TeamTc.png" width="250" alt="Team Chat Toggle"/> <img src="assets/images/TeamMessage.png" width="250" alt="Team Message"/><br><img src="assets/images/TeamTc2.png" width="250" alt="Team Chat Mode"/> |
| `/ac <message>`<br>`/ac` | Sends a message to ally chat or toggles ally chat mode.<br><br><sub><b>Permission:</b> `vanillateams.use`</sub> | <img src="assets/images/AllyChat.png" width="280" alt="Ally Chat"/> |

</details>

<details>
<summary><strong>Alliances</strong></summary>
<br>

| Command | Description | In-Game Preview |
|---|---|:---:|
| `/team ally <team>`<br>*(or `/ally <team>`)* | Sends an alliance request to another team.<br><br><sub><b>Role:</b> Leader only<br><b>Permission:</b> `vanillateams.use`</sub> | <img src="assets/images/TeamAllyAccept.png" width="280" alt="Alliance Request"/> |
| `/team ally accept`<br>`/team ally deny` | Responds to an incoming alliance request.<br><br><sub><b>Role:</b> Leader only<br><b>Permission:</b> `vanillateams.use`</sub> | <sub>*(Clickable <b>[Accept]</b> / <b>[Deny]</b> buttons)*</sub> |
| `/team ally break`<br>*(or `/ally break`)* | Ends your current alliance.<br><br><sub><b>Role:</b> Leader only<br><b>Permission:</b> `vanillateams.use`</sub> | <img src="assets/images/TeamAllyBreak.png" width="280" alt="Break Alliance"/> |

</details>

<details>
<summary><strong>Administration</strong></summary>
<br>

| Command | Description | In-Game Preview |
|---|---|:---:|
| `/team disband <team>` | Force-disbands a specific team.<br><br><sub><b>Permission:</b> `vanillateams.admin`</sub> | <sub>*(Console / Operator command)*</sub> |
| `/team reload` | Reloads the plugin configuration.<br><br><sub><b>Permission:</b> `vanillateams.admin`</sub> | <sub>*(Console / Operator command)*</sub> |

</details>

---

## Requirements

- Paper / Purpur 1.21.4+ (or Paper 26.x)
- Java 21+ (Java 25 if running 26.1+)
