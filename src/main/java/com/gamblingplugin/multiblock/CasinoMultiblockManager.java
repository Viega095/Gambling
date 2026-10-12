package com.gamblingplugin.multiblock;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CasinoMultiblockManager {

    private final GamblingPlugin plugin;
    private final NamespacedKey structureKey;
    private final Map<UUID, ActiveCasinoMultiblock> activeMultiblocks = new ConcurrentHashMap<>();
    private final Map<String, UUID> blockToMultiblock = new ConcurrentHashMap<>();
    private File storageFile;
    private FileConfiguration storageConfig;

    public CasinoMultiblockManager(GamblingPlugin plugin) {
        this.plugin = plugin;
        this.structureKey = new NamespacedKey(plugin, "casino_structure");
        initStorage();
    }

    public void init() {
        registerRecipes();
        loadFromDisk();
        plugin.getLogger().info("CasinoMultiblockManager inicializado con " + activeMultiblocks.size() + " estructuras.");
    }

    public void shutdown() {
        saveToDisk();
    }

    public NamespacedKey getStructureKey() {
        return structureKey;
    }

    public void registerRecipes() {
        for (CasinoMultiblockType type : CasinoMultiblockType.values()) {
            type.registerRecipe(plugin);
        }
    }

    public ItemStack createStructureItem(CasinoMultiblockType type) {
        return type.createItemStack(plugin);
    }

    public CasinoMultiblockType getTypeFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(structureKey, PersistentDataType.STRING)) return null;
        String id = pdc.get(structureKey, PersistentDataType.STRING);
        return CasinoMultiblockType.fromId(id);
    }

    public boolean isMultiblockBlock(Location loc) {
        if (loc == null) return false;
        return blockToMultiblock.containsKey(toLocationKey(loc));
    }

    public ActiveCasinoMultiblock getAt(Location loc) {
        if (loc == null) return null;
        UUID id = blockToMultiblock.get(toLocationKey(loc));
        return id != null ? activeMultiblocks.get(id) : null;
    }

    public Map<UUID, ActiveCasinoMultiblock> getActiveMultiblocks() {
        return Collections.unmodifiableMap(activeMultiblocks);
    }

    /**
     * Checks clearance, builds physical blocks, spawns 3D entities, registers index and saves to disk.
     */
    public boolean placeMultiblock(Player player, Location placedLoc, CasinoMultiblockType type) {
        CasinoMultiblockTemplate template = new CasinoMultiblockTemplate(type);

        if (!template.checkClearance(placedLoc)) {
            if (player != null) {
                player.sendMessage("§c[VieGambling] ¡No hay suficiente espacio despejado para desplegar la estructura!");
                player.sendMessage("§7Asegúrate de que el área alrededor esté libre de bloques.");
                player.playSound(placedLoc, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            }
            return false;
        }

        UUID instanceId = UUID.randomUUID();
        ActiveCasinoMultiblock multiblock = new ActiveCasinoMultiblock(instanceId, type, placedLoc);
        multiblock.build(plugin);

        activeMultiblocks.put(instanceId, multiblock);
        for (Location bLoc : multiblock.getConstituentBlocks()) {
            blockToMultiblock.put(toLocationKey(bLoc), instanceId);
        }

        saveToDisk();

        if (player != null) {
            player.sendMessage("§a[VieGambling] ✔ ¡Estructura " + type.getDisplayName() + " §adesplegada correctamente!");
            player.sendMessage("§7Haz clic derecho para interactuar. Romper cualquier bloque la desmontará sin pérdidas.");
            player.playSound(placedLoc, Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.2f);
        }

        return true;
    }

    /**
     * Dismantles multiblock atomically at the given block location.
     */
    public boolean dismantleAt(Location blockLoc, Player player) {
        if (blockLoc == null) return false;
        String key = toLocationKey(blockLoc);
        UUID instanceId = blockToMultiblock.remove(key);
        if (instanceId == null) return false;

        ActiveCasinoMultiblock multiblock = activeMultiblocks.remove(instanceId);
        if (multiblock == null) return false;

        // Remove all constituent blocks from index
        for (Location loc : multiblock.getConstituentBlocks()) {
            blockToMultiblock.remove(toLocationKey(loc));
        }

        // Execute 100% clean dismantle and refund
        multiblock.dismantle(plugin, player);

        saveToDisk();
        return true;
    }

    /**
     * Dismantles all multiblocks in a given radius around location.
     */
    public int dismantleAllNearby(Location center, double radius, Player player) {
        if (center == null || center.getWorld() == null) return 0;
        List<ActiveCasinoMultiblock> toRemove = new ArrayList<>();
        for (ActiveCasinoMultiblock mb : activeMultiblocks.values()) {
            if (mb.getBaseLocation().getWorld().equals(center.getWorld()) &&
                    mb.getBaseLocation().distance(center) <= radius) {
                toRemove.add(mb);
            }
        }

        for (ActiveCasinoMultiblock mb : toRemove) {
            activeMultiblocks.remove(mb.getInstanceId());
            for (Location loc : mb.getConstituentBlocks()) {
                blockToMultiblock.remove(toLocationKey(loc));
            }
            mb.dismantle(plugin, player);
        }

        saveToDisk();
        return toRemove.size();
    }

    private void initStorage() {
        storageFile = new File(plugin.getDataFolder(), "structures.yml");
        if (!storageFile.exists()) {
            try {
                storageFile.getParentFile().mkdirs();
                storageFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("No se pudo crear structures.yml para multiblocks: " + e.getMessage());
            }
        }
        storageConfig = YamlConfiguration.loadConfiguration(storageFile);
    }

    public synchronized void saveToDisk() {
        if (storageConfig == null || storageFile == null) return;
        storageConfig.set("multiblocks", null);

        for (ActiveCasinoMultiblock mb : activeMultiblocks.values()) {
            String path = "multiblocks." + mb.getInstanceId().toString();
            storageConfig.set(path + ".type", mb.getType().getId());
            storageConfig.set(path + ".location", locationToString(mb.getBaseLocation()));
        }

        try {
            storageConfig.save(storageFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Error al guardar estructuras multibloque: " + e.getMessage());
        }
    }

    public synchronized void loadFromDisk() {
        if (storageConfig == null) return;
        ConfigurationSection sec = storageConfig.getConfigurationSection("multiblocks");
        if (sec == null) return;

        for (String idStr : sec.getKeys(false)) {
            try {
                UUID id = UUID.fromString(idStr);
                String typeStr = sec.getString(idStr + ".type");
                String locStr = sec.getString(idStr + ".location");
                CasinoMultiblockType type = CasinoMultiblockType.fromId(typeStr);
                Location loc = stringToLocation(locStr);

                if (type != null && loc != null && loc.getWorld() != null) {
                    ActiveCasinoMultiblock mb = new ActiveCasinoMultiblock(id, type, loc);
                    mb.build(plugin);
                    activeMultiblocks.put(id, mb);
                    for (Location bLoc : mb.getConstituentBlocks()) {
                        blockToMultiblock.put(toLocationKey(bLoc), id);
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error al restaurar multiblock " + idStr + ": " + e.getMessage());
            }
        }
    }

    private String toLocationKey(Location loc) {
        if (loc == null || loc.getWorld() == null) return "";
        return loc.getWorld().getName() + "_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
    }

    private String locationToString(Location loc) {
        if (loc == null || loc.getWorld() == null) return "";
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    private Location stringToLocation(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        try {
            String[] parts = str.split(",");
            World w = Bukkit.getWorld(parts[0]);
            if (w == null) return null;
            return new Location(w, Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
        } catch (Exception e) {
            return null;
        }
    }
}
