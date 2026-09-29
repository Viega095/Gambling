package com.gamblingplugin;

import com.gamblingplugin.manager.ConfigManager;
import com.gamblingplugin.manager.EconomyManager;
import com.gamblingplugin.manager.GameManager;
import com.gamblingplugin.manager.StatsManager;
import com.gamblingplugin.manager.StructureManager;
import com.gamblingplugin.manager.CooldownManager;
import com.gamblingplugin.manager.JackpotManager;
import org.bukkit.plugin.java.JavaPlugin;

public class GamblingPlugin extends JavaPlugin {

    private static GamblingPlugin instance;

    private ConfigManager configManager;
    private EconomyManager economyManager;
    private GameManager gameManager;
    private StatsManager statsManager;
    private StructureManager structureManager;
    private CooldownManager cooldownManager;
    private JackpotManager jackpotManager;
    private com.gamblingplugin.manager.NotificationManager notificationManager;
    private com.gamblingplugin.gui.BettingGUI bettingGUI;
    private com.gamblingplugin.gui.RouletteSelectionGUI rouletteSelectionGUI;
    private com.gamblingplugin.gui.CoinflipGUI coinflipGUI;
    private com.gamblingplugin.gui.DiceGUI diceGUI;
    private com.gamblingplugin.games.blackjack.BlackjackManager blackjackManager;
    private com.gamblingplugin.manager.VIPManager vipManager;
    private com.gamblingplugin.manager.MegaJackpotEngine megaJackpotEngine;
    private com.gamblingplugin.gui.VIPGUI vipGUI;
    private com.gamblingplugin.games.CommunityRouletteTable communityRouletteTable;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.economyManager = new EconomyManager(this);
        this.gameManager = new GameManager(this);
        this.statsManager = new StatsManager(this);
        this.structureManager = new StructureManager(this);
        this.cooldownManager = new CooldownManager();
        this.jackpotManager = new JackpotManager(this);
        this.vipManager = new com.gamblingplugin.manager.VIPManager(this);
        this.megaJackpotEngine = new com.gamblingplugin.manager.MegaJackpotEngine(this);
        this.vipGUI = new com.gamblingplugin.gui.VIPGUI(this);
        this.communityRouletteTable = new com.gamblingplugin.games.CommunityRouletteTable(this);
        this.notificationManager = new com.gamblingplugin.manager.NotificationManager(this);
        this.bettingGUI = new com.gamblingplugin.gui.BettingGUI(this);
        this.rouletteSelectionGUI = new com.gamblingplugin.gui.RouletteSelectionGUI(this);
        this.coinflipGUI = new com.gamblingplugin.gui.CoinflipGUI(this);
        this.diceGUI = new com.gamblingplugin.gui.DiceGUI(this);
        this.blackjackManager = new com.gamblingplugin.games.blackjack.BlackjackManager();

        if (economyManager.getEconomy() == null) {
            getLogger().warning("Vault dependency not found! Economy features will be disabled.");
            getLogger().warning("To enable economy features, install Vault and an economy plugin.");
        }

        // Register games
        this.gameManager.registerGame(new com.gamblingplugin.games.Roulette(this));
        this.gameManager.registerGame(new com.gamblingplugin.games.Dice(this));
        this.gameManager.registerGame(new com.gamblingplugin.games.Slots(this));
        this.gameManager.registerGame(new com.gamblingplugin.games.CrashRocket(this));
        this.gameManager.registerGame(new com.gamblingplugin.games.TexasHoldemPoker(this));

