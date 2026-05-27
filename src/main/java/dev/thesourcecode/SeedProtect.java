package dev.thesourcecode;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class SeedProtect extends JavaPlugin {

    private static final TextColor GREEN  = TextColor.color(100, 220, 80);
    private static final TextColor DIM    = TextColor.color(60, 130, 50);
    private static final TextColor WHITE  = TextColor.color(200, 200, 200);

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new SeedProtectorEvents(), this);

        String v = getPluginMeta().getVersion();
        var c = Bukkit.getConsoleSender();

        c.sendMessage(Component.text("  ╔══════════════════════════════════════════╗", DIM));
        c.sendMessage(
            Component.text("  ║  ", DIM)
                .append(Component.text("SeedProtect ", GREEN))
                .append(Component.text("v" + v, WHITE))
                .append(Component.text("  │  ", DIM))
                .append(Component.text("crops are safe", GREEN))
                .append(Component.text("  ║", DIM))
        );
        c.sendMessage(Component.text("  ╚══════════════════════════════════════════╝", DIM));
    }

    @Override
    public void onDisable() {
        var c = Bukkit.getConsoleSender();
        c.sendMessage(
            Component.text("  ═══ ", DIM)
                .append(Component.text("SeedProtect done. Replant again soon! o/", TextColor.color(100, 220, 80)))
                .append(Component.text(" ═══", DIM))
        );
    }
}
