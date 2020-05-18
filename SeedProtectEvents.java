package dev.thesourcecode.seeds;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;

public class SeedProtectEvents implements Listener {

    private SeedProtectMain plugin;

    public SeedProtectEvents(SeedProtectMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void farmBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (!player.isSneaking()) {

            if (isBlockCrop(block)) {
                Ageable ageable = (Ageable) block.getState().getBlockData();
                if (ageable.getAge() == ageable.getMaximumAge()) {
                    autoReplant(player, block);
                    spawnParticles(block.getLocation());
                    event.setCancelled(true);
                } else {
                    event.setCancelled(true);
                    if (plugin.cropMessage.containsKey(player.getUniqueId())) {
                        if (plugin.cropMessage.get(player.getUniqueId()).plusSeconds(10).isBefore(Instant.now())) {
                            player.sendMessage(ChatColor.GOLD + "[SP]" + ChatColor.YELLOW + " Sneak to break baby seeds.");
                            plugin.cropMessage.put(player.getUniqueId(), Instant.now());
                        }
                    } else {
                        player.sendMessage(ChatColor.GOLD + "[SP]" + ChatColor.YELLOW + " Sneak to break baby seeds.");
                        plugin.cropMessage.put(player.getUniqueId(), Instant.now());
                    }
                }
            }
        }
    }

    @EventHandler
    public void cropTrample(BlockDestroyEvent event) {
        Block block = event.getBlock();

        if (isBlockCrop(block)) {
            Block below = event.getBlock().getRelative(BlockFace.DOWN);
            below.setType(Material.FARMLAND);
            event.setCancelled(true);
        }

    }


    private Boolean isBlockCrop(Block block) {
        switch (block.getType()) {
            case WHEAT:
            case CARROTS:
            case POTATOES:
            case BEETROOTS:
            case MELON_STEM:
            case PUMPKIN_STEM:
            case NETHER_WART:
                return true;
        }
        return false;
    }

    private void autoReplant(Player player, Block block) {

        block.getDrops(new ItemStack(player.getInventory().getItemInMainHand())).forEach(drop -> {
            switch (drop.getType()) {
                case WHEAT_SEEDS:
                case BEETROOT_SEEDS:
                case CARROTS:
                case POTATOES:
                case NETHER_WART:
                    drop.setAmount(drop.getAmount() - 1);
                    break;
            }
            player.getWorld().dropItem(block.getLocation(),drop);

        });

        block.setType(block.getType());
    }

    private void spawnParticles(Location location) {
        location.add(0.5, 0.5, 0.5);
        location.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, location, 10, .5, .5, .5, 0);
    }

}
