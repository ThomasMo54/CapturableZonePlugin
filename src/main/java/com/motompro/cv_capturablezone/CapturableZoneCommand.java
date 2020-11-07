package com.motompro.cv_capturablezone;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

public class CapturableZoneCommand implements CommandExecutor {

    private CV_CapturableZone plugin;

    public CapturableZoneCommand(CV_CapturableZone plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player;
        if(sender instanceof Player) {
            player = (Player) sender;
        } else {
            return false;
        }

        if(args.length == 0) {
            helpMessage(player);
            return false;
        }
        String action = args[0];

        if(action.equals("create")) {
            if(args.length < 4) {
                player.sendMessage(CV_CapturableZone.MESSAGE_PREFIX + ChatColor.RED + "Commande incorrecte : /capturablezone create <rayon> <hauteur> <nom> <random>");
                return false;
            }
            int radius = Integer.parseInt(args[1]);
            if(radius <= 0) {
                player.sendMessage(CV_CapturableZone.MESSAGE_PREFIX + ChatColor.RED + "Le rayon doit être superieur à 0 !");
                return false;
            }

            int height = Integer.parseInt(args[2]);
            if(height <= 0) {
                player.sendMessage(CV_CapturableZone.MESSAGE_PREFIX + ChatColor.RED + "La hauteur de la zone doit être superieure à 0 !");
                return false;
            }

            String name = args[3];
            if(plugin.zoneExists(name)) {
                player.sendMessage(CV_CapturableZone.MESSAGE_PREFIX + ChatColor.RED + "Une zone avec ce nom existe déjà !");
                return false;
            }

            if(args.length == 5) {
                if(args[4].equals("random")) {
                    plugin.addZone(name, player.getLocation(), radius, height,true, player);
                    player.sendMessage(CV_CapturableZone.MESSAGE_PREFIX + ChatColor.GREEN + "Une nouvelle zone a été créée.");
                    return false;
                }
            }

            plugin.addZone(name, player.getLocation(), radius, height,false, player);
            player.sendMessage(CV_CapturableZone.MESSAGE_PREFIX + ChatColor.GREEN + "Une nouvelle zone a été créée.");
        }

        if(action.equals("delete")) {
            if(args.length < 2) {
                player.sendMessage(CV_CapturableZone.MESSAGE_PREFIX + ChatColor.RED + "Commande incorrecte : /capturablezone delete <nom>");
                return false;
            }

            String name = args[1];
            if(!plugin.zoneExists(name)) {
                player.sendMessage(CV_CapturableZone.MESSAGE_PREFIX + ChatColor.RED + "Cette zone n'existe pas !");
                return false;
            }

            plugin.deleteZone(name);
            player.sendMessage(CV_CapturableZone.MESSAGE_PREFIX + ChatColor.GREEN + "Une zone a été supprimée.");
        }

        if(action.equals("edit")) {
            if(args.length < 3) {
                player.sendMessage(CV_CapturableZone.MESSAGE_PREFIX + ChatColor.RED + "Commande incorrecte : /capturablezone edit <nom> <param> <valeur>");
                return false;
            }

            String name = args[1];
            if(!plugin.zoneExists(name)) {
                player.sendMessage(CV_CapturableZone.MESSAGE_PREFIX + ChatColor.RED + "Cette zone n'existe pas !");
                return false;
            }

            String param = args[2];
            if(param.equals("name")) {
                for(Zone zone : plugin.getZones()) {
                    if(zone.getName().equals(name)) {
                        player.sendMessage(CV_CapturableZone.MESSAGE_PREFIX + ChatColor.AQUA + zone.getName() + ChatColor.GREEN + " a été changé en " + ChatColor.AQUA + args[3] + ".");
                        zone.setName(args[3]);
                        break;
                    }
                }
            }
            if(param.equals("radius")) {
                for(Zone zone : plugin.getZones()) {
                    if(zone.getName().equals(name)) {
                        zone.setRadius(Integer.parseInt(args[3]));
                        player.sendMessage(CV_CapturableZone.MESSAGE_PREFIX + ChatColor.GREEN + "Le rayon de la zone a été modifié à " + ChatColor.AQUA + args[3] + ".");
                        break;
                    }
                }
            }
            if(param.equals("height")) {
                for(Zone zone : plugin.getZones()) {
                    if(zone.getName().equals(name)) {
                        zone.setHeight(Integer.parseInt(args[3]));
                        player.sendMessage(CV_CapturableZone.MESSAGE_PREFIX + ChatColor.GREEN + "La hauteur de la zone a été modifié à " + ChatColor.AQUA + args[3] + ".");
                        break;
                    }
                }
            }
            if(param.equals("loot")) {
                for(Zone zone : plugin.getZones()) {
                    if(zone.getName().equals(name)) {
                        zone.isEditingLoots = true;
                        Inventory inventory = Bukkit.createInventory(null, 54, "Zone loots");
                        inventory.addItem(zone.getLoots());
                        player.openInventory(inventory);
                        break;
                    }
                }
            }
            if(param.equals("random")) {
                for(Zone zone : plugin.getZones()) {
                    if(zone.getName().equals(name)) {
                        if(args[3].equals("true")) {
                            zone.setRandomLoot(true);
                            player.sendMessage(CV_CapturableZone.MESSAGE_PREFIX + ChatColor.GREEN + "Le loot de la zone est maintenant aléatoire.");
                        } else {
                            if(args[3].equals("false")) {
                                zone.setRandomLoot(false);
                                player.sendMessage(CV_CapturableZone.MESSAGE_PREFIX + ChatColor.GREEN + "Le loot de la zone n'est plus aléatoire.");
                            } else {
                                player.sendMessage(CV_CapturableZone.MESSAGE_PREFIX + ChatColor.RED + "Valeur incorrecte ! (true/false)");
                            }
                        }
                        break;
                    }
                }
            }
        }

        if(action.equals("help")) {
            helpMessage(player);
        }

        return false;
    }

    private void helpMessage(Player player) {
        player.sendMessage(CV_CapturableZone.MESSAGE_PREFIX + ChatColor.GREEN + "Liste des commandes :");
        player.sendMessage(ChatColor.GOLD + "- " + ChatColor.GREEN + "/capturablezone create <rayon> <hauteur> <nom> <random> : crée une nouvelle zone capturable de rayon donné");
        player.sendMessage(ChatColor.GOLD + "- " + ChatColor.GREEN + "/capturablezone delete <nom> : supprime la zone capturable correspondant au nom donné");
        player.sendMessage(ChatColor.GOLD + "- " + ChatColor.GREEN + "/capturablezone edit <nom> <param> <valeur> : modifie un paramètre de la zone correspondant au nom donné");
    }
}
