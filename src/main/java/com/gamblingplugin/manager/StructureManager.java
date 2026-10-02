package com.gamblingplugin.manager;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.structures.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class StructureManager {

    private final GamblingPlugin plugin;
    private final Map<String, RouletteWheel> rouletteWheels = new HashMap<>(); // ID -> Wheel
    private final Map<Location, DiceStructure> diceStructures = new HashMap<>();
    private final Map<Location, CoinflipStructure> coinflipStructures = new HashMap<>();
    private final Map<Location, MinesStructure> minesStructures = new HashMap<>();
    private final Map<Location, CaseStructure> caseStructures = new HashMap<>();
    private final Map<Location, PlinkoStructure> plinkoStructures = new HashMap<>();
    private final Map<Location, BlackjackStructure> blackjackStructures = new HashMap<>();
    private final Map<Location, CrashStructure> crashStructures = new HashMap<>();
    private final Map<Location, BaccaratStructure> baccaratStructures = new HashMap<>();
    private final Map<Location, LotteryStructure> lotteryStructures = new HashMap<>();
    private final Map<Location, VIPLoungeStructure> vipLoungeStructures = new HashMap<>();
    private final Map<Location, WheelStructure> wheelStructures = new HashMap<>();
    private final Map<Location, Slots3DStructure> slotsStructures = new HashMap<>();

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
                structuresFile.getParentFile().mkdirs();
                structuresFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create structures.yml!");
            }
        }
        structuresConfig = YamlConfiguration.loadConfiguration(structuresFile);
    }

    public synchronized void saveStructures() {
        // Roulettes
        structuresConfig.set("roulettes", null);
        for (Map.Entry<String, RouletteWheel> entry : rouletteWheels.entrySet()) {
            structuresConfig.set("roulettes." + entry.getKey(), locationToString(entry.getValue().getCenter()));
        }
        structuresConfig.set("next-roulette-id", nextRouletteId);

        // Simple List locations
        saveLocationList("dice", diceStructures.keySet());
        saveLocationList("coinflips", coinflipStructures.keySet());
        saveLocationList("mines", minesStructures.keySet());
        saveLocationList("cases", caseStructures.keySet());
        saveLocationList("plinko", plinkoStructures.keySet());
        saveLocationList("blackjack", blackjackStructures.keySet());
        saveLocationList("crash", crashStructures.keySet());
        saveLocationList("baccarat", baccaratStructures.keySet());
        saveLocationList("lottery", lotteryStructures.keySet());
        saveLocationList("vipLounge", vipLoungeStructures.keySet());
        saveLocationList("wheels", wheelStructures.keySet());
        saveLocationList("slots", slotsStructures.keySet());

        try {
            structuresConfig.save(structuresFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save structures.yml!");
        }
    }

    private void saveLocationList(String key, Set<Location> locations) {
        List<String> list = new ArrayList<>();
        for (Location loc : locations) {
            if (loc != null && loc.getWorld() != null) {
                list.add(locationToString(loc));
            }
        }
        structuresConfig.set(key, list);
    }

    public synchronized void loadStructures() {
        nextRouletteId = structuresConfig.getInt("next-roulette-id", 1);

        // Load Roulettes
        if (structuresConfig.getConfigurationSection("roulettes") != null) {
            for (String id : structuresConfig.getConfigurationSection("roulettes").getKeys(false)) {
                Location loc = stringToLocation(structuresConfig.getString("roulettes." + id));
                if (loc != null) {
                    RouletteWheel wheel = new RouletteWheel(plugin, loc, id);
                    wheel.spawn();
                    rouletteWheels.put(id, wheel);
                }
            }
        }

        // Load Dice
        for (String s : structuresConfig.getStringList("dice")) {
            Location loc = stringToLocation(s);
            if (loc != null) {
                DiceStructure d = new DiceStructure(plugin, loc);
                d.spawn();
                diceStructures.put(loc, d);
            }
        }

        // Load Coinflip
        for (String s : structuresConfig.getStringList("coinflips")) {
            Location loc = stringToLocation(s);
            if (loc != null) {
                CoinflipStructure c = new CoinflipStructure(plugin, loc);
                c.spawn();
                coinflipStructures.put(loc, c);
            }
        }

        // Load Mines
        for (String s : structuresConfig.getStringList("mines")) {
            Location loc = stringToLocation(s);
            if (loc != null) {
                MinesStructure m = new MinesStructure(plugin, loc);
                m.spawn();
                minesStructures.put(loc, m);
            }
        }

        // Load Cases
        for (String s : structuresConfig.getStringList("cases")) {
            Location loc = stringToLocation(s);
            if (loc != null) {
                CaseStructure cs = new CaseStructure(plugin, loc);
                cs.spawn();
                caseStructures.put(loc, cs);
            }
        }

        // Load Plinko
        for (String s : structuresConfig.getStringList("plinko")) {
            Location loc = stringToLocation(s);
            if (loc != null) {
                PlinkoStructure p = new PlinkoStructure(plugin, loc);
                p.spawn();
                plinkoStructures.put(loc, p);
            }
        }

        // Load Blackjack
        for (String s : structuresConfig.getStringList("blackjack")) {
            Location loc = stringToLocation(s);
            if (loc != null) {
                BlackjackStructure b = new BlackjackStructure(plugin, loc);
                b.spawn();
                blackjackStructures.put(loc, b);
            }
        }

        // Load Crash
        for (String s : structuresConfig.getStringList("crash")) {
            Location loc = stringToLocation(s);
            if (loc != null) {
                CrashStructure cr = new CrashStructure(plugin, loc);
                cr.spawn();
                crashStructures.put(loc, cr);
            }
        }

        // Load Baccarat
        for (String s : structuresConfig.getStringList("baccarat")) {
            Location loc = stringToLocation(s);
            if (loc != null) {
                BaccaratStructure bac = new BaccaratStructure(plugin, loc);
                bac.spawn();
                baccaratStructures.put(loc, bac);
            }
        }

        // Load Lottery
        for (String s : structuresConfig.getStringList("lottery")) {
            Location loc = stringToLocation(s);
            if (loc != null) {
                LotteryStructure lot = new LotteryStructure(plugin, loc);
                lot.spawn();
                lotteryStructures.put(loc, lot);
            }
        }

        // Load VIP Lounge
        for (String s : structuresConfig.getStringList("vipLounge")) {
            Location loc = stringToLocation(s);
            if (loc != null) {
                VIPLoungeStructure vip = new VIPLoungeStructure(plugin, loc);
                vip.spawn();
                vipLoungeStructures.put(loc, vip);
            }
        }

        // Load Mega Wheels
        for (String s : structuresConfig.getStringList("wheels")) {
            Location loc = stringToLocation(s);
            if (loc != null) {
                WheelStructure w = new WheelStructure(plugin, loc);
                w.spawn();
                wheelStructures.put(loc, w);
            }
        }

        // Load Slots 3D
        for (String s : structuresConfig.getStringList("slots")) {
            Location loc = stringToLocation(s);
            if (loc != null) {
                Slots3DStructure slot = new Slots3DStructure(plugin, loc);
                slot.spawn();
                slotsStructures.put(loc, slot);
            }
        }

        plugin.getLogger().info("Casino Structures loaded & restored successfully.");
    }

    private String locationToString(Location loc) {
        if (loc == null || loc.getWorld() == null) return "";
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
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

    // ================= Spawn Methods =================
    public void spawnRouletteWheel(Location location) {
        String id = "Ruleta-" + nextRouletteId++;
        RouletteWheel wheel = new RouletteWheel(plugin, location, id);
        wheel.spawn();
        rouletteWheels.put(id, wheel);
        saveStructures();
    }

    public void spawnDiceStructure(Location location) {
        DiceStructure dice = new DiceStructure(plugin, location);
        dice.spawn();
        diceStructures.put(location, dice);
        saveStructures();
    }

    public void spawnCoinflipStructure(Location location) {
        CoinflipStructure coinflip = new CoinflipStructure(plugin, location);
        coinflip.spawn();
        coinflipStructures.put(location, coinflip);
        saveStructures();
    }

    public void spawnMinesStructure(Location location) {
        MinesStructure mines = new MinesStructure(plugin, location);
        mines.spawn();
        minesStructures.put(location, mines);
        saveStructures();
    }

    public void spawnCaseStructure(Location location) {
        CaseStructure caseStructure = new CaseStructure(plugin, location);
        caseStructure.spawn();
        caseStructures.put(location, caseStructure);
        saveStructures();
    }

    public void spawnPlinkoStructure(Location location) {
        PlinkoStructure plinko = new PlinkoStructure(plugin, location);
        plinko.spawn();
        plinkoStructures.put(location, plinko);
        saveStructures();
    }

    public void spawnBlackjackStructure(Location location) {
        BlackjackStructure structure = new BlackjackStructure(plugin, location);
        structure.spawn();
        blackjackStructures.put(location, structure);
        saveStructures();
    }

    public void spawnCrashStructure(Location loc) {
        CrashStructure crash = new CrashStructure(plugin, loc);
        crash.spawn();
        crashStructures.put(loc, crash);
        saveStructures();
    }

    public void spawnBaccaratStructure(Location loc) {
        BaccaratStructure baccarat = new BaccaratStructure(plugin, loc);
        baccarat.spawn();
        baccaratStructures.put(loc, baccarat);
        saveStructures();
    }

    public void spawnLotteryStructure(Location loc) {
        LotteryStructure lottery = new LotteryStructure(plugin, loc);
        lottery.spawn();
        lotteryStructures.put(loc, lottery);
        saveStructures();
    }

    public void spawnVIPLoungeStructure(Location loc) {
        VIPLoungeStructure lounge = new VIPLoungeStructure(plugin, loc);
        lounge.spawn();
        vipLoungeStructures.put(loc, lounge);
        saveStructures();
    }

    public void spawnWheelStructure(Location loc) {
        WheelStructure wheel = new WheelStructure(plugin, loc);
        wheel.spawn();
        wheelStructures.put(loc, wheel);
        saveStructures();
    }

    public void spawnSlotsStructure(Location loc) {
        Slots3DStructure slots = new Slots3DStructure(plugin, loc);
        slots.spawn();
        slotsStructures.put(loc, slots);
        saveStructures();
    }

    // ================= Query / Get Methods =================
    public RouletteWheel getRouletteWheel(Location location) {
        for (RouletteWheel wheel : rouletteWheels.values()) {
            if (wheel.getCenter().getWorld() != null && location.getWorld() != null &&
                    wheel.getCenter().getWorld().equals(location.getWorld()) &&
                    wheel.getCenter().distance(location) <= 3.5) {
                return wheel;
            }
        }
        return null;
    }

    public RouletteWheel getNearestRouletteWheel(Location playerLocation) {
        RouletteWheel nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (RouletteWheel wheel : rouletteWheels.values()) {
            if (wheel.getCenter().getWorld() != null && playerLocation.getWorld() != null &&
                    wheel.getCenter().getWorld().equals(playerLocation.getWorld())) {
                double distance = wheel.getCenter().distance(playerLocation);
                if (distance < nearestDistance && distance < 25) {
                    nearestDistance = distance;
                    nearest = wheel;
                }
            }
        }
        return nearest;
    }

    public DiceStructure getDiceStructure(Location location) {
        for (DiceStructure dice : diceStructures.values()) {
            if (dice.isNearby(location)) return dice;
        }
        return null;
    }

    public CoinflipStructure getCoinflipStructure(Location location) {
        for (CoinflipStructure coinflip : coinflipStructures.values()) {
            if (coinflip.isNearby(location)) return coinflip;
        }
        return null;
    }

    public MinesStructure getMinesStructure(Location location) {
        for (MinesStructure mines : minesStructures.values()) {
            if (mines.isNearby(location)) return mines;
        }
        return null;
    }

    public CaseStructure getCaseStructure(Location location) {
        for (CaseStructure cs : caseStructures.values()) {
            if (cs.isNearby(location)) return cs;
        }
        return null;
    }

    public PlinkoStructure getPlinkoStructure(Location location) {
        for (PlinkoStructure plinko : plinkoStructures.values()) {
            if (plinko.isNearby(location)) return plinko;
        }
        return null;
    }

    public BlackjackStructure getBlackjackStructure(Location location) {
        for (BlackjackStructure bj : blackjackStructures.values()) {
            if (bj.isNearby(location)) return bj;
        }
        return null;
    }

    public CrashStructure getCrashStructure(Location loc) {
        for (CrashStructure c : crashStructures.values()) {
            if (c.isNearby(loc)) return c;
        }
        return null;
    }

    public CrashStructure getNearestCrashStructure(Location loc) {
        CrashStructure nearest = null;
        double min = Double.MAX_VALUE;
        for (CrashStructure c : crashStructures.values()) {
            if (c.getCenter().getWorld() != null && loc.getWorld() != null &&
                    c.getCenter().getWorld().equals(loc.getWorld())) {
                double d = c.getCenter().distance(loc);
                if (d < min) {
                    min = d;
                    nearest = c;
                }
            }
        }
        return nearest;
    }

    public BaccaratStructure getBaccaratStructure(Location loc) {
        for (BaccaratStructure b : baccaratStructures.values()) {
            if (b.isNearby(loc)) return b;
        }
        return null;
    }

    public LotteryStructure getLotteryStructure(Location loc) {
        for (LotteryStructure l : lotteryStructures.values()) {
            if (l.isNearby(loc)) return l;
        }
        return null;
    }

    public VIPLoungeStructure getVIPLoungeStructure(Location loc) {
        for (VIPLoungeStructure v : vipLoungeStructures.values()) {
            if (v.isNearby(loc)) return v;
        }
        return null;
    }

    public WheelStructure getWheelStructure(Location loc) {
        for (WheelStructure w : wheelStructures.values()) {
            if (w.isNearby(loc)) return w;
        }
        return null;
    }

    public Slots3DStructure getSlotsStructure(Location loc) {
        for (Slots3DStructure s : slotsStructures.values()) {
            if (s.isNearby(loc)) return s;
        }
        return null;
    }

    // ================= Cleanup & Removal =================
    public RouletteWheel getRouletteWheelById(String id) {
        return rouletteWheels.get(id);
    }

    public void removeRouletteWheel(String id) {
        RouletteWheel w = rouletteWheels.remove(id);
        if (w != null) {
            w.remove();
            saveStructures();
        }
    }

    public void removeNearestRouletteWheel(Location playerLocation) {
        RouletteWheel nearest = getNearestRouletteWheel(playerLocation);
        if (nearest != null) {
            removeRouletteWheel(nearest.getId());
        }
    }

    public void removeNearestDiceStructure(Location playerLocation) {
        DiceStructure nearest = getDiceStructure(playerLocation);
        if (nearest != null) {
            removeStructureAt(nearest.getCenter());
        }
    }

    public void removeNearestCoinflipStructure(Location playerLocation) {
        CoinflipStructure nearest = getCoinflipStructure(playerLocation);
        if (nearest != null) {
            removeStructureAt(nearest.getCenter());
        }
    }

    public void removeBlackjackStructure(Location playerLocation) {
        BlackjackStructure nearest = getBlackjackStructure(playerLocation);
        if (nearest != null) {
            removeStructureAt(nearest.getCenter());
        }
    }

    public void removeStructureAt(Location loc) {
        if (loc == null) return;

        // Roulette
        String toRemoveWheelId = null;
        for (Map.Entry<String, RouletteWheel> entry : rouletteWheels.entrySet()) {
            if (entry.getValue().getCenter().distance(loc) <= 2.0) {
                entry.getValue().remove();
                toRemoveWheelId = entry.getKey();
                break;
            }
        }
        if (toRemoveWheelId != null) rouletteWheels.remove(toRemoveWheelId);

        // Remove from maps by location proximity
        removeProx(loc, diceStructures);
        removeProx(loc, coinflipStructures);
        removeProx(loc, minesStructures);
        removeProx(loc, caseStructures);
        removeProx(loc, plinkoStructures);
        removeProx(loc, blackjackStructures);
        removeProx(loc, crashStructures);
        removeProx(loc, baccaratStructures);
        removeProx(loc, lotteryStructures);
        removeProx(loc, vipLoungeStructures);
        removeProx(loc, wheelStructures);
        removeProx(loc, slotsStructures);

        saveStructures();
    }

    private <T> void removeProx(Location loc, Map<Location, T> map) {
        Location matched = null;
        for (Location l : map.keySet()) {
            if (l.getWorld() != null && loc.getWorld() != null && l.getWorld().equals(loc.getWorld()) && l.distance(loc) <= 2.5) {
                matched = l;
                break;
            }
        }
        if (matched != null) {
            T obj = map.remove(matched);
            try {
                obj.getClass().getMethod("remove").invoke(obj);
            } catch (Exception ignored) {}
        }
    }

    public void spawnStructureByType(String type, Location loc) {
        String lower = type.toLowerCase();
        if (lower.contains("crash")) spawnCrashStructure(loc);
        else if (lower.contains("wheel") || lower.contains("rueda")) spawnWheelStructure(loc);
        else if (lower.contains("roulette") || lower.contains("ruleta")) spawnRouletteWheel(loc);
        else if (lower.contains("slots") || lower.contains("tragamonedas")) spawnSlotsStructure(loc);
        else if (lower.contains("dice") || lower.contains("dado")) spawnDiceStructure(loc);
        else if (lower.contains("coinflip") || lower.contains("moneda")) spawnCoinflipStructure(loc);
        else if (lower.contains("blackjack")) spawnBlackjackStructure(loc);
        else if (lower.contains("mines") || lower.contains("mina")) spawnMinesStructure(loc);
        else if (lower.contains("case") || lower.contains("caja")) spawnCaseStructure(loc);
        else if (lower.contains("plinko")) spawnPlinkoStructure(loc);
        else if (lower.contains("baccarat")) spawnBaccaratStructure(loc);
        else if (lower.contains("lottery") || lower.contains("loteria")) spawnLotteryStructure(loc);
        else if (lower.contains("lounge") || lower.contains("vip") || lower.contains("bar")) spawnVIPLoungeStructure(loc);
    }

    public void removeAll() {
        for (RouletteWheel wheel : rouletteWheels.values()) wheel.remove();
        for (DiceStructure dice : diceStructures.values()) dice.remove();
        for (CoinflipStructure cf : coinflipStructures.values()) cf.remove();
        for (MinesStructure m : minesStructures.values()) m.remove();
        for (CaseStructure c : caseStructures.values()) c.remove();
        for (PlinkoStructure p : plinkoStructures.values()) p.remove();
        for (BlackjackStructure b : blackjackStructures.values()) b.remove();
        for (CrashStructure cr : crashStructures.values()) cr.remove();
        for (BaccaratStructure ba : baccaratStructures.values()) ba.remove();
        for (LotteryStructure l : lotteryStructures.values()) l.remove();
        for (VIPLoungeStructure v : vipLoungeStructures.values()) v.remove();
        for (WheelStructure w : wheelStructures.values()) w.remove();
        for (Slots3DStructure s : slotsStructures.values()) s.remove();

        rouletteWheels.clear();
        diceStructures.clear();
        coinflipStructures.clear();
        minesStructures.clear();
        caseStructures.clear();
        plinkoStructures.clear();
        blackjackStructures.clear();
        crashStructures.clear();
        baccaratStructures.clear();
        lotteryStructures.clear();
        vipLoungeStructures.clear();
        wheelStructures.clear();
        slotsStructures.clear();

        saveStructures();
    }

    // ================= 🏛️ CASINO RESORT PLAZA GENERATOR 🏛️ =================
    public void generateCasinoPlaza(Player player) {
        Location center = player.getLocation().getBlock().getLocation();
        World world = center.getWorld();
        if (world == null) return;

        removeAll(); // Clear previous structures cleanly

        int radius = 18;
        int y = center.getBlockY();

        // 1. Generate Platform & Floor
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Block floorBlock = world.getBlockAt(center.getBlockX() + x, y - 1, center.getBlockZ() + z);
                Block airBlock1 = world.getBlockAt(center.getBlockX() + x, y, center.getBlockZ() + z);
                Block airBlock2 = world.getBlockAt(center.getBlockX() + x, y + 1, center.getBlockZ() + z);
                Block airBlock3 = world.getBlockAt(center.getBlockX() + x, y + 2, center.getBlockZ() + z);

                // Clear upper air
                airBlock1.setType(Material.AIR);
                airBlock2.setType(Material.AIR);
                airBlock3.setType(Material.AIR);

                int distMax = Math.max(Math.abs(x), Math.abs(z));

                if (distMax == radius) {
                    // Outer border with polished blackstone & sea lanterns
                    if (Math.abs(x) == radius && Math.abs(z) == radius) {
                        floorBlock.setType(Material.SEA_LANTERN);
                    } else if (distMax % 4 == 0) {
                        floorBlock.setType(Material.SEA_LANTERN);
                    } else {
                        floorBlock.setType(Material.POLISHED_BLACKSTONE);
                    }
                } else if (Math.abs(x) <= 2 || Math.abs(z) <= 2) {
                    // Central Red Carpet Walkways
                    floorBlock.setType(Material.RED_CARPET.isBlock() ? Material.RED_WOOL : Material.RED_CONCRETE);
                } else if ((Math.abs(x) + Math.abs(z)) % 2 == 0) {
                    floorBlock.setType(Material.SMOOTH_QUARTZ);
                } else {
                    floorBlock.setType(Material.QUARTZ_BRICKS);
                }
            }
        }

        // 2. Spawn All 14 Interactive Game Structures across the Pavilion
        // Center (0, 0): Crash 3D Rocket Launchpad
        Location crashLoc = center.clone().add(0, 0, 0);
        spawnCrashStructure(crashLoc);

        // North quadrant (Z negative): Mega Wheel of Fortune & Roulette & Slots 3D
        Location wheelLoc = center.clone().add(0, 0, -10);
        spawnWheelStructure(wheelLoc);
        Location rouletteLoc = center.clone().add(6, 0, -10);
        spawnRouletteWheel(rouletteLoc);
        Location slotsLoc = center.clone().add(-6, 0, -10);
        spawnSlotsStructure(slotsLoc);

        // East quadrant (X positive): Blackjack & Texas Hold'em Poker & VIP Lounge
        Location bjLoc = center.clone().add(10, 0, 0);
        spawnBlackjackStructure(bjLoc);
        Location pokerLoc = center.clone().add(10, 0, -6);
        spawnBaccaratStructure(pokerLoc);
        Location vipLoc = center.clone().add(10, 0, 6);
        spawnVIPLoungeStructure(vipLoc);

        // South quadrant (Z positive): 3D Dice Table, Coinflip Arena, Mines Field
        Location diceLoc = center.clone().add(0, 0, 10);
        spawnDiceStructure(diceLoc);
        Location coinflipLoc = center.clone().add(-6, 0, 10);
        spawnCoinflipStructure(coinflipLoc);
        Location minesLoc = center.clone().add(6, 0, 10);
        spawnMinesStructure(minesLoc);

        // West quadrant (X negative): Plinko Machine, Case Opening, Futures Board, Lottery Booth
        Location plinkoLoc = center.clone().add(-10, 0, 0);
        spawnPlinkoStructure(plinkoLoc);
        Location caseLoc = center.clone().add(-10, 0, -6);
        spawnCaseStructure(caseLoc);
        Location lotteryLoc = center.clone().add(-10, 0, 6);
        spawnLotteryStructure(lotteryLoc);

        saveStructures();

        // Teleport player near the entrance walkway looking at center
        Location entrance = center.clone().add(0, 0, 14);
        entrance.setPitch(0f);
        entrance.setYaw(180f);
        player.teleport(entrance);

        player.sendTitle("§6👑 ¡CASINO RESORT 3D CREADO!", "§eTodos los juegos interactivos han sido generados", 10, 70, 20);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        world.spawnParticle(Particle.FIREWORKS_SPARK, entrance.clone().add(0, 2, 0), 50, 1.5, 1.5, 1.5, 0.1);

        player.sendMessage("§6═════════════════════════════════════════════════════");
        player.sendMessage("§e🏛️ ¡PLAZA Y RESORT DEL CASINO 3D GENERADO EXITOSAMENTE!");
        player.sendMessage("§7Se ha construido una plataforma de cuarzo liso con todos los juegos:");
        player.sendMessage("§a• 🚀 Centro: §fCohete Crash 3D Gigante con Despegue");
        player.sendMessage("§a• 🎡 Norte: §fMega Rueda 3D, Ruleta Francesa & Tragamonedas 3D");
        player.sendMessage("§a• 🃏 Este: §fMesa de Blackjack, Baccarat & VIP Lounge");
        player.sendMessage("§a• 🎲 Sur: §fMesa de Dados 3D, Coinflip & Campo de Minas");
        player.sendMessage("§a• 🎯 Oeste: §fMáquina de Plinko, Apertura de Cajas & Lotería");
        player.sendMessage("§e▶ ¡Simplemente camina hacia cualquier mesa y haz clic derecho!");
        player.sendMessage("§6═════════════════════════════════════════════════════");
    }

    public Map<String, RouletteWheel> getAllRoulettes() { return rouletteWheels; }
    public Map<Location, DiceStructure> getAllDice() { return diceStructures; }
    public Map<Location, CoinflipStructure> getAllCoinflips() { return coinflipStructures; }
    public Map<Location, MinesStructure> getAllMines() { return minesStructures; }
    public Map<Location, CaseStructure> getAllCases() { return caseStructures; }
    public Map<Location, PlinkoStructure> getAllPlinko() { return plinkoStructures; }
    public Map<Location, BlackjackStructure> getAllBlackjack() { return blackjackStructures; }
    public Map<Location, CrashStructure> getAllCrash() { return crashStructures; }
    public Map<Location, BaccaratStructure> getAllBaccarat() { return baccaratStructures; }
    public Map<Location, LotteryStructure> getAllLottery() { return lotteryStructures; }
    public Map<Location, VIPLoungeStructure> getAllVIPLounge() { return vipLoungeStructures; }
    public Map<Location, WheelStructure> getAllWheels() { return wheelStructures; }
    public Map<Location, Slots3DStructure> getAllSlots() { return slotsStructures; }
}
