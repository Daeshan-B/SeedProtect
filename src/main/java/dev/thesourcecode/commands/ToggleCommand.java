package dev.thesourcecode.commands;

import dev.thesourcecode.SeedProtect;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ToggleCommand implements CommandExecutor {

    private final @NotNull SeedProtect plugin;

    public ToggleCommand(@NotNull SeedProtect plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be executed by a player.", TextColor.color(255, 0, 0)));
            return true;
        }

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("enable")) {
                plugin.setEnabled(true);
                player.sendMessage(Component.text("SeedProtect has been enabled.", TextColor.color(100, 220, 80)));
                return true;
            } else if (args[0].equalsIgnoreCase("disable")) {
                plugin.setEnabled(false);
                player.sendMessage(Component.text("SeedProtect has been disabled.", TextColor.color(255, 0, 0)));
                return true;
            }
        }

        // Default toggle
        plugin.setEnabled(!plugin.isEnabled());
        String status = plugin.isEnabled() ? "enabled" : "disabled";
        
        player.sendMessage(Component.text("SeedProtect has been " + status + ".", plugin.isEnabled() ? TextColor.color(100, 220, 80) : TextColor.color(255, 0, 0)));
        
        return true;
    }
}