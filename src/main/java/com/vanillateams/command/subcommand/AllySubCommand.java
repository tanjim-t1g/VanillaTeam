package com.vanillateams.command.subcommand;

import com.vanillateams.VanillaTeams;
import com.vanillateams.manager.TeamManager;
import com.vanillateams.model.Team;
import com.vanillateams.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AllySubCommand implements SubCommand {
    private final VanillaTeams plugin;
    private final TeamManager teamManager;

    public AllySubCommand(VanillaTeams plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
    }

    @Override
    public String getName() {
        return "ally";
    }

    @Override
    public String getDescription() {
        return "Manage your team's alliance";
    }

    @Override
    public String getSyntax() {
        return "/team ally <team|accept|deny|break>";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Team team = teamManager.getPlayerTeam(player);

        if (team == null) {
            plugin.sendPrefixed(player, plugin.msg("not-in-team"));
            return;
        }

        if (!team.isLeader(player.getUniqueId())) {
            plugin.sendPrefixed(player, plugin.msg("not-leader"));
            return;
        }

        if (args.length < 2) {
            if (team.hasAlly()) {
                plugin.sendPrefixed(player, "&7Usage: &f/team ally break");
            } else if (teamManager.getAllyRequest(team) != null) {
                plugin.sendPrefixed(player, "&7Usage: &f/team ally <accept|deny>");
            } else {
                plugin.sendPrefixed(player, "&7Usage: &f/team ally <team>");
            }
            return;
        }

        String action = args[1].toLowerCase();
        switch (action) {
            case "accept" -> handleAllyAccept(player, team);
            case "deny" -> handleAllyDeny(player, team);
            case "break" -> handleAllyBreak(player, team);
            default -> handleAllyRequest(player, team, args[1]);
        }
    }

    private void handleAllyRequest(Player player, Team senderTeam, String targetTeamName) {
        if (senderTeam.hasAlly()) {
            plugin.sendPrefixed(player, plugin.msg("already-have-ally").replace("<ally>", senderTeam.getAllyTeam()));
            return;
        }

        Team targetTeam = teamManager.getTeamByName(targetTeamName);
        if (targetTeam == null) {
            plugin.sendPrefixed(player, plugin.msg("team-not-found").replace("<team>", targetTeamName));
            return;
        }

        if (senderTeam.equals(targetTeam)) {
            plugin.sendPrefixed(player, plugin.msg("cannot-ally-self"));
            return;
        }

        if (targetTeam.hasAlly()) {
            plugin.sendPrefixed(player, plugin.msg("target-has-ally").replace("<team>", targetTeam.getName()));
            return;
        }

        teamManager.createAllyRequest(senderTeam, targetTeam);
        plugin.sendPrefixed(player, plugin.msg("ally-request-sent").replace("<team>", targetTeam.getName()));

        Component allyMsg = MessageUtil.parse(plugin.allyPrefix() + plugin.msg("ally-request-received").replace("<team>", senderTeam.getName()))
                .append(MessageUtil.acceptDenyButtons("/team ally accept", "/team ally deny"));
        for (UUID memberId : targetTeam.getMemberIds()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage(allyMsg);
            }
        }
    }

    private void handleAllyAccept(Player player, Team receiverTeam) {
        if (receiverTeam.hasAlly()) {
            plugin.sendPrefixed(player, plugin.msg("already-have-ally").replace("<ally>", receiverTeam.getAllyTeam()));
            return;
        }

        TeamManager.AllyRequest req = teamManager.getAllyRequest(receiverTeam);
        if (req == null) {
            plugin.sendPrefixed(player, plugin.msg("no-ally-request"));
            return;
        }

        Team senderTeam = teamManager.getTeamByName(req.senderTeam());
        if (senderTeam == null) {
            teamManager.removeAllyRequest(receiverTeam);
            plugin.sendPrefixed(player, plugin.msg("team-not-found").replace("<team>", req.senderTeam()));
            return;
        }

        if (senderTeam.hasAlly()) {
            teamManager.removeAllyRequest(receiverTeam);
            plugin.sendPrefixed(player, plugin.msg("target-has-ally").replace("<team>", senderTeam.getName()));
            return;
        }

        teamManager.setAlliance(senderTeam, receiverTeam);
        teamManager.broadcastAllyMessage(senderTeam, plugin.msg("ally-accepted").replace("<team>", receiverTeam.getName()));
        teamManager.broadcastAllyMessage(receiverTeam, plugin.msg("ally-accepted").replace("<team>", senderTeam.getName()));
    }

    private void handleAllyDeny(Player player, Team receiverTeam) {
        TeamManager.AllyRequest req = teamManager.getAllyRequest(receiverTeam);
        if (req == null) {
            plugin.sendPrefixed(player, plugin.msg("no-ally-request"));
            return;
        }

        teamManager.removeAllyRequest(receiverTeam);
        plugin.sendPrefixed(player, plugin.msg("ally-denied").replace("<team>", req.senderTeam()));

        Team senderTeam = teamManager.getTeamByName(req.senderTeam());
        if (senderTeam != null) {
            teamManager.broadcastAllyMessage(senderTeam, plugin.msg("ally-denied-notify").replace("<team>", receiverTeam.getName()));
        }
    }

    private void handleAllyBreak(Player player, Team team) {
        if (!team.hasAlly()) {
            plugin.sendPrefixed(player, plugin.msg("no-ally"));
            return;
        }

        String allyName = team.getAllyTeam();
        Team allyTeam = teamManager.getTeamByName(allyName);
        teamManager.breakAlliance(team, allyTeam);

        teamManager.broadcastAllyMessage(team, plugin.msg("ally-broken").replace("<team>", allyName));
        if (allyTeam != null) {
            teamManager.broadcastAllyMessage(allyTeam, plugin.msg("ally-broken").replace("<team>", team.getName()));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2 && sender instanceof Player p) {
            Team t = teamManager.getPlayerTeam(p.getUniqueId());
            if (t != null && t.isLeader(p.getUniqueId())) {
                if (t.hasAlly()) {
                    return List.of("break").stream()
                            .filter(s -> s.startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                } else {
                    List<String> suggestions = new ArrayList<>();
                    if (teamManager.getAllyRequest(t) != null) {
                        suggestions.add("accept");
                        suggestions.add("deny");
                    }
                    for (Team other : teamManager.getAllTeams()) {
                        if (!other.getName().equalsIgnoreCase(t.getName()) && !other.hasAlly()) {
                            suggestions.add(other.getName());
                        }
                    }
                    return suggestions.stream()
                            .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                }
            }
        }
        return Collections.emptyList();
    }
}
