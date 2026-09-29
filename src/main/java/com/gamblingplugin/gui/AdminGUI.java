package com.gamblingplugin.gui;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class AdminGUI implements Listener {

    private final GamblingPlugin plugin;

    public AdminGUI(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        if (!player.hasPermission("gambling.admin")) {
            player.sendMessage("§c[VieGambling] No tienes permiso para usar esto.");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, "§c§lGAMBLING ADMIN");

        // Structure Management
        ItemStack structures = createItem(Material.BEACON, "§6Gestión de Estructuras",
                "§7Click para administrar",
                "§7todas las estructuras del servidor",
                "",
                "§e▸ Ver todas",
                "§e▸ Teleportarse",
                "§e▸ Eliminar en masa");
        inv.setItem(10, structures);

        // Player Management
        ItemStack players = createItem(Material.PLAYER_HEAD, "§aGestión de Jugadores",
                "§7Administra jugadores y balances",
                "",
                "§e▸ Ver balances",
                "§e▸ Añadir/quitar dinero",
                "§e▸ Resetear estadísticas",
                "§e▸ Ver historial");
        inv.setItem(12, players);

        // Statistics
        ItemStack stats = createItem(Material.BOOK, "§bEstadísticas del Servidor",
                "§7Vista general del servidor",
                "",
                "§e▸ Total apostado",
                "§e▸ Total ganado",
                "§e▸ Juego más popular",
                "§e▸ Mayor ganancia");
        inv.setItem(14, stats);

        // Config Editor
        ItemStack config = createItem(Material.COMPARATOR, "§dEditor de Configuración",
                "§7Modifica ajustes en vivo",
                "",
                "§e▸ Toggle efectos de partículas",
                "§e▸ Toggle sonidos",
                "§e▸ Ajustar tasas de pago",
                "§e▸ Límites de apuestas");
        inv.setItem(16, config);

        // Jackpot Control
        ItemStack jackpot = createItem(Material.NETHER_STAR, "§6§lControl de Jackpot",
                "§7Gestiona el jackpot del servidor",
                "",
                "§e▸ Ver jackpot actual",
                "§e▸ Añadir al jackpot",
                "§e▸ Forzar ganador",
                "§e▸ Configurar triggers");
        inv.setItem(28, jackpot);

        // Server Controls
        ItemStack controls = createItem(Material.LEVER, "§cControles del Servidor",
                "§7Acciones globales",
                "",
                "§e▸ Pausar todas las apuestas",
                "§e▸ Recargar config",
                "§e▸ Limpiar cooldowns",
                "§e▸ Backup de datos");
        inv.setItem(30, controls);

        // Leaderboard Manager
        ItemStack leaderboard = createItem(Material.GOLD_BLOCK, "§e§lGestión de Leaderboard",
                "§7Administra las tablas de clasificación",
                "",
                "§e▸ Ver Top 10",
                "§e▸ Resetear rankings",
                "§e▸ Premios automáticos");
        inv.setItem(32, leaderboard);

        // Economy Tools
        ItemStack economy = createItem(Material.EMERALD, "§2Herramientas de Economía",
                "§7Gestión económica avanzada",
                "",
                "§e▸ Balance total del servidor",
                "§e▸ Inflación/Deflación",
                "§e▸ Transacciones recientes",
                "§e▸ Auditoría");
        inv.setItem(34, economy);

        // Close button
        ItemStack close = createItem(Material.BARRIER, "§cCerrar", "§7Click para cerrar");
        inv.setItem(49, close);

        // Info
        ItemStack info = createItem(Material.PAPER, "§7Información",
                "§eGamblingPlugin v1.0",
                "§7Desarrollado para VieServer",
                "",
                "§aPlugins: §f" + plugin.getServer().getPluginManager().getPlugins().length,
                "§aJugadores Online: §f" + plugin.getServer().getOnlinePlayers().size());
        inv.setItem(4, info);

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player))
            return;
        Player player = (Player) e.getWhoClicked();

        if (!e.getView().getTitle().equals("§c§lGAMBLING ADMIN"))
            return;

        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR)
            return;

        String displayName = clicked.getItemMeta().getDisplayName();

        if (displayName.contains("Estructuras")) {
            player.closeInventory();
            player.sendMessage("§a[Admin] §7Abriendo gestor de estructuras...");
            // TODO: Open StructureManagerGUI
        } else if (displayName.contains("Jugadores")) {
            player.closeInventory();
            player.sendMessage("§a[Admin] §7Abriendo gestor de jugadores...");
            // TODO: Open PlayerManagerGUI
        } else if (displayName.contains("Estadísticas")) {
            player.closeInventory();
            showStatistics(player);
        } else if (displayName.contains("Configuración")) {
            player.closeInventory();
            player.sendMessage("§a[Admin] §7Abriendo editor de configuración...");
            // TODO: Open ConfigEditorGUI
        } else if (displayName.contains("Jackpot")) {
            player.closeInventory();
            showJackpotInfo(player);
        } else if (displayName.contains("Controles")) {
            player.closeInventory();
            showServerControls(player);
        } else if (displayName.contains("Leaderboard")) {
            player.closeInventory();
            showLeaderboard(player);
        } else if (displayName.contains("Economía")) {
            player.closeInventory();
            showEconomyTools(player);
        } else if (displayName.contains("Cerrar")) {
            player.closeInventory();
        }
    }

    private void showStatistics(Player player) {
        player.sendMessage("");
        player.sendMessage("§6§l═══════ ESTADÍSTICAS DEL SERVIDOR ═══════");
        player.sendMessage("");
        player.sendMessage("§e📊 Estadísticas Generales:");
        player.sendMessage("  §7• Total Estructuras: §f" + getTotalStructures());
        player.sendMessage("  §7• Jugadores Online: §f" + plugin.getServer().getOnlinePlayers().size());
        player.sendMessage("");
        player.sendMessage("§e💰 Estadísticas Económicas:");
        player.sendMessage("  §7• Total Apostado (Histórico): §a$XX,XXX.XX");
        player.sendMessage("  §7• Total Ganado (Histórico): §c$XX,XXX.XX");
        player.sendMessage("  §7• Beneficio del Casa: §6$XX,XXX.XX");
        player.sendMessage("");
        player.sendMessage("§e🎮 Juegos Populares:");
        player.sendMessage("  §7#1 Ruleta");
        player.sendMessage("  §7#2 Blackjack");
        player.sendMessage("  §7#3 Coinflip");
        player.sendMessage("");
        player.sendMessage("§6§l══════════════════════════════════════");
        player.sendMessage("");
    }

    private void showJackpotInfo(Player player) {
        double jackpot = 0.0; // TODO: Implement getBalance() in JackpotManager
        player.sendMessage("");
        player.sendMessage("§6§l═══════ JACKPOT INFO ═══════");
        player.sendMessage("");
        player.sendMessage("  §e💎 Jackpot Actual: §6§l$" + String.format("%.2f", jackpot));
        player.sendMessage("");
        player.sendMessage("§7Comandos disponibles:");
        player.sendMessage("  §a/gamble admin jackpot add <cantidad>");
        player.sendMessage("  §a/gamble admin jackpot set <cantidad>");
        player.sendMessage("  §a/gamble admin jackpot force <jugador>");
        player.sendMessage("");
        player.sendMessage("§6§l═════════════════════════════");
        player.sendMessage("");
    }

    private void showServerControls(Player player) {
        player.sendMessage("");
        player.sendMessage("§c§l═══════ CONTROLES DEL SERVIDOR ═══════");
        player.sendMessage("");
        player.sendMessage("§7Comandos disponibles:");
        player.sendMessage("  §e/gamble admin pause §7- Pausar todas las apuestas");
        player.sendMessage("  §e/gamble admin reload §7- Recargar configuración");
        player.sendMessage("  §e/gamble admin clearcd §7- Limpiar cooldowns");
        player.sendMessage("  §e/gamble admin backup §7- Crear backup de datos");
        player.sendMessage("");
        player.sendMessage("§c§l════════════════════════════════════════");
        player.sendMessage("");
    }

    private void showLeaderboard(Player player) {
        player.sendMessage("");
        player.sendMessage("§e§l═══════ TOP 10 JUGADORES ═══════");
        player.sendMessage("");

        // Get stats (simplified - would need actual implementation)
        player.sendMessage("  §7Ver jugadores con /gamble stats <player>");
        player.sendMessage("  §7O usar el admin GUI para gestión detallada");

        player.sendMessage("");
        player.sendMessage("§e§l══════════════════════════════");
        player.sendMessage("");
    }

    private void showEconomyTools(Player player) {
        player.sendMessage("");
        player.sendMessage("§2§l═══════ HERRAMIENTAS DE ECONOMÍA ═══════");
        player.sendMessage("");
        player.sendMessage("§e📈 Balance Total del Servidor: §a$XX,XXX.XX");
        player.sendMessage("");
        player.sendMessage("§7Comandos:");
        player.sendMessage("  §a/gamble admin eco add <jugador> <cantidad>");
        player.sendMessage("  §a/gamble admin eco remove <jugador> <cantidad>");
        player.sendMessage("  §a/gamble admin eco reset <jugador>");
        player.sendMessage("  §a/gamble admin eco audit");
        player.sendMessage("");
        player.sendMessage("§2§l══════════════════════════════════════════");
        player.sendMessage("");
    }

    private int getTotalStructures() {
        int total = 0;
        total += plugin.getStructureManager().getAllRoulettes().size();
        total += plugin.getStructureManager().getAllDice().size();
        total += plugin.getStructureManager().getAllCoinflips().size();
        total += plugin.getStructureManager().getAllMines().size();
        total += plugin.getStructureManager().getAllCases().size();
        total += plugin.getStructureManager().getAllPlinko().size();
        total += plugin.getStructureManager().getAllBlackjack().size();
        return total;
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
