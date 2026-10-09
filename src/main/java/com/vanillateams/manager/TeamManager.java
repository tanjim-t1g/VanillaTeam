package com.vanillateams.manager;

import com.vanillateams.VanillaTeams;
import com.vanillateams.data.DatabaseManager;
import com.vanillateams.model.Team;
import com.vanillateams.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TeamManager {
    public record Invite(String teamName, long expiresAt) {
        public boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    public record AllyRequest(String senderTeam, long expiresAt) {
        public boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    private final VanillaTeams plugin;
    private final DatabaseManager db;

    private final Map<String, Team> teamsByName = new LinkedHashMap<>();
    private final Map<UUID, Team> teamsByPlayer = new HashMap<>();
    private final Map<UUID, Invite> pendingInvites = new HashMap<>();
    private final Map<String, AllyRequest> pendingAllyRequests = new HashMap<>();
    private final Set<UUID> teamChatToggled = new HashSet<>();
    private final Set<UUID> allyChatToggled = new HashSet<>();

    public TeamManager(VanillaTeams plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }

    public void loadTeams() {
        teamsByName.clear();
        teamsByPlayer.clear();
        for (Team team : db.loadAllTeams()) {
            teamsByName.put(team.getName().toLowerCase(), team);
            for (UUID memberId : team.getMemberIds()) {
                teamsByPlayer.put(memberId, team);
            }
        }
    }

    public Collection<Team> getAllTeams() {
        return Collections.unmodifiableCollection(teamsByName.values());
    }

    public Team getTeamByName(String name) {
        if (name == null) return null;
        return teamsByName.get(name.toLowerCase());
    }

    public Team getPlayerTeam(UUID uuid) {
        return teamsByPlayer.get(uuid);
    }

    public Team getPlayerTeam(Player player) {
        return teamsByPlayer.get(player.getUniqueId());
    }

    public boolean areTeammates(Player a, Player b) {
        Team teamA = getPlayerTeam(a.getUniqueId());
        return teamA != null && teamA.equals(getPlayerTeam(b.getUniqueId()));
    }

    public Team createTeam(String name, Player leader) {
        boolean defaultPvp = plugin.getConfig().getBoolean("settings.default-team-pvp", true);
        Team team = new Team(name, leader.getUniqueId(), leader.getName(), defaultPvp);
        teamsByName.put(name.toLowerCase(), team);
        teamsByPlayer.put(leader.getUniqueId(), team);
        db.createTeam(team);
        return team;
    }

    public void disbandTeam(Team team) {
        if (team.hasAlly()) {
            Team ally = getTeamByName(team.getAllyTeam());
            if (ally != null) {
                ally.setAllyTeam(null);
                db.updateAlly(ally.getName(), null);
                broadcastAllyMessage(ally, plugin.msg("ally-broken").replace("<team>", team.getName()));
            }
        }

        for (UUID memberId : team.getMemberIds()) {
            teamsByPlayer.remove(memberId);
            teamChatToggled.remove(memberId);
            allyChatToggled.remove(memberId);
        }

        pendingAllyRequests.remove(team.getName().toLowerCase());
        teamsByName.remove(team.getName().toLowerCase());
        db.deleteTeam(team.getName());
    }

    public void addMember(Team team, Player player) {
        team.addMember(player.getUniqueId(), player.getName());
        teamsByPlayer.put(player.getUniqueId(), team);
        pendingInvites.remove(player.getUniqueId());
        db.addOrUpdateMember(player.getUniqueId(), player.getName(), team.getName(), "MEMBER");
    }

    public void removeMember(Team team, UUID memberId) {
        team.removeMember(memberId);
        teamsByPlayer.remove(memberId);
        teamChatToggled.remove(memberId);
        allyChatToggled.remove(memberId);
        db.removeMember(memberId);
    }

    public void transferLeader(Team team, UUID newLeaderUuid) {
        UUID oldLeader = team.getLeader();
        team.setLeader(newLeaderUuid);
        db.updateLeader(team, oldLeader);
    }

    public void setTeamPvp(Team team, boolean enabled) {
        team.setPvpEnabled(enabled);
        db.updatePvp(team.getName(), enabled);
    }

    public void createInvite(UUID targetPlayer, String teamName) {
        int seconds = plugin.getConfig().getInt("settings.invite-expire-seconds", 60);
        pendingInvites.put(targetPlayer, new Invite(teamName, System.currentTimeMillis() + (seconds * 1000L)));
    }

    public Invite getInvite(UUID targetPlayer) {
        Invite invite = pendingInvites.get(targetPlayer);
        if (invite != null && invite.isExpired()) {
            pendingInvites.remove(targetPlayer);
            return null;
        }
        return invite;
    }

    public void removeInvite(UUID targetPlayer) {
        pendingInvites.remove(targetPlayer);
    }

    public void createAllyRequest(Team sender, Team target) {
        int seconds = plugin.getConfig().getInt("settings.ally-request-expire-seconds", 60);
        pendingAllyRequests.put(
                target.getName().toLowerCase(),
                new AllyRequest(sender.getName(), System.currentTimeMillis() + (seconds * 1000L))
        );
    }

    public AllyRequest getAllyRequest(Team target) {
        AllyRequest req = pendingAllyRequests.get(target.getName().toLowerCase());
        if (req != null && req.isExpired()) {
            pendingAllyRequests.remove(target.getName().toLowerCase());
            return null;
        }
        return req;
    }

    public void removeAllyRequest(Team target) {
        pendingAllyRequests.remove(target.getName().toLowerCase());
    }

    public void setAlliance(Team teamA, Team teamB) {
        teamA.setAllyTeam(teamB.getName());
        teamB.setAllyTeam(teamA.getName());
        pendingAllyRequests.remove(teamA.getName().toLowerCase());
        pendingAllyRequests.remove(teamB.getName().toLowerCase());
        db.updateAlly(teamA.getName(), teamB.getName());
        db.updateAlly(teamB.getName(), teamA.getName());
    }

    public void breakAlliance(Team teamA, Team teamB) {
        teamA.setAllyTeam(null);
        db.updateAlly(teamA.getName(), null);
        for (UUID id : teamA.getMemberIds()) {
            allyChatToggled.remove(id);
        }
        if (teamB != null) {
            teamB.setAllyTeam(null);
            db.updateAlly(teamB.getName(), null);
            for (UUID id : teamB.getMemberIds()) {
                allyChatToggled.remove(id);
            }
        }
    }

    public boolean toggleTeamChat(UUID uuid) {
        if (teamChatToggled.contains(uuid)) {
            teamChatToggled.remove(uuid);
            return false;
        } else {
            teamChatToggled.add(uuid);
            allyChatToggled.remove(uuid);
            return true;
        }
    }

    public boolean isTeamChatToggled(UUID uuid) {
        return teamChatToggled.contains(uuid);
    }

    public void disableTeamChat(UUID uuid) {
        teamChatToggled.remove(uuid);
    }

    public boolean toggleAllyChat(UUID uuid) {
        if (allyChatToggled.contains(uuid)) {
            allyChatToggled.remove(uuid);
            return false;
        } else {
            allyChatToggled.add(uuid);
            teamChatToggled.remove(uuid);
            return true;
        }
    }

    public boolean isAllyChatToggled(UUID uuid) {
        return allyChatToggled.contains(uuid);
    }

    public void disableAllyChat(UUID uuid) {
        allyChatToggled.remove(uuid);
    }

    public void sendTeamChat(Team team, Player sender, String message) {
        String raw = plugin.getConfig().getString("messages.chat-format", "&8[&7TC&8] &f<player>&8: &7<message>")
                .replace("<player>", sender.getName())
                .replace("<team>", team.getName())
                .replace("<message>", message);
        Component component = MessageUtil.parse(raw);
        for (UUID memberId : team.getMemberIds()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage(component);
            }
        }
    }

    public void sendAllyChat(Team senderTeam, Player sender, String message) {
        String raw = plugin.getConfig().getString("messages.ally-chat-format", "&8[&7AC&8] &8[&7<team>&8] &f<player>&8: &7<message>")
                .replace("<player>", sender.getName())
                .replace("<team>", senderTeam.getName())
                .replace("<message>", message);
        Component component = MessageUtil.parse(raw);

        for (UUID memberId : senderTeam.getMemberIds()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage(component);
            }
        }

        if (senderTeam.hasAlly()) {
            Team allyTeam = getTeamByName(senderTeam.getAllyTeam());
            if (allyTeam != null) {
                for (UUID memberId : allyTeam.getMemberIds()) {
                    Player member = Bukkit.getPlayer(memberId);
                    if (member != null && member.isOnline()) {
                        member.sendMessage(component);
                    }
                }
            }
        }
    }

    public void broadcastTeamMessage(Team team, String rawMsgWithPrefix) {
        Component parsed = MessageUtil.parse(rawMsgWithPrefix);
        for (UUID memberId : team.getMemberIds()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage(parsed);
            }
        }
    }

    public void broadcastAllyMessage(Team team, String rawMsgWithoutPrefix) {
        String prefix = plugin.getConfig().getString("messages.ally-prefix", "&8[&7Ally&8] ");
        Component parsed = MessageUtil.parse(prefix + rawMsgWithoutPrefix);
        for (UUID memberId : team.getMemberIds()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage(parsed);
            }
        }
    }

    public void updatePlayerNameOnJoin(Player player) {
        Team team = getPlayerTeam(player.getUniqueId());
        if (team != null) {
            String stored = team.getMemberName(player.getUniqueId());
            if (!player.getName().equals(stored)) {
                team.updateMemberName(player.getUniqueId(), player.getName());
                db.updateUsername(player.getUniqueId(), player.getName(), team.isLeader(player.getUniqueId()), team.getName());
            }
        }
    }
}
