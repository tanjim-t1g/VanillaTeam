package com.vanillateams.listener;

import com.vanillateams.VanillaTeams;
import com.vanillateams.manager.TeamManager;
import com.vanillateams.model.Team;
import com.vanillateams.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class TeamCombatListener implements Listener {
    private final VanillaTeams plugin;
    private final TeamManager teamManager;

    public TeamCombatListener(VanillaTeams plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
    }

    /**
     * Intercepts entity damage at LOWEST priority to cancel friendly-fire
     * before combat taggers, damage-indicators, or logging plugins process the hit.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        // Resolve direct attacks or projectile shooters (arrows, tridents, splash potions)
        Player attacker = null;
        if (event.getDamager() instanceof Player direct) {
            attacker = direct;
        } else if (event.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player shooter) {
            attacker = shooter;
        }

        if (attacker == null || attacker.equals(victim)) return;

        Team attackerTeam = teamManager.getPlayerTeam(attacker.getUniqueId());
        if (attackerTeam == null) return;

        Team victimTeam = teamManager.getPlayerTeam(victim.getUniqueId());
        if (!attackerTeam.equals(victimTeam)) return;

        // If PvP is disabled within the team, block damage and alert the attacker
        if (!attackerTeam.isPvpEnabled()) {
            event.setCancelled(true);
            String msg = plugin.msg("friendly-fire-blocked");
            if (!msg.isEmpty()) {
                attacker.sendActionBar(MessageUtil.parse(msg));
            }
        }
    }
}
