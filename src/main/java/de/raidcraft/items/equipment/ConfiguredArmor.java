package de.raidcraft.items.equipment;

import de.raidcraft.api.items.ArmorType;
import de.raidcraft.api.items.CustomArmor;
import de.raidcraft.api.items.ItemType;
import de.raidcraft.api.items.tooltip.EquipmentTypeTooltip;
import de.raidcraft.api.items.tooltip.SingleLineTooltip;
import de.raidcraft.api.items.tooltip.Tooltip;
import de.raidcraft.api.items.tooltip.TooltipSlot;
import de.raidcraft.items.items.DatabaseEquipment;
import de.raidcraft.items.tables.items.TCustomArmor;

/**
 * @author Silthus
 */
public class ConfiguredArmor extends DatabaseEquipment implements CustomArmor {

    private final ArmorType armorType;
    private int armorValue;

    public ConfiguredArmor(TCustomArmor armor) {

        super(armor.getEquipment(), ItemType.ARMOR);
        this.armorType = armor.getArmorType();
        this.armorValue = armor.getArmorValue();
        setTooltip(new EquipmentTypeTooltip(getEquipmentSlot().getGermanName() + Tooltip.LINE_SEPARATOR + getArmorType().getGermanName()));
        setTooltip(new SingleLineTooltip(TooltipSlot.ARMOR, getArmorValue() + " Rüstung"));
    }

    @Override
    public ArmorType getArmorType() {

        return armorType;
    }

    @Override
    public void setArmorValue(int armorValue) {

        this.armorValue = armorValue;
    }

    @Override
    public int getArmorValue() {

        return armorValue;
    }
}
