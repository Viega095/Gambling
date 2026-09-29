package com.gamblingplugin.games;

import com.gamblingplugin.GamblingPlugin;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PhysicalCrashRocket {

    public static class CrashPlayerBet {
        public final UUID uuid;
        public final double betAmount;
        public boolean cashedOut = false;
        public double cashedMultiplier = 0.0;

        public CrashPlayerBet(UUID uuid, double betAmount) {
            this.uuid = uuid;
            this.betAmount = betAmount;
        }
    }

    private final GamblingPlugin plugin;
    private final Map<UUID, CrashPlayerBet> currentBets = new ConcurrentHashMap<>();
    private boolean roundActive = false;
    private ArmorStand rocketStand = null;
    private double currentMultiplier = 1.00;
    private double crashPoint = 1.00;

    public PhysicalCrashRocket(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isRoundActive() {
        return roundActive;
    }

    public double getCurrentMultiplier() {
        return currentMultiplier;
    }

    public boolean joinRound(Player player, double amount) {
        if (roundActive) {
            player.sendMessage(ChatColor.RED + "✖ El cohete ya ha despegado. Espera al siguiente vuelo.");
            return false;
        }

        if (!plugin.getEconomyManager().hasEnough(player, amount)) {
            player.sendMessage(ChatColor.RED + "✖ Saldo insuficiente para apostar " + plugin.getEconomyManager().format(amount));
            return false;
        }

        plugin.getEconomyManager().withdraw(player, amount);
        currentBets.put(player.getUniqueId(), new CrashPlayerBet(player.getUniqueId(), amount));

        player.sendMessage(ChatColor.GREEN + "🚀 ¡Apuesta de " + plugin.getEconomyManager().format(amount) + " registrada para el próximo lanzamiento de Crash 3D!");
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.5f);
        return true;
    }

    public boolean cashOut(Player player) {
        if (!roundActive) {
            player.sendMessage(ChatColor.RED + "✖ No hay ninguna ronda de Crash activa en este momento.");
            return false;
        }

        CrashPlayerBet bet = currentBets.get(player.getUniqueId());
        if (bet == null) {
            player.sendMessage(ChatColor.RED + "✖ No estás participando en esta ronda de vuelo.");
            return false;
        }

        if (bet.cashedOut) {
            player.sendMessage(ChatColor.YELLOW + "Ya aseguraste tus ganancias a " + String.format("%.2fx", bet.cashedMultiplier));
            return false;
        }

        bet.cashedOut = true;
        bet.cashedMultiplier = currentMultiplier;
        double winAmount = bet.betAmount * currentMultiplier;
        plugin.getEconomyManager().deposit(player.getUniqueId(), winAmount);

        player.sendMessage(ChatColor.GREEN + "💰 ¡RETIRADA EXITOSA! Aseguraste " + plugin.getEconomyManager().format(winAmount) +
                " a un multiplicador de " + ChatColor.GOLD + String.format("%.2fx", currentMultiplier) + "!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.8f);
        return true;
    }

    public boolean launchRocket(Location baseLoc) {
        if (roundActive) return false;
        if (currentBets.isEmpty()) return false;

        this.roundActive = true;
        this.currentMultiplier = 1.00;

        // Roll Crash Point with 4% house edge
        double r = Math.random();
        if (r < 0.05) {
            this.crashPoint = 1.00 + (Math.random() * 0.15); // Instant crash
        } else {
            this.crashPoint = 1.00 + (0.96 / (1.0 - r));
        }

        World world = baseLoc.getWorld();
        if (world == null) return false;

        // Spawn Rocket ArmorStand
        Location spawnAt = baseLoc.clone();
        ArmorStand stand = (ArmorStand) world.spawnEntity(spawnAt, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setCustomNameVisible(true);
        stand.setCustomName("§e🚀 Multiplicador: §a§l1.00x");
        stand.setHelmet(new ItemStack(Material.FIREWORK_ROCKET));
        this.rocketStand = stand;

        Bukkit.broadcastMessage("§6🚀 =============================================");
        Bukkit.broadcastMessage("§e☄ ¡COHETE CRASH 3D HA DESPEGADO!");
        Bukkit.broadcastMessage("§7¡Usa §b/gamble cashout §7para asegurar ganancias antes de que explote!");
        Bukkit.broadcastMessage("§6🚀 =============================================");

        new BukkitRunnable() {
            @Override
            public void run() {
                if (rocketStand == null || !rocketStand.isValid()) {
                    cancel();
                    return;
                }

                currentMultiplier += 0.03 + (currentMultiplier * 0.015);
                Location loc = rocketStand.getLocation().add(0, 0.15, 0);
                rocketStand.teleport(loc);
                rocketStand.setCustomName("§e🚀 Multiplicador: §a§l" + String.format("%.2fx", currentMultiplier));

                // Particles
                loc.getWorld().spawnParticle(Particle.FLAME, loc.clone().subtract(0, 0.3, 0), 4, 0.1, 0.1, 0.1, 0.02);
                loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 2, 0.1, 0.1, 0.1, 0.02);

                // Actionbar for participating players
                TextComponent bar = new TextComponent("§e🚀 CRASH: §a§l" + String.format("%.2fx", currentMultiplier) +
                        " §7| §b/gamble cashout");
                for (UUID u : currentBets.keySet()) {
                    Player p = Bukkit.getPlayer(u);
                    if (p != null && p.isOnline()) {
                        CrashPlayerBet pb = currentBets.get(u);
                        if (!pb.cashedOut) {
                            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, bar);
                        }
                    }
                }

                // Check crash
                if (currentMultiplier >= crashPoint) {
                    triggerCrash(loc);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 10L, 2L);

        return true;
    }

    private void triggerCrash(Location loc) {
        loc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc, 1);
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.8f);

        Bukkit.broadcastMessage("§c💥 =============================================");
        Bukkit.broadcastMessage("§4💥 ¡EL COHETE HA EXPLOTADO EN §e" + String.format("%.2fx", currentMultiplier) + "§4!");
        Bukkit.broadcastMessage("§c💥 =============================================");

        for (Map.Entry<UUID, CrashPlayerBet> entry : currentBets.entrySet()) {
            if (!entry.getValue().cashedOut) {
                Player p = Bukkit.getPlayer(entry.getKey());
                if (p != null && p.isOnline()) {
                    p.sendMessage(ChatColor.RED + "☠ ¡No retiraste a tiempo y perdiste tu apuesta de " +
                            plugin.getEconomyManager().format(entry.getValue().betAmount) + "!");
                    p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 0.8f);
                }
            }
        }

        if (rocketStand != null && rocketStand.isValid()) {
            rocketStand.remove();
        }

        currentBets.clear();
        roundActive = false;
    }
}
