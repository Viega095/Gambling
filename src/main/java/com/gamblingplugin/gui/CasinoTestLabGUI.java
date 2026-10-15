package com.gamblingplugin.gui;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.multiblock.CasinoMultiblockType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

        // Fila 0 (0-8): Obtención de Ítems Multiblock con PDC & Economía de Prueba
        inv.setItem(0, createBtn(Material.GREEN_GLAZED_TERRACOTTA, "§a§l[ÍTEM] Mesa de Ruleta 3D",
                Arrays.asList("§7Obtén el ítem crafteable con PDC.", "§7Colócalo en el mundo para desplegarla.", "", "§e▶ Haz clic para recibir")));

        inv.setItem(1, createBtn(Material.YELLOW_GLAZED_TERRACOTTA, "§e§l[ÍTEM] Mesa de Coinflip 3D",
                Arrays.asList("§7Obtén el ítem crafteable con PDC.", "§7Colócalo en el mundo para desplegarla.", "", "§e▶ Haz clic para recibir")));

        inv.setItem(2, createBtn(Material.WHITE_GLAZED_TERRACOTTA, "§f§l[ÍTEM] Mesa de Dados 3D",
                Arrays.asList("§7Obtén el ítem crafteable con PDC.", "§7Colócalo en el mundo para desplegarla.", "", "§e▶ Haz clic para recibir")));

        inv.setItem(3, createBtn(Material.RED_GLAZED_TERRACOTTA, "§c§l[ÍTEM] Tragamonedas 3D Gigante",
                Arrays.asList("§7Obtén el ítem crafteable con PDC.", "§7Colócalo en el mundo para desplegarla.", "", "§e▶ Haz clic para recibir")));

        inv.setItem(4, createBtn(Material.ORANGE_GLAZED_TERRACOTTA, "§6§l[ÍTEM] Mega Rueda de la Fortuna",
                Arrays.asList("§7Obtén el ítem crafteable con PDC.", "§7Colócalo en el mundo para desplegarla.", "", "§e▶ Haz clic para recibir")));

        inv.setItem(5, createBtn(Material.CHEST, "§d§l[PACK] Kit Completo Multiblocks 3D",
                Arrays.asList("§7Recibe 1 unidad de cada una de las", "§7estructuras de casino con PDC.", "", "§d▶ Haz clic para recibir todo")));

        inv.setItem(6, createBtn(Material.GOLD_INGOT, "§a§l+10,000 Monedas de Prueba",
                Arrays.asList("§7Deposita 10,000 monedas en tu cuenta Vault", "§7para realizar apuestas y pruebas.", "", "§a▶ Haz clic para recargar")));

        inv.setItem(7, createBtn(Material.GOLD_BLOCK, "§a§l+100,000 Monedas de Prueba",
                Arrays.asList("§7Deposita 100,000 monedas en tu cuenta Vault", "§7para apuestas de alto límite.", "", "§a▶ Haz clic para recargar")));

        inv.setItem(8, createBtn(Material.BARRIER, "§c§lDesmontar Estructuras Cercanas",
                Arrays.asList("§7Desmonta limpiamente todas las mesas", "§7en un radio de 15 bloques (0 dupes).", "", "§c▶ Haz clic para limpiar")));

        // Fila 1 (9-17): Spawning Directo en el Mundo & Eventos Masivos
        inv.setItem(9, createBtn(Material.COMPASS, "§a🎡 Spawnear Ruleta 3D en el Mundo",
                Arrays.asList("§7Construye la mesa de ruleta 3D física", "§7directamente en tus coordenadas.", "", "§a▶ Haz clic para spawnear")));

        inv.setItem(10, createBtn(Material.SUNFLOWER, "§e🪙 Spawnear Coinflip 3D en el Mundo",
                Arrays.asList("§7Construye la mesa de cara o cruz 3D", "§7directamente en tus coordenadas.", "", "§e▶ Haz clic para spawnear")));

        inv.setItem(11, createBtn(Material.BONE_BLOCK, "§f🎲 Spawnear Mesa de Dados 3D en el Mundo",
                Arrays.asList("§7Construye la mesa de dados craps 3D", "§7directamente en tus coordenadas.", "", "§f▶ Haz clic para spawnear")));

        inv.setItem(12, createBtn(Material.DISPENSER, "§c🎰 Spawnear Tragamonedas 3D en el Mundo",
                Arrays.asList("§7Construye la cabina tragamonedas 3D", "§7directamente en tus coordenadas.", "", "§c▶ Haz clic para spawnear")));

        inv.setItem(13, createBtn(Material.TARGET, "§6🎯 Spawnear Mega Rueda 3D en el Mundo",
                Arrays.asList("§7Construye la rueda de 16 sectores 3D", "§7directamente en tus coordenadas.", "", "§6▶ Haz clic para spawnear")));

        inv.setItem(14, createBtn(Material.BEACON, "§6🏛️ Generar Casino Resort 3D (Plaza)",
                Arrays.asList("§7Construye la plaza completa de cuarzo", "§7con todos los 14 juegos y mesas 3D.", "", "§6▶ Haz clic para generar")));

        inv.setItem(15, createBtn(Material.NETHER_STAR, "§e⭐ Disparar Evento Mega Jackpot",
                Arrays.asList("§7Simula la entrega del pozo acumulado", "§7con pirotecnia y anuncio global.", "", "§e▶ Haz clic para disparar")));

        inv.setItem(16, createBtn(Material.FIREWORK_ROCKET, "§c🚀 Despegar Cohete Crash 3D",
                Arrays.asList("§7Genera el cohete ascendente físico 3D", "§7con multiplicadores de cobro en vivo.", "", "§c▶ Haz clic para despegar")));

        inv.setItem(17, createBtn(Material.IRON_SWORD, "§4🥷 Iniciar Evento de Atraco al Casino",
                Arrays.asList("§7Desata una redada de ladrones en el casino", "§7para defender las bóvedas de fichas.", "", "§4▶ Haz clic para desatar")));

        // Fila 2 (18-26): Juegos Principales & Modalidades Nuevas
        inv.setItem(18, createBtn(Material.GREEN_CONCRETE, "§a🎡 Probar Ruleta (Docenas & Columnas)",
                Arrays.asList("§7Abre el menú interactivo de la ruleta", "§7con apuestas a docenas y columnas.", "", "§a▶ Haz clic para abrir")));

        inv.setItem(19, createBtn(Material.GOLD_NUGGET, "§e🪙 Probar Coinflip (Modo Racha)",
                Arrays.asList("§7Abre el menú de apuestas cara o cruz", "§7con el nuevo modo racha exponencial.", "", "§e▶ Haz clic para abrir")));

        inv.setItem(20, createBtn(Material.WHITE_CONCRETE, "§f🎲 Probar Dados (Craps & Dobles)",
                Arrays.asList("§7Abre el menú de dados 3D con apuestas", "§7a pase craps (7 u 11) y dobles pares.", "", "§f▶ Haz clic para abrir")));

        inv.setItem(21, createBtn(Material.REDSTONE_LAMP, "§c🎰 Probar Tragamonedas (Multilíneas)",
                Arrays.asList("§7Abre la interfaz de apuestas de slots", "§7con rodillos mecánicos y jackpots.", "", "§c▶ Haz clic para abrir")));

        inv.setItem(22, createBtn(Material.GLOWSTONE, "§6🎯 Probar Mega Rueda de la Fortuna",
                Arrays.asList("§7Gira la rueda monumental de 16 sectores", "§7con multiplicadores de hasta 50x.", "", "§6▶ Haz clic para abrir")));

        inv.setItem(23, createBtn(Material.BLACK_CONCRETE, "§8🂡 Lobby de Blackjack Multijugador",
                Arrays.asList("§7Abre el menú de mesas de Blackjack", "§7con límites clásicos y high-roller.", "", "§8▶ Haz clic para abrir")));

        inv.setItem(24, createBtn(Material.MAP, "§2🃏 Mesa de Baccarat Punto Banco",
                Arrays.asList("§7Abre la mesa interactiva de Baccarat", "§7apostando a Jugador, Banca o Empate.", "", "§2▶ Haz clic para abrir")));

        inv.setItem(25, createBtn(Material.PAPER, "§2🀄 Texas Hold'em Poker Multijugador",
                Arrays.asList("§7Mesa interactiva para 2-6 jugadores con", "§7ciegas, cartas comunitarias y bote.", "", "§2▶ Haz clic para sentarte")));

        inv.setItem(26, createBtn(Material.TNT, "§4💣 Juego de Minas (Mines)",
                Arrays.asList("§7Abre la cuadrícula de gemas y bombas", "§7con retiro de ganancias en cada paso.", "", "§4▶ Haz clic para abrir")));

        // Fila 3 (27-35): Minijuegos Secundarios & Loterías
        inv.setItem(27, createBtn(Material.SLIME_BALL, "§a🎱 Máquina Plinko Física 3D",
                Arrays.asList("§7Genera y suelta una bola física que rebota", "§7en clavijas hacia multiplicadores.", "", "§a▶ Haz clic para soltar")));

        inv.setItem(28, createBtn(Material.CHEST, "§b📦 Apertura de Cajas (Case Opening)",
                Arrays.asList("§7Abre cajas con animación de ruleta CS:GO", "§7para ganar premios legendarios.", "", "§b▶ Haz clic para abrir")));

        inv.setItem(29, createBtn(Material.NAME_TAG, "§e🎟️ Raspe de la Fortuna (Scratch Card)",
                Arrays.asList("§7Abre una tarjeta rascable interactiva", "§7buscando 3 símbolos coincidentes.", "", "§e▶ Haz clic para abrir")));

        inv.setItem(30, createBtn(Material.SADDLE, "§6🏇 Iniciar Carrera de Caballos",
                Arrays.asList("§7Lanza la carrera animada con 4 corceles", "§7y cuotas de apuesta en tiempo real.", "", "§6▶ Haz clic para iniciar")));

        inv.setItem(31, createBtn(Material.EMERALD, "§a🎫 Forzar Sorteo de Lotería Progresiva",
                Arrays.asList("§7Ejecuta el sorteo inmediato de la lotería", "§7entregando el pozo al ganador.", "", "§a▶ Haz clic para sortear")));

        inv.setItem(32, createBtn(Material.NETHERITE_INGOT, "§e📉 Futuros Binarios (30s)",
                Arrays.asList("§7Opera contratos rápidos de subida/bajada", "§7con gráficos de velas en tiempo real.", "", "§e▶ Haz clic para operar")));

        inv.setItem(33, createBtn(Material.CLOCK, "§6🎁 Ruleta de la Suerte Diaria",
                Arrays.asList("§7Gira la ruleta de recompensas gratuitas", "§7disponible una vez cada 24 horas.", "", "§6▶ Haz clic para girar")));

        inv.setItem(34, createBtn(Material.GOLDEN_CARROT, "§e🎟️ Pase de Temporada del Casino",
                Arrays.asList("§7Consulta tu nivel de pase y reclama", "§7recompensas exclusivas de nivel.", "", "§e▶ Haz clic para abrir")));

        inv.setItem(35, createBtn(Material.EXPERIENCE_BOTTLE, "§6💱 Casa de Cambio de Fichas VIP",
                Arrays.asList("§7Intercambia dinero líquido por fichas", "§7de casino VIP y viceversa.", "", "§6▶ Haz clic para ver")));

        // Fila 4 (36-44): Sistemas de Utilidad, VIP y Mantenimiento
        inv.setItem(36, createBtn(Material.CRYING_OBSIDIAN, "§5💳 Bóveda Segura & Tarjeta VIP",
                Arrays.asList("§7Abre la interfaz de cashback semanal,", "§7interés de bóveda y tarjeta de lealtad.", "", "§5▶ Haz clic para abrir")));

        inv.setItem(37, createBtn(Material.POTION, "§d🍸 Bar VIP & Bebidas de Suerte",
                Arrays.asList("§7Abre la carta de cócteles especiales", "§7con efectos y potenciadores de suerte.", "", "§d▶ Haz clic para abrir")));

        inv.setItem(38, createBtn(Material.DARK_OAK_SIGN, "§6🏛️ Casino Tycoon (Dueño de Mesas)",
                Arrays.asList("§7Compra mesas de Blackjack, Ruleta", "§7y genera ganancias pasivas del casino.", "", "§6▶ Haz clic para abrir")));

        inv.setItem(39, createBtn(Material.ARMOR_STAND, "§b🗼 Spawnear Holograma Flotante",
                Arrays.asList("§7Coloca un holograma con los mayores", "§7ganadores y pozo flotante en tu posición.", "", "§b▶ Haz clic para spawnear")));

        inv.setItem(40, createBtn(Material.BOOK, "§d📊 Visor de Estadísticas Personales",
                Arrays.asList("§7Abre tu historial de apuestas, partidas", "§7ganadas, perdidas y balance neto.", "", "§d▶ Haz clic para abrir")));

        inv.setItem(41, createBtn(Material.DIAMOND_BLOCK, "§6✦ Club VIP & Rakeback ✦",
                Arrays.asList("§7Consulta tu rango VIP y reclama", "§7tu porcentaje acumulado de rakeback.", "", "§6▶ Haz clic para abrir")));

        inv.setItem(42, createBtn(Material.IRON_BLOCK, "§e🏛️ Gestor Maestro de Estructuras 3D",
                Arrays.asList("§7Administra, teleporta y elimina todas", "§7las mesas y juegos 3D activos.", "", "§e▶ Haz clic para abrir")));

        inv.setItem(43, createBtn(Material.ENDER_EYE, "§a🔄 Probar Auto-Update en GitHub",
                Arrays.asList("§7Verifica nuevas versiones y releases en", "§7GitHub sin salir del juego.", "", "§a▶ Haz clic para verificar")));

        inv.setItem(44, createBtn(Material.REDSTONE_TORCH, "§c⚡ Recarga en Caliente (Hot-Reload)",
                Arrays.asList("§7Recarga configs, juegos y probabilidades", "§7en <50ms sin reiniciar el servidor.", "", "§c▶ Haz clic para recargar")));

        // Fila 5 (45-53): Keno 3D, Raspes Especiales, y Panel Maestro
        inv.setItem(45, createBtn(Material.CYAN_GLAZED_TERRACOTTA, "§6§l[ÍTEM] Mesa de Keno 3D (PDC)",
                Arrays.asList("§7Obtén el ítem crafteable con PDC.", "§7Colócalo en el mundo para desplegarla.", "", "§e▶ Haz clic para recibir")));
        inv.setItem(46, createBtn(Material.SEA_LANTERN, "§b🎱 Spawnear Mesa de Keno 3D",
                Arrays.asList("§7Construye la mesa de Keno 3D física", "§7directamente en tus coordenadas.", "", "§b▶ Haz clic para spawnear")));
        inv.setItem(47, createBtn(Material.LIME_DYE, "§a🎱 Probar Keno de la Fortuna (GUI)",
                Arrays.asList("§7Abre el tablero de 40 números de Keno", "§7con sorteos y multiplicadores de hasta 50x.", "", "§a▶ Haz clic para jugar")));
        inv.setItem(48, createBtn(Material.LIME_TERRACOTTA, "§a§l[ÍTEM] Mesa de Craps Real 3D (PDC)",
                Arrays.asList("§7Obtén el ítem crafteable con PDC.", "§7Colócalo en el mundo para desplegarla.", "", "§e▶ Haz clic para recibir")));
        inv.setItem(49, createBtn(Material.LIME_WOOL, "§a🎲 Spawnear Mesa de Craps 3D",
                Arrays.asList("§7Construye la mesa de Craps 3D física", "§7con paño reglamentario en tus coordenadas.", "", "§b▶ Haz clic para spawnear")));

        inv.setItem(50, createBtn(Material.GREEN_TERRACOTTA, "§2§l[ÍTEM] Mesa de Blackjack 3D (PDC)",
                Arrays.asList("§7Obtén el ítem crafteable con PDC.", "§7Colócalo en el mundo para desplegarla.", "", "§e▶ Haz clic para recibir")));
        inv.setItem(51, createBtn(Material.DARK_OAK_DOOR, "§2🃏 Spawnear Mesa de Blackjack 3D",
                Arrays.asList("§7Construye la mesa de Blackjack 3D física", "§7con crupier animado en tus coordenadas.", "", "§b▶ Haz clic para spawnear")));
        inv.setItem(52, createBtn(Material.KNOWLEDGE_BOOK, "§a§l✦ Abrir Compendio / Guía del Casino ✦",
                Arrays.asList("§7Abre la enciclopedia completa de VieGambling", "§7con reglas, comandos, mesas y economía.", "", "§a▶ Haz clic para consultar la guía")));
        inv.setItem(53, createBtn(Material.PLAYER_HEAD, "§e🂡 Probar Blackjack 21 Directo",
                Arrays.asList("§7Abre el lobby de Blackjack multijugador", "§7para jugar una partida contra el crupier.", "", "§e▶ Haz clic para jugar")));

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
            // Fila 0: Obtención de Ítems Multiblock con PDC & Fondos
            case 0:
                giveItem(player, CasinoMultiblockType.ROULETTE_3D);
                break;
            case 1:
                giveItem(player, CasinoMultiblockType.COINFLIP_3D);
                break;
            case 2:
                giveItem(player, CasinoMultiblockType.DICE_3D);
                break;
            case 3:
                giveItem(player, CasinoMultiblockType.SLOTS_3D);
                break;
            case 4:
                giveItem(player, CasinoMultiblockType.WHEEL_OF_FORTUNE);
                break;
            case 5:
                for (CasinoMultiblockType type : CasinoMultiblockType.values()) {
                    giveItem(player, type);
                }
                player.sendMessage("§a[TestLab] ✔ Has recibido el kit completo de todos los multiblocks desplegables con PDC.");
                break;
            case 6:
                plugin.getEconomyManager().deposit(player, 10000.0);
                player.sendMessage("§a[TestLab] ✔ Se han depositado §e+10,000 monedas §aen tu cuenta.");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
                break;
            case 7:
                plugin.getEconomyManager().deposit(player, 100000.0);
                player.sendMessage("§a[TestLab] ✔ Se han depositado §e+100,000 monedas §aen tu cuenta.");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.8f);
                break;
            case 8:
                int count = plugin.getCasinoMultiblockManager().dismantleAllNearby(player.getLocation(), 15.0, player);
                player.sendMessage("§a[TestLab] ✔ Se han desmontado limpiamente " + count + " estructuras cercanas.");
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1f);
                break;

            // Fila 1: Spawning Directo en el Mundo
            case 9:
                player.closeInventory();
                plugin.getCasinoMultiblockManager().placeMultiblock(player, player.getLocation().getBlock().getLocation(), CasinoMultiblockType.ROULETTE_3D);
                break;
            case 10:
                player.closeInventory();
                plugin.getCasinoMultiblockManager().placeMultiblock(player, player.getLocation().getBlock().getLocation(), CasinoMultiblockType.COINFLIP_3D);
                break;
            case 11:
                player.closeInventory();
                plugin.getCasinoMultiblockManager().placeMultiblock(player, player.getLocation().getBlock().getLocation(), CasinoMultiblockType.DICE_3D);
                break;
            case 12:
                player.closeInventory();
                plugin.getCasinoMultiblockManager().placeMultiblock(player, player.getLocation().getBlock().getLocation(), CasinoMultiblockType.SLOTS_3D);
                break;
            case 13:
                player.closeInventory();
                plugin.getCasinoMultiblockManager().placeMultiblock(player, player.getLocation().getBlock().getLocation(), CasinoMultiblockType.WHEEL_OF_FORTUNE);
                break;
            case 14:
                player.closeInventory();
                if (plugin.getStructureManager() != null) {
                    plugin.getStructureManager().generateCasinoPlaza(player);
                }
                break;
            case 15:
                player.closeInventory();
                plugin.getMegaJackpotEngine().triggerMegaJackpotWin(player);
                break;
            case 16:
                player.closeInventory();
                if (plugin.getCrashBettingAndCashoutGUI() != null) {
                    plugin.getCrashBettingAndCashoutGUI().openBettingGUI(player);
                }
                break;
            case 17:
                player.closeInventory();
                plugin.getCasinoHeistEvent().startHeist(player);
                break;

            // Fila 2: Modos Principales
            case 18:
                player.closeInventory();
                plugin.getRouletteSelectionGUI().open(player, 100.0);
                break;
            case 19:
                player.closeInventory();
                plugin.getCoinflipGUI().open(player);
                break;
            case 20:
                player.closeInventory();
                plugin.getDiceGUI().open(player);
                break;
            case 21:
                player.closeInventory();
                new SlotsBettingGUI(plugin).open(player);
                break;
            case 22:
                player.closeInventory();
                if (plugin.getMegaWheelOfFortune() != null) {
                    plugin.getMegaWheelOfFortune().openWheelGUI(player);
                }
                break;
            case 23:
                player.closeInventory();
                player.performCommand("gamble blackjack");
                break;
            case 24:
                player.closeInventory();
                plugin.getBaccaratTable().openGUI(player);
                break;
            case 25:
                player.closeInventory();
                if (plugin.getMultiplayerPokerEngine() != null) {
                    plugin.getMultiplayerPokerEngine().openPokerTableGUI(player);
                }
                break;
            case 26:
                player.closeInventory();
                player.performCommand("gamble mines 100");
                break;

            // Fila 3: Minijuegos Secundarios
            case 27:
                player.closeInventory();
                player.performCommand("gamble plinko3d");
                break;
            case 28:
                player.closeInventory();
                player.performCommand("gamble case");
                break;
            case 29:
                player.closeInventory();
                plugin.getScratchCardGUI().openNewCard(player, 500.0);
                break;
            case 30:
                player.closeInventory();
                player.performCommand("gamble race start");
                break;
            case 31:
                player.closeInventory();
                plugin.getLotteryDrawEngine().executeDraw();
                break;
            case 32:
                player.closeInventory();
                if (plugin.getBinaryFuturesMarket() != null) {
                    plugin.getBinaryFuturesMarket().openFuturesGUI(player);
                }
                break;
            case 33:
                player.closeInventory();
                player.performCommand("gamble daily");
                break;
            case 34:
                player.closeInventory();
                plugin.getCasinoPassManager().showPassStatus(player);
                break;
            case 35:
                player.closeInventory();
                player.performCommand("gamble token");
                break;

            // Fila 4: Sistemas de Utilidad & VIP
            case 36:
                player.closeInventory();
                plugin.getCasinoVaultAndLoyalty().openVaultGUI(player);
                break;
            case 37:
                player.closeInventory();
                plugin.getCasinoVIPLounge().openLoungeMenu(player);
                break;
            case 38:
                player.closeInventory();
                if (plugin.getCasinoTycoonManager() != null) {
                    plugin.getCasinoTycoonManager().openTycoonGUI(player);
                }
                break;
            case 39:
                player.closeInventory();
                plugin.getCasinoHologramEngine().spawnHologram(player.getLocation());
                player.sendMessage(ChatColor.GREEN + "✔ Holograma de Casino creado en tu posición.");
                break;
            case 40:
                player.closeInventory();
                new com.gamblingplugin.gui.StatsViewerGUI(plugin).open(player, player);
                break;
            case 41:
                player.closeInventory();
                plugin.getVIPGUI().open(player);
                break;
            case 42:
                player.closeInventory();
                new com.gamblingplugin.gui.StructureManagerGUI(plugin).open(player);
                break;
            case 43:
                player.closeInventory();
                plugin.getUpdateManager().checkUpdate(player, true);
                break;
            case 44:
                player.closeInventory();
                plugin.getUpdateManager().performLiveReload(player);
                break;
            case 45:
                giveItem(player, CasinoMultiblockType.KENO_3D);
                break;
            case 46:
                player.closeInventory();
                Location kenoLoc = player.getLocation().getBlock().getLocation();
                plugin.getCasinoMultiblockManager().placeMultiblock(player, kenoLoc, CasinoMultiblockType.KENO_3D);
                player.sendMessage("§a[TestLab] ✔ Mesa de Keno 3D construida en tu ubicación.");
                break;
            case 47:
                player.closeInventory();
                new com.gamblingplugin.gui.KenoGUI(plugin).open(player);
                break;
            case 48:
                giveItem(player, CasinoMultiblockType.CRAPS_TABLE_3D);
                break;
            case 49:
                player.closeInventory();
                Location crapsLoc = player.getLocation().getBlock().getLocation();
                plugin.getCasinoMultiblockManager().placeMultiblock(player, crapsLoc, CasinoMultiblockType.CRAPS_TABLE_3D);
                player.sendMessage("§a[TestLab] ✔ Mesa de Craps Real 3D construida en tu ubicación.");
                break;
            case 50:
                giveItem(player, CasinoMultiblockType.BLACKJACK_3D);
                break;
            case 51:
                player.closeInventory();
                Location bjLoc = player.getLocation().getBlock().getLocation();
                plugin.getCasinoMultiblockManager().placeMultiblock(player, bjLoc, CasinoMultiblockType.BLACKJACK_3D);
                player.sendMessage("§a[TestLab] ✔ Mesa de Blackjack 3D construida en tu ubicación.");
                break;
            case 52:
                player.closeInventory();
                new CasinoContentGuideGUI(plugin).open(player);
                break;
            case 53:
                player.closeInventory();
                new BlackjackLobbyGUI(plugin).open(player);
                break;
        }
    }

    private void giveItem(Player player, CasinoMultiblockType type) {
        ItemStack item = type.createItemStack(plugin);
        player.getInventory().addItem(item);
        player.sendMessage("§a[TestLab] ✔ Has recibido: " + type.getDisplayName() + " §a(PDC: casino_structure)");
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1.2f);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof LabHolder) {
            event.setCancelled(true);
        }
    }
}
