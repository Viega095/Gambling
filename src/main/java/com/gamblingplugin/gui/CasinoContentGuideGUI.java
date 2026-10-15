package com.gamblingplugin.gui;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.Bukkit;
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

public class CasinoContentGuideGUI implements Listener {

    public static class GuideHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private final GamblingPlugin plugin;

    public CasinoContentGuideGUI(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(new GuideHolder(), 54, "§6§lVieGambling §8| §0Guía & Compendio");

        ItemStack border = createItem(Material.YELLOW_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, border);
            }
        }

        // Slot 4: Cabecera Principal
        inv.setItem(4, createItem(Material.NETHER_STAR, "§6§l✦ COMPENDIO INTEGRAL DEL CASINO ✦",
                Arrays.asList(
                        "§7Bienvenido a la enciclopedia oficial de VieGambling.",
                        "§7Aquí encontrarás toda la información detallada sobre",
                        "§7juegos de mesa, estructuras 3D, fichas y eventos.",
                        "",
                        "§e▶ Haz clic en cualquier sección para explorar."
                )));

        // Slot 10: Estructuras 3D & Multiblocks Desplegables
        inv.setItem(10, createItem(Material.BEACON, "§e🏛️ Mesas & Multiblocks 3D Desplegables",
                Arrays.asList(
                        "§7Estructuras físicas crafteables con metadatos PDC:",
                        "§8▪ §aMesa de Ruleta 3D §7- Paño verde y aguja giratoria.",
                        "§8▪ §eMesa de Coinflip 3D §7- Moneda suspendida interactiva.",
                        "§8▪ §fMesa de Dados Craps 3D §7- Dados volumétricos parabólicos.",
                        "§8▪ §cTragamonedas 3D Gigante §7- Cabina arcade animada.",
                        "§8▪ §6Mega Rueda de la Fortuna §7- Rueda monumental de 16 sectores.",
                        "§8▪ §bMesa de Keno 3D §7- Bombo extractor con 40 números.",
                        "§8▪ §2Mesa de Blackjack 3D §7- Semicírculo con crupier elegante.",
                        "§8▪ §aMesa de Craps Real 3D §6[NUEVO] §7- Paño verde y dados parabólicos.",
                        "",
                        "§d✦ Desmontaje Inteligente: §7Romper cualquier bloque",
                        "§7desmonta la estructura al 100% y devuelve el ítem."
                )));

        // Slot 12: Juegos Clásicos de Cartas & Azar
        inv.setItem(12, createItem(Material.BOOK, "§a🃏 Juegos Clásicos de Cartas",
                Arrays.asList(
                        "§7Experiencia de juego fiel y balanceada:",
                        "§8▪ §fBlackjack 21: §7Juega contra la banca. Pide carta,",
                        "  §7plántate o dobla. Blackjack paga 3 a 2.",
                        "§8▪ §aBaccarat Punto Banco: §7Apuesta a Jugador,",
                        "  §7Banca (5% comisión) o Empate (pago 8:1).",
                        "§8▪ §6Texas Hold'em Poker: §7Mesas comunitarias con",
                        "  §7flop, turn, river y apuestas multijugador.",
                        "§8▪ §bKeno de la Fortuna: §7Elige de 1 a 10 números;",
                        "  §7sorteo en vivo de 10 bolas con hasta 50x."
                )));

        // Slot 14: Slots, Ruleta & Minijuegos
        inv.setItem(14, createItem(Material.EMERALD, "§c🎰 Tragamonedas & Minijuegos",
                Arrays.asList(
                        "§7Diversión rápida con altos premios:",
                        "§8▪ §cSlots Clásicos: §7Rodillos mecánicos con",
                        "  §7símbolos scatter, wild y jackpots.",
                        "§8▪ §aRuleta Americana: §7Pleno, color, par/impar,",
                        "  §7docenas y columnas con física de giro.",
                        "§8▪ §4Minas (Mines): §7Cuadrícula de gemas y explosivos;",
                        "  §7retira tus ganancias cuando desees.",
                        "§8▪ §eRaspes de la Fortuna: §7Tarjetas rascables",
                        "  §7con premios instantáneos hasta 500x.",
                        "§8▪ §dPlinko Físico: §7Bolas con rebote en clavijas."
                )));

        // Slot 16: Eventos en Vivo & Jackpots
        inv.setItem(16, createItem(Material.FIREWORK_ROCKET, "§6🚀 Eventos en Vivo & Jackpots",
                Arrays.asList(
                        "§7Adrenalina comunitaria en el servidor:",
                        "§8▪ §cCohete Crash 3D: §7Despega hacia el espacio con",
                        "  §7un multiplicador creciente. ¡Cobra antes del crash!",
                        "§8▪ §eMega Jackpot Acumulado: §7Cada apuesta alimenta",
                        "  §7el pozo global anunciado por todo el servidor.",
                        "§8▪ §4Atraco al Casino: §7Oleadas de asaltantes intentan",
                        "  §7saquear las bóvedas; defiéndelas por recompensas.",
                        "§8▪ §6Carrera de Caballos: §7Apuestas en vivo a 4 jinetes."
                )));

        // Slot 28: Fichas VIP & Economía Segura
        inv.setItem(28, createItem(Material.GOLD_INGOT, "§e💱 Fichas de Juego & Economía",
                Arrays.asList(
                        "§7Integración económica con Vault:",
                        "§8▪ §fConversión 1:1 o tasa configurable.",
                        "§8▪ §aBóveda de Seguridad: §7Depósitos con interés semanal.",
                        "§8▪ §6Cashback de Pérdidas: §7Recupera un porcentaje de",
                        "  §7tus apuestas perdidas cada fin de semana.",
                        "§8▪ §bCasino Tycoon: §7Sé dueño de mesas de juego y cobra",
                        "  §7comisiones de las apuestas de otros jugadores."
                )));

        // Slot 30: Club VIP, Rakeback & Pases
        inv.setItem(30, createItem(Material.DIAMOND, "§b✦ Club VIP & Beneficios ✦",
                Arrays.asList(
                        "§7Niveles exclusivos (Bronce, Plata, Oro, Diamante):",
                        "§8▪ §aRakeback Pasivo: §7Reclama fichas por cada apuesta.",
                        "§8▪ §dBar VIP Exclusivo: §7Cócteles con suerte aumentada.",
                        "§8▪ §ePase de Temporada: §7Misiones diarias y semanales",
                        "  §7para desbloquear cosméticos y fichas gratis.",
                        "§8▪ §6Giro Diario: §7Ruleta de la suerte gratuita cada 24h."
                )));

        // Slot 32: Comandos Disponibles
        inv.setItem(32, createItem(Material.WRITABLE_BOOK, "§f📜 Guía de Comandos Rápidos",
                Arrays.asList(
                        "§7Comandos del casino para jugadores y staff:",
                        "§8▪ §e/gamble §7o §e/casino §7- Abre el menú general.",
                        "§8▪ §e/gamble guia §7o §e/gamble contenido §7- Abre esta guía.",
                        "§8▪ §e/gamble test §7- Laboratorio de pruebas completas.",
                        "§8▪ §e/gamble fichas §7- Gestión de fichas del casino.",
                        "§8▪ §e/gamble stats §7- Historial de apuestas personal.",
                        "§8▪ §e/gamble vip §7- Panel de recompensas VIP."
                )));

        // Slot 34: Arquitectura Zero-Dupe & Protección
        inv.setItem(34, createItem(Material.SHIELD, "§2🛡️ Seguridad & Protección Total",
                Arrays.asList(
                        "§7Blindaje del sistema:",
                        "§8▪ §aZero-Duplication: §7Bloques de construcción no",
                        "  §7sueltan ítems vanilla; se reembolsa el PDC exacto.",
                        "§8▪ §aProtección Anti-Robo: §7Todas las GUIs tienen",
                        "  §7bloqueo estricto de arrastre y extracción.",
                        "§8▪ §aPersistencia Atómica: §7Las mesas se guardan",
                        "  §7y recargan de forma segura tras reinicios."
                )));

        // Slot 40: Botón de Acceso al Laboratorio de Test
        inv.setItem(40, createItem(Material.BREWING_STAND, "§a§l🧪 ABRIR LABORATORIO DE PRUEBAS 🧪",
                Arrays.asList(
                        "§7Accede al panel maestro con botones de 1-clic",
                        "§7para spawnear mesas, recibir ítems multiblock,",
                        "§7activar eventos y probar todos los minijuegos.",
                        "",
                        "§a▶ Haz clic para abrir el Laboratorio de Test"
                )));

        // Slot 49: Botón de Cerrar
        inv.setItem(49, createItem(Material.BARRIER, "§c§l✖ Cerrar Compendio",
                Arrays.asList("§7Haz clic para salir del menú.")));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1.1f);
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createItem(Material material, String name) {
        return createItem(material, name, null);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof GuideHolder)) return;

        event.setCancelled(true); // Complete anti-theft GUI protection

        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();
        if (slot >= 54) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (slot == 40) {
            player.closeInventory();
            new CasinoTestLabGUI(plugin).open(player);
        } else if (slot == 49) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_DOOR_CLOSE, 1f, 1f);
        } else {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof GuideHolder) {
            event.setCancelled(true);
        }
    }
}
