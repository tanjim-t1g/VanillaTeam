package com.vanillateams.command.subcommand;

import com.vanillateams.VanillaTeams;
import com.vanillateams.manager.TeamManager;
import com.vanillateams.model.Team;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class AcceptSubCommand implements SubCommand {
    private final VanillaTeams plugin;
    private final TeamManager teamManager;

    public AcceptSubCommand(VanillaTeams plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
    }

    @Override
    public String getName() {
        return "accept";
    }

    @Override
    public List<String> getAliases() {
        return List.of("join");
    }

    @Override
    public String getDescription() {
        return "Accept a pending team invitation";
    }

    @Override
    public String getSyntax() {
        return "/team accept [team]";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (teamManager.getPlayerTeam(player) != null) {
            plugin.sendPrefixed(player, plugin.msg("already-in-team"));
            return;
        }

        TeamManager.Invite invite = teamManager.getInvite(player.getUniqueId());
        if (invite == null) {
            plugin.sendPrefixed(player, plugin.msg("no-pending-invite"));
            return;
        }

        if (args.length >= 2 && !invite.teamName().equalsIgnoreCase(args[1])) {
            plugin.sendPrefixed(player, plugin.msg("no-pending-invite"));
            return;
        }

        Team team = teamManager.getTeamByName(invite.teamName());
        if (team == null) {
            teamManager.removeInvite(player.getUniqueId());
            plugin.sendPrefixed(player, plugin.msg("team-not-found").replace("<team>", invite.teamName()));
            return;
        }

        int maxMembers = plugin.getConfig().getInt("settings.max-members", 10);
        if (team.getSize() >= maxMembers) {
            plugin.sendPrefixed(player, plugin.msg("team-full").replace("<max>", String.valueOf(maxMembers)));
            return;
        }

        teamManager.addMember(team, player);
        teamManager.broadcastTeamMessage(team, plugin.prefix() + plugin.msg("member-joined").replace("<player>", player.getName()));
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
