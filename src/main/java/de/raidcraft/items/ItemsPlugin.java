package de.raidcraft.items;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.NestedCommand;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.BasePlugin;
import de.raidcraft.api.config.ConfigUtil;
import de.raidcraft.api.config.ConfigurationBase;
import de.raidcraft.api.config.KeyValueMap;
import de.raidcraft.api.config.Setting;
import de.raidcraft.api.items.CustomArmor;
import de.raidcraft.api.items.CustomEquipment;
import de.raidcraft.api.items.CustomItem;
import de.raidcraft.api.items.CustomItemManager;
import de.raidcraft.api.items.CustomItemStack;
import de.raidcraft.api.items.DuplicateCustomItemException;
import de.raidcraft.api.items.ItemAttribute;
import de.raidcraft.api.items.attachments.AttachableCustomItem;
import de.raidcraft.api.items.attachments.ConfiguredAttachment;
import de.raidcraft.items.commands.ItemCommands;
import de.raidcraft.items.commands.RecipeCommands;
import de.raidcraft.items.configs.AttachmentConfig;
import de.raidcraft.items.crafting.CraftingManager;
import de.raidcraft.items.equipment.ConfiguredArmor;
import de.raidcraft.items.equipment.ConfiguredWeapon;
import de.raidcraft.items.listener.PlayerListener;
import de.raidcraft.items.tables.crafting.TCraftingRecipe;
import de.raidcraft.items.tables.crafting.TCraftingRecipeIngredient;
import de.raidcraft.items.tables.items.TCustomArmor;
import de.raidcraft.items.tables.items.TCustomEquipment;
import de.raidcraft.items.tables.items.TCustomItem;
import de.raidcraft.items.tables.items.TCustomItemAttachment;
import de.raidcraft.items.tables.items.TCustomWeapon;
import de.raidcraft.items.tables.items.TEquipmentAttribute;
import de.raidcraft.items.tables.items.TItemAttachmentData;
import de.raidcraft.items.useable.UseableItem;
import de.raidcraft.util.CustomItemUtil;
import de.raidcraft.util.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Silthus
 */
public class ItemsPlugin extends BasePlugin {

    private final Set<Integer> loadedCustomItems = new HashSet<>();
    private final Map<String, AttachmentConfig> loadedAttachments = new HashMap<>();
    private LocalConfiguration config;
    private CraftingManager craftingManager;

    @Override
    public void enable() {

        config = configure(new LocalConfiguration(this), true);
        registerEvents(new PlayerListener(this));
        registerCommands(Commands.class);
        // the attachments need to load before the custom items because we use them in there
        loadAttachments();
        loadCustomItems();
        // load the crafting manager after init of the custom items
        craftingManager = new CraftingManager(this);
    }

    @Override
    public void disable() {

        // lets unload all of our custom items
        unloadRegisteredItems();
    }

    private void unloadRegisteredItems() {

        CustomItemManager component = RaidCraft.getComponent(CustomItemManager.class);
        for (int id : loadedCustomItems) {
            component.unregisterCustomItem(id);
        }
        loadedCustomItems.clear();
    }

    @Override
    public void reload() {

        config.reload();
        unloadRegisteredItems();
        // the attachments need to load before the custom items because we use them in there
        loadAttachments();
        loadCustomItems();
        // load the crafting manager after init of the custom items
        craftingManager.reload();
    }

    public CraftingManager getCraftingManager() {

        return craftingManager;
    }

    public LocalConfiguration getConfig() {

        return config;
    }

    private void loadAttachments() {

        loadedAttachments.clear();
        File dir = new File(getDataFolder(), "attachments/");
        dir.mkdirs();
        loadAttachments(dir);
    }

