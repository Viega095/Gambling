package com.gamblingplugin.structures;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

public class DiceStructure {

    private final GamblingPlugin plugin;
    private final Location center;
    private ArmorStand diceStand;
    private BukkitRunnable rotationTask;

    public DiceStructure(GamblingPlugin plugin, Location center) {
        this.plugin = plugin;
        this.center = center.clone();
    }

    public void spawn() {
        // Create dice armor stand
        diceStand = (ArmorStand) center.getWorld().spawnEntity(center.clone().add(0, -1, 0), EntityType.ARMOR_STAND);
        diceStand.setVisible(false);
        diceStand.setGravity(false);
        diceStand.setCustomName("§f§l⚀ §6§lDICE §f§l⚅");
        diceStand.setCustomNameVisible(true);
        diceStand.setMarker(true);

        // White concrete as dice block
        ItemStack diceBlock = new ItemStack(Material.WHITE_CONCRETE);
        diceStand.getEquipment().setHelmet(diceBlock);

        // Start rotation animation
        startAnimation();
    }

    private void startAnimation() {
        rotationTask = new BukkitRunnable() {
            double angle = 0;
            int colorCycle = 0;

            @Override
            public void run() {
                if (diceStand == null || !diceStand.isValid()) {
                    this.cancel();
                    return;
                }

                // Rotation
                angle += 0.15;
                if (angle >= 360)
                    angle = 0;

                EulerAngle headPose = new EulerAngle(Math.toRadians(angle), Math.toRadians(angle), 0);
                diceStand.setHeadPose(headPose);

                // Change dice color every 30 ticks (1.5 seconds)
                colorCycle++;
                if (colorCycle >= 30) {
                    colorCycle = 0;
                    Material[] diceColors = {
                            Material.WHITE_CONCRETE,
                            Material.LIGHT_GRAY_CONCRETE,
                            Material.BONE_BLOCK,
                            Material.QUARTZ_BLOCK
                    };
                    Material nextColor = diceColors[(int) (angle / 90) % diceColors.length];
                    diceStand.getEquipment().setHelmet(new ItemStack(nextColor));
                }
            }
        };
        rotationTask.runTaskTimer(plugin, 0L, 2L);
    }

    public void remove() {
        if (rotationTask != null) {
            rotationTask.cancel();
        }
        if (diceStand != null && diceStand.isValid()) {
            diceStand.remove();
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
    private boolean isRolling = false;

    public boolean isRolling() {
        return isRolling;
    }

    public void roll(Runnable callback) {
        isRolling = true;
        // The animation is already running in the background
        // Just simulate a roll delay and callback
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            isRolling = false;
            if (callback != null) {
                callback.run();
            }
        }, 40L); // 2 seconds
    }
}
