package com.gamblingplugin.games;

import com.gamblingplugin.GamblingPlugin;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CasinoRaceTrack {

    public static class HorseCompetitor {
        public final int id;
        public final String name;
        public final ChatColor color;
        public final double odds;
        public Horse entity;
        public double distanceTraveled = 0.0;
        public double speed = 0.3;

        public HorseCompetitor(int id, String name, ChatColor color, double odds) {
            this.id = id;
            this.name = name;
            this.color = color;
            this.odds = odds;
        }
    }

    public static class RaceBet {
        public final UUID playerUuid;
        public final int horseId;
        public final double amount;

        public RaceBet(UUID playerUuid, int horseId, double amount) {
            this.playerUuid = playerUuid;
            this.horseId = horseId;
            this.amount = amount;
        }
    }

    private final GamblingPlugin plugin;
    private final List<HorseCompetitor> competitors = new ArrayList<>();
    private final List<RaceBet> activeBets = new ArrayList<>();
    private boolean raceInProgress = false;
    private Location trackStartLocation = null;

    public CasinoRaceTrack(GamblingPlugin plugin) {
        this.plugin = plugin;
        initCompetitors();
    }

    private void initCompetitors() {
        competitors.clear();
        competitors.add(new HorseCompetitor(1, "⚡ Relámpago Rojo", ChatColor.RED, 2.4));
        competitors.add(new HorseCompetitor(2, "🌊 Furia Azul", ChatColor.AQUA, 3.1));
        competitors.add(new HorseCompetitor(3, "🌟 Cometa Dorado", ChatColor.GOLD, 4.8));
        competitors.add(new HorseCompetitor(4, "☠ Titán Sombrío", ChatColor.DARK_PURPLE, 7.5));
    }

    public List<HorseCompetitor> getCompetitors() {
        return competitors;
    }

    public boolean isRaceInProgress() {
        return raceInProgress;
    }

    public boolean placeBet(Player player, int horseId, double amount) {
        if (raceInProgress) {
            player.sendMessage(ChatColor.RED + "✖ ¡La carrera ya está en curso! Espera a la siguiente.");
            return false;
        }

        if (horseId < 1 || horseId > competitors.size()) {
            player.sendMessage(ChatColor.RED + "✖ Caballo inválido. Selecciona del 1 al 4.");
            return false;
        }

        if (!plugin.getEconomyManager().hasEnough(player, amount)) {
            player.sendMessage(ChatColor.RED + "✖ No tienes suficiente saldo para apostar " + plugin.getEconomyManager().format(amount));
            return false;
        }

        plugin.getEconomyManager().withdraw(player, amount);
        activeBets.add(new RaceBet(player.getUniqueId(), horseId, amount));

        HorseCompetitor hc = competitors.get(horseId - 1);
        player.sendMessage(ChatColor.GREEN + "🏇 ¡Apuesta de " + plugin.getEconomyManager().format(amount) +
                " colocada en [" + hc.color + hc.name + "§r] con multiplicador de cuota §e" + hc.odds + "x§a!");
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
        return true;
    }

    public boolean startRace(Location startLoc) {
        if (raceInProgress) return false;
        this.raceInProgress = true;
        this.trackStartLocation = startLoc.clone();

        // Spawn Horses
        World world = startLoc.getWorld();
        if (world == null) return false;

        double finishLineDistance = 50.0;
        Vector forward = startLoc.getDirection().setY(0).normalize();
        Vector lateral = new Vector(-forward.getZ(), 0, forward.getX()).normalize();

        for (int i = 0; i < competitors.size(); i++) {
            HorseCompetitor hc = competitors.get(i);
            hc.distanceTraveled = 0.0;
            Location spawnAt = startLoc.clone().add(lateral.clone().multiply((i - 1.5) * 2.0));

            Horse horse = (Horse) world.spawnEntity(spawnAt, EntityType.HORSE);
            horse.setCustomName(hc.color + "🏇 " + hc.name + " §e(" + hc.odds + "x)");
            horse.setCustomNameVisible(true);
            horse.setAdult();
            horse.setTamed(true);
            horse.getInventory().setSaddle(new org.bukkit.inventory.ItemStack(Material.SADDLE));
            hc.entity = horse;
        }

        Bukkit.broadcastMessage("§6🏇 =============================================");
        Bukkit.broadcastMessage("§e🏁 ¡LA GRAN CARRERA DEL HIPÓDROMO HA COMENZADO!");
        Bukkit.broadcastMessage("§7¡Los caballos se disputan la gloria en la pista!");
        Bukkit.broadcastMessage("§6🏇 =============================================");

        new BukkitRunnable() {
            int ticks = 0;
            Random random = new Random();

            @Override
            public void run() {
                ticks++;
                HorseCompetitor leader = null;
                double maxDist = -1;

                for (HorseCompetitor hc : competitors) {
                    if (hc.entity == null || !hc.entity.isValid()) continue;

                    // Surge or stumble calculation
                    double speedDelta = (random.nextDouble() - 0.48) * 0.15;
                    hc.speed = Math.max(0.2, Math.min(0.8, hc.speed + speedDelta));
                    hc.distanceTraveled += hc.speed;

                    // Move entity
                    Location cur = hc.entity.getLocation();
                    Location next = cur.clone().add(forward.clone().multiply(hc.speed));
                    hc.entity.teleport(next);

                    // Particle dust
                    cur.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, cur, 2, 0.2, 0.1, 0.2, 0.01);

                    if (hc.distanceTraveled > maxDist) {
                        maxDist = hc.distanceTraveled;
                        leader = hc;
                    }
                }

                // Send live actionbar update to nearby players
                if (leader != null && trackStartLocation != null) {
                    String progress = String.format("%.0f", (maxDist / finishLineDistance) * 100.0);
                    TextComponent bar = new TextComponent("§e🏁 Líder: " + leader.color + leader.name +
                            " §7| §bProgreso: §f" + progress + "%");
                    for (Player p : trackStartLocation.getWorld().getPlayers()) {
                        if (p.getLocation().distance(trackStartLocation) < 70) {
                            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, bar);
                        }
                    }
                }

                // Check finish
                if (maxDist >= finishLineDistance || ticks >= 300) {
                    finishRace(leader);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 2L);

        return true;
    }

    private void finishRace(HorseCompetitor winner) {
        if (winner == null && !competitors.isEmpty()) winner = competitors.get(0);

        Bukkit.broadcastMessage("§6🏆 =============================================");
        Bukkit.broadcastMessage("§e🎉 ¡GANADOR DE LA CARRERA: " + (winner != null ? winner.color + winner.name : "Nadie") + "§e!");
        Bukkit.broadcastMessage("§6🏆 =============================================");

        if (winner != null && trackStartLocation != null) {
            trackStartLocation.getWorld().playSound(trackStartLocation, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            trackStartLocation.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, trackStartLocation, 60, 2, 2, 2, 0.2);
        }

        // Payout winning bets
        for (RaceBet bet : activeBets) {
            Player p = Bukkit.getPlayer(bet.playerUuid);
            if (winner != null && bet.horseId == winner.id) {
                double payout = bet.amount * winner.odds;
                plugin.getEconomyManager().deposit(bet.playerUuid, payout);
                if (p != null && p.isOnline()) {
                    p.sendMessage(ChatColor.GREEN + "💰 ¡Felicidades! Ganaste " + plugin.getEconomyManager().format(payout) +
                            " en la carrera!");
                    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
                }
            } else {
                if (p != null && p.isOnline()) {
                    p.sendMessage(ChatColor.RED + "✖ Tu caballo no logró ganar esta vez.");
                }
            }
        }

        // Clean up entities
        for (HorseCompetitor hc : competitors) {
            if (hc.entity != null && hc.entity.isValid()) {
                hc.entity.remove();
            }
        }

        activeBets.clear();
        raceInProgress = false;
    }
}
