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

public class WheelStructure {

    public static class WheelSector {
        public final String name;
        public final Material material;
        public final double multiplier;
        public final boolean isJackpot;

        public WheelSector(String name, Material material, double multiplier, boolean isJackpot) {
            this.name = name;
            this.material = material;
            this.multiplier = multiplier;
            this.isJackpot = isJackpot;
        }
    }

    private final GamblingPlugin plugin;
    private final Location center;
    private ArmorStand hubStand;
    private ArmorStand pointerStand;
    private ArmorStand holoStand;
    private final List<ArmorStand> spokeStands = new ArrayList<>();
    private BukkitRunnable idleTask;
    private boolean isSpinning = false;
    private double currentWheelAngle = 0;

    private static final WheelSector[] SECTORS = {
            new WheelSector("§a✨ Multiplicador x2", Material.EMERALD_BLOCK, 2.0, false),
            new WheelSector("§e✨ Multiplicador x5", Material.GOLD_BLOCK, 5.0, false),
            new WheelSector("§b💎 Multiplicador x10", Material.DIAMOND_BLOCK, 10.0, false),
            new WheelSector("§d⭐ MEGA PREMIO x50 ⭐", Material.NETHER_STAR, 50.0, true),
            new WheelSector("§5🔮 PREMIO MÍSTICO x20", Material.AMETHYST_BLOCK, 20.0, false),
            new WheelSector("§e✨ Multiplicador x3", Material.LAPIS_BLOCK, 3.0, false),
            new WheelSector("§6⚡ Multiplicador x1.5", Material.REDSTONE_BLOCK, 1.5, false),
            new WheelSector("§f🎁 GIRO EXTRA x2.5", Material.SUNFLOWER, 2.5, false)
    };

    public WheelStructure(GamblingPlugin plugin, Location center) {
        this.plugin = plugin;
        this.center = center.clone();
    }

    public void spawn() {
        World world = center.getWorld();
        if (world == null) return;

        // 1. Hologram
        holoStand = (ArmorStand) world.spawnEntity(center.clone().add(0, 2.6, 0), EntityType.ARMOR_STAND);
        holoStand.setVisible(false);
        holoStand.setGravity(false);
        holoStand.setMarker(true);
        holoStand.setCustomName("§6🎡 §e§lMEGA RUEDA DE LA FORTUNA §6🎡");
        holoStand.setCustomNameVisible(true);

        // 2. Hub center stand
        hubStand = (ArmorStand) world.spawnEntity(center.clone().add(0, 0.4, 0), EntityType.ARMOR_STAND);
        hubStand.setVisible(false);
        hubStand.setGravity(false);
        hubStand.setMarker(true);
        hubStand.getEquipment().setHelmet(new ItemStack(Material.GLOWSTONE));

        // 3. Pointer needle at top
        pointerStand = (ArmorStand) world.spawnEntity(center.clone().add(0, 2.0, 0), EntityType.ARMOR_STAND);
        pointerStand.setVisible(false);
        pointerStand.setGravity(false);
        pointerStand.setMarker(true);
        pointerStand.getEquipment().setHelmet(new ItemStack(Material.GOLDEN_SWORD));
        pointerStand.setHeadPose(new EulerAngle(Math.toRadians(180), 0, 0)); // Pointing down

        // 4. Circular Spoke Stands (radius = 1.2 blocks vertically in X-Y plane)
        double radius = 1.25;
        for (int i = 0; i < SECTORS.length; i++) {
            double angle = (2 * Math.PI / SECTORS.length) * i;
            double offsetX = radius * Math.cos(angle);
            double offsetY = 1.0 + radius * Math.sin(angle);

            Location spokeLoc = center.clone().add(offsetX, offsetY - 1.4, 0);
            ArmorStand spoke = (ArmorStand) world.spawnEntity(spokeLoc, EntityType.ARMOR_STAND);
            spoke.setVisible(false);
            spoke.setGravity(false);
            spoke.setMarker(true);
            spoke.getEquipment().setHelmet(new ItemStack(SECTORS[i].material));
            spokeStands.add(spoke);
        }

        startIdleAnimation();
    }

