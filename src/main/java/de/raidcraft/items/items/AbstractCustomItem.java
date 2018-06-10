package de.raidcraft.items.items;

import com.google.common.base.MoreObjects;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.*;
import de.raidcraft.api.items.attachments.*;
import de.raidcraft.api.items.tooltip.*;
import de.raidcraft.api.requirement.Requirement;
import de.raidcraft.items.ItemsPlugin;
import de.raidcraft.util.CustomItemUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * @author mdoering
 */
@Getter
@Setter
public abstract class AbstractCustomItem implements CustomItem, AttachableCustomItem {

    private final int id;
    private final String encodedId;
    private final String name;
    protected Material minecraftItem;
    @Deprecated
    protected int minecraftId;
    private ItemType type;
    protected short minecraftDataValue;
    protected String lore = "";
    protected ItemQuality quality = ItemQuality.COMMON;
    protected int maxStackSize = 1;
    protected double sellPrice = 0;
    protected boolean blockingUsage = false;
    protected boolean lootable = true;
    protected boolean enchantmentEffect = false;
    protected ItemBindType bindType = ItemBindType.NONE;
    private final Map<TooltipSlot, Tooltip> tooltips = new EnumMap<>(TooltipSlot.class);
    private final List<Requirement<Player>> requirements = new ArrayList<>();
    private final Set<ItemCategory> categories = new HashSet<>();
    protected final Map<String, ConfiguredAttachment> attachments = new HashMap<>();
    protected int itemLevel = 1;

    public AbstractCustomItem(int id, String name, ItemType type) {

        this.id = id;
        this.encodedId = CustomItemUtil.encodeItemId(id);
        this.name = name;
        this.type = type;
    }

    @Deprecated
    protected void setMinecraftId(int id) {

        this.minecraftId = id;
        setMinecraftItem(Material.getMaterial(id));
    }

    protected void setMinecraftItem(Material minecraftItem) {

        if (minecraftItem == null) return;
        this.minecraftItem = minecraftItem;
        this.minecraftId = minecraftItem.getId();
    }

    protected void buildTooltips() {

        setTooltip(new NameTooltip(getId(), getName(), getQuality().getColor()));
        if (getItemLevel() > 0 && !RaidCraft.getComponent(ItemsPlugin.class).getConfig().hideItemLevel) {
            setTooltip(new SingleLineTooltip(TooltipSlot.ITEM_LEVEL, "Gegenstandsstufe " + getItemLevel(), ChatColor.GOLD));
        }
        if (getSellPrice() > 0) {
            setTooltip(new SingleLineTooltip(TooltipSlot.SELL_PRICE, "Verkaufspreis: " + CustomItemUtil.getSellPriceString(getSellPrice()), ChatColor.WHITE));
        }
        if (getLore() != null && !getLore().equals("")) {
            setTooltip(new VariableMultilineTooltip(TooltipSlot.LORE, getLore(), true, true, ChatColor.GOLD));
        }
        if (getBindType() != null && getBindType() != ItemBindType.NONE) {
            setTooltip(new BindTooltip(getBindType(), null));
        }
        updateAttachmentTooltip();
    }

    private void updateAttachmentTooltip() {

        for (ConfiguredAttachment attachment : attachments.values()) {
            if (attachment.getDescription() != null && !attachment.getDescription().equals("")) {
                setTooltip(new VariableMultilineTooltip(TooltipSlot.ATTACHMENT, attachment.getDescription(), false, false, attachment.getColor()));
            }
        }
    }

    @Override
    public void addCategory(ItemCategory category) {

        this.categories.add(category);
    }

    @Override
    public final boolean matches(ItemStack itemStack) {

        try {
            return getId() == CustomItemUtil.decodeItemId(itemStack.getItemMeta());
        } catch (CustomItemException ignored) {
        }
        return false;
    }

    @Override
    public Player getObject() {

        throw new UnsupportedOperationException();
    }

    @Override
    public List<Requirement<Player>> getRequirements() {

        return requirements;
    }

