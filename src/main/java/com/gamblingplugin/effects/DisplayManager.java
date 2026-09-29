package com.gamblingplugin.effects;

import org.bukkit.entity.Player;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class DisplayManager {

    /**
     * Send a title to the player
     */
    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    /**
     * Send an action bar message
     */
    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }

    // ==================== WIN/LOSS DISPLAYS ====================

    public static void showWinTitle(Player player, double amount) {
        String title = "§6§l¡GANASTE!";
        String subtitle = "§e+$" + String.format("%.2f", amount);
        sendTitle(player, title, subtitle, 10, 40, 10);
    }

    public static void showLossTitle(Player player) {
        String title = "§c§lPERDISTE";
        String subtitle = "§7Mejor suerte la próxima vez";
        sendTitle(player, title, subtitle, 10, 30, 10);
    }

    public static void showPushTitle(Player player) {
        String title = "§e§lEMPATE";
        String subtitle = "§7Apuesta devuelta";
        sendTitle(player, title, subtitle, 10, 30, 10);
    }

    public static void showJackpotTitle(Player player, double amount) {
        String title = "§6§l✦ JACKPOT ✦";
        String subtitle = "§e§l$" + String.format("%.2f", amount);
        sendTitle(player, title, subtitle, 20, 60, 20);
    }

    public static void showWinStreakTitle(Player player, int streak) {
        String title = "§d§l⚡ RACHA!";
        String subtitle = "§5" + streak + " victorias consecutivas";
        sendTitle(player, title, subtitle, 10, 40, 10);
    }

    // ==================== ACTION BAR UPDATES ====================

    public static void updateBalance(Player player, double balance) {
        String message = "§6Balance: §e$" + String.format("%.2f", balance);
        sendActionBar(player, message);
    }

    public static void updateBetProgress(Player player, String gameName, double bet) {
        String message = "§7Jugando §e" + gameName + " §7| Apuesta: §6$" + String.format("%.2f", bet);
        sendActionBar(player, message);
    }

    public static void updateMultiplier(Player player, double multiplier) {
        String message = "§aMultiplicador: §e§lx" + String.format("%.1f", multiplier);
        sendActionBar(player, message);
    }

    public static void showCooldown(Player player, int seconds) {
        String message = "§c⏳ Espera " + seconds + "s antes de jugar de nuevo";
        sendActionBar(player, message);
    }

    public static void showInsufficientFunds(Player player, double needed) {
        String message = "§c✖ Fondos insuficientes | Necesitas: §e$" + String.format("%.2f", needed);
        sendActionBar(player, message);
    }

    // ==================== GAME-SPECIFIC DISPLAYS ====================

    public static void showRouletteResult(Player player, String color, int number) {
        String colorCode = color.equalsIgnoreCase("red") ? "§c" : color.equalsIgnoreCase("black") ? "§8" : "§a";
        String title = colorCode + "§l" + number;
        String subtitle = colorCode + color.toUpperCase();
        sendTitle(player, title, subtitle, 5, 30, 10);
    }

    public static void showDiceResult(Player player, int result) {
        String title = "§6§l🎲 " + result + " 🎲";
        String subtitle = "§eResultado del dado";
        sendTitle(player, title, subtitle, 5, 30, 10);
    }

    public static void showCoinflipResult(Player player, String result) {
        String emoji = result.equalsIgnoreCase("heads") ? "🪙" : "🔘";
        String title = "§6§l" + emoji + " " + result.toUpperCase() + " " + emoji;
        String subtitle = "§eResultado de lanzamiento";
        sendTitle(player, title, subtitle, 5, 30, 10);
    }

    public static void showBlackjackScore(Player player, int score) {
        String message = "§7Tu mano: §e§l" + score;
        sendActionBar(player, message);
    }

    public static void showBlackjackResult(Player player, String result, int playerScore, int dealerScore) {
        String title;
        switch (result.toLowerCase()) {
            case "blackjack":
                title = "§6§l✦ BLACKJACK! ✦";
                break;
            case "win":
                title = "§a§l¡GANASTE!";
                break;
            case "bust":
                title = "§c§lTE PASASTE!";
                break;
            case "push":
                title = "§e§lEMPATE";
                break;
            case "loss":
                title = "§c§lPERDISTE";
                break;
            default:
                title = "§7" + result;
        }
        String subtitle = "§7Tu: §e" + playerScore + " §7| Dealer: §e" + dealerScore;
        sendTitle(player, title, subtitle, 10, 40, 10);
    }

    public static void showMinesProgress(Player player, int tilesRevealed, double currentMultiplier) {
        String message = "§7Baldosas: §a" + tilesRevealed + " §7| Multiplicador: §e§lx" +
                String.format("%.2f", currentMultiplier);
        sendActionBar(player, message);
    }

    public static void showPlinkoLanding(Player player, double multiplier) {
        String color = multiplier >= 5.0 ? "§6" : multiplier >= 2.0 ? "§a" : multiplier >= 1.0 ? "§e" : "§c";
        String title = color + "§lx" + String.format("%.1f", multiplier);
        String subtitle = "§7Multiplicador de Plinko";
        sendTitle(player, title, subtitle, 5, 30, 10);
    }

    // ==================== ADMIN NOTIFICATIONS ====================

    public static void showAdminNotification(Player admin, String message) {
        String title = "§c§lADMIN";
        String subtitle = "§7" + message;
        sendTitle(admin, title, subtitle, 10, 40, 10);
    }

    public static void showStructureCreated(Player admin, String structureType) {
        String message = "§a✓ " + structureType + " creado exitosamente";
        sendActionBar(admin, message);
    }

    public static void showStructureDeleted(Player admin, String structureType) {
        String message = "§c✖ " + structureType + " eliminado";
        sendActionBar(admin, message);
    }

    // ==================== FIRST WIN BONUS ====================

    public static void showFirstWinBonus(Player player) {
        String title = "§d§l⭐ PRIMERA VICTORIA ⭐";
        String subtitle = "§5¡Bonus del día!";
        sendTitle(player, title, subtitle, 10, 50, 10);
    }

    // ==================== BETTING DISPLAYS ====================

    public static void showBetPlaced(Player player, String game, double amount) {
        String message = "§7Apuesta colocada en §e" + game + " §7: §6$" + String.format("%.2f", amount);
        sendActionBar(player, message);
    }

    public static void showBetTooHigh(Player player, double max) {
        String message = "§c✖ Apuesta máxima: §e$" + String.format("%.2f", max);
        sendActionBar(player, message);
    }

    public static void showBetTooLow(Player player, double min) {
        String message = "§c✖ Apuesta mínima: §e$" + String.format("%.2f", min);
        sendActionBar(player, message);
    }

    // ==================== UTILITY ====================

    public static void clearTitle(Player player) {
        player.resetTitle();
    }

    public static void showError(Player player, String error) {
        String message = "§c✖ " + error;
        sendActionBar(player, message);
    }

    public static void showSuccess(Player player, String success) {
        String message = "§a✓ " + success;
        sendActionBar(player, message);
    }
}
