package com.gamblingplugin.gui;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.manager.StatsManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class StatsViewerGUI implements Listener {

    private final GamblingPlugin plugin;

    public StatsViewerGUI(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player viewer, Player target) {
        Inventory inv = Bukkit.createInventory(null, 54, "§6§lEstadísticas: §e" + target.getName());

        // TODO: Get real stats from StatsManager when methods are implemented
        // For now using placeholder values
        double totalBet = 0.0;
        double totalWon = 0.0;
        int gamesWon = 0;
        int totalGamesPlayed = 0;
        double biggestWin = 0.0;
        int currentStreak = 0;
        int longestStreak = 0;
        String favoriteGame = "N/A";

        // Player Head
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta headMeta = head.getItemMeta();
        headMeta.setDisplayName("§6§l" + target.getName());
        headMeta.setLore(Arrays.asList(
                "",
                "§7UUID: §f" + target.getUniqueId().toString().substring(0, 8) + "...",
                "§7Jugador desde: §aHace tiempo",
                ""));
        head.setItemMeta(headMeta);
        inv.setItem(4, head);

        // Total Bets
        ItemStack totalBets = createItem(Material.GOLD_INGOT, "§e💰 Total Apostado",
                "§7Cantidad total apostada:",
                "§6$" + String.format("%.2f", totalBet),
                "",
                "§7Indica cuánto has arriesgado en total");
        inv.setItem(11, totalBets);

        // Total Won
        ItemStack totalWon1 = createItem(Material.EMERALD, "§a💎 Total Ganado",
                "§7Cantidad total ganada:",
                "§a$" + String.format("%.2f", totalWon),
                "",
                "§7Todo lo que has ganado acumulado");
        inv.setItem(13, totalWon1);

        // Win Rate
        double winRate = totalGamesPlayed > 0 ? (gamesWon * 100.0 / totalGamesPlayed) : 0;
        ItemStack winRateItem = createItem(Material.DIAMOND, "§b📊 Tasa de Victoria",
                "§7Partidas ganadas: §a" + gamesWon,
                "§7Partidas totales: §e" + totalGamesPlayed,
                "",
                "§7Win Rate: §6" + String.format("%.1f%%", winRate),
                "",
                "§7Porcentaje de victorias");
        inv.setItem(15, winRateItem);

        // Net Profit
        double netProfit = totalWon - totalBet;
        String profitColor = netProfit >= 0 ? "§a" : "§c";
        String profitSymbol = netProfit >= 0 ? "+" : "";
        ItemStack profitItem = createItem(Material.NETHER_STAR, "§d💸 Beneficio Neto",
                "§7Ganancias - Apuestas:",
                profitColor + "$" + profitSymbol + String.format("%.2f", netProfit),
                "",
                netProfit >= 0 ? "§a§l¡En positivo!" : "§c§lEn negativo",
                "",
                "§7Tu balance total en el casino");
        inv.setItem(22, profitItem);

        // Biggest Win
        ItemStack biggestWin1 = createItem(Material.GOLD_BLOCK, "§6🏆 Mayor Victoria",
                "§7Tu victoria más grande:",
                "§a$" + String.format("%.2f", biggestWin),
                "",
                "§7¡Momento de gloria!");
        inv.setItem(29, biggestWin1);

        // Current Streak
        ItemStack streak = createItem(Material.FIRE_CHARGE, "§c⚡ Racha Actual",
                "§7Victorias consecutivas:",
                "§e" + currentStreak,
                "",
                "§7Racha máxima: §6" + longestStreak,
                "",
                "§7¡Sigue ganando!");
        inv.setItem(31, streak);

        // Favorite Game
        ItemStack favGame = createItem(Material.COMPASS, "§9🎲 Juego Favorito",
                "§7Juego más jugado:",
                "§e" + favoriteGame,
                "",
                "§7Basado en número de partidas");
        inv.setItem(33, favGame);

        // Close button
        ItemStack close = createItem(Material.BARRIER, "§cCerrar", "§7Click para cerrar");
        inv.setItem(49, close);

        // Info
        ItemStack info = createItem(Material.BOOK, "§7Información",
                "§eEstadísticas de " + target.getName(),
                "",
                "§7Estas son estadísticas completas",
                "§7de todas las partidas jugadas");
        inv.setItem(45, info);

        viewer.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player))
            return;
        Player player = (Player) e.getWhoClicked();

        if (!e.getView().getTitle().contains("Estadísticas"))
            return;

        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR)
            return;

        String displayName = clicked.getItemMeta().getDisplayName();

        if (displayName.contains("Cerrar")) {
            player.closeInventory();
        }
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }
}
