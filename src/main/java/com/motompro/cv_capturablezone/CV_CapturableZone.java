package com.motompro.cv_capturablezone;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class CV_CapturableZone extends JavaPlugin {

    private File zoneConfigFile;

    private FileConfiguration defaultConfig = this.getConfig();
    private FileConfiguration zonesConfig;

    private ArrayList<Zone> zones = new ArrayList<>();

    public static String MESSAGE_PREFIX = ChatColor.AQUA + "CV" + ChatColor.GOLD + "CapturableZone " + ChatColor.GRAY + "» ";

    @Override
    public void onEnable() {
        this.createZoneConfig();
        if(zonesConfig.getConfigurationSection("zones") == null) {
            zonesConfig.createSection("zones");
        }
        this.saveZonesConfig();

        defaultConfig.addDefault("timeToCapture", 10);
        defaultConfig.options().copyDefaults(true);
        this.saveConfig();

        for(String zoneName : zonesConfig.getConfigurationSection("zones").getKeys(false)) {
            ConfigurationSection zoneSection = zonesConfig.getConfigurationSection("zones").getConfigurationSection(zoneName);
            String name = zoneSection.getString("name");
            Location center = Location.deserialize(zoneSection.getConfigurationSection("center").getValues(false));
            int radius = zoneSection.getInt("radius");
            int height = zoneSection.getInt("height");

            UUID capturer = null;
            String capturerStr = zoneSection.getString("capturer");
            if(capturerStr != null) capturer = UUID.fromString(capturerStr);

            ArrayList<ItemStack> lootsArrayList = new ArrayList<>();
            for(String itemName : zoneSection.getConfigurationSection("loots").getKeys(false)) {
                lootsArrayList.add(ItemStack.deserialize(zoneSection.getConfigurationSection("loots").getConfigurationSection(itemName).getValues(false)));
            }
            ItemStack[] loots = new ItemStack[lootsArrayList.size()];
            loots = lootsArrayList.toArray(loots);

            boolean randomLoot = zoneSection.getBoolean("randomLoot");
            int nRandom = zoneSection.getInt("nRandom");

            zones.add(new Zone(name, center, radius, height, capturer, loots, randomLoot, nRandom,this));
        }

        new ZoneDrawer(this).runTaskTimer(this, 0, 10);

        getCommand("capturablezone").setExecutor(new CapturableZoneCommand(this));

        getServer().getPluginManager().registerEvents(new PlayerEnterZoneEvent(this), this);
        getServer().getPluginManager().registerEvents(new SetLootEvent(this), this);
    }

    @Override
    public void onDisable() {}

    public void createZoneConfig() {
        zoneConfigFile = new File(getDataFolder(), "zones.yml");
        if (!zoneConfigFile.exists()) {
            zoneConfigFile.getParentFile().mkdirs();
            saveResource("zones.yml", false);
        }

        zonesConfig = new YamlConfiguration();
        try {
            zonesConfig.load(zoneConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getZonesConfig() {
        return this.zonesConfig;
    }

    public void saveZonesConfig() {
        try {
            zonesConfig.save(zoneConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Zone> getZones() {
        return zones;
    }

    public void addZone(String name, Location center, int radius, int height, boolean randomLoot, int nRandom, Player player) {
        zones.add(new Zone(name, center, radius, height, player.getUniqueId(), randomLoot, nRandom, this));

        Inventory inventory = Bukkit.createInventory(null, 54, "Zone loots");
        player.openInventory(inventory);
    }

    public void deleteZone(String name) {
        for(int i = 0; i < zones.size(); i++) {
            if(zones.get(i).getName().equals(name)) {
                zones.get(i).delete();
                zones.remove(i);
                break;
            }
        }
    }

    public boolean zoneExists(String name) {
        for(Zone zone : zones) {
            if(zone.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}

class ZoneDrawer extends BukkitRunnable {

    private CV_CapturableZone plugin;
    private HashMap<UUID, Integer> captureCountdown = new HashMap<>();

    public ZoneDrawer(CV_CapturableZone plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        ArrayList<Zone> zones = plugin.getZones();

        for(Zone zone : zones) {
            Location center = zone.getCenter();
            int radius = zone.getRadius();
            int height = zone.getHeight();
            boolean captured = zone.isCaptured();
            double angle = 0;
            double step = Math.PI / (radius * 2);

            for(int h = 0; h < height; h++) {
                for(int i = 0; i < radius * 4; i++) {
                    double x = radius * Math.cos(angle);
                    double z = radius * Math.sin(angle);
                    if(captured) {
                        center.getWorld().spawnParticle(Particle.REDSTONE, center.getBlockX() + x, center.getBlockY() + h, center.getBlockZ() + z, 0, Float.MIN_VALUE, 1, 0);
                    } else {
                        center.getWorld().spawnParticle(Particle.REDSTONE, center.getBlockX() + x, center.getBlockY() + h, center.getBlockZ() + z, 0, 0, 0, 0);
                    }
                    angle += step;
                }
            }
        }
    }
}
