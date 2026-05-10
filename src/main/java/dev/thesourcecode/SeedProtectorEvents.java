package dev.thesourcecode;

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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Listens for block-break and player-interact events to:
 *   1. Prevent immature crops from being broken (sneak to bypass).
 *   2. Auto-harvest & replant fully-grown crops, consuming 1 seed.
 *   3. Stop farmland trampling when a crop sits on top.
 *
 * Crops supported: WHEAT, CARROTS, POTATOES, BEETROOTS, NETHER_WART,
 *                   MELON_STEM, PUMPKIN_STEM, TORCHFLOWER_CROP, PITCHER_CROP.
 */
public class SeedProtectorEvents implements Listener {

    /*
     * Tracks the last time each player was told about sneaking.
     * Used to avoid spamming the same message repeatedly.
     */
    private final Map<Player, Instant> messageCooldown = new HashMap<>();

    /*
     * Shared random instance for XP chance rolls.
     */
    private final Random random = new Random();

    /* ---- configurable constants ---- */

    private static final long   MESSAGE_COOLDOWN = 10;     // seconds between nag messages
    private static final double EXP_CHANCE       = 0.05;   // 5 % chance
    private static final int    EXP_MIN          = 1;      // minimum XP orbs
    private static final int    EXP_MAX          = 3;      // maximum XP orbs
    private static final int    PARTICLE_COUNT   = 10;
    private static final double PARTICLE_RADIUS  = 0.5;

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
    private void onCropBreak(BlockBreakEvent event) {
        Block  block  = event.getBlock();
        Player player = event.getPlayer();

        // Let sneaking players break any block normally (including immature crops).
        if (player.isSneaking() || !isCrop(block)) {
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
     * Called for any player interaction (right-click, left-click, physical).
     *
     * We only care about PHYSICAL actions (walking) on farmland that has
     * a crop growing above it — this prevents trampling.
     */
    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL || !event.hasBlock()) {
            return;
        }

        Block farmland = event.getClickedBlock();
        if (farmland == null) return;

        // If the block above the farmland is a protected crop, cancel the trample.
        if (isCrop(farmland.getRelative(BlockFace.UP))) {
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
     *   2. Consume 1 seed/item for the replant.
     *   3. Spill everything else on the ground.
     *   4. Reset the block to its original type (age → 0).
     */
    private void autoReplant(Player player, Block block) {
        Location center    = block.getLocation().add(0.5, 0.5, 0.5);
        ItemStack tool     = player.getInventory().getItemInMainHand();
        Material  cropType = block.getType();

        /*
         * Stems — drop their loot (usually 0-3 seeds) and remove the
         * block.  We don't replant stems because they'd instantly
         * re-attach to a nearby melon/pumpkin, which feels wrong.
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
         * drop everything except 1 unit of that item, then replant.
         */
        Material plantMaterial = getPlantMaterial(cropType);

        for (ItemStack drop : block.getDrops(tool)) {
            if (drop.getType() == plantMaterial) {
                /*
                 * "Spend" 1 seed for the replant:
                 *   - If the stack has 2+, reduce it by 1 and drop the rest.
                 *   - If the stack only has 1, it is fully consumed — don't
                 *     drop anything for this entry.
                 */
                if (drop.getAmount() > 1) {
                    drop.setAmount(drop.getAmount() - 1);
                    center.getWorld().dropItem(center, drop);
                }
                // else: amount == 1 → fully consumed, skip dropping.
            } else {
                center.getWorld().dropItem(center, drop);
            }
        }

        /*
         * Reset the block to its default state (age 0) so it starts
         * growing again automatically.
         */
        block.setType(cropType);
    }

    /**
     * Gives the player a small XP reward (5 % chance, 1–3 orbs)
     * and shows a particle burst.
     */
    private void tryDropExperience(Location location, Player player) {
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
    private void spawnParticles(Location location) {
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
    private void sendCooldownMessage(Player player, String message) {
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
    private boolean isCrop(Block block) {
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
    private Material getPlantMaterial(Material crop) {
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
