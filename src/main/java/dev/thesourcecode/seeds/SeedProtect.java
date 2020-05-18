package dev.thesourcecode.seeds;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Instant;
import java.util.IdentityHashMap;
import java.util.Map;

public final class SeedProtect extends JavaPlugin {
    private final Map<Player, Instant> cropMessage = new IdentityHashMap<>();

    @Override
    public void onDisable() {
        cropMessage.clear();
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new SeedProtectEvents(cropMessage), this);
    }
}
