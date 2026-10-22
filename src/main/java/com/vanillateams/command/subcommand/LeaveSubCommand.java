package com.vanillateams.command.subcommand;

import com.vanillateams.VanillaTeams;
import com.vanillateams.manager.TeamManager;
import com.vanillateams.manager.VoteManager;
import com.vanillateams.model.Team;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveSubCommand implements SubCommand {
    private final VanillaTeams plugin;
    private final TeamManager teamManager;
    private final VoteManager voteManager;

    public LeaveSubCommand(VanillaTeams plugin, TeamManager teamManager, VoteManager voteManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
        this.voteManager = voteManager;
    }

    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public String getDescription() {
        return "Leave your current team";
    }

    @Override
    public String getSyntax() {
        return "/team leave";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Team team = teamManager.getPlayerTeam(player);

        if (team == null) {
            plugin.sendPrefixed(player, plugin.msg("not-in-team"));
            return;
        }

        if (team.isLeader(player.getUniqueId())) {
            if (team.getSize() == 1) {
                voteManager.cancelVote(team);
                String disbandMsg = plugin.prefix() + plugin.msg("team-disbanded").replace("<team>", team.getName());
                teamManager.broadcastTeamMessage(team, disbandMsg);
                teamManager.disbandTeam(team);
            } else {
                plugin.sendPrefixed(player, plugin.msg("leader-cannot-leave"));
            }
            return;
        }

        teamManager.removeMember(team, player.getUniqueId());
        plugin.sendPrefixed(player, plugin.msg("member-left").replace("<player>", player.getName()));
        teamManager.broadcastTeamMessage(team, plugin.prefix() + plugin.msg("member-left").replace("<player>", player.getName()));
    }
}
