package com.motompro.cv_capturablezone;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.UUID;

public class PlayerEnterZoneEvent implements Listener {

    private CV_CapturableZone plugin;

    public PlayerEnterZoneEvent(CV_CapturableZone plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        UUID playerUUID = player.getUniqueId();

        for(Zone zone : plugin.getZones()) {
            if(player.getWorld().getName().equals(zone.getCenter().getWorld().getName())) {
                if(zone.isLocationInside(e.getTo())) {
                    if(!zone.isPlayerInside(playerUUID)) {
                        zone.playerEnter(playerUUID);
                        if(zone.isCaptured()) {
                            player.sendMessage(CV_CapturableZone.MESSAGE_PREFIX + ChatColor.GREEN + "Vous entrez dans la zone " + zone.getName() + " capturée par " + Bukkit.getPlayer(zone.getCapturer()).getName() + ".");
                        } else {
                            player.sendMessage(CV_CapturableZone.MESSAGE_PREFIX + ChatColor.GREEN + "Vous entrez dans la zone " + zone.getName() + ".");
                        }
                    }
                } else {
                    if(zone.isPlayerInside(playerUUID)) {
                        zone.playerExit(playerUUID);
                    }
                }
            }
        }
    }
}
