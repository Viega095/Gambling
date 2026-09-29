package com.gamblingplugin.effects;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Random;

public class ParticleEffects {

    private static final Random random = new Random();

    // ==================== STRUCTURE AMBIENT EFFECTS ====================

    public static void playRouletteEffect(Location center) {
        World world = center.getWorld();
        if (world == null)
            return;

        // Spinning particles around wheel
        double radius = 1.5;
        for (int i = 0; i < 8; i++) {
            double angle = (System.currentTimeMillis() / 500.0 + i * Math.PI / 4) % (2 * Math.PI);
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location particleLoc = new Location(world, x, center.getY() + 1.5, z);
            world.spawnParticle(Particle.FLAME, particleLoc, 1, 0, 0, 0, 0);
        }
    }

    public static void playDiceEffect(Location center) {
        World world = center.getWorld();
        if (world == null)
            return;

        // Sparkles above dice
        world.spawnParticle(Particle.VILLAGER_HAPPY,
                center.clone().add(0, 2, 0), 3, 0.3, 0.3, 0.3, 0);
    }

    public static void playCoinflipEffect(Location center) {
        World world = center.getWorld();
        if (world == null)
            return;

        // Gold sparkles (coin shimmer)
        world.spawnParticle(Particle.CRIT_MAGIC,
                center.clone().add(0, 1.5, 0), 2, 0.2, 0.2, 0.2, 0);
    }

    public static void playMinesEffect(Location center) {
        World world = center.getWorld();
        if (world == null)
            return;

        // Smoke particles (danger)
        world.spawnParticle(Particle.SMOKE_NORMAL,
                center.clone().add(0, 1, 0), 2, 0.3, 0.3, 0.3, 0);
    }

    public static void playCaseEffect(Location center) {
        World world = center.getWorld();
        if (world == null)
            return;

        // Mystery sparkles
        world.spawnParticle(Particle.ENCHANTMENT_TABLE,
                center.clone().add(0, 1.5, 0), 5, 0.5, 0.5, 0.5, 0);
    }

    public static void playPlinkoEffect(Location center) {
        World world = center.getWorld();
        if (world == null)
            return;

        // Falling particles
        world.spawnParticle(Particle.REDSTONE,
                center.clone().add(0, 2.5, 0), 3, 0.3, 0.3, 0.3, 0);
    }

    public static void playBlackjackEffect(Location center) {
        World world = center.getWorld();
        if (world == null)
            return;

        // Card shuffle effect
        world.spawnParticle(Particle.CLOUD,
                center.clone().add(0, 1.2, 0), 2, 0.4, 0.1, 0.4, 0);
    }

    // ==================== WIN/LOSS EFFECTS ====================