    private void loadAttachments(File dir) {

        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                loadAttachments(file);
            }
            if (!file.getName().endsWith(".yml")) {
                continue;
            }
            AttachmentConfig config = configure(new AttachmentConfig(this, file), false);
            loadedAttachments.put(config.getName(), config);
            getLogger().info("Loaded item attachment config: " + config.getName());
        }
    }

    private void loadCustomItems() {

        int loaded = 0;
        int failed = 0;
        loadedCustomItems.clear();
        // lets load all custom items that are defined in the database
        CustomItemManager component = RaidCraft.getComponent(CustomItemManager.class);
        Set<TCustomItem> customItems = getDatabase().find(TCustomItem.class).findSet();
        for (TCustomItem item : customItems) {

            try {
                CustomItem customItem;
                TCustomEquipment equipment = getDatabase().find(TCustomEquipment.class).where().eq("item_id", item.getId()).findUnique();
                switch (item.getItemType()) {
                    case WEAPON:
                        if (equipment == null) continue;
                        TCustomWeapon weapon = getDatabase().find(TCustomWeapon.class).where().eq("equipment_id", equipment.getId()).findUnique();
                        if (weapon == null) continue;
                        customItem = new ConfiguredWeapon(weapon);
                        break;
                    case ARMOR:
                        if (equipment == null) continue;
                        TCustomArmor armor = getDatabase().find(TCustomArmor.class).where().eq("equipment_id", equipment.getId()).findUnique();
                        if (armor == null) continue;
                        customItem = new ConfiguredArmor(armor);
                        break;
                    case USEABLE:
                        customItem = new UseableItem(item);
                        break;
                    case EQUIPMENT:
                        if (equipment == null) continue;
                        customItem = new BaseEquipment(equipment);
                        break;
                    default:
                        customItem = new SimpleItem(item);
                        break;
                }
                // lets check for custom item attachments
                if (item.getAttachments() != null && !item.getAttachments().isEmpty()) {
                    for (TCustomItemAttachment attachment : item.getAttachments()) {
                        // lets create a new configured attachment
                        ConfiguredAttachment configuredAttachment = new ConfiguredAttachment(
                                attachment.getAttachmentName(),
                                attachment.getProviderName(),
                                attachment.getDescription(),
                                (attachment.getColor() == null || attachment.getColor().equals(""))
                                        ? ChatColor.WHITE : ChatColor.valueOf(attachment.getColor())
                        );
                        // if a local config file for the attachment exists load it into the memory map
                        String attachmentName = StringUtils.formatName(attachment.getAttachmentName());
                        if (loadedAttachments.containsKey(attachmentName)) {
                            configuredAttachment.merge(loadedAttachments.get(attachmentName));
                        }
                        // also merge our database
                        ArrayList<KeyValueMap> dataList = new ArrayList<>();
                        for (TItemAttachmentData data : attachment.getItemAttachmentDataList()) {
                            dataList.add(data);
                        }
                        configuredAttachment.merge(ConfigUtil.parseKeyValueTable(dataList));

                        ((AttachableCustomItem) customItem).addAttachment(configuredAttachment);
                    }
                }
                // lets calculate the item level
                if (customItem instanceof CustomEquipment && customItem.getItemLevel() < 1) {
                    int itemLevel = calculateItemLevel((CustomEquipment) customItem);
                    item.setItemLevel(itemLevel);
                    getDatabase().save(item);
                }
                // lets calculate the armor value if its an item
                if (customItem instanceof CustomArmor && ((CustomArmor) customItem).getArmorValue() < 1) {
                    double armorModifier = ((CustomArmor) customItem).getArmorType().getArmorModifier(customItem.getQuality(), customItem.getItemLevel());
                    double armorValue = ((CustomArmor) customItem).getEquipmentSlot().getArmorSlotModifier() * armorModifier;
                    ((CustomArmor) customItem).setArmorValue((int) armorValue);
                    TCustomArmor armor = getDatabase().find(TCustomArmor.class).where().eq("equipment_id", equipment.getId()).findUnique();
                    armor.setArmorValue((int) armorValue);
                    getDatabase().save(armor);
                }
                // register the actual custom item
                component.registerCustomItem(customItem);
                loadedCustomItems.add(customItem.getId());
                loaded++;
            } catch (DuplicateCustomItemException e) {
                failed++;
                getLogger().warning(e.getMessage());
            }
        }
        getLogger().info("Loaded " + loaded + "/" + loaded + failed + " Custom Items...");
    }

    /**
     * The item level is calculated based on the wow formula.
     * ItemValue = [(StatValue[1]*StatMod[1])^1.7095 + (StatValue[2]*StatMod[2])^1.7095 + ...]1/1.7095
     * ItemSlotValue = 	ItemValue / SlotMod
     * ItemLevel = 	ItemSlotValue * QualityModifier
     * @param item to calculate itemlevel for
     * @return item level
     */
    private int calculateItemLevel(CustomEquipment item) {

        double itemValue = 0;
        for (ItemAttribute attribute : item.getAttributes()) {
            itemValue += Math.pow((attribute.getValue() * attribute.getType().getItemLevelValue()), 1.7095);
        }
        itemValue = Math.pow(itemValue, 1 / 1.7095);
        double itemSlotValue = itemValue / item.getEquipmentSlot().getSlotModifier();
        return (int) ((itemSlotValue * item.getQuality().getQualityMultiplier()) + item.getQuality().getQualityModifier());
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {

        ArrayList<Class<?>> tables = new ArrayList<>();
        tables.add(TCustomItem.class);
        tables.add(TCustomEquipment.class);
        tables.add(TEquipmentAttribute.class);
        tables.add(TCustomWeapon.class);
        tables.add(TCustomArmor.class);
        tables.add(TCustomItemAttachment.class);
        tables.add(TItemAttachmentData.class);
        // custom crafting
        tables.add(TCraftingRecipe.class);
        tables.add(TCraftingRecipeIngredient.class);
        return tables;
    }

    public void updateItemDurability(ItemStack item, double chance) {

        // lets check the durability loss and negate it by using our own durability if it is a custom item
        if (!CustomItemUtil.isEquipment(item)) {
            return;
        }
        CustomItemStack customItem = RaidCraft.getCustomItem(item);
        // on each interact with the item the player has a chance of 0.1% chance to loose one durability point
        if (Math.random() < chance) {
            customItem.setCustomDurability(customItem.getCustomDurability() - 1);
        } else {
            item.setDurability(CustomItemUtil.getMinecraftDurability(item, customItem.getCustomDurability(), customItem.getMaxDurability()));
        }
    }

    public static class LocalConfiguration extends ConfigurationBase<ItemsPlugin> {

        public LocalConfiguration(ItemsPlugin plugin) {

            super(plugin, "config.yml");
        }

        @Setting("durability-loss-chance-on-use")
        public double durabilityLossChanceOnUse = 0.001;
        @Setting("durability-loss-chance-on-damage")
        public double durabilityLossChanceOnDamage = 0.0001;
        @Setting("hide-item-level")
        public boolean hideItemLevel = true;

        public int getDefaultCustomItem(int minecraftId) {

            if (!isSet("defaults." + minecraftId)) {
                return 0;
            }
            return getInt("defaults." + minecraftId, 0);
        }
    }

    public static class Commands {

        private final ItemsPlugin plugin;

        public Commands(ItemsPlugin plugin) {

            this.plugin = plugin;
        }

        @Command(
                aliases = {"rci", "item", "rcitems"},
                desc = "Custom Item Commands",
                min = 1
        )
        @NestedCommand(value = ItemCommands.class)
        public void items(CommandContext args, CommandSender sender) {

        }

        @Command(
                aliases = {"recipe", "rezept"},
                desc = "Custom Item Commands",
                min = 1
        )
        @NestedCommand(value = RecipeCommands.class)
        public void recipes(CommandContext args, CommandSender sender) {

        }
    }
}
