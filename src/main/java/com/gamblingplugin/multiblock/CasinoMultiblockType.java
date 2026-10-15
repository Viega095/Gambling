package com.gamblingplugin.multiblock;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum CasinoMultiblockType {

    ROULETTE_3D(
            "ROULETTE_3D",
            "§a§lMesa de Ruleta 3D",
            Material.GREEN_GLAZED_TERRACOTTA,
            Arrays.asList(
                    "§7Estructura de casino desplegable.",
                    "§7Colócala en el suelo para construir una",
                    "§7auténtica mesa de ruleta americana 3D.",
                    "",
                    "§8▪ §aPaño verde con números y aguja 3D.",
                    "§8▪ §eRomper cualquier bloque la desmonta.",
                    "§8▪ §eDevuelve el ítem original sin pérdidas."
            )
    ),

    COINFLIP_3D(
            "COINFLIP_3D",
            "§e§lMesa de Coinflip 3D",
            Material.YELLOW_GLAZED_TERRACOTTA,
            Arrays.asList(
                    "§7Estructura de casino desplegable.",
                    "§7Colócala en el suelo para erigir una",
                    "§7mesa de cara o cruz con moneda 3D.",
                    "",
                    "§8▪ §aPedestal de cuarzo y oro animado.",
                    "§8▪ §eRomper cualquier bloque la desmonta.",
                    "§8▪ §eDevuelve el ítem original sin pérdidas."
            )
    ),

    DICE_3D(
            "DICE_3D",
            "§f§lMesa de Dados 3D",
            Material.WHITE_GLAZED_TERRACOTTA,
            Arrays.asList(
                    "§7Estructura de casino desplegable.",
                    "§7Colócala en el suelo para desplegar una",
                    "§7mesa de dados craps con física 3D.",
                    "",
                    "§8▪ §aPaño rojo con dados parabólicos 3D.",
                    "§8▪ §eRomper cualquier bloque la desmonta.",
                    "§8▪ §eDevuelve el ítem original sin pérdidas."
            )
    ),

    SLOTS_3D(
            "SLOTS_3D",
            "§c§lTragamonedas 3D Gigante",
            Material.RED_GLAZED_TERRACOTTA,
            Arrays.asList(
                    "§7Estructura de casino desplegable.",
                    "§7Colócala en el suelo para ensamblar una",
                    "§7cabina arcade tragamonedas con rodillos 3D.",
                    "",
                    "§8▪ §aCabina iluminada con palanca mecánica.",
                    "§8▪ §eRomper cualquier bloque la desmonta.",
                    "§8▪ §eDevuelve el ítem original sin pérdidas."
            )
    ),

    WHEEL_OF_FORTUNE(
            "WHEEL_OF_FORTUNE",
            "§6§lMega Rueda de la Fortuna",
            Material.ORANGE_GLAZED_TERRACOTTA,
            Arrays.asList(
                    "§7Estructura de casino desplegable.",
                    "§7Colócala en el suelo para erigir una",
                    "§7imponente rueda de la fortuna de 16 sectores.",
                    "",
                    "§8▪ §aRueda vertical con aguja y premios.",
                    "§8▪ §eRomper cualquier bloque la desmonta.",
                    "§8▪ §eDevuelve el ítem original sin pérdidas."
            )
    ),

    KENO_3D(
            "KENO_3D",
            "§6§lMesa de Keno 3D",
            Material.CYAN_GLAZED_TERRACOTTA,
            Arrays.asList(
                    "§7Estructura de casino desplegable.",
                    "§7Colócala en el suelo para desplegar una",
                    "§7mesa de Keno con bombo extractor holográfico 3D.",
                    "",
                    "§8▪ §aBombo interactivo con sorteo de 10 bolas.",
                    "§8▪ §eRomper cualquier bloque la desmonta.",
                    "§8▪ §eDevuelve el ítem original sin pérdidas."
            )
    ),

    BLACKJACK_3D(
            "BLACKJACK_3D",
            "§2§lMesa de Blackjack 3D",
            Material.GREEN_TERRACOTTA,
            Arrays.asList(
                    "§7Estructura de casino desplegable.",
                    "§7Colócala en el suelo para erigir una",
                    "§7mesa reglamentaria de Blackjack 21 con crupier 3D.",
                    "",
                    "§8▪ §aMesa semicircular verde con crupier animado.",
                    "§8▪ §eRomper cualquier bloque la desmonta.",
                    "§8▪ §eDevuelve el ítem original sin pérdidas."
            )
    ),

    CRAPS_TABLE_3D(
            "CRAPS_TABLE_3D",
            "§a§lMesa de Craps Real 3D",
            Material.LIME_TERRACOTTA,
            Arrays.asList(
                    "§7Estructura de casino desplegable.",
                    "§7Colócala en el suelo para erigir una",
                    "§7auténtica mesa reglamentaria de dados Craps.",
                    "",
                    "§8▪ §aPaño verde esmeralda con área de tiro.",
                    "§8▪ §eRomper cualquier bloque la desmonta.",
                    "§8▪ §eDevuelve el ítem original sin pérdidas."
            )
    );

    private final String id;
    private final String displayName;
    private final Material baseMaterial;
    private final List<String> lore;

    CasinoMultiblockType(String id, String displayName, Material baseMaterial, List<String> lore) {
        this.id = id;
        this.displayName = displayName;
        this.baseMaterial = baseMaterial;
        this.lore = lore;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getBaseMaterial() {
        return baseMaterial;
    }

    public Material getIconMaterial() {
        return baseMaterial;
    }

    public List<String> getLore() {
        return new ArrayList<>(lore);
    }

    /**
     * Generates a craftable ItemStack tagged with PersistentDataContainer key "casino_structure".
     */
    public ItemStack createItemStack(GamblingPlugin plugin) {
        ItemStack item = new ItemStack(baseMaterial, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(lore);

            NamespacedKey key = new NamespacedKey(plugin, "casino_structure");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, this.id);

            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Registers a balanced shaped crafting recipe in the Spigot server.
     */
    public void registerRecipe(GamblingPlugin plugin) {
        NamespacedKey recipeKey = new NamespacedKey(plugin, "recipe_" + id.toLowerCase());

        // Remove existing recipe if already present (e.g. reload)
        try {
            Bukkit.removeRecipe(recipeKey);
        } catch (Throwable ignored) {}

        ItemStack result = createItemStack(plugin);
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, result);

        switch (this) {
            case ROULETTE_3D:
                // W: GREEN_WOOL, D: DARK_OAK_PLANKS, G: GOLD_INGOT, C: COMPASS, R: REDSTONE
                recipe.shape("WDW", "GCG", "RDR");
                recipe.setIngredient('W', Material.GREEN_WOOL);
                recipe.setIngredient('D', Material.DARK_OAK_PLANKS);
                recipe.setIngredient('G', Material.GOLD_INGOT);
                recipe.setIngredient('C', Material.COMPASS);
                recipe.setIngredient('R', Material.REDSTONE);
                break;

            case COINFLIP_3D:
                // G: GOLD_BLOCK, S: SUNFLOWER, I: IRON_INGOT, Q: SMOOTH_QUARTZ
                recipe.shape("GSG", "IQI", "SIS");
                recipe.setIngredient('G', Material.GOLD_BLOCK);
                recipe.setIngredient('S', Material.SUNFLOWER);
                recipe.setIngredient('I', Material.IRON_INGOT);
                recipe.setIngredient('Q', Material.SMOOTH_QUARTZ);
                break;

            case DICE_3D:
                // B: BONE_BLOCK, N: GOLD_NUGGET, R: RED_WOOL, W: WHITE_CONCRETE, D: DARK_OAK_PLANKS
                recipe.shape("BNB", "RWR", "DND");
                recipe.setIngredient('B', Material.BONE_BLOCK);
                recipe.setIngredient('N', Material.GOLD_NUGGET);
                recipe.setIngredient('R', Material.RED_WOOL);
                recipe.setIngredient('W', Material.WHITE_CONCRETE);
                recipe.setIngredient('D', Material.DARK_OAK_PLANKS);
                break;

            case SLOTS_3D:
                // I: IRON_BLOCK, L: REDSTONE_LAMP, D: DIAMOND, S: DISPENSER, R: REDSTONE, P: POLISHED_BLACKSTONE
                recipe.shape("ILI", "DSD", "RPR");
                recipe.setIngredient('I', Material.IRON_BLOCK);
                recipe.setIngredient('L', Material.REDSTONE_LAMP);
                recipe.setIngredient('D', Material.DIAMOND);
                recipe.setIngredient('S', Material.DISPENSER);
                recipe.setIngredient('R', Material.REDSTONE);
                recipe.setIngredient('P', Material.POLISHED_BLACKSTONE);
                break;

            case WHEEL_OF_FORTUNE:
                // E: EMERALD, T: TARGET, G: GOLD_BLOCK, W: DIAMOND_BLOCK, O: DARK_OAK_LOG, I: IRON_BARS
                recipe.shape("ETE", "GWG", "OIO");
                recipe.setIngredient('E', Material.EMERALD);
                recipe.setIngredient('T', Material.TARGET);
                recipe.setIngredient('G', Material.GOLD_BLOCK);
                recipe.setIngredient('W', Material.DIAMOND_BLOCK);
                recipe.setIngredient('O', Material.DARK_OAK_LOG);
                recipe.setIngredient('I', Material.IRON_BARS);
                break;

            case KENO_3D:
                // S: SEA_LANTERN, G: GOLD_BLOCK, Q: SMOOTH_QUARTZ, D: DIAMOND, C: COMPASS, B: CYAN_WOOL
                recipe.shape("SGS", "DCD", "QBQ");
                recipe.setIngredient('S', Material.SEA_LANTERN);
                recipe.setIngredient('G', Material.GOLD_BLOCK);
                recipe.setIngredient('D', Material.DIAMOND);
                recipe.setIngredient('C', Material.COMPASS);
                recipe.setIngredient('Q', Material.SMOOTH_QUARTZ);
                recipe.setIngredient('B', Material.CYAN_WOOL);
                break;

            case BLACKJACK_3D:
                // G: GREEN_WOOL, D: DARK_OAK_PLANKS, P: PAPER, E: EMERALD, C: LEATHER_CHESTPLATE
                recipe.shape("GGG", "DCD", "PEP");
                recipe.setIngredient('G', Material.GREEN_WOOL);
                recipe.setIngredient('D', Material.DARK_OAK_PLANKS);
                recipe.setIngredient('P', Material.PAPER);
                recipe.setIngredient('E', Material.EMERALD);
                recipe.setIngredient('C', Material.LEATHER_CHESTPLATE);
                break;

            case CRAPS_TABLE_3D:
                // L: LIME_WOOL, B: BONE_BLOCK, D: DARK_OAK_PLANKS, G: GOLD_INGOT, R: REDSTONE
                recipe.shape("LBL", "DGD", "RDR");
                recipe.setIngredient('L', Material.LIME_WOOL);
                recipe.setIngredient('B', Material.BONE_BLOCK);
                recipe.setIngredient('D', Material.DARK_OAK_PLANKS);
                recipe.setIngredient('G', Material.GOLD_INGOT);
                recipe.setIngredient('R', Material.REDSTONE);
                break;
        }

        try {
            Bukkit.addRecipe(recipe);
        } catch (Throwable e) {
            plugin.getLogger().warning("No se pudo registrar receta para " + id + ": " + e.getMessage());
        }
    }

    public static CasinoMultiblockType fromId(String id) {
        if (id == null) return null;
        for (CasinoMultiblockType type : values()) {
            if (type.getId().equalsIgnoreCase(id)) return type;
        }
        return null;
    }
}
