package com.gamblingplugin.manager;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.structures.CoinflipStructure;
import com.gamblingplugin.structures.DiceStructure;
import com.gamblingplugin.structures.RouletteWheel;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class StructureManager {

    private final GamblingPlugin plugin;
    private final Map<String, RouletteWheel> rouletteWheels = new HashMap<>(); // ID -> Wheel
    private final Map<Location, DiceStructure> diceStructures = new HashMap<>();
    private final Map<Location, CoinflipStructure> coinflipStructures = new HashMap<>();
    private final Map<Location, com.gamblingplugin.structures.MinesStructure> minesStructures = new HashMap<>();
    private final Map<Location, com.gamblingplugin.structures.CaseStructure> caseStructures = new HashMap<>();
    private final Map<Location, com.gamblingplugin.structures.PlinkoStructure> plinkoStructures = new HashMap<>();
    private final Map<Location, com.gamblingplugin.structures.BlackjackStructure> blackjackStructures = new HashMap<>();
    private final Map<Location, com.gamblingplugin.structures.CrashStructure> crashStructures = new HashMap<>();
    private final Map<Location, com.gamblingplugin.structures.BaccaratStructure> baccaratStructures = new HashMap<>();
    private final Map<Location, com.gamblingplugin.structures.LotteryStructure> lotteryStructures = new HashMap<>();
    private final Map<Location, com.gamblingplugin.structures.VIPLoungeStructure> vipLoungeStructures = new HashMap<>();
    private File structuresFile;
    private FileConfiguration structuresConfig;
    private int nextRouletteId = 1;

    public StructureManager(GamblingPlugin plugin) {
        this.plugin = plugin;
        loadStructuresConfig();
        loadStructures();
    }

    private void loadStructuresConfig() {
        structuresFile = new File(plugin.getDataFolder(), "structures.yml");
        if (!structuresFile.exists()) {
            try {
                structuresFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create structures.yml!");
            }
        }
        structuresConfig = YamlConfiguration.loadConfiguration(structuresFile);
    }

    private void saveStructures() {
        // Clear existing roulette data to rewrite
        structuresConfig.set("roulettes", null);
        // Save roulette wheels with IDs
        for (Map.Entry<String, RouletteWheel> entry : rouletteWheels.entrySet()) {
            String id = entry.getKey();
            Location loc = entry.getValue().getCenter();
            structuresConfig.set("roulettes." + id, locationToString(loc));
        }

        // Save next ID
        structuresConfig.set("next-roulette-id", nextRouletteId);

        // Save dice structures
        List<String> diceLocations = new ArrayList<>();
        for (Location loc : diceStructures.keySet()) {
            diceLocations.add(locationToString(loc));
        }
        structuresConfig.set("dice", diceLocations);

        // Save coinflips
        List<String> coinflipLocations = new ArrayList<>();
        for (Location loc : coinflipStructures.keySet()) {
            coinflipLocations.add(locationToString(loc));
        }
        structuresConfig.set("coinflips", coinflipLocations);

        // Save mines
        List<String> minesLocations = new ArrayList<>();
        for (Location loc : minesStructures.keySet()) {
            minesLocations.add(locationToString(loc));
        }
        structuresConfig.set("mines", minesLocations);

        // Save cases
        List<String> caseLocations = new ArrayList<>();
        for (Location loc : caseStructures.keySet()) {
            caseLocations.add(locationToString(loc));
        }
        structuresConfig.set("cases", caseLocations);

        // Save plinko
        List<String> plinkoLocations = new ArrayList<>();
        for (Location loc : plinkoStructures.keySet()) {
            plinkoLocations.add(locationToString(loc));
        }
        structuresConfig.set("plinko", plinkoLocations);

        try {
            structuresConfig.save(structuresFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save structures.yml!");
        }
    }

    private void loadStructures() {
        // Load next ID
        nextRouletteId = structuresConfig.getInt("next-roulette-id", 1);

        // Load roulette wheels
        if (structuresConfig.getConfigurationSection("roulettes") != null) {
            for (String id : structuresConfig.getConfigurationSection("roulettes").getKeys(false)) {
                String locStr = structuresConfig.getString("roulettes." + id);
                Location loc = stringToLocation(locStr);
                if (loc != null) {
                    RouletteWheel wheel = new RouletteWheel(plugin, loc, id);
                    wheel.spawn();
                    rouletteWheels.put(id, wheel);
                    // Update next ID if necessary
                    try {
                        int idNum = Integer.parseInt(id.replace("Ruleta-", ""));
                        if (idNum >= nextRouletteId) {
                            nextRouletteId = idNum + 1;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        // Load dice structures
        List<String> diceLocations = structuresConfig.getStringList("dice");
        for (String locStr : diceLocations) {
            Location loc = stringToLocation(locStr);
            if (loc != null) {
                DiceStructure dice = new DiceStructure(plugin, loc);
                dice.spawn();
                diceStructures.put(loc, dice);
            }
        }

        // Load coinflips
        List<String> coinflipLocations = structuresConfig.getStringList("coinflips");
        for (String locStr : coinflipLocations) {
            Location loc = stringToLocation(locStr);
            if (loc != null) {
                CoinflipStructure coinflip = new CoinflipStructure(plugin, loc);
                coinflip.spawn();
                coinflipStructures.put(loc, coinflip);
            }
        }

        // Load mines
        List<String> minesLocations = structuresConfig.getStringList("mines");
        for (String locStr : minesLocations) {
            Location loc = stringToLocation(locStr);
            if (loc != null) {
                com.gamblingplugin.structures.MinesStructure mines = new com.gamblingplugin.structures.MinesStructure(
                        plugin, loc);
                mines.spawn();
                minesStructures.put(loc, mines);
            }
        }

        // Load cases
        List<String> caseLocations = structuresConfig.getStringList("cases");
        for (String locStr : caseLocations) {
            Location loc = stringToLocation(locStr);
            if (loc != null) {
                com.gamblingplugin.structures.CaseStructure caseStructure = new com.gamblingplugin.structures.CaseStructure(
                        plugin, loc);
                caseStructure.spawn();
                caseStructures.put(loc, caseStructure);
            }
        }

        // Load plinko
        List<String> plinkoLocations = structuresConfig.getStringList("plinko");
        for (String locStr : plinkoLocations) {
            Location loc = stringToLocation(locStr);
            if (loc != null) {
                com.gamblingplugin.structures.PlinkoStructure plinkoStructure = new com.gamblingplugin.structures.PlinkoStructure(
                        plugin, loc);
                plinkoStructure.spawn();
                plinkoStructures.put(loc, plinkoStructure);
            }
        }

        if (!rouletteWheels.isEmpty() || !diceStructures.isEmpty() || !coinflipStructures.isEmpty()
                || !minesStructures.isEmpty() || !caseStructures.isEmpty() || !plinkoStructures.isEmpty()) {
            plugin.getLogger().info("Restored " + rouletteWheels.size() + " roulettes, " + diceStructures.size()
                    + " dice, " + coinflipStructures.size() + " coinflips, " + minesStructures.size() + " mines, "
                    + caseStructures.size() + " cases, and " + plinkoStructures.size() + " plinko");
        }
    }

    private String locationToString(Location loc) {
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
    }

    private Location stringToLocation(String str) {
        try {
            String[] parts = str.split(",");
            return new Location(
                    plugin.getServer().getWorld(parts[0]),
                    Double.parseDouble(parts[1]),
                    Double.parseDouble(parts[2]),
                    Double.parseDouble(parts[3]));
        } catch (Exception e) {
            plugin.getLogger().warning("Invalid location string: " + str);
            return null;
        }
    }

    public void spawnRouletteWheel(Location location) {
        String id = "Ruleta-" + nextRouletteId++;
        RouletteWheel wheel = new RouletteWheel(plugin, location, id);
        wheel.spawn();
        rouletteWheels.put(id, wheel);
        saveStructures(); // Auto-save when spawned
    }

    public void spawnDiceStructure(Location location) {
        DiceStructure dice = new DiceStructure(plugin, location);
        dice.spawn();
        diceStructures.put(location, dice);
        saveStructures(); // Auto-save when spawned
    }

    public void spawnCoinflipStructure(Location location) {
        CoinflipStructure coinflip = new CoinflipStructure(plugin, location);
        coinflip.spawn();
        coinflipStructures.put(location, coinflip);
        saveStructures(); // Auto-save when spawned
    }

    public void removeRouletteWheel(String id) {
        if (rouletteWheels.containsKey(id)) {
            rouletteWheels.get(id).remove();
            rouletteWheels.remove(id);
            saveStructures(); // Auto-save when removed
        }
    }

    public void removeNearestRouletteWheel(Location location) {
        RouletteWheel nearest = getNearestRouletteWheel(location);
        if (nearest != null) {
            String id = nearest.getId();
            removeRouletteWheel(id);
        }
    }

    public void removeDiceStructure(Location location) {
        if (diceStructures.containsKey(location)) {
            diceStructures.get(location).remove();
            diceStructures.remove(location);
            saveStructures(); // Auto-save when removed
        }
    }

    public void removeNearestDiceStructure(Location playerLocation) {
        DiceStructure nearest = getDiceStructure(playerLocation);
        if (nearest != null) {
            // Find the location key for this dice structure
            Location toRemove = null;
            for (java.util.Map.Entry<Location, DiceStructure> entry : diceStructures.entrySet()) {
                if (entry.getValue() == nearest) {
                    toRemove = entry.getKey();
                    break;
                }
            }
            if (toRemove != null) {
                diceStructures.get(toRemove).remove();
                diceStructures.remove(toRemove);
                saveStructures();
            }
        }
    }

    public void removeCoinflipStructure(Location location) {
        if (coinflipStructures.containsKey(location)) {
            coinflipStructures.get(location).remove();
            coinflipStructures.remove(location);
            saveStructures();
        }
    }

    public void removeCoinflipAt(Location location) {
        CoinflipStructure structure = coinflipStructures.remove(location);
        if (structure != null) {
            structure.remove();
            saveStructures();
        }
    }

    // === Mines Structure Methods ===
    public void spawnMinesStructure(Location location) {
        com.gamblingplugin.structures.MinesStructure mines = new com.gamblingplugin.structures.MinesStructure(plugin,
                location);
        mines.spawn();
        minesStructures.put(location, mines);
        saveStructures();
    }

    public com.gamblingplugin.structures.MinesStructure getMinesStructure(Location playerLocation) {
        for (Map.Entry<Location, com.gamblingplugin.structures.MinesStructure> entry : minesStructures.entrySet()) {
            if (entry.getKey().distance(playerLocation) <= 4) {
                return entry.getValue();
            }
        }
        return null;
    }

    public Map<Location, com.gamblingplugin.structures.MinesStructure> getAllMines() {
        return minesStructures;
    }

    // ========== Case Opening Methods ==========

    public void spawnCaseStructure(Location location) {
        com.gamblingplugin.structures.CaseStructure caseStructure = new com.gamblingplugin.structures.CaseStructure(
                plugin, location);
        caseStructure.spawn();
        caseStructures.put(location, caseStructure);
        saveStructures();
    }

    public com.gamblingplugin.structures.CaseStructure getCaseStructure(Location playerLocation) {
        for (Map.Entry<Location, com.gamblingplugin.structures.CaseStructure> entry : caseStructures.entrySet()) {
            if (entry.getKey().distance(playerLocation) <= 4) {
                return entry.getValue();
            }
        }
        return null;
    }

    public Map<Location, com.gamblingplugin.structures.CaseStructure> getAllCases() {
        return caseStructures;
    }

    public void removeCaseAt(Location location) {
        com.gamblingplugin.structures.CaseStructure structure = caseStructures.remove(location);
        if (structure != null) {
            structure.remove();
            saveStructures();
        }
    }

    // ========== Plinko Methods ==========

    public void spawnPlinkoStructure(Location location) {
        com.gamblingplugin.structures.PlinkoStructure plinkoStructure = new com.gamblingplugin.structures.PlinkoStructure(
                plugin, location);
        plinkoStructure.spawn();
        plinkoStructures.put(location, plinkoStructure);
        saveStructures();
    }

    public com.gamblingplugin.structures.PlinkoStructure getPlinkoStructure(Location playerLocation) {
        for (Map.Entry<Location, com.gamblingplugin.structures.PlinkoStructure> entry : plinkoStructures.entrySet()) {
            if (entry.getValue().isNearby(playerLocation)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public Map<Location, com.gamblingplugin.structures.PlinkoStructure> getAllPlinko() {
        return plinkoStructures;
    }

    // Blackjack structure methods
    public void spawnBlackjackStructure(Location location) {
        com.gamblingplugin.structures.BlackjackStructure structure = new com.gamblingplugin.structures.BlackjackStructure(
                plugin, location);
        structure.spawn();
        blackjackStructures.put(location, structure);
        saveStructures();
    }

    public com.gamblingplugin.structures.BlackjackStructure getBlackjackStructure(Location location) {
        for (Map.Entry<Location, com.gamblingplugin.structures.BlackjackStructure> entry : blackjackStructures
                .entrySet()) {
            if (entry.getValue().isNearby(location)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void removeBlackjackStructure(Location location) {
        com.gamblingplugin.structures.BlackjackStructure structure = getBlackjackStructure(location);
        if (structure != null) {
            structure.remove();
            blackjackStructures.remove(structure.getCenter());
            saveStructures();
        }
    }

    public Map<Location, com.gamblingplugin.structures.BlackjackStructure> getAllBlackjack() {
        return blackjackStructures;
    }

    public void removePlinkoAt(Location location) {
        com.gamblingplugin.structures.PlinkoStructure structure = plinkoStructures.remove(location);
        if (structure != null) {
            structure.remove();
            saveStructures();
        }
    }

    public void removeMinesAt(Location location) {
        com.gamblingplugin.structures.MinesStructure structure = minesStructures.remove(location);
        if (structure != null) {
            structure.remove();
            saveStructures();
        }
    }

    public void removeNearestCoinflipStructure(Location playerLocation) {
        CoinflipStructure nearest = getNearestCoinflipStructure(playerLocation);
        if (nearest != null) {
            Location toRemove = null;
            for (java.util.Map.Entry<Location, CoinflipStructure> entry : coinflipStructures.entrySet()) {
                if (entry.getValue() == nearest) {
                    toRemove = entry.getKey();
                    break;
                }
            }
            if (toRemove != null) {
                coinflipStructures.get(toRemove).remove();
                coinflipStructures.remove(toRemove);
                saveStructures();
            }
        }
    }

    public void removeAll() {
        for (RouletteWheel wheel : rouletteWheels.values()) {
            wheel.remove();
        }
        for (DiceStructure dice : diceStructures.values()) {
            dice.remove();
        }
        rouletteWheels.clear();
        diceStructures.clear();
        saveStructures();
    }

    public RouletteWheel getRouletteWheelById(String id) {
        return rouletteWheels.get(id);
    }

    public RouletteWheel getRouletteWheel(Location location) {
        // This method now searches for a wheel by its center location
        for (RouletteWheel wheel : rouletteWheels.values()) {
            if (wheel.getCenter().equals(location)) {
                return wheel;
            }
        }
        return null;
    }

    public RouletteWheel getNearestRouletteWheel(Location playerLocation) {
        RouletteWheel nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (RouletteWheel wheel : rouletteWheels.values()) {
            Location wheelLoc = wheel.getCenter();
            if (wheelLoc.getWorld() != playerLocation.getWorld()) {
                continue;
            }

            double distance = wheelLoc.distance(playerLocation);
            if (distance < nearestDistance && distance < 20) { // Within 20 blocks
                nearestDistance = distance;
                nearest = wheel;
            }
        }

        return nearest;
    }

    public Map<String, RouletteWheel> getAllRoulettes() {
        return new HashMap<>(rouletteWheels);
    }

    public Map<Location, DiceStructure> getAllDice() {
        return new HashMap<>(diceStructures);
    }

    public Map<Location, CoinflipStructure> getAllCoinflips() {
        return new HashMap<>(coinflipStructures);
    }

    public DiceStructure getDiceStructure(Location location) {
        return getNearestDiceStructure(location);
    }

    public CoinflipStructure getCoinflipStructure(Location location) {
        return getNearestCoinflipStructure(location);
    }

    public DiceStructure getNearestDiceStructure(Location playerLocation) {
        DiceStructure nearest = null;
        double minDistanceSq = 25.0;

        for (Map.Entry<Location, DiceStructure> entry : diceStructures.entrySet()) {
            if (entry.getKey().getWorld() != playerLocation.getWorld())
                continue;

            double distSq = entry.getKey().distanceSquared(playerLocation);
            if (distSq < minDistanceSq) {
                minDistanceSq = distSq;
                nearest = entry.getValue();
            }
        }

        return nearest;
    }

    public CoinflipStructure getNearestCoinflipStructure(Location playerLocation) {
        CoinflipStructure nearest = null;
        double minDistanceSq = 25.0;

        for (Map.Entry<Location, CoinflipStructure> entry : coinflipStructures.entrySet()) {
            if (entry.getKey().getWorld() != playerLocation.getWorld())
                continue;

            double distSq = entry.getKey().distanceSquared(playerLocation);
            if (distSq < minDistanceSq) {
                minDistanceSq = distSq;
                nearest = entry.getValue();
            }
        }

        return nearest;
    }

    public void spawnCrashStructure(Location loc) {
        com.gamblingplugin.structures.CrashStructure crash = new com.gamblingplugin.structures.CrashStructure(plugin, loc);
        crash.spawn();
        crashStructures.put(loc, crash);
    }

    public void spawnBaccaratStructure(Location loc) {
        com.gamblingplugin.structures.BaccaratStructure baccarat = new com.gamblingplugin.structures.BaccaratStructure(plugin, loc);
        baccarat.spawn();
        baccaratStructures.put(loc, baccarat);
    }

    public void spawnLotteryStructure(Location loc) {
        com.gamblingplugin.structures.LotteryStructure lottery = new com.gamblingplugin.structures.LotteryStructure(plugin, loc);
        lottery.spawn();
        lotteryStructures.put(loc, lottery);
    }

    public void spawnVIPLoungeStructure(Location loc) {
        com.gamblingplugin.structures.VIPLoungeStructure lounge = new com.gamblingplugin.structures.VIPLoungeStructure(plugin, loc);
        lounge.spawn();
        vipLoungeStructures.put(loc, lounge);
    }

    public com.gamblingplugin.structures.CrashStructure getCrashStructure(Location loc) {
        for (com.gamblingplugin.structures.CrashStructure c : crashStructures.values()) {
            if (c.isNearby(loc)) return c;
        }
        return null;
    }

    public com.gamblingplugin.structures.BaccaratStructure getBaccaratStructure(Location loc) {
        for (com.gamblingplugin.structures.BaccaratStructure b : baccaratStructures.values()) {
            if (b.isNearby(loc)) return b;
        }
        return null;
    }

    public com.gamblingplugin.structures.LotteryStructure getLotteryStructure(Location loc) {
        for (com.gamblingplugin.structures.LotteryStructure l : lotteryStructures.values()) {
            if (l.isNearby(loc)) return l;
        }
        return null;
    }

    public com.gamblingplugin.structures.VIPLoungeStructure getVIPLoungeStructure(Location loc) {
        for (com.gamblingplugin.structures.VIPLoungeStructure v : vipLoungeStructures.values()) {
            if (v.isNearby(loc)) return v;
        }
        return null;
    }
}
