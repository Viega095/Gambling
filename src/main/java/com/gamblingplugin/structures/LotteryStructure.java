package com.gamblingplugin.structures;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class LotteryStructure {

    private final GamblingPlugin plugin;
    private final Location center;
    private ArmorStand kioskStand;
    private ArmorStand titleStand;

    public LotteryStructure(GamblingPlugin plugin, Location center) {
        this.plugin = plugin;
        this.center = center.clone();
    }

    public void spawn() {
        World world = center.getWorld();
        if (world == null) return;

        kioskStand = (ArmorStand) world.spawnEntity(center.clone().add(0, -0.9, 0), EntityType.ARMOR_STAND);
        kioskStand.setVisible(false);
        kioskStand.setGravity(false);
        kioskStand.setHelmet(new ItemStack(Material.GOLD_BLOCK));

        titleStand = (ArmorStand) world.spawnEntity(center.clone().add(0, 0.6, 0), EntityType.ARMOR_STAND);
        titleStand.setVisible(false);
        titleStand.setGravity(false);
        titleStand.setCustomName("§6§l🎫 LOTERÍA NACIONAL §7(Clic)");
        titleStand.setCustomNameVisible(true);
        titleStand.setHelmet(new ItemStack(Material.PAPER));
    }

    public void remove() {
        if (kioskStand != null && kioskStand.isValid()) kioskStand.remove();
        if (titleStand != null && titleStand.isValid()) titleStand.remove();
    }

    public Location getCenter() {
        return center.clone();
    }

    public boolean isNearby(Location loc) {
        return center.getWorld().equals(loc.getWorld()) && center.distance(loc) <= 3.5;
    }
}
