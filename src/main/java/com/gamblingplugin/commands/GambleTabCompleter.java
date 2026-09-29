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
        if (!(sender instanceof Player)) {
            return null;
        }

        Player player = (Player) sender;
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Main subcommands
            List<String> subcommands = new ArrayList<>(Arrays.asList(
                    "roulette", "dice", "slots", "coinflip",
                    "jackpot", "top", "help"));

            // Admin commands
            if (player.hasPermission("gambling.admin")) {
                subcommands.addAll(Arrays.asList("spawn", "remove", "list"));
            }

            return filterStartingWith(subcommands, args[0]);
        }

        if (args.length == 2) {
            String subcommand = args[0].toLowerCase();

            switch (subcommand) {
                case "spawn":
                case "remove":
                    if (player.hasPermission("gambling.admin")) {
                        return filterStartingWith(
                                Arrays.asList("roulette", "dice", "coinflip", "mines", "case", "plinko", "blackjack"),
                                args[1]);
                    }
                    break;

                case "roulette":
                    // First arg is bet amount
                    return Arrays.asList("<cantidad>");

                case "dice":
                    // First arg is bet amount
                    return Arrays.asList("<cantidad>");

                case "slots":
                case "coinflip":
                    // Only needs amount
                    return Arrays.asList("<cantidad>");
            }
        }

        if (args.length == 3) {
            String subcommand = args[0].toLowerCase();

            switch (subcommand) {
                case "roulette":
                    // Third arg is bet type
                    List<String> rouletteOptions = new ArrayList<>(Arrays.asList(
                            "red", "black", "green", "0", "00"));
                    // Add numbers 1-36
                    for (int i = 1; i <= 36; i++) {
                        rouletteOptions.add(String.valueOf(i));
                    }
                    return filterStartingWith(rouletteOptions, args[2]);

                case "dice":
                    // Third arg is number 1-6
                    return filterStartingWith(
                            Arrays.asList("1", "2", "3", "4", "5", "6"),
                            args[2]);

                case "remove":
                    if (player.hasPermission("gambling.admin")) {
                        String type = args[1].toLowerCase();
                        if (type.equals("roulette")) {
                            // Show roulette IDs
                            return new ArrayList<>(plugin.getStructureManager().getAllRoulettes().keySet());
                        } else if (type.equals("dice")) {
                            // Show dice indices
                            int diceCount = plugin.getStructureManager().getAllDice().size();
                            List<String> indices = new ArrayList<>();
                            for (int i = 0; i < diceCount; i++) {
                                indices.add(String.valueOf(i));
                            }
                            return indices;
                        } else if (type.equals("coinflip")) {
                            // Show coinflip indices
                            int coinflipCount = plugin.getStructureManager().getAllCoinflips().size();
                            List<String> indices = new ArrayList<>();
                            for (int i = 0; i < coinflipCount; i++) {
                                indices.add(String.valueOf(i));
                            }
                            return indices;
                        }
                    }
                    break;
            }
        }

        return completions;
    }

    private List<String> filterStartingWith(List<String> list, String prefix) {
        String lowerPrefix = prefix.toLowerCase();
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(lowerPrefix))
                .collect(Collectors.toList());
    }
}
