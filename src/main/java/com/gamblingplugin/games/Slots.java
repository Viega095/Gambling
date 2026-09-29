package com.gamblingplugin.games;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.utils.ArmorStandUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Slots extends Game {

    private final Random random = new Random();
    private final Material[] symbols = {
            Material.DIAMOND, Material.EMERALD, Material.GOLD_INGOT,
            Material.IRON_INGOT, Material.COAL, Material.REDSTONE
    };

    public Slots(GamblingPlugin plugin) {
        super(plugin, "slots");
    }

    @Override
    public void play(Player player, double bet, String[] args) {
        // Check cooldown
        if (plugin.getCooldownManager().hasCooldown(player, "slots")) {
            long remaining = plugin.getCooldownManager().getRemainingCooldown(player, "slots");
            player.sendMessage("§c[VieGambling] Debes esperar " + (int) (remaining / 1000) + " segundos.");
            return;
        }

        if (!plugin.getEconomyManager().has(player, bet)) {
            player.sendMessage(plugin.getConfigManager().getMessage("insufficient-funds"));
            return;
        }

        plugin.getEconomyManager().withdraw(player, bet);

        // Contribute to jackpot
        plugin.getJackpotManager().contributeBet(bet);

        // Set cooldown (10 seconds)
        plugin.getCooldownManager().setCooldown(player, "slots");

        player.sendMessage(plugin.getConfigManager().getMessage("bet-placed")
                .replace("%amount%", String.valueOf(bet)));

        startAnimation(player, bet);
    }

    private void startAnimation(Player player, double bet) {
        Location baseLoc = player.getLocation().add(player.getLocation().getDirection().multiply(3));
        baseLoc.setY(player.getLocation().getY());

        // Create 3 reels
        List<ArmorStand> reels = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Location reelLoc = baseLoc.clone().add(i - 1, 0, 0); // Spaced out
            ArmorStand as = ArmorStandUtils.spawnInvisibleArmorStand(reelLoc);
            as.setCustomNameVisible(true);
            as.setCustomName("§6Spinning...");
            ArmorStandUtils.setHead(as, Material.BARRIER); // Placeholder
            reels.add(as);
        }

        new BukkitRunnable() {
            int ticks = 0;
            int[] results = new int[3];
            boolean[] stopped = new boolean[3];

            @Override
            public void run() {
                if (stopped[0] && stopped[1] && stopped[2]) {
                    this.cancel();
                    finalizeGame(player, bet, results);
                    reels.forEach(ArmorStand::remove);
                    return;
                }

                for (int i = 0; i < 3; i++) {
                    if (!stopped[i]) {
                        // Spin effect: Change head item rapidly
                        Material symbol = symbols[random.nextInt(symbols.length)];
                        ArmorStandUtils.setHead(reels.get(i), symbol);
                        reels.get(i).setHeadPose(reels.get(i).getHeadPose().add(0.5, 0, 0)); // Spin head

                        // Stop reels one by one
                        if (ticks > 40 + (i * 20)) {
                            stopped[i] = true;
                            results[i] = random.nextInt(symbols.length);
                            ArmorStandUtils.setHead(reels.get(i), symbols[results[i]]);
                            player.playSound(reels.get(i).getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                        }
                    }
                }

                if (ticks % 5 == 0) {
                    player.playSound(baseLoc, Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void finalizeGame(Player player, double bet, int[] results) {
        Material s1 = symbols[results[0]];
        Material s2 = symbols[results[1]];
        Material s3 = symbols[results[2]];

        player.sendMessage("");
        player.sendMessage("§6§l[VieGambling] §7Resultado:");
        player.sendMessage("§e  " + s1.name() + " - " + s2.name() + " - " + s3.name());
        player.sendMessage("");

        if (s1 == s2 && s2 == s3) {
            // Jackpot
            double payout = bet * 50;
            if (s1 == Material.DIAMOND)
                payout = bet * 100;

            plugin.getEconomyManager().deposit(player, payout);
            player.sendMessage("§a§l[VieGambling] ¡JACKPOT!");
            player.sendMessage("§7Premio: §a$" + String.format("%.2f", payout));
            player.sendMessage("");

            // Big win celebration
            Location loc = player.getLocation();
            player.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc.add(0, 2, 0), 100, 1, 1, 1, 0.1);
            player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 50, 1, 1, 1, 0);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1.5f);
        } else if (s1 == s2 || s2 == s3 || s1 == s3) {
            // Small win (2 matching)
            double payout = bet * 2;
            plugin.getEconomyManager().deposit(player, payout);
            player.sendMessage("§a§l[VieGambling] ¡Ganaste!");
            player.sendMessage("§7Premio: §a$" + String.format("%.2f", payout));
            player.sendMessage("");

            // Small win effects
            Location loc = player.getLocation();
            player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc.add(0, 2, 0), 20, 0.5, 0.5, 0.5, 0);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
        } else {
            player.sendMessage("§c§l[VieGambling] Perdiste");
            player.sendMessage("§7Mejor suerte la próxima vez");
            player.sendMessage("");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 0.8f);
        }

        // Try to win jackpot (regardless of game result)
        if (plugin.getJackpotManager().tryWinJackpot(player, bet)) {
            player.sendMessage("");
            player.sendMessage("§6§l¡BONUS! §7¡También ganaste el §6§lJACKPOT§7!");
            player.sendMessage("");
        }
    }
}
