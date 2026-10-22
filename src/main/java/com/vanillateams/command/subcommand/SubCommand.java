package com.vanillateams.command.subcommand;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface SubCommand {
    String getName();

    default List<String> getAliases() {
        return List.of();
    }

    String getDescription();

    String getSyntax();

    default String getPermission() {
        return "vanillateams.use";
    }

    default boolean isPlayerOnly() {
        return true;
    }

    void execute(CommandSender sender, String[] args);

    default List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
