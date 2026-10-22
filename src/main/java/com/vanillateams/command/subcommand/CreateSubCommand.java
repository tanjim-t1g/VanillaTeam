package com.vanillateams.command.subcommand;

import com.vanillateams.VanillaTeams;
import com.vanillateams.manager.TeamManager;
import com.vanillateams.model.Team;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateSubCommand implements SubCommand {
    private final VanillaTeams plugin;
    private final TeamManager teamManager;

    public CreateSubCommand(VanillaTeams plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
    }

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getDescription() {
        return "Create a new team";
    }

    @Override
    public String getSyntax() {
        return "/team create <name>";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (teamManager.getPlayerTeam(player) != null) {
            plugin.sendPrefixed(player, plugin.msg("already-in-team"));
            return;
        }

        if (args.length < 2) {
            plugin.sendPrefixed(player, "&7Usage: &f" + getSyntax());
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
}
