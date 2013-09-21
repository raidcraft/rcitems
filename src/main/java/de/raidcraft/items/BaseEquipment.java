package de.raidcraft.items;

import de.raidcraft.api.items.CustomEquipment;
import de.raidcraft.api.items.EquipmentSlot;
import de.raidcraft.api.items.ItemAttribute;
import de.raidcraft.api.items.tooltip.AttributeTooltip;
import de.raidcraft.api.items.tooltip.SingleLineTooltip;
import de.raidcraft.api.items.tooltip.TooltipSlot;
import de.raidcraft.items.tables.items.TCustomEquipment;

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
        setTooltip(new AttributeTooltip(attributes));
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
