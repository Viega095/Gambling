package com.gamblingplugin.manager;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private static final long DEFAULT_COOLDOWN = 3000; // 3 seconds

    public boolean hasCooldown(Player player, String gameType) {
        UUID uuid = player.getUniqueId();
        if (!cooldowns.containsKey(uuid)) {
            return false;
        }

        Map<String, Long> playerCooldowns = cooldowns.get(uuid);
        if (!playerCooldowns.containsKey(gameType)) {
            return false;
        }

        long lastPlayed = playerCooldowns.get(gameType);
        return System.currentTimeMillis() - lastPlayed < DEFAULT_COOLDOWN;
    }

    public long getRemainingCooldown(Player player, String gameType) {
        UUID uuid = player.getUniqueId();
        if (!cooldowns.containsKey(uuid) || !cooldowns.get(uuid).containsKey(gameType)) {
            return 0;
        }

        long lastPlayed = cooldowns.get(uuid).get(gameType);
        long remaining = DEFAULT_COOLDOWN - (System.currentTimeMillis() - lastPlayed);
        return Math.max(0, remaining);
    }

    public void setCooldown(Player player, String gameType) {
        UUID uuid = player.getUniqueId();
        cooldowns.computeIfAbsent(uuid, k -> new HashMap<>());
        cooldowns.get(uuid).put(gameType, System.currentTimeMillis());
    }

    public void removeCooldown(Player player, String gameType) {
        UUID uuid = player.getUniqueId();
        if (cooldowns.containsKey(uuid)) {
            cooldowns.get(uuid).remove(gameType);
        }
    }
}
