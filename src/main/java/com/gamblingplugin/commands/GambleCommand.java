package com.gamblingplugin.commands;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.games.Game;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class GambleCommand implements CommandExecutor {

    private final GamblingPlugin plugin;

    public GambleCommand(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can gamble!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("gambling.use")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (plugin.getEconomyManager().getEconomy() == null) {
            player.sendMessage("§cEconomy is not enabled on this server. Gambling is disabled.");
            return true;
        }

        // Handle single-argument commands first
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("vip")) {
                plugin.getVIPGUI().open(player);
                return true;
            }
            if (args[0].equalsIgnoreCase("heist")) {
                plugin.getCasinoHeistEvent().startHeist(player);
                return true;
            }
            if (args[0].equalsIgnoreCase("token") || args[0].equalsIgnoreCase("chips")) {
                double price = plugin.getCasinoTokenExchange().getTokenPrice();
                double myTokens = plugin.getCasinoTokenExchange().getTokens(player.getUniqueId());
                player.sendMessage("§6=== 🪙 §eEXCHANGE DE FICHAS $CHIPS §6===");
                player.sendMessage("§7Precio actual: §a" + plugin.getEconomyManager().format(price) + " §7por cada $CHIP");
                player.sendMessage("§7Tus $CHIPS: §e" + myTokens + " $CHIPS");
                player.sendMessage("§7Comprar: §e/gamble token buy <cantidad>");
                player.sendMessage("§7Vender: §e/gamble token sell <cantidad>");
                return true;
            }
            if (args[0].equalsIgnoreCase("plinko3d")) {
                com.gamblingplugin.games.PlinkoPhysicalMachine machine = new com.gamblingplugin.games.PlinkoPhysicalMachine(plugin, player.getLocation());
                machine.dropBall(player, 100.0, 1);
                return true;
            }
            if (args[0].equalsIgnoreCase("top")) {
                sendLeaderboard(player);
                return true;
            }
            if (args[0].equalsIgnoreCase("admin")) {
                if (!player.hasPermission("gambling.admin")) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    return true;
                }
                // Open Admin GUI
                com.gamblingplugin.gui.AdminGUI adminGUI = new com.gamblingplugin.gui.AdminGUI(plugin);
                adminGUI.open(player);
                return true;
            }
            if (args[0].equalsIgnoreCase("list")) {
                // List command - moved here so it works with just 1 arg
                if (!player.hasPermission("gambling.admin")) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    return true;
                }
                player.sendMessage("§6§l[VieGambling] Estructuras:");
                player.sendMessage("");

                // List roulettes
                java.util.Map<String, com.gamblingplugin.structures.RouletteWheel> roulettes = plugin
                        .getStructureManager().getAllRoulettes();
                if (roulettes.isEmpty()) {
                    player.sendMessage("§7  No hay ruletas spawneadas");
                } else {
                    player.sendMessage("§e§lRULETAS: §7(" + roulettes.size() + ")");
                    for (java.util.Map.Entry<String, com.gamblingplugin.structures.RouletteWheel> entry : roulettes
                            .entrySet()) {
                        String id = entry.getKey();
                        org.bukkit.Location loc = entry.getValue().getCenter();
                        int distance = (int) loc.distance(player.getLocation());

                        // Create clickable message using TextComponent
                        TextComponent message = new TextComponent("§a  ● §f" + id + "§7 - " + distance + "m ");

                        // Teleport button
                        TextComponent tpButton = new TextComponent("§b[TP]");
                        tpButton.setClickEvent(new ClickEvent(
                                ClickEvent.Action.RUN_COMMAND,
                                "/gamble tp " + id));
                        tpButton.setHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder("§aTeleportarte a " + id).create()));

                        // Remove button
                        TextComponent removeButton = new TextComponent(" §c[X]");
                        removeButton.setClickEvent(new ClickEvent(
                                ClickEvent.Action.RUN_COMMAND,
                                "/gamble remove roulette " + id));
                        removeButton.setHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder("§cEliminar " + id).create()));

                        message.addExtra(tpButton);
                        message.addExtra(removeButton);
                        player.spigot().sendMessage(message);
                    }
                }
                player.sendMessage("");

                // List dice structures
                java.util.Map<org.bukkit.Location, com.gamblingplugin.structures.DiceStructure> diceMap = plugin
                        .getStructureManager().getAllDice();
                if (diceMap.isEmpty()) {
                    player.sendMessage("§7  No hay dados");
                } else {
                    player.sendMessage("§d§lDADOS: §7(" + diceMap.size() + ")");
                    int diceIndex = 1;
                    for (org.bukkit.Location loc : diceMap.keySet()) {
                        int distance = (int) loc.distance(player.getLocation());
                        String diceId = "Dado-" + diceIndex++;

                        // Create clickable message
                        TextComponent message = new TextComponent("§a  ● §f" + diceId + "§7 - " + distance + "m ");

                        // Teleport button
                        TextComponent tpButton = new TextComponent("§b[TP]");
                        int x = loc.getBlockX();
                        int y = loc.getBlockY();
                        int z = loc.getBlockZ();
                        tpButton.setClickEvent(new ClickEvent(
                                ClickEvent.Action.RUN_COMMAND,
                                "/tp " + x + " " + y + " " + z));
                        tpButton.setHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder("§aTeleportarte a " + diceId).create()));

                        // Remove button
                        TextComponent removeButton = new TextComponent(" §c[X]");
                        removeButton.setClickEvent(new ClickEvent(
                                ClickEvent.Action.RUN_COMMAND,
                                "/gamble remove dice"));
                        removeButton.setHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder("§cEliminar estructura cercana").create()));

                        message.addExtra(tpButton);
                        message.addExtra(removeButton);
                        player.spigot().sendMessage(message);
                    }
                }

                player.sendMessage("");
                int totalStructures = roulettes.size() + diceMap.size();
                player.sendMessage("§7Total: §e" + totalStructures + " §7estructuras");
                player.sendMessage("§7Haz click en §b[TP] §7para teleportarte o §c[X] §7para eliminar");
                return true;
            }

            // Jackpot command
            if (args[0].equalsIgnoreCase("jackpot")) {
                double jackpotAmount = plugin.getJackpotManager().getJackpotAmount();
                java.util.List<String> recentWinners = plugin.getJackpotManager().getRecentWinners();

                player.sendMessage("");
                player.sendMessage("§6§l[VieGambling] JACKPOT:");
                player.sendMessage("");
                player.sendMessage("§e  Pozo actual: §a§l$" + String.format("%.2f", jackpotAmount));
                player.sendMessage("");
                player.sendMessage("§7  §oRequisitos:");
                player.sendMessage("§7  - Apostar al menos §e$100");
                player.sendMessage("§7  - Probabilidad: §e0.1%§7 por apuesta");
                player.sendMessage("§7  - Se puede ganar en cualquier juego");
                player.sendMessage("");

                if (!recentWinners.isEmpty()) {
                    player.sendMessage("§6  Últimos Ganadores:");
                    for (String winner : recentWinners) {
                        player.sendMessage("§7  - " + winner);
                    }
                    player.sendMessage("");
                }

                return true;
            }
        }

        if (args[0].equalsIgnoreCase("token") || args[0].equalsIgnoreCase("chips")) {
            if (args.length >= 3) {
                double amount;
                try {
                    amount = Double.parseDouble(args[2]);
                    if (amount <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    player.sendMessage("§cCantidad inválida.");
                    return true;
                }

                if (args[1].equalsIgnoreCase("buy")) {
                    plugin.getCasinoTokenExchange().buyTokens(player, amount);
                    return true;
                } else if (args[1].equalsIgnoreCase("sell")) {
                    plugin.getCasinoTokenExchange().sellTokens(player, amount);
                    return true;
                }
            }
        }

        // Blackjack command
        if (args[0].equalsIgnoreCase("blackjack")) {
            if (!player.hasPermission("gambling.play")) {
                player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                return true;
            }
            com.gamblingplugin.gui.BlackjackLobbyGUI lobby = new com.gamblingplugin.gui.BlackjackLobbyGUI(plugin);
            lobby.open(player);
            return true;
        }

        // Help command
        if (args[0].equalsIgnoreCase("help")) {
            if (args.length == 1) {
                // General help
                player.sendMessage("");
                player.sendMessage("§6§l[VieGambling] §eAyuda de Juegos");
                player.sendMessage("");
                player.sendMessage("§7Usa §a/gamble help <juego> §7para ver cómo jugar:");
                player.sendMessage("  §e● §6Roulette §7- Ruleta clásica");
                player.sendMessage("  §e● §6Dice §7- Lanza el dado");
                player.sendMessage("  §e● §6Coinflip §7- Cara o cruz");
                player.sendMessage("  §e● §6Mines §7- Evita las minas");
                player.sendMessage("  §e● §6Case §7- Abre cajas");
                player.sendMessage("  §e● §6Plinko §7- Suelta la bola");
                player.sendMessage("  §e● §6Blackjack §7- 21");
                player.sendMessage("  §e● §6Slots §7- Tragamonedas");
                player.sendMessage("");
                return true;
            }

            // Specific game help
            String game = args[1].toLowerCase();
            switch (game) {
                case "roulette":
                case "ruleta":
                    com.gamblingplugin.utils.GameTutorials.sendRouletteTutorial(player);
                    break;
                case "dice":
                case "dados":
                    com.gamblingplugin.utils.GameTutorials.sendDiceTutorial(player);
                    break;
                case "coinflip":
                case "moneda":
                    com.gamblingplugin.utils.GameTutorials.sendCoinflipTutorial(player);
                    break;
                case "mines":
                case "minas":
                    com.gamblingplugin.utils.GameTutorials.sendMinesTutorial(player);
                    break;
                case "case":
                case "caja":
                    com.gamblingplugin.utils.GameTutorials.sendCaseTutorial(player);
                    break;
                case "plinko":
                    com.gamblingplugin.utils.GameTutorials.sendPlinkoTutorial(player);
                    break;
                case "blackjack":
                case "bj":
                    com.gamblingplugin.utils.GameTutorials.sendBlackjackTutorial(player);
                    break;
                case "slots":
                case "tragamonedas":
                    com.gamblingplugin.utils.GameTutorials.sendSlotsTutorial(player);
                    break;
                default:
                    player.sendMessage("§c[VieGambling] Juego desconocido. Usa /gamble help para ver la lista.");
                    break;
            }
            return true;
        }

        if (args.length < 2) {
            sendHelp(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("spawn")) {
            if (!player.hasPermission("gambling.spawn")) {
                player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                return true;
            }
            if (args.length < 2) {
                player.sendMessage("§cUsage: /gamble spawn <roulette/dice/coinflip>");
                return true;
            }
            String type = args[1].toLowerCase();
            // Spawn 1 block above to prevent clipping
            Location spawnLoc = player.getLocation().clone().add(0, 1, 0);

            if (type.equals("roulette")) {
                plugin.getStructureManager().spawnRouletteWheel(spawnLoc);
                player.sendMessage("§aRoulette wheel spawned!");
            } else if (type.equals("dice")) {
                plugin.getStructureManager().spawnDiceStructure(spawnLoc);
                player.sendMessage("§aDice structure spawned!");
            } else if (type.equals("coinflip")) {
                plugin.getStructureManager().spawnCoinflipStructure(spawnLoc);
                player.sendMessage("§aCoinflip structure spawned!");
            } else if (type.equals("mines")) {
                plugin.getStructureManager().spawnMinesStructure(spawnLoc);
                player.sendMessage("§aMines structure spawned!");
            } else if (type.equals("case")) {
                plugin.getStructureManager().spawnCaseStructure(spawnLoc);
                player.sendMessage("§aCase structure spawned!");
            } else if (type.equals("plinko")) {
                plugin.getStructureManager().spawnPlinkoStructure(spawnLoc);
                player.sendMessage("§aPlinko structure spawned!");
            } else if (type.equals("blackjack")) {
                plugin.getStructureManager().spawnBlackjackStructure(spawnLoc);
                player.sendMessage("§aBlackjack table spawned!");
            } else {
                player.sendMessage("§cUnknown structure type.");
            }
            return true;
        }

        // Remove command
        if (args[0].equalsIgnoreCase("remove")) {
            if (!player.hasPermission("gambling.remove")) {
                player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                return true;
            }
            if (args.length < 2) {
                player.sendMessage("§c[VieGambling] §7Usage: /gamble remove <roulette/dice> [id]");
                player.sendMessage("§7  Ejemplos:");
                player.sendMessage("§e  /gamble remove roulette §7- Elimina la ruleta más cercana");
                player.sendMessage("§e  /gamble remove roulette Ruleta-1 §7- Elimina ruleta específica");
                return true;
            }
            String type = args[1].toLowerCase();
            if (type.equals("roulette")) {
                if (args.length >= 3) {
                    // Remove by ID
                    String id = args[2];
                    plugin.getStructureManager().removeRouletteWheel(id);
                    player.sendMessage("§a[VieGambling] Ruleta '" + id + "' eliminada!");
                } else {
                    // Remove nearest
                    plugin.getStructureManager().removeNearestRouletteWheel(player.getLocation());
                    player.sendMessage("§a[VieGambling] Ruleta más cercana eliminada!");
                }
            } else if (type.equals("dice")) {
                plugin.getStructureManager().removeNearestDiceStructure(player.getLocation());
                player.sendMessage("§a[VieGambling] Estructura de dados más cercana eliminada!");
            } else if (type.equals("coinflip")) {
                plugin.getStructureManager().removeNearestCoinflipStructure(player.getLocation());
                player.sendMessage("§a[VieGambling] Coinflip más cercano eliminado!");
            } else if (type.equals("blackjack")) {
                plugin.getStructureManager().removeBlackjackStructure(player.getLocation());
                player.sendMessage("§a[VieGambling] Mesa de Blackjack eliminada!");
            } else {
                player.sendMessage("§c[VieGambling] Tipo desconocido.");
            }
            return true;
        }

        // List command
        if (args[0].equalsIgnoreCase("list")) {
            if (!player.hasPermission("gambling.list")) {
                player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                return true;
            }
            player.sendMessage("§6§l[VieGambling] Estructuras:");
            player.sendMessage("");

            // List roulettes
            java.util.Map<String, com.gamblingplugin.structures.RouletteWheel> roulettes = plugin.getStructureManager()
                    .getAllRoulettes();
            if (roulettes.isEmpty()) {
                player.sendMessage("§7  No hay ruletas spawneadas");
            } else {
                player.sendMessage("§e§lRULETAS: §7(" + roulettes.size() + ")");
                for (java.util.Map.Entry<String, com.gamblingplugin.structures.RouletteWheel> entry : roulettes
                        .entrySet()) {
                    String id = entry.getKey();
                    org.bukkit.Location loc = entry.getValue().getCenter();
                    int distance = (int) loc.distance(player.getLocation());

                    // Create clickable message using TextComponent
                    TextComponent message = new TextComponent("§a  ● §f" + id + "§7 - " + distance + "m ");

                    // Teleport button
                    TextComponent tpButton = new TextComponent("§b[TP]");
                    tpButton.setClickEvent(new ClickEvent(
                            ClickEvent.Action.RUN_COMMAND,
                            "/gamble tp " + id));
                    tpButton.setHoverEvent(new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("§aTeleportarte a " + id).create()));

                    // Remove button
                    TextComponent removeButton = new TextComponent(" §c[X]");
                    removeButton.setClickEvent(new ClickEvent(
                            ClickEvent.Action.RUN_COMMAND,
                            "/gamble remove roulette " + id));
                    removeButton.setHoverEvent(new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("§cEliminar " + id).create()));

                    message.addExtra(tpButton);
                    message.addExtra(removeButton);
                    player.spigot().sendMessage(message);
                }
            }
            player.sendMessage("");

            // List dice structures
            java.util.Map<org.bukkit.Location, com.gamblingplugin.structures.DiceStructure> dice = plugin
                    .getStructureManager().getAllDice();
            if (!dice.isEmpty()) {
                player.sendMessage("§e§lDADOS: §7(" + dice.size() + ")");
                int diceIndex = 0;
                for (java.util.Map.Entry<org.bukkit.Location, com.gamblingplugin.structures.DiceStructure> entry : dice
                        .entrySet()) {
                    org.bukkit.Location loc = entry.getKey();
                    int distance = (int) loc.distance(player.getLocation());

                    TextComponent message = new TextComponent("§a  ● §fDice #" + diceIndex + " §7- " + distance + "m ");

                    TextComponent tpButton = new TextComponent("§b[TP]");
                    tpButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/tp " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ()));
                    tpButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("§aTeleportarte a este dado").create()));

                    TextComponent removeButton = new TextComponent(" §c[X]");
                    removeButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/gamble remove dice"));
                    removeButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("§cEliminar este dado").create()));

                    message.addExtra(tpButton);
                    message.addExtra(removeButton);
                    player.spigot().sendMessage(message);
                    diceIndex++;
                }
                player.sendMessage("");
            }

            // List coinflip structures
            java.util.Map<org.bukkit.Location, com.gamblingplugin.structures.CoinflipStructure> coinflips = plugin
                    .getStructureManager().getAllCoinflips();
            if (coinflips.isEmpty()) {
                player.sendMessage("§e§lCOINFLIPS:");
                player.sendMessage("§7  No hay coinflips spawneados");
                player.sendMessage("");
            } else {
                player.sendMessage("§e§lCOINFLIPS: §7(" + coinflips.size() + ")");
                int coinflipIndex = 0;
                for (java.util.Map.Entry<org.bukkit.Location, com.gamblingplugin.structures.CoinflipStructure> entry : coinflips
                        .entrySet()) {
                    org.bukkit.Location loc = entry.getKey();
                    int distance = (int) loc.distance(player.getLocation());

                    TextComponent message = new TextComponent(
                            "§a  ● §fCoinflip #" + coinflipIndex + " §7- " + distance + "m ");

                    TextComponent tpButton = new TextComponent("§b[TP]");
                    tpButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/tp " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ()));
                    tpButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("§aTeleportarte a este coinflip").create()));

                    TextComponent removeButton = new TextComponent(" §c[X]");
                    removeButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/gamble remove coinflip"));
                    removeButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("§cEliminar este coinflip").create()));

                    message.addExtra(tpButton);
                    message.addExtra(removeButton);
                    player.spigot().sendMessage(message);
                    coinflipIndex++;
                }
                player.sendMessage("");
            }

            // List mines structures
            java.util.Map<org.bukkit.Location, com.gamblingplugin.structures.MinesStructure> mines = plugin
                    .getStructureManager().getAllMines();
            if (mines.isEmpty()) {
                player.sendMessage("§e§lMINES:");
                player.sendMessage("§7  No hay mines spawneados");
                player.sendMessage("");
            } else {
                player.sendMessage("§e§lMINES: §7(" + mines.size() + ")");
                int minesIndex = 0;
                for (java.util.Map.Entry<org.bukkit.Location, com.gamblingplugin.structures.MinesStructure> entry : mines
                        .entrySet()) {
                    org.bukkit.Location loc = entry.getKey();
                    int distance = (int) loc.distance(player.getLocation());

                    TextComponent message = new TextComponent(
                            "§a  ● §fMines #" + minesIndex + " §7- " + distance + "m ");

                    TextComponent tpButton = new TextComponent("§b[TP]");
                    tpButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/tp " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ()));
                    tpButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("§aTeleportarte a este mines").create()));

                    TextComponent removeButton = new TextComponent(" §c[X]");
                    removeButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/gamble remove mines"));
                    removeButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("§cEliminar este mines").create()));

                    message.addExtra(tpButton);
                    message.addExtra(removeButton);
                    player.spigot().sendMessage(message);
                    minesIndex++;
                }
                player.sendMessage("");
            }

            // List case structures
            java.util.Map<org.bukkit.Location, com.gamblingplugin.structures.CaseStructure> cases = plugin
                    .getStructureManager()
                    .getAllCases();
            if (cases.isEmpty()) {
                player.sendMessage("§e§lCASE OPENING:");
                player.sendMessage("§7  No hay cases spawneados");
                player.sendMessage("");
            } else {
                player.sendMessage("§e§lCASE OPENING: §7(" + cases.size() + ")");
                int caseIndex = 0;
                for (java.util.Map.Entry<org.bukkit.Location, com.gamblingplugin.structures.CaseStructure> entry : cases
                        .entrySet()) {
                    org.bukkit.Location loc = entry.getKey();
                    int distance = (int) loc.distance(player.getLocation());

                    TextComponent message = new TextComponent("§a  ● §fCase #" + caseIndex + " §7- " + distance + "m ");

                    TextComponent tpButton = new TextComponent("§b[TP]");
                    tpButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/tp " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ()));
                    tpButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("§aTeleportarte a este case").create()));

                    TextComponent removeButton = new TextComponent(" §c[X]");
                    removeButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/gamble remove case"));
                    removeButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("§cEliminar este case").create()));

                    message.addExtra(tpButton);
                    message.addExtra(removeButton);
                    player.spigot().sendMessage(message);
                    caseIndex++;
                }
                player.sendMessage("");
            }

            // List plinko structures
            java.util.Map<org.bukkit.Location, com.gamblingplugin.structures.PlinkoStructure> plinkos = plugin
                    .getStructureManager().getAllPlinko();
            if (plinkos.isEmpty()) {
                player.sendMessage("§e§lPLINKO:");
                player.sendMessage("§7  No hay plinkos spawneados");
                player.sendMessage("");
            } else {
                player.sendMessage("§e§lPLINKO: §7(" + plinkos.size() + ")");
                int plinkoIndex = 0;
                for (java.util.Map.Entry<org.bukkit.Location, com.gamblingplugin.structures.PlinkoStructure> entry : plinkos
                        .entrySet()) {
                    org.bukkit.Location loc = entry.getKey();
                    int distance = (int) loc.distance(player.getLocation());

                    TextComponent message = new TextComponent(
                            "§a  ● §fPlinko #" + plinkoIndex + " §7- " + distance + "m ");

                    TextComponent tpButton = new TextComponent("§b[TP]");
                    tpButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/tp " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ()));
                    tpButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("§aTeleportarte a este plinko").create()));

                    TextComponent removeButton = new TextComponent(" §c[X]");
                    removeButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/gamble remove plinko"));
                    removeButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("§cEliminar este plinko").create()));

                    message.addExtra(tpButton);
                    message.addExtra(removeButton);
                    player.spigot().sendMessage(message);
                    plinkoIndex++;
                }
                player.sendMessage("");
            }

            // List blackjack structures
            java.util.Map<org.bukkit.Location, com.gamblingplugin.structures.BlackjackStructure> blackjacks = plugin
                    .getStructureManager().getAllBlackjack();
            if (blackjacks.isEmpty()) {
                player.sendMessage("§e§lBLACKJACK:");
                player.sendMessage("§7  No hay mesas de blackjack spawneadas");
                player.sendMessage("");
            } else {
                player.sendMessage("§e§lBLACKJACK: §7(" + blackjacks.size() + ")");
                int blackjackIndex = 0;
                for (java.util.Map.Entry<org.bukkit.Location, com.gamblingplugin.structures.BlackjackStructure> entry : blackjacks
                        .entrySet()) {
                    org.bukkit.Location loc = entry.getKey();
                    int distance = (int) loc.distance(player.getLocation());

                    TextComponent message = new TextComponent(
                            "§a  ● §fBlackjack #" + blackjackIndex + " §7- " + distance + "m ");

                    TextComponent tpButton = new TextComponent("§b[TP]");
                    tpButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/tp " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ()));
                    tpButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("§aTeleportarte a esta mesa de Blackjack").create()));

                    TextComponent removeButton = new TextComponent(" §c[X]");
                    removeButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/gamble remove blackjack"));
                    removeButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("§cEliminar esta mesa de Blackjack").create()));

                    message.addExtra(tpButton);
                    message.addExtra(removeButton);
                    player.spigot().sendMessage(message);
                    blackjackIndex++;
                }
                player.sendMessage("");
            }

            player.sendMessage("§7Haz click en §b[TP] §7para teleportarte o §c[X] §7para eliminar");
            return true;
        }

        // Teleport command
        if (args[0].equalsIgnoreCase("tp")) {
            if (!player.hasPermission("gambling.admin")) {
                player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                return true;
            }
            if (args.length < 2) {
                player.sendMessage("§c[VieGambling] Usage: /gamble tp <id>");
                return true;
            }
            String id = args[1];
            com.gamblingplugin.structures.RouletteWheel wheel = plugin.getStructureManager().getRouletteWheelById(id);
            if (wheel == null) {
                player.sendMessage("§c[VieGambling] Ruleta '" + id + "' no encontrada!");
                return true;
            }
            org.bukkit.Location tpLoc = wheel.getCenter().clone().add(0, 0.5, 0);
            player.teleport(tpLoc);
            player.sendMessage("§a[VieGambling] Teleportado a " + id + "!");
            return true;
        }

        String gameName = args[0];
        Game game = plugin.getGameManager().getGame(gameName);

        if (game == null || !game.isEnabled()) {
            player.sendMessage("§cGame not found or disabled.");
            return true;
        }

        double bet;
        try {
            bet = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid bet amount.");
            return true;
        }

        if (bet < game.getMinBet()) {
            player.sendMessage("§cMinimum bet is " + game.getMinBet());
            return true;
        }

        if (bet > game.getMaxBet()) {
            player.sendMessage("§cMaximum bet is " + game.getMaxBet());
            return true;
        }

        // Pass remaining args to the game
        String[] gameArgs = Arrays.copyOfRange(args, 2, args.length);
        game.play(player, bet, gameArgs);

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6--- Gambling Help ---");
        player.sendMessage("§e/gamble roulette <amount> <red/black/green/0-36/00>");
        player.sendMessage("§e/gamble dice <amount> <1-6>");
        player.sendMessage("§e/gamble slots <amount>");
        player.sendMessage("§e/gamble top - View top winners");
        if (player.hasPermission("gambling.admin")) {
            player.sendMessage("§c/gamble spawn <roulette/dice> - Spawn game structure");
        }
    }

    private void sendLeaderboard(Player player) {
        player.sendMessage("§6--- Top Winners ---");
        plugin.getStatsManager().getTopWinners(10).forEach((name, amount) -> {
            player.sendMessage("§e" + name + ": §a"
                    + plugin.getConfigManager().getConfig().getString("economy.currency-symbol") + amount);
        });
    }

    // TP command (add before closing brace at top level)
    // Note: This should be added in onCommand method instead
}
