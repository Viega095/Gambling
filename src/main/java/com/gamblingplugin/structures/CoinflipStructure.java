package com.gamblingplugin.structures;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.games.Coinflip;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

public class CoinflipStructure {

    private final GamblingPlugin plugin;
    private final Location center;
    private ArmorStand coinStand;
    private ArmorStand holoStand;
    private BukkitRunnable idleTask;
    private boolean isFlipping = false;

    public CoinflipStructure(GamblingPlugin plugin, Location center) {
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
        holoStand.setCustomName("§6§l◯ §e§lCOINFLIP 3D §6§l◯");
        holoStand.setCustomNameVisible(true);
        holoStand.setMarker(true);

        // 2. Physical Coin Stand
        coinStand = (ArmorStand) world.spawnEntity(center.clone().add(0, -0.6, 0), EntityType.ARMOR_STAND);
        coinStand.setVisible(false);
        coinStand.setGravity(false);
        coinStand.setMarker(true);
        coinStand.getEquipment().setHelmet(new ItemStack(Material.GOLD_BLOCK));

        startIdleAnimation();
    }

    private void startIdleAnimation() {
        if (idleTask != null) idleTask.cancel();

        idleTask = new BukkitRunnable() {
            double angle = 0;

            @Override
            public void run() {
                if (isFlipping) return;
                if (coinStand == null || !coinStand.isValid()) {
                    cancel();
                    return;
                }

                angle += 0.08;
                if (angle >= 360) angle = 0;

                // Coin gently spin
                EulerAngle headPose = new EulerAngle(0, Math.toRadians(angle * 30), Math.toRadians(15));
                coinStand.setHeadPose(headPose);
            }
        };
        idleTask.runTaskTimer(plugin, 0L, 2L);
    }

    public void flip(Runnable callback) {
        flip3D(null, Coinflip.Side.HEADS, callback);
    }

    public void flip3D(Player player, Coinflip.Side result, Runnable callback) {
        if (isFlipping) return;
        isFlipping = true;

        World world = center.getWorld();
        if (world == null || coinStand == null || !coinStand.isValid()) {
            isFlipping = false;
            if (callback != null) callback.run();
            return;
        }

        if (holoStand != null && holoStand.isValid()) {
            holoStand.setCustomName("§e⚡ §l¡MONEDA EN EL AIRE! §e⚡");
        }

        Location baseLoc = center.clone().add(0, -0.6, 0);

        new BukkitRunnable() {
            int tick = 0;
            final int maxTicks = 40;
            double currentY = baseLoc.getY();
            double yVelocity = 0.16;
            double pitchAngle = 0;

            @Override
            public void run() {
                tick++;

                if (coinStand == null || !coinStand.isValid()) {
                    cancel();
                    isFlipping = false;
                    return;
                }

                // Physics arc
                currentY += yVelocity;
                yVelocity -= 0.008; // gravity

                if (currentY < baseLoc.getY() && tick > 15) {
                    currentY = baseLoc.getY();
                    yVelocity = -yVelocity * 0.45; // bounce decay
                    world.playSound(center, Sound.BLOCK_ANVIL_LAND, 0.4f, 1.6f);
                    world.spawnParticle(Particle.CRIT, center.clone().add(0, 0.3, 0), 10, 0.2, 0.1, 0.2, 0.05);
                }

                Location currentLoc = coinStand.getLocation();
                currentLoc.setY(currentY);
                coinStand.teleport(currentLoc);

                // Rapid vertical flip rotation
                pitchAngle += 35.0;
                coinStand.setHeadPose(new EulerAngle(Math.toRadians(pitchAngle), 0, 0));

                // Mid-air sparkles and sounds
                if (tick % 2 == 0) {
                    world.playSound(center, Sound.BLOCK_NOTE_BLOCK_HAT, 0.8f, 1.2f + (float) Math.sin(tick * 0.2f) * 0.4f);
                    world.spawnParticle(Particle.FIREWORKS_SPARK, currentLoc.clone().add(0, 1.0, 0), 2, 0.05, 0.05, 0.05, 0.01);
                }

                // Alternate visual during flip
                if (tick % 4 == 0) {
                    Material mat = (tick % 8 == 0) ? Material.GOLD_BLOCK : Material.IRON_BLOCK;
                    coinStand.getEquipment().setHelmet(new ItemStack(mat));
                }

                if (tick >= maxTicks) {
                    cancel();
                    // Final position on table
                    coinStand.teleport(baseLoc);

                    boolean isHeads = (result == Coinflip.Side.HEADS);
                    coinStand.getEquipment().setHelmet(new ItemStack(isHeads ? Material.GOLD_BLOCK : Material.IRON_BLOCK));
                    coinStand.setHeadPose(new EulerAngle(0, 0, 0));

                    world.playSound(center, Sound.ENTITY_PLAYER_LEVELUP, 0.9f, 1.3f);
                    world.spawnParticle(Particle.VILLAGER_HAPPY, center.clone().add(0, 0.8, 0), 25, 0.4, 0.4, 0.4, 0.1);

                    String outcomeText = isHeads ? "§6§l✦ CARA (HEADS) ✦" : "§7§l✧ CRUZ (TAILS) ✧";
                    if (holoStand != null && holoStand.isValid()) {
                        holoStand.setCustomName(outcomeText);
                    }

                    if (player != null) {
                        player.sendTitle(outcomeText, "§7¡Resultado oficial de la moneda!", 5, 40, 10);
                    }

                    if (callback != null) {
                        callback.run();
                    }

                    // Reset hologram after 4 seconds
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        isFlipping = false;
                        if (holoStand != null && holoStand.isValid()) {
                            holoStand.setCustomName("§6§l◯ §e§lCOINFLIP 3D §6§l◯");
                        }
                    }, 80L);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    public void remove() {
        if (idleTask != null) {
            idleTask.cancel();
        }
        if (coinStand != null && coinStand.isValid()) coinStand.remove();
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

    public boolean isFlipping() {
        return isFlipping;
    }
}
