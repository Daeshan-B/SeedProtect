package dev.thesourcecode;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.w3c.dom.Text;

import java.util.logging.Logger;

public class MessageManager {

    private static String prefix = "[!] ";

    private static final Logger log = Logger.getLogger("Minecraft");

    public static void console(String message) {
        log.info(message);
    }

    public static void severe(String message) {
        log.severe(message);
    }

    public static void debug(String message) {
        log.info("[debug] " + message);
    }

    public static void good(Player player, String message) {
        player.sendMessage(Component.text(prefix).color(TextColor.color(0, 186, 0))
                .append(Component.text(message).color(TextColor.color(92,92,92))));
    }

    public static void info(Player player, String message) {
        player.sendMessage(Component.text(prefix).color(TextColor.color(252, 186, 3))
                .append(Component.text(message).color(TextColor.color(92,92,92))));
    }

    public static void error(Player player, String message) {
        player.sendMessage(Component.text(prefix).color(TextColor.color(0, 186, 0))
                .append(Component.text(message).color(TextColor.color(255,0,0))));
    }



}
