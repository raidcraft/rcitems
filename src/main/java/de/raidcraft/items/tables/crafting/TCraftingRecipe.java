package de.raidcraft.items.tables.crafting;

import com.avaje.ebean.validation.NotNull;
import de.raidcraft.items.crafting.CraftingRecipeType;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

/**
 * @author Silthus
 */
@Entity
@Table(name = "rcitems_crafting_recipes")
public class TCraftingRecipe {

    @Id
    private int id;
    @NotNull
    @Column(unique = true)
    private String name;
    private String permission;
    @NotNull
    private String result;
    private int amount;
    private String shape;
    @NotNull
    private CraftingRecipeType type;
    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "recipe_id")
    private List<TCraftingRecipeIngredient> ingredients;

    public int getId() {

        return id;
    }

    public void setId(int id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getPermission() {

        return permission;
    }

    public void setPermission(String permission) {

        this.permission = permission;
    }

    public String getResult() {

        return result;
    }

    public void setResult(String result) {

        this.result = result;
    }

    public int getAmount() {

        return amount;
    }

    public void setAmount(int amount) {

        this.amount = amount;
    }

    public String getShape() {

        return shape;
    }

    public void setShape(String shape) {

        this.shape = shape;
    }

    public CraftingRecipeType getType() {

        return type;
    }

    public void setType(CraftingRecipeType type) {

        this.type = type;
    }

    public List<TCraftingRecipeIngredient> getIngredients() {

        return ingredients;
    }

    public void setIngredients(List<TCraftingRecipeIngredient> ingredients) {

        this.ingredients = ingredients;
    }
}
