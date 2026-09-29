package com.gamblingplugin.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class ArmorStandUtils {

    public static ArmorStand createArmorStand(Location location, String name, boolean visible) {
        ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        stand.setCustomName(name);
        stand.setCustomNameVisible(true);
        stand.setVisible(visible);
        stand.setGravity(false);
        stand.setMarker(true);
        return stand;
    }

    public static void setHelmet(ArmorStand stand, ItemStack item) {
        if (stand != null && stand.isValid()) {
            stand.getEquipment().setHelmet(item);
        }
    }

    public static void remove(ArmorStand stand) {
        if (stand != null && stand.isValid()) {
            stand.remove();
        }
    }

    public static ArmorStand spawnInvisibleArmorStand(Location location) {
        return createArmorStand(location, "", false);
    }

    public static void setHead(ArmorStand stand, Material material) {
        if (stand != null && stand.isValid()) {
            stand.getEquipment().setHelmet(new ItemStack(material));
        }
    }
}
