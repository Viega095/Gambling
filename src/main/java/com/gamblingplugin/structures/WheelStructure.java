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
    private ArmorStand pointerHoloStand;
    private ArmorStand holoStand;
    private final List<ArmorStand> spokeStands = new ArrayList<>();
    private BukkitRunnable idleTask;
    private boolean isSpinning = false;
    private double currentWheelAngle = 0;

    // 16 Diverse Sectors with high variety of iconic items
    public static final WheelSector[] SECTORS = {
            new WheelSector("§d⭐ MEGA PREMIO x50 ⭐", Material.NETHER_STAR, 50.0, true),
            new WheelSector("§b💎 DIAMANTE REAL x25", Material.DIAMOND_BLOCK, 25.0, false),
            new WheelSector("§8⚔️ RELIQUIA NETHERITE x15", Material.NETHERITE_INGOT, 15.0, false),
            new WheelSector("§6🛡️ TÓTEM ANCESTRAL x12", Material.TOTEM_OF_UNDYING, 12.0, false),
            new WheelSector("§a💰 LLUVIA DE ESMERALDAS x10", Material.EMERALD_BLOCK, 10.0, false),
            new WheelSector("§5🔮 CRISTAL MÍSTICO x8", Material.AMETHYST_CLUSTER, 8.0, false),
            new WheelSector("§e👑 CORONA DE ORO x5", Material.GOLD_BLOCK, 5.0, false),
            new WheelSector("§d🍎 MANZANA SAGRADA x4", Material.ENCHANTED_GOLDEN_APPLE, 4.0, false),
            new WheelSector("§9🌊 ZAFIRO ABISAL x3", Material.LAPIS_BLOCK, 3.0, false),
            new WheelSector("§f🎁 GIRO EXTRA x2.5", Material.SUNFLOWER, 2.5, false),
            new WheelSector("§c⚡ RAYO DE LA SUERTE x2", Material.REDSTONE_BLOCK, 2.0, false),
            new WheelSector("§3🐚 PERLA MARINA x1.5", Material.PRISMARINE_SHARD, 1.5, false),
            new WheelSector("§e✨ BRILLO DORADO x2", Material.GLOWSTONE, 2.0, false),
            new WheelSector("§2👁️ OJO DE LA FORTUNA x3", Material.ENDER_EYE, 3.0, false),
            new WheelSector("§6🍯 COSECHA DULCE x1.5", Material.HONEYCOMB, 1.5, false),
            new WheelSector("§6🪙 COBRE DE LA SUERTE x1.2", Material.COPPER_BLOCK, 1.2, false)
    };

    private static final double WHEEL_RADIUS = 2.3;
    private static final double HUB_HEIGHT = 2.0;

    public WheelStructure(GamblingPlugin plugin, Location center) {
        this.plugin = plugin;
        this.center = center.clone();
    }

    public void spawn() {
        World world = center.getWorld();
        if (world == null) return;

        // 1. Top Header Hologram
        holoStand = (ArmorStand) world.spawnEntity(center.clone().add(0, HUB_HEIGHT + WHEEL_RADIUS + 1.2, 0), EntityType.ARMOR_STAND);
        holoStand.setVisible(false);
        holoStand.setGravity(false);
        holoStand.setMarker(true);
        holoStand.setCustomName("§6🎡 §e§lMEGA RUEDA DE LA FORTUNA 3D §6🎡");
        holoStand.setCustomNameVisible(true);

        // 2. Hub Center Stand
        hubStand = (ArmorStand) world.spawnEntity(center.clone().add(0, HUB_HEIGHT - 0.7, 0), EntityType.ARMOR_STAND);
        hubStand.setVisible(false);
        hubStand.setGravity(false);
        hubStand.setMarker(true);
        hubStand.getEquipment().setHelmet(new ItemStack(Material.BEACON));

        // 3. Pointer Indicator Hologram & Sword pointing DOWN
        Location pointerHoloLoc = center.clone().add(0, HUB_HEIGHT + WHEEL_RADIUS + 0.6, 0);
        pointerHoloStand = (ArmorStand) world.spawnEntity(pointerHoloLoc, EntityType.ARMOR_STAND);
        pointerHoloStand.setVisible(false);
        pointerHoloStand.setGravity(false);
        pointerHoloStand.setMarker(true);
        pointerHoloStand.setCustomName("§e▼ §6§lSELECTOR §e▼");
        pointerHoloStand.setCustomNameVisible(true);

        Location pointerLoc = center.clone().add(0, HUB_HEIGHT + WHEEL_RADIUS + 0.1, 0);
        pointerStand = (ArmorStand) world.spawnEntity(pointerLoc, EntityType.ARMOR_STAND);
        pointerStand.setVisible(false);
        pointerStand.setGravity(false);
        pointerStand.setMarker(true);
        pointerStand.getEquipment().setItemInMainHand(new ItemStack(Material.GOLDEN_SWORD));
        // Pointing straight down towards the top wheel sector
        pointerStand.setRightArmPose(new EulerAngle(Math.toRadians(180), Math.toRadians(0), Math.toRadians(0)));

        // 4. 16 Circular Spoke Stands in Vertical Plane (radius = 2.3 blocks)
        for (int i = 0; i < SECTORS.length; i++) {
            double angle = (2 * Math.PI / SECTORS.length) * i;
            double offsetX = WHEEL_RADIUS * Math.cos(angle);
            double offsetY = HUB_HEIGHT + WHEEL_RADIUS * Math.sin(angle);

            Location spokeLoc = center.clone().add(offsetX, offsetY - 0.7, 0);
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

                currentWheelAngle += 0.012;
                if (currentWheelAngle >= 2 * Math.PI) currentWheelAngle = 0;

                updateSpokePositions(currentWheelAngle);
            }
        };
        idleTask.runTaskTimer(plugin, 0L, 2L);
    }

    private void updateSpokePositions(double baseAngle) {
        for (int i = 0; i < spokeStands.size(); i++) {
            ArmorStand spoke = spokeStands.get(i);
            if (spoke == null || !spoke.isValid()) continue;

            double angle = baseAngle + (2 * Math.PI / SECTORS.length) * i;
            double offsetX = WHEEL_RADIUS * Math.cos(angle);
            double offsetY = HUB_HEIGHT + WHEEL_RADIUS * Math.sin(angle);

            Location newLoc = center.clone().add(offsetX, offsetY - 0.7, 0);
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
            holoStand.setCustomName("§e⚡ §l¡GIRANDO LA MEGA RUEDA 3D! §e⚡");
        }

        // Determine winning sector randomly
        int winningIndex = ThreadLocalRandom.current().nextInt(SECTORS.length);
        WheelSector won = SECTORS[winningIndex];

        new BukkitRunnable() {
            int tick = 0;
            final int maxTicks = 65;
            double speed = 0.50;

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
                if (tick > 25) {
                    speed *= 0.945;
                }

                // Audio ticker sound and sparks from sword tip as sectors pass under pointer
                if (tick % Math.max(1, (int) (0.4 / Math.max(speed, 0.04))) == 0) {
                    world.playSound(center.clone().add(0, HUB_HEIGHT + WHEEL_RADIUS, 0), Sound.BLOCK_NOTE_BLOCK_HAT, 0.8f, 1.2f + (tick * 0.015f));
                    world.spawnParticle(Particle.CRIT, center.clone().add(0, HUB_HEIGHT + WHEEL_RADIUS + 0.1, 0), 4, 0.1, 0.1, 0.1, 0.05);
                    world.spawnParticle(Particle.FLAME, center.clone().add(0, HUB_HEIGHT + WHEEL_RADIUS + 0.1, 0), 2, 0.05, 0.05, 0.05, 0.01);
                }

                if (tick >= maxTicks || speed < 0.01) {
                    cancel();

                    // Align precisely to winning sector at top (angle = PI/2)
                    double targetBaseAngle = (Math.PI / 2.0) - (2 * Math.PI / SECTORS.length) * winningIndex;
                    updateSpokePositions(targetBaseAngle);

                    double prize = bet * won.multiplier;
                    plugin.getEconomyManager().deposit(player, prize);

                    // Spectacular win celebration
                    Location topLoc = center.clone().add(0, HUB_HEIGHT + WHEEL_RADIUS, 0);
                    world.playSound(topLoc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                    world.playSound(topLoc, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
                    world.spawnParticle(Particle.FIREWORKS_SPARK, topLoc, 50, 1.2, 1.2, 1.2, 0.15);
                    world.spawnParticle(Particle.VILLAGER_HAPPY, topLoc, 35, 0.8, 0.8, 0.8, 0.05);

                    if (won.isJackpot) {
                        Bukkit.broadcastMessage("§6🎡💥 §l¡" + player.getName() + " §eha ganado el " + won.name + " §6en la Mega Rueda del Casino!");
                        world.spawnParticle(Particle.TOTEM, topLoc, 100, 1.2, 1.2, 1.2, 0.25);
                    }

                    player.sendTitle(won.name, "§a+" + plugin.getEconomyManager().format(prize), 10, 60, 15);
                    player.sendMessage("§6🎡 [Mega Rueda 3D] §7¡Has obtenido §e" + won.name + "§7! Premio: §a" + plugin.getEconomyManager().format(prize));

                    if (holoStand != null && holoStand.isValid()) {
                        holoStand.setCustomName("§a🏆 §l" + won.name + " §7(+" + plugin.getEconomyManager().format(prize) + ")");
                    }

                    if (callback != null) callback.run();

                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        isSpinning = false;
                        if (holoStand != null && holoStand.isValid()) {
                            holoStand.setCustomName("§6🎡 §e§lMEGA RUEDA DE LA FORTUNA 3D §6🎡");
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
        if (pointerHoloStand != null && pointerHoloStand.isValid()) pointerHoloStand.remove();
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
                center.distance(location) <= 5.5;
    }

    public boolean isSpinning() {
        return isSpinning;
    }
}
