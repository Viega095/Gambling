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

public class BlackjackStructure {

    private final GamblingPlugin plugin;
    private final Location center;
    private ArmorStand holoStand;
    private ArmorStand tableStand;
    private ArmorStand dealerStand;
    private final List<ArmorStand> decoStands = new ArrayList<>();
    private BukkitRunnable idleTask;

    public BlackjackStructure(GamblingPlugin plugin, Location center) {
        this.plugin = plugin;
        this.center = center.clone();
    }

    public void spawn() {
        World world = center.getWorld();
        if (world == null) return;

        // 1. Hologram Stand
        holoStand = (ArmorStand) world.spawnEntity(center.clone().add(0, 1.5, 0), EntityType.ARMOR_STAND);
        holoStand.setVisible(false);
        holoStand.setGravity(false);
        holoStand.setMarker(true);
        holoStand.setCustomName("§0§l♠ §c§lMESA DE BLACKJACK 21 §0§l♥");
        holoStand.setCustomNameVisible(true);

        // 2. Green Felt Table Stand
        tableStand = (ArmorStand) world.spawnEntity(center.clone().add(0, -0.6, 0), EntityType.ARMOR_STAND);
        tableStand.setVisible(false);
        tableStand.setGravity(false);
        tableStand.setMarker(true);
        tableStand.getEquipment().setHelmet(new ItemStack(Material.GREEN_CONCRETE));

        // 3. Dealer Stand (Behind table: Z - 1.0)
        Location dealerLoc = center.clone().add(0, -0.8, -1.0);
        dealerStand = (ArmorStand) world.spawnEntity(dealerLoc, EntityType.ARMOR_STAND);
        dealerStand.setVisible(false);
        dealerStand.setGravity(false);
        dealerStand.setMarker(true);

        ItemStack dealerTux = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta meta = (LeatherArmorMeta) dealerTux.getItemMeta();
        if (meta != null) {
            meta.setColor(Color.fromRGB(20, 20, 20));
            dealerTux.setItemMeta(meta);
        }
        dealerStand.getEquipment().setChestplate(dealerTux);
        dealerStand.getEquipment().setHelmet(new ItemStack(Material.PLAYER_HEAD));
        dealerStand.getEquipment().setItemInMainHand(new ItemStack(Material.PAPER)); // Cards

        // 4. Chip stacks (Gold & Emerald nuggets on table)
        ArmorStand chips1 = (ArmorStand) world.spawnEntity(center.clone().add(0.4, -0.7, 0.2), EntityType.ARMOR_STAND);
        chips1.setVisible(false);
        chips1.setGravity(false);
        chips1.setMarker(true);
        chips1.getEquipment().setHelmet(new ItemStack(Material.GOLD_NUGGET));
        decoStands.add(chips1);

        ArmorStand chips2 = (ArmorStand) world.spawnEntity(center.clone().add(-0.4, -0.7, 0.2), EntityType.ARMOR_STAND);
        chips2.setVisible(false);
        chips2.setGravity(false);
        chips2.setMarker(true);
        chips2.getEquipment().setHelmet(new ItemStack(Material.EMERALD));
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
                // Dealer gentle breathing / looking around
                double yaw = Math.sin(tick) * 12;
                dealerStand.setHeadPose(new EulerAngle(Math.toRadians(10), Math.toRadians(yaw), 0));
                dealerStand.setRightArmPose(new EulerAngle(Math.toRadians(-20 + Math.sin(tick * 2) * 5), 0, 0));
            }
        };
        idleTask.runTaskTimer(plugin, 0L, 2L);
    }

    public void remove() {
        if (idleTask != null) idleTask.cancel();
        if (holoStand != null && holoStand.isValid()) holoStand.remove();
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

    public boolean isNearby(Location location) {
        return center.getWorld() != null && location.getWorld() != null &&
                center.getWorld().equals(location.getWorld()) &&
                center.distance(location) <= 4.0;
    }
}
