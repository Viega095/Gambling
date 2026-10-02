package com.gamblingplugin.structures;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Slots3DStructure {

    private final GamblingPlugin plugin;
    private final Location center;
    private ArmorStand holoStand;
    private ArmorStand leverStand;
    private final List<ArmorStand> reelStands = new ArrayList<>();
    private BukkitRunnable idleTask;
    private boolean isSpinning = false;

    private static final Material[] SYMBOLS = {
            Material.DIAMOND,
            Material.EMERALD,
            Material.GOLD_INGOT,
            Material.NETHER_STAR,
            Material.REDSTONE,
            Material.LAPIS_LAZULI,
            Material.AMETHYST_SHARD
    };

    public Slots3DStructure(GamblingPlugin plugin, Location center) {
        this.plugin = plugin;
        this.center = center.clone();
    }

    public void spawn() {
        World world = center.getWorld();
        if (world == null) return;

        // 1. Hologram Stand
        holoStand = (ArmorStand) world.spawnEntity(center.clone().add(0, 1.6, 0), EntityType.ARMOR_STAND);
        holoStand.setVisible(false);
        holoStand.setGravity(false);
        holoStand.setMarker(true);
        holoStand.setCustomName("§e§l🎰 TRAGAMONEDAS 3D 🎰");
        holoStand.setCustomNameVisible(true);

        // 2. Lever Stand on right side
        leverStand = (ArmorStand) world.spawnEntity(center.clone().add(0.6, 0.0, 0), EntityType.ARMOR_STAND);
        leverStand.setVisible(false);
        leverStand.setGravity(false);
        leverStand.setMarker(true);
        leverStand.getEquipment().setItemInMainHand(new ItemStack(Material.LEVER));
        leverStand.setRightArmPose(new EulerAngle(Math.toRadians(-20), 0, 0));

        // 3. Three Reel Stands (-0.45, 0, +0.45 X-offset)
        for (int i = 0; i < 3; i++) {
            Location reelLoc = center.clone().add((i - 1) * 0.45, -0.4, 0);
            ArmorStand reel = (ArmorStand) world.spawnEntity(reelLoc, EntityType.ARMOR_STAND);
            reel.setVisible(false);
            reel.setGravity(false);
            reel.setMarker(true);
            reel.getEquipment().setHelmet(new ItemStack(SYMBOLS[i % SYMBOLS.length]));
            reelStands.add(reel);
        }

        startIdleAnimation();
    }

    private void startIdleAnimation() {
        if (idleTask != null) idleTask.cancel();

        idleTask = new BukkitRunnable() {
            double tick = 0;

            @Override
            public void run() {
                if (isSpinning) return;
                if (holoStand == null || !holoStand.isValid()) {
                    cancel();
                    return;
                }

                tick += 0.05;
                // Subtle shine effect on reels
                for (int i = 0; i < reelStands.size(); i++) {
                    ArmorStand r = reelStands.get(i);
                    if (r != null && r.isValid()) {
                        double yaw = Math.sin(tick + (i * 1.5)) * 15;
                        r.setHeadPose(new EulerAngle(0, Math.toRadians(yaw), 0));
                    }
                }
            }
        };
        idleTask.runTaskTimer(plugin, 0L, 2L);
    }

    public void spin(Player player, double bet, Runnable callback) {
        if (isSpinning) {
            player.sendMessage("§c✖ La máquina tragamonedas ya está en uso.");
            return;
        }

        if (!plugin.getEconomyManager().has(player, bet)) {
            player.sendMessage("§c✖ No tienes suficiente dinero para apostar $" + bet);
            return;
        }

        plugin.getEconomyManager().withdraw(player, bet);
        plugin.getJackpotManager().contributeBet(bet);
        isSpinning = true;

        World world = center.getWorld();
        if (world == null) {
            isSpinning = false;
            return;
        }

        // Pull Lever animation
        world.playSound(center, Sound.BLOCK_LEVER_CLICK, 1f, 1f);
        if (leverStand != null && leverStand.isValid()) {
            leverStand.setRightArmPose(new EulerAngle(Math.toRadians(60), 0, 0));
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (leverStand != null && leverStand.isValid()) {
                    leverStand.setRightArmPose(new EulerAngle(Math.toRadians(-20), 0, 0));
                }
            }, 10L);
        }

        if (holoStand != null && holoStand.isValid()) {
            holoStand.setCustomName("§6⚡ §l¡GIRANDO RODILLOS 3D! §6⚡");
        }

        player.sendMessage("§6🎰 [Tragamonedas 3D] §7¡Palanca accionada con apuesta de §a$" + String.format("%.2f", bet) + "§7!");

        // Determine outcomes
        Material s1 = SYMBOLS[ThreadLocalRandom.current().nextInt(SYMBOLS.length)];
        Material s2 = SYMBOLS[ThreadLocalRandom.current().nextInt(SYMBOLS.length)];
        Material s3 = SYMBOLS[ThreadLocalRandom.current().nextInt(SYMBOLS.length)];

        new BukkitRunnable() {
            int tick = 0;
            boolean stop1 = false;
            boolean stop2 = false;
            boolean stop3 = false;

            @Override
            public void run() {
                tick++;

                if (holoStand == null || !holoStand.isValid()) {
                    cancel();
                    isSpinning = false;
                    return;
                }

                // Reel 1 Spin
                if (!stop1 && reelStands.size() > 0 && reelStands.get(0).isValid()) {
                    Material r = SYMBOLS[ThreadLocalRandom.current().nextInt(SYMBOLS.length)];
                    reelStands.get(0).getEquipment().setHelmet(new ItemStack(r));
                    reelStands.get(0).setHeadPose(new EulerAngle(Math.toRadians(tick * 35), 0, 0));
                }

                // Reel 2 Spin
                if (!stop2 && reelStands.size() > 1 && reelStands.get(1).isValid()) {
                    Material r = SYMBOLS[ThreadLocalRandom.current().nextInt(SYMBOLS.length)];
                    reelStands.get(1).getEquipment().setHelmet(new ItemStack(r));
                    reelStands.get(1).setHeadPose(new EulerAngle(Math.toRadians(tick * 40), 0, 0));
                }

                // Reel 3 Spin
                if (!stop3 && reelStands.size() > 2 && reelStands.get(2).isValid()) {
                    Material r = SYMBOLS[ThreadLocalRandom.current().nextInt(SYMBOLS.length)];
                    reelStands.get(2).getEquipment().setHelmet(new ItemStack(r));
                    reelStands.get(2).setHeadPose(new EulerAngle(Math.toRadians(tick * 45), 0, 0));
                }

                if (tick % 2 == 0) {
                    world.playSound(center, Sound.BLOCK_NOTE_BLOCK_HAT, 0.7f, 1.4f);
                }

                // Stop reel 1
                if (tick >= 22 && !stop1) {
                    stop1 = true;
                    if (reelStands.size() > 0 && reelStands.get(0).isValid()) {
                        reelStands.get(0).getEquipment().setHelmet(new ItemStack(s1));
                        reelStands.get(0).setHeadPose(new EulerAngle(0, 0, 0));
                        world.playSound(center, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.2f);
                    }
                }

                // Stop reel 2
                if (tick >= 34 && !stop2) {
                    stop2 = true;
                    if (reelStands.size() > 1 && reelStands.get(1).isValid()) {
                        reelStands.get(1).getEquipment().setHelmet(new ItemStack(s2));
                        reelStands.get(1).setHeadPose(new EulerAngle(0, 0, 0));
                        world.playSound(center, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
                    }
                }

                // Stop reel 3 & Finalize
                if (tick >= 46 && !stop3) {
                    stop3 = true;
                    cancel();
                    if (reelStands.size() > 2 && reelStands.get(2).isValid()) {
                        reelStands.get(2).getEquipment().setHelmet(new ItemStack(s3));
                        reelStands.get(2).setHeadPose(new EulerAngle(0, 0, 0));
                        world.playSound(center, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.8f);
                    }

                    evaluateResult(player, bet, s1, s2, s3, callback);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void evaluateResult(Player player, double bet, Material s1, Material s2, Material s3, Runnable callback) {
        World world = center.getWorld();
        if (world == null) return;

        if (s1 == s2 && s2 == s3) {
            // Triple match
            double mult = (s1 == Material.NETHER_STAR) ? 50.0 : ((s1 == Material.DIAMOND) ? 25.0 : 12.0);
            double prize = bet * mult;
            plugin.getEconomyManager().deposit(player, prize);

            world.playSound(center, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            world.playSound(center, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1.2f);
            world.spawnParticle(Particle.TOTEM, center.clone().add(0, 1.2, 0), 60, 0.6, 0.6, 0.6, 0.2);

            Bukkit.broadcastMessage("§6🎰💥 §l¡JACKPOT EN TRAGAMONEDAS 3D! §e" + player.getName() +
                    " §7sacó 3x §e" + s1.name() + " §7y ganó §a" + plugin.getEconomyManager().format(prize) + "!");

            player.sendTitle("§6🎰 ¡JACKPOT 3X! 🎰", "§a+" + plugin.getEconomyManager().format(prize), 10, 60, 15);

            if (holoStand != null && holoStand.isValid()) {
                holoStand.setCustomName("§6🏆 ¡JACKPOT 3X! §a+" + plugin.getEconomyManager().format(prize));
            }
        } else if (s1 == s2 || s2 == s3 || s1 == s3) {
            // Double match
            double prize = bet * 1.5;
            plugin.getEconomyManager().deposit(player, prize);

            world.playSound(center, Sound.ENTITY_PLAYER_LEVELUP, 0.9f, 1.4f);
            world.spawnParticle(Particle.VILLAGER_HAPPY, center.clone().add(0, 0.8, 0), 20, 0.4, 0.4, 0.4, 0.05);

            player.sendTitle("§a✨ ¡DOBLE COINCIDENCIA! ✨", "§a+" + plugin.getEconomyManager().format(prize), 5, 45, 10);
            player.sendMessage("§a🎰 [Tragamonedas 3D] ¡2 figuras iguales! Premio: §e" + plugin.getEconomyManager().format(prize));

            if (holoStand != null && holoStand.isValid()) {
                holoStand.setCustomName("§a✨ ¡GANASTE! §e+" + plugin.getEconomyManager().format(prize));
            }
        } else {
            // Loss
            world.playSound(center, Sound.BLOCK_ANVIL_LAND, 0.4f, 0.8f);
            player.sendTitle("§c✖ Sin Coincidencias", "§7¡Inténtalo de nuevo!", 5, 35, 10);
            player.sendMessage("§c🎰 [Tragamonedas 3D] No hubo suerte esta vez. ¡Vuelve a tirar de la palanca!");

            if (holoStand != null && holoStand.isValid()) {
                holoStand.setCustomName("§c✖ Suerte la próxima vez ✖");
            }
        }

        // Jackpot bonus check
        plugin.getJackpotManager().tryWinJackpot(player, bet);

        if (callback != null) callback.run();

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            isSpinning = false;
            if (holoStand != null && holoStand.isValid()) {
                holoStand.setCustomName("§e§l🎰 TRAGAMONEDAS 3D 🎰");
            }
        }, 80L);
    }

    public void remove() {
        if (idleTask != null) idleTask.cancel();
        if (holoStand != null && holoStand.isValid()) holoStand.remove();
        if (leverStand != null && leverStand.isValid()) leverStand.remove();
        for (ArmorStand r : reelStands) {
            if (r != null && r.isValid()) r.remove();
        }
        reelStands.clear();
    }

    public Location getCenter() {
        return center.clone();
    }

    public boolean isNearby(Location location) {
        return center.getWorld() != null && location.getWorld() != null &&
                center.getWorld().equals(location.getWorld()) &&
                center.distance(location) <= 4.0;
    }

    public boolean isSpinning() {
        return isSpinning;
    }
}
