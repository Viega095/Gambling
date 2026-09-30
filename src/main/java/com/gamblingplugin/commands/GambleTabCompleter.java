package com.gamblingplugin.commands;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GambleTabCompleter implements TabCompleter {

    private final GamblingPlugin plugin;

    public GambleTabCompleter(GamblingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return null;
        }

        if (args.length == 1) {
            // Main subcommands
            List<String> subcommands = new ArrayList<>(Arrays.asList(
                    "guide", "help", "baccarat", "lottery", "lounge", "bar", "race", "crash3d", "cashout", "slots3d", "blackjack3d", "plinko3d", "pass", "daily", "heist",
                    "token", "chips", "vip", "jackpot", "top", "blackjack", "roulette", "dice",
                    "slots", "coinflip", "mines", "plinko", "case"
            ));

            // Admin commands
            if (player.hasPermission("gambling.admin")) {
                subcommands.addAll(Arrays.asList("admin", "hologram", "spawn", "remove", "list", "tp"));
            }

            return filterStartingWith(subcommands, args[0]);
        }

        if (args.length == 2) {
            String subcommand = args[0].toLowerCase();

            switch (subcommand) {
                case "lottery":
                    List<String> lSubs = new ArrayList<>(Arrays.asList("buy", "info"));
                    if (player.hasPermission("gambling.admin")) lSubs.add("draw");
                    return filterStartingWith(lSubs, args[1]);

                case "race":
                    List<String> rSubs = new ArrayList<>(Arrays.asList("bet"));
                    if (player.hasPermission("gambling.admin")) rSubs.add("start");
                    return filterStartingWith(rSubs, args[1]);

                case "crash3d":
                case "crash":
                    List<String> cSubs = new ArrayList<>(Arrays.asList("50", "100", "500", "1000"));
                    if (player.hasPermission("gambling.admin")) cSubs.add("start");
                    return filterStartingWith(cSubs, args[1]);

                case "hologram":
                    if (player.hasPermission("gambling.admin")) {
                        return filterStartingWith(Arrays.asList("spawn", "remove"), args[1]);
                    }
                    break;

                case "spawn":
                case "remove":
                    if (player.hasPermission("gambling.admin")) {
                        return filterStartingWith(
                                Arrays.asList("roulette", "dice", "coinflip", "mines", "case", "plinko", "blackjack", "slots3d", "blackjack3d", "plinko3d"),
                                args[1]);
                    }
                    break;

                case "token":
                case "chips":
                    return filterStartingWith(Arrays.asList("buy", "sell"), args[1]);

                case "help":
                    return filterStartingWith(
                            Arrays.asList("roulette", "dice", "coinflip", "mines", "case", "plinko", "blackjack", "slots"),
                            args[1]);

                case "roulette":
                case "dice":
                case "slots":
                case "coinflip":
                case "mines":
                case "plinko":
                    return Arrays.asList("50", "100", "500", "1000", "5000");
            }
        }

        if (args.length == 3) {
            String subcommand = args[0].toLowerCase();

            switch (subcommand) {
                case "token":
                case "chips":
                    return Arrays.asList("1", "5", "10", "50", "100");

                case "roulette":
                    List<String> rouletteOptions = new ArrayList<>(Arrays.asList("red", "black", "green", "0", "00"));
                    for (int i = 1; i <= 36; i++) {
                        rouletteOptions.add(String.valueOf(i));
                    }
                    return filterStartingWith(rouletteOptions, args[2]);

                case "dice":
                    return filterStartingWith(Arrays.asList("1", "2", "3", "4", "5", "6"), args[2]);

                case "remove":
                    if (player.hasPermission("gambling.admin")) {
                        String type = args[1].toLowerCase();
                        if (type.equals("roulette")) {
                            return new ArrayList<>(plugin.getStructureManager().getAllRoulettes().keySet());
                        }
                    }
                    break;
            }
        }

        return new ArrayList<>();
    }

    private List<String> filterStartingWith(List<String> list, String prefix) {
        String lowerPrefix = prefix.toLowerCase();
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(lowerPrefix))
                .collect(Collectors.toList());
    }
}
