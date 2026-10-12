package com.vanillateams.listener;

import com.vanillateams.VanillaTeams;
import com.vanillateams.manager.TeamManager;
import com.vanillateams.model.Team;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TeamChatListener implements Listener {
    private final VanillaTeams plugin;
    private final TeamManager teamManager;
    private final Set<UUID> handledChat = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public TeamChatListener(VanillaTeams plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        if (teamManager.isTeamChatToggled(player.getUniqueId())) {
            Team team = teamManager.getPlayerTeam(player.getUniqueId());
            if (team == null) {
                teamManager.disableTeamChat(player.getUniqueId());
                return;
            }

            event.setCancelled(true);
            handledChat.add(player.getUniqueId());
            String message = PlainTextComponentSerializer.plainText().serialize(event.message());
            teamManager.sendTeamChat(team, player, message);
            return;
        }

        if (teamManager.isAllyChatToggled(player.getUniqueId())) {
            Team team = teamManager.getPlayerTeam(player.getUniqueId());
            if (team == null || !team.hasAlly()) {
                teamManager.disableAllyChat(player.getUniqueId());
                return;
            }

            event.setCancelled(true);
            handledChat.add(player.getUniqueId());
            String message = PlainTextComponentSerializer.plainText().serialize(event.message());
            teamManager.sendAllyChat(team, player, message);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLegacyChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (!teamManager.isTeamChatToggled(player.getUniqueId()) && !teamManager.isAllyChatToggled(player.getUniqueId())) {
            return;
        }

        event.setCancelled(true);

        if (handledChat.remove(player.getUniqueId())) {
            return;
        }

        if (teamManager.isTeamChatToggled(player.getUniqueId())) {
            Team team = teamManager.getPlayerTeam(player.getUniqueId());
            if (team == null) {
                teamManager.disableTeamChat(player.getUniqueId());
                return;
            }
            teamManager.sendTeamChat(team, player, event.getMessage());
        } else if (teamManager.isAllyChatToggled(player.getUniqueId())) {
            Team team = teamManager.getPlayerTeam(player.getUniqueId());
            if (team == null || !team.hasAlly()) {
                teamManager.disableAllyChat(player.getUniqueId());
                return;
            }
            teamManager.sendAllyChat(team, player, event.getMessage());
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
        handledChat.remove(event.getPlayer().getUniqueId());
    }
}