    private void startIdleAnimation() {
        if (idleTask != null) idleTask.cancel();

        idleTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (isSpinning) return;
                if (hubStand == null || !hubStand.isValid()) {
                    cancel();
                    return;
                }

                currentWheelAngle += 0.02;
                if (currentWheelAngle >= 2 * Math.PI) currentWheelAngle = 0;

                updateSpokePositions(currentWheelAngle);
            }
        };
        idleTask.runTaskTimer(plugin, 0L, 2L);
    }

    private void updateSpokePositions(double baseAngle) {
        double radius = 1.25;
        for (int i = 0; i < spokeStands.size(); i++) {
            ArmorStand spoke = spokeStands.get(i);
            if (spoke == null || !spoke.isValid()) continue;

            double angle = baseAngle + (2 * Math.PI / SECTORS.length) * i;
            double offsetX = radius * Math.cos(angle);
            double offsetY = 1.0 + radius * Math.sin(angle);

            Location newLoc = center.clone().add(offsetX, offsetY - 1.4, 0);
            spoke.teleport(newLoc);
        }
    }

    public void spin(Player player, double bet, Runnable callback) {
        if (isSpinning) {
            player.sendMessage("§c✖ La Mega Rueda ya está girando.");
            return;
        }
        isSpinning = true;

        World world = center.getWorld();
        if (world == null) {
            isSpinning = false;
            return;
        }

        if (holoStand != null && holoStand.isValid()) {
            holoStand.setCustomName("§e⚡ §l¡GIRANDO LA MEGA RUEDA! §e⚡");
        }

        // Determine winning sector
        int winningIndex = ThreadLocalRandom.current().nextInt(SECTORS.length);
        WheelSector won = SECTORS[winningIndex];

        new BukkitRunnable() {
            int tick = 0;
            final int maxTicks = 55;
            double speed = 0.45;

            @Override
            public void run() {
                tick++;

                if (hubStand == null || !hubStand.isValid()) {
                    cancel();
                    isSpinning = false;
                    return;
                }

                currentWheelAngle += speed;
                if (currentWheelAngle >= 2 * Math.PI) currentWheelAngle -= 2 * Math.PI;

                updateSpokePositions(currentWheelAngle);

                // Deceleration physics
                if (tick > 20) {
                    speed *= 0.94;
                }

                // Audio ticker sound as sectors pass top pointer
                if (tick % Math.max(1, (int)(0.5 / Math.max(speed, 0.05))) == 0) {
                    world.playSound(center, Sound.BLOCK_NOTE_BLOCK_HAT, 0.8f, 1.2f + (tick * 0.015f));
                    world.spawnParticle(Particle.CRIT, center.clone().add(0, 2.2, 0), 3, 0.1, 0.1, 0.1, 0.02);
                }

                if (tick >= maxTicks || speed < 0.01) {
                    cancel();

                    // Align precisely to winning sector at top (angle = PI/2)
                    double targetBaseAngle = (Math.PI / 2.0) - (2 * Math.PI / SECTORS.length) * winningIndex;
                    updateSpokePositions(targetBaseAngle);

                    double prize = bet * won.multiplier;
                    plugin.getEconomyManager().deposit(player, prize);

                    // Spectacular win effects
                    world.playSound(center, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                    world.playSound(center, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
                    world.spawnParticle(Particle.FIREWORKS_SPARK, center.clone().add(0, 1.5, 0), 40, 1.0, 1.0, 1.0, 0.1);
                    world.spawnParticle(Particle.VILLAGER_HAPPY, center.clone().add(0, 1.5, 0), 30, 0.8, 0.8, 0.8, 0.05);

                    if (won.isJackpot) {
                        Bukkit.broadcastMessage("§6🎡💥 §l¡" + player.getName() + " §eha ganado el " + won.name + " §6en la Mega Rueda del Casino!");
                        world.spawnParticle(Particle.TOTEM, center.clone().add(0, 1.5, 0), 80, 1.0, 1.0, 1.0, 0.2);
                    }

                    player.sendTitle(won.name, "§a+" + plugin.getEconomyManager().format(prize), 10, 60, 15);
                    player.sendMessage("§6🎡 [Mega Rueda] §7¡Has obtenido §e" + won.name + "§7! Premio: §a" + plugin.getEconomyManager().format(prize));

                    if (holoStand != null && holoStand.isValid()) {
                        holoStand.setCustomName("§a🏆 §l" + won.name + " §7(+" + plugin.getEconomyManager().format(prize) + ")");
                    }

                    if (callback != null) callback.run();

                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        isSpinning = false;
                        if (holoStand != null && holoStand.isValid()) {
                            holoStand.setCustomName("§6🎡 §e§lMEGA RUEDA DE LA FORTUNA §6🎡");
                        }
                    }, 80L);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    public void remove() {
        if (idleTask != null) idleTask.cancel();
        if (hubStand != null && hubStand.isValid()) hubStand.remove();
        if (pointerStand != null && pointerStand.isValid()) pointerStand.remove();
        if (holoStand != null && holoStand.isValid()) holoStand.remove();
        for (ArmorStand s : spokeStands) {
            if (s != null && s.isValid()) s.remove();
        }
        spokeStands.clear();
    }

    public Location getCenter() {
        return center.clone();
    }

    public boolean isNearby(Location location) {
        return center.getWorld() != null && location.getWorld() != null &&
                center.getWorld().equals(location.getWorld()) &&
                center.distance(location) <= 4.5;
    }

    public boolean isSpinning() {
        return isSpinning;
    }
}
