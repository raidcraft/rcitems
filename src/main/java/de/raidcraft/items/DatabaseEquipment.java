package de.raidcraft.items;

import de.raidcraft.api.items.CustomEquipment;
import de.raidcraft.api.items.EquipmentSlot;
import de.raidcraft.api.items.ItemAttribute;
import de.raidcraft.api.items.ItemType;
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
public class DatabaseEquipment extends DatabaseItem implements CustomEquipment {

    private final EquipmentSlot equipmentSlot;
    private final int maxDurability;
    private final Set<ItemAttribute> attributes;

    public DatabaseEquipment(TCustomEquipment equipment, ItemType type) {

        super(equipment.getItem(), type);
        this.equipmentSlot = equipment.getEquipmentSlot();
        this.maxDurability = equipment.getDurability();
        this.attributes = equipment.createAttributes();
        // always add a spacer because we have durability
        setTooltip(new SingleLineTooltip(TooltipSlot.SPACER, ""));
        // also add our attributes
        setTooltip(new AttributeTooltip(attributes));
    }

    public DatabaseEquipment(TCustomEquipment equipment) {

        this(equipment, ItemType.EQUIPMENT);
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
    public boolean hasAttributes() {

        return attributes != null && !attributes.isEmpty();
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
