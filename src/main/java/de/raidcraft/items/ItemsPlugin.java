package de.raidcraft.items;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.NestedCommand;
import de.raidcraft.CommonConfig;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.BasePlugin;
import de.raidcraft.api.action.ActionAPI;
import de.raidcraft.api.chat.Chat;
import de.raidcraft.api.config.*;
import de.raidcraft.api.conversations.Conversations;
import de.raidcraft.api.items.*;
import de.raidcraft.api.items.attachments.AttachableCustomItem;
import de.raidcraft.api.items.attachments.ConfiguredAttachment;
import de.raidcraft.api.quests.Quests;
import de.raidcraft.api.random.RDS;
import de.raidcraft.items.commands.*;
import de.raidcraft.items.configs.AttachmentConfig;
import de.raidcraft.items.configs.NamedYAMLCustomItem;
import de.raidcraft.items.crafting.CraftingManager;
import de.raidcraft.items.equipment.ConfiguredArmor;
import de.raidcraft.items.equipment.ConfiguredWeapon;
import de.raidcraft.items.items.DatabaseEquipment;
import de.raidcraft.items.items.SimpleItem;
import de.raidcraft.items.listener.PlayerListener;
import de.raidcraft.items.loottables.FilteredItemsTable;
import de.raidcraft.items.tables.crafting.TCraftingRecipe;
import de.raidcraft.items.tables.crafting.TCraftingRecipeIngredient;
import de.raidcraft.items.tables.items.*;
import de.raidcraft.items.trigger.CustomItemTrigger;
import de.raidcraft.items.trigger.InventoryTrigger;
import de.raidcraft.items.useable.UseableItem;
import de.raidcraft.util.ConfigUtil;
import de.raidcraft.util.CustomItemUtil;
import de.raidcraft.util.ItemUtils;
import de.raidcraft.util.StringUtils;
import de.raidcraft.util.fanciful.FancyMessage;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Silthus
 */
public class ItemsPlugin extends BasePlugin {

    private final Set<Integer> loadedCustomItems = new HashSet<>();
    private final Set<String> loadedConfigCustomItems = new HashSet<>();
    private final Map<String, AttachmentConfig> loadedAttachments = new HashMap<>();
    private LocalConfiguration config;
    private CraftingManager craftingManager;
    @Getter
    private CustomItemManager customItemManager;

    @Override
    public void enable() {

        config = configure(new LocalConfiguration(this));
        registerEvents(new PlayerListener(this));
        registerCommands(Commands.class);
        // the attachments need to load before the custom items because we use them in there
        loadAttachments();
        loadCustomItems();
        loadConfiguredCustomItems();
        // load the crafting manager after init of the custom items
        craftingManager = new CraftingManager(this);
        // register action api stuff
        ActionAPI.register(this)
                .global()
                .trigger(new InventoryTrigger(this))
                .trigger(new CustomItemTrigger());

        Chat.registerAutoCompletionProvider(this, new ItemsAutoCompletionProvider());

        RDS.registerObject(new FilteredItemsTable.Factory());

        Conversations.registerConversationVariable(Pattern.compile("\\[([\\w-_\\.]+)]"), (matcher, conversation) -> {
            String group = matcher.group(1);
            return RaidCraft.getItem(group).map(CustomItemUtil::getFormattedItemTooltip).map(FancyMessage::toJSONString).orElse(matcher.group(0));
        });

        Quests.registerQuestLoader(new ConfigLoader(this, "item") {
            @Override
            public void loadConfig(String id, ConfigurationSection config) {
                registerNamedCustomItem(id, config);
            }

            @Override
            public String replaceReference(String key) {

                return RaidCraft.getItem(key)
                        .filter(itemStack -> itemStack instanceof CustomItemStack)
                        .map(itemStack -> ((CustomItemStack) itemStack).getItem().getName())
                        .orElse(key);
            }
        });

        Quests.registerQuestLoader(new ConfigLoader(this, "items") {
            @Override
            public void loadConfig(String id, ConfigurationSection config) {
                registerCustomItemAlias(id, config);
            }

            @Override
            public String replaceReference(String key) {
                return RaidCraft.getItem(key)
                        .filter(itemStack -> itemStack instanceof CustomItemStack)
                        .map(itemStack -> ((CustomItemStack) itemStack).getItem().getName())
                        .orElse(key);
            }
        });
    }

    @Override
    public void disable() {

        // lets unload all of our custom items
        unloadRegisteredItems();
    }

