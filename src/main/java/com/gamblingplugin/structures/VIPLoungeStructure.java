package com.gamblingplugin.structures;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class VIPLoungeStructure {

    private final GamblingPlugin plugin;
    private final Location center;
    private ArmorStand barStand;
    private ArmorStand titleStand;

    public VIPLoungeStructure(GamblingPlugin plugin, Location center) {
        this.plugin = plugin;
        this.center = center.clone();
    }

    public void spawn() {
        World world = center.getWorld();
        if (world == null) return;

        barStand = (ArmorStand) world.spawnEntity(center.clone().add(0, -0.9, 0), EntityType.ARMOR_STAND);
        barStand.setVisible(false);
        barStand.setGravity(false);
        barStand.setHelmet(new ItemStack(Material.BREWING_STAND));

        titleStand = (ArmorStand) world.spawnEntity(center.clone().add(0, 0.6, 0), EntityType.ARMOR_STAND);
        titleStand.setVisible(false);
        titleStand.setGravity(false);
        titleStand.setCustomName("§8🍸 §6§lVIP BAR & LOUNGE §8🍸 §7(Clic)");
        titleStand.setCustomNameVisible(true);
        titleStand.setHelmet(new ItemStack(Material.POTION));
    }

    public void remove() {
        if (barStand != null && barStand.isValid()) barStand.remove();
        if (titleStand != null && titleStand.isValid()) titleStand.remove();
    }

    public Location getCenter() {
        return center.clone();
    }

    public boolean isNearby(Location loc) {
        return center.getWorld().equals(loc.getWorld()) && center.distance(loc) <= 3.5;
    }
}
