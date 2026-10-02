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

public class CaseStructure {

    private final GamblingPlugin plugin;
    private final Location center;
    private ArmorStand caseStand;
    private ArmorStand prizeStand;
    private ArmorStand holoStand;
    private BukkitRunnable idleTask;
    private boolean isOpening = false;

    private static final Material[] PREVIEW_ITEMS = {
            Material.NETHERITE_SWORD,
            Material.NETHER_STAR,
            Material.ELYTRA,
            Material.TOTEM_OF_UNDYING,
            Material.ENCHANTED_GOLDEN_APPLE,
            Material.DIAMOND_BLOCK,
            Material.NETHERITE_CHESTPLATE,
            Material.GOLD_BLOCK
    };

    public CaseStructure(GamblingPlugin plugin, Location center) {
        this.plugin = plugin;
        this.center = center.clone();
    }

    public void spawn() {
        World world = center.getWorld();
        if (world == null) return;

        // 1. Hologram Stand
        holoStand = (ArmorStand) world.spawnEntity(center.clone().add(0, 0.6, 0), EntityType.ARMOR_STAND);
        holoStand.setVisible(false);
        holoStand.setGravity(false);
        holoStand.setMarker(true);
        holoStand.setCustomName("§e§l📦 §6§lCAJA DE RECOMPENSAS 3D §e§l✨");
        holoStand.setCustomNameVisible(true);

        // 2. Base Chest Stand
        caseStand = (ArmorStand) world.spawnEntity(center.clone().add(0, -0.6, 0), EntityType.ARMOR_STAND);
        caseStand.setVisible(false);
        caseStand.setGravity(false);
        caseStand.setMarker(true);
        caseStand.getEquipment().setHelmet(new ItemStack(Material.ENDER_CHEST));

        // 3. Prize Stand (hidden inside chest initially)
        prizeStand = (ArmorStand) world.spawnEntity(center.clone().add(0, -0.6, 0), EntityType.ARMOR_STAND);
        prizeStand.setVisible(false);
        prizeStand.setGravity(false);
        prizeStand.setMarker(true);

        startIdleAnimation();
    }

    private void startIdleAnimation() {
        if (idleTask != null) idleTask.cancel();

        idleTask = new BukkitRunnable() {
            double angle = 0;

            @Override
            public void run() {
                if (isOpening) return;
                if (caseStand == null || !caseStand.isValid()) {
                    cancel();
                    return;
                }

                angle += 0.04;
                if (angle >= 360) angle = 0;

                // Slow rotation
                EulerAngle headPose = new EulerAngle(0, Math.toRadians(angle * 20), 0);
                caseStand.setHeadPose(headPose);
            }
        };
        idleTask.runTaskTimer(plugin, 0L, 2L);
    }

    public void open3D(Player player, ItemStack wonItem, Runnable callback) {
        if (isOpening) return;
        isOpening = true;

        World world = center.getWorld();
        if (world == null || prizeStand == null || !prizeStand.isValid()) {
            isOpening = false;
            if (callback != null) callback.run();
            return;
        }

        if (holoStand != null && holoStand.isValid()) {
            holoStand.setCustomName("§d✨ §l¡ABRIENDO CAJA MISTERIOSA! §d✨");
        }

        world.playSound(center, Sound.BLOCK_CHEST_OPEN, 1f, 1f);
        world.playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.5f);

        Location baseLoc = center.clone().add(0, -0.6, 0);

        new BukkitRunnable() {
            int tick = 0;
            final int maxTicks = 45;
            double currentY = baseLoc.getY();

            @Override
            public void run() {
                tick++;

                if (prizeStand == null || !prizeStand.isValid()) {
                    cancel();
                    isOpening = false;
                    return;
                }

                // Smoothly rise out of chest up to +1.6
                if (currentY < baseLoc.getY() + 1.6) {
                    currentY += 0.08;
                }

                Location currentLoc = prizeStand.getLocation();
                currentLoc.setY(currentY);
                prizeStand.teleport(currentLoc);

                // Rapidly rotate and cycle items
                prizeStand.setHeadPose(new EulerAngle(0, Math.toRadians(tick * 30), 0));
                Material displayMat = PREVIEW_ITEMS[ThreadLocalRandom.current().nextInt(PREVIEW_ITEMS.length)];
                prizeStand.getEquipment().setHelmet(new ItemStack(displayMat));

                // Enchantment / Portal particles
                world.spawnParticle(Particle.PORTAL, currentLoc.clone().add(0, 1.0, 0), 10, 0.3, 0.3, 0.3, 0.5);
                world.spawnParticle(Particle.END_ROD, currentLoc.clone().add(0, 0.8, 0), 2, 0.1, 0.1, 0.1, 0.02);

                if (tick % 2 == 0) {
                    world.playSound(center, Sound.BLOCK_NOTE_BLOCK_HAT, 0.8f, 1.2f + (tick * 0.02f));
                }

                if (tick >= maxTicks) {
                    cancel();

                    // Lock on won item
                    ItemStack finalItem = (wonItem != null) ? wonItem : new ItemStack(Material.NETHER_STAR);
                    prizeStand.getEquipment().setHelmet(finalItem);
                    prizeStand.setHeadPose(new EulerAngle(0, 0, 0));

                    world.playSound(center, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                    world.playSound(center, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1.2f);
                    world.spawnParticle(Particle.TOTEM, currentLoc.clone().add(0, 1.0, 0), 50, 0.5, 0.5, 0.5, 0.2);
                    world.spawnParticle(Particle.FIREWORKS_SPARK, currentLoc.clone().add(0, 1.0, 0), 30, 0.5, 0.5, 0.5, 0.1);

                    String itemName = finalItem.getItemMeta() != null && finalItem.getItemMeta().hasDisplayName()
                            ? finalItem.getItemMeta().getDisplayName()
                            : finalItem.getType().name();

                    if (holoStand != null && holoStand.isValid()) {
                        holoStand.setCustomName("§a🏆 ¡OBTENIDO! §e" + itemName);
                    }

                    if (player != null) {
                        player.sendTitle("§a§l¡PREMIO OBTENIDO!", "§e" + itemName, 10, 50, 15);
                    }

                    if (callback != null) callback.run();

                    // Hide prize back into chest after 5 seconds
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        isOpening = false;
                        if (prizeStand != null && prizeStand.isValid()) {
                            prizeStand.teleport(baseLoc);
                            prizeStand.getEquipment().setHelmet(null);
                        }
                        if (holoStand != null && holoStand.isValid()) {
                            holoStand.setCustomName("§e§l📦 §6§lCAJA DE RECOMPENSAS 3D §e§l✨");
                        }
                    }, 100L);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    public void remove() {
        if (idleTask != null) idleTask.cancel();
        if (caseStand != null && caseStand.isValid()) caseStand.remove();
        if (prizeStand != null && prizeStand.isValid()) prizeStand.remove();
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

    public boolean isOpening() {
        return isOpening;
    }

    public void playOpenAnimation() {
        open3D(null, new ItemStack(Material.NETHER_STAR), null);
    }

    public ArmorStand getDisplayStand() {
        return caseStand;
    }
}
