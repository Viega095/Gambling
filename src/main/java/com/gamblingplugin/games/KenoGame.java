package com.gamblingplugin.games;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;

public class KenoGame extends Game {

    public KenoGame(GamblingPlugin plugin) {
        super(plugin, "keno");
    }

    @Override
    public void play(Player player, double bet, String[] args) {
        // Fallback for command execution /gamble keno <bet>
        player.sendMessage("§e[VieGambling] Abre la mesa o interfaz de Keno para elegir tus números de la suerte.");
        new com.gamblingplugin.gui.KenoGUI(plugin).open(player);
    }

    public void playKeno(Player player, double bet, Set<Integer> chosenNumbers, Runnable onFinish) {
        if (!plugin.getEconomyManager().has(player, bet)) {
            player.sendMessage("§c✖ No tienes suficiente dinero para jugar Keno ($" + String.format("%.2f", bet) + ").");
            return;
        }

        plugin.getEconomyManager().withdraw(player, bet);
        plugin.getJackpotManager().contributeBet(bet);

        // Draw 10 distinct random numbers between 1 and 40
        List<Integer> pool = new ArrayList<>();
        for (int i = 1; i <= 40; i++) pool.add(i);
        Collections.shuffle(pool);
        Set<Integer> drawnNumbers = new HashSet<>(pool.subList(0, 10));

        // Count matches
        int matches = 0;
        List<Integer> matchedList = new ArrayList<>();
        for (int n : chosenNumbers) {
            if (drawnNumbers.contains(n)) {
                matches++;
                matchedList.add(n);
            }
        }

        double multiplier = calculateMultiplier(chosenNumbers.size(), matches);
        double payout = bet * multiplier;

        player.sendMessage("§6═══════════════════════════════════════════");
        player.sendMessage("§e§l🎱 SORTEO DE KENO 3D §7(Apuesta: §a$" + String.format("%.2f", bet) + "§7)");
        player.sendMessage("§7Tus números: §f" + chosenNumbers);
        player.sendMessage("§7Bolas extraídas: §e" + drawnNumbers);
        player.sendMessage("§7Aciertos (§a" + matches + "§7): §a" + matchedList);

        if (multiplier > 0) {
            plugin.getEconomyManager().deposit(player, payout);
            player.sendMessage("§a§l✔ ¡PREMIO! §7Ganaste §a$" + String.format("%.2f", payout) + " §7(" + multiplier + "x)");
            player.sendTitle("§a§l¡KENO GANADOR!", "§e+" + plugin.getEconomyManager().format(payout) + " §7(" + matches + " Aciertos)", 5, 45, 10);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f);
            player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 1.5, 0), 25, 0.5, 0.5, 0.5, 0);
        } else {
            player.sendMessage("§c§l✖ No alcanzaste el mínimo de aciertos para ganar.");
            player.sendTitle("§c§lSIN PREMIO", "§7" + matches + " aciertos. ¡Prueba otra combinación!", 5, 35, 10);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.8f);
        }
        player.sendMessage("§6═══════════════════════════════════════════");

        if (onFinish != null) onFinish.run();
    }

    public static double calculateMultiplier(int chosenCount, int matches) {
        if (chosenCount <= 0 || matches <= 0) return 0.0;
        double ratio = (double) matches / chosenCount;

        if (matches == 1 && chosenCount <= 3) return 1.5;
        if (matches == 2) return 2.2;
        if (matches == 3) return 4.5;
        if (matches == 4) return 9.0;
        if (matches == 5) return 18.0;
        if (matches == 6) return 30.0;
        if (matches >= 7) return 50.0;
        return ratio >= 0.5 ? 2.0 : 0.0;
    }
}
