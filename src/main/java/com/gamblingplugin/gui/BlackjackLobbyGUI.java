package com.gamblingplugin.gui;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.games.blackjack.BlackjackManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class BlackjackLobbyGUI implements Listener {

    private final GamblingPlugin plugin;
    private static final String TITLE = "§0§lBLACKJACK - Elige apuesta";
    private BlackjackManager.BlackjackLobby activeLobby;

    public BlackjackLobbyGUI(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        // Background
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < 27; i++) {
            inv.setItem(i, glass);
        }

        // Bet options
        inv.setItem(10, createBetItem(100, Material.IRON_INGOT));
        inv.setItem(11, createBetItem(500, Material.GOLD_INGOT));
        inv.setItem(12, createBetItem(1000, Material.DIAMOND));
        inv.setItem(13, createBetItem(2500, Material.EMERALD));
        inv.setItem(14, createBetItem(5000, Material.NETHER_STAR));

        // Info
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName("§6§l🃏 BLACKJACK");
        infoMeta.setLore(Arrays.asList(
                "",
                "§7Objetivo: Llegar a §e21 §7sin pasarte",
                "",
                "§7Reglas:",
                "§7• Blackjack (21 en 2 cartas): §a1.5x",
                "§7• Ganar: §a1x",
                "§7• Empate: Devuelve apuesta",
                "§7• Bust/Perder: Pierdes apuesta",
                "",
                "§7Dealer: Hit ≤16, Stand ≥17",
                "§7Jugadores: 1-4 simultáneos",
                "§7Tiempo de espera: §e5 segundos",
                "",
                "§eElige tu apuesta!"));
        info.setItemMeta(infoMeta);
        inv.setItem(4, info);

        // Exit
        ItemStack exit = new ItemStack(Material.BARRIER);
        ItemMeta exitMeta = exit.getItemMeta();
        exitMeta.setDisplayName("§cCerrar");
        exit.setItemMeta(exitMeta);
        inv.setItem(26, exit);

        player.openInventory(inv);
    }

    private ItemStack createBetItem(double amount, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6§lAPUESTA: §a$" + String.format("%.0f", amount));
        meta.setLore(Arrays.asList(
                "",
                "§7Click para apostar §a$" + String.format("%.0f", amount),
                "",
                "§7Posibles ganancias:",
                "§6  Blackjack: §a$" + String.format("%.0f", amount * 1.5),
                "§a  Ganar: §a$" + String.format("%.0f", amount),
                "",
                "§7Esperarás §e5 segundos §7para que",
                "§7otros jugadores se unan (máx 4)",
                "",
                "§eClick para jugar!"));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(TITLE)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (event.getCurrentItem() == null) {
            return;
        }

        int slot = event.getSlot();

        // Exit
        if (slot == 26) {
            player.closeInventory();
            return;
        }

        // Bet selection
        double bet = 0;
        switch (slot) {
            case 10:
                bet = 100;
                break;
            case 11:
                bet = 500;
                break;
            case 12:
                bet = 1000;
                break;
            case 13:
                bet = 2500;
                break;
            case 14:
                bet = 5000;
                break;
            default:
                return;
        }

        // Check balance
        if (!plugin.getEconomyManager().has(player, bet)) {
            player.sendMessage(plugin.getConfigManager().getMessage("insufficient-funds"));
            return;
        }

        // Withdraw bet
        plugin.getEconomyManager().withdraw(player, bet);

        player.closeInventory();
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);

        // Create lobby and start countdown
        final double finalBet = bet;
        activeLobby = plugin.getBlackjackManager().createLobby(player, finalBet);

        player.sendMessage("");
        player.sendMessage("§6§l[BLACKJACK] §7Esperando jugadores...");
        player.sendMessage("§7Otros jugadores tienen §e5 segundos §7para unirse.");
        player.sendMessage("");

        // Countdown task
        new BukkitRunnable() {
            int countdown = 5;

            @Override
            public void run() {
                if (activeLobby == null) {
                    this.cancel();
                    return;
                }

                countdown--;

                if (countdown <= 0) {
                    this.cancel();
                    // Start game
                    plugin.getBlackjackManager().startGame(activeLobby);

                    // Open game GUI for all players
                    for (Player p : activeLobby.getPlayers().keySet()) {
                        BlackjackGameGUI gameGUI = new BlackjackGameGUI(plugin);
                        gameGUI.openGame(p);
                        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f);
                    }

                    activeLobby = null;
                } else {
                    for (Player p : activeLobby.getPlayers().keySet()) {
                        p.sendMessage("§7Iniciando en §e" + countdown + "§7...");
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1f);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }
}