    private void unloadRegisteredItems() {

        loadedCustomItems.forEach(getCustomItemManager()::unregisterCustomItem);
        loadedCustomItems.clear();
    }

    @Override
    public void reload() {

        config.reload();
        unloadRegisteredItems();
        // the attachments need to load before the custom items because we use them in there
        loadAttachments();
        loadCustomItems();
        loadConfiguredCustomItems();
        // load the crafting manager after init of the custom items
        craftingManager.reload();
    }

    public CustomItemManager getCustomItemManager() {

        if (customItemManager == null) {
            customItemManager = RaidCraft.getComponent(CustomItemManager.class);
        }
        return customItemManager;
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
            AttachmentConfig config = configure(new AttachmentConfig(this, file));
            loadedAttachments.put(config.getName(), config);
            info("Loaded item attachment config: " + config.getName());
        }
    }

    private void loadConfiguredCustomItems() {
        loadedConfigCustomItems.clear();
        File dir = new File(getDataFolder(), getConfig().customItemConfigDir);
        dir.mkdirs();
        loadConfiguredCustomItems(dir);
    }

    // TODO: rework custom item handling of configs
    private void loadConfiguredCustomItems(File dir) {

        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                loadConfiguredCustomItems(file);
            }
            if (!file.getName().endsWith(".yml")) {
                continue;
            }
            CommonConfig config = new CommonConfig(this, file);
            config.load();
            registerNamedCustomItem(file.getName().replace(".yml", ""), config);
        }
    }

    private void registerNamedCustomItem(String id, ConfigurationSection config) {
        if (config.isSet("id")) {
            registerCustomItemAlias(config.getInt("id"), id);
            return;
        }
        try {
            CustomItem customItem = new NamedYAMLCustomItem(config.getString("name", id), config);
            RaidCraft.getComponent(ItemsPlugin.class).getCustomItemManager().registerNamedCustomItem(id, customItem);
            loadedConfigCustomItems.add(id);
            getLogger().info("Loaded custom config item: " + id + " (" + customItem.getName() + ")");
        } catch (CustomItemException e) {
            // ignore
        }
    }

    private void registerCustomItemAlias(int id, String alias) {

        try {
            getCustomItemManager().registerCustomItemAlias(id, alias);
            loadedConfigCustomItems.add(alias);
            getLogger().info("Loaded custom item alias " + alias + " for item with id " + id);
        } catch (DuplicateCustomItemException e) {
            // ignore
        }
    }

    private void registerCustomItemAlias(String path, ConfigurationSection config) {
        for (String key : config.getKeys(false)) {
            try {
                if (!config.isInt(key)) {
                    getLogger().warning("The item with the name " + config.get(key) + " ist not an INT");
                }
                int id = config.getInt(key);
                String alias = path + "." + key;
                getCustomItemManager().registerCustomItemAlias(id, alias);
                loadedConfigCustomItems.add(alias);
                getLogger().info("Loaded custom item alias " + alias + " for item with id " + id);
            } catch (DuplicateCustomItemException e) {
                // ignore
            }
        }
    }

    private void loadCustomItems() {

        int loaded = 0;
        int failed = 0;
        loadedCustomItems.clear();
        // lets load all custom items that are defined in the database
        Set<TCustomItem> customItems = getRcDatabase().find(TCustomItem.class).findSet();
        for (TCustomItem item : customItems) {
            Optional<CustomItem> itemOptional = loadCustomDatabaseItem(item);
            if (!itemOptional.isPresent()) {
                failed++;
                continue;
            }
            try {
                // register the actual custom item
                getCustomItemManager().registerCustomItem(itemOptional.get());
                loadedCustomItems.add(itemOptional.get().getId());
                loaded++;
            } catch (DuplicateCustomItemException e) {
                failed++;
                getLogger().warning(e.getMessage());
            }
        }
        getLogger().info("Loaded " + loaded + "/" + (loaded + failed) + " Custom Items...");
    }

    @Nullable
    private CustomItem createCustomItemFromType(TCustomItem item) {

        CustomItem customItem;
        TCustomEquipment equipment = getRcDatabase().find(TCustomEquipment.class).where().eq("item_id", item.getId()).findOne();
        switch (item.getItemType()) {
            case WEAPON:
                if (equipment == null) return null;
                TCustomWeapon weapon = getRcDatabase().find(TCustomWeapon.class).where().eq("equipment_id", equipment.getId()).findOne();
                if (weapon == null) return null;
                customItem = new ConfiguredWeapon(weapon);
                break;
            case ARMOR:
                if (equipment == null) return null;
                TCustomArmor armor = getRcDatabase().find(TCustomArmor.class).where().eq("equipment_id", equipment.getId()).findOne();
                if (armor == null) return null;
                customItem = new ConfiguredArmor(armor);
                // lets calculate the armor value if its an item
                if (((CustomArmor) customItem).getArmorValue() < 1) {
                    double armorModifier = ((CustomArmor) customItem).getArmorType().getArmorModifier(customItem.getQuality(), customItem.getItemLevel());
                    double armorValue = ((CustomArmor) customItem).getEquipmentSlot().getArmorSlotModifier() * armorModifier;
                    ((CustomArmor) customItem).setArmorValue((int) armorValue);
                    armor.setArmorValue((int) armorValue);
                    getRcDatabase().save(armor);
                }
                break;
            case USEABLE:
                customItem = new UseableItem(item);
                break;
            case EQUIPMENT:
                if (equipment == null) return null;
                customItem = new DatabaseEquipment(equipment);
                break;
            default:
                customItem = new SimpleItem(item, item.getItemType());
                break;
        }
        // lets calculate the item level
        if (customItem instanceof CustomEquipment && customItem.getItemLevel() < 1) {
            int itemLevel = calculateItemLevel((CustomEquipment) customItem);
            item.setItemLevel(itemLevel);
            getRcDatabase().save(item);
        }
        return customItem;
    }

    private Optional<CustomItem> loadCustomDatabaseItem(TCustomItem item) {

        if (Material.matchMaterial(item.getMinecraftItem()) == null) {
            getLogger().warning("Invalid Minecraft Item " + item.getMinecraftItem() + " in Custom Item: " + item.getName() + " (ID: " + item.getId() + ")");
            return Optional.empty();
        }

        CustomItem customItem = createCustomItemFromType(item);
        if (customItem == null) return Optional.empty();
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
                ArrayList<KeyValueMap> dataList = new ArrayList<>(attachment.getItemAttachmentDataList());
                configuredAttachment.merge(ConfigUtil.parseKeyValueTable(dataList));

                ((AttachableCustomItem) customItem).addAttachment(configuredAttachment);
            }
        }
        return Optional.of(customItem);
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
        tables.add(TItemCategory.class);
        // custom crafting
        tables.add(TCraftingRecipe.class);
        tables.add(TCraftingRecipeIngredient.class);
        return tables;
    }

    public ItemStack updateItemDurability(Player player, ItemStack item, double chance) {

        // lets check the durability loss and negate it by using our own durability if it is a custom item
        if (!CustomItemUtil.isEquipment(item)) {
            return item;
        }
        CustomItemStack customItem = RaidCraft.getCustomItem(item);
        // on each interact with the item the player has a chance of 0.1% chance to loose one durability point
        if (Math.random() < chance) {
            try {
                customItem.setCustomDurability(customItem.getCustomDurability() - 1);
                customItem.rebuild(player);
            } catch (CustomItemException e) {
                player.sendMessage(ChatColor.RED + e.getMessage());
            }
        } else {
            item.setDurability(CustomItemUtil.getMinecraftDurability(item, customItem.getCustomDurability(), customItem.getMaxDurability()));
            return item;
        }
        return customItem;
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
        @Setting("soulbound-item-warn-interval")
        public double soulboundItemPickupWarnInterval = 60;
        @Setting("configured-custom-items.path")
        @Comment("Where are you storing your custom item configs?")
        public String customItemConfigDir = "custom-items/";
        @Setting("configured-custom-items.start-id")
        @Comment("Config custom items will get a assigned a unique id from this pool.")
        public int customItemStartId = 900000;
        @Setting("configured-custom-items.end-id")
        public int customItemEndId = 999999;

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

        @Command(
                aliases = {"book"},
                desc = "Book Util Commands",
                min = 1
        )
        @NestedCommand(value = BookUtilCommands.class)
        public void book(CommandContext args, CommandSender sender) {

        }

        @Command(
                aliases = {"storage"},
                desc = "Storage Commands",
                min = 1
        )
        @NestedCommand(value = StorageCommands.class)
        public void storage(CommandContext args, CommandSender sender) {

        }

        @Command(
                aliases = {"lore"},
                desc = "Lore Commands",
                min = 1
        )
        @NestedCommand(value = LoreCommands.class)
        public void lore(CommandContext args, CommandSender sender) {

        }
    }
}
