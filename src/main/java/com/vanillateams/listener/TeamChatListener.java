package com.vanillateams.listener;

import com.vanillateams.VanillaTeams;
import com.vanillateams.manager.TeamManager;
import com.vanillateams.model.Team;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class TeamChatListener implements Listener {
    private final VanillaTeams plugin;
    private final TeamManager teamManager;

    public TeamChatListener(VanillaTeams plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        if (teamManager.isTeamChatToggled(player.getUniqueId())) {
            Team team = teamManager.getPlayerTeam(player.getUniqueId());
            if (team == null) {
                teamManager.disableTeamChat(player.getUniqueId());
                return;
            }

            event.setCancelled(true);
            String plainMsg = PlainTextComponentSerializer.plainText().serialize(event.message());
            Bukkit.getScheduler().runTask(plugin, () -> teamManager.sendTeamChat(team, player, plainMsg));
            return;
        }

        if (teamManager.isAllyChatToggled(player.getUniqueId())) {
            Team team = teamManager.getPlayerTeam(player.getUniqueId());
            if (team == null || !team.hasAlly()) {
                teamManager.disableAllyChat(player.getUniqueId());
                return;
            }

            event.setCancelled(true);
            String plainMsg = PlainTextComponentSerializer.plainText().serialize(event.message());
            Bukkit.getScheduler().runTask(plugin, () -> teamManager.sendAllyChat(team, player, plainMsg));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLegacyChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (teamManager.isTeamChatToggled(player.getUniqueId()) || teamManager.isAllyChatToggled(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        teamManager.updatePlayerNameOnJoin(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        teamManager.disableTeamChat(event.getPlayer().getUniqueId());
        teamManager.disableAllyChat(event.getPlayer().getUniqueId());
    }
}
