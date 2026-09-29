package com.gamblingplugin.manager;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class DailySpinManager {

    private final GamblingPlugin plugin;
    private final Map<UUID, Long> lastSpins = new HashMap<>();

    public DailySpinManager(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean canSpin(UUID uuid) {
        long last = lastSpins.getOrDefault(uuid, 0L);
        return (System.currentTimeMillis() - last) >= (24 * 3600 * 1000L);
    }

    public void spinWheel(Player player) {
        if (!canSpin(player.getUniqueId())) {
            long remaining = (24 * 3600 * 1000L) - (System.currentTimeMillis() - lastSpins.get(player.getUniqueId()));
            long hours = remaining / (3600 * 1000L);
            long mins = (remaining % (3600 * 1000L)) / (60 * 1000L);
            player.sendMessage(ChatColor.RED + "✖ Ya has girado la Ruleta Diaria hoy. Vuelve en " + hours + "h " + mins + "m.");
            return;
        }

        lastSpins.put(player.getUniqueId(), System.currentTimeMillis());

        double[] prizes = {500.0, 1000.0, 2500.0, 5000.0, 10000.0, 50000.0};
        double prize = prizes[ThreadLocalRandom.current().nextInt(prizes.length)];

        plugin.getEconomyManager().deposit(player, prize);

        player.sendMessage(ChatColor.GOLD + "🎡 [Ruleta Diaria Gratuita] ¡Has ganado " +
                plugin.getEconomyManager().format(prize) + "!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
        player.spawnParticle(Particle.FIREWORKS_SPARK, player.getLocation().add(0, 1, 0), 40, 0.4, 0.6, 0.4, 0.1);
    }
}
