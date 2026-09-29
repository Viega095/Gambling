package com.gamblingplugin.structures;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

public class CoinflipStructure {

    private final GamblingPlugin plugin;
    private final Location center;
    private ArmorStand coinStand;
    private BukkitRunnable rotationTask;

    public CoinflipStructure(GamblingPlugin plugin, Location center) {
        this.plugin = plugin;
        this.center = center.clone();
    }

    public void spawn() {
        // Create coin armor stand
        coinStand = (ArmorStand) center.getWorld().spawnEntity(center.clone().add(0, -1, 0), EntityType.ARMOR_STAND);
        coinStand.setVisible(false);
        coinStand.setGravity(false);
        coinStand.setCustomName("§6§l◯ §e§lCOINFLIP §6§l◯");
        coinStand.setCustomNameVisible(true);
        coinStand.setMarker(true);

        // Gold block as coin
        ItemStack coinBlock = new ItemStack(Material.GOLD_BLOCK);
        coinStand.getEquipment().setHelmet(coinBlock);

        // Start rotation animation
        startAnimation();
    }

    private void startAnimation() {
        rotationTask = new BukkitRunnable() {
            double angle = 0;
            int colorCycle = 0;
            boolean flipDirection = true;

            @Override
            public void run() {
                if (coinStand == null || !coinStand.isValid()) {
                    this.cancel();
                    return;
                }

                // Coin flip rotation (like a spinning coin)
                angle += 0.2;
                if (angle >= 360)
                    angle = 0;

                // Flip on Y axis to simulate coin flip
                EulerAngle headPose = new EulerAngle(0, Math.toRadians(angle), Math.toRadians(angle * 2));
                coinStand.setHeadPose(headPose);

                // Alternate between gold and yellow every 25 ticks
                colorCycle++;
                if (colorCycle >= 25) {
                    colorCycle = 0;
                    flipDirection = !flipDirection;
                    Material[] coinColors = {
                            Material.GOLD_BLOCK,
                            Material.YELLOW_CONCRETE,
                            Material.GOLD_ORE,
                            Material.RAW_GOLD_BLOCK
                    };
                    Material nextColor = flipDirection ? coinColors[0]
                            : coinColors[(int) (angle / 120) % coinColors.length];
                    coinStand.getEquipment().setHelmet(new ItemStack(nextColor));
                }
            }
        };
        rotationTask.runTaskTimer(plugin, 0L, 2L);
    }

    public void remove() {
        if (rotationTask != null) {
            rotationTask.cancel();
        }
        if (coinStand != null && coinStand.isValid()) {
            coinStand.remove();
        }
    }

    public Location getCenter() {
        return center.clone();
    }

    public boolean isNearby(Location location) {
        return center.getWorld().equals(location.getWorld()) &&
                center.distance(location) <= 4;
    }

    // Compatibility methods for game logic
    private boolean isFlipping = false;

    public boolean isFlipping() {
        return isFlipping;
    }

    public void flip(Runnable callback) {
        isFlipping = true;
        // The animation is already running in the background
        // Just simulate a flip delay and callback
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            isFlipping = false;
            if (callback != null) {
                callback.run();
            }
        }, 30L); // 1.5 seconds
    }
}
