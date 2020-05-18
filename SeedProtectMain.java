package dev.thesourcecode.seeds;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class SeedProtectMain extends JavaPlugin implements Listener {


    public Map<UUID, Instant> cropMessage = new HashMap<>();

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(new SeedProtectEvents(this), this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        cropMessage.clear();
    }
}
