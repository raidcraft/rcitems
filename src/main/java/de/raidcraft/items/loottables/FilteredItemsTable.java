package de.raidcraft.items.loottables;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.ItemBindType;
import de.raidcraft.api.items.ItemCategory;
import de.raidcraft.api.items.ItemQuality;
import de.raidcraft.api.items.ItemType;
import de.raidcraft.api.random.GenericRDSTable;
import de.raidcraft.api.random.Loadable;
import de.raidcraft.api.random.RDSObject;
import de.raidcraft.api.random.RDSObjectFactory;
import de.raidcraft.api.random.objects.ItemLootObject;
import de.raidcraft.items.ItemsPlugin;
import de.raidcraft.util.ConfigUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author mdoering
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FilteredItemsTable extends GenericRDSTable implements Loadable {

    @RDSObjectFactory.Name("filtered-custom-items")
    public static class Factory implements RDSObjectFactory {

        @Override
        public RDSObject createInstance(ConfigurationSection config) {

            return new FilteredItemsTable(config, config.getInt("min-level", 0), config.getInt("max-level", 0));
        }
    }

    public FilteredItemsTable(ConfigurationSection config, int minItemLevel, int maxItemLevel) {

        config.set("min-level", minItemLevel);
        config.set("max-level", maxItemLevel);
        load(config);
    }

    public void load(ConfigurationSection config) {

        int minItemLevel = config.getInt("min-level", 0);
        int maxItemLevel = config.getInt("max-level", 0);

        List<ItemType> itemTypes = new ArrayList<>();
        for (String type : config.getStringList("types")) {
            ItemType itemType = ItemType.fromString(type);
            if (itemType != null) {
                itemTypes.add(itemType);
            } else {
                RaidCraft.LOGGER.warning("Invalid item type " + type + " in loot table " + ConfigUtil.getFileName(config));
            }
        }

        List<ItemQuality> itemQualities = new ArrayList<>();
        for (String quality : config.getStringList("qualities")) {
            ItemQuality itemQuality = ItemQuality.fromString(quality);
            if (itemQuality != null) {
                itemQualities.add(itemQuality);
            } else {
                RaidCraft.LOGGER.warning("Invalid item quality " + quality + " in loot table " + ConfigUtil.getFileName(config));
            }
        }

        List<ItemBindType> bindTypes = new ArrayList<>();
        for (String binding : config.getStringList("bind-types")) {
            ItemBindType itemBindType = ItemBindType.fromString(binding);
            if (itemBindType != null) {
                bindTypes.add(itemBindType);
            } else {
                RaidCraft.LOGGER.warning("Invalid item bind type " + binding + " in loot table " + ConfigUtil.getFileName(config));
            }
        }

        List<String> includeCategories = config.getStringList("include-categories");
        List<String> excludeCategories = config.getStringList("exclude-categories");

        List<Integer> itemIds = config.getIntegerList("ids");

        Pattern nameFilter;
        if (config.isSet("name-filter")) {
            nameFilter = Pattern.compile(config.getString("name-filter"));
        } else {
            nameFilter = null;
        }

        int idFilterMin = config.getInt("min-id", 0);
        int idFilterMax = config.getInt("max-id", 0);

        boolean ignoreUnlootable = config.getBoolean("ignore-unlootable", false);

        RaidCraft.getComponent(ItemsPlugin.class).getCustomItemManager().getLoadedCustomItems().stream()
                .filter(item -> itemTypes.isEmpty() || itemTypes.contains(item.getType()))
                .filter(item -> itemQualities.isEmpty() || itemQualities.contains(item.getQuality()))
                .filter(item -> bindTypes.isEmpty() || bindTypes.contains(item.getBindType()))
                .filter(item -> itemIds.isEmpty() || itemIds.contains(item.getId()))
                .filter(item -> minItemLevel < 1 || item.getItemLevel() >= minItemLevel)
                .filter(item -> maxItemLevel < 1 || item.getItemLevel() <= maxItemLevel)
                .filter(item -> idFilterMin < 1 || item.getId() >= idFilterMin)
                .filter(item -> idFilterMax < 1 || item.getId() <= idFilterMax)
                .filter(item -> nameFilter == null || nameFilter.matcher(item.getName()).matches())
                .filter(item -> includeCategories.isEmpty() || item.getCategories().stream().map(ItemCategory::getName).anyMatch(includeCategories::contains))
                .filter(item -> excludeCategories.isEmpty() || item.getCategories().stream().map(ItemCategory::getName).noneMatch(excludeCategories::contains))
                .filter(item -> ignoreUnlootable || item.isLootable())
                .forEach(item -> addEntry(new ItemLootObject(item)));
    }
}
