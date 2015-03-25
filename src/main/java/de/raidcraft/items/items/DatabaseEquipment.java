package de.raidcraft.items.items;

import de.raidcraft.api.items.AttributeType;
import de.raidcraft.api.items.CustomEquipment;
import de.raidcraft.api.items.EquipmentSlot;
import de.raidcraft.api.items.ItemAttribute;
import de.raidcraft.api.items.ItemType;
import de.raidcraft.api.items.tooltip.AttributeTooltip;
import de.raidcraft.api.items.tooltip.SingleLineTooltip;
import de.raidcraft.api.items.tooltip.TooltipSlot;
import de.raidcraft.items.tables.items.TCustomEquipment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Silthus
 */
public class DatabaseEquipment extends DatabaseItem implements CustomEquipment {

    private final EquipmentSlot equipmentSlot;
    private final int maxDurability;
    private final Map<AttributeType, ItemAttribute> attributes = new HashMap<>();

    public DatabaseEquipment(TCustomEquipment equipment, ItemType type) {

        super(equipment.getItem(), type);
        this.equipmentSlot = equipment.getEquipmentSlot();
        this.maxDurability = equipment.getDurability();
        for (ItemAttribute attribute : equipment.createAttributes()) {
            attributes.put(attribute.getType(), attribute);
        }
        // always add a spacer because we have durability
        setTooltip(new SingleLineTooltip(TooltipSlot.SPACER, ""));
        // also add our attributes
        setTooltip(new AttributeTooltip(attributes.values()));
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
    public void addAttribute(ItemAttribute attribute) {

        attributes.put(attribute.getType(), attribute);
        setTooltip(new AttributeTooltip(attributes.values()));
    }

    @Override
    public ItemAttribute removeAttribute(AttributeType attribute) {

        ItemAttribute itemAttribute = attributes.remove(attribute);
        setTooltip(new AttributeTooltip(attributes.values()));
        return itemAttribute;
    }

    @Override
    public Collection<ItemAttribute> getAttributes() {

        return attributes.values();
    }

    protected List<ItemAttribute> getSortedAttributes() {

        ArrayList<ItemAttribute> list = new ArrayList<>(attributes.values());
        Collections.sort(list);
        return list;
    }
}
