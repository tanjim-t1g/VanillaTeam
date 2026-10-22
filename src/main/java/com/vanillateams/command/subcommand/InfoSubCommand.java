package com.vanillateams.command.subcommand;

import com.vanillateams.VanillaTeams;
import com.vanillateams.manager.TeamManager;
import com.vanillateams.model.Team;
import com.vanillateams.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InfoSubCommand implements SubCommand {
    private final VanillaTeams plugin;
    private final TeamManager teamManager;

    public InfoSubCommand(VanillaTeams plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getDescription() {
        return "View team details and member list";
    }

    @Override
    public String getSyntax() {
        return "/team info [team]";
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Team team = null;
        if (args.length < 2) {
            if (sender instanceof Player player) {
                team = teamManager.getPlayerTeam(player);
                if (team == null) {
                    plugin.sendPrefixed(player, plugin.msg("not-in-team"));
                    return;
                }
            } else {
                plugin.sendPrefixed(sender, "&7Usage: &f/team info <team>");
                return;
            }
        } else {
            String query = args[1];
            team = teamManager.getTeamByName(query);
            if (team == null) {
                Player targetPlayer = Bukkit.getPlayerExact(query);
                if (targetPlayer != null) {
                    team = teamManager.getPlayerTeam(targetPlayer);
                }
            }
            if (team == null) {
                plugin.sendPrefixed(sender, plugin.msg("team-not-found").replace("<team>", query));
                return;
            }
        }

        String pvpState = team.isPvpEnabled() ? "&aON" : "&cOFF";
        String allyName = team.hasAlly() ? "&f" + team.getAllyTeam() : "&7None";
        List<String> memberNames = new ArrayList<>();
        for (var entry : team.getMembers().entrySet()) {
            if (!entry.getKey().equals(team.getLeader())) {
                memberNames.add(entry.getValue());
            }
        }
        String membersStr = memberNames.isEmpty() ? "None" : String.join(", ", memberNames);

        sender.sendMessage(MessageUtil.parse("&8[&7Team&8] &f" + team.getName()));
        sender.sendMessage(MessageUtil.parse(" &7Leader: &f" + team.getLeaderName()));
        sender.sendMessage(MessageUtil.parse(" &7Members: &f" + membersStr));
        sender.sendMessage(MessageUtil.parse(" &7Ally: " + allyName));
        sender.sendMessage(MessageUtil.parse(" &7Team PvP: " + pvpState));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return teamManager.getAllTeams().stream()
                    .map(Team::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
