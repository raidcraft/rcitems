package de.raidcraft.items;

import de.raidcraft.api.items.CustomEquipment;
import de.raidcraft.api.items.EquipmentSlot;
import de.raidcraft.api.items.FixedMultilineTooltip;
import de.raidcraft.api.items.ItemAttribute;
import de.raidcraft.api.items.SingleLineTooltip;
import de.raidcraft.api.items.TooltipSlot;
import de.raidcraft.items.tables.items.TCustomEquipment;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Silthus
 */
public class BaseEquipment extends BaseItem implements CustomEquipment {

    private final EquipmentSlot equipmentSlot;
    private final int maxDurability;
    private final Set<ItemAttribute> attributes;

    public BaseEquipment(TCustomEquipment equipment) {

        super(equipment.getItem());
        this.equipmentSlot = equipment.getEquipmentSlot();
        this.maxDurability = equipment.getDurability();
        this.attributes = equipment.createAttributes();
        // always add a spacer because we have durability
        setTooltip(new SingleLineTooltip(TooltipSlot.SPACER, ""));
        // also add our attributes
        List<String> lines = new ArrayList<>();
        for (ItemAttribute attribute : attributes) {
            String str = ChatColor.GREEN + "+";
            if (attribute.getValue() < 0) {
                str = ChatColor.RED + "-";
            }
            str += attribute.getValue() + " " + attribute.getDisplayName();
            lines.add(str);
        }
        setTooltip(new FixedMultilineTooltip(TooltipSlot.ATTRIBUTES, lines.toArray(new String[lines.size()])));
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
