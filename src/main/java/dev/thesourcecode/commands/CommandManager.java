package dev.thesourcecode.commands;

import dev.thesourcecode.SeedProtect;
import dev.thesourcecode.commands.ToggleCommand;
import org.bukkit.command.PluginCommand;

public class CommandManager {

    public static void registerCommands(SeedProtect plugin) {
        plugin.getCommand("toggle").setExecutor(new ToggleCommand(plugin));
    }
}