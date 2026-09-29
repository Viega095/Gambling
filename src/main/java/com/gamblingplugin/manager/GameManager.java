package com.gamblingplugin.manager;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.games.*;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class GameManager {

    private final Map<String, Game> games = new HashMap<>();
    private final GamblingPlugin plugin;
    private final Coinflip coinflip;
    private final com.gamblingplugin.games.Mines mines; // Declare Mines field
    private final com.gamblingplugin.games.CaseOpening caseOpening; // Declare CaseOpening field
    private final com.gamblingplugin.games.Plinko plinko; // Declare Plinko field

    public GameManager(GamblingPlugin plugin) {
        this.plugin = plugin;
        this.coinflip = new Coinflip(plugin);
        this.mines = new com.gamblingplugin.games.Mines(plugin); // Initialize Mines
        this.caseOpening = new com.gamblingplugin.games.CaseOpening(plugin); // Initialize CaseOpening
        this.plinko = new com.gamblingplugin.games.Plinko(plugin); // Initialize Plinko

        // Register games (only those that extend Game)
        registerGame(new Roulette(plugin));
        registerGame(new Dice(plugin));
        registerGame(new Slots(plugin));
        // Don't register coinflip - it doesn't extend Game
        registerGame(mines); // Register Mines
        registerGame(caseOpening); // Register CaseOpening
        registerGame(plinko); // Register Plinko
    }

    public void registerGame(Game game) {
        games.put(game.getName().toLowerCase(), game);
    }

    public Game getGame(String name) {
        return games.get(name.toLowerCase());
    }

    public Map<String, Game> getGames() {
        return games;
    }

    public Coinflip getCoinflip() {
        return coinflip;
    }

    public com.gamblingplugin.games.Mines getMines() {
        return mines;
    }

    public com.gamblingplugin.games.CaseOpening getCaseOpening() {
        return caseOpening;
    }

    public com.gamblingplugin.games.Plinko getPlinko() {
        return plinko;
    }
}
