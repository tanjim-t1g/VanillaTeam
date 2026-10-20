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
import java.util.Map;
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
            case "transfer", "leader" -> handleTransfer(player, args);
            case "ally" -> handleAlly(player, args);
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

    private void handleTransfer(Player player, String[] args) {
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
            plugin.sendPrefixed(player, "&7Usage: &f/team transfer <player>");
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

    private void handleAlly(Player player, String[] args) {
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
            if (team.hasAlly()) {
                plugin.sendPrefixed(player, "&7Usage: &f/team ally break");
            } else if (teamManager.getAllyRequest(team) != null) {
                plugin.sendPrefixed(player, "&7Usage: &f/team ally <team|accept|deny>");
            } else {
                plugin.sendPrefixed(player, "&7Usage: &f/team ally <team>");
            }
            return;
        }

        String sub = args[1].toLowerCase();
        switch (sub) {
            case "accept" -> handleAllyAccept(player, team);
            case "deny" -> handleAllyDeny(player, team);
            case "break", "remove", "unally" -> handleAllyBreak(player, team);
            default -> handleAllyRequest(player, team, args[1]);
        }
    }

    private void handleAllyRequest(Player player, Team senderTeam, String targetTeamName) {
        if (senderTeam.hasAlly()) {
            plugin.sendPrefixed(player, plugin.msg("already-have-ally").replace("<ally>", senderTeam.getAllyTeam()));
            return;
        }

        Team targetTeam = teamManager.getTeamByName(targetTeamName);
        if (targetTeam == null) {
            plugin.sendPrefixed(player, plugin.msg("team-not-found").replace("<team>", targetTeamName));
            return;
        }

        if (senderTeam.equals(targetTeam)) {
            plugin.sendPrefixed(player, plugin.msg("cannot-ally-self"));
            return;
        }

        if (targetTeam.hasAlly()) {
            plugin.sendPrefixed(player, plugin.msg("target-has-ally").replace("<team>", targetTeam.getName()));
            return;
        }

        teamManager.createAllyRequest(senderTeam, targetTeam);
        plugin.sendPrefixed(player, plugin.msg("ally-request-sent").replace("<team>", targetTeam.getName()));

        Component allyMsg = MessageUtil.parse(plugin.allyPrefix() + plugin.msg("ally-request-received").replace("<team>", senderTeam.getName()))
                .append(MessageUtil.acceptDenyButtons("/team ally accept", "/team ally deny"));
        for (UUID memberId : targetTeam.getMemberIds()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage(allyMsg);
            }
        }
    }

    private void handleAllyAccept(Player player, Team receiverTeam) {
        if (receiverTeam.hasAlly()) {
            plugin.sendPrefixed(player, plugin.msg("already-have-ally").replace("<ally>", receiverTeam.getAllyTeam()));
            return;
        }

        TeamManager.AllyRequest req = teamManager.getAllyRequest(receiverTeam);
        if (req == null) {
            plugin.sendPrefixed(player, plugin.msg("no-ally-request"));
            return;
        }

        Team senderTeam = teamManager.getTeamByName(req.senderTeam());
        if (senderTeam == null) {
            teamManager.removeAllyRequest(receiverTeam);
            plugin.sendPrefixed(player, plugin.msg("team-not-found").replace("<team>", req.senderTeam()));
            return;
        }

        if (senderTeam.hasAlly()) {
            teamManager.removeAllyRequest(receiverTeam);
            plugin.sendPrefixed(player, plugin.msg("target-has-ally").replace("<team>", senderTeam.getName()));
            return;
        }

        teamManager.setAlliance(senderTeam, receiverTeam);
        teamManager.broadcastAllyMessage(senderTeam, plugin.msg("ally-accepted").replace("<team>", receiverTeam.getName()));
        teamManager.broadcastAllyMessage(receiverTeam, plugin.msg("ally-accepted").replace("<team>", senderTeam.getName()));
    }

    private void handleAllyDeny(Player player, Team receiverTeam) {
        TeamManager.AllyRequest req = teamManager.getAllyRequest(receiverTeam);
        if (req == null) {
            plugin.sendPrefixed(player, plugin.msg("no-ally-request"));
            return;
        }

        teamManager.removeAllyRequest(receiverTeam);
        plugin.sendPrefixed(player, plugin.msg("ally-denied").replace("<team>", req.senderTeam()));

        Team senderTeam = teamManager.getTeamByName(req.senderTeam());
        if (senderTeam != null) {
            teamManager.broadcastAllyMessage(senderTeam, plugin.msg("ally-denied-notify").replace("<team>", receiverTeam.getName()));
        }
    }

    private void handleAllyBreak(Player player, Team team) {
        if (!team.hasAlly()) {
            plugin.sendPrefixed(player, plugin.msg("no-ally"));
            return;
        }

        String allyName = team.getAllyTeam();
        Team allyTeam = teamManager.getTeamByName(allyName);
        teamManager.breakAlliance(team, allyTeam);

        teamManager.broadcastAllyMessage(team, plugin.msg("ally-broken").replace("<team>", allyName));
        if (allyTeam != null) {
            teamManager.broadcastAllyMessage(allyTeam, plugin.msg("ally-broken").replace("<team>", team.getName()));
        }
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

        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessageUtil.parse(" &f/teams &8- &7List all teams"));
            sender.sendMessage(MessageUtil.parse(" &f/team info <team> &8- &7View team details"));
            sender.sendMessage(MessageUtil.parse(" &f/team disband <team> &8- &7Force disband a team"));
            sender.sendMessage(MessageUtil.parse(" &f/team reload &8- &7Reload configuration"));
            return;
        }

        Team team = teamManager.getPlayerTeam(player.getUniqueId());

        if (team == null) {
            sender.sendMessage(MessageUtil.parse(" &f/team create <name> &8- &7Create a team"));
            if (teamManager.getInvite(player.getUniqueId()) != null) {
                sender.sendMessage(MessageUtil.parse(" &f/team accept &8| &fdeny &8- &7Respond to invite"));
            }
            sender.sendMessage(MessageUtil.parse(" &f/team info [team] &8- &7View team details"));
            sender.sendMessage(MessageUtil.parse(" &f/teams &8- &7List all teams"));
        } else {
            boolean isLeader = team.isLeader(player.getUniqueId());

            if (isLeader) {
                sender.sendMessage(MessageUtil.parse(" &f/team invite <player> &8- &7Invite someone"));
                sender.sendMessage(MessageUtil.parse(" &f/team kick <player> &8- &7Kick a member"));
                sender.sendMessage(MessageUtil.parse(" &f/team transfer <player> &8- &7Transfer leadership"));
                if (team.hasAlly()) {
                    sender.sendMessage(MessageUtil.parse(" &f/team ally break &8- &7Break alliance"));
                } else {
                    if (teamManager.getAllyRequest(team) != null) {
                        sender.sendMessage(MessageUtil.parse(" &f/team ally accept &8| &fdeny &8- &7Respond to ally request"));
                    }
                    sender.sendMessage(MessageUtil.parse(" &f/team ally <team> &8- &7Send ally request"));
                }
            }

            if (voteManager.hasActiveVote(team)) {
                sender.sendMessage(MessageUtil.parse(" &f/team pvp [yes|no] &8- &7Vote on active PvP change"));
            } else {
                sender.sendMessage(MessageUtil.parse(" &f/team pvp [on|off] &8- &7Start a PvP vote"));
            }

            sender.sendMessage(MessageUtil.parse(" &f/tc [message] &8- &7Team chat"));
            if (team.hasAlly()) {
                sender.sendMessage(MessageUtil.parse(" &f/ac [message] &8- &7Ally chat"));
            }
            sender.sendMessage(MessageUtil.parse(" &f/team info [team] &8- &7View team details"));
            sender.sendMessage(MessageUtil.parse(" &f/teams &8- &7List all teams"));
            sender.sendMessage(MessageUtil.parse(" &f/team leave &8- &7Leave your team"));

            if (isLeader) {
                sender.sendMessage(MessageUtil.parse(" &f/team disband &8- &7Disband your team"));
            }
        }

        if (sender.hasPermission("vanillateams.admin")) {
            sender.sendMessage(MessageUtil.parse(" &f/team disband <team> &8- &7Force disband"));
            sender.sendMessage(MessageUtil.parse(" &f/team reload &8- &7Reload configuration"));
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>();

            if (!(sender instanceof Player player)) {
                subs.add("info");
                subs.add("list");
                subs.add("disband");
                subs.add("reload");
                return subs.stream()
                        .filter(s -> s.startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }

            Team team = teamManager.getPlayerTeam(player.getUniqueId());

            if (team == null) {
                subs.add("create");
                if (teamManager.getInvite(player.getUniqueId()) != null) {
                    subs.add("accept");
                    subs.add("deny");
                }
                subs.add("info");
                subs.add("list");
            } else {
                subs.add("leave");
                subs.add("info");
                subs.add("list");
                subs.add("chat");
                subs.add("pvp");

                if (team.isLeader(player.getUniqueId())) {
                    subs.add("invite");
                    subs.add("kick");
                    subs.add("transfer");
                    subs.add("ally");
                    subs.add("disband");
                }
            }

            if (sender.hasPermission("vanillateams.admin")) {
                if (!subs.contains("disband")) {
                    subs.add("disband");
                }
                subs.add("reload");
            }

            return subs.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            return switch (sub) {
                case "pvp" -> {
                    if (!(sender instanceof Player player)) yield Collections.emptyList();
                    Team team = teamManager.getPlayerTeam(player.getUniqueId());
                    if (team == null) yield Collections.emptyList();

                    if (voteManager.hasActiveVote(team)) {
                        VoteManager.PvpVote vote = voteManager.getActiveVote(team);
                        if (vote != null && vote.isEligible(player.getUniqueId()) && !vote.hasVoted(player.getUniqueId())) {
                            yield List.of("yes", "no").stream()
                                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                                    .collect(Collectors.toList());
                        }
                        yield Collections.emptyList();
                    } else {
                        yield List.of("on", "off").stream()
                                .filter(s -> s.startsWith(args[1].toLowerCase()))
                                .collect(Collectors.toList());
                    }
                }
                case "chat", "c" -> {
                    if (sender instanceof Player player && teamManager.getPlayerTeam(player.getUniqueId()) != null) {
                        yield List.of("on", "off").stream()
                                .filter(s -> s.startsWith(args[1].toLowerCase()))
                                .collect(Collectors.toList());
                    }
                    yield Collections.emptyList();
                }
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
                case "invite" -> {
                    if (sender instanceof Player p) {
                        Team t = teamManager.getPlayerTeam(p.getUniqueId());
                        if (t != null && t.isLeader(p.getUniqueId())) {
                            yield Bukkit.getOnlinePlayers().stream()
                                    .filter(target -> teamManager.getPlayerTeam(target.getUniqueId()) == null)
                                    .map(Player::getName)
                                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                                    .collect(Collectors.toList());
                        }
                    }
                    yield Collections.emptyList();
                }
                case "kick", "transfer", "leader" -> {
                    if (sender instanceof Player p) {
                        Team t = teamManager.getPlayerTeam(p.getUniqueId());
                        if (t != null && t.isLeader(p.getUniqueId())) {
                            yield t.getMembers().entrySet().stream()
                                    .filter(entry -> !entry.getKey().equals(p.getUniqueId()))
                                    .map(Map.Entry::getValue)
                                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                                    .collect(Collectors.toList());
                        }
                    }
                    yield Collections.emptyList();
                }
                case "accept", "deny" -> {
                    if (sender instanceof Player p) {
                        TeamManager.Invite invite = teamManager.getInvite(p.getUniqueId());
                        if (invite != null && invite.teamName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            yield List.of(invite.teamName());
                        }
                    }
                    yield Collections.emptyList();
                }
                case "ally" -> {
                    if (sender instanceof Player p) {
                        Team t = teamManager.getPlayerTeam(p.getUniqueId());
                        if (t != null && t.isLeader(p.getUniqueId())) {
                            if (t.hasAlly()) {
                                yield List.of("break").stream()
                                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                                        .collect(Collectors.toList());
                            } else {
                                List<String> suggestions = new ArrayList<>();
                                if (teamManager.getAllyRequest(t) != null) {
                                    suggestions.add("accept");
                                    suggestions.add("deny");
                                }
                                for (Team other : teamManager.getAllTeams()) {
                                    if (!other.getName().equalsIgnoreCase(t.getName()) && !other.hasAlly()) {
                                        suggestions.add(other.getName());
                                    }
                                }
                                yield suggestions.stream()
                                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                                        .collect(Collectors.toList());
                            }
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
