package com.gamblingplugin.manager;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VIPManager {

    private final GamblingPlugin plugin;
    private File vipFile;
    private FileConfiguration vipConfig;

    public enum VIPTier {
        NONE("Normal", 0, 0.0, ChatColor.GRAY),
        BRONZE("Bronce", 1000, 0.01, ChatColor.GOLD),
        SILVER("Plata", 5000, 0.02, ChatColor.WHITE),
        GOLD("Oro", 25000, 0.035, ChatColor.YELLOW),
        PLATINUM("Platino", 100000, 0.05, ChatColor.AQUA),
        DIAMOND("Diamante", 500000, 0.075, ChatColor.DARK_AQUA),
        WHALE("Ballena (VIP Elite)", 2000000, 0.10, ChatColor.DARK_PURPLE);

        public final String displayName;
        public final double requiredWager;
        public final double rakebackRate;
        public final ChatColor color;

        VIPTier(String displayName, double requiredWager, double rakebackRate, ChatColor color) {
            this.displayName = displayName;
            this.requiredWager = requiredWager;
            this.rakebackRate = rakebackRate;
            this.color = color;
        }
    }

    private final Map<UUID, Double> totalWagered = new HashMap<>();
    private final Map<UUID, Double> claimableRakeback = new HashMap<>();

    public VIPManager(GamblingPlugin plugin) {
        this.plugin = plugin;
        loadVIPData();
    }

    private void loadVIPData() {
        vipFile = new File(plugin.getDataFolder(), "vip_data.yml");
        if (!vipFile.exists()) {
            try {
                vipFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        vipConfig = YamlConfiguration.loadConfiguration(vipFile);

        if (vipConfig.contains("players")) {
            for (String key : vipConfig.getConfigurationSection("players").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    totalWagered.put(uuid, vipConfig.getDouble("players." + key + ".wagered", 0));
                    claimableRakeback.put(uuid, vipConfig.getDouble("players." + key + ".rakeback", 0));
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    public void saveVIPData() {
        for (Map.Entry<UUID, Double> entry : totalWagered.entrySet()) {
            vipConfig.set("players." + entry.getKey() + ".wagered", entry.getValue());
            vipConfig.set("players." + entry.getKey() + ".rakeback", claimableRakeback.getOrDefault(entry.getKey(), 0.0));
        }
        try {
            vipConfig.save(vipFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void recordBet(Player player, double betAmount) {
        UUID uuid = player.getUniqueId();
        double currentWager = totalWagered.getOrDefault(uuid, 0.0) + betAmount;
        totalWagered.put(uuid, currentWager);

        VIPTier tier = getTier(player);
        if (tier.rakebackRate > 0) {
            double rakebackAdded = betAmount * tier.rakebackRate;
            claimableRakeback.put(uuid, claimableRakeback.getOrDefault(uuid, 0.0) + rakebackAdded);
        }
        saveVIPData();
    }

    public VIPTier getTier(Player player) {
        double wagered = totalWagered.getOrDefault(player.getUniqueId(), 0.0);
        VIPTier current = VIPTier.NONE;
        for (VIPTier t : VIPTier.values()) {
            if (wagered >= t.requiredWager) {
                current = t;
            }
        }
        return current;
    }

    public double getClaimableRakeback(Player player) {
        return claimableRakeback.getOrDefault(player.getUniqueId(), 0.0);
    }

    public double getTotalWagered(Player player) {
        return totalWagered.getOrDefault(player.getUniqueId(), 0.0);
    }

    public boolean claimRakeback(Player player) {
        UUID uuid = player.getUniqueId();
        double amount = claimableRakeback.getOrDefault(uuid, 0.0);
        if (amount <= 0.1) {
            player.sendMessage(ChatColor.RED + "✖ No tienes Rakeback acumulado para reclamar.");
            return false;
        }

        claimableRakeback.put(uuid, 0.0);
        plugin.getEconomyManager().deposit(player, amount);
        saveVIPData();

        player.sendMessage(ChatColor.GOLD + "✦ [VIP Rakeback] ¡Has reclamado " +
                ChatColor.GREEN + plugin.getEconomyManager().format(amount) + ChatColor.GOLD + "!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.6f);
        return true;
    }
}
