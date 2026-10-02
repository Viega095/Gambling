package com.gamblingplugin.structures;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;
import java.util.List;

public class VIPLoungeStructure {

    private final GamblingPlugin plugin;
    private final Location center;
    private ArmorStand barStand;
    private ArmorStand titleStand;
    private ArmorStand bartenderStand;
    private final List<ArmorStand> barProps = new ArrayList<>();
    private BukkitRunnable idleTask;

    public VIPLoungeStructure(GamblingPlugin plugin, Location center) {
        this.plugin = plugin;
        this.center = center.clone();
    }

    public void spawn() {
        World world = center.getWorld();
        if (world == null) return;

        // 1. Hologram Stand
        titleStand = (ArmorStand) world.spawnEntity(center.clone().add(0, 1.6, 0), EntityType.ARMOR_STAND);
        titleStand.setVisible(false);
        titleStand.setGravity(false);
        titleStand.setMarker(true);
        titleStand.setCustomName("§8🍸 §6§lVIP LOUNGE & BAR EXCLUSIVO §8🍸 §7(Clic)");
        titleStand.setCustomNameVisible(true);

        // 2. Polished Bar Counter Stand
        barStand = (ArmorStand) world.spawnEntity(center.clone().add(0, -0.6, 0), EntityType.ARMOR_STAND);
        barStand.setVisible(false);
        barStand.setGravity(false);
        barStand.setMarker(true);
        barStand.getEquipment().setHelmet(new ItemStack(Material.POLISHED_BLACKSTONE_SLAB));

        // 3. Bartender NPC Stand (Behind bar: Z - 1.0)
        Location bartenderLoc = center.clone().add(0, -0.8, -1.0);
        bartenderStand = (ArmorStand) world.spawnEntity(bartenderLoc, EntityType.ARMOR_STAND);
        bartenderStand.setVisible(false);
        bartenderStand.setGravity(false);
        bartenderStand.setMarker(true);

        ItemStack vest = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta meta = (LeatherArmorMeta) vest.getItemMeta();
        if (meta != null) {
            meta.setColor(Color.fromRGB(240, 240, 240)); // Crisp White Bartender Shirt
            vest.setItemMeta(meta);
        }
        bartenderStand.getEquipment().setChestplate(vest);
        bartenderStand.getEquipment().setHelmet(new ItemStack(Material.PLAYER_HEAD));
        bartenderStand.getEquipment().setItemInMainHand(new ItemStack(Material.POTION));

        // 4. Props: Brewing Stand & Cocktail Glass
        ArmorStand propBrew = (ArmorStand) world.spawnEntity(center.clone().add(-0.4, -0.7, 0.2), EntityType.ARMOR_STAND);
        propBrew.setVisible(false);
        propBrew.setGravity(false);
        propBrew.setMarker(true);
        propBrew.getEquipment().setHelmet(new ItemStack(Material.BREWING_STAND));
        barProps.add(propBrew);

        ArmorStand propGlass = (ArmorStand) world.spawnEntity(center.clone().add(0.4, -0.7, 0.2), EntityType.ARMOR_STAND);
        propGlass.setVisible(false);
        propGlass.setGravity(false);
        propGlass.setMarker(true);
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta pMeta = (PotionMeta) potion.getItemMeta();
        if (pMeta != null) {
            pMeta.setBasePotionData(new PotionData(PotionType.LUCK));
            potion.setItemMeta(pMeta);
        }
        propGlass.getEquipment().setHelmet(potion);
        barProps.add(propGlass);

        startIdleAnimation();
    }

    private void startIdleAnimation() {
        if (idleTask != null) idleTask.cancel();

        idleTask = new BukkitRunnable() {
            double tick = 0;

            @Override
            public void run() {
                if (bartenderStand == null || !bartenderStand.isValid()) {
                    cancel();
                    return;
                }

                tick += 0.05;
                double yaw = Math.sin(tick) * 15;
                bartenderStand.setHeadPose(new EulerAngle(Math.toRadians(10), Math.toRadians(yaw), 0));
                // Cocktail shaking animation with right arm
                bartenderStand.setRightArmPose(new EulerAngle(Math.toRadians(-40 + Math.sin(tick * 4) * 15), Math.toRadians(20), 0));

                if (Math.random() < 0.15) {
                    center.getWorld().spawnParticle(Particle.SPELL_MOB, center.clone().add(0, 0.6, 0), 2, 0.2, 0.1, 0.2, 0.05);
                }
            }
        };
        idleTask.runTaskTimer(plugin, 0L, 2L);
    }

    public void remove() {
        if (idleTask != null) idleTask.cancel();
        if (barStand != null && barStand.isValid()) barStand.remove();
        if (titleStand != null && titleStand.isValid()) titleStand.remove();
        if (bartenderStand != null && bartenderStand.isValid()) bartenderStand.remove();
        for (ArmorStand p : barProps) {
            if (p != null && p.isValid()) p.remove();
        }
        barProps.clear();
    }

    public Location getCenter() {
        return center.clone();
    }

    public boolean isNearby(Location loc) {
        return center.getWorld() != null && loc.getWorld() != null &&
                center.getWorld().equals(loc.getWorld()) && center.distance(loc) <= 4.0;
    }
}
