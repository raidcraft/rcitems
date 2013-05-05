package de.raidcraft.items;

import de.raidcraft.api.items.Attribute;
import de.raidcraft.api.items.CustomEquipment;
import de.raidcraft.api.items.EquipmentSlot;
import de.raidcraft.items.tables.TCustomEquipment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Silthus
 */
public abstract class BaseEquipment extends BaseItem implements CustomEquipment {

    private final EquipmentSlot equipmentSlot;
    private final int durability;
    private final Set<Attribute> attributes;

    public BaseEquipment(TCustomEquipment equipment) {

        super(equipment.getItem());
        this.equipmentSlot = equipment.getEquipmentSlot();
        this.durability = equipment.getDurability();
        this.attributes = equipment.createAttributes();
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {

        return equipmentSlot;
    }

    @Override
    public int getDurability() {

        return durability;
    }

    @Override
    public Set<Attribute> getAttributes() {

        return attributes;
    }

    protected List<Attribute> getSortedAttributes() {

        ArrayList<Attribute> list = new ArrayList<>(attributes);
        Collections.sort(list);
        return list;
    }
}
