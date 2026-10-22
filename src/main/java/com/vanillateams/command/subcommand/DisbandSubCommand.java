package com.vanillateams.command.subcommand;

import com.vanillateams.VanillaTeams;
import com.vanillateams.manager.TeamManager;
import com.vanillateams.manager.VoteManager;
import com.vanillateams.model.Team;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DisbandSubCommand implements SubCommand {
    private final VanillaTeams plugin;
    private final TeamManager teamManager;
    private final VoteManager voteManager;

    public DisbandSubCommand(VanillaTeams plugin, TeamManager teamManager, VoteManager voteManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
        this.voteManager = voteManager;
    }

    @Override
    public String getName() {
        return "disband";
    }

    @Override
    public String getDescription() {
        return "Disband your team completely";
    }

    @Override
    public String getSyntax() {
        return "/team disband [team]";
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length >= 2) {
            if (!sender.hasPermission("vanillateams.admin")) {
                plugin.sendPrefixed(sender, plugin.msg("no-permission"));
                return;
            }
            String targetName = args[1];
            Team target = teamManager.getTeamByName(targetName);
            if (target == null) {
                plugin.sendPrefixed(sender, plugin.msg("team-not-found").replace("<team>", targetName));
                return;
            }
            voteManager.cancelVote(target);
            String disbandMsg = plugin.prefix() + plugin.msg("team-disbanded").replace("<team>", target.getName());
            teamManager.broadcastTeamMessage(target, disbandMsg);
            teamManager.disbandTeam(target);
            if (!(sender instanceof Player p && target.isMember(p.getUniqueId()))) {
                plugin.sendPrefixed(sender, plugin.msg("team-disbanded").replace("<team>", target.getName()));
            }
            return;
        }

        if (!(sender instanceof Player player)) {
            plugin.sendPrefixed(sender, "&7Usage: &f/team disband <teamname>");
            return;
        }

        Team team = teamManager.getPlayerTeam(player);
        if (team == null) {
            plugin.sendPrefixed(player, plugin.msg("not-in-team"));
            return;
        }
        if (!team.isLeader(player.getUniqueId())) {
            plugin.sendPrefixed(player, plugin.msg("not-leader"));
            return;
        }

        voteManager.cancelVote(team);
        String disbandMsg = plugin.prefix() + plugin.msg("team-disbanded").replace("<team>", team.getName());
        teamManager.broadcastTeamMessage(team, disbandMsg);
        teamManager.disbandTeam(team);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2 && sender.hasPermission("vanillateams.admin")) {
            return teamManager.getAllTeams().stream()
                    .map(Team::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
