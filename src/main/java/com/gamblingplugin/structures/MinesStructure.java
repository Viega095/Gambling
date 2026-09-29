package com.gamblingplugin.structures;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

public class MinesStructure {

    private final GamblingPlugin plugin;
    private final Location center;
    private ArmorStand minesStand;
    private BukkitRunnable rotationTask;

    public MinesStructure(GamblingPlugin plugin, Location center) {
        this.plugin = plugin;
        this.center = center.clone();
    }

    public void spawn() {
        // Create mines armor stand
        minesStand = (ArmorStand) center.getWorld().spawnEntity(center.clone().add(0, -1, 0), EntityType.ARMOR_STAND);
        minesStand.setVisible(false);
        minesStand.setGravity(false);
        minesStand.setCustomName("§c§l💣 §4§lMINES §c§l💥");
        minesStand.setCustomNameVisible(true);
        minesStand.setMarker(true);

        // TNT as mines block
        ItemStack minesBlock = new ItemStack(Material.TNT);
        minesStand.getEquipment().setHelmet(minesBlock);

        // Start rotation animation
        startAnimation();
    }

    private void startAnimation() {
        rotationTask = new BukkitRunnable() {
            double angle = 0;
            int colorCycle = 0;

            @Override
            public void run() {
                if (minesStand == null || !minesStand.isValid()) {
                    this.cancel();
                    return;
                }

                // Slow rotation
                angle += 0.12;
                if (angle >= 360)
                    angle = 0;

                EulerAngle headPose = new EulerAngle(0, Math.toRadians(angle), 0);
                minesStand.setHeadPose(headPose);

                // Change between explosive blocks every 35 ticks
                colorCycle++;
                if (colorCycle >= 35) {
                    colorCycle = 0;
                    Material[] mineBlocks = {
                            Material.TNT,
                            Material.REDSTONE_BLOCK,
                            Material.RED_CONCRETE,
                            Material.LAVA_BUCKET
                    };
                    Material nextBlock = mineBlocks[(int) (angle / 90) % mineBlocks.length];
                    minesStand.getEquipment().setHelmet(new ItemStack(nextBlock));
                }
            }
        };
        rotationTask.runTaskTimer(plugin, 0L, 2L);
    }

    public void remove() {
        if (rotationTask != null) {
            rotationTask.cancel();
        }
        if (minesStand != null && minesStand.isValid()) {
            minesStand.remove();
        }
    }

    public Location getCenter() {
        return center.clone();
    }

    public boolean isNearby(Location location) {
        return center.getWorld().equals(location.getWorld()) &&
                center.distance(location) <= 4;
    }

    // Compatibility methods for game logic
    public void playExplosionEffect() {
        // Play explosion effect at center
        if (center.getWorld() != null) {
            center.getWorld().createExplosion(center, 0f, false, false);
        }
    }

    public ArmorStand getDisplayStand() {
        return minesStand;
    }
}
