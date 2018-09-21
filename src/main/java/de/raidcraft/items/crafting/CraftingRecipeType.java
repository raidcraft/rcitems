package de.raidcraft.items.crafting;

import de.raidcraft.util.EnumUtils;
import io.ebean.annotation.EnumValue;

/**
 * @author Silthus
 */
public enum CraftingRecipeType {

    @EnumValue("SHAPED")
    SHAPED,
    @EnumValue("SHAPELESS")
    SHAPELESS,
    @EnumValue("FURNACE")
    FURNACE,
    @EnumValue("ANVIL")
    ANVIL;

    public static CraftingRecipeType fromString(String str) {

        return EnumUtils.getEnumFromString(CraftingRecipeType.class, str);
    }
}
