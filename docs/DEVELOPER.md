# RCItems Developer Documentation

Das package `de.raidcraft.api.items` enthält Interfaces und Funktionen zur Verwaltung und Erstellung von Custom Items.

Die eigentliche Implementierung von Custom Items befindet sich im [RCItems](../README.md) Plugin. Ist das Plugin nicht installiert findet ein Fallback auf das Vanilla Minecraft Item System statt.

- [Getting started](#getting-started)
- [Useage](#useage)

## Getting started

Um die Custom Items API zu nutzen muss nur die [RaidCraft API](https://git.faldoria.de/raidcraft/raidcraft-api) als Dependency hinzugefügt werden.

Damit das System normale Items von Custom Items unterscheiden kann wird bei jedem Custom Item eine [MetaDataTooltip](https://git.faldoria.de/raidcraft/raidcraft-api/tree/master/src/main/java/de/raidcraft/api/items/tooltip/MetaDataTooltip.java) Zeile hinzugefügt in der eine unsichtbare ID kodiert ist.

Außerdem wurde direkt der `ItemStack` um einen [CustomItemStack](https://git.faldoria.de/raidcraft/raidcraft-api/tree/master/src/main/java/de/raidcraft/api/items/CustomItemStack.java) erweitert, der sich um die Anwendung der Tooltips und Metadaten kümmert.

## Useage

Um ein `ItemStack`, z.B. von einer Spielereingabe zu erhalten, kann folgende Funktion verwendet werden. Dabei wird bei dieser Methode immer der Standard Minecraft `ItemStack` zurückgegeben.

```java
// can get a custom item by the specified raidcraft item id starting with rc
ItemStack customItemFromId = RaidCraft.getItem("rc1234");
// can get a custom stored object by its id starting with so
ItemStack storedObjectFromId = RaidCraft.getItem("so1234");
// can get a custom item by its full displayName
ItemStack namedCustomItem = RaidCraft.getItem("Vase aus Faldoria");
// can get normal vanilla minecraft items
ItemStack minecraftItem = RaidCraft.getItem("wool");
```

Um mit der [CustomItem](https://git.faldoria.de/raidcraft/raidcraft-api/tree/master/src/main/java/de/raidcraft/api/items/CustomItem.java) API zu interagieren kann anstatt einem `ItemStack` ein `CustomItem` oder ein `CustomItemStack` abgefragt werden.

```java
// gets the raw model of the given custom item
CustomItem item = RaidCraft.getCustomItem("rc1234");
// the item does not exist in the world yet and needs to be created first
CustomItemStack itemStack = item.createNewItem();
// now you can give players the item
player.getInventory().addItem(itemStack);
// or drop it in the world
world.dropItem(location, itemStack);
```