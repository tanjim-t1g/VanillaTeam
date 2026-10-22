package com.vanillateams.command.subcommand;

import com.vanillateams.VanillaTeams;
import com.vanillateams.manager.TeamManager;
import com.vanillateams.model.Team;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class DenySubCommand implements SubCommand {
    private final VanillaTeams plugin;
    private final TeamManager teamManager;

    public DenySubCommand(VanillaTeams plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
    }

    @Override
    public String getName() {
        return "deny";
    }

    @Override
    public String getDescription() {
        return "Decline a pending team invitation";
    }

    @Override
    public String getSyntax() {
        return "/team deny [team]";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        TeamManager.Invite invite = teamManager.getInvite(player.getUniqueId());

        if (invite == null) {
            plugin.sendPrefixed(player, plugin.msg("no-pending-invite"));
            return;
        }

        if (args.length >= 2 && !invite.teamName().equalsIgnoreCase(args[1])) {
            plugin.sendPrefixed(player, plugin.msg("no-pending-invite"));
            return;
        }

        teamManager.removeInvite(player.getUniqueId());
        plugin.sendPrefixed(player, plugin.msg("invite-denied").replace("<team>", invite.teamName()));

        Team team = teamManager.getTeamByName(invite.teamName());
        if (team != null) {
            Player leader = Bukkit.getPlayer(team.getLeader());
            if (leader != null && leader.isOnline()) {
                plugin.sendPrefixed(leader, plugin.msg("invite-denied-notify").replace("<player>", player.getName()));
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2 && sender instanceof Player player) {
            TeamManager.Invite invite = teamManager.getInvite(player.getUniqueId());
            if (invite != null && invite.teamName().toLowerCase().startsWith(args[1].toLowerCase())) {
                return List.of(invite.teamName());
            }
        }
        return Collections.emptyList();
    }
}
