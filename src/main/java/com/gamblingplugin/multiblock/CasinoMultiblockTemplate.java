package com.gamblingplugin.multiblock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.*;

public class CasinoMultiblockTemplate {

    public static class BlockOffset {
        private final int dx;
        private final int dy;
        private final int dz;

        public BlockOffset(int dx, int dy, int dz) {
            this.dx = dx;
            this.dy = dy;
            this.dz = dz;
        }

        public int getDx() {
            return dx;
        }

        public int getDy() {
            return dy;
        }

        public int getDz() {
            return dz;
        }

        public Location toLocation(Location base) {
            return base.clone().add(dx, dy, dz);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BlockOffset that = (BlockOffset) o;
            return dx == that.dx && dy == that.dy && dz == that.dz;
        }

        @Override
        public int hashCode() {
            return Objects.hash(dx, dy, dz);
        }
    }

    private final CasinoMultiblockType type;
    private final Map<BlockOffset, Material> relativeBlocks = new LinkedHashMap<>();
    private final double entityOffsetY;

    public CasinoMultiblockTemplate(CasinoMultiblockType type) {
        this.type = type;
        this.entityOffsetY = computeEntityOffsetY(type);
        initializeBlocks();
    }

    private double computeEntityOffsetY(CasinoMultiblockType type) {
        return switch (type) {
            case ROULETTE_3D -> 0.0;
            case COINFLIP_3D -> 1.0;
            case DICE_3D -> 1.0;
            case SLOTS_3D -> 0.2;
            case WHEEL_OF_FORTUNE -> 3.0;
            case KENO_3D -> 1.0;
            case BLACKJACK_3D -> 0.8;
        };
    }