    public static void playWinEffect(Player player, double amount) {
        Location loc = player.getLocation().add(0, 1, 0);
        World world = loc.getWorld();
        if (world == null)
            return;

        // Firework explosion
        world.spawnParticle(Particle.FIREWORKS_SPARK, loc, 50, 0.5, 0.5, 0.5, 0.1);
        world.spawnParticle(Particle.VILLAGER_HAPPY, loc, 20, 1, 1, 1, 0);

        // Sound
        player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        player.playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.5f, 1.5f);
    }

    public static void playLossEffect(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        World world = loc.getWorld();
        if (world == null)
            return;

        // Subtle smoke
        world.spawnParticle(Particle.SMOKE_LARGE, loc, 10, 0.3, 0.3, 0.3, 0.02);

        // Sound
        player.playSound(loc, Sound.ENTITY_VILLAGER_NO, 0.5f, 0.8f);
    }

    public static void playJackpotEffect(Location loc) {
        World world = loc.getWorld();
        if (world == null)
            return;

        // MASSIVE explosion of particles
        world.spawnParticle(Particle.FIREWORKS_SPARK, loc, 200, 2, 2, 2, 0.2);
        world.spawnParticle(Particle.TOTEM, loc, 100, 1.5, 1.5, 1.5, 0.1);
        world.spawnParticle(Particle.END_ROD, loc, 50, 1, 1, 1, 0.15);

        // Epic sounds
        world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_DEATH, 0.5f, 2.0f);
        world.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    }

    public static void playWinStreakEffect(Player player, int streak) {
        Location loc = player.getLocation().add(0, 1, 0);
        World world = loc.getWorld();
        if (world == null)
            return;

        // Progressive intensity
        int particleCount = Math.min(streak * 10, 50);
        world.spawnParticle(Particle.CRIT_MAGIC, loc, particleCount, 0.5, 0.5, 0.5, 0.1);
        world.spawnParticle(Particle.ENCHANTMENT_TABLE, loc, particleCount / 2, 1, 1, 1, 0);

        player.playSound(loc, Sound.BLOCK_BELL_USE, 1.0f, 1.0f + (streak * 0.1f));
    }

    // ==================== SPAWN EFFECTS ====================

    public static void playStructureSpawnEffect(Location loc) {
        World world = loc.getWorld();
        if (world == null)
            return;

        // Lightning strike (visual only - no damage)
        world.strikeLightningEffect(loc);

        // Particle burst
        world.spawnParticle(Particle.EXPLOSION_LARGE, loc.clone().add(0, 1, 0), 3, 0, 0, 0, 0);
        world.spawnParticle(Particle.CLOUD, loc.clone().add(0, 1, 0), 50, 1, 1, 1, 0.1);

        // Epic sound
        world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.2f);
        world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.3f, 2.0f);
    }

    // ==================== SPECIAL GAME EFFECTS ====================

    public static void playRouletteSpinEffect(Location center) {
        World world = center.getWorld();
        if (world == null)
            return;

        // Fast spinning particles
        for (int i = 0; i < 16; i++) {
            double angle = i * Math.PI / 8;
            double x = center.getX() + 1.5 * Math.cos(angle);
            double z = center.getZ() + 1.5 * Math.sin(angle);
            Location particleLoc = new Location(world, x, center.getY() + 1, z);
            world.spawnParticle(Particle.REDSTONE, particleLoc, 1, 0, 0, 0, 0);
        }

        world.playSound(center, Sound.BLOCK_NOTE_BLOCK_HARP, 0.5f, 2.0f);
    }

    public static void playDiceRollEffect(Location center, int result) {
        World world = center.getWorld();
        if (world == null)
            return;

        // Dice tumbling particles
        world.spawnParticle(Particle.CLOUD, center.clone().add(0, 1.5, 0), 20, 0.3, 0.3, 0.3, 0.05);
        world.playSound(center, Sound.BLOCK_WOOD_PLACE, 1.0f, 0.8f);
    }

    public static void playCoinFlipAnimation(Location center) {
        World world = center.getWorld();
        if (world == null)
            return;

        // Spinning trail
        world.spawnParticle(Particle.CRIT_MAGIC,
                center.clone().add(0, 2, 0), 10, 0.1, 0.5, 0.1, 0.1);
        world.playSound(center, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.5f);
    }

    public static void playMineExplosion(Location center) {
        World world = center.getWorld();
        if (world == null)
            return;

        // Explosion effect
        world.spawnParticle(Particle.EXPLOSION_LARGE, center, 1, 0, 0, 0, 0);
        world.spawnParticle(Particle.LAVA, center, 20, 0.5, 0.5, 0.5, 0);
        world.spawnParticle(Particle.SMOKE_LARGE, center, 30, 0.3, 0.3, 0.3, 0.05);

        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);
    }

    public static void playCaseOpenEffect(Location center) {
        World world = center.getWorld();
        if (world == null)
            return;

        // Mystery reveal
        world.spawnParticle(Particle.ENCHANTMENT_TABLE, center, 30, 0.5, 0.5, 0.5, 1);
        world.spawnParticle(Particle.FIREWORKS_SPARK, center, 20, 0.3, 0.3, 0.3, 0.1);

        world.playSound(center, Sound.BLOCK_CHEST_OPEN, 1.0f, 0.8f);
        world.playSound(center, Sound.ENTITY_PLAYER_LEVELUP, 0.3f, 2.0f);
    }

    public static void playPlinkoDropEffect(Location ballLocation) {
        World world = ballLocation.getWorld();
        if (world == null)
            return;

        // Ball trail
        world.spawnParticle(Particle.REDSTONE, ballLocation, 2, 0.05, 0.05, 0.05, 0);
    }

    public static void playCardDealEffect(Location center) {
        World world = center.getWorld();
        if (world == null)
            return;

        // Card swoosh
        world.spawnParticle(Particle.SWEEP_ATTACK, center, 1, 0, 0, 0, 0);
        world.playSound(center, Sound.ENTITY_BAT_TAKEOFF, 0.3f, 1.8f);
    }

    // ==================== UTILITY ====================

    public static void playClickFeedback(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }

    public static void playErrorSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
    }

    public static void playSuccessSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.2f);
    }

    /**
     * Creates a circle of particles
     */
    public static void createParticleCircle(Location center, Particle particle, double radius, int points) {
        World world = center.getWorld();
        if (world == null)
            return;

        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location loc = new Location(world, x, center.getY(), z);
            world.spawnParticle(particle, loc, 1, 0, 0, 0, 0);
        }
    }

    /**
     * Creates a helix of particles
     */
    public static void createParticleHelix(Location center, Particle particle, double height, int rotations) {
        World world = center.getWorld();
        if (world == null)
            return;

        int points = rotations * 20;
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * rotations * i / points;
            double y = center.getY() + (height * i / points);
            double x = center.getX() + 0.5 * Math.cos(angle);
            double z = center.getZ() + 0.5 * Math.sin(angle);
            Location loc = new Location(world, x, y, z);
            world.spawnParticle(particle, loc, 1, 0, 0, 0, 0);
        }
    }
}
