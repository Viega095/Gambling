package com.gamblingplugin.manager;

import com.gamblingplugin.GamblingPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyManager {

    private final GamblingPlugin plugin;
    private Economy economy;
    private final Map<UUID, Double> fallbackBalances = new HashMap<>();

    public EconomyManager(GamblingPlugin plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    public boolean setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().info("Vault no detectado: VieGambling operando con balance local de prueba.");
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().info("Esperando proveedor de economía Vault...");
            return false;
        }

        economy = rsp.getProvider();
        if (economy != null) {
            plugin.getLogger().info("✔ Hooked into Vault economy provider: " + economy.getName() + " (Unified server wallet)");
        }
        return economy != null;
    }

    public Economy getEconomy() {
        if (economy == null) {
            setupEconomy();
        }
        return economy;
    }

    public boolean hasAccount(Player player) {
        Economy eco = getEconomy();
        return eco != null ? eco.hasAccount(player) : true;
    }

    public double getBalance(Player player) {
        if (player == null) return 0.0;
        Economy eco = getEconomy();
        return eco != null ? eco.getBalance(player) : fallbackBalances.getOrDefault(player.getUniqueId(), 1000.0);
    }

    public boolean has(Player player, double amount) {
        if (player == null) return false;
        Economy eco = getEconomy();
        return eco != null ? eco.has(player, amount) : getBalance(player) >= amount;
    }

    public void withdraw(Player player, double amount) {
        if (player == null || amount <= 0) return;
        Economy eco = getEconomy();
        if (eco != null) {
            eco.withdrawPlayer(player, amount);
        } else {
            fallbackBalances.put(player.getUniqueId(), Math.max(0, getBalance(player) - amount));
        }
    }

    public void deposit(Player player, double amount) {
        if (player == null || amount <= 0) return;
        Economy eco = getEconomy();
        if (eco != null) {
            eco.depositPlayer(player, amount);
        } else {
            fallbackBalances.put(player.getUniqueId(), getBalance(player) + amount);
        }
    }

    public void deposit(UUID uuid, double amount) {
        if (uuid == null || amount <= 0) return;
        Economy eco = getEconomy();
        if (eco != null) {
            org.bukkit.OfflinePlayer offline = org.bukkit.Bukkit.getOfflinePlayer(uuid);
            eco.depositPlayer(offline, amount);
        } else {
            fallbackBalances.put(uuid, fallbackBalances.getOrDefault(uuid, 1000.0) + amount);
        }
    }

    public boolean hasEnough(Player player, double amount) {
        return has(player, amount);
    }

    public String format(double amount) {
        Economy eco = getEconomy();
        return eco != null ? eco.format(amount) : ("$" + String.format("%.2f", amount));
    }
}
