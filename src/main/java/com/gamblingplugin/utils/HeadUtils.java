package com.gamblingplugin.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class HeadUtils {

    // Texture values for custom heads (base64)
    public static final String DICE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjZkYWI3MjcxZjRmZjA0ZDU0MGU5ZmViMGY2YmQ2YzI3YWM2MjZhY2M1ZTkyYTk4NjdiNTNjNTYxOTYxZjFkNSJ9fX0=";

    public static ItemStack createPlayerHead(String playerName) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwner(playerName);
            head.setItemMeta(meta);
        }
        return head;
    }

    public static ItemStack createCustomHead(String name, String texture) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            // Note: Setting custom textures requires reflection or external libraries
            // For now, just use player names
            head.setItemMeta(meta);
        }
        return head;
    }

    public static ItemStack createDiceHead(int number) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Dice: " + number);
            head.setItemMeta(meta);
        }
        return head;
    }

    public static ItemStack getDiceHead() {
        return createDiceHead(1);
    }
}
