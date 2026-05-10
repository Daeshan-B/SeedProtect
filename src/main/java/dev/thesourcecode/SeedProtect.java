package dev.thesourcecode;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * SeedProtect - A Paper plugin that protects your farm crops.
 *
 * Features:
 *   - Prevents immature crops from being broken (sneak to override)
 *   - Auto-replants fully-grown crops when harvested, consuming 1 seed
 *   - Prevents farmland from being trampled when crops are on it
 *   - Small XP reward chance with particle effects on harvest
 *
 * This plugin uses Paper's BlockDropProvider API for accurate tool-based
 * drops and Adventure for rich text messaging.
 */
public class SeedProtect extends JavaPlugin {

    @Override
    public void onEnable() {
        /*
         * Register our single event listener. All the crop-protection logic
         * lives in SeedProtectorEvents — this class just boots things up.
         */
        getServer().getPluginManager().registerEvents(new SeedProtectorEvents(), this);
        getLogger().info("SeedProtect v" + getPluginMeta().getVersion() + " enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("SeedProtect disabled.");
    }
}
