package com.gamblingplugin.structures;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

public class BlackjackStructure {

    private final GamblingPlugin plugin;
    private final Location center;
    private ArmorStand tableStand;
    private BukkitRunnable rotationTask;

    public BlackjackStructure(GamblingPlugin plugin, Location center) {
        this.plugin = plugin;
        this.center = center.clone();
    }

    public void spawn() {
        // Create table (armor stand with green wool as "table")
        tableStand = (ArmorStand) center.getWorld().spawnEntity(center.clone().add(0, -1, 0), EntityType.ARMOR_STAND);
        tableStand.setVisible(false);
        tableStand.setGravity(false);
        tableStand.setCustomName("§0§l♠ §c§lBLACKJACK §0§l♥");
        tableStand.setCustomNameVisible(true);
        tableStand.setMarker(true);

        // Green wool block as table surface
        ItemStack table = new ItemStack(Material.GREEN_WOOL);
        tableStand.getEquipment().setHelmet(table);

        // Start rotation animation
        startAnimation();
    }

    private void startAnimation() {
        rotationTask = new BukkitRunnable() {
            double angle = 0;
            int colorCycle = 0;

            @Override
            public void run() {
                if (tableStand == null || !tableStand.isValid()) {
                    this.cancel();
                    return;
                }

                // Slow rotation
                angle += 0.1;
                if (angle >= 360)
                    angle = 0;

                EulerAngle headPose = new EulerAngle(0, Math.toRadians(angle), 0);
                tableStand.setHeadPose(headPose);

                // Change table color every 40 ticks (2 seconds)
                colorCycle++;
                if (colorCycle >= 40) {
                    colorCycle = 0;
                    Material[] tableColors = {
                            Material.GREEN_WOOL,
                            Material.LIME_WOOL,
                            Material.EMERALD_BLOCK,
                            Material.GREEN_CONCRETE
                    };
                    Material nextColor = tableColors[(int) (angle / 90) % tableColors.length];
                    tableStand.getEquipment().setHelmet(new ItemStack(nextColor));
                }
            }
        };
        rotationTask.runTaskTimer(plugin, 0L, 2L); // Every 2 ticks for smooth rotation
    }

    public void remove() {
        if (rotationTask != null) {
            rotationTask.cancel();
        }
        if (tableStand != null && tableStand.isValid()) {
            tableStand.remove();
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
