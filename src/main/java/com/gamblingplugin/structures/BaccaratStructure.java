package com.gamblingplugin.structures;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;
import java.util.List;

public class BaccaratStructure {

    private final GamblingPlugin plugin;
    private final Location center;
    private ArmorStand titleStand;
    private ArmorStand tableStand;
    private ArmorStand dealerStand;
    private final List<ArmorStand> decoStands = new ArrayList<>();
    private BukkitRunnable idleTask;

    public BaccaratStructure(GamblingPlugin plugin, Location center) {
        this.plugin = plugin;
        this.center = center.clone();
    }

    public void spawn() {
        World world = center.getWorld();
        if (world == null) return;

        // 1. Hologram Stand
        titleStand = (ArmorStand) world.spawnEntity(center.clone().add(0, 1.5, 0), EntityType.ARMOR_STAND);
        titleStand.setVisible(false);
        titleStand.setGravity(false);
        titleStand.setMarker(true);
        titleStand.setCustomName("§6§l⚜ §e§lBACCARAT HIGH-ROLLER §6§l⚜ §7(Clic)");
        titleStand.setCustomNameVisible(true);

        // 2. Velvet Table Stand
        tableStand = (ArmorStand) world.spawnEntity(center.clone().add(0, -0.6, 0), EntityType.ARMOR_STAND);
        tableStand.setVisible(false);
        tableStand.setGravity(false);
        tableStand.setMarker(true);
        tableStand.getEquipment().setHelmet(new ItemStack(Material.RED_CARPET));

        // 3. VIP Croupier Dealer Stand (Behind table)
        Location dealerLoc = center.clone().add(0, -0.8, -1.0);
        dealerStand = (ArmorStand) world.spawnEntity(dealerLoc, EntityType.ARMOR_STAND);
        dealerStand.setVisible(false);
        dealerStand.setGravity(false);
        dealerStand.setMarker(true);

        ItemStack tux = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta meta = (LeatherArmorMeta) tux.getItemMeta();
        if (meta != null) {
            meta.setColor(Color.fromRGB(45, 10, 20)); // Deep Royal Burgundy
            tux.setItemMeta(meta);
        }
        dealerStand.getEquipment().setChestplate(tux);
        dealerStand.getEquipment().setHelmet(new ItemStack(Material.PLAYER_HEAD));
        dealerStand.getEquipment().setItemInMainHand(new ItemStack(Material.BOOK)); // Card Shoe / Rulebook

        // 4. Chip Stacks & Card Shoe on table
        ArmorStand chips1 = (ArmorStand) world.spawnEntity(center.clone().add(0.4, -0.7, 0.2), EntityType.ARMOR_STAND);
        chips1.setVisible(false);
        chips1.setGravity(false);
        chips1.setMarker(true);
        chips1.getEquipment().setHelmet(new ItemStack(Material.NETHER_STAR));
        decoStands.add(chips1);

        ArmorStand chips2 = (ArmorStand) world.spawnEntity(center.clone().add(-0.4, -0.7, 0.2), EntityType.ARMOR_STAND);
        chips2.setVisible(false);
        chips2.setGravity(false);
        chips2.setMarker(true);
        chips2.getEquipment().setHelmet(new ItemStack(Material.GOLD_INGOT));
        decoStands.add(chips2);

        startIdleAnimation();
    }

    private void startIdleAnimation() {
        if (idleTask != null) idleTask.cancel();

        idleTask = new BukkitRunnable() {
            double tick = 0;

            @Override
            public void run() {
                if (dealerStand == null || !dealerStand.isValid()) {
                    cancel();
                    return;
                }

                tick += 0.05;
                double yaw = Math.sin(tick) * 10;
                dealerStand.setHeadPose(new EulerAngle(Math.toRadians(12), Math.toRadians(yaw), 0));
                dealerStand.setRightArmPose(new EulerAngle(Math.toRadians(-25 + Math.sin(tick * 2) * 4), 0, 0));

                if (Math.random() < 0.1) {
                    center.getWorld().spawnParticle(Particle.CRIT, center.clone().add(0, 0.5, 0), 2, 0.2, 0.1, 0.2, 0.02);
                }
            }
        };
        idleTask.runTaskTimer(plugin, 0L, 2L);
    }

    public void remove() {
        if (idleTask != null) idleTask.cancel();
        if (titleStand != null && titleStand.isValid()) titleStand.remove();
        if (tableStand != null && tableStand.isValid()) tableStand.remove();
        if (dealerStand != null && dealerStand.isValid()) dealerStand.remove();
        for (ArmorStand d : decoStands) {
            if (d != null && d.isValid()) d.remove();
        }
        decoStands.clear();
    }

    public Location getCenter() {
        return center.clone();
    }

    public boolean isNearby(Location loc) {
        return center.getWorld() != null && loc.getWorld() != null &&
                center.getWorld().equals(loc.getWorld()) && center.distance(loc) <= 4.0;
    }
}
