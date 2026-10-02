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
        if (args.length == 0 || (args.length == 1 && (args[0].equalsIgnoreCase("guide") || args[0].equalsIgnoreCase("menu")))) {
            sendInteractiveGuide(player);
            return true;
        }

        if (args.length >= 1 && (args[0].equalsIgnoreCase("update") || args[0].equalsIgnoreCase("autoupdate"))) {
            if (!player.hasPermission("gambling.admin")) {
                player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                return true;
            }
            if (args.length >= 2 && (args[1].equalsIgnoreCase("apply") || args[1].equalsIgnoreCase("download"))) {
                plugin.getUpdateManager().applyAutoUpdate(player);
            } else {
                plugin.getUpdateManager().checkUpdate(player, true);
            }
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (!player.hasPermission("gambling.admin")) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    return true;
                }
                plugin.getUpdateManager().performLiveReload(player);
                return true;
            }
            if (args[0].equalsIgnoreCase("test") || args[0].equalsIgnoreCase("lab") || args[0].equalsIgnoreCase("demo")) {
                plugin.getCasinoTestLabGUI().open(player);
                return true;
            }
            if (args[0].equalsIgnoreCase("resort") || args[0].equalsIgnoreCase("plaza") || args[0].equalsIgnoreCase("spawnall") || args[0].equalsIgnoreCase("layout")) {
                if (!player.hasPermission("gambling.admin")) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    return true;
                }
                plugin.getStructureManager().generateCasinoPlaza(player);
                return true;
            }
            if (args[0].equalsIgnoreCase("vault") || args[0].equalsIgnoreCase("card") || args[0].equalsIgnoreCase("loyalty")) {
                plugin.getCasinoVaultAndLoyalty().openVaultGUI(player);
                return true;
            }
            if (args[0].equalsIgnoreCase("baccarat") || args[0].equalsIgnoreCase("puntobanco")) {
                plugin.getBaccaratTable().openGUI(player);
                return true;
            }
            if (args[0].equalsIgnoreCase("lounge") || args[0].equalsIgnoreCase("bar")) {
                plugin.getCasinoVIPLounge().openLoungeMenu(player);
                return true;
            }
            if (args[0].equalsIgnoreCase("lottery")) {
                com.gamblingplugin.manager.LotteryDrawEngine lot = plugin.getLotteryDrawEngine();
                player.sendMessage("§6╔════════════════════════════════════════════════╗");
                player.sendMessage("§6║      §e🎫 LOTERÍA NACIONAL PROGRESIVA DEL SERVIDOR  §6║");
                player.sendMessage("§6╠════════════════════════════════════════════════╝");
                player.sendMessage("§6║ §aPozo acumulado: §e§l" + (plugin.getEconomyManager().getEconomy() != null ? plugin.getEconomyManager().format(lot.getProgressivePot()) : "$" + lot.getProgressivePot()));
                player.sendMessage("§6║ §7Precio del boleto: §f$" + lot.getTicketPrice());
                player.sendMessage("§6║ §7Tus boletos activos: §e" + lot.getTickets(player.getUniqueId()) + " boletos");
                player.sendMessage("§6║ §7Total de boletos en juego: §f" + lot.getTotalTickets());
                player.sendMessage("§6║ §7Próximo sorteo en: §b" + (lot.getRemainingSeconds() / 60) + "m " + (lot.getRemainingSeconds() % 60) + "s");
                player.sendMessage("§6║ §eComprar boletos: §a/gamble lottery buy <cantidad>");
                if (player.hasPermission("gambling.admin")) {
                    player.sendMessage("§6║ §cForzar sorteo inmediato: §e/gamble lottery draw");
                }
                player.sendMessage("§6╚════════════════════════════════════════════════╝");
                return true;
            }
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
            if (args[0].equalsIgnoreCase("pass")) {
                plugin.getCasinoPassManager().showPassStatus(player);
                return true;
            }
            if (args[0].equalsIgnoreCase("daily") || args[0].equalsIgnoreCase("wheel")) {
                plugin.getDailySpinManager().spinWheel(player);
                return true;
            }
            if (args[0].equalsIgnoreCase("plinko3d")) {
                com.gamblingplugin.games.PlinkoPhysicalMachine machine = new com.gamblingplugin.games.PlinkoPhysicalMachine(plugin, player.getLocation());
                machine.dropBall(player, 100.0, 1);
                return true;
            }
            if (args[0].equalsIgnoreCase("slots3d")) {
                com.gamblingplugin.games.PhysicalSlotsCabinet cabinet = new com.gamblingplugin.games.PhysicalSlotsCabinet(plugin, player.getLocation());
                cabinet.spin(player, 100.0);
                return true;
            }
            if (args[0].equalsIgnoreCase("cashout")) {
                plugin.getPhysicalCrashRocket().cashOut(player);
                return true;
            }
            if (args[0].equalsIgnoreCase("hologram")) {
                if (!player.hasPermission("gambling.admin")) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    return true;
                }
                plugin.getCasinoHologramEngine().spawnHologram(player.getLocation());
                player.sendMessage("§a✦ Holograma de Casino creado.");
                return true;
            }
            if (args[0].equalsIgnoreCase("race")) {
                player.sendMessage("§6=== 🏇 HIPÓDROMO DE CARRERAS DE CABALLOS ===");
                for (com.gamblingplugin.games.CasinoRaceTrack.HorseCompetitor hc : plugin.getCasinoRaceTrack().getCompetitors()) {
                    player.sendMessage("  " + hc.color + "#" + hc.id + " " + hc.name + " §7- Cuota: §e" + hc.odds + "x");
                }
                player.sendMessage("§7Apostar: §e/gamble race bet <1-4> <monto>");
                if (player.hasPermission("gambling.admin")) {
                    player.sendMessage("§cIniciar: §e/gamble race start");
                }
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

        if (args[0].equalsIgnoreCase("lottery")) {
            if (args.length >= 2 && args[1].equalsIgnoreCase("draw")) {
                if (!player.hasPermission("gambling.admin")) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    return true;
                }
                plugin.getLotteryDrawEngine().executeDraw();
                return true;
            }
            if (args.length >= 3 && args[1].equalsIgnoreCase("buy")) {
                try {
                    int count = Integer.parseInt(args[2]);
                    plugin.getLotteryDrawEngine().buyTickets(player, count);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cUso: /gamble lottery buy <cantidad>");
                }
                return true;
            }
        }

        if (args[0].equalsIgnoreCase("race")) {
            if (args.length >= 2 && args[1].equalsIgnoreCase("start")) {
                if (!player.hasPermission("gambling.admin")) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    return true;
                }
                plugin.getCasinoRaceTrack().startRace(player.getLocation());
                return true;
            }
            if (args.length >= 4 && args[1].equalsIgnoreCase("bet")) {
                try {
                    int horse = Integer.parseInt(args[2]);
                    double bet = Double.parseDouble(args[3]);
                    plugin.getCasinoRaceTrack().placeBet(player, horse, bet);
                } catch (Exception e) {
                    player.sendMessage("§cUso: /gamble race bet <1-4> <monto>");
                }
                return true;
            }
        }

        if (args[0].equalsIgnoreCase("update") || args[0].equalsIgnoreCase("autoupdate")) {
            if (!player.hasPermission("gambling.admin")) {
                player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                return true;
            }
            if (args.length >= 2 && (args[1].equalsIgnoreCase("apply") || args[1].equalsIgnoreCase("download"))) {
                plugin.getUpdateManager().applyAutoUpdate(player);
            } else {
                plugin.getUpdateManager().checkUpdate(player, true);
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("crash3d") || args[0].equalsIgnoreCase("crash")) {
            if (args.length >= 2 && args[1].equalsIgnoreCase("start")) {
                if (!player.hasPermission("gambling.admin")) {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    return true;
                }
                plugin.getPhysicalCrashRocket().launchRocket(player.getLocation());
                return true;
            }
            if (args.length >= 2) {
                try {
                    double bet = Double.parseDouble(args[1]);
                    plugin.getPhysicalCrashRocket().joinRound(player, bet);
                } catch (Exception e) {
                    player.sendMessage("§cUso: /gamble crash3d <monto> | /gamble crash3d start");
                }
                return true;
            }
        }

        if (args[0].equalsIgnoreCase("hologram") && args.length >= 2) {
            if (!player.hasPermission("gambling.admin")) {
                player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                return true;
            }
            if (args[1].equalsIgnoreCase("remove")) {
                plugin.getCasinoHologramEngine().removeHologram();
                player.sendMessage("§c✦ Holograma eliminado.");
            } else {
                plugin.getCasinoHologramEngine().spawnHologram(player.getLocation());
                player.sendMessage("§a✦ Holograma de Casino creado en tu posición.");
            }
            return true;
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
            } else if (type.equals("crash") || type.equals("crash3d")) {
                plugin.getStructureManager().spawnCrashStructure(spawnLoc);
                player.sendMessage("§a✦ Estructura y plataforma interactiva del Cohete Crash 3D generada!");
            } else if (type.equals("baccarat")) {
                plugin.getStructureManager().spawnBaccaratStructure(spawnLoc);
                player.sendMessage("§a✦ Mesa interactiva de Baccarat Punto Banco generada!");
            } else if (type.equals("lottery")) {
                plugin.getStructureManager().spawnLotteryStructure(spawnLoc);
                player.sendMessage("§a✦ Quiosco interactivo de Lotería Nacional generado!");
            } else if (type.equals("lounge") || type.equals("bar")) {
                plugin.getStructureManager().spawnVIPLoungeStructure(spawnLoc);
                player.sendMessage("§a✦ Barra interactiva del VIP Bar & Lounge generada!");
            } else {
                player.sendMessage("§cTipo desconocido. Opciones: roulette, dice, coinflip, mines, case, plinko, blackjack, crash, baccarat, lottery, lounge");
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

    private void sendInteractiveGuide(Player player) {
        player.sendMessage("§6╔════════════════════════════════════════════════╗");
        player.sendMessage("§6║       §e🎰 GUÍA MAESTRA DE CASINO Y APUESTAS§6      ║");
        sendClickable(player, "§6🧪 §l[ABRIR PANEL MAESTRO DE PRUEBAS DEL CASINO GUI]", "/gamble test", "§aAbre el laboratorio visual para probar todos los juegos y jackpots");
        sendClickable(player, "§6▶ §eBóveda Segura & Tarjeta VIP §7(/gamble vault)", "/gamble vault", "§aGuardar saldo, ganar interés diario y reclamar cashback");
        sendClickable(player, "§6▶ §eHipódromo y Carreras de Caballos §7(/gamble race)", "/gamble race", "§aApostar en carreras de caballos animadas");
        sendClickable(player, "§6▶ §eCohete Crash 3D Ascendente §7(/gamble crash3d 100)", "/gamble crash3d 100", "§aApostar en el cohete y retirar antes de la explosión");
        sendClickable(player, "§6▶ §eTragamonedas 3D en el Mundo §7(/gamble slots3d)", "/gamble slots3d", "§aProbar máquina tragamonedas física 3D animada");
        sendClickable(player, "§6▶ §eMesa de Blackjack Holográfica §7(/gamble blackjack3d)", "/gamble blackjack3d", "§aJugar Blackjack en mesa física");
        sendClickable(player, "§6▶ §eMáquina Plinko Física §7(/gamble plinko3d)", "/gamble plinko3d", "§aSoltar bola física de Plinko");
        sendClickable(player, "§6▶ §eRuleta de la Suerte Diaria §7(/gamble daily)", "/gamble daily", "§aGirar gratis cada 24 horas");
        sendClickable(player, "§6▶ §ePase de Temporada VIP §7(/gamble pass)", "/gamble pass", "§aVer tu progreso y recompensas del pase");
        sendClickable(player, "§6▶ §eExchange de Fichas $CHIPS §7(/gamble token)", "/gamble token", "§aComprar y vender fichas del casino");
        sendClickable(player, "§6▶ §eLobby de Blackjack §7(/gamble blackjack)", "/gamble blackjack", "§aAbrir menú de mesas de Blackjack");
        sendClickable(player, "§6▶ §eMesa de Baccarat Punto Banco §7(/gamble baccarat)", "/gamble baccarat", "§aJugar Baccarat interactivo Jugador vs Banca");
        sendClickable(player, "§6▶ §eLotería Progresiva del Servidor §7(/gamble lottery)", "/gamble lottery", "§aComprar boletos para el gran sorteo global");
        sendClickable(player, "§6▶ §eBar & VIP Cocktail Lounge §7(/gamble lounge)", "/gamble lounge", "§aOrdenar bebidas con buffs y efectos de suerte");
        sendClickable(player, "§6▶ §eBolsa de Pozo Progresivo §7(/gamble jackpot)", "/gamble jackpot", "§aVer el pozo acumulado actual");

        if (player.hasPermission("gambling.admin")) {
            player.sendMessage("");
            player.sendMessage("§d⚡ [HERRAMIENTAS DE ADMINISTRADOR]");
            sendClickable(player, "§a• Auto-Update / Verificar GitHub", "/gamble update", "§eVerificar y descargar actualizaciones de GitHub");
            sendClickable(player, "§a• Recarga en Caliente (Hot-Reload)", "/gamble reload", "§eRecargar configuración y juegos sin reiniciar");
            sendClickable(player, "§d• Iniciar Carrera de Caballos", "/gamble race start", "§eLanzar carrera de caballos en tu posición");
            sendClickable(player, "§d• Iniciar Vuelo Cohete Crash 3D", "/gamble crash3d start", "§eDespegar cohete crash en tu posición");
            sendClickable(player, "§d• Spawnear Holograma Leaderboard", "/gamble hologram spawn", "§eCrear podio y pozo flotante");
            sendClickable(player, "§d• Panel Administrativo GUI", "/gamble admin", "§eAbrir panel de control de administración");
            sendClickable(player, "§d• Spawnear Ruleta Física", "/gamble spawn roulette", "§eColocar una ruleta en tus pies");
            sendClickable(player, "§d• Spawnear Mesa de Blackjack", "/gamble spawn blackjack", "§eColocar mesa física de blackjack");
            sendClickable(player, "§d• Spawnear Máquina de Minas", "/gamble spawn mines", "§eColocar estructura física de minas");
        }
        player.sendMessage("§6══════════════════════════════════════════════════");
    }

    private void sendClickable(Player player, String text, String command, String hover) {
        TextComponent component = new TextComponent(text);
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
        player.spigot().sendMessage(component);
    }
}
