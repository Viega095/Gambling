package com.gamblingplugin.gui;

import com.gamblingplugin.GamblingPlugin;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PlinkoBallSelectionGUI implements Listener {

    private final GamblingPlugin plugin;
    private final String mode;
    private final String TITLE_PREFIX = "§c§lPLINKO - ";
    private static final Map<String, Double> MODE_MAX_MULTIPLIERS = new HashMap<>();

    static {
        MODE_MAX_MULTIPLIERS.put("SAFE", 1.3);
        MODE_MAX_MULTIPLIERS.put("NORMAL", 10.0);
        MODE_MAX_MULTIPLIERS.put("EXTREME", 50.0);
    }

    public PlinkoBallSelectionGUI(GamblingPlugin plugin, String mode) {
        this.plugin = plugin;
        this.mode = mode;
    }

    public void open(Player player) {
        String title = TITLE_PREFIX + getModeName();
        Inventory inv = Bukkit.createInventory(null, 54, title);

        // Background
        ItemStack gray = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta grayMeta = gray.getItemMeta();
        grayMeta.setDisplayName(" ");
        gray.setItemMeta(grayMeta);

        for (int i = 0; i < 54; i++) {
            inv.setItem(i, gray);
        }

        // Ball options - 6 choices
        inv.setItem(10, createBallOption(1, 100));
        inv.setItem(12, createBallOption(3, 300));
        inv.setItem(14, createBallOption(5, 500));
        inv.setItem(16, createBallOption(10, 1000));
        inv.setItem(28, createBallOption(15, 1500));
        inv.setItem(30, createBallOption(20, 2000));

        // Mode info
        inv.setItem(4, createModeInfo());

        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§e← Volver a modos");
        back.setItemMeta(backMeta);
        inv.setItem(45, back);

        // Exit
        ItemStack exit = new ItemStack(Material.BARRIER);
        ItemMeta exitMeta = exit.getItemMeta();
        exitMeta.setDisplayName("§cCerrar");
        exit.setItemMeta(exitMeta);
        inv.setItem(53, exit);

        player.openInventory(inv);
    }

    private String getModeName() {
        switch (mode) {
            case "SAFE":
                return "§a🛡️ SEGURO";
            case "EXTREME":
                return "§c💀 EXTREMO";
            default:
                return "§e⚖️ NORMAL";
        }
    }

    private ItemStack createModeInfo() {
        ItemStack info = new ItemStack(getModeIcon());
        ItemMeta meta = info.getItemMeta();
        meta.setDisplayName(getModeName());

        switch (mode) {
            case "SAFE":
                meta.setLore(Arrays.asList(
                        "",
                        "§7Multiplicadores:",
                        "§e  1.3x §7(bordes)",
                        "§e  1.0x §7(medio)",
                        "§e  0.5x §7(centro)",
                        "",
                        "§a§lJUEGO SEGURO",
                        "§7No perderás mucho dinero"));
                break;
            case "NORMAL":
                meta.setLore(Arrays.asList(
                        "",
                        "§7Multiplicadores:",
                        "§6  10x §7(bordes extremos)",
                        "§a  5x §7(bordes)",
                        "§b  2x",
                        "§e  1x",
                        "§c  0.5x §7(centro)",
                        "",
                        "§e§lBALANCEADO"));
                break;
            case "EXTREME":
                meta.setLore(Arrays.asList(
                        "",
                        "§7Multiplicadores:",
                        "§6§l  50x §7(bordes extremos) §c★",
                        "§6  25x §7(bordes)",
                        "§a  10x",
                        "§e  2x",
                        "§c  0.2x §7(centro) §c¡80% PÉRDIDA!",
                        "",
                        "§c§l¡ALTÍSIMO RIESGO!"));
                break;
        }

        info.setItemMeta(meta);
        return info;
    }

    private Material getModeIcon() {
        switch (mode) {
            case "SAFE":
                return Material.SHIELD;
            case "EXTREME":
                return Material.NETHER_STAR;
            default:
                return Material.DIAMOND;
        }
    }

    private ItemStack createBallOption(int balls, double totalBet) {
        Material material = getBallMaterial(balls);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String ballText = balls == 1 ? "BOLA" : "BOLAS";
        meta.setDisplayName("§6§l" + balls + " " + ballText + " §8- §a$" + String.format("%.0f", totalBet));

        double betPerBall = totalBet / balls;
        double maxMulti = MODE_MAX_MULTIPLIERS.get(mode);

        meta.setLore(Arrays.asList(
                "",
                "§7Costo total: §a$" + String.format("%.0f", totalBet),
                "§7Por bola: §e$" + String.format("%.0f", betPerBall),
                "",
                "§7Caerán §e" + balls + " " + ballText.toLowerCase(),
                "",
                "§7Posibles resultados (" + getModeName() + "§7):",
                "§6  Mejor caso: §a$" + String.format("%.0f", totalBet * maxMulti),
                "§c  Peor caso: §c$" + String.format("%.0f", totalBet * getMinMultiplier()),
                "",
                "§eClick para jugar!"));

        item.setItemMeta(meta);
        return item;
    }

    private Material getBallMaterial(int balls) {
        switch (balls) {
            case 1:
                return Material.ENDER_PEARL;
            case 3:
                return Material.SNOWBALL;
            case 5:
                return Material.SLIME_BALL;
            case 10:
                return Material.FIRE_CHARGE;
            case 15:
                return Material.MAGMA_CREAM;
            case 20:
                return Material.GHAST_TEAR;
            default:
                return Material.SNOWBALL;
        }
    }

    private double getMinMultiplier() {
        switch (mode) {
            case "SAFE":
                return 0.5;
            case "EXTREME":
                return 0.2;
            default:
                return 0.5;
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith(TITLE_PREFIX)) {
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

        // Back button
        if (slot == 45) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.8f);
            PlinkoGUI mainGUI = new PlinkoGUI(plugin);
            mainGUI.open(player);
            return;
        }

        // Exit
        if (slot == 53) {
            player.closeInventory();
            return;
        }

        // Ball selection
        int balls = 0;
        double totalBet = 0;

        switch (slot) {
            case 10:
                balls = 1;
                totalBet = 100;
                break;
            case 12:
                balls = 3;
                totalBet = 300;
                break;
            case 14:
                balls = 5;
                totalBet = 500;
                break;
            case 16:
                balls = 10;
                totalBet = 1000;
                break;
            case 28:
                balls = 15;
                totalBet = 1500;
                break;
            case 30:
                balls = 20;
                totalBet = 2000;
                break;
            default:
                return;
        }

        // Check balance
        if (!plugin.getEconomyManager().has(player, totalBet)) {
            player.sendMessage(plugin.getConfigManager().getMessage("insufficient-funds"));
            return;
        }

        // Withdraw bet
        plugin.getEconomyManager().withdraw(player, totalBet);

        player.closeInventory();
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);

        // Start game
        double betPerBall = totalBet / balls;
        PlinkoAnimatedGUI animGUI = new PlinkoAnimatedGUI(plugin, mode);
        animGUI.playAnimation(player, betPerBall, balls);
    }
}
