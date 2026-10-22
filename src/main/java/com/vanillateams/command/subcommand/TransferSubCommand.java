package com.vanillateams.command.subcommand;

import com.vanillateams.VanillaTeams;
import com.vanillateams.manager.TeamManager;
import com.vanillateams.model.Team;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class TransferSubCommand implements SubCommand {
    private final VanillaTeams plugin;
    private final TeamManager teamManager;

    public TransferSubCommand(VanillaTeams plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
    }

    @Override
    public String getName() {
        return "transfer";
    }

    @Override
    public List<String> getAliases() {
        return List.of("leader");
    }

    @Override
    public String getDescription() {
        return "Transfer team leadership to another member";
    }

    @Override
    public String getSyntax() {
        return "/team transfer <player>";
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
            plugin.sendPrefixed(player, "&7Usage: &f" + getSyntax());
            return;
        }

        UUID targetId = team.findMemberByName(args[1]);
        if (targetId == null) {
            plugin.sendPrefixed(player, plugin.msg("not-in-your-team").replace("<player>", args[1]));
            return;
        }

        if (targetId.equals(player.getUniqueId())) {
            return;
        }

        String targetName = team.getMemberName(targetId);
        teamManager.transferLeader(team, targetId);
        teamManager.broadcastTeamMessage(team, plugin.prefix() + plugin.msg("leader-transferred").replace("<player>", targetName));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2 && sender instanceof Player p) {
            Team t = teamManager.getPlayerTeam(p.getUniqueId());
            if (t != null && t.isLeader(p.getUniqueId())) {
                return t.getMembers().entrySet().stream()
                        .filter(entry -> !entry.getKey().equals(p.getUniqueId()))
                        .map(Map.Entry::getValue)
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }
}
