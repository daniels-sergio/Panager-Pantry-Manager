package com.example.smartpantrymanager.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * One line from a recipe's ingredient list, for example "2 pieces of tomato".
 * Each one is a row in the recipe_ingredients table.
 *
 * recipeId says which recipe this line belongs to. A recipe with five ingredients
 * has five of these rows, all with the same recipeId.
 *
 * The methods below the constructor are getters and setters, which let the rest
 * of the app (and Room) read and change each field.
 */
@Entity(tableName = "recipe_ingredients")
public class RecipeIngredient {

    // Unique number for this row, picked by the database.
    @PrimaryKey(autoGenerate = true)
    private int id;

    // The ID of the recipe this ingredient belongs to.
    @ColumnInfo(name = "recipe_id")
    private int recipeId;

    // What the recipe needs, e.g. "tomato".
    @NonNull
    @ColumnInfo(name = "ingredient_name")
    private String ingredientName;

    // How much the recipe needs. The pantry must have at least this much.
    @ColumnInfo(name = "required_quantity")
    private double requiredQuantity;

    // What the amount is measured in, e.g. "g" or "pieces".
    @NonNull
    private String unit;

    public RecipeIngredient(int recipeId, @NonNull String ingredientName, double requiredQuantity, @NonNull String unit) {
        this.recipeId = recipeId;
        this.ingredientName = ingredientName;
        this.requiredQuantity = requiredQuantity;
        this.unit = unit;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }

    @NonNull
    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(@NonNull String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public double getRequiredQuantity() {
        return requiredQuantity;
    }

    public void setRequiredQuantity(double requiredQuantity) {
        this.requiredQuantity = requiredQuantity;
    }

    @NonNull
    public String getUnit() {
        return unit;
    }

    public void setUnit(@NonNull String unit) {
        this.unit = unit;
    }
}
