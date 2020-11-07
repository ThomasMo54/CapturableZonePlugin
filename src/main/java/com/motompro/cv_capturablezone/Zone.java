package com.motompro.cv_capturablezone;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Zone {

    private String name;
    private Location center;
    private int radius, height;
    private UUID creator = null;
    private boolean randomLoot = false;

    private CV_CapturableZone plugin;

    private boolean captured = false;
    private UUID capturer = null;
    private boolean created = false;
    public boolean isEditingLoots = false;
    private ArrayList<UUID> playersInside = new ArrayList<>();
    public HashMap<UUID, Long> enterTime = new HashMap<>();
    private ItemStack[] loots;

    // New zone contructor
    public Zone(String name, Location center, int radius, int height, UUID creator, boolean randomLoot, CV_CapturableZone plugin) {
        this.name = name;
        this.center = center;
        center.add(0, 1, 0);
        this.radius = radius;
        this.height = height;
        this.creator = creator;
        this.randomLoot = randomLoot;
        this.plugin = plugin;
    }

    // From save constructor
    public Zone(String name, Location center, int radius, int height, UUID capturer, ItemStack[] loots, boolean randomLoot, CV_CapturableZone plugin) {
        this.name = name;
        this.center = center;
        this.radius = radius;
        this.height = height;
        this.capturer = capturer;
        if(capturer != null) {
            this.captured = true;
        }
        this.loots = loots;
        this.randomLoot = randomLoot;
        this.plugin = plugin;
        this.created = true;

        new ZoneUpdater(this, plugin).runTaskTimer(plugin, 0, 5);
    }

    public void create() {
        if(created) return;
        HashMap<String, Object> configSection = new HashMap<>();
        configSection.put("name", name);
        configSection.put("center", center.serialize());
        configSection.put("radius", radius);
        configSection.put("height", height);
        configSection.put("captured", false);
        configSection.put("randomLoot", randomLoot);
        plugin.getZonesConfig().getConfigurationSection("zones").createSection(name, configSection);
        plugin.getZonesConfig().getConfigurationSection("zones").getConfigurationSection(name).createSection("loots");

        for(int i = 0; i < loots.length; i++) {
            plugin.getZonesConfig().getConfigurationSection("zones").getConfigurationSection(name).getConfigurationSection("loots").createSection("loot" + i, loots[i].serialize());
        }

        plugin.saveZonesConfig();
        plugin.createZoneConfig();

        new ZoneUpdater(this, plugin).runTaskTimer(plugin, 0, 5);

        this.created = true;
    }

    public void delete() {
        plugin.getZonesConfig().getConfigurationSection("zones").set(name, null);
        plugin.saveZonesConfig();
    }

    public void setLoots(ItemStack[] loots) {
        this.loots = loots;
    }

    public void editLoots(ItemStack[] loots) {
        this.loots = loots;

        plugin.getZonesConfig().getConfigurationSection("zones").getConfigurationSection(name).set("loots", null);
        plugin.getZonesConfig().getConfigurationSection("zones").getConfigurationSection(name).createSection("loots");

        for(int i = 0; i < loots.length; i++) {
            plugin.getZonesConfig().getConfigurationSection("zones").getConfigurationSection(name).getConfigurationSection("loots").createSection("loot" + i, loots[i].serialize());
        }

        plugin.saveZonesConfig();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Map<String, Object> zoneSection = plugin.getZonesConfig().getConfigurationSection("zones").getConfigurationSection(this.name).getValues(true);
        zoneSection.put("name", name);
        plugin.getZonesConfig().getConfigurationSection("zones").set(this.name, null);
        plugin.getZonesConfig().getConfigurationSection("zones").createSection(name, zoneSection);
        plugin.saveZonesConfig();
        this.name = name;
    }

    public Location getCenter() {
        return center;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
        plugin.getZonesConfig().getConfigurationSection("zones").getConfigurationSection(name).set("radius", radius);
        plugin.saveZonesConfig();
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
        plugin.getZonesConfig().getConfigurationSection("zones").getConfigurationSection(name).set("height", height);
        plugin.saveZonesConfig();
    }

    public UUID getCapturer() {
        return capturer;
    }

    public UUID getCreator() {
        return creator;
    }

    public ArrayList<UUID> getPlayersInside() {
        return playersInside;
    }

    public ItemStack[] getLoots() {
        return loots;
    }

    public boolean isCreated() {
        return created;
    }

    public boolean isCaptured() {
        return captured;
    }

    public void setRandomLoot(boolean random) {
        this.randomLoot = random;
    }

    public boolean isLocationInside(Location playerLoc) {
        if(!playerLoc.getWorld().getName().equals(center.getWorld().getName())) return false;
        if(playerLoc.getBlockY() > center.getBlockY() + height - 1 || playerLoc.getBlockY() < center.getBlockY() - 1) return false;
        double distanceSquared = Math.pow((center.getX() - playerLoc.getX()), 2) + Math.pow((center.getZ() - playerLoc.getZ()), 2);
        return (distanceSquared <= Math.pow(radius, 2));
    }

    public boolean isPlayerInside(UUID playerUUID) {
        return playersInside.contains(playerUUID);
    }

    public void playerEnter(UUID playerUUID) {
        if(!playersInside.contains(playerUUID)) {
            playersInside.add(playerUUID);
            enterTime.put(playerUUID, System.currentTimeMillis());
        }
    }

    public void playerExit(UUID playerUUID) {
        if(playersInside.contains(playerUUID)) {
            playersInside.remove(playerUUID);
            enterTime.remove(playerUUID);
        }
    }

    public void capture(UUID capturer) {
        captured = true;
        this.capturer = capturer;

        plugin.getZonesConfig().getConfigurationSection("zones").getConfigurationSection(name).set("captured", true);
        plugin.getZonesConfig().getConfigurationSection("zones").getConfigurationSection(name).set("capturer", capturer.toString());
        plugin.saveZonesConfig();

        enterTime.replaceAll((k,v) -> v = System.currentTimeMillis());

        Player capturerPlayer = Bukkit.getPlayer(capturer);
        capturerPlayer.sendMessage(CV_CapturableZone.MESSAGE_PREFIX + ChatColor.GREEN + "Vous avez capturé la zone " + name + " !");
        capturerPlayer.playSound(capturerPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 3.0f, 0.5f);

        if(randomLoot) {
            ItemStack loot = loots[new Random().nextInt(loots.length)];
            capturerPlayer.getInventory().addItem(loot);
        } else {
            capturerPlayer.getInventory().addItem(loots);
        }
    }
}

class ZoneUpdater extends BukkitRunnable {

    private Zone zone;
    private CV_CapturableZone plugin;

    ZoneUpdater(Zone zone, CV_CapturableZone plugin) {
        this.zone = zone;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for(UUID playerUUID : zone.getPlayersInside()) {
            if(!playerUUID.equals(zone.getCapturer())) {
                if(zone.isPlayerInside(zone.getCapturer())) {
                    zone.enterTime.put(playerUUID, System.currentTimeMillis());
                } else {
                    long countdown = plugin.getConfig().getInt("timeToCapture") - ((System.currentTimeMillis() - zone.enterTime.get(playerUUID)) / 1000);
                    if(countdown == 0) {
                        zone.capture(playerUUID);
                    } else {
                        Bukkit.getPlayer(playerUUID).sendTitle(String.valueOf(countdown), "", 0, 10, 0);
                    }
                }
            }
        }
    }
}