package com.gamblingplugin.structures;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PlinkoStructure {

    private final GamblingPlugin plugin;
    private final Location center;
    private ArmorStand titleStand;
    private ArmorStand boardBackStand;
    private final List<ArmorStand> pegStands = new ArrayList<>();
    private final List<ArmorStand> bucketStands = new ArrayList<>();
    private BukkitRunnable idleTask;
    private boolean isDropping = false;

    public PlinkoStructure(GamblingPlugin plugin, Location center) {
        this.plugin = plugin;
        this.center = center.clone();
    }

    public void spawn() {
        World world = center.getWorld();
        if (world == null) return;

        // 1. Title Hologram
        titleStand = (ArmorStand) world.spawnEntity(center.clone().add(0, 2.2, 0), EntityType.ARMOR_STAND);
        titleStand.setVisible(false);
        titleStand.setGravity(false);
        titleStand.setMarker(true);
        titleStand.setCustomName("§e⭐ §c§lTORRE ARCADE PLINKO 3D §e⭐ §7(Clic)");
        titleStand.setCustomNameVisible(true);

        // 2. Backing Board Stand
        boardBackStand = (ArmorStand) world.spawnEntity(center.clone().add(0, -0.5, 0), EntityType.ARMOR_STAND);
        boardBackStand.setVisible(false);
        boardBackStand.setGravity(false);
        boardBackStand.setMarker(true);
        boardBackStand.getEquipment().setHelmet(new ItemStack(Material.POLISHED_DEEPSLATE));

        // 3. Pegs Grid (Pyramid: 3 rows of glowing pegs)
        double[][] pegOffsets = {
                {0.0, 1.2, 0.0},
                {-0.3, 0.7, 0.0}, {0.3, 0.7, 0.0},
                {-0.6, 0.2, 0.0}, {0.0, 0.2, 0.0}, {0.6, 0.2, 0.0}
        };
        for (double[] offset : pegOffsets) {
            ArmorStand peg = (ArmorStand) world.spawnEntity(center.clone().add(offset[0], offset[1] - 0.7, offset[2] + 0.1), EntityType.ARMOR_STAND);
            peg.setVisible(false);
            peg.setGravity(false);
            peg.setMarker(true);
            peg.getEquipment().setHelmet(new ItemStack(Material.END_ROD));
            pegStands.add(peg);
        }

        // 4. Bottom Reward Buckets
        String[] bucketLabels = {"§c50x", "§e3x", "§a1.5x", "§c0.2x", "§a1.5x", "§e3x", "§c50x"};
        Material[] bucketMats = {
                Material.NETHER_STAR, Material.GOLD_INGOT, Material.EMERALD,
                Material.COAL, Material.EMERALD, Material.GOLD_INGOT, Material.NETHER_STAR
        };
        for (int i = 0; i < 7; i++) {
            double xOff = -0.9 + (i * 0.3);
            ArmorStand bucket = (ArmorStand) world.spawnEntity(center.clone().add(xOff, -0.6, 0.2), EntityType.ARMOR_STAND);
            bucket.setVisible(false);
            bucket.setGravity(false);
            bucket.setMarker(true);
            bucket.getEquipment().setHelmet(new ItemStack(bucketMats[i]));
            bucketStands.add(bucket);
        }

        startIdleAnimation();
    }

    private void startIdleAnimation() {
        if (idleTask != null) idleTask.cancel();

        idleTask = new BukkitRunnable() {
            double tick = 0;

            @Override
            public void run() {
                if (isDropping) return;
                tick += 0.05;

                // Subtle neon pulse at top and pegs
                if (Math.random() < 0.2) {
                    Location pLoc = center.clone().add((Math.random() - 0.5) * 1.2, 0.5 + Math.random() * 1.2, 0.2);
                    center.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, pLoc, 1, 0.05, 0.05, 0.05, 0.01);
                }
            }
        };
        idleTask.runTaskTimer(plugin, 0L, 2L);
    }

    public void dropBall3D(Player player, double bet, String risk, Runnable callback) {
        if (isDropping) return;
        isDropping = true;

        World world = center.getWorld();
        if (world == null) {
            isDropping = false;
            if (callback != null) callback.run();
            return;
        }

        // Spawn ball at top funnel
        Location startLoc = center.clone().add(0, 1.8, 0.2);
        ArmorStand ballStand = (ArmorStand) world.spawnEntity(startLoc.clone().add(0, -0.7, 0), EntityType.ARMOR_STAND);
        ballStand.setVisible(false);
        ballStand.setGravity(false);
        ballStand.setMarker(true);
        ballStand.getEquipment().setHelmet(new ItemStack(Material.SNOWBALL));

        if (titleStand != null && titleStand.isValid()) {
            titleStand.setCustomName("§a⚡ §l¡BOLA CAYENDO EN 3D! §a⚡");
        }

        new BukkitRunnable() {
            int step = 0;
            final int maxSteps = 24;
            double currentX = startLoc.getX();
            double currentY = startLoc.getY();
            double xVel = 0;

            @Override
            public void run() {
                step++;

                if (ballStand == null || !ballStand.isValid()) {
                    cancel();
                    isDropping = false;
                    return;
                }

                currentY -= 0.09;
                currentX += xVel;

                // Bounce on pegs
                if (step % 5 == 0) {
                    xVel = (ThreadLocalRandom.current().nextBoolean() ? 0.04 : -0.04) * (1.0 + Math.random() * 0.5);
                    world.playSound(ballStand.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.9f, 1.2f + (step * 0.04f));
                    world.spawnParticle(Particle.CRIT, ballStand.getLocation().clone().add(0, 0.7, 0), 4, 0.1, 0.1, 0.1, 0.02);
                }

                Location bLoc = ballStand.getLocation();
                bLoc.setX(currentX);
                bLoc.setY(currentY - 0.7);
                ballStand.teleport(bLoc);

                if (step >= maxSteps) {
                    cancel();
                    ballStand.remove();
                    world.playSound(center, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.6f);
                    world.spawnParticle(Particle.TOTEM, center.clone().add(0, 0.3, 0.2), 15, 0.3, 0.3, 0.3, 0.1);

                    if (titleStand != null && titleStand.isValid()) {
                        titleStand.setCustomName("§6§l¡PREMIO OBTENIDO!");
                    }

                    if (callback != null) callback.run();

                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        isDropping = false;
                        if (titleStand != null && titleStand.isValid()) {
                            titleStand.setCustomName("§e⭐ §c§lTORRE ARCADE PLINKO 3D §e⭐ §7(Clic)");
                        }
                    }, 60L);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    public void remove() {
        if (idleTask != null) idleTask.cancel();
        if (titleStand != null && titleStand.isValid()) titleStand.remove();
        if (boardBackStand != null && boardBackStand.isValid()) boardBackStand.remove();
        for (ArmorStand p : pegStands) {
            if (p != null && p.isValid()) p.remove();
        }
        pegStands.clear();
        for (ArmorStand b : bucketStands) {
            if (b != null && b.isValid()) b.remove();
        }
        bucketStands.clear();
    }

    public Location getCenter() {
        return center.clone();
    }

    public boolean isNearby(Location loc) {
        return center.getWorld() != null && loc.getWorld() != null &&
                center.getWorld().equals(loc.getWorld()) && center.distance(loc) <= 4.0;
    }
}
