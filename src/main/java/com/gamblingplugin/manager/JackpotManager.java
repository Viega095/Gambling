package com.gamblingplugin.manager;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JackpotManager {

    private final GamblingPlugin plugin;
    private double jackpotAmount = 0;
    private final double contributionPercent = 0.01; // 1% of bets
    private final double winChance = 0.001; // 0.1% chance
    private List<String> recentWinners = new ArrayList<>();

    private File jackpotFile;
    private FileConfiguration jackpotConfig;

    public JackpotManager(GamblingPlugin plugin) {
        this.plugin = plugin;
        setupJackpotFile();
        loadJackpot();
    }

    private void setupJackpotFile() {
        jackpotFile = new File(plugin.getDataFolder(), "jackpot.yml");
        if (!jackpotFile.exists()) {
            try {
                jackpotFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create jackpot.yml!");
            }
        }
        jackpotConfig = YamlConfiguration.loadConfiguration(jackpotFile);
    }

    private void loadJackpot() {
        jackpotAmount = jackpotConfig.getDouble("jackpot-amount", 0.0);
        recentWinners = jackpotConfig.getStringList("recent-winners");
        plugin.getLogger().info("Loaded jackpot: $" + String.format("%.2f", jackpotAmount));
    }

    private void saveJackpot() {
        jackpotConfig.set("jackpot-amount", jackpotAmount);
        jackpotConfig.set("recent-winners", recentWinners);
        try {
            jackpotConfig.save(jackpotFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save jackpot.yml!");
        }
    }

    public void contributeBet(double betAmount) {
        double contribution = betAmount * contributionPercent;
        jackpotAmount += contribution;
        saveJackpot(); // Auto-save on contribution
    }

    public boolean tryWinJackpot(Player player, double betAmount) {
        // Must bet at least 100 to qualify
        if (betAmount < 100) {
            return false;
        }

        if (Math.random() < winChance) {
            payoutJackpot(player);
            return true;
        }
        return false;
    }

    private void payoutJackpot(Player player) {
        if (jackpotAmount <= 0) {
            return;
        }

        double winAmount = jackpotAmount;
        plugin.getEconomyManager().deposit(player, winAmount);

        // Add to recent winners (keep last 5)
        String winnerEntry = player.getName() + " - $" + String.format("%.2f", winAmount);
        recentWinners.add(0, winnerEntry);
        if (recentWinners.size() > 5) {
            recentWinners = recentWinners.subList(0, 5);
        }

        // Visual effects
        Location loc = player.getLocation();
        player.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        player.sendTitle("§6§l¡JACKPOT!", "§e$" + String.format("%.2f", winAmount), 10, 70, 20);

        // Spawn fireworks
        for (int i = 0; i < 3; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Firework fw = loc.getWorld().spawn(loc.clone().add(0, 1, 0), Firework.class);
                FireworkMeta meta = fw.getFireworkMeta();
                meta.addEffect(FireworkEffect.builder()
                        .withColor(org.bukkit.Color.YELLOW, org.bukkit.Color.ORANGE)
                        .with(FireworkEffect.Type.BALL_LARGE)
                        .withTrail()
                        .build());
                meta.setPower(1);
                fw.setFireworkMeta(meta);
            }, i * 10L);
        }

        // Broadcast win
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§6§l========= ¡JACKPOT! =========");
        Bukkit.broadcastMessage("§e§l" + player.getName() + " §7ha ganado el jackpot de §a§l$"
                + String.format("%.2f", winAmount) + "§7!");
        Bukkit.broadcastMessage("§7¡Felicitaciones al ganador!");
        Bukkit.broadcastMessage("§6§l=============================");
        Bukkit.broadcastMessage("");

        // Reset jackpot
        jackpotAmount = 0;
        saveJackpot();
    }

    public double getJackpotAmount() {
        return jackpotAmount;
    }

    public void setJackpotAmount(double amount) {
        this.jackpotAmount = amount;
        saveJackpot();
    }

    public void addToJackpot(double amount) {
        this.jackpotAmount += amount;
        saveJackpot();
    }

    public List<String> getRecentWinners() {
        return new ArrayList<>(recentWinners);
    }
}
