package com.gamblingplugin.manager;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LotteryDrawEngine {

    private final GamblingPlugin plugin;
    private final Map<UUID, Integer> tickets = new ConcurrentHashMap<>();
    private double progressivePot = 1000.0;
    private final double ticketPrice = 50.0;
    private int countdownSeconds = 1800; // 30 minutes

    public LotteryDrawEngine(GamblingPlugin plugin) {
        this.plugin = plugin;
        startLotteryTimer();
    }

    private void startLotteryTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (countdownSeconds > 0) {
                    countdownSeconds--;
                    if (countdownSeconds == 300 || countdownSeconds == 60) {
                        Bukkit.broadcastMessage("§6[Lotería] §e¡Quedan §f" + (countdownSeconds / 60) + " minutos §epara el gran sorteo de la lotería! Pozo actual: §a$" + String.format("%.2f", progressivePot));
                    }
                } else {
                    executeDraw();
                    countdownSeconds = 1800;
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public double getProgressivePot() {
        return progressivePot;
    }

    public double getTicketPrice() {
        return ticketPrice;
    }

    public int getTickets(UUID uuid) {
        return tickets.getOrDefault(uuid, 0);
    }

    public int getTotalTickets() {
        return tickets.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int getRemainingSeconds() {
        return countdownSeconds;
    }

    public boolean buyTickets(Player player, int count) {
        if (count <= 0) return false;
        double cost = count * ticketPrice;

        if (plugin.getEconomyManager().getEconomy() != null) {
            if (!plugin.getEconomyManager().has(player, cost)) {
                player.sendMessage(ChatColor.RED + "✖ No tienes suficiente dinero. Necesitas " + plugin.getEconomyManager().format(cost));
                return false;
            }
            plugin.getEconomyManager().withdraw(player, cost);
        }

        tickets.put(player.getUniqueId(), tickets.getOrDefault(player.getUniqueId(), 0) + count);
        progressivePot += cost * 0.90; // 90% goes into the pot

        player.sendMessage(ChatColor.GREEN + "✔ ¡Has comprado §e" + count + " boletos §ade lotería por " + (plugin.getEconomyManager().getEconomy() != null ? plugin.getEconomyManager().format(cost) : "$" + cost) + "!");
        player.sendMessage(ChatColor.GRAY + "Tienes un total de §e" + tickets.get(player.getUniqueId()) + " boletos §7para el próximo sorteo.");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
        return true;
    }

    public void executeDraw() {
        if (tickets.isEmpty()) {
            Bukkit.broadcastMessage("§6[Lotería] §eEl sorteo no tuvo participantes. El pozo de §a$" + String.format("%.2f", progressivePot) + " §ese acumula para el próximo sorteo.");
            return;
        }

        List<UUID> ticketPool = new ArrayList<>();
        for (Map.Entry<UUID, Integer> entry : tickets.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                ticketPool.add(entry.getKey());
            }
        }

        UUID winnerUUID = ticketPool.get(new Random().nextInt(ticketPool.size()));
        OfflinePlayer winnerPlayer = Bukkit.getOfflinePlayer(winnerUUID);
        String winnerName = winnerPlayer.getName() != null ? winnerPlayer.getName() : "Desconocido";
        double wonAmount = progressivePot;

        Bukkit.broadcastMessage("§6╔════════════════════════════════════════════════╗");
        Bukkit.broadcastMessage("§6║      §e🎉 ¡GRAN SORTEO DE LA LOTERÍA NACIONAL!     §6║");
        Bukkit.broadcastMessage("§6╠════════════════════════════════════════════════╝");
        Bukkit.broadcastMessage("§6║ §aGanador afortunado: §e§l" + winnerName);
        Bukkit.broadcastMessage("§6║ §aPremio entregado: §6§l" + (plugin.getEconomyManager().getEconomy() != null ? plugin.getEconomyManager().format(wonAmount) : "$" + wonAmount));
        Bukkit.broadcastMessage("§6║ §7Total de boletos jugados: §f" + ticketPool.size());
        Bukkit.broadcastMessage("§6╚════════════════════════════════════════════════╝");

        if (plugin.getEconomyManager().getEconomy() != null) {
            plugin.getEconomyManager().deposit(winnerUUID, wonAmount);
        }

        if (winnerPlayer.isOnline() && winnerPlayer.getPlayer() != null) {
            Player p = winnerPlayer.getPlayer();
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            spawnFireworks(p.getLocation());
        }

        tickets.clear();
        progressivePot = 1000.0;
    }

    private void spawnFireworks(Location loc) {
        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder()
                .withColor(Color.YELLOW, Color.ORANGE, Color.GREEN)
                .withFade(Color.WHITE)
                .with(FireworkEffect.Type.BALL_LARGE)
                .withTrail()
                .build());
        meta.setPower(1);
        fw.setFireworkMeta(meta);
    }
}