    private void initializeBlocks() {
        switch (type) {
            case ROULETTE_3D:
                // Base at 0,0,0
                relativeBlocks.put(new BlockOffset(0, 0, 0), Material.GREEN_GLAZED_TERRACOTTA);

                // Felt circle (radius 1-2)
                relativeBlocks.put(new BlockOffset(1, 0, 0), Material.GREEN_CONCRETE);
                relativeBlocks.put(new BlockOffset(-1, 0, 0), Material.GREEN_CONCRETE);
                relativeBlocks.put(new BlockOffset(0, 0, 1), Material.GREEN_CONCRETE);
                relativeBlocks.put(new BlockOffset(0, 0, -1), Material.GREEN_CONCRETE);
                relativeBlocks.put(new BlockOffset(1, 0, 1), Material.GREEN_CONCRETE);
                relativeBlocks.put(new BlockOffset(-1, 0, 1), Material.GREEN_CONCRETE);
                relativeBlocks.put(new BlockOffset(1, 0, -1), Material.GREEN_CONCRETE);
                relativeBlocks.put(new BlockOffset(-1, 0, -1), Material.GREEN_CONCRETE);

                // Wood border
                relativeBlocks.put(new BlockOffset(2, 0, 0), Material.DARK_OAK_PLANKS);
                relativeBlocks.put(new BlockOffset(-2, 0, 0), Material.DARK_OAK_PLANKS);
                relativeBlocks.put(new BlockOffset(0, 0, 2), Material.DARK_OAK_PLANKS);
                relativeBlocks.put(new BlockOffset(0, 0, -2), Material.DARK_OAK_PLANKS);
                relativeBlocks.put(new BlockOffset(2, 0, 1), Material.DARK_OAK_PLANKS);
                relativeBlocks.put(new BlockOffset(2, 0, -1), Material.DARK_OAK_PLANKS);
                relativeBlocks.put(new BlockOffset(-2, 0, 1), Material.DARK_OAK_PLANKS);
                relativeBlocks.put(new BlockOffset(-2, 0, -1), Material.DARK_OAK_PLANKS);
                relativeBlocks.put(new BlockOffset(1, 0, 2), Material.DARK_OAK_PLANKS);
                relativeBlocks.put(new BlockOffset(-1, 0, 2), Material.DARK_OAK_PLANKS);
                relativeBlocks.put(new BlockOffset(1, 0, -2), Material.DARK_OAK_PLANKS);
                relativeBlocks.put(new BlockOffset(-1, 0, -2), Material.DARK_OAK_PLANKS);
                break;

            case COINFLIP_3D:
                // Base at 0,0,0
                relativeBlocks.put(new BlockOffset(0, 0, 0), Material.YELLOW_GLAZED_TERRACOTTA);

                // Pedestal & Table top
                relativeBlocks.put(new BlockOffset(1, 0, 0), Material.SMOOTH_QUARTZ);
                relativeBlocks.put(new BlockOffset(-1, 0, 0), Material.SMOOTH_QUARTZ);
                relativeBlocks.put(new BlockOffset(0, 0, 1), Material.SMOOTH_QUARTZ);
                relativeBlocks.put(new BlockOffset(0, 0, -1), Material.SMOOTH_QUARTZ);

                // Gold Corners
                relativeBlocks.put(new BlockOffset(1, 0, 1), Material.GOLD_BLOCK);
                relativeBlocks.put(new BlockOffset(-1, 0, 1), Material.GOLD_BLOCK);
                relativeBlocks.put(new BlockOffset(1, 0, -1), Material.GOLD_BLOCK);
                relativeBlocks.put(new BlockOffset(-1, 0, -1), Material.GOLD_BLOCK);
                break;

            case DICE_3D:
                // Base at 0,0,0
                relativeBlocks.put(new BlockOffset(0, 0, 0), Material.WHITE_GLAZED_TERRACOTTA);

                // Craps felt (Red concrete)
                relativeBlocks.put(new BlockOffset(1, 0, 0), Material.RED_CONCRETE);
                relativeBlocks.put(new BlockOffset(-1, 0, 0), Material.RED_CONCRETE);
                relativeBlocks.put(new BlockOffset(0, 0, 1), Material.RED_CONCRETE);
                relativeBlocks.put(new BlockOffset(0, 0, -1), Material.RED_CONCRETE);

                // Bone Block corners
                relativeBlocks.put(new BlockOffset(1, 0, 1), Material.BONE_BLOCK);
                relativeBlocks.put(new BlockOffset(-1, 0, 1), Material.BONE_BLOCK);
                relativeBlocks.put(new BlockOffset(1, 0, -1), Material.BONE_BLOCK);
                relativeBlocks.put(new BlockOffset(-1, 0, -1), Material.BONE_BLOCK);

                // Wood rims
                relativeBlocks.put(new BlockOffset(2, 0, 0), Material.DARK_OAK_PLANKS);
                relativeBlocks.put(new BlockOffset(-2, 0, 0), Material.DARK_OAK_PLANKS);
                relativeBlocks.put(new BlockOffset(2, 0, 1), Material.DARK_OAK_PLANKS);
                relativeBlocks.put(new BlockOffset(-2, 0, 1), Material.DARK_OAK_PLANKS);
                relativeBlocks.put(new BlockOffset(2, 0, -1), Material.DARK_OAK_PLANKS);
                relativeBlocks.put(new BlockOffset(-2, 0, -1), Material.DARK_OAK_PLANKS);
                break;

            case SLOTS_3D:
                // Base center at 0,0,0
                relativeBlocks.put(new BlockOffset(0, 0, 0), Material.RED_GLAZED_TERRACOTTA);

                // Cabinet frame
                relativeBlocks.put(new BlockOffset(-1, 0, 0), Material.POLISHED_BLACKSTONE);
                relativeBlocks.put(new BlockOffset(1, 0, 0), Material.POLISHED_BLACKSTONE);

                // Light pillars
                relativeBlocks.put(new BlockOffset(-1, 1, 0), Material.REDSTONE_LAMP);
                relativeBlocks.put(new BlockOffset(1, 1, 0), Material.REDSTONE_LAMP);

                // Back wall
                relativeBlocks.put(new BlockOffset(0, 0, -1), Material.POLISHED_BLACKSTONE);
                relativeBlocks.put(new BlockOffset(-1, 0, -1), Material.POLISHED_BLACKSTONE);
                relativeBlocks.put(new BlockOffset(1, 0, -1), Material.POLISHED_BLACKSTONE);
                relativeBlocks.put(new BlockOffset(0, 1, -1), Material.POLISHED_BLACKSTONE);
                relativeBlocks.put(new BlockOffset(-1, 1, -1), Material.POLISHED_BLACKSTONE);
                relativeBlocks.put(new BlockOffset(1, 1, -1), Material.POLISHED_BLACKSTONE);

                // Top canopy
                relativeBlocks.put(new BlockOffset(0, 2, 0), Material.GOLD_BLOCK);
                relativeBlocks.put(new BlockOffset(-1, 2, 0), Material.POLISHED_BLACKSTONE_SLAB);
                relativeBlocks.put(new BlockOffset(1, 2, 0), Material.POLISHED_BLACKSTONE_SLAB);
                relativeBlocks.put(new BlockOffset(0, 2, -1), Material.POLISHED_BLACKSTONE);
                break;

            case WHEEL_OF_FORTUNE:
                // Base at 0,0,0
                relativeBlocks.put(new BlockOffset(0, 0, 0), Material.ORANGE_GLAZED_TERRACOTTA);

                // Base sides
                relativeBlocks.put(new BlockOffset(-1, 0, 0), Material.DARK_OAK_WOOD);
                relativeBlocks.put(new BlockOffset(1, 0, 0), Material.DARK_OAK_WOOD);

                // Pillar
                relativeBlocks.put(new BlockOffset(0, 1, 0), Material.IRON_BARS);
                relativeBlocks.put(new BlockOffset(0, 2, 0), Material.TARGET);
                break;

            case KENO_3D:
                // Base at 0,0,0
                relativeBlocks.put(new BlockOffset(0, 0, 0), Material.CYAN_GLAZED_TERRACOTTA);

                // Quartz Felt Table
                relativeBlocks.put(new BlockOffset(1, 0, 0), Material.SMOOTH_QUARTZ);
                relativeBlocks.put(new BlockOffset(-1, 0, 0), Material.SMOOTH_QUARTZ);
                relativeBlocks.put(new BlockOffset(0, 0, 1), Material.SMOOTH_QUARTZ);
                relativeBlocks.put(new BlockOffset(0, 0, -1), Material.SMOOTH_QUARTZ);

                // Sea Lantern and Gold corners
                relativeBlocks.put(new BlockOffset(1, 0, 1), Material.SEA_LANTERN);
                relativeBlocks.put(new BlockOffset(-1, 0, 1), Material.SEA_LANTERN);
                relativeBlocks.put(new BlockOffset(1, 0, -1), Material.GOLD_BLOCK);
                relativeBlocks.put(new BlockOffset(-1, 0, -1), Material.GOLD_BLOCK);
                break;

            case BLACKJACK_3D:
                // Base at 0,0,0
                relativeBlocks.put(new BlockOffset(0, 0, 0), Material.GREEN_TERRACOTTA);

                // Table felt (green concrete semicircle)
                relativeBlocks.put(new BlockOffset(1, 0, 0), Material.GREEN_CONCRETE);
                relativeBlocks.put(new BlockOffset(-1, 0, 0), Material.GREEN_CONCRETE);
                relativeBlocks.put(new BlockOffset(2, 0, 0), Material.DARK_OAK_PLANKS);
                relativeBlocks.put(new BlockOffset(-2, 0, 0), Material.DARK_OAK_PLANKS);

                // Front player arc
                relativeBlocks.put(new BlockOffset(0, 0, 1), Material.GREEN_CONCRETE);
                relativeBlocks.put(new BlockOffset(1, 0, 1), Material.GREEN_CONCRETE);
                relativeBlocks.put(new BlockOffset(-1, 0, 1), Material.GREEN_CONCRETE);
                relativeBlocks.put(new BlockOffset(0, 0, 2), Material.DARK_OAK_PLANKS);
                relativeBlocks.put(new BlockOffset(1, 0, 2), Material.DARK_OAK_PLANKS);
                relativeBlocks.put(new BlockOffset(-1, 0, 2), Material.DARK_OAK_PLANKS);

                // Dealer side back border
                relativeBlocks.put(new BlockOffset(0, 0, -1), Material.POLISHED_BLACKSTONE);
                relativeBlocks.put(new BlockOffset(1, 0, -1), Material.DARK_OAK_PLANKS);
                relativeBlocks.put(new BlockOffset(-1, 0, -1), Material.DARK_OAK_PLANKS);
                break;
        }
    }

    public Map<BlockOffset, Material> getRelativeBlocks() {
        return Collections.unmodifiableMap(relativeBlocks);
    }

    public double getEntityOffsetY() {
        return entityOffsetY;
    }

    /**
     * Checks if the area is clear for placement (all required blocks except 0,0,0 must be air or replaceable).
     */
    public boolean checkClearance(Location baseLocation) {
        World world = baseLocation.getWorld();
        if (world == null) return false;

        for (BlockOffset offset : relativeBlocks.keySet()) {
            Location targetLoc = offset.toLocation(baseLocation);
            if (targetLoc.getBlockY() < world.getMinHeight() || targetLoc.getBlockY() >= world.getMaxHeight()) {
                return false;
            }
            Block b = world.getBlockAt(targetLoc);
            Material mat = b.getType();
            if (mat == Material.BEDROCK || mat == Material.BARRIER || mat == Material.END_PORTAL ||
                    mat == Material.END_PORTAL_FRAME || mat == Material.COMMAND_BLOCK ||
                    mat == Material.STRUCTURE_BLOCK) {
                return false;
            }
        }
        return true;
    }
}
