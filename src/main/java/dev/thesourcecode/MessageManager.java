package dev.thesourcecode;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public class MessageManager {
    enum MESSAGE_LEVEL {
        GOOD, INFO, SEVERE, DEBUG
    }

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
        player.sendMessage(translateColorCodes("&a" + prefix + "&7" + message));
    }

    public static void info(Player player, String message) {
        player.sendMessage(translateColorCodes("&e" + prefix + "&7" + message));
    }

    public static void error(Player player, String message) {
        player.sendMessage(translateColorCodes("&c" + prefix + "&7" + message));
    }

    public static void broadcast(Server server, String message, Enum level) {
        if (level == MESSAGE_LEVEL.GOOD) {
            server.broadcastMessage(translateColorCodes("&a" + prefix + "&7" + message));
        } else if (level == MESSAGE_LEVEL.INFO) {
            server.broadcastMessage(translateColorCodes("&e" + prefix + "&7" + message));
        } else if (level == MESSAGE_LEVEL.SEVERE) {
            server.broadcastMessage(translateColorCodes("&c" + prefix + "&7" + message));
        } else if (level == MESSAGE_LEVEL.DEBUG) {
            server.broadcastMessage(translateColorCodes("&d[debug] &7" + message));
        }
    }

    public static String translateColorCodes(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

}
