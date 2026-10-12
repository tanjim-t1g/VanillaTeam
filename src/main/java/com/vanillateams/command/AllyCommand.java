package com.vanillateams.command;

import com.vanillateams.VanillaTeams;
import com.vanillateams.manager.TeamManager;
import com.vanillateams.model.Team;
import com.vanillateams.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AllyCommand implements CommandExecutor, TabCompleter {
    private final VanillaTeams plugin;
    private final TeamManager teamManager;

    public AllyCommand(VanillaTeams plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessageUtil.parse(plugin.msg("player-only")));
            return true;
        }

        Team team = teamManager.getPlayerTeam(player);
        if (team == null) {
            plugin.sendAllyPrefixed(player, plugin.msg("not-in-team"));
            return true;
        }

        if (args.length == 0) {
            plugin.sendAllyPrefixed(player, "&7Usage: &f/ally <team|accept|deny|break|chat>");
            return true;
        }

        String sub = args[0].toLowerCase();
        if (sub.equals("chat") || sub.equals("c")) {
            if (!team.hasAlly()) {
                plugin.sendAllyPrefixed(player, plugin.msg("no-ally"));
                return true;
            }
            if (args.length == 1) {
                boolean toggled = teamManager.toggleAllyChat(player.getUniqueId());
                plugin.sendAllyPrefixed(player, toggled ? plugin.msg("ally-chat-toggled-on") : plugin.msg("ally-chat-toggled-off"));
                return true;
            }
            if (args.length == 2) {
                String arg = args[1].toLowerCase();
                if (arg.equals("on") || arg.equals("enable") || arg.equals("true")) {
                    teamManager.setAllyChatToggled(player.getUniqueId(), true);
                    plugin.sendAllyPrefixed(player, plugin.msg("ally-chat-toggled-on"));
                    return true;
                }
                if (arg.equals("off") || arg.equals("disable") || arg.equals("false")) {
                    teamManager.setAllyChatToggled(player.getUniqueId(), false);
                    plugin.sendAllyPrefixed(player, plugin.msg("ally-chat-toggled-off"));
                    return true;
                }
            }
            String msg = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
            teamManager.sendAllyChat(team, player, msg);
            return true;
        }

        if (!team.isLeader(player.getUniqueId())) {
            plugin.sendAllyPrefixed(player, plugin.msg("not-leader"));
            return true;
        }

        switch (sub) {
            case "accept" -> handleAccept(player, team);
            case "deny" -> handleDeny(player, team);
            case "break", "remove", "unally" -> handleBreak(player, team);
            default -> handleRequest(player, team, args[0]);
        }
        return true;
    }

    private void handleRequest(Player player, Team senderTeam, String targetTeamName) {
        if (senderTeam.hasAlly()) {
            plugin.sendAllyPrefixed(player, plugin.msg("already-have-ally").replace("<ally>", senderTeam.getAllyTeam()));
            return;
        }

        Team targetTeam = teamManager.getTeamByName(targetTeamName);
        if (targetTeam == null) {
            plugin.sendAllyPrefixed(player, plugin.msg("team-not-found").replace("<team>", targetTeamName));
            return;
        }

        if (senderTeam.equals(targetTeam)) {
            plugin.sendAllyPrefixed(player, plugin.msg("cannot-ally-self"));
            return;
        }

        if (targetTeam.hasAlly()) {
            plugin.sendAllyPrefixed(player, plugin.msg("target-has-ally").replace("<team>", targetTeam.getName()));
            return;
        }

        teamManager.createAllyRequest(senderTeam, targetTeam);
        plugin.sendAllyPrefixed(player, plugin.msg("ally-request-sent").replace("<team>", targetTeam.getName()));

        Component allyMsg = MessageUtil.parse(plugin.allyPrefix() + plugin.msg("ally-request-received").replace("<team>", senderTeam.getName()))
                .append(MessageUtil.acceptDenyButtons("/ally accept", "/ally deny"));
        for (UUID memberId : targetTeam.getMemberIds()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage(allyMsg);
            }
        }
    }

    private void handleAccept(Player player, Team receiverTeam) {
        if (receiverTeam.hasAlly()) {
            plugin.sendAllyPrefixed(player, plugin.msg("already-have-ally").replace("<ally>", receiverTeam.getAllyTeam()));
            return;
        }

        TeamManager.AllyRequest req = teamManager.getAllyRequest(receiverTeam);
        if (req == null) {
            plugin.sendAllyPrefixed(player, plugin.msg("no-ally-request"));
            return;
        }

        Team senderTeam = teamManager.getTeamByName(req.senderTeam());
        if (senderTeam == null) {
            teamManager.removeAllyRequest(receiverTeam);
            plugin.sendAllyPrefixed(player, plugin.msg("team-not-found").replace("<team>", req.senderTeam()));
            return;
        }

        if (senderTeam.hasAlly()) {
            teamManager.removeAllyRequest(receiverTeam);
            plugin.sendAllyPrefixed(player, plugin.msg("target-has-ally").replace("<team>", senderTeam.getName()));
            return;
        }

        teamManager.setAlliance(senderTeam, receiverTeam);
        teamManager.broadcastAllyMessage(senderTeam, plugin.msg("ally-accepted").replace("<team>", receiverTeam.getName()));
        teamManager.broadcastAllyMessage(receiverTeam, plugin.msg("ally-accepted").replace("<team>", senderTeam.getName()));
    }

    private void handleDeny(Player player, Team receiverTeam) {
        TeamManager.AllyRequest req = teamManager.getAllyRequest(receiverTeam);
        if (req == null) {
            plugin.sendAllyPrefixed(player, plugin.msg("no-ally-request"));
            return;
        }

        teamManager.removeAllyRequest(receiverTeam);
        plugin.sendAllyPrefixed(player, plugin.msg("ally-denied").replace("<team>", req.senderTeam()));

        Team senderTeam = teamManager.getTeamByName(req.senderTeam());
        if (senderTeam != null) {
            teamManager.broadcastAllyMessage(senderTeam, plugin.msg("ally-denied-notify").replace("<team>", receiverTeam.getName()));
        }
    }

    private void handleBreak(Player player, Team team) {
        if (!team.hasAlly()) {
            plugin.sendAllyPrefixed(player, plugin.msg("no-ally"));
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
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>(List.of("accept", "deny", "break", "chat"));
            for (Team t : teamManager.getAllTeams()) {
                suggestions.add(t.getName());
            }
            return suggestions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("chat") || args[0].equalsIgnoreCase("c"))) {
            return List.of("on", "off").stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
