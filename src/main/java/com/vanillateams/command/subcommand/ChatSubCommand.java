package com.vanillateams.command.subcommand;

import com.vanillateams.VanillaTeams;
import com.vanillateams.manager.TeamManager;
import com.vanillateams.model.Team;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ChatSubCommand implements SubCommand {
    private final VanillaTeams plugin;
    private final TeamManager teamManager;

    public ChatSubCommand(VanillaTeams plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
    }

    @Override
    public String getName() {
        return "chat";
    }

    @Override
    public List<String> getAliases() {
        return List.of("c");
    }

    @Override
    public String getDescription() {
        return "Send a message to team chat or toggle team chat";
    }

    @Override
    public String getSyntax() {
        return "/team chat [on|off|message]";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Team team = teamManager.getPlayerTeam(player);

        if (team == null) {
            plugin.sendPrefixed(player, plugin.msg("not-in-team"));
            return;
        }

        if (args.length == 1) {
            boolean toggled = teamManager.toggleTeamChat(player.getUniqueId());
            plugin.sendPrefixed(player, toggled ? plugin.msg("chat-toggled-on") : plugin.msg("chat-toggled-off"));
            return;
        }

        if (args.length == 2) {
            String arg = args[1].toLowerCase();
            if (arg.equals("on") || arg.equals("enable") || arg.equals("true")) {
                teamManager.setTeamChatToggled(player.getUniqueId(), true);
                plugin.sendPrefixed(player, plugin.msg("chat-toggled-on"));
                return;
            }
            if (arg.equals("off") || arg.equals("disable") || arg.equals("false")) {
                teamManager.setTeamChatToggled(player.getUniqueId(), false);
                plugin.sendPrefixed(player, plugin.msg("chat-toggled-off"));
                return;
            }
        }

        String msg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        teamManager.sendTeamChat(team, player, msg);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2 && sender instanceof Player player && teamManager.getPlayerTeam(player.getUniqueId()) != null) {
            return List.of("on", "off").stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
