package com.vanillateams.command.subcommand;

import com.vanillateams.VanillaTeams;
import org.bukkit.command.CommandSender;

public class ReloadSubCommand implements SubCommand {
    private final VanillaTeams plugin;

    public ReloadSubCommand(VanillaTeams plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reload plugin configuration";
    }

    @Override
    public String getSyntax() {
        return "/team reload";
    }

    @Override
    public String getPermission() {
        return "vanillateams.admin";
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        plugin.reloadConfig();
        plugin.sendPrefixed(sender, plugin.msg("reloaded"));
    }
}
