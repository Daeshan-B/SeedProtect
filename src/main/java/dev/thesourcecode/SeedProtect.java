package dev.thesourcecode;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import dev.thesourcecode.commands.CommandManager;
import dev.thesourcecode.SeedProtectorEvents;

/**
 * SeedProtect - A Minecraft Bukkit plugin for protecting crop seeds.
 *
 * @author Unknown
 * @version 2.0.0
 */
public class SeedProtect extends JavaPlugin {

    private static final TextColor GREEN = TextColor.color(100, 220, 80);
    private static final TextColor DIM = TextColor.color(60, 130, 50);
    private static final TextColor WHITE = TextColor.color(200, 200, 200);

    private static final String VERSION = "2.0.0";

    /**
     * Creates a new SeedProtect instance.
     */
    public SeedProtect() {
    }

    /**
     * Local variable for the plugin's event handler.
     */
    private SeedProtectorEvents events;

    /**
     * Constructs the plugin's welcome message as a single Component.
     *
     * @return A Component representing the plugin's ASCII art header
     */
    private Component buildWelcomeMessage() {
        return Component.empty()
            .append(Component.text("  ╔══════════════════════════════════════════╗", DIM))
            .append(Component.text("  ║  ", DIM))
            .append(Component.text("SeedProtect ", GREEN))
            .append(Component.text("v" + VERSION, WHITE))
            .append(Component.text("  │  ", DIM))
            .append(Component.text("crops are safe", GREEN))
            .append(Component.text("  ║", DIM))
            .append(Component.text("  ╚══════════════════════════════════════════╝", DIM));
    }

    /**
     * Constructs the plugin's shutdown message as a single Component.
     *
     * @return A Component representing the plugin's shutdown footer
     */
    private Component buildShutdownMessage() {
        return Component.empty()
            .append(Component.text("  ═══ ", DIM))
            .append(Component.text("SeedProtect done. Replant again soon! o/", GREEN))
            .append(Component.text(" ═══", DIM));
    }

    @Override
    public void onEnable() {
        // Initialize events
        events = new SeedProtectorEvents(this);
        getServer().getPluginManager().registerEvents(events, this);

        // Register commands
        CommandManager.registerCommands(this);

        // Send welcome message
        Bukkit.getConsoleSender().sendMessage(buildWelcomeMessage());

        // Log startup
        getLogger().info("SeedProtect v" + VERSION + " has been enabled!");
    }

    @Override
    public void onDisable() {
        // Send shutdown message
        Bukkit.getConsoleSender().sendMessage(buildShutdownMessage());

        // Log shutdown
        getLogger().info("SeedProtect v" + VERSION + " has been disabled!");
    }
}
