package com.vanillateams.command;

import com.vanillateams.VanillaTeams;
import com.vanillateams.manager.TeamManager;
import com.vanillateams.model.Team;
import com.vanillateams.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class TeamChatCommand implements CommandExecutor, TabCompleter {
    private final VanillaTeams plugin;
    private final TeamManager teamManager;

    public TeamChatCommand(VanillaTeams plugin, TeamManager teamManager) {
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
            plugin.sendPrefixed(player, plugin.msg("not-in-team"));
            return true;
        }

        if (args.length == 0) {
            boolean toggled = teamManager.toggleTeamChat(player.getUniqueId());
            plugin.sendPrefixed(player, toggled ? plugin.msg("chat-toggled-on") : plugin.msg("chat-toggled-off"));
            return true;
        }

        if (args.length == 1) {
            String arg = args[0].toLowerCase();
            if (arg.equals("on") || arg.equals("enable") || arg.equals("true")) {
                teamManager.setTeamChatToggled(player.getUniqueId(), true);
                plugin.sendPrefixed(player, plugin.msg("chat-toggled-on"));
                return true;
            }
            if (arg.equals("off") || arg.equals("disable") || arg.equals("false")) {
                teamManager.setTeamChatToggled(player.getUniqueId(), false);
                plugin.sendPrefixed(player, plugin.msg("chat-toggled-off"));
                return true;
            }
        }

        String msg = String.join(" ", args);
        teamManager.sendTeamChat(team, player, msg);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }
        Team team = teamManager.getPlayerTeam(player.getUniqueId());
        if (team == null) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            return List.of("on", "off").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return Collections.emptyList();
    }
}
