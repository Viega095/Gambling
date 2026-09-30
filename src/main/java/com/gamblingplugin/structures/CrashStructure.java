package com.gamblingplugin.structures;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

public class CrashStructure {

    private final GamblingPlugin plugin;
    private final Location center;
    private ArmorStand launchpadStand;
    private ArmorStand hologramStand;
    private BukkitRunnable animationTask;

    public CrashStructure(GamblingPlugin plugin, Location center) {
        this.plugin = plugin;
        this.center = center.clone();
    }

    public void spawn() {
        World world = center.getWorld();
        if (world == null) return;

        // Base Launchpad ArmorStand
        launchpadStand = (ArmorStand) world.spawnEntity(center.clone().add(0, -0.8, 0), EntityType.ARMOR_STAND);
        launchpadStand.setVisible(false);
        launchpadStand.setGravity(false);
        launchpadStand.setHelmet(new ItemStack(Material.REDSTONE_BLOCK));

        // Floating Title Hologram
        hologramStand = (ArmorStand) world.spawnEntity(center.clone().add(0, 0.8, 0), EntityType.ARMOR_STAND);
        hologramStand.setVisible(false);
        hologramStand.setGravity(false);
        hologramStand.setCustomName("§6§l🚀 COHETE CRASH 3D §7(Clic Derecho)");
        hologramStand.setCustomNameVisible(true);
        hologramStand.setHelmet(new ItemStack(Material.FIREWORK_ROCKET));

        startIdleAnimation();
    }

    private void startIdleAnimation() {
        animationTask = new BukkitRunnable() {
            double angle = 0;
            @Override
            public void run() {
                if (hologramStand == null || !hologramStand.isValid()) {
                    cancel();
                    return;
                }
                angle += 0.08;
                hologramStand.setHeadPose(new EulerAngle(0, angle, 0));

                Location loc = center.clone().add(0, 0.2, 0);
                if (Math.random() < 0.3) {
                    loc.getWorld().spawnParticle(Particle.FLAME, loc, 2, 0.1, 0.05, 0.1, 0.01);
                }
            }
        };
        animationTask.runTaskTimer(plugin, 0L, 2L);
    }

    public void remove() {
        if (animationTask != null) animationTask.cancel();
        if (launchpadStand != null && launchpadStand.isValid()) launchpadStand.remove();
        if (hologramStand != null && hologramStand.isValid()) hologramStand.remove();
    }

    public Location getCenter() {
        return center.clone();
    }

    public boolean isNearby(Location loc) {
        return center.getWorld().equals(loc.getWorld()) && center.distance(loc) <= 3.5;
    }
}
