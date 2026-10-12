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

public class AllyChatCommand implements CommandExecutor, TabCompleter {
    private final VanillaTeams plugin;
    private final TeamManager teamManager;

    public AllyChatCommand(VanillaTeams plugin, TeamManager teamManager) {
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

        if (!team.hasAlly()) {
            plugin.sendAllyPrefixed(player, plugin.msg("no-ally"));
            return true;
        }

        if (args.length == 0) {
            boolean toggled = teamManager.toggleAllyChat(player.getUniqueId());
            plugin.sendAllyPrefixed(player, toggled ? plugin.msg("ally-chat-toggled-on") : plugin.msg("ally-chat-toggled-off"));
            return true;
        }

        if (args.length == 1) {
            String arg = args[0].toLowerCase();
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

        String msg = String.join(" ", args);
        teamManager.sendAllyChat(team, player, msg);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("on", "off").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return Collections.emptyList();
    }
}
