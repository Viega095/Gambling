package com.gamblingplugin.manager;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CasinoVaultAndLoyalty implements Listener {

    public static class VaultHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    public enum VIPTier {
        BRONZE("§6Bronce", 0, 0.02, 0.015, Material.COPPER_INGOT),
        SILVER("§7Plata", 1000, 0.05, 0.025, Material.IRON_INGOT),
        GOLD("§eOro", 5000, 0.10, 0.035, Material.GOLD_INGOT),
        DIAMOND("§bDiamante", 25000, 0.15, 0.045, Material.DIAMOND),
        OBSIDIAN("§5Obsidiana Real", 100000, 0.25, 0.060, Material.CRYING_OBSIDIAN);

        public final String display;
        public final int pointsRequired;
        public final double cashbackPercent;
        public final double interestRate;
        public final Material icon;

        VIPTier(String display, int pointsRequired, double cashbackPercent, double interestRate, Material icon) {
            this.display = display;
            this.pointsRequired = pointsRequired;
            this.cashbackPercent = cashbackPercent;
            this.interestRate = interestRate;
            this.icon = icon;
        }
    }

    private final GamblingPlugin plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;

    private final Map<UUID, Double> vaultBalances = new HashMap<>();
    private final Map<UUID, Integer> loyaltyPoints = new HashMap<>();
    private final Map<UUID, Double> weeklyLosses = new HashMap<>();
    private final Map<UUID, Long> lastCashbackClaim = new HashMap<>();

    public CasinoVaultAndLoyalty(GamblingPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "vault_loyalty.yml");
        loadData();
    }

    public void loadData() {
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create vault_loyalty.yml");
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        if (dataConfig.contains("vault")) {
            for (String key : dataConfig.getConfigurationSection("vault").getKeys(false)) {
                try {
                    UUID u = UUID.fromString(key);
                    vaultBalances.put(u, dataConfig.getDouble("vault." + key));
                } catch (Exception ignored) {}
            }
        }
        if (dataConfig.contains("points")) {
            for (String key : dataConfig.getConfigurationSection("points").getKeys(false)) {
                try {
                    UUID u = UUID.fromString(key);
                    loyaltyPoints.put(u, dataConfig.getInt("points." + key));
                } catch (Exception ignored) {}
            }
        }
        if (dataConfig.contains("losses")) {
            for (String key : dataConfig.getConfigurationSection("losses").getKeys(false)) {
                try {
                    UUID u = UUID.fromString(key);
                    weeklyLosses.put(u, dataConfig.getDouble("losses." + key));
                } catch (Exception ignored) {}
            }
        }
    }

    public void saveData() {
        if (dataConfig == null) return;
        for (Map.Entry<UUID, Double> e : vaultBalances.entrySet()) {
            dataConfig.set("vault." + e.getKey().toString(), e.getValue());
        }
        for (Map.Entry<UUID, Integer> e : loyaltyPoints.entrySet()) {
            dataConfig.set("points." + e.getKey().toString(), e.getValue());
        }
        for (Map.Entry<UUID, Double> e : weeklyLosses.entrySet()) {
            dataConfig.set("losses." + e.getKey().toString(), e.getValue());
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save vault_loyalty.yml");
        }
    }

    public void recordBet(UUID uuid, double amount, boolean won) {
        int earnedPoints = (int) Math.max(1, amount / 50.0);
        loyaltyPoints.put(uuid, loyaltyPoints.getOrDefault(uuid, 0) + earnedPoints);
        if (!won) {
            weeklyLosses.put(uuid, weeklyLosses.getOrDefault(uuid, 0.0) + amount);
        }
        saveData();
    }

    public VIPTier getTier(UUID uuid) {
        int points = loyaltyPoints.getOrDefault(uuid, 0);
        VIPTier best = VIPTier.BRONZE;
        for (VIPTier t : VIPTier.values()) {
            if (points >= t.pointsRequired) {
                best = t;
            }
        }
        return best;
    }

    public void openVaultGUI(Player player) {
        Inventory inv = Bukkit.createInventory(new VaultHolder(), 45, "§8🏦 §eBóveda & Tarjeta VIP del Casino §8🏦");

        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta bMeta = border.getItemMeta();
        if (bMeta != null) {
            bMeta.setDisplayName(" ");
            border.setItemMeta(bMeta);
        }
        for (int i = 0; i < 45; i++) inv.setItem(i, border);

        UUID uuid = player.getUniqueId();
        VIPTier tier = getTier(uuid);
        int points = loyaltyPoints.getOrDefault(uuid, 0);
        double vBalance = vaultBalances.getOrDefault(uuid, 0.0);
        double losses = weeklyLosses.getOrDefault(uuid, 0.0);
        double availableCashback = losses * tier.cashbackPercent;

        // Slot 11: Tarjeta VIP
        ItemStack card = new ItemStack(tier.icon);
        ItemMeta cMeta = card.getItemMeta();
        if (cMeta != null) {
            cMeta.setDisplayName("§6💳 §lTarjeta de Membresía VIP: " + tier.display);
            cMeta.setLore(Arrays.asList(
                    "§7Puntos de Lealtad: §b" + String.format("%,d", points) + " pts",
                    "§7Cashback por Pérdidas: §a" + (int)(tier.cashbackPercent * 100) + "%",
                    "§7Interés Diario de Bóveda: §e" + (tier.interestRate * 100) + "%",
                    "",
                    "§e¡Apuesta en cualquier minijuego para subir de Rango VIP!"
            ));
            card.setItemMeta(cMeta);
        }
        inv.setItem(11, card);

        // Slot 13: Bóveda de Fichas
        ItemStack vault = new ItemStack(Material.CHEST);
        ItemMeta vMeta = vault.getItemMeta();
        if (vMeta != null) {
            vMeta.setDisplayName("§e🏦 §lCaja Fuerte de Depósito");
            vMeta.setLore(Arrays.asList(
                    "§7Saldo en Bóveda: §a$" + String.format("%,.2f", vBalance),
                    "",
                    "§e[Clic Izquierdo] §7Depositar $1,000",
                    "§e[Clic Derecho] §7Retirar TODO a tu bolsillo",
                    "§6[Shift + Clic] §7Depositar $10,000"
            ));
            vault.setItemMeta(vMeta);
        }
        inv.setItem(13, vault);

        // Slot 15: Cashback Semanal
        ItemStack cashItem = new ItemStack(Material.EMERALD);
        ItemMeta cmMeta = cashItem.getItemMeta();
        if (cmMeta != null) {
            cmMeta.setDisplayName("§a💰 §lReclamar Cashback Semanal");
            cmMeta.setLore(Arrays.asList(
                    "§7Pérdidas acumuladas esta semana: §c$" + String.format("%,.2f", losses),
                    "§7Cashback acumulado (" + (int)(tier.cashbackPercent * 100) + "%): §a$" + String.format("%,.2f", availableCashback),
                    "",
                    availableCashback > 0 ? "§a▶ ¡Haz clic para transferir a tu cuenta!" : "§7Sin cashback pendiente."
            ));
            cashItem.setItemMeta(cmMeta);
        }
        inv.setItem(15, cashItem);

        // Slot 31: Libro de Información
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta bkMeta = book.getItemMeta();
        if (bkMeta != null) {
            bkMeta.setDisplayName("§b📖 §lGuía de Beneficios VIP");
            bkMeta.setLore(Arrays.asList(
                    "§7• Bronce (0 pts): 2% Cashback | 1.5% Interés",
                    "§7• Plata (1,000 pts): 5% Cashback | 2.5% Interés",
                    "§7• Oro (5,000 pts): 10% Cashback | 3.5% Interés",
                    "§7• Diamante (25,000 pts): 15% Cashback | 4.5% Interés",
                    "§7• Obsidiana (100,000 pts): 25% Cashback | 6.0% Interés"
            ));
            book.setItemMeta(bkMeta);
        }
        inv.setItem(31, book);

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 1f, 1.2f);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof VaultHolder)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();
        int slot = event.getRawSlot();

        if (slot == 13) {
            // Deposit / Withdraw
            if (event.isRightClick()) {
                // Withdraw all
                double vBalance = vaultBalances.getOrDefault(uuid, 0.0);
                if (vBalance <= 0) {
                    player.sendMessage(ChatColor.RED + "✖ Tu bóveda no tiene fondos guardados.");
                    return;
                }
                vaultBalances.put(uuid, 0.0);
                saveData();
                plugin.getEconomyManager().deposit(player, vBalance);
                player.sendMessage(ChatColor.GREEN + "✔ Has retirado $" + String.format("%,.2f", vBalance) + " de la bóveda a tu bolsillo.");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.4f);
                openVaultGUI(player);
            } else if (event.isShiftClick()) {
                // Deposit 10,000
                depositToVault(player, 10000.0);
            } else {
                // Deposit 1,000
                depositToVault(player, 1000.0);
            }
        } else if (slot == 15) {
            // Claim Cashback
            VIPTier tier = getTier(uuid);
            double losses = weeklyLosses.getOrDefault(uuid, 0.0);
            double cashback = losses * tier.cashbackPercent;
            if (cashback <= 0) {
                player.sendMessage(ChatColor.RED + "✖ No tienes ningún monto de cashback disponible para reclamar.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }
            weeklyLosses.put(uuid, 0.0);
            saveData();
            plugin.getEconomyManager().deposit(player, cashback);
            player.sendMessage(ChatColor.GOLD + "✨ ¡Has reclamado $" + String.format("%,.2f", cashback) + " de cashback directo a tu cuenta!");
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f);
            openVaultGUI(player);
        }
    }

    private void depositToVault(Player player, double amount) {
        if (!plugin.getEconomyManager().has(player, amount)) {
            player.sendMessage(ChatColor.RED + "✖ No tienes $" + String.format("%,.2f", amount) + " en tu bolsillo para depositar.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }
        plugin.getEconomyManager().withdraw(player, amount);
        UUID u = player.getUniqueId();
        vaultBalances.put(u, vaultBalances.getOrDefault(u, 0.0) + amount);
        saveData();
        player.sendMessage(ChatColor.GREEN + "✔ Depositados $" + String.format("%,.2f", amount) + " en tu bóveda segura del casino.");
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1f, 1.2f);
        openVaultGUI(player);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof VaultHolder) {
            event.setCancelled(true);
        }
    }
}
