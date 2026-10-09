package com.vanillateams;

import com.vanillateams.command.AllyChatCommand;
import com.vanillateams.command.AllyCommand;
import com.vanillateams.command.TeamChatCommand;
import com.vanillateams.command.TeamCommand;
import com.vanillateams.command.TeamsCommand;
import com.vanillateams.data.DatabaseManager;
import com.vanillateams.hook.DiscordSRVHook;
import com.vanillateams.listener.TeamChatListener;
import com.vanillateams.listener.TeamCombatListener;
import com.vanillateams.manager.TeamManager;
import com.vanillateams.manager.VoteManager;
import com.vanillateams.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class VanillaTeams extends JavaPlugin {
    private DatabaseManager databaseManager;
    private TeamManager teamManager;
    private VoteManager voteManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        databaseManager = new DatabaseManager(this);
        databaseManager.init();

        teamManager = new TeamManager(this, databaseManager);
        teamManager.loadTeams();

        voteManager = new VoteManager(this, teamManager);

        var pm = getServer().getPluginManager();
        pm.registerEvents(new TeamCombatListener(this, teamManager), this);
        pm.registerEvents(new TeamChatListener(this, teamManager), this);

        DiscordSRVHook.register(this, teamManager);

        var teamCmd = new TeamCommand(this, teamManager, voteManager);
        var team = getCommand("team");
        if (team != null) {
            team.setExecutor(teamCmd);
            team.setTabCompleter(teamCmd);
        }

        var teams = getCommand("teams");
        if (teams != null) {
            teams.setExecutor(new TeamsCommand(teamCmd));
        }

        var tc = getCommand("tc");
        if (tc != null) {
            tc.setExecutor(new TeamChatCommand(this, teamManager));
        }

        var ac = getCommand("ac");
        if (ac != null) {
            ac.setExecutor(new AllyChatCommand(this, teamManager));
        }

        var allyCmd = new AllyCommand(this, teamManager);
        var ally = getCommand("ally");
        if (ally != null) {
            ally.setExecutor(allyCmd);
            ally.setTabCompleter(allyCmd);
        }
    }

    @Override
    public void onDisable() {
        DiscordSRVHook.unregister();

        if (voteManager != null) {
            voteManager.cancelAll();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    public String prefix() {
        return getConfig().getString("messages.prefix", "&8[&7Team&8] ");
    }

    public String allyPrefix() {
        return getConfig().getString("messages.ally-prefix", "&8[&7Ally&8] ");
    }

    public String msg(String key) {
        return getConfig().getString("messages." + key, "");
    }

    public void sendPrefixed(CommandSender sender, String message) {
        sender.sendMessage(MessageUtil.parse(prefix() + message));
    }

    public void sendAllyPrefixed(CommandSender sender, String message) {
        sender.sendMessage(MessageUtil.parse(allyPrefix() + message));
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public VoteManager getVoteManager() {
        return voteManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
