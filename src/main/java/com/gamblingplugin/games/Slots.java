package com.gamblingplugin.games;

import com.gamblingplugin.GamblingPlugin;
import com.gamblingplugin.structures.Slots3DStructure;
import org.bukkit.entity.Player;

public class Slots extends Game {

    public Slots(GamblingPlugin plugin) {
        super(plugin, "slots");
    }

    @Override
    public void play(Player player, double bet, String[] args) {
        // Check balance
        if (!plugin.getEconomyManager().has(player, bet)) {
            player.sendMessage(plugin.getConfigManager().getMessage("insufficient-funds"));
            return;
        }

        // 1. Check if near a 3D physical slot machine structure
        Slots3DStructure slotStruct = plugin.getStructureManager().getSlotsStructure(player.getLocation());
        if (slotStruct != null) {
            slotStruct.spin(player, bet, null);
            return;
        }

        // 2. Otherwise spawn the 3D ItemDisplay cabinet in front of player
        PhysicalSlotsCabinet cabinet = new PhysicalSlotsCabinet(plugin, player.getLocation().add(player.getLocation().getDirection().multiply(2.2)));
        cabinet.spin(player, bet);
    }
}
