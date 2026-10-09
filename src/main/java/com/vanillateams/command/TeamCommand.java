package com.vanillateams.command;

import com.vanillateams.VanillaTeams;
import com.vanillateams.manager.TeamManager;
import com.vanillateams.manager.VoteManager;
import com.vanillateams.model.Team;
import com.vanillateams.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TeamCommand implements CommandExecutor, TabCompleter {
    private final VanillaTeams plugin;
    private final TeamManager teamManager;
    private final VoteManager voteManager;

    public TeamCommand(VanillaTeams plugin, TeamManager teamManager, VoteManager voteManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
        this.voteManager = voteManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "reload" -> {
                if (!sender.hasPermission("vanillateams.admin")) {
                    plugin.sendPrefixed(sender, plugin.msg("no-permission"));
                    return true;
                }
                plugin.reloadConfig();
                plugin.sendPrefixed(sender, plugin.msg("reloaded"));
                return true;
            }
            case "list", "teams" -> {
                sendTeamList(sender);
                return true;
            }
            case "info" -> {
                handleInfo(sender, args);
                return true;
            }
            case "disband" -> {
                handleDisband(sender, args);
                return true;
            }
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessageUtil.parse(plugin.msg("player-only")));
            return true;
        }

        switch (sub) {
            case "create" -> handleCreate(player, args);
            case "invite" -> handleInvite(player, args);
            case "accept", "join" -> handleAccept(player, args);
            case "deny" -> handleDeny(player, args);
            case "leave" -> handleLeave(player);
            case "kick" -> handleKick(player, args);
            case "leader", "transfer" -> handleLeader(player, args);
            case "pvp" -> handlePvp(player, args);
            case "chat", "c" -> handleChat(player, args);
            default -> sendHelp(sender);
        }
        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (teamManager.getPlayerTeam(player) != null) {
            plugin.sendPrefixed(player, plugin.msg("already-in-team"));
            return;
        }
        if (args.length < 2) {
            plugin.sendPrefixed(player, "&7Usage: &f/team create <name>");
            return;
        }

        String name = args[1];
        int min = plugin.getConfig().getInt("settings.min-name-length", 2);
        int max = plugin.getConfig().getInt("settings.max-name-length", 16);

        if (name.length() < min || name.length() > max || !name.matches("^[a-zA-Z0-9_]+$")) {
            plugin.sendPrefixed(player, plugin.msg("invalid-name")
                    .replace("<min>", String.valueOf(min))
                    .replace("<max>", String.valueOf(max)));
            return;
        }

        if (teamManager.getTeamByName(name) != null) {
            plugin.sendPrefixed(player, plugin.msg("team-exists").replace("<team>", name));
            return;
        }

        Team team = teamManager.createTeam(name, player);
        plugin.sendPrefixed(player, plugin.msg("team-created").replace("<team>", team.getName()));
    }

    private void handleDisband(CommandSender sender, String[] args) {
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

    private void handleInvite(Player player, String[] args) {
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
            plugin.sendPrefixed(player, "&7Usage: &f/team invite <player>");
            return;
        }

        int maxMembers = plugin.getConfig().getInt("settings.max-members", 10);
        if (team.getSize() >= maxMembers) {
            plugin.sendPrefixed(player, plugin.msg("team-full").replace("<max>", String.valueOf(maxMembers)));
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null || !target.isOnline()) {
            plugin.sendPrefixed(player, plugin.msg("player-not-found").replace("<player>", args[1]));
            return;
        }

        if (teamManager.getPlayerTeam(target) != null) {
            plugin.sendPrefixed(player, plugin.msg("target-in-team").replace("<player>", target.getName()));
            return;
        }

        TeamManager.Invite existing = teamManager.getInvite(target.getUniqueId());
        if (existing != null && existing.teamName().equalsIgnoreCase(team.getName())) {
            plugin.sendPrefixed(player, plugin.msg("already-invited").replace("<player>", target.getName()));
            return;
        }

        teamManager.createInvite(target.getUniqueId(), team.getName());
        plugin.sendPrefixed(player, plugin.msg("invite-sent").replace("<player>", target.getName()));

        Component inviteMsg = MessageUtil.parse(plugin.prefix() + plugin.msg("invite-received").replace("<team>", team.getName()))
                .append(MessageUtil.acceptDenyButtons("/team accept " + team.getName(), "/team deny " + team.getName()));
        target.sendMessage(inviteMsg);
    }

    private void handleAccept(Player player, String[] args) {
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

    private void handleDeny(Player player, String[] args) {
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

    private void handleLeave(Player player) {
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

    private void handleKick(Player player, String[] args) {
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
            plugin.sendPrefixed(player, "&7Usage: &f/team kick <player>");
            return;
        }

        UUID targetId = team.findMemberByName(args[1]);
        if (targetId == null) {
            plugin.sendPrefixed(player, plugin.msg("not-in-your-team").replace("<player>", args[1]));
            return;
        }
        if (targetId.equals(player.getUniqueId())) {
            plugin.sendPrefixed(player, plugin.msg("cannot-kick-self"));
            return;
        }

        String targetName = team.getMemberName(targetId);
        teamManager.removeMember(team, targetId);
        teamManager.broadcastTeamMessage(team, plugin.prefix() + plugin.msg("member-kicked").replace("<player>", targetName));

        Player targetOnline = Bukkit.getPlayer(targetId);
        if (targetOnline != null && targetOnline.isOnline()) {
            plugin.sendPrefixed(targetOnline, plugin.msg("kicked-notify").replace("<team>", team.getName()));
        }
    }

    private void handleLeader(Player player, String[] args) {
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
            plugin.sendPrefixed(player, "&7Usage: &f/team leader <player>");
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

    private void handlePvp(Player player, String[] args) {
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
            plugin.sendPrefixed(player, "&7Usage: &f/team pvp [on|off|yes|no]");
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

    private void handleChat(Player player, String[] args) {
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

        String msg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        teamManager.sendTeamChat(team, player, msg);
    }

    private void handleInfo(CommandSender sender, String[] args) {
        Team team = null;
        if (args.length < 2) {
            if (sender instanceof Player player) {
                team = teamManager.getPlayerTeam(player);
                if (team == null) {
                    plugin.sendPrefixed(player, plugin.msg("not-in-team"));
                    return;
                }
            } else {
                plugin.sendPrefixed(sender, "&7Usage: &f/team info <team>");
                return;
            }
        } else {
            String query = args[1];
            team = teamManager.getTeamByName(query);
            if (team == null) {
                Player targetPlayer = Bukkit.getPlayerExact(query);
                if (targetPlayer != null) {
                    team = teamManager.getPlayerTeam(targetPlayer);
                }
            }
            if (team == null) {
                plugin.sendPrefixed(sender, plugin.msg("team-not-found").replace("<team>", query));
                return;
            }
        }

        String pvpState = team.isPvpEnabled() ? "&aON" : "&cOFF";
        String allyName = team.hasAlly() ? "&f" + team.getAllyTeam() : "&7None";
        List<String> memberNames = new ArrayList<>();
        for (var entry : team.getMembers().entrySet()) {
            if (!entry.getKey().equals(team.getLeader())) {
                memberNames.add(entry.getValue());
            }
        }
        String membersStr = memberNames.isEmpty() ? "None" : String.join(", ", memberNames);

        sender.sendMessage(MessageUtil.parse("&8[&7Team&8] &f" + team.getName()));
        sender.sendMessage(MessageUtil.parse(" &7Leader: &f" + team.getLeaderName()));
        sender.sendMessage(MessageUtil.parse(" &7Members: &f" + membersStr));
        sender.sendMessage(MessageUtil.parse(" &7Team PvP: " + pvpState + " &8| &7Ally: " + allyName));
    }

    public void sendTeamList(CommandSender sender) {
        Collection<Team> all = teamManager.getAllTeams();
        if (all.isEmpty()) {
            plugin.sendPrefixed(sender, "&7No teams yet.");
            return;
        }

        sender.sendMessage(MessageUtil.parse("&8[&7Teams&8] &7Total: &f" + all.size()));
        for (Team t : all) {
            String allyInfo = t.hasAlly() ? " &8(&7Ally: &f" + t.getAllyTeam() + "&8)" : "";
            sender.sendMessage(MessageUtil.parse(" &8- &f" + t.getName()
                    + " &7(" + t.getSize() + ") &8| &7Leader: &f" + t.getLeaderName() + allyInfo));
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(MessageUtil.parse("&8[&7Team Commands&8]"));
        sender.sendMessage(MessageUtil.parse(" &f/team create <name> &8- &7Create a team"));
        sender.sendMessage(MessageUtil.parse(" &f/team invite <player> &8- &7Invite someone"));
        sender.sendMessage(MessageUtil.parse(" &f/team accept &8| &fdeny &8- &7Respond to invite"));
        sender.sendMessage(MessageUtil.parse(" &f/team leave &8- &7Leave your team"));
        sender.sendMessage(MessageUtil.parse(" &f/team kick <player> &8- &7Kick a member"));
        sender.sendMessage(MessageUtil.parse(" &f/team leader <player> &8- &7Transfer leadership"));
        sender.sendMessage(MessageUtil.parse(" &f/team pvp [on|off] &8- &7Start a PvP vote"));
        sender.sendMessage(MessageUtil.parse(" &f/team pvp [yes|no] &8- &7Vote on PvP change"));
        sender.sendMessage(MessageUtil.parse(" &f/team info [team] &8- &7View team details"));
        sender.sendMessage(MessageUtil.parse(" &f/teams &8- &7List all teams"));
        sender.sendMessage(MessageUtil.parse(" &f/tc [message] &8- &7Team chat"));
        sender.sendMessage(MessageUtil.parse(" &f/ally <team|accept|deny|break> &8- &7Manage ally"));
        if (sender.hasPermission("vanillateams.admin")) {
            sender.sendMessage(MessageUtil.parse(" &f/team disband <team> &8- &7Force disband"));
        } else {
            sender.sendMessage(MessageUtil.parse(" &f/team disband &8- &7Disband your team"));
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>(List.of(
                    "create", "invite", "accept", "deny", "leave",
                    "kick", "leader", "pvp", "info", "list", "chat", "disband"
            ));
            if (sender.hasPermission("vanillateams.admin")) {
                subs.add("reload");
            }
            return subs.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            return switch (sub) {
                case "pvp" -> List.of("on", "off", "yes", "no").stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                case "info" -> teamManager.getAllTeams().stream()
                        .map(Team::getName)
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                case "disband" -> sender.hasPermission("vanillateams.admin")
                        ? teamManager.getAllTeams().stream()
                            .map(Team::getName)
                            .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList())
                        : Collections.emptyList();
                case "invite" -> Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                case "kick", "leader", "transfer" -> {
                    if (sender instanceof Player p) {
                        Team t = teamManager.getPlayerTeam(p);
                        if (t != null) {
                            yield t.getMembers().values().stream()
                                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                                    .collect(Collectors.toList());
                        }
                    }
                    yield Collections.emptyList();
                }
                default -> Collections.emptyList();
            };
        }
        return Collections.emptyList();
    }
}
