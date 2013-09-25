package de.raidcraft.items;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.CustomItem;
import de.raidcraft.api.items.CustomItemException;
import de.raidcraft.api.items.CustomItemStack;
import de.raidcraft.api.items.ItemQuality;
import de.raidcraft.api.items.tooltip.NameTooltip;
import de.raidcraft.api.items.tooltip.VariableMultilineTooltip;
import de.raidcraft.api.items.tooltip.SingleLineTooltip;
import de.raidcraft.api.items.tooltip.Tooltip;
import de.raidcraft.api.items.tooltip.TooltipSlot;
import de.raidcraft.api.items.attachments.AttachableCustomItem;
import de.raidcraft.api.items.attachments.ConfiguredAttachment;
import de.raidcraft.api.items.attachments.ItemAttachment;
import de.raidcraft.api.items.attachments.ItemAttachmentException;
import de.raidcraft.api.items.attachments.ItemAttachmentManager;
import de.raidcraft.api.items.attachments.RequiredItemAttachment;
import de.raidcraft.api.requirement.Requirement;
import de.raidcraft.items.tables.items.TCustomItem;
import de.raidcraft.util.CustomItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Silthus
 */
public abstract class BaseItem implements CustomItem, AttachableCustomItem {

    protected final Map<String, ConfiguredAttachment> attachments = new HashMap<>();

    private final int id;
    private final String encodedId;
    private final int minecraftId;
    private final short minecraftData;
    private final String name;
    private final String lore;
    private final int itemLevel;
    private final ItemQuality quality;
    private final int maxStackSize;
    private final double sellPrice;
    private final boolean dropable;
    private final List<Requirement<Player>> requirements = new ArrayList<>();
    private final Map<TooltipSlot, Tooltip> tooltips = new EnumMap<>(TooltipSlot.class);

    public BaseItem(TCustomItem item) {

        this.id = item.getId();
        this.encodedId = CustomItemUtil.encodeItemId(id);
        this.minecraftId = item.getMinecraftId();
        this.minecraftData = (short) item.getMinecraftDataValue();
        this.name = item.getName();
        this.lore = item.getLore();
        this.itemLevel = item.getItemLevel();
        this.quality = item.getQuality();
        this.maxStackSize = item.getMaxStackSize();
        this.sellPrice = item.getSellPrice();
        this.dropable = item.isDropable();

        buildTooltips();
    }

    private void buildTooltips() {

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
    public int getId() {

        return id;
    }

    public String getEncodedId() {

        return encodedId;
    }

    @Override
    public int getMinecraftId() {

        return minecraftId;
    }

    @Override
    public short getMinecraftDataValue() {

        return minecraftData;
    }

    @Override
    public String getName() {

        return name;
    }

    @Override
    public String getLore() {

        return lore;
    }

    @Override
    public int getItemLevel() {

        return itemLevel > 0 ? itemLevel : 1;
    }

    @Override
    public ItemQuality getQuality() {

        return quality;
    }

    @Override
    public int getMaxStackSize() {

        return maxStackSize;
    }

    @Override
    public double getSellPrice() {

        return sellPrice;
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
    public boolean isMeetingAllRequirements(Player object) {

        for (Requirement<Player> requirement : requirements) {
            if (!requirement.isMet(object)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getResolveReason(Player object) {

        for (Requirement<Player> requirement : requirements) {
            if (!requirement.isMet(object)) {
                return requirement.getLongReason();
            }
        }
        return "All requirements are met!";
    }

    protected void setTooltip(Tooltip tooltip) {

        this.tooltips.put(tooltip.getSlot(), tooltip);
    }

    @Override
    public Tooltip getTooltip(TooltipSlot slot) {

        return tooltips.get(slot);
    }

    @Override
    public Map<TooltipSlot, Tooltip> getTooltips() {

        return tooltips;
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
        }
        return itemAttachments;
    }

    @Override
    public void apply(Player player, CustomItemStack itemStack) throws CustomItemException {

        for (ConfiguredAttachment config : attachments.values()) {
            ItemAttachment attachment = RaidCraft.getComponent(ItemAttachmentManager.class)
                    .getItemAttachment(config.getProvider(), config.getAttachmentName(), player);
            attachment.applyAttachment(itemStack, player, config);
            // check if the attachment is an requirement
            if (attachment instanceof RequiredItemAttachment) {
                if (!((RequiredItemAttachment) attachment).isRequirementMet(player)) {
                    String errorMessage = ((RequiredItemAttachment) attachment).getErrorMessage(player);
                    if (errorMessage == null) errorMessage = config.getDescription();
                    throw new CustomItemException(errorMessage);
                }
            }
        }
    }

    @Override
    public void remove(Player player, CustomItemStack itemStack) throws CustomItemException {

        for (ConfiguredAttachment config : attachments.values()) {
            ItemAttachment attachment = RaidCraft.getComponent(ItemAttachmentManager.class)
                    .getItemAttachment(config.getProvider(), config.getAttachmentName(), player);
            attachment.removeAttachment(itemStack, player, config);
        }
    }

    @Override
    public String toString() {

        return getName();
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof BaseItem)) return false;

        BaseItem baseItem = (BaseItem) o;

        return id == baseItem.id;
    }

    @Override
    public int hashCode() {

        return id;
    }
}
