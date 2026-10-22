package com.vanillateams.command.subcommand;

import com.vanillateams.command.TeamCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ListSubCommand implements SubCommand {
    private final TeamCommand teamCommand;

    public ListSubCommand(TeamCommand teamCommand) {
        this.teamCommand = teamCommand;
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public List<String> getAliases() {
        return List.of("teams");
    }

    @Override
    public String getDescription() {
        return "List all teams on the server";
    }

    @Override
    public String getSyntax() {
        return "/team list";
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        teamCommand.sendTeamList(sender);
    }
}