        // Register commands
        getCommand("gamble").setExecutor(new com.gamblingplugin.commands.GambleCommand(this));
        getCommand("gamble").setTabCompleter(new com.gamblingplugin.commands.GambleTabCompleter(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new com.gamblingplugin.listeners.StructureListener(this), this);
        getServer().getPluginManager().registerEvents(new com.gamblingplugin.listeners.RouletteClickListener(this),
                this);
        getServer().getPluginManager().registerEvents(new com.gamblingplugin.listeners.CoinflipClickListener(this),
                this);
        getServer().getPluginManager().registerEvents(new com.gamblingplugin.listeners.DiceClickListener(this), this);
        getServer().getPluginManager().registerEvents(bettingGUI, this);
        getServer().getPluginManager().registerEvents(rouletteSelectionGUI, this);
        getServer().getPluginManager().registerEvents(coinflipGUI, this);
        getServer().getPluginManager().registerEvents(diceGUI, this);

        // Register Mines GUI and listener
        com.gamblingplugin.gui.MinesGUI minesGUI = new com.gamblingplugin.gui.MinesGUI(this);
        getServer().getPluginManager().registerEvents(minesGUI, this);
        getServer().getPluginManager().registerEvents(new com.gamblingplugin.listeners.MinesClickListener(this), this);

        // Register Case Opening GUI and listener
        com.gamblingplugin.gui.CaseOpeningGUI caseOpeningGUI = new com.gamblingplugin.gui.CaseOpeningGUI(this);
        getServer().getPluginManager().registerEvents(caseOpeningGUI, this);
        getServer().getPluginManager().registerEvents(new com.gamblingplugin.listeners.CaseClickListener(this), this);

        // Register Plinko listeners
        com.gamblingplugin.gui.PlinkoGUI plinkoGUI = new com.gamblingplugin.gui.PlinkoGUI(this);
        com.gamblingplugin.gui.PlinkoBallSelectionGUI plinkoBallGUI = new com.gamblingplugin.gui.PlinkoBallSelectionGUI(
                this, "NORMAL");
        getServer().getPluginManager().registerEvents(plinkoGUI, this);
        getServer().getPluginManager().registerEvents(plinkoBallGUI, this);
        getServer().getPluginManager().registerEvents(new com.gamblingplugin.listeners.PlinkoClickListener(this), this);

        // Register Blackjack listeners
        com.gamblingplugin.gui.BlackjackLobbyGUI blackjackLobbyGUI = new com.gamblingplugin.gui.BlackjackLobbyGUI(this);
        com.gamblingplugin.gui.BlackjackGameGUI blackjackGameGUI = new com.gamblingplugin.gui.BlackjackGameGUI(this);
        getServer().getPluginManager().registerEvents(blackjackLobbyGUI, this);
        getServer().getPluginManager().registerEvents(blackjackGameGUI, this);
        getServer().getPluginManager().registerEvents(new com.gamblingplugin.listeners.BlackjackClickListener(this),
                this);

        // Register Custom Bet Listener
        getServer().getPluginManager().registerEvents(new com.gamblingplugin.listeners.CustomBetListener(this), this);

        // Register AdminGUI, StatsViewerGUI and VIPGUI
        getServer().getPluginManager().registerEvents(new com.gamblingplugin.gui.AdminGUI(this), this);
        getServer().getPluginManager().registerEvents(new com.gamblingplugin.gui.StatsViewerGUI(this), this);
        getServer().getPluginManager().registerEvents(vipGUI, this);

        getLogger().info("GamblingPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        if (structureManager != null) {
            structureManager.removeAll();
        }
        getLogger().info("GamblingPlugin has been disabled!");
    }

    public static GamblingPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public StructureManager getStructureManager() {
        return structureManager;
    }

    public com.gamblingplugin.gui.BettingGUI getBettingGUI() {
        return bettingGUI;
    }

    public com.gamblingplugin.gui.DiceGUI getDiceGUI() {
        return diceGUI;
    }

    public com.gamblingplugin.games.blackjack.BlackjackManager getBlackjackManager() {
        return blackjackManager;
    }

    public com.gamblingplugin.gui.RouletteSelectionGUI getRouletteSelectionGUI() {
        return rouletteSelectionGUI;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public com.gamblingplugin.manager.NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public JackpotManager getJackpotManager() {
        return jackpotManager;
    }

    public com.gamblingplugin.manager.VIPManager getVIPManager() {
        return vipManager;
    }

    public com.gamblingplugin.manager.MegaJackpotEngine getMegaJackpotEngine() {
        return megaJackpotEngine;
    }

    public com.gamblingplugin.gui.VIPGUI getVIPGUI() {
        return vipGUI;
    }

    public com.gamblingplugin.games.CommunityRouletteTable getCommunityRouletteTable() {
        return communityRouletteTable;
    }

    private com.gamblingplugin.manager.CasinoTokenExchange casinoTokenExchange;
    private com.gamblingplugin.events.CasinoHeistEvent casinoHeistEvent;

    public com.gamblingplugin.manager.CasinoTokenExchange getCasinoTokenExchange() {
        if (casinoTokenExchange == null) casinoTokenExchange = new com.gamblingplugin.manager.CasinoTokenExchange(this);
        return casinoTokenExchange;
    }

    public com.gamblingplugin.events.CasinoHeistEvent getCasinoHeistEvent() {
        if (casinoHeistEvent == null) casinoHeistEvent = new com.gamblingplugin.events.CasinoHeistEvent(this);
        return casinoHeistEvent;
    }
}
