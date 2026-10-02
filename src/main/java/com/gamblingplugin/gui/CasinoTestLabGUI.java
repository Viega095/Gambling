package com.gamblingplugin.gui;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class CasinoTestLabGUI implements Listener {

    public static class LabHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private final GamblingPlugin plugin;

    public CasinoTestLabGUI(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(new LabHolder(), 54, "§8🧪 §6Laboratorio de Pruebas: VieGambling §8🧪");

        ItemStack border = new ItemStack(Material.GOLD_INGOT);
        ItemMeta bMeta = border.getItemMeta();
        if (bMeta != null) {
            bMeta.setDisplayName(" ");
            border.setItemMeta(bMeta);
        }
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, createPane(Material.YELLOW_STAINED_GLASS_PANE));
            }
        }

        // Slot 10: Disparar / Ganar Mega Jackpot
        inv.setItem(10, createBtn(Material.NETHER_STAR, "§e⭐ Disparar Evento Mega Jackpot",
                Arrays.asList("§7Simula la entrega del pozo acumulado", "§7con pirotecnia y anuncio global.", "", "§e▶ Haz clic para disparar")));

        // Slot 11: Despegar Cohete Crash 3D
        inv.setItem(11, createBtn(Material.FIREWORK_ROCKET, "§c🚀 Despegar Cohete Crash 3D",
                Arrays.asList("§7Genera el cohete ascendente físico 3D", "§7con multiplicadores de cobro en vivo.", "", "§c▶ Haz clic para despegar")));

        // Slot 12: Iniciar Carrera de Caballos
        inv.setItem(12, createBtn(Material.SADDLE, "§6🏇 Iniciar Carrera de Caballos",
                Arrays.asList("§7Lanza la carrera animada con 4 corceles", "§7y cuotas de apuesta en tiempo real.", "", "§6▶ Haz clic para iniciar")));

        // Slot 13: Forzar Sorteo de Lotería
        inv.setItem(13, createBtn(Material.NAME_TAG, "§a🎫 Forzar Sorteo de Lotería Progresiva",
                Arrays.asList("§7Ejecuta el sorteo inmediato de la lotería", "§7entregando el pozo al ganador.", "", "§a▶ Haz clic para sortear")));

        // Slot 14: Evento de Atraco al Casino
        inv.setItem(14, createBtn(Material.IRON_SWORD, "§4🥷 Iniciar Evento de Atraco al Casino",
                Arrays.asList("§7Desata una redada de ladrones en el casino", "§7para defender las bóvedas de fichas.", "", "§4▶ Haz clic para desatar")));

        // Slot 15: Bóveda Segura & Tarjeta VIP
        inv.setItem(15, createBtn(Material.CRYING_OBSIDIAN, "§5💳 Bóveda Segura & Tarjeta VIP",
                Arrays.asList("§7Abre la interfaz de cashback semanal,", "§7interés de bóveda y tarjeta de lealtad.", "", "§5▶ Haz clic para abrir")));

        // Slot 16: Bar VIP & Cócteles con Buffs
        inv.setItem(16, createBtn(Material.POTION, "§d🍸 Bar VIP & Bebidas de Suerte",
                Arrays.asList("§7Abre la carta de cócteles especiales", "§7con efectos y potenciadores de suerte.", "", "§d▶ Haz clic para abrir")));

        // Slot 17: Casino Tycoon (Dueño de Mesas)
        inv.setItem(17, createBtn(Material.DARK_OAK_SIGN, "§6🏛️ Casino Tycoon (Dueño de Mesas)",
                Arrays.asList("§7Compra mesas de Blackjack, Ruleta", "§7y genera ganancias pasivas del casino.", "", "§6▶ Haz clic para abrir")));

        // Slot 19: Mesa de Baccarat Punto Banco
        inv.setItem(19, createBtn(Material.MAP, "§2🃏 Mesa de Baccarat Punto Banco",
                Arrays.asList("§7Abre la mesa interactiva de Baccarat", "§7apostando a Jugador, Banca o Empate.", "", "§2▶ Haz clic para abrir")));

        // Slot 20: Lobby de Blackjack
        inv.setItem(20, createBtn(Material.BLACK_CONCRETE, "§8🂡 Lobby de Mesas de Blackjack",
                Arrays.asList("§7Abre el menú de mesas de Blackjack", "§7con límites clásicos y high-roller.", "", "§8▶ Haz clic para abrir")));

        // Slot 21: Ruleta Clásica
        inv.setItem(21, createBtn(Material.COMPASS, "§c🎡 Ruleta Europea / Americana",
                Arrays.asList("§7Abre la ruleta interactiva para apostar", "§7a colores, números, pares o docenas.", "", "§c▶ Haz clic para abrir")));

        // Slot 22: Dados Casino
        inv.setItem(22, createBtn(Material.WHITE_CONCRETE, "§f🎲 Mesa de Dados (Dice Roll)",
                Arrays.asList("§7Abre el juego de dados 3D con tiradas", "§7animadas e historial de resultados.", "", "§f▶ Haz clic para abrir")));

        // Slot 23: Campo de Minas
        inv.setItem(23, createBtn(Material.TNT, "§4💣 Juego de Minas (Mines)",
                Arrays.asList("§7Abre la cuadrícula de gemas y bombas", "§7con retiro de ganancias en cada paso.", "", "§4▶ Haz clic para abrir")));

        // Slot 24: Soltar Bola en Plinko 3D
        inv.setItem(24, createBtn(Material.SLIME_BALL, "§a🎱 Máquina Plinko Física 3D",
                Arrays.asList("§7Genera y suelta una bola física que rebota", "§7en clavijas hacia multiplicadores.", "", "§a▶ Haz clic para soltar")));

        // Slot 25: Tragamonedas 3D Física
        inv.setItem(25, createBtn(Material.DISPENSER, "§6🎰 Tragamonedas Física 3D",
                Arrays.asList("§7Genera y gira los rodillos mecánicos 3D", "§7en el mundo con sonidos de monedas.", "", "§6▶ Haz clic para girar")));

        // Slot 26: Texas Hold'em Poker Multijugador
        inv.setItem(26, createBtn(Material.PAPER, "§2🀄 Texas Hold'em Poker Multijugador",
                Arrays.asList("§7Mesa interactiva para 2-6 jugadores con", "§7ciegas, cartas comunitarias y bote.", "", "§2▶ Haz clic para sentarte")));

        // Slot 28: Apertura de Cajas (Cases)
        inv.setItem(28, createBtn(Material.CHEST, "§b📦 Apertura de Cajas (Case Opening)",
                Arrays.asList("§7Abre cajas con animación de ruleta CS:GO", "§7para ganar premios legendarios.", "", "§b▶ Haz clic para abrir")));

        // Slot 29: Coinflip (Cara o Cruz)
        inv.setItem(29, createBtn(Material.SUNFLOWER, "§e🪙 Cara o Cruz (Coinflip 1v1)",
                Arrays.asList("§7Abre el menú de apuestas cara o cruz", "§7contra otros jugadores o la casa.", "", "§e▶ Haz clic para abrir")));

        // Slot 30: Ruleta Diaria Gratuita
        inv.setItem(30, createBtn(Material.CLOCK, "§6🎁 Ruleta de la Suerte Diaria",
                Arrays.asList("§7Gira la ruleta de recompensas gratuitas", "§7disponible una vez cada 24 horas.", "", "§6▶ Haz clic para girar")));

        // Slot 31: Pase de Temporada VIP
        inv.setItem(31, createBtn(Material.GOLDEN_CARROT, "§e🎟️ Pase de Temporada del Casino",
                Arrays.asList("§7Consulta tu nivel de pase y reclama", "§7recompensas exclusivas de nivel.", "", "§e▶ Haz clic para abrir")));

        // Slot 32: Exchange de Fichas $CHIPS
        inv.setItem(32, createBtn(Material.GOLD_NUGGET, "§6💱 Exchange de Fichas $CHIPS",
                Arrays.asList("§7Intercambia dinero líquido por fichas", "§7de casino $CHIPS y viceversa.", "", "§6▶ Haz clic para ver")));

        // Slot 33: Spawnear Holograma Leaderboard
        inv.setItem(33, createBtn(Material.ARMOR_STAND, "§b🗼 Spawnear Holograma Flotante",
                Arrays.asList("§7Coloca un holograma con los mayores", "§7ganadores y pozo flotante en tu posición.", "", "§b▶ Haz clic para spawnear")));

        // Slot 34: Visor de Estadísticas
        inv.setItem(34, createBtn(Material.BOOK, "§d📊 Visor de Estadísticas Personales",
                Arrays.asList("§7Abre tu historial de apuestas, partidas", "§7ganadas, perdidas y balance neto.", "", "§d▶ Haz clic para abrir")));

        // Slot 35: Futuros Binarios 30s
        inv.setItem(35, createBtn(Material.NETHERITE_INGOT, "§e📉 Futuros Binarios (30s)",
                Arrays.asList("§7Opera contratos rápidos de subida/bajada", "§7con gráficos de velas en tiempo real.", "", "§e▶ Haz clic para operar")));

        // Slot 47: Generar Casino Resort 3D Completo
        inv.setItem(47, createBtn(Material.BEACON, "§6🏛️ Generar Casino Resort 3D (Plaza)",
                Arrays.asList("§7Construye una plataforma de cuarzo liso", "§7con todos los 14 juegos y mesas 3D.", "", "§6▶ Haz clic para generar")));

        // Slot 48: Auto-Update Check
        inv.setItem(48, createBtn(Material.EXPERIENCE_BOTTLE, "§a🔄 Probar Auto-Update en GitHub",
                Arrays.asList("§7Verifica nuevas versiones y releases en", "§7GitHub sin salir del juego.", "", "§a▶ Haz clic para verificar")));

        // Slot 50: Live Hot-Reload
        inv.setItem(50, createBtn(Material.REDSTONE_TORCH, "§c⚡ Recarga en Caliente (Hot-Reload)",
                Arrays.asList("§7Recarga configs, juegos y probabilidades", "§7en <50ms sin reiniciar el servidor.", "", "§c▶ Haz clic para recargar")));

        // Center bottom
        inv.setItem(49, createBtn(Material.NETHER_STAR, "§6§l✦ PANEL MAESTRO DE PRUEBAS DEL CASINO",
                Arrays.asList("§7Haz clic en cualquier juego o evento", "§7para ejecutarlo y probarlo al instante.")));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1.2f);
    }

    private ItemStack createPane(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createBtn(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof LabHolder)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        int slot = event.getRawSlot();

        switch (slot) {
            case 10: // Jackpot Event
                player.closeInventory();
                plugin.getMegaJackpotEngine().triggerMegaJackpotWin(player);
                break;

            case 11: // Crash 3D
                player.closeInventory();
                if (plugin.getCrashBettingAndCashoutGUI() != null) {
                    plugin.getCrashBettingAndCashoutGUI().openBettingGUI(player);
                }
                break;

            case 12: // Race
                player.closeInventory();
                player.performCommand("gamble race start");
                break;

            case 13: // Lottery Draw
                player.closeInventory();
                plugin.getLotteryDrawEngine().executeDraw();
                break;

            case 14: // Heist Event
                player.closeInventory();
                plugin.getCasinoHeistEvent().startHeist(player);
                break;

            case 15: // Vault & VIP
                player.closeInventory();
                plugin.getCasinoVaultAndLoyalty().openVaultGUI(player);
                break;

            case 16: // Lounge
                player.closeInventory();
                plugin.getCasinoVIPLounge().openLoungeMenu(player);
                break;

            case 17: // Tycoon
                player.closeInventory();
                if (plugin.getCasinoTycoonManager() != null) {
                    plugin.getCasinoTycoonManager().openTycoonGUI(player);
                }
                break;

            case 19: // Baccarat
                player.closeInventory();
                plugin.getBaccaratTable().openGUI(player);
                break;

            case 20: // Blackjack
                player.closeInventory();
                player.performCommand("gamble blackjack");
                break;

            case 21: // Roulette
                player.closeInventory();
                player.performCommand("gamble roulette 100 red");
                break;

            case 22: // Dice
                player.closeInventory();
                player.performCommand("gamble dice 100 6");
                break;

            case 23: // Mines
                player.closeInventory();
                player.performCommand("gamble mines 100");
                break;

            case 24: // Plinko 3D
                player.closeInventory();
                player.performCommand("gamble plinko3d");
                break;

            case 25: // Slots 3D
                player.closeInventory();
                player.performCommand("gamble slots3d");
                break;

            case 26: // Multiplayer Poker
                player.closeInventory();
                if (plugin.getMultiplayerPokerEngine() != null) {
                    plugin.getMultiplayerPokerEngine().openPokerTableGUI(player);
                }
                break;

            case 28: // Case
                player.closeInventory();
                player.performCommand("gamble case");
                break;

            case 29: // Coinflip
                player.closeInventory();
                player.performCommand("gamble coinflip");
                break;

            case 30: // Mega Wheel
                player.closeInventory();
                if (plugin.getMegaWheelOfFortune() != null) {
                    plugin.getMegaWheelOfFortune().openWheelGUI(player);
                }
                break;

            case 31: // Pass
                player.closeInventory();
                plugin.getCasinoPassManager().showPassStatus(player);
                break;

            case 32: // Tokens
                player.closeInventory();
                player.performCommand("gamble token");
                break;

            case 33: // Hologram
                player.closeInventory();
                plugin.getCasinoHologramEngine().spawnHologram(player.getLocation());
                player.sendMessage(ChatColor.GREEN + "✔ Holograma de Casino creado en tu posición.");
                break;

            case 34: // Stats
                player.closeInventory();
                new com.gamblingplugin.gui.StatsViewerGUI(plugin).open(player, player);
                break;

            case 35: // Binary Futures
                player.closeInventory();
                if (plugin.getBinaryFuturesMarket() != null) {
                    plugin.getBinaryFuturesMarket().openFuturesGUI(player);
                }
                break;

            case 47: // Casino Resort Plaza
                player.closeInventory();
                if (plugin.getStructureManager() != null) {
                    plugin.getStructureManager().generateCasinoPlaza(player);
                }
                break;

            case 48: // Update
                player.closeInventory();
                plugin.getUpdateManager().checkUpdate(player, true);
                break;

            case 50: // Reload
                player.closeInventory();
                plugin.getUpdateManager().performLiveReload(player);
                break;
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof LabHolder) {
            event.setCancelled(true);
        }
    }
}
