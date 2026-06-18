package dev.thesourcecode;

import dev.thesourcecode.MessageManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Listens for block-break and player-interact events to:
 *   1. Prevent immature crops from being broken (sneak to bypass).
 *   2. Auto-harvest & replant fully-grown crops, consuming 1 seed.
 *   3. Right-click harvest of fully-grown crops.
 *   4. Stop farmland trampling when a crop sits on top.
 *   5. Protect crops from water/lava flow.
 *   6. Subtle growth particles when a crop advances a stage.
 *
 * Crops supported: WHEAT, CARROTS, POTATOES, BEETROOTS, NETHER_WART,
 *                   MELON_STEM, PUMPKIN_STEM, TORCHFLOWER_CROP, PITCHER_CROP.
 */
public class SeedProtectorEvents implements Listener {

    /*
     * Reference to the plugin instance.
     * Used to check if the plugin is enabled during event handling.
     */
    private final SeedProtect plugin;

    /*
     * Tracks the last time each player was told about sneaking.
     * Used to avoid spamming the same message repeatedly.
     */
    private final Map<Player, Instant> messageCooldown = new HashMap<>();

    /**
     * Creates a new SeedProtectorEvents listener instance.
     * @param plugin The plugin instance to reference.
     */
    public SeedProtectorEvents(@NotNull SeedProtect plugin) {
        this.plugin = plugin;
    }



    /* ---- configurable constants ---- */

    private static final long   MESSAGE_COOLDOWN = 10;     // seconds between nag messages
    private static final double EXP_CHANCE       = 0.05;   // 5 % chance
    private static final int    EXP_MIN          = 1;      // minimum XP orbs
    private static final int    EXP_MAX          = 3;      // maximum XP orbs
    private static final int    PARTICLE_COUNT   = 10;
    private static final double PARTICLE_RADIUS  = 0.5;
    private static final int    GROWTH_PARTICLE_COUNT = 3;    // particles emitted per growth tick
    private static final boolean GROWTH_PARTICLES      = true; // toggle growth particles on/off

    /* ================================================================
     *                         EVENT HANDLERS
     * ================================================================ */

    /**
     * Called whenever a player breaks a block.
     *
     * If the block is a fully-grown crop we auto-replant it, dropping
     * the loot (minus one seed used for replanting).  If the crop is
     * still growing we cancel the break and tell the player to sneak.
     */
    @EventHandler
    private void onCropBreak(@NotNull BlockBreakEvent event) {
        Block  block  = event.getBlock();
        Player player = event.getPlayer();

        // Let sneaking players break any block normally (including immature crops).
        if (player.isSneaking() || !isCrop(block)) {
            return;
        }

        if (!(block.getBlockData() instanceof Ageable)) {
            return;
        }

        Ageable ageable = (Ageable) block.getBlockData();

        if (ageable.getAge() == ageable.getMaximumAge()) {
            /*
             * Fully grown → harvest the crop, drop items (save 1 seed for
             * replanting), then set the block back so it starts growing again.
             */
            autoReplant(player, block);
            tryDropExperience(block.getLocation(), player);
            event.setCancelled(true);
        } else {
            // Immature → prevent break and warn the player (rate-limited).
            event.setCancelled(true);
            sendCooldownMessage(player, "Sneak to break baby seeds.");
        }
    }

    /**
     * Handles physical (trample), right-click (harvest / bonemeal) interactions.
     *
     * PHYSICAL       → prevent farmland trampling if a crop is on top.
     * RIGHT_CLICK    → harvest & replant fully-grown crops;
     *                   immature crops pass through so bonemeal works normally.
     */
    @EventHandler
    private void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        if (!event.hasBlock() || event.getClickedBlock() == null) return;

        Block  block  = event.getClickedBlock();
        Action action = event.getAction();

        // ---------- trample protection ----------
        if (action == Action.PHYSICAL) {
            if (isCrop(block.getRelative(BlockFace.UP))) {
                event.setCancelled(true);
            }
            return;
        }

