package com.gamblingplugin.events;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.Calendar;
import java.util.List;

public class GlobalEventLock {

    public static final String META_ACTIVE = "GLOBAL_EVENT_LOCK_ACTIVE";
    public static final String META_TITLE = "GLOBAL_EVENT_LOCK_TITLE";
    public static final String META_PLUGIN = "GLOBAL_EVENT_LOCK_PLUGIN";
    public static final String META_EXPIRES = "GLOBAL_EVENT_LOCK_EXPIRES";

    private static World getMainWorld() {
        List<World> worlds = Bukkit.getWorlds();
        return worlds.isEmpty() ? null : worlds.get(0);
    }

    public static boolean isEventActive() {
        World world = getMainWorld();
        if (world == null || !world.hasMetadata(META_ACTIVE)) {
            return false;
        }
        long expires = getExpiresTimestamp();
        if (expires > 0 && System.currentTimeMillis() >= expires) {
            clearLock();
            return false;
        }
        List<MetadataValue> list = world.getMetadata(META_ACTIVE);
        return !list.isEmpty() && list.get(0).asBoolean();
    }

    public static String getActiveEventTitle() {
        World world = getMainWorld();
        if (world == null || !world.hasMetadata(META_TITLE)) return "Ninguno";
        List<MetadataValue> list = world.getMetadata(META_TITLE);
        return list.isEmpty() ? "Ninguno" : list.get(0).asString();
    }

    public static String getActivePlugin() {
        World world = getMainWorld();
        if (world == null || !world.hasMetadata(META_PLUGIN)) return "Ninguno";
        List<MetadataValue> list = world.getMetadata(META_PLUGIN);
        return list.isEmpty() ? "Ninguno" : list.get(0).asString();
    }

    public static long getExpiresTimestamp() {
        World world = getMainWorld();
        if (world == null || !world.hasMetadata(META_EXPIRES)) return 0;
        List<MetadataValue> list = world.getMetadata(META_EXPIRES);
        return list.isEmpty() ? 0 : list.get(0).asLong();
    }

    public static long getRemainingSeconds() {
        if (!isEventActive()) return 0;
        long remaining = (getExpiresTimestamp() - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }

    public static synchronized boolean acquireLock(Plugin plugin, String eventTitle, int durationSeconds) {
        if (isEventActive()) {
            return false;
        }
        World world = getMainWorld();
        if (world == null) return false;

        long expires = System.currentTimeMillis() + (durationSeconds * 1000L);
        world.setMetadata(META_ACTIVE, new FixedMetadataValue(plugin, true));
        world.setMetadata(META_TITLE, new FixedMetadataValue(plugin, eventTitle));
        world.setMetadata(META_PLUGIN, new FixedMetadataValue(plugin, plugin.getName()));
        world.setMetadata(META_EXPIRES, new FixedMetadataValue(plugin, expires));
        return true;
    }

    public static synchronized void releaseLock(Plugin plugin) {
        World world = getMainWorld();
        if (world != null) {
            world.removeMetadata(META_ACTIVE, plugin);
            world.removeMetadata(META_TITLE, plugin);
            world.removeMetadata(META_PLUGIN, plugin);
            world.removeMetadata(META_EXPIRES, plugin);
        }
    }

    public static synchronized void clearLock() {
        World world = getMainWorld();
        if (world != null) {
            for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
                world.removeMetadata(META_ACTIVE, p);
                world.removeMetadata(META_TITLE, p);
                world.removeMetadata(META_PLUGIN, p);
                world.removeMetadata(META_EXPIRES, p);
            }
        }
    }

    public static String getNextScheduledEventName() {
        int minute = Calendar.getInstance().get(Calendar.MINUTE);
        if (minute < 10) return "§b🎣 Marea Abisal / Torneo (EconomyFish) [En Curso]";
        if (minute < 15) return "§d🌸 Florecimiento Astral (FlorArcana) en " + (15 - minute) + "m";
        if (minute < 25) return "§d🌸 Florecimiento Astral (FlorArcana) [En Curso]";
        if (minute < 30) return "§c⚔ Incursión del Abismo (RPGCore) en " + (30 - minute) + "m";
        if (minute < 40) return "§c⚔ Incursión del Abismo (RPGCore) [En Curso]";
        if (minute < 45) return "§6🎰 Hora Dorada del Casino (VieGambling) en " + (45 - minute) + "m";
        if (minute < 55) return "§6🎰 Hora Dorada del Casino (VieGambling) [En Curso]";
        return "§b🎣 Marea Abisal / Torneo (EconomyFish) en " + (60 - minute) + "m";
    }
}
