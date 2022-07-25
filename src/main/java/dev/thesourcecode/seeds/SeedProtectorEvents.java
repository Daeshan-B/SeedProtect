package dev.thesourcecode.seeds;

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
import java.util.Map;
import java.util.Random;

public class SeedProtectorEvents implements Listener {
    private Map<Player, Instant> cropMessage;

    public SeedProtectorEvents(Map<Player, Instant> cropMessage) {
        this.cropMessage = cropMessage;
    }


    //TODO Prevent water breaking plants
/*
    @EventHandler
    public void onWaterPassThrough(BlockFromToEvent event) {
        final Block block = event.getToBlock();
        final Location blockLocation = block.getLocation();

        if (isCrop(block)) {
            if (block.getType() == Material.WHEAT) {
                block.getDrops().forEach(drop -> {
                    blockLocation.getWorld().dropItem(blockLocation, drop);
                });
                block.setType(Material.WHEAT);
                return;
            }

            block.getDrops().forEach(drop -> {
                switch (drop.getType()) {
                    case WHEAT_SEEDS:
                    case BEETROOT_SEEDS:
                    case CARROTS:
                    case POTATOES:
                    case NETHER_WART:
                        drop.setAmount(drop.getAmount() - 1);
                        break;
                }
                blockLocation.getWorld().dropItem(blockLocation, drop);
            });

            block.setType(block.getType());
            spawnParticles(block.getLocation());
            event.setCancelled(true);
        }
    }
*/

    @EventHandler
    private void farmBreak(BlockBreakEvent event) {
        final Block block = event.getBlock();
        final Player player = event.getPlayer();

        if (player.isSneaking() || !isCrop(block)) return;

        final Ageable ageable = (Ageable) block.getState().getBlockData();

        if (ageable.getAge() == ageable.getMaximumAge()) {
            autoReplant(player, block);
            dropExperience(block.getLocation(), player);
        } else {
            final Instant now = Instant.now();
            cropMessage.compute(player, (uuid, instant) -> {
                if (instant != null && now.isBefore(instant)) {
                    return instant;
                }
                MessageManager.info(player, "Sneak to break baby seeds.");
                return now.plusSeconds(10);
            });
        }

        event.setCancelled(true);
    }

    private void dropExperience(Location location, Player player) {

        Random dropChanceRandom = new Random();
        double dropChance = dropChanceRandom.nextDouble();
        double chancePercent = .05;

        Random experience = new Random();
        int low = 1;
        int high = 3;
        int experienceDrop = experience.nextInt(high - low) + low;

        if (dropChance <= chancePercent) {
            World world = location.getWorld();
            world.spawn(location, ExperienceOrb.class).setExperience(experienceDrop);
            MessageManager.good(player, "You gained &a+" + experienceDrop + " Experience");
            spawnParticles(location);
        }
    }

    @EventHandler
    private void cropTrample(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) {
            return;
        }
        if (!event.hasBlock()) {
            return;
        }

        final Block farmland = event.getClickedBlock();
        if (farmland == null) return;
        final Block crop = farmland.getRelative(BlockFace.UP);
        if (!isCrop(crop)) {
            return;
        }
        event.setCancelled(true);
    }

    private void spawnParticles(Location location) {
        location.add(.5, .5, .5);
        location.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, location, 10, .5, .5, .5, 0);
    }

    private void autoReplant(Player player, Block block) {
        Location blockLocation = block.getLocation();
        ItemStack mainHand = player.getInventory().getItemInMainHand();

        switch (block.getType()) {
            case WHEAT -> {
                block.getDrops().removeIf(drop -> drop.getType() == Material.AIR);
                block.getDrops(mainHand).forEach(drop -> {
                    blockLocation.getWorld().dropItem(blockLocation, drop);
                });
                block.setType(Material.WHEAT);
                return;
            }
            case MELON_STEM, PUMPKIN_STEM -> {
                block.getDrops(mainHand).forEach(drop -> {
                    blockLocation.getWorld().dropItem(blockLocation, drop);
                });
                block.setType(Material.AIR);
                return;
            }
        }
        block.getDrops(mainHand).forEach(drop -> {
            switch (drop.getType()) {
                case WHEAT_SEEDS, BEETROOT_SEEDS, CARROTS, POTATOES, NETHER_WART -> {
                    if (drop.getAmount() == 0) break;
                    drop.setAmount(drop.getAmount() - 1);
                }
            }
            blockLocation.getWorld().dropItem(blockLocation, drop);
        });

        block.setType(block.getType());
    }

    private boolean isCrop(Block block) {
        return switch (block.getType()) {
            case WHEAT, CARROTS, POTATOES, BEETROOTS, MELON_STEM, PUMPKIN_STEM, NETHER_WART -> true;
            default -> false;
        };
    }
}