        // ---------- right-click harvest ----------
        if (action == Action.RIGHT_CLICK_BLOCK && isCrop(block)) {
            Player  player  = event.getPlayer();
            Ageable ageable = (Ageable) block.getBlockData();

            if (ageable.getAge() == ageable.getMaximumAge()) {
                autoReplant(player, block);
                tryDropExperience(block.getLocation(), player);
                event.setCancelled(true);
            }
            // Immature crops fall through → bonemeal works normally.
        }
    }

    /**
     * Fired when a crop naturally grows or is bonemealed to the next stage.
     * Shows a subtle particle burst if GROWTH_PARTICLES is enabled.
     */
    @EventHandler
    private void onCropGrow(@NotNull BlockGrowEvent event) {
        if (!GROWTH_PARTICLES) return;

        Block block = event.getBlock();
        if (!isCrop(block)) return;

        Location center = block.getLocation().add(0.5, 0.5, 0.5);
        World world = center.getWorld();
        if (world == null) return;

        world.spawnParticle(Particle.HAPPY_VILLAGER, center,
                GROWTH_PARTICLE_COUNT, 0.3, 0.3, 0.3, 0);
    }

    /**
     * Prevents water/lava from flowing into and destroying crops.
     */
    @EventHandler
    private void onWaterFlow(@NotNull BlockFromToEvent event) {
        if (!plugin.isEnabled()) return;

        if (isCrop(event.getToBlock())) {
            event.setCancelled(true);
        }
    }

    /* ================================================================
     *                       HELPER METHODS
     * ================================================================ */

    /**
     * Harvests a fully-grown crop and replants it.
     *
     * For stems (melon, pumpkin) the block is simply removed — they
     * don't get replanted.  For every other crop we:
     *   1. Collect the drops (affected by the player's held tool).
     *   2. If the player holds the matching seed in-hand, consume 1
     *      from the held stack and drop everything (no seed tax).
     *      Otherwise, consume 1 seed from the drops themselves.
     *   3. Spill everything on the ground.
     *   4. Reset the block to its original type (age → 0).
     */
    private void autoReplant(@NotNull Player player, @NotNull Block block) {
        Location center    = block.getLocation().add(0.5, 0.5, 0.5);
        ItemStack tool     = player.getInventory().getItemInMainHand();
        Material  cropType = block.getType();

        /*
         * Stems — drop their loot and remove the block.
         * They don't replant because they'd instantly re-attach to
         * a nearby melon/pumpkin, which feels wrong.
         */
        if (cropType == Material.MELON_STEM || cropType == Material.PUMPKIN_STEM) {
            for (ItemStack drop : block.getDrops(tool)) {
                center.getWorld().dropItem(center, drop);
            }
            block.setType(Material.AIR);
            return;
        }

        /*
         * Plantable crops — figure out which seed/item is needed,
         * collect the drops, consume 1 seed for replanting
         * (either from the player's hand or from the drops), then replant.
         */
        Material plantMaterial = getPlantMaterial(cropType);
        boolean plantedFromHand = tryConsumeFromHand(player, plantMaterial);

        for (ItemStack drop : block.getDrops(tool)) {
            if (drop.getType() == plantMaterial) {
                if (plantedFromHand) {
                    // Seed was consumed from hand, don't drop it from drops
                    continue;
                } else {
                    // Seed was from drops, consume 1 for replanting
                    if (drop.getAmount() > 1) {
                        drop.setAmount(drop.getAmount() - 1);
                        center.getWorld().dropItem(center, drop);
                    }
                    // else: amount == 1, fully consumed, skip dropping
                    continue;
                }
            }
            center.getWorld().dropItem(center, drop);
        }

        // Reset the block to its default state (age 0) so it regrows.
        block.setType(cropType);
    }

    /**
     * If the player is holding {@code seedType} in their main hand,
     * consume 1 unit from that stack and return true (the replant
     * seed came from the hand, not the drops).
     */
    private boolean tryConsumeFromHand(@NotNull Player player, @NotNull Material seedType) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() != seedType) return false;

        if (hand.getAmount() > 1) {
            hand.setAmount(hand.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        return true;
    }

    /**
     * Gives the player a small XP reward (5 % chance, 1–3 orbs)
     * and shows a particle burst.
     */
    private void tryDropExperience(@NotNull Location location, @NotNull Player player) {
        if (random.nextDouble() > EXP_CHANCE) return;

        int amount = EXP_MIN + random.nextInt(EXP_MAX - EXP_MIN + 1);

        World world = location.getWorld();
        if (world == null) return;

        world.spawn(location, ExperienceOrb.class).setExperience(amount);
        MessageManager.good(player, "You gained +" + amount + " Experience");
        spawnParticles(location);
    }

    /**
     * Spawns a ring of happy-villager particles above the block.
     */
    private void spawnParticles(@NotNull Location location) {
        Location center = location.clone().add(0.5, 0.5, 0.5);
        World world = center.getWorld();
        if (world == null) return;

        world.spawnParticle(Particle.HAPPY_VILLAGER, center,
                PARTICLE_COUNT, PARTICLE_RADIUS, PARTICLE_RADIUS, PARTICLE_RADIUS, 0);
    }

    /**
     * Sends a message to the player, but only if the cooldown period
     * has elapsed.  Prevents chat spam.
     */
    private void sendCooldownMessage(@NotNull Player player, @NotNull String message) {
        Instant now       = Instant.now();
        Instant lastSent  = messageCooldown.get(player);

        if (lastSent == null || now.isAfter(lastSent)) {
            MessageManager.info(player, message);
            messageCooldown.put(player, now.plusSeconds(MESSAGE_COOLDOWN));
        }
    }

    /**
     * Returns true if the given block is a crop we should protect.
     *
     * Covers all standard food crops plus the 1.20 torchflower & pitcher.
     */
    private boolean isCrop(@NotNull Block block) {
        return switch (block.getType()) {
            case WHEAT, CARROTS, POTATOES, BEETROOTS,
                 MELON_STEM, PUMPKIN_STEM, NETHER_WART,
                 TORCHFLOWER_CROP, PITCHER_CROP -> true;
            default -> false;
        };
    }

    /**
     * Maps a crop type → the seed / item that you need to plant it.
     * Returns null for unhandled (shouldn't happen in practice).
     */
    private @Nullable Material getPlantMaterial(@NotNull Material crop) {
        return switch (crop) {
            case WHEAT           -> Material.WHEAT_SEEDS;
            case CARROTS         -> Material.CARROTS;
            case POTATOES        -> Material.POTATOES;
            case BEETROOTS       -> Material.BEETROOT_SEEDS;
            case NETHER_WART     -> Material.NETHER_WART;
            case TORCHFLOWER_CROP -> Material.TORCHFLOWER_SEEDS;
            case PITCHER_CROP    -> Material.PITCHER_POD;
            default              -> null;
        };
    }
}
