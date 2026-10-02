package com.gamblingplugin.structures;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;
import java.util.List;

public class LotteryStructure {

    private final GamblingPlugin plugin;
    private final Location center;
    private ArmorStand kioskBaseStand;
    private ArmorStand titleStand;
    private ArmorStand jackpotHoloStand;
    private final List<ArmorStand> swirlingTickets = new ArrayList<>();
    private BukkitRunnable animationTask;

    public LotteryStructure(GamblingPlugin plugin, Location center) {
        this.plugin = plugin;
        this.center = center.clone();
    }

    public void spawn() {
        World world = center.getWorld();
        if (world == null) return;

        // 1. Kiosk Base
        kioskBaseStand = (ArmorStand) world.spawnEntity(center.clone().add(0, -0.6, 0), EntityType.ARMOR_STAND);
        kioskBaseStand.setVisible(false);
        kioskBaseStand.setGravity(false);
        kioskBaseStand.setMarker(true);
        kioskBaseStand.getEquipment().setHelmet(new ItemStack(Material.GOLD_BLOCK));

        // 2. Title Hologram
        titleStand = (ArmorStand) world.spawnEntity(center.clone().add(0, 1.6, 0), EntityType.ARMOR_STAND);
        titleStand.setVisible(false);
        titleStand.setGravity(false);
        titleStand.setMarker(true);
        titleStand.setCustomName("§6§l🎫 LOTERÍA NACIONAL DEL CASINO §6§l🎫 §7(Clic)");
        titleStand.setCustomNameVisible(true);

        // 3. Live Jackpot Hologram
        jackpotHoloStand = (ArmorStand) world.spawnEntity(center.clone().add(0, 1.25, 0), EntityType.ARMOR_STAND);
        jackpotHoloStand.setVisible(false);
        jackpotHoloStand.setGravity(false);
        jackpotHoloStand.setMarker(true);
        jackpotHoloStand.setCustomName("§e💰 POZO ACUMULADO: §a§l$100,000+");
        jackpotHoloStand.setCustomNameVisible(true);

        // 4. Swirling Golden Ticket Stands (Vortex around center)
        for (int i = 0; i < 3; i++) {
            ArmorStand ticket = (ArmorStand) world.spawnEntity(center.clone().add(0, 0.2, 0), EntityType.ARMOR_STAND);
            ticket.setVisible(false);
            ticket.setGravity(false);
            ticket.setMarker(true);
            ticket.getEquipment().setHelmet(new ItemStack(Material.PAPER));
            swirlingTickets.add(ticket);
        }

        startAnimation();
    }

    private void startAnimation() {
        if (animationTask != null) animationTask.cancel();

        animationTask = new BukkitRunnable() {
            double angle = 0;

            @Override
            public void run() {
                angle += 0.08;
                if (angle >= 360) angle = 0;

                for (int i = 0; i < swirlingTickets.size(); i++) {
                    ArmorStand ticket = swirlingTickets.get(i);
                    if (ticket == null || !ticket.isValid()) {
                        cancel();
                        return;
                    }

                    double offsetAngle = angle + (i * (2 * Math.PI / swirlingTickets.size()));
                    double radius = 0.6;
                    double x = center.getX() + radius * Math.cos(offsetAngle);
                    double z = center.getZ() + radius * Math.sin(offsetAngle);
                    double y = center.getY() + 0.1 + Math.sin(angle * 2 + i) * 0.15;

                    Location tLoc = ticket.getLocation();
                    tLoc.setX(x);
                    tLoc.setY(y);
                    tLoc.setZ(z);
                    ticket.teleport(tLoc);
                    ticket.setHeadPose(new EulerAngle(Math.toRadians(angle * 40), Math.toRadians(angle * 60), 0));
                }

                if (Math.random() < 0.2) {
                    center.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, center.clone().add(0, 0.8, 0), 2, 0.3, 0.2, 0.3, 0.05);
                }
            }
        };
        animationTask.runTaskTimer(plugin, 0L, 2L);
    }

    public void remove() {
        if (animationTask != null) animationTask.cancel();
        if (kioskBaseStand != null && kioskBaseStand.isValid()) kioskBaseStand.remove();
        if (titleStand != null && titleStand.isValid()) titleStand.remove();
        if (jackpotHoloStand != null && jackpotHoloStand.isValid()) jackpotHoloStand.remove();
        for (ArmorStand t : swirlingTickets) {
            if (t != null && t.isValid()) t.remove();
        }
        swirlingTickets.clear();
    }

    public Location getCenter() {
        return center.clone();
    }

    public boolean isNearby(Location loc) {
        return center.getWorld() != null && loc.getWorld() != null &&
                center.getWorld().equals(loc.getWorld()) && center.distance(loc) <= 4.0;
    }
}
