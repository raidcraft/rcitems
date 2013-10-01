package de.raidcraft.items.crafting.recipes;

import com.avaje.ebean.EbeanServer;
import de.raidcraft.RaidCraft;
import de.raidcraft.items.ItemsPlugin;
import de.raidcraft.items.tables.crafting.TCraftingRecipe;

/**
 * @author Silthus
 */
public abstract class AbstractCustomRecipe implements CustomRecipe {

    private final int id;
    private final String name;

    public AbstractCustomRecipe(int id, String name) {

        this.id = id;
        this.name = name;
    }

    public int getId() {

        return id;
    }

    @Override
    public String getName() {

        return name;
    }

    public void remove() {

        EbeanServer database = RaidCraft.getDatabase(ItemsPlugin.class);
        TCraftingRecipe recipe = database.find(TCraftingRecipe.class, getId());
        if (recipe != null) {
            database.delete(recipe);
        }
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof AbstractCustomRecipe)) return false;

        AbstractCustomRecipe that = (AbstractCustomRecipe) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {

        return id;
    }

    @Override
    public String toString() {

        return getName();
    }
}
