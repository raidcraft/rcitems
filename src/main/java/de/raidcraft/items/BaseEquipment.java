package de.raidcraft.items;

import de.raidcraft.api.items.CustomEquipment;
import de.raidcraft.api.items.EquipmentSlot;
import de.raidcraft.api.items.ItemAttribute;
import de.raidcraft.items.tables.TCustomEquipment;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Silthus
 */
public abstract class BaseEquipment extends BaseItem implements CustomEquipment {

    public static final Pattern DURABILITY_PATTERN = Pattern.compile("^Haltbarkeit: ([0-9]+)/([0-9]+)$");

    private final EquipmentSlot equipmentSlot;
    private final int maxDurability;
    private final Set<ItemAttribute> attributes;

    public BaseEquipment(TCustomEquipment equipment) {

        super(equipment.getItem());
        this.equipmentSlot = equipment.getEquipmentSlot();
        this.maxDurability = equipment.getDurability();
        this.attributes = equipment.createAttributes();
    }

    @Override
    public int parseDurability(ItemStack itemStack) {

        if (itemStack.getItemMeta().hasLore()) {
            Matcher matcher;
            for (String line : itemStack.getItemMeta().getLore()) {
                matcher = DURABILITY_PATTERN.matcher(ChatColor.stripColor(line));
                if (matcher.matches()) {
                    return Integer.parseInt(matcher.group(1));
                }
            }
        }
        return getMaxDurability();
    }

    @Override
    public void updateDurability(ItemStack itemStack, int durability) {

        // define the state of the item via color
        ChatColor color = ChatColor.GRAY;
        double durabilityInPercent = (double) durability / (double) getMaxDurability();
        if (durabilityInPercent < 0.10) {
            color = ChatColor.RED;
        } else if (durabilityInPercent < 0.20) {
            color = ChatColor.YELLOW;
        }
        // also set the minecraft items durability
        // minecrafts max durability is when the item is completly broken so we need to invert our durability
        double mcDurabilityPercent = 1.0 - durabilityInPercent;
        // always set -1 so we dont break the item
        short mcDurability = (short) ((itemStack.getType().getMaxDurability() * mcDurabilityPercent) - 1);
        itemStack.setDurability(mcDurability);

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta.hasLore()) {
            Matcher matcher;
            List<String> lore = itemMeta.getLore();
            for (int i = 0; i < lore.size(); i++) {
                matcher = DURABILITY_PATTERN.matcher(ChatColor.stripColor(lore.get(i)));
                if (matcher.matches()) {
                    lore.set(i, color + "Haltbarkeit: " + durability + "/" + getMaxDurability());
                    itemMeta.setLore(lore);
                    itemStack.setItemMeta(itemMeta);
                    return;
                }
            }
            // if no line with the durablity was found set it in the last slot
            lore.add(color + "Haltbarkeit: " + durability + "/" + getMaxDurability());
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);
        }
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {

        return equipmentSlot;
    }

    @Override
    public int getMaxDurability() {

        return maxDurability;
    }

    @Override
    public Set<ItemAttribute> getAttributes() {

        return attributes;
    }

    protected List<ItemAttribute> getSortedAttributes() {

        ArrayList<ItemAttribute> list = new ArrayList<>(attributes);
        Collections.sort(list);
        return list;
    }
}
