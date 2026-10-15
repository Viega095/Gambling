package com.gamblingplugin.multiblock;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.structures.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ActiveCasinoMultiblock {

    private final UUID instanceId;
    private final CasinoMultiblockType type;
    private final Location baseLocation;
    private final Set<Location> constituentBlocks = new HashSet<>();
    private final List<UUID> spawnedEntityUuids = new ArrayList<>();
    private Object structureReference;

    public ActiveCasinoMultiblock(UUID instanceId, CasinoMultiblockType type, Location baseLocation) {
        this.instanceId = instanceId;
        this.type = type;
        this.baseLocation = baseLocation.clone();
    }

    public UUID getInstanceId() {
        return instanceId;
    }

    public CasinoMultiblockType getType() {
        return type;
    }

    public Location getBaseLocation() {
        return baseLocation.clone();
    }

    public Set<Location> getConstituentBlocks() {
        return Collections.unmodifiableSet(constituentBlocks);
    }

    public List<UUID> getSpawnedEntityUuids() {
        return Collections.unmodifiableList(spawnedEntityUuids);
    }

    /**
     * Builds physical blocks and spawns 3D animated entities.
     */
    public void build(GamblingPlugin plugin) {
        World world = baseLocation.getWorld();
        if (world == null) return;

        CasinoMultiblockTemplate template = new CasinoMultiblockTemplate(type);

        // 1. Build physical blocks
        for (Map.Entry<CasinoMultiblockTemplate.BlockOffset, Material> entry : template.getRelativeBlocks().entrySet()) {
            Location blockLoc = entry.getKey().toLocation(baseLocation);
            blockLoc.getBlock().setType(entry.getValue(), false);
            constituentBlocks.add(blockLoc.getBlock().getLocation());
        }

        // 2. Spawn 3D interactive entities and link to StructureManager
        Location entityCenter = baseLocation.clone().add(0, template.getEntityOffsetY(), 0);

        // Collect existing entities in area to identify new entities after spawn
        Set<UUID> beforeEntities = new HashSet<>();
        for (Entity e : world.getNearbyEntities(entityCenter, 6, 6, 6)) {
            beforeEntities.add(e.getUniqueId());
        }

        switch (type) {
            case ROULETTE_3D:
                String rouletteId = "Ruleta-" + instanceId.toString().substring(0, 5);
                RouletteWheel wheel = new RouletteWheel(plugin, entityCenter, rouletteId);
                wheel.spawn();
                structureReference = wheel;
                plugin.getStructureManager().getAllRoulettes().put(rouletteId, wheel);
                break;

            case COINFLIP_3D:
                CoinflipStructure coinflip = new CoinflipStructure(plugin, entityCenter);
                coinflip.spawn();
                structureReference = coinflip;
                plugin.getStructureManager().getAllCoinflips().put(entityCenter, coinflip);
                break;

            case DICE_3D:
                DiceStructure dice = new DiceStructure(plugin, entityCenter);
                dice.spawn();
                structureReference = dice;
                plugin.getStructureManager().getAllDice().put(entityCenter, dice);
                break;

            case SLOTS_3D:
                Slots3DStructure slots = new Slots3DStructure(plugin, entityCenter);
                slots.spawn();
                structureReference = slots;
                plugin.getStructureManager().getAllSlots().put(entityCenter, slots);
                break;

            case WHEEL_OF_FORTUNE:
                WheelStructure megaWheel = new WheelStructure(plugin, entityCenter);
                megaWheel.spawn();
                structureReference = megaWheel;
                plugin.getStructureManager().getAllWheels().put(entityCenter, megaWheel);
                break;

            case KENO_3D:
                com.gamblingplugin.structures.KenoTableStructure keno = new com.gamblingplugin.structures.KenoTableStructure(plugin, entityCenter);
                keno.spawn();
                structureReference = keno;
                break;

            case BLACKJACK_3D:
                BlackjackStructure blackjack = new BlackjackStructure(plugin, entityCenter);
                blackjack.spawn();
                structureReference = blackjack;
                plugin.getStructureManager().getAllBlackjack().put(entityCenter, blackjack);
                break;

            case CRAPS_TABLE_3D:
                DiceStructure crapsDice = new DiceStructure(plugin, entityCenter);
                crapsDice.spawn();
                structureReference = crapsDice;
                plugin.getStructureManager().getAllDice().put(entityCenter, crapsDice);
                break;
        }

        // Identify and store spawned entity UUIDs
        for (Entity e : world.getNearbyEntities(entityCenter, 6, 6, 6)) {
            if (!beforeEntities.contains(e.getUniqueId()) && (e instanceof ArmorStand || e.getType().name().contains("DISPLAY"))) {
                spawnedEntityUuids.add(e.getUniqueId());
            }
        }
    }

    /**
     * Completely dismantles the multiblock: removes all physical blocks,
     * terminates all 3D entities, cancels drops, and refunds 1 PDC item.
     */
    public void dismantle(GamblingPlugin plugin, Player player) {
        World world = baseLocation.getWorld();

        // 1. Terminate 3D structure instance
        if (structureReference != null) {
            try {
                structureReference.getClass().getMethod("remove").invoke(structureReference);
            } catch (Throwable ignored) {}
            structureReference = null;
        }

        // 2. Remove all tracked entity UUIDs
        if (world != null) {
            for (UUID uid : spawnedEntityUuids) {
                Entity ent = Bukkit.getEntity(uid);
                if (ent != null && ent.isValid()) {
                    ent.remove();
                }
            }
        }
        spawnedEntityUuids.clear();

        // Also clean up any lingering armorstands at the structure location
        if (world != null) {
            for (Entity ent : world.getNearbyEntities(baseLocation, 5, 5, 5)) {
                if (ent instanceof ArmorStand) {
                    String customName = ent.getCustomName();
                    if (customName != null && (customName.contains("COINFLIP") || customName.contains("DADOS")
                            || customName.contains("TRAGAMONEDAS") || customName.contains("Ruleta")
                            || customName.contains("FORTUNA") || customName.contains("JACKPOT")
                            || customName.contains("▼"))) {
                        ent.remove();
                    }
                }
            }
        }

        // 3. Remove all physical blocks (set to AIR without dropping anything)
        for (Location loc : constituentBlocks) {
            if (loc.getWorld() != null) {
                loc.getBlock().setType(Material.AIR, false);
            }
        }
        constituentBlocks.clear();

        // 4. Refund EXACTLY 1 original PDC item to player (ZERO duplication)
        if (player != null && player.isOnline()) {
            ItemStack refundItem = type.createItemStack(plugin);
            Map<Integer, ItemStack> overflow = player.getInventory().addItem(refundItem);
            if (!overflow.isEmpty()) {
                for (ItemStack remaining : overflow.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), remaining);
                }
            }
            player.sendMessage("§a✔ Estructura " + type.getDisplayName() + " §adesmontada limpiamente. Se te ha reembolsado el ítem original.");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1.2f);
        } else if (world != null) {
            // Drop cleanly at base location if dismantled in player's absence
            ItemStack refundItem = type.createItemStack(plugin);
            world.dropItemNaturally(baseLocation, refundItem);
        }
    }
}
