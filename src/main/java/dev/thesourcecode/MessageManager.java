package dev.thesourcecode;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

/**
 * Tiny helper for sending consistently-styled messages to players.
 *
 * Every message is prefixed with "[!] " in a colour that indicates the
 * tone: green = success, gold = info, red = error.
 */
public class MessageManager {

    /** The message prefix used in all messages. */
    public static final String PREFIX = "[!]";

    /** Green prefix, grey body — used for positive feedback (e.g. XP gained). */
    public static void good(Player player, String message) {
        player.sendMessage(
                Component.text(PREFIX).color(TextColor.color(0, 186, 0))
                        .append(Component.text(message).color(TextColor.color(92, 92, 92)))
        );
    }

    /** Gold prefix, grey body — used for neutral info (e.g. "sneak to break"). */
    public static void info(Player player, String message) {
        player.sendMessage(
                Component.text(PREFIX).color(TextColor.color(252, 186, 3))
                        .append(Component.text(message).color(TextColor.color(92, 92, 92)))
        );
    }

    /** Red prefix, red body — used for errors. */
    public static void error(Player player, String message) {
        player.sendMessage(
                Component.text(PREFIX).color(TextColor.color(186, 0, 0))
                        .append(Component.text(message).color(TextColor.color(255, 0, 0)))
        );
    }
}
