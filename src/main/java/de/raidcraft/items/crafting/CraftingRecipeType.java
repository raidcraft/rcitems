package de.raidcraft.items.crafting;

import com.avaje.ebean.annotation.EnumValue;

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
    ANVIL
}
