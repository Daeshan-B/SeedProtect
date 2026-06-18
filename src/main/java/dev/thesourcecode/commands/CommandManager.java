package dev.thesourcecode.commands;

import dev.thesourcecode.SeedProtect;
import dev.thesourcecode.commands.ToggleCommand;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;

public class CommandManager {

    public static void registerCommands(@NotNull SeedProtect plugin) {
        org.bukkit.command.Command command = plugin.getCommand("toggle");
        if (command instanceof org.bukkit.command.PluginCommand pluginCommand) {
            pluginCommand.setExecutor(new ToggleCommand(plugin));
        }
    }
}