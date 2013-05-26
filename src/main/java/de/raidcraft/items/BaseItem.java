package de.raidcraft.items;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.CustomEquipment;
import de.raidcraft.api.items.CustomItem;
import de.raidcraft.api.items.CustomItemException;
import de.raidcraft.api.items.CustomItemStack;
import de.raidcraft.api.items.ItemQuality;
import de.raidcraft.api.requirement.Requirement;
import de.raidcraft.items.tables.TCustomItem;
import de.raidcraft.util.CustomItemUtil;
import de.raidcraft.util.Font;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Silthus
 */
public abstract class BaseItem implements CustomItem {

    public static final int DEFAULT_WIDTH = 150;
    public static final String LINE_SEPARATOR = "->";

    private final int id;
    private final String encodedId;
    private final int minecraftId;
    private final short minecraftData;
    private final String name;
    private final String lore;
    private final int itemLevel;
    private final ItemQuality quality;
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

    private List<String> getTooltipLines() {

        ArrayList<String> output = new ArrayList<>();
        int maxWidth = calculateMaxWidth();
        // we always add the first and last two lines, the rest is parsed by subclasses
        output.add(encodedId + getQuality().getColor() + (this instanceof CustomEquipment ? ChatColor.BOLD : "") + getName());
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
        // lets put one empty space between the main body and the rest
        output.add("");
        // now lets add the lore text
        if (getLore() != null && !getLore().equals("") && getLore().length() > 0) {
            int cWidth = 0;
            int tWidth = 0;
            StringBuilder out = new StringBuilder();
            StringBuilder temp = new StringBuilder();
            out.append(ChatColor.YELLOW);
            out.append(ChatColor.ITALIC);
            String currentColour = ChatColor.YELLOW.toString();
            String dMsg = "\"" + getLore() + "\"";
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
                        out.append(ChatColor.ITALIC);
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
            output.add(out.toString());
        }
        // and add the sell price last
        String priceString = CustomItemUtil.getSellPriceString(getSellPrice());
        if (priceString != null) {
            output.add(ChatColor.WHITE + "Verkaufspreis: " + priceString);
        }
        return output;
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

        List<String> lines = getTooltipLines();
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(lines.get(0));
        lines.remove(0);
        itemMeta.setLore(lines);
        itemStack.setItemMeta(itemMeta);
    }
}
