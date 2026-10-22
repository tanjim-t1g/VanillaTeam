package com.vanillateams.command.subcommand;

import com.vanillateams.VanillaTeams;
import com.vanillateams.manager.TeamManager;
import com.vanillateams.model.Team;
import com.vanillateams.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InviteSubCommand implements SubCommand {
    private final VanillaTeams plugin;
    private final TeamManager teamManager;

    public InviteSubCommand(VanillaTeams plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
    }

    @Override
    public String getName() {
        return "invite";
    }

    @Override
    public String getDescription() {
        return "Invite a player to your team";
    }

    @Override
    public String getSyntax() {
        return "/team invite <player>";
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

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2 && sender instanceof Player player) {
            Team team = teamManager.getPlayerTeam(player.getUniqueId());
            if (team != null && team.isLeader(player.getUniqueId())) {
                return Bukkit.getOnlinePlayers().stream()
                        .filter(target -> teamManager.getPlayerTeam(target.getUniqueId()) == null)
                        .map(Player::getName)
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }
}