    @Override
    public Tooltip getTooltip(TooltipSlot slot) {

        return tooltips.get(slot);
    }

    @Override
    public boolean isMeetingAllRequirements(Player object) {

        try {
            for (Requirement<Player> requirement : requirements) {
                if (!requirement.isMet(object)) {
                    return false;
                }
            }
            for (ItemAttachment attachment : getAttachments(object)) {
                if (attachment instanceof RequiredItemAttachment) {
                    if (!((RequiredItemAttachment) attachment).isRequirementMet(object)) {
                        return false;
                    }
                }
            }
        } catch (ItemAttachmentException ignored) {
        }
        return true;
    }

    @Override
    public String getResolveReason(Player object) {

        try {
            for (Requirement<Player> requirement : requirements) {
                if (!requirement.isMet(object)) {
                    return requirement.getLongReason();
                }
            }
            for (ItemAttachment attachment : getAttachments(object)) {
                if (attachment instanceof RequiredItemAttachment) {
                    if (!((RequiredItemAttachment) attachment).isRequirementMet(object)) {
                        return ((RequiredItemAttachment) attachment).getErrorMessage();
                    }
                }
            }
        } catch (ItemAttachmentException ignored) {
        }
        return "All requirements are met!";
    }

    protected void setTooltip(Tooltip tooltip) {

        this.tooltips.put(tooltip.getSlot(), tooltip);
    }

    @Override
    public final CustomItemStack createNewItem() {

        CustomItemStack customItemStack = RaidCraft.getCustomItem(this);
        customItemStack.rebuild();
        return customItemStack;
    }

    @Override
    public void addAttachment(ConfiguredAttachment attachment) {

        attachments.put(de.raidcraft.util.StringUtils.formatName(attachment.getAttachmentName()), attachment);
        updateAttachmentTooltip();
    }

    @Override
    public List<ItemAttachment> getAttachments(Player player) throws ItemAttachmentException {

        List<ItemAttachment> itemAttachments = new ArrayList<>();
        for (ConfiguredAttachment config : attachments.values()) {
            ItemAttachment attachment = RaidCraft.getComponent(ItemAttachmentManager.class)
                    .getItemAttachment(config.getProvider(), config.getAttachmentName(), player);
            itemAttachments.add(attachment);
            attachment.loadAttachment(config);
        }
        return itemAttachments;
    }

    @Override
    public void apply(Player player, CustomItemStack itemStack, boolean loadOnly) throws CustomItemException {

        for (ConfiguredAttachment config : attachments.values()) {
            ItemAttachment attachment = RaidCraft.getComponent(ItemAttachmentManager.class)
                    .getItemAttachment(config.getProvider(), config.getAttachmentName(), player);
            attachment.loadAttachment(config);
            if (!loadOnly) {
                attachment.applyAttachment(player);
                // check if the attachment is an requirement
                if (attachment instanceof RequiredItemAttachment) {
                    if (!((RequiredItemAttachment) attachment).isRequirementMet(player)) {
                        String errorMessage = ((RequiredItemAttachment) attachment).getErrorMessage();
                        if (errorMessage == null) errorMessage = config.getDescription();
                        throw new CustomItemException(errorMessage);
                    }
                }
            }
        }
    }

    @Override
    public void remove(Player player, CustomItemStack itemStack) throws CustomItemException {

        for (ConfiguredAttachment config : attachments.values()) {
            ItemAttachment attachment = RaidCraft.getComponent(ItemAttachmentManager.class)
                    .getItemAttachment(config.getProvider(), config.getAttachmentName(), player);
            attachment.removeAttachment(player);
        }
    }

    @Override
    public String toString() {

        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("minecraftItem", minecraftItem)
                .add("type", type)
                .add("minecraftDataValue", minecraftDataValue)
                .add("lore", lore)
                .add("quality", quality)
                .add("maxStackSize", maxStackSize)
                .add("sellPrice", sellPrice)
                .add("blockingUsage", blockingUsage)
                .add("bindType", bindType)
                .add("itemLevel", itemLevel)
                .toString();
    }
}
