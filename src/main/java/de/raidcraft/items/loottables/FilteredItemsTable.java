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

            FilteredItemsTable filteredItemsTable = new FilteredItemsTable(config.getInt("min-level", 0), config.getInt("max-level", 0));
            filteredItemsTable.load(config);
            return filteredItemsTable;
        }
    }

    protected int minItemLevel;
    protected int maxItemLevel;
    protected List<ItemType> itemTypes = new ArrayList<>();
    protected List<ItemQuality> itemQualities = new ArrayList<>();
    protected List<ItemBindType> bindTypes = new ArrayList<>();
    protected List<String> includeCategories = new ArrayList<>();
    protected List<String> excludeCategories = new ArrayList<>();
    protected List<Integer> itemIds = new ArrayList<>();
    protected Pattern nameFilter;
    protected int idFilterMin;
    protected int idFilterMax;
    protected boolean ignoreUnlootable;

    public FilteredItemsTable(int minItemLevel, int maxItemLevel) {

        this.minItemLevel = minItemLevel;
        this.maxItemLevel = maxItemLevel;
    }

    public void load(ConfigurationSection config) {

        itemTypes = new ArrayList<>();
        for (String type : config.getStringList("types")) {
            ItemType itemType = ItemType.fromString(type);
            if (itemType != null) {
                itemTypes.add(itemType);
            } else {
                RaidCraft.LOGGER.warning("Invalid item type " + type + " in loot table " + ConfigUtil.getFileName(config));
            }
        }

        itemQualities = new ArrayList<>();
        for (String quality : config.getStringList("qualities")) {
            ItemQuality itemQuality = ItemQuality.fromString(quality);
            if (itemQuality != null) {
                itemQualities.add(itemQuality);
            } else {
                RaidCraft.LOGGER.warning("Invalid item quality " + quality + " in loot table " + ConfigUtil.getFileName(config));
            }
        }

        bindTypes = new ArrayList<>();
        for (String binding : config.getStringList("bind-types")) {
            ItemBindType itemBindType = ItemBindType.fromString(binding);
            if (itemBindType != null) {
                bindTypes.add(itemBindType);
            } else {
                RaidCraft.LOGGER.warning("Invalid item bind type " + binding + " in loot table " + ConfigUtil.getFileName(config));
            }
        }

        includeCategories = config.getStringList("include-categories");
        excludeCategories = config.getStringList("exclude-categories");

        itemIds = config.getIntegerList("ids");

        if (config.isSet("displayName-filter")) {
            nameFilter = Pattern.compile(config.getString("displayName-filter"));
        } else {
            nameFilter = null;
        }

        idFilterMin = config.getInt("min-id", 0);
        idFilterMax = config.getInt("max-id", 0);

        ignoreUnlootable = config.getBoolean("ignore-unlootable", false);

        loadItems();
    }

    public void loadItems() {

        clearContents();
        if (minItemLevel < 0) minItemLevel = 0;
        if (maxItemLevel < 0) maxItemLevel = 0;
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
