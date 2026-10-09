package com.vanillateams.hook;

import com.vanillateams.manager.TeamManager;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.GameChatMessagePostProcessEvent;
import github.scarsz.discordsrv.api.events.GameChatMessagePreProcessEvent;
import org.bukkit.entity.Player;

public class DiscordSRVListener {
    private final TeamManager teamManager;

    public DiscordSRVListener(TeamManager teamManager) {
        this.teamManager = teamManager;
        try {
            DiscordSRV.api.subscribe(this);
        } catch (Throwable ignored) {
        }
    }

    public void unregister() {
        try {
            DiscordSRV.api.unsubscribe(this);
        } catch (Throwable ignored) {
        }
    }

    @Subscribe
    public void onGameChatMessagePreProcess(GameChatMessagePreProcessEvent event) {
        Player player = event.getPlayer();
        if (player != null && (teamManager.isTeamChatToggled(player.getUniqueId()) || teamManager.isAllyChatToggled(player.getUniqueId()))) {
            event.setCancelled(true);
        }
    }

    @Subscribe
    public void onGameChatMessagePostProcess(GameChatMessagePostProcessEvent event) {
        Player player = event.getPlayer();
        if (player != null && (teamManager.isTeamChatToggled(player.getUniqueId()) || teamManager.isAllyChatToggled(player.getUniqueId()))) {
            event.setCancelled(true);
        }
    }
}
