package com.example.smartpantrymanager.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * One ingredient requirement belonging to a {@link Recipe}, e.g.
 * "recipeId=3, ingredientName=tomato, requiredQuantity=2, unit=pieces".
 */
@Entity(tableName = "recipe_ingredients")
public class RecipeIngredient {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "recipe_id")
    private int recipeId;

    @NonNull
    @ColumnInfo(name = "ingredient_name")
    private String ingredientName;

    @ColumnInfo(name = "required_quantity")
    private double requiredQuantity;

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
