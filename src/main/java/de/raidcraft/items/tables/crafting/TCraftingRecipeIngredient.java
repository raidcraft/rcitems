package de.raidcraft.items.tables.crafting;

import com.avaje.ebean.validation.NotNull;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Silthus
 */
@Entity
@Table(name = "rc_crafting_recipe_slots")
public class TCraftingRecipeIngredient {

    @Id
    private int id;
    @NotNull
    @ManyToOne
    private TCraftingRecipe recipe;
    @NotNull
    private char slot;
    @NotNull
    private String item;
    private int amount;

    public int getId() {

        return id;
    }

    public void setId(int id) {

        this.id = id;
    }

    public TCraftingRecipe getRecipe() {

        return recipe;
    }

    public void setRecipe(TCraftingRecipe recipe) {

        this.recipe = recipe;
    }

    public char getSlot() {

        return slot;
    }

    public void setSlot(char slot) {

        this.slot = slot;
    }

    public String getItem() {

        return item;
    }

    public void setItem(String item) {

        this.item = item;
    }

    public int getAmount() {

        return amount;
    }

    public void setAmount(int amount) {

        this.amount = amount;
    }
}
