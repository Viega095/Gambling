package com.gamblingplugin.structures;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.concurrent.ThreadLocalRandom;

public class DiceStructure {

    private final GamblingPlugin plugin;
    private final Location center;
    private ArmorStand diceStand;
    private ArmorStand diceStand2;
    private ArmorStand holoStand;
    private BukkitRunnable idleTask;
    private boolean isRolling = false;

    public DiceStructure(GamblingPlugin plugin, Location center) {
        this.plugin = plugin;
        this.center = center.clone();
    }

    public void spawn() {
        World world = center.getWorld();
        if (world == null) return;

        // 1. Hologram Stand
        holoStand = (ArmorStand) world.spawnEntity(center.clone().add(0, 0.4, 0), EntityType.ARMOR_STAND);
        holoStand.setVisible(false);
        holoStand.setGravity(false);
        holoStand.setCustomName("§f§l⚀ §6§lMESA DE DADOS 3D §f§l⚅");
        holoStand.setCustomNameVisible(true);
        holoStand.setMarker(true);

        // 2. Primary Dice Stand
        diceStand = (ArmorStand) world.spawnEntity(center.clone().add(0, -0.6, 0), EntityType.ARMOR_STAND);
        diceStand.setVisible(false);
        diceStand.setGravity(false);
        diceStand.setMarker(true);
        diceStand.getEquipment().setHelmet(new ItemStack(Material.WHITE_CONCRETE));

        // 3. Secondary Decorative Dice Stand
        diceStand2 = (ArmorStand) world.spawnEntity(center.clone().add(0.4, -0.7, 0.3), EntityType.ARMOR_STAND);
        diceStand2.setVisible(false);
        diceStand2.setGravity(false);
        diceStand2.setMarker(true);
        diceStand2.getEquipment().setHelmet(new ItemStack(Material.RED_CONCRETE));
        diceStand2.setHeadPose(new EulerAngle(Math.toRadians(25), Math.toRadians(45), Math.toRadians(15)));

        startIdleAnimation();
    }

    private void startIdleAnimation() {
        if (idleTask != null) idleTask.cancel();

        idleTask = new BukkitRunnable() {
            double angle = 0;

            @Override
            public void run() {
                if (isRolling) return;
                if (diceStand == null || !diceStand.isValid()) {
                    cancel();
                    return;
                }

                angle += 0.05;
                if (angle >= 360) angle = 0;

                // Gentle floating hover & slow rotation
                EulerAngle headPose = new EulerAngle(Math.toRadians(10 * Math.sin(angle)), Math.toRadians(angle * 20), 0);
                diceStand.setHeadPose(headPose);
            }
        };
        idleTask.runTaskTimer(plugin, 0L, 2L);
    }

    public void roll(Runnable callback) {
        roll3D(null, ThreadLocalRandom.current().nextInt(1, 7), callback);
    }

    public void roll3D(Player player, int targetResult, Runnable callback) {
        if (isRolling) return;
        isRolling = true;

        World world = center.getWorld();
        if (world == null || diceStand == null || !diceStand.isValid()) {
            isRolling = false;
            if (callback != null) callback.run();
            return;
        }

        if (holoStand != null && holoStand.isValid()) {
            holoStand.setCustomName("§e🎲 §l¡LANZANDO DADOS EN 3D! §e🎲");
        }

        Location baseLoc = center.clone().add(0, -0.6, 0);

        new BukkitRunnable() {
            int tick = 0;
            final int maxTicks = 32;
            double currentY = baseLoc.getY();
            double yVelocity = 0.13;

            @Override
            public void run() {
                tick++;

                if (diceStand == null || !diceStand.isValid()) {
                    cancel();
                    isRolling = false;
                    return;
                }

                // Physics simulation: parabolic arc + bounce
                currentY += yVelocity;
                yVelocity -= 0.009; // gravity

                if (currentY < baseLoc.getY()) {
                    currentY = baseLoc.getY();
                    yVelocity = -yVelocity * 0.50; // bounce decay
                    world.playSound(center, Sound.BLOCK_STONE_STEP, 0.9f, 1.3f + (tick * 0.02f));
                    world.spawnParticle(Particle.CRIT, center.clone().add(0, 0.4, 0), 8, 0.2, 0.1, 0.2, 0.05);
                }

                Location currentStandLoc = diceStand.getLocation();
                currentStandLoc.setY(currentY);
                diceStand.teleport(currentStandLoc);

                // Rapid 3D tumbling rotation
                double rotX = ThreadLocalRandom.current().nextDouble(0, 360);
                double rotY = ThreadLocalRandom.current().nextDouble(0, 360);
                double rotZ = ThreadLocalRandom.current().nextDouble(0, 360);
                diceStand.setHeadPose(new EulerAngle(Math.toRadians(rotX), Math.toRadians(rotY), Math.toRadians(rotZ)));

                // Rolling sounds & particle trail
                if (tick % 3 == 0) {
                    world.playSound(center, Sound.ENTITY_ITEM_PICKUP, 0.7f, 1.6f);
                    world.spawnParticle(Particle.FIREWORKS_SPARK, currentStandLoc.clone().add(0, 1.0, 0), 3, 0.1, 0.1, 0.1, 0.02);
                }

                if (tick >= maxTicks) {
                    cancel();
                    // Settle dice cleanly on table
                    diceStand.teleport(baseLoc);
                    diceStand.setHeadPose(new EulerAngle(0, Math.toRadians(targetResult * 60), 0));

                    // Final Dice block styling according to result
                    Material[] diceMats = {
                            Material.WHITE_CONCRETE,
                            Material.LIGHT_GRAY_CONCRETE,
                            Material.BONE_BLOCK,
                            Material.QUARTZ_BLOCK,
                            Material.IRON_BLOCK,
                            Material.GOLD_BLOCK
                    };
                    Material chosenMat = diceMats[Math.min(targetResult - 1, diceMats.length - 1)];
                    diceStand.getEquipment().setHelmet(new ItemStack(chosenMat));

                    String unicodeDice = getDiceUnicode(targetResult);
                    if (holoStand != null && holoStand.isValid()) {
                        holoStand.setCustomName("§a§l🎲 RESULTADO: §e§l" + targetResult + " §f(" + unicodeDice + ")");
                    }

                    // Execute callback (finalizeGame in Dice.java)
                    if (callback != null) {
                        callback.run();
                    }

                    // Reset to idle hologram after 4 seconds
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        isRolling = false;
                        if (holoStand != null && holoStand.isValid()) {
                            holoStand.setCustomName("§f§l⚀ §6§lMESA DE DADOS 3D §f§l⚅");
                        }
                    }, 80L);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private String getDiceUnicode(int result) {
        return switch (result) {
            case 1 -> "⚀";
            case 2 -> "⚁";
            case 3 -> "⚂";
            case 4 -> "⚃";
            case 5 -> "⚄";
            case 6 -> "⚅";
            default -> "🎲";
        };
    }

    public void remove() {
        if (idleTask != null) {
            idleTask.cancel();
        }
        if (diceStand != null && diceStand.isValid()) diceStand.remove();
        if (diceStand2 != null && diceStand2.isValid()) diceStand2.remove();
        if (holoStand != null && holoStand.isValid()) holoStand.remove();
    }

    public Location getCenter() {
        return center.clone();
    }

    public boolean isNearby(Location location) {
        return center.getWorld() != null && location.getWorld() != null &&
                center.getWorld().equals(location.getWorld()) &&
                center.distance(location) <= 4.0;
    }

    public boolean isRolling() {
        return isRolling;
    }
}
