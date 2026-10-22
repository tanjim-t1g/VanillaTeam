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

public class PvpSubCommand implements SubCommand {
    private final VanillaTeams plugin;
    private final TeamManager teamManager;
    private final VoteManager voteManager;

    public PvpSubCommand(VanillaTeams plugin, TeamManager teamManager, VoteManager voteManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
        this.voteManager = voteManager;
    }

    @Override
    public String getName() {
        return "pvp";
    }

    @Override
    public String getDescription() {
        return "Vote to enable or disable friendly fire";
    }

    @Override
    public String getSyntax() {
        return "/team pvp [on|off|yes|no]";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Team team = teamManager.getPlayerTeam(player);

        if (team == null) {
            plugin.sendPrefixed(player, plugin.msg("not-in-team"));
            return;
        }

        if (args.length < 2) {
            String state = team.isPvpEnabled() ? "&aON" : "&cOFF";
            plugin.sendPrefixed(player, plugin.msg("pvp-status").replace("<state>", state));
            return;
        }

        String arg = args[1].toLowerCase();
        if (arg.equals("yes") || arg.equals("y")) {
            handleVoteCast(player, team, true);
            return;
        }
        if (arg.equals("no") || arg.equals("n")) {
            handleVoteCast(player, team, false);
            return;
        }

        boolean proposedState;
        if (arg.equals("on") || arg.equals("true") || arg.equals("enable")) {
            proposedState = true;
        } else if (arg.equals("off") || arg.equals("false") || arg.equals("disable")) {
            proposedState = false;
        } else {
            plugin.sendPrefixed(player, "&7Usage: &f" + getSyntax());
            return;
        }

        if (team.isPvpEnabled() == proposedState) {
            String state = proposedState ? "&aON" : "&cOFF";
            plugin.sendPrefixed(player, plugin.msg("pvp-already-set").replace("<state>", state));
            return;
        }

        if (voteManager.hasActiveVote(team)) {
            plugin.sendPrefixed(player, plugin.msg("pvp-vote-already-active"));
            return;
        }

        voteManager.startVote(team, player, proposedState);
    }

    private void handleVoteCast(Player player, Team team, boolean yes) {
        if (!voteManager.hasActiveVote(team)) {
            plugin.sendPrefixed(player, plugin.msg("pvp-no-active-vote"));
            return;
        }

        VoteManager.PvpVote vote = voteManager.getActiveVote(team);
        if (!vote.isEligible(player.getUniqueId())) {
            plugin.sendPrefixed(player, plugin.msg("pvp-not-eligible"));
            return;
        }

        if (vote.hasVoted(player.getUniqueId())) {
            plugin.sendPrefixed(player, plugin.msg("pvp-already-voted"));
            return;
        }

        voteManager.castVote(team, player, yes);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2 && sender instanceof Player player && teamManager.getPlayerTeam(player.getUniqueId()) != null) {
            return List.of("on", "off", "yes", "no").stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
