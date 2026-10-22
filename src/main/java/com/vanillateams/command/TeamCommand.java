package com.vanillateams.command;

import com.vanillateams.VanillaTeams;
import com.vanillateams.command.subcommand.AcceptSubCommand;
import com.vanillateams.command.subcommand.AllySubCommand;
import com.vanillateams.command.subcommand.ChatSubCommand;
import com.vanillateams.command.subcommand.CreateSubCommand;
import com.vanillateams.command.subcommand.DenySubCommand;
import com.vanillateams.command.subcommand.DisbandSubCommand;
import com.vanillateams.command.subcommand.InfoSubCommand;
import com.vanillateams.command.subcommand.InviteSubCommand;
import com.vanillateams.command.subcommand.KickSubCommand;
import com.vanillateams.command.subcommand.LeaveSubCommand;
import com.vanillateams.command.subcommand.ListSubCommand;
import com.vanillateams.command.subcommand.PvpSubCommand;
import com.vanillateams.command.subcommand.ReloadSubCommand;
import com.vanillateams.command.subcommand.SubCommand;
import com.vanillateams.command.subcommand.TransferSubCommand;
import com.vanillateams.manager.TeamManager;
import com.vanillateams.manager.VoteManager;
import com.vanillateams.model.Team;
import com.vanillateams.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TeamCommand implements CommandExecutor, TabCompleter {
    private final VanillaTeams plugin;
    private final TeamManager teamManager;
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public TeamCommand(VanillaTeams plugin, TeamManager teamManager, VoteManager voteManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;

        registerSubCommand(new CreateSubCommand(plugin, teamManager));
        registerSubCommand(new InviteSubCommand(plugin, teamManager));
        registerSubCommand(new AcceptSubCommand(plugin, teamManager));
        registerSubCommand(new DenySubCommand(plugin, teamManager));
        registerSubCommand(new LeaveSubCommand(plugin, teamManager, voteManager));
        registerSubCommand(new KickSubCommand(plugin, teamManager));
        registerSubCommand(new TransferSubCommand(plugin, teamManager));
        registerSubCommand(new AllySubCommand(plugin, teamManager));
        registerSubCommand(new PvpSubCommand(plugin, teamManager, voteManager));
        registerSubCommand(new ChatSubCommand(plugin, teamManager));
        registerSubCommand(new InfoSubCommand(plugin, teamManager));
        registerSubCommand(new ListSubCommand(this));
        registerSubCommand(new DisbandSubCommand(plugin, teamManager, voteManager));
        registerSubCommand(new ReloadSubCommand(plugin));
    }

    private void registerSubCommand(SubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
        for (String alias : subCommand.getAliases()) {
            subCommands.put(alias.toLowerCase(), subCommand);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        SubCommand sub = subCommands.get(args[0].toLowerCase());
        if (sub == null) {
            sendHelp(sender);
            return true;
        }

        if (sub.getPermission() != null && !sender.hasPermission(sub.getPermission())) {
            plugin.sendPrefixed(sender, plugin.msg("no-permission"));
            return true;
        }

        if (sub.isPlayerOnly() && !(sender instanceof Player)) {
            sender.sendMessage(MessageUtil.parse(plugin.msg("player-only")));
            return true;
        }

        sub.execute(sender, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();

            if (!(sender instanceof Player player)) {
                completions.add("reload");
                completions.add("info");
                completions.add("disband");
                completions.add("list");
                return completions.stream()
                        .filter(s -> s.startsWith(input))
                        .collect(Collectors.toList());
            }

            Team team = teamManager.getPlayerTeam(player.getUniqueId());

            if (team == null) {
                completions.add("create");
                completions.add("info");
                completions.add("list");
                if (teamManager.getInvite(player.getUniqueId()) != null) {
                    completions.add("accept");
                    completions.add("deny");
                }
            } else {
                completions.add("info");
                completions.add("leave");
                completions.add("pvp");
                completions.add("chat");
                completions.add("list");

                if (team.isLeader(player.getUniqueId())) {
                    completions.add("invite");
                    completions.add("kick");
                    completions.add("transfer");
                    completions.add("ally");
                    completions.add("disband");
                }
            }

            if (sender.hasPermission("vanillateams.admin")) {
                if (!completions.contains("disband")) completions.add("disband");
                completions.add("reload");
            }

            return completions.stream()
                    .filter(s -> s.startsWith(input))
                    .collect(Collectors.toList());
        }

        if (args.length >= 2) {
            SubCommand sub = subCommands.get(args[0].toLowerCase());
            if (sub != null) {
                return sub.tabComplete(sender, args);
            }
        }

        return Collections.emptyList();
    }

    public void sendHelp(CommandSender sender) {
        sender.sendMessage(MessageUtil.parse("&8&m----------------&r &8[&7VanillaTeams&8] &8&m----------------"));
        sender.sendMessage(MessageUtil.parse("&f/team create <name> &8- &7Create a new team"));
        sender.sendMessage(MessageUtil.parse("&f/team invite <player> &8- &7Invite a player to your team"));
        sender.sendMessage(MessageUtil.parse("&f/team accept [team] &8- &7Accept a team invite"));
        sender.sendMessage(MessageUtil.parse("&f/team deny [team] &8- &7Decline a team invite"));
        sender.sendMessage(MessageUtil.parse("&f/team leave &8- &7Leave your current team"));
        sender.sendMessage(MessageUtil.parse("&f/team kick <player> &8- &7Kick a member (Leader only)"));
        sender.sendMessage(MessageUtil.parse("&f/team transfer <player> &8- &7Transfer leadership (Leader only)"));
        sender.sendMessage(MessageUtil.parse("&f/team ally <team|accept|deny|break> &8- &7Manage team alliance"));
        sender.sendMessage(MessageUtil.parse("&f/team pvp [on|off|yes|no] &8- &7Vote on friendly fire"));
        sender.sendMessage(MessageUtil.parse("&f/team chat [on|off|msg] &8- &7Team chat toggle or message"));
        sender.sendMessage(MessageUtil.parse("&f/team info [team] &8- &7View team information"));
        sender.sendMessage(MessageUtil.parse("&f/team list &8- &7List all teams on the server"));
        sender.sendMessage(MessageUtil.parse("&f/team disband &8- &7Disband your team (Leader only)"));
        if (sender.hasPermission("vanillateams.admin")) {
            sender.sendMessage(MessageUtil.parse("&f/team disband <team> &8- &7Force-disband any team (Admin)"));
            sender.sendMessage(MessageUtil.parse("&f/team reload &8- &7Reload configuration (Admin)"));
        }
        sender.sendMessage(MessageUtil.parse("&8&m--------------------------------------------------"));
    }

    public void sendTeamList(CommandSender sender) {
        Collection<Team> teams = teamManager.getAllTeams();
        if (teams.isEmpty()) {
            plugin.sendPrefixed(sender, "&7There are currently no active teams.");
            return;
        }

        sender.sendMessage(MessageUtil.parse("&8&m----------------&r &8[&7Active Teams &8(&f" + teams.size() + "&8)] &8&m----------------"));
        for (Team team : teams) {
            String allyInfo = team.hasAlly() ? " &8[&7Ally: &f" + team.getAllyTeam() + "&8]" : "";
            sender.sendMessage(MessageUtil.parse(" &8• &f" + team.getName() + " &8- &7Leader: &f" + team.getLeaderName()
                    + " &8(&f" + team.getSize() + " &7members&8)" + allyInfo));
        }
        sender.sendMessage(MessageUtil.parse("&8&m--------------------------------------------------"));
    }
}
