package de.raidcraft.items;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.CustomEquipment;
import de.raidcraft.api.items.CustomItem;
import de.raidcraft.api.items.CustomItemException;
import de.raidcraft.api.items.CustomItemStack;
import de.raidcraft.api.items.ItemQuality;
import de.raidcraft.api.items.attachments.AttachableCustomItem;
import de.raidcraft.api.items.attachments.ConfiguredAttachment;
import de.raidcraft.api.items.attachments.ItemAttachment;
import de.raidcraft.api.items.attachments.ItemAttachmentException;
import de.raidcraft.api.items.attachments.ItemAttachmentManager;
import de.raidcraft.api.requirement.Requirement;
import de.raidcraft.items.tables.items.TCustomItem;
import de.raidcraft.util.CustomItemUtil;
import de.raidcraft.util.Font;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Silthus
 */
public abstract class BaseItem implements CustomItem, AttachableCustomItem {

    public static final int DEFAULT_WIDTH = 150;
    public static final String LINE_SEPARATOR = "->";
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
    private final List<Requirement<Player>> requirements = new ArrayList<>();

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

        return itemLevel;
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
    public void rebuild(ItemStack itemStack) {

        setItemMeta(itemStack);
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

    protected abstract List<String> getCustomTooltipLines();

    private List<String> getTooltipLines(boolean broken) {

        ArrayList<String> output = new ArrayList<>();
        int maxWidth = calculateMaxWidth();
        // we always add the first and last two lines, the rest is parsed by subclasses
        output.add(encodedId + (broken ? ChatColor.DARK_RED : getQuality().getColor())
                + (this instanceof CustomEquipment ? ChatColor.BOLD : "") + getName());
        output.add(ChatColor.GOLD + "Gegenstandsstufe " + getItemLevel());
        // a "->" means we need to replace the line width the width
        for (String line : getCustomTooltipLines()) {
            if (line.contains(LINE_SEPARATOR)) {
                String[] split = line.split(LINE_SEPARATOR);
                String buffer = StringUtils.repeat(" ", (maxWidth - CustomItemUtil.getStringWidth(split[0] + split[1])) / 4);
                output.add(ChatColor.WHITE + split[0] + buffer + split[1]);
            } else {
                output.add(ChatColor.WHITE + line);
            }
        }
        // lets add the attachment information
        for (ConfiguredAttachment attachment : attachments.values()) {
            if (attachment.getDescription() != null && !attachment.getDescription().equals("")) {
                output.add(buildMultiLine(attachment.getDescription(), output, maxWidth, false, false, ChatColor.GREEN));
            }
        }
        // lets put one empty space between the main body and the rest
        output.add("");
        // now lets add the lore text
        if (getLore() != null && !getLore().equals("") && getLore().length() > 0) {
            output.add(buildMultiLine(getLore(), output, maxWidth, true, true, ChatColor.YELLOW));
        }
        // and add the sell price last
        if (getSellPrice() > 0.0) {
            output.add(ChatColor.WHITE + "Verkaufspreis: " + CustomItemUtil.getSellPriceString(getSellPrice()));
        }
        return output;
    }

    private String buildMultiLine(String original, List<String> output, int maxWidth, boolean quote, boolean italic, ChatColor color) {

        StringBuilder out = new StringBuilder();
        StringBuilder temp = new StringBuilder();
        out.append(color);
        if (italic) out.append(ChatColor.ITALIC);
        int cWidth = 0;
        int tWidth = 0;
        String currentColour = color.toString();
        String dMsg = quote ? "\"" + original + "\"" : original;
        for (int i = 0; i < dMsg.length(); i++) {
            char c = dMsg.charAt(i);
            temp.append(c);
            if (c == ChatColor.COLOR_CHAR || c == '&') {
                i += 1;
                temp.append(dMsg.charAt(i));
                currentColour = ChatColor.COLOR_CHAR + "" + dMsg.charAt(i);
                continue;
            }
            if (c == ' ')
                tWidth += 4;
            else
                tWidth += Font.WIDTHS[c] + 1;
            if (c == ' ' || i == dMsg.length() - 1) {
                if (cWidth + tWidth > maxWidth) {
                    cWidth = 0;
                    cWidth += tWidth;
                    tWidth = 0;
                    output.add(out.toString());
                    out = new StringBuilder();
                    out.append(currentColour);
                    if (italic) out.append(ChatColor.ITALIC);
                    out.append(temp);
                    temp = new StringBuilder();
                } else {
                    out.append(temp);
                    temp = new StringBuilder();
                    cWidth += tWidth;
                    tWidth = 0;
                }
            }
        }
        out.append(temp);
        return out.toString();
    }

    private int calculateMaxWidth() {

        // we need to keep track of the widest width in order to format the item nicely
        int width = CustomItemUtil.checkWidth(getName(), DEFAULT_WIDTH, true);
        width = CustomItemUtil.checkWidth("Gegenstandsstufe " + getItemLevel(), width);
        for (String str : getCustomTooltipLines()) {
            if (str.contains(LINE_SEPARATOR)) {
                str = str.replace(LINE_SEPARATOR, "     ");
            }
            width = CustomItemUtil.checkWidth(str, width);
        }
        // lore and sell price are not relevant because they are never above the width limit
        return width;
    }

    @Override
    public final CustomItemStack createNewItem() {

        ItemStack itemStack = new ItemStack(getMinecraftId(), 1, getMinecraftDataValue());
        setItemMeta(itemStack);
        return RaidCraft.getCustomItem(itemStack);
    }

    private void setItemMeta(ItemStack itemStack) {

        // lets first check if this item has durability and if yes set it later
        int durability = 0;
        if (this instanceof CustomEquipment) {
            durability = ((CustomEquipment) this).parseDurability(itemStack);
        }

        List<String> lines = getTooltipLines(durability < 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        int itemMetaKeyId = -1;
        // lets get the last line to preserve the unique key
        if (itemMeta.hasLore() && itemMeta.getLore().size() > 1) {
            try {
                itemMetaKeyId = CustomItemUtil.decodeItemId(itemMeta.getLore().get(itemMeta.getLore().size() - 1));
            } catch (CustomItemException ignored) {
            }
        }

        itemMeta.setDisplayName(lines.get(0));
        lines.remove(0);
        itemMeta.setLore(lines);
        itemStack.setItemMeta(itemMeta);
        // and at last update the durability
        if (this instanceof CustomEquipment) {
            CustomEquipment equipment = (CustomEquipment) this;
            equipment.updateDurability(itemStack, durability);
        }
        // lets add a last line in which we hide the item meta id
        if (itemMetaKeyId > 0) {
            List<String> strings = itemStack.getItemMeta().getLore();
            strings.add(CustomItemUtil.encodeItemId(itemMetaKeyId));
            itemStack.getItemMeta().setLore(strings);
        }
    }

    @Override
    public void addAttachment(ConfiguredAttachment attachment) {

        attachments.put(de.raidcraft.util.StringUtils.formatName(attachment.getAttachmentName()), attachment);
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
