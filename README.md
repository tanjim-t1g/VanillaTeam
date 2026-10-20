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

<details>
<summary><strong>Team Management</strong></summary>

### Create a team
`/team create <name>`

Creates a new team.

<p align="center">
  <img src="assets/images/TeamCreate1.png" width="350" alt="Create Team 1">
  <img src="assets/images/TeamCreate2.png" width="350" alt="Create Team 2">
</p>

> **Permission:** `vanillateams.use`

### Invite a player
`/team invite <player>`

Sends an invitation to a player to join your team.

<p align="center">
  <img src="assets/images/TeamInvite1.png" width="350" alt="Team Invite 1">
  <img src="assets/images/TeamInvite2-P2.png" width="350" alt="Team Invite 2">
</p>

> **Permission:** `vanillateams.use`

### Accept or decline an invitation
- `/team accept <team>` — Accept an invitation.
- `/team deny <team>` — Decline an invitation.

<p align="center">
  <img src="assets/images/TeamAccept-P2.png" width="350" alt="Team Accept 1">
  <img src="assets/images/TeamAccept2-P2.png" width="350" alt="Team Accept 2">
</p>

<p align="center">
  <img src="assets/images/TeamDeny1-P2.png" width="350" alt="Team Deny 1">
  <img src="assets/images/Teamdeny2-P2.png" width="350" alt="Team Deny 2">
</p>

> **Permission:** `vanillateams.use`

### Leave a team
`/team leave`

Leaves your current team.

<p align="center">
  <img src="assets/images/TeamLeave1-P2.png" width="350" alt="Leave Team 1">
  <img src="assets/images/TeamLeave2-P2.png" width="350" alt="Leave Team 2">
</p>

> **Permission:** `vanillateams.use`

### Kick a member
`/team kick <player>`

Removes a member from your team. Team leaders only.

<p align="center">
  <img src="assets/images/TeamKick-P1.png" width="350" alt="Kick Team Member 1">
  <img src="assets/images/TeamKick2-P1.png" width="350" alt="Kick Team Member 2">
</p>

> **Permission:** `vanillateams.use`

### Transfer leadership
`/team transfer <player>` *(or `/team leader <player>`)*

Transfers leadership to another team member.

<p align="center">
  <img src="assets/images/TeamTransfer1-P1.png" width="350" alt="Transfer Leadership 1">
  <img src="assets/images/TeamTransfer2-P1.png" width="350" alt="Transfer Leadership 2">
</p>

> **Permission:** `vanillateams.use`

### View team information
`/team info [team]`

Displays team information and its members.

<p align="center">
  <img src="assets/images/TeamInfo.png" width="350" alt="Team Information">
</p>

> **Permission:** `vanillateams.use`

### Disband a team
`/team disband`

Disbands your team. Team leaders only.

<p align="center">
  <img src="assets/images/TeamDisband1.png" width="350" alt="Disband Team 1">
  <img src="assets/images/TeamDisband2.png" width="350" alt="Disband Team 2">
</p>

> **Permission:** `vanillateams.use`

### List all teams
`/teams`

Lists all teams on the server.

> **Permission:** `vanillateams.use`

</details>

<details>
<summary><strong>Friendly Fire Voting</strong></summary>

### Start a vote
- `/team pvp on` — Propose enabling friendly fire.
- `/team pvp off` — Propose disabling friendly fire.

Starts a vote to change friendly fire settings for your team.

<p align="center">
  <img src="assets/images/TeamVoting1.png" width="350" alt="Friendly Fire Voting">
</p>

> **Permission:** `vanillateams.use`

### Cast your vote
- `/team pvp yes` — Vote yes.
- `/team pvp no` — Vote no.

Casts your vote on an active friendly fire proposal.

> **Permission:** `vanillateams.use`

</details>

<details>
<summary><strong>Team and Ally Chat</strong></summary>

### Team chat
- `/tc <message>` — Send a message to your team.
- `/tc` — Toggle team chat mode.

<p align="center">
  <img src="assets/images/TeamTc.png" width="350" alt="Team Chat">
  <img src="assets/images/TeamMessage.png" width="350" alt="Team Message">
</p>

<p align="center">
  <img src="assets/images/TeamTc2.png" width="350" alt="Team Chat 2">
</p>

> **Permission:** `vanillateams.use`

### Ally chat
- `/ac <message>` — Send a message to your allied team.
- `/ac` — Toggle ally chat mode.

<p align="center">
  <img src="assets/images/AllyChat.png" width="350" alt="Ally Chat">
</p>

> **Permission:** `vanillateams.use`

</details>

<details>
<summary><strong>Alliances</strong></summary>

### Request an alliance
`/team ally <team>` *(or `/ally <team>`)*

Sends an alliance request to another team.

<p align="center">
  <img src="assets/images/TeamAllyAccept.png" width="350" alt="Alliance Request">
</p>

> **Permission:** `vanillateams.use`

### Accept or decline an alliance
- `/team ally accept` *(or `/ally accept`)* — Accept an incoming alliance request.
- `/team ally deny` *(or `/ally deny`)* — Decline an incoming alliance request.

> **Permission:** `vanillateams.use`

### Break an alliance
`/team ally break` *(or `/ally break`)*

Ends your current alliance.

<p align="center">
  <img src="assets/images/TeamAllyBreak.png" width="350" alt="Break Alliance">
</p>

> **Permission:** `vanillateams.use`

</details>

<details>
<summary><strong>Administration</strong></summary>

### Force-disband a team
`/team disband <team>`

Force-disbands a team.

> **Permission:** `vanillateams.admin`

### Reload configuration
`/team reload`

Reloads the plugin configuration.

> **Permission:** `vanillateams.admin`

</details>

---

## Requirements

- Paper / Purpur 1.21.4+ (or Paper 26.x)
- Java 21+ (Java 25 if running 26.1+)
