package com.gamblingplugin.structures;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

public class PlinkoStructure {

    private final GamblingPlugin plugin;
    private final Location center;
    private ArmorStand plinkoStand;
    private BukkitRunnable rotationTask;

    public PlinkoStructure(GamblingPlugin plugin, Location center) {
        this.plugin = plugin;
        this.center = center.clone();
    }

    public void spawn() {
        // Create plinko armor stand
        plinkoStand = (ArmorStand) center.getWorld().spawnEntity(center.clone().add(0, -1, 0), EntityType.ARMOR_STAND);
        plinkoStand.setVisible(false);
        plinkoStand.setGravity(false);
        plinkoStand.setCustomName("§6§l⬇ §c§lPLINKO §6§l⬇");
        plinkoStand.setCustomNameVisible(true);
        plinkoStand.setMarker(true);

        // Diamond block for plinko
        ItemStack plinkoBlock = new ItemStack(Material.DIAMOND_BLOCK);
        plinkoStand.getEquipment().setHelmet(plinkoBlock);

        // Start rotation animation
        startAnimation();
    }

    private void startAnimation() {
        rotationTask = new BukkitRunnable() {
            double angle = 0;
            int colorCycle = 0;

            @Override
            public void run() {
                if (plinkoStand == null || !plinkoStand.isValid()) {
                    this.cancel();
                    return;
                }

                // Rotation
                angle += 0.11;
                if (angle >= 360)
                    angle = 0;

                EulerAngle headPose = new EulerAngle(0, Math.toRadians(angle), 0);
                plinkoStand.setHeadPose(headPose);

                // Change between valuable blocks every 40 ticks
                colorCycle++;
                if (colorCycle >= 40) {
                    colorCycle = 0;
                    Material[] plinkoBlocks = {
                            Material.DIAMOND_BLOCK,
                            Material.EMERALD_BLOCK,
                            Material.GOLD_BLOCK,
                            Material.NETHERITE_BLOCK
                    };
                    Material nextBlock = plinkoBlocks[(int) (angle / 90) % plinkoBlocks.length];
                    plinkoStand.getEquipment().setHelmet(new ItemStack(nextBlock));
                }
            }
        };
        rotationTask.runTaskTimer(plugin, 0L, 2L);
    }

    public void remove() {
        if (rotationTask != null) {
            rotationTask.cancel();
        }
        if (plinkoStand != null && plinkoStand.isValid()) {
            plinkoStand.remove();
        }
    }

    public Location getCenter() {
        return center.clone();
    }

    public boolean isNearby(Location location) {
        return center.getWorld().equals(location.getWorld()) &&
                center.distance(location) <= 4;
    }
}
