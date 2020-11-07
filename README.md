# CapturableZonePlugin

A Minecraft plugin which adds the possibility to create capturable zones with a loot system.

## Commands

### /capturablezone create \<test> \<radius> \<height> \<name> \<random>
* Radius (int > 0) : The zone's radius. The player position is the center of this zone.
* Height (int > 0) : The zone's height.
* Name (String) : The zone's name.
* Random : optional, set loot random.

### /capturablezone delete \<name>
* Name (String) : The name of the zone to delete.

### /capturablezone edit \<name> \<param> \<value>
* Name (String) : The name of the zone to edit.
* Param (String) : Can be name, radius, height, loot, random
* Value :
  - For name : a String representing the new name of the zone.
  - For radius : an int representing the new radius of the zone.
  - For height : an int representing the new height of the zone.
  - For loot : Nothing, a GUI will appear.
  - For random : a boolean, true or false.
