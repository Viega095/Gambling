package com.gamblingplugin.manager;

import com.gamblingplugin.GamblingPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {

    private final GamblingPlugin plugin;
    private Economy economy;

    public EconomyManager(GamblingPlugin plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    private boolean setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().severe("Vault plugin not found!");
            return false;
        }
        plugin.getLogger().info("Vault plugin found, attempting to hook into economy...");

        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger()
                    .severe("No economy provider found! Make sure your economy plugin is installed and loaded.");
            plugin.getLogger().severe("Available plugins: " + String.join(", ",
                    plugin.getServer().getPluginManager().getPlugins().length > 0
                            ? java.util.Arrays.stream(plugin.getServer().getPluginManager().getPlugins())
                                    .map(p -> p.getName())
                                    .toArray(String[]::new)
                            : new String[] { "none" }));
            return false;
        }

        economy = rsp.getProvider();
        if (economy != null) {
            plugin.getLogger().info("Successfully hooked into economy provider: " + economy.getName());
        } else {
            plugin.getLogger().severe("Economy provider returned null!");
        }
        return economy != null;
    }

    public boolean hasAccount(Player player) {
        return economy != null && economy.hasAccount(player);
    }

    public double getBalance(Player player) {
        return economy != null ? economy.getBalance(player) : 0;
    }

    public boolean has(Player player, double amount) {
        return economy != null && economy.has(player, amount);
    }

    public void withdraw(Player player, double amount) {
        if (economy != null)
            economy.withdrawPlayer(player, amount);
    }

    public void deposit(Player player, double amount) {
        if (economy != null)
            economy.depositPlayer(player, amount);
    }

    public boolean hasEnough(Player player, double amount) {
        return has(player, amount);
    }

    public String format(double amount) {
        return economy != null ? economy.format(amount) : ("$" + String.format("%.2f", amount));
    }

    public Economy getEconomy() {
        return economy;
    }
}
