package com.gamblingplugin.engine;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

public class DisplayEntityEngine {

    private final GamblingPlugin plugin;

    public DisplayEntityEngine(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public BlockDisplay spawnBlockDisplay(Location loc, Material material, Vector3f scale) {
        BlockDisplay display = (BlockDisplay) loc.getWorld().spawnEntity(loc, EntityType.BLOCK_DISPLAY);
        display.setBlock(material.createBlockData());
        display.setTransformation(new Transformation(
                new Vector3f(-scale.x / 2f, 0, -scale.z / 2f),
                new AxisAngle4f(0, 0, 0, 1),
                scale,
                new AxisAngle4f(0, 0, 0, 1)
        ));
        display.setInterpolationDuration(2);
        display.setInterpolationDelay(0);
        return display;
    }

    public ItemDisplay spawnItemDisplay(Location loc, ItemStack item, Vector3f scale) {
        ItemDisplay display = (ItemDisplay) loc.getWorld().spawnEntity(loc, EntityType.ITEM_DISPLAY);
        display.setItemStack(item);
        display.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),
                new AxisAngle4f(0, 0, 0, 1),
                scale,
                new AxisAngle4f(0, 0, 0, 1)
        ));
        display.setBillboard(Display.Billboard.FIXED);
        display.setInterpolationDuration(2);
        display.setInterpolationDelay(0);
        return display;
    }

    public TextDisplay spawnTextDisplay(Location loc, String text, boolean seeThrough) {
        TextDisplay display = (TextDisplay) loc.getWorld().spawnEntity(loc, EntityType.TEXT_DISPLAY);
        display.setText(text);
        display.setBillboard(Display.Billboard.CENTER);
        display.setSeeThrough(seeThrough);
        display.setBackgroundColor(Color.fromARGB(120, 0, 0, 0));
        display.setShadowed(true);
        return display;
    }

    public Interaction spawnInteraction(Location loc, float width, float height) {
        Interaction interaction = (Interaction) loc.getWorld().spawnEntity(loc, EntityType.INTERACTION);
        interaction.setInteractionWidth(width);
        interaction.setInteractionHeight(height);
        interaction.setResponsive(true);
        return interaction;
    }
}
