package com.gamblingplugin.listeners;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomBetListener implements Listener {

    private final GamblingPlugin plugin;
    private final Map<UUID, BetRequest> activeBetRequests = new HashMap<>();

    public CustomBetListener(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public void requestBet(Player player, String gameType, String selection) {
        activeBetRequests.put(player.getUniqueId(), new BetRequest(gameType, selection));
        player.closeInventory();
        player.sendMessage("");
        player.sendMessage("§6§l[VieGambling] §eEscribe tu apuesta personalizada en el chat:");
        player.sendMessage("§7Ejemplo: §a500 §7o §a1250.50");
        player.sendMessage("§7Escribe §c'cancelar' §7para cancelar");
        player.sendMessage("");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!activeBetRequests.containsKey(uuid)) {
            return;
        }

        event.setCancelled(true);
        BetRequest request = activeBetRequests.remove(uuid);
        String message = event.getMessage().trim();

        // Cancel
        if (message.equalsIgnoreCase("cancelar") || message.equalsIgnoreCase("cancel")) {
            player.sendMessage("§c§l[VieGambling] §7Apuesta cancelada.");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 1f);
            return;
        }

        // Parse bet amount
        double amount;
        try {
            amount = Double.parseDouble(message);
        } catch (NumberFormatException e) {
            player.sendMessage("§c§l[VieGambling] §cCantidad inválida. Intenta de nuevo o escribe 'cancelar'.");
            activeBetRequests.put(uuid, request); // Put it back
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1f);
            return;
        }

        // Validate amount
        if (amount <= 0) {
            player.sendMessage("§c§l[VieGambling] §cLa apuesta debe ser mayor a 0.");
            activeBetRequests.put(uuid, request);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1f);
            return;
        }

        if (amount > 1000000) {
            player.sendMessage("§c§l[VieGambling] §cLa apuesta máxima es $1,000,000.");
            activeBetRequests.put(uuid, request);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1f);
            return;
        }

        // Check balance
        if (!plugin.getEconomyManager().has(player, amount)) {
            player.sendMessage("§c§l[VieGambling] §cNo tienes suficiente dinero.");
            player.sendMessage("§7Necesitas: §a$" + String.format("%.2f", amount));
            player.sendMessage("§7Tienes: §a$" + String.format("%.2f", plugin.getEconomyManager().getBalance(player)));
            activeBetRequests.put(uuid, request);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1f);
            return;
        }

        // Success - open game GUI with custom bet
        final double finalAmount = amount;
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            player.sendMessage(
                    "§a§l[VieGambling] §7Apuesta de §a$" + String.format("%.2f", finalAmount) + " §7aceptada!");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);

            // Open appropriate GUI based on game type
            com.gamblingplugin.gui.BettingGUI bettingGUI = plugin.getBettingGUI();
            bettingGUI.openWithCustomBet(player, request.gameType, request.selection, finalAmount);
        });
    }

    private static class BetRequest {
        final String gameType;
        final String selection;

        BetRequest(String gameType, String selection) {
            this.gameType = gameType;
            this.selection = selection;
        }
    }
}
