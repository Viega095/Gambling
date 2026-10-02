package com.gamblingplugin.games;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.structures.CrashStructure;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

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
    private final List<ArmorStand> giantRocketStands = new ArrayList<>();
    private ArmorStand holoStand = null;
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

        player.sendMessage(ChatColor.GREEN + "🚀 ¡Apuesta de " + plugin.getEconomyManager().format(amount) + " registrada para el próximo lanzamiento del Cohete Crash 3D!");
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

        // Visual cashout effect near rocket
        if (holoStand != null && holoStand.isValid()) {
            holoStand.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, holoStand.getLocation(), 25, 1.0, 1.0, 1.0, 0.1);
        }
        return true;
    }

    public boolean launchRocket(Location fallbackLoc) {
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

        // Determine optimal launch location: Check if there is a CrashStructure in the world!
        Location launchLoc = null;
        CrashStructure nearestStructure = plugin.getStructureManager().getNearestCrashStructure(fallbackLoc);
        if (nearestStructure != null) {
            launchLoc = nearestStructure.getCenter().clone().add(0, 1.0, 0);
        } else {
            // Spawn 5 blocks in front of fallback player location to not block their camera view
            launchLoc = fallbackLoc.clone();
            org.bukkit.util.Vector dir = launchLoc.getDirection().setY(0);
            if (dir.lengthSquared() > 0.01) {
                dir.normalize().multiply(5.0);
                launchLoc.add(dir);
            } else {
                launchLoc.add(3, 0, 3);
            }
            launchLoc.add(0, 1.0, 0);
        }

        World world = launchLoc.getWorld();
        if (world == null) return false;

        cleanupStands();

        // 🚀 CREATE GIGANTIC 3D ROCKET COMPRISING MULTIPLE SEGMENTS 🚀
        // 1. Nose Cone (Top)
        ArmorStand noseStand = createRocketSegment(world, launchLoc.clone().add(0, 2.2, 0), Material.REDSTONE_BLOCK, false);
        // 2. Fuselage Main Core (Middle)
        ArmorStand bodyStand = createRocketSegment(world, launchLoc.clone().add(0, 1.2, 0), Material.IRON_BLOCK, false);
        // 3. Engine Thrusters (Bottom)
        ArmorStand thrusterStand = createRocketSegment(world, launchLoc.clone().add(0, 0.2, 0), Material.DISPENSER, false);

        // 4. Large Floating Holographic Display (Above Rocket)
        holoStand = (ArmorStand) world.spawnEntity(launchLoc.clone().add(0, 3.5, 0), EntityType.ARMOR_STAND);
        holoStand.setVisible(false);
        holoStand.setGravity(false);
        holoStand.setCustomNameVisible(true);
        holoStand.setCustomName("§6🚀 §e§lAPOLLO CRASH 3D §8[ §a§l1.00x §8]");

        giantRocketStands.add(noseStand);
        giantRocketStands.add(bodyStand);
        giantRocketStands.add(thrusterStand);
        giantRocketStands.add(holoStand);

        Bukkit.broadcastMessage("§6🚀 =============================================");
        Bukkit.broadcastMessage("§e☄ ¡EL GIGANTESCO COHETE CRASH 3D HA DESPEGADO!");
        Bukkit.broadcastMessage("§7¡Usa §b/gamble cashout §7o haz clic en el menú para asegurar tus ganancias!");
        Bukkit.broadcastMessage("§6🚀 =============================================");

        final Location currentPos = launchLoc.clone();

        new BukkitRunnable() {
            double flightTick = 0;

            @Override
            public void run() {
                if (holoStand == null || !holoStand.isValid() || !roundActive) {
                    cancel();
                    return;
                }

                flightTick++;
                currentMultiplier += 0.025 + (currentMultiplier * 0.012);

                // Ascend motion
                double speed = 0.12 + Math.min(0.25, flightTick * 0.003);
                currentPos.add(0, speed, 0);

                // Teleport all segments synchronously
                if (noseStand.isValid()) noseStand.teleport(currentPos.clone().add(0, 2.2, 0));
                if (bodyStand.isValid()) bodyStand.teleport(currentPos.clone().add(0, 1.2, 0));
                if (thrusterStand.isValid()) thrusterStand.teleport(currentPos.clone().add(0, 0.2, 0));
                if (holoStand.isValid()) {
                    holoStand.teleport(currentPos.clone().add(0, 3.5, 0));
                    holoStand.setCustomName(getMultiplierFormattedTitle(currentMultiplier));
                }

                // Giant Thruster Exhaust Particles
                Location exhaust = currentPos.clone().add(0, -0.2, 0);
                world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, exhaust, 8, 0.25, 0.2, 0.25, 0.02);
                world.spawnParticle(Particle.FLAME, exhaust, 15, 0.3, 0.4, 0.3, 0.05);
                world.spawnParticle(Particle.SOUL_FIRE_FLAME, exhaust, 6, 0.15, 0.2, 0.15, 0.03);
                world.spawnParticle(Particle.LAVA, exhaust, 3, 0.1, 0.1, 0.1, 0.01);

                // Sound effect every 6 ticks
                if (flightTick % 4 == 0) {
                    world.playSound(currentPos, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.2f, 0.6f);
                    world.playSound(currentPos, Sound.ENTITY_MINECART_RIDING, 0.8f, 1.8f);
                }

                // Actionbar for participating players
                String actionText = "§e🚀 CRASH: " + getMultiplierColor(currentMultiplier) + String.format("%.2fx", currentMultiplier) +
                        " §7| §b/gamble cashout §7o Clic Panel";
                TextComponent bar = new TextComponent(actionText);

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
                    triggerCrash(currentPos);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 5L, 2L);

        return true;
    }

    private ArmorStand createRocketSegment(World world, Location loc, Material headMat, boolean small) {
        ArmorStand stand = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setSmall(small);
        stand.setHelmet(new ItemStack(headMat));
        return stand;
    }

    private String getMultiplierColor(double mult) {
        if (mult < 2.0) return "§a§l";
        if (mult < 5.0) return "§e§l";
        if (mult < 10.0) return "§6§l";
        if (mult < 25.0) return "§c§l";
        return "§d§l★ ";
    }

    private String getMultiplierFormattedTitle(double mult) {
        String color = getMultiplierColor(mult);
        return "§6🚀 §e§lAPOLLO CRASH §8[ " + color + String.format("%.2fx", mult) + " §8]";
    }

    private void triggerCrash(Location loc) {
        loc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc.clone().add(0, 1.5, 0), 3, 0.5, 0.5, 0.5, 0.1);
        loc.getWorld().spawnParticle(Particle.LAVA, loc, 35, 1.5, 1.5, 1.5, 0.2);
        loc.getWorld().spawnParticle(Particle.FLAME, loc, 50, 1.2, 1.2, 1.2, 0.1);
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.6f);

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

        cleanupStands();
        currentBets.clear();
        roundActive = false;
    }

    public void cleanupStands() {
        for (ArmorStand stand : giantRocketStands) {
            if (stand != null && stand.isValid()) {
                stand.remove();
            }
        }
        giantRocketStands.clear();
        if (holoStand != null && holoStand.isValid()) {
            holoStand.remove();
            holoStand = null;
        }
    }
}
