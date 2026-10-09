package com.vanillateams.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class TeamsCommand implements CommandExecutor {
    private final TeamCommand teamCommand;

    public TeamsCommand(TeamCommand teamCommand) {
        this.teamCommand = teamCommand;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        teamCommand.sendTeamList(sender);
        return true;
    }
}
