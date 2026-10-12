package com.gamblingplugin.structures;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

public class KenoTableStructure {

    private final GamblingPlugin plugin;
    private final Location center;
    private ArmorStand holoStand;
    private ArmorStand tumblerStand;
    private ArmorStand ballStand;
    private BukkitRunnable idleTask;
    private boolean isDrawing = false;

    public KenoTableStructure(GamblingPlugin plugin, Location center) {
        this.plugin = plugin;
        this.center = center.clone();
    }

    public void spawn() {
        World world = center.getWorld();
        if (world == null) return;

        // Clean up orphan stands at this spot
        for (org.bukkit.entity.Entity entity : world.getNearbyEntities(center, 2.5, 2.5, 2.5)) {
            if (entity instanceof ArmorStand stand && !stand.isVisible()) {
                String name = stand.getCustomName();
                if (name != null && name.contains("KENO")) {
                    stand.remove();
                }
            }
        }

        // 1. Hologram Stand
        holoStand = (ArmorStand) world.spawnEntity(center.clone().add(0, 0.6, 0), EntityType.ARMOR_STAND);
        holoStand.setVisible(false);
        holoStand.setGravity(false);
        holoStand.setMarker(true);
        holoStand.setCustomName("§6§l🎱 §e§lKENO DE LA FORTUNA 3D §6§l🎱");
        holoStand.setCustomNameVisible(true);

        // 2. Tumbler Base Stand
        tumblerStand = (ArmorStand) world.spawnEntity(center.clone().add(0, -0.6, 0), EntityType.ARMOR_STAND);
        tumblerStand.setVisible(false);
        tumblerStand.setGravity(false);
        tumblerStand.setMarker(true);
        tumblerStand.getEquipment().setHelmet(new ItemStack(Material.SEA_LANTERN));

        // 3. Central Extractor Ball Stand
        ballStand = (ArmorStand) world.spawnEntity(center.clone().add(0, -0.1, 0), EntityType.ARMOR_STAND);
        ballStand.setVisible(false);
        ballStand.setGravity(false);
        ballStand.setMarker(true);
        ballStand.getEquipment().setHelmet(new ItemStack(Material.GOLD_BLOCK));

        startIdleAnimation();
    }

    private void startIdleAnimation() {
        if (idleTask != null) idleTask.cancel();

        idleTask = new BukkitRunnable() {
            double angle = 0;

            @Override
            public void run() {
                if (isDrawing) return;
                if (tumblerStand == null || !tumblerStand.isValid()) {
                    cancel();
                    return;
                }

                angle += 0.06;
                if (angle >= 360) angle = 0;

                tumblerStand.setHeadPose(new EulerAngle(0, angle, 0));
                if (ballStand != null && ballStand.isValid()) {
                    ballStand.setHeadPose(new EulerAngle(Math.sin(angle) * 0.2, -angle, 0));
                }

                if (Math.random() < 0.25) {
                    center.getWorld().spawnParticle(Particle.CRIT_MAGIC, center.clone().add(0, 0.4, 0), 2, 0.2, 0.2, 0.2, 0.02);
                }
            }
        };
        idleTask.runTaskTimer(plugin, 0L, 2L);
    }

    public void remove() {
        if (idleTask != null) idleTask.cancel();
        if (holoStand != null && holoStand.isValid()) holoStand.remove();
        if (tumblerStand != null && tumblerStand.isValid()) tumblerStand.remove();
        if (ballStand != null && ballStand.isValid()) ballStand.remove();
    }

    public Location getCenter() {
        return center.clone();
    }

    public boolean isNearby(Location loc) {
        return center.getWorld() != null && loc.getWorld() != null &&
                center.getWorld().equals(loc.getWorld()) &&
                center.distance(loc) <= 3.5;
    }
}
