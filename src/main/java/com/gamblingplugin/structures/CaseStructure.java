package com.gamblingplugin.structures;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

public class CaseStructure {

    private final GamblingPlugin plugin;
    private final Location center;
    private ArmorStand caseStand;
    private BukkitRunnable rotationTask;

    public CaseStructure(GamblingPlugin plugin, Location center) {
        this.plugin = plugin;
        this.center = center.clone();
    }

    public void spawn() {
        // Create case armor stand
        caseStand = (ArmorStand) center.getWorld().spawnEntity(center.clone().add(0, -1, 0), EntityType.ARMOR_STAND);
        caseStand.setVisible(false);
        caseStand.setGravity(false);
        caseStand.setCustomName("§e§l📦 §6§lCASE OPENING §e§l✨");
        caseStand.setCustomNameVisible(true);
        caseStand.setMarker(true);

        // Chest as case
        ItemStack caseBlock = new ItemStack(Material.CHEST);
        caseStand.getEquipment().setHelmet(caseBlock);

        // Start rotation animation
        startAnimation();
    }

    private void startAnimation() {
        rotationTask = new BukkitRunnable() {
            double angle = 0;
            int colorCycle = 0;

            @Override
            public void run() {
                if (caseStand == null || !caseStand.isValid()) {
                    this.cancel();
                    return;
                }

                // Rotation
                angle += 0.13;
                if (angle >= 360)
                    angle = 0;

                EulerAngle headPose = new EulerAngle(0, Math.toRadians(angle), 0);
                caseStand.setHeadPose(headPose);

                // Cycle through treasure chests every 30 ticks
                colorCycle++;
                if (colorCycle >= 30) {
                    colorCycle = 0;
                    Material[] caseTypes = {
                            Material.CHEST,
                            Material.TRAPPED_CHEST,
                            Material.ENDER_CHEST,
                            Material.BARREL
                    };
                    Material nextCase = caseTypes[(int) (angle / 90) % caseTypes.length];
                    caseStand.getEquipment().setHelmet(new ItemStack(nextCase));
                }
            }
        };
        rotationTask.runTaskTimer(plugin, 0L, 2L);
    }

    public void remove() {
        if (rotationTask != null) {
            rotationTask.cancel();
        }
        if (caseStand != null && caseStand.isValid()) {
            caseStand.remove();
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
    public void playOpenAnimation() {
        // Visual effect - make the case spin faster temporarily
        if (caseStand != null && caseStand.isValid()) {
            // You could add particle effects here if desired
        }
    }

    public ArmorStand getDisplayStand() {
        return caseStand;
    }
}
