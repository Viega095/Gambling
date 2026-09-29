package com.gamblingplugin.games;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.utils.HeadUtils;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import java.util.function.Consumer;

public class DiceAnimation {

    private final GamblingPlugin plugin;

    public DiceAnimation(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public void play(Player player, int result, Consumer<Void> onFinish) {
        Location startLoc = player.getEyeLocation().subtract(0, 0.5, 0);
        Vector direction = player.getLocation().getDirection().multiply(0.8);

        ArmorStand dice = (ArmorStand) player.getWorld().spawnEntity(startLoc, EntityType.ARMOR_STAND);
        dice.setVisible(false);
        dice.setSmall(true);
        dice.setGravity(true); // Let it fall
        dice.getEquipment().setHelmet(HeadUtils.getDiceHead());
        // In a real scenario, we'd set a custom texture for the head based on the
        // result

        // Give it initial velocity
        dice.setVelocity(direction.add(new Vector(0, 0.5, 0))); // Arc up slightly

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks > 40 || dice.isOnGround()) {
                    this.cancel();
                    dice.remove();

                    // Play landing sound
                    player.playSound(dice.getLocation(), Sound.BLOCK_STONE_PLACE, 1f, 1f);

                    // Show result (maybe a hologram or just message)
                    if (onFinish != null)
                        onFinish.accept(null);
                    return;
                }

                // Rotate the head to simulate tumbling
                dice.setHeadPose(dice.getHeadPose().add(0.2, 0.2, 0.2));

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
