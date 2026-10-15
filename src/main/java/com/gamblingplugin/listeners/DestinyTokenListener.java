package com.gamblingplugin.listeners;

import com.gamblingplugin.GamblingPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.concurrent.ThreadLocalRandom;

public class DestinyTokenListener implements Listener {

    private final GamblingPlugin plugin;
    private final NamespacedKey tokenKey1;
    private final NamespacedKey tokenKey2;

    public DestinyTokenListener(GamblingPlugin plugin) {
        this.plugin = plugin;
        this.tokenKey1 = new NamespacedKey("gamblingplugin", "destiny_token");
        this.tokenKey2 = new NamespacedKey(plugin, "destiny_token");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onUseToken(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        boolean isToken = pdc.has(tokenKey1, PersistentDataType.BYTE)
                || pdc.has(tokenKey2, PersistentDataType.BYTE)
                || (meta.hasDisplayName() && meta.getDisplayName().contains("Moneda de la Fortuna Suprema"));

        if (!isToken) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        // Consume 1 token safely
        if (event.getHand() == EquipmentSlot.HAND) {
            ItemStack main = player.getInventory().getItemInMainHand();
            if (main.getAmount() > 1) {
                main.setAmount(main.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
        } else if (event.getHand() == EquipmentSlot.OFF_HAND) {
            ItemStack off = player.getInventory().getItemInOffHand();
            if (off.getAmount() > 1) {
                off.setAmount(off.getAmount() - 1);
            } else {
                player.getInventory().setItemInOffHand(null);
            }
        }

        // Give reward
        double cashReward = ThreadLocalRandom.current().nextInt(2500, 10001);
        plugin.getEconomyManager().deposit(player, cashReward);

        Location loc = player.getLocation();
        if (loc.getWorld() != null) {
            loc.getWorld().playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.1f);
            loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.4f);
            loc.getWorld().spawnParticle(Particle.TOTEM, loc.clone().add(0, 1.0, 0), 40, 0.5, 0.5, 0.5, 0.2);
            loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc.clone().add(0, 1.0, 0), 25, 0.4, 0.4, 0.4, 0.1);
        }

        player.sendMessage("§6========================================");
        player.sendMessage("§e✦ ¡MONEDA DE LA FORTUNA SUPREMA ACTIVADA! ✦");
        player.sendMessage("§a✔ Recompensa de Casino: §e+" + plugin.getEconomyManager().format(cashReward));
        player.sendMessage("§d✔ Sinergia Multi-Plugin: §f¡Has sido bendecido con la Gracia de la Suerte Arcana!");
        player.sendMessage("§6========================================");
    }
}
