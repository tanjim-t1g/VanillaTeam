package com.vanillateams.hook;

import com.vanillateams.VanillaTeams;
import com.vanillateams.manager.TeamManager;
import org.bukkit.Bukkit;

public class DiscordSRVHook {
    private static DiscordSRVListener listener;

    public static void register(VanillaTeams plugin, TeamManager teamManager) {
        if (Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) {
            try {
                listener = new DiscordSRVListener(teamManager);
                plugin.getLogger().info("Hooked into DiscordSRV to keep team and ally chat private.");
            } catch (Throwable t) {
                plugin.getLogger().warning("Could not hook into DiscordSRV: " + t.getMessage());
            }
        }
    }

    public static void unregister() {
        if (listener != null) {
            listener.unregister();
            listener = null;
        }
    }
}
