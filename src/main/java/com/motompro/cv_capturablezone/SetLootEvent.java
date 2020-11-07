package com.motompro.cv_capturablezone;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class SetLootEvent implements Listener {

    private CV_CapturableZone plugin;

    public SetLootEvent(CV_CapturableZone plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerCloseInventory(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        InventoryView inventory = e.getView();

        if(inventory.getTitle().equals("Zone loots")) {
            for(Zone zone : plugin.getZones()) {
                if(!zone.isCreated()) {
                    if(zone.getCreator().equals(player.getUniqueId())) {
                        zone.setLoots(createLootList(inventory));
                        zone.create();
                        break;
                    }
                } else {
                    if(zone.isEditingLoots) {
                        zone.isEditingLoots = false;
                        zone.editLoots(createLootList(inventory));
                        player.sendMessage(CV_CapturableZone.MESSAGE_PREFIX + ChatColor.GREEN + "Le loot de la zone a été modifié.");
                        break;
                    }
                }
            }
        }
    }

    private ItemStack[] createLootList(InventoryView inventory) {
        ArrayList<ItemStack> itemsArrayList = new ArrayList<>();
        for(int i = 0; i < 54; i++) {
            if(!inventory.getItem(i).getType().equals(Material.AIR)) {
                itemsArrayList.add(inventory.getItem(i));
            }
        }
        ItemStack[] items = new ItemStack[itemsArrayList.size()];
        items = itemsArrayList.toArray(items);
        return items;
    }
}
