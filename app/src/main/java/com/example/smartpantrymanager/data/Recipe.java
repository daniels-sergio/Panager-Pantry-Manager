package com.example.smartpantrymanager.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * One recipe, stored as a row in the recipes table.
 *
 * The recipe's ingredient list isn't stored here. Each ingredient is its own row
 * in a separate table (see RecipeIngredient), and each of those rows holds this
 * recipe's ID so the app knows which recipe it belongs to. Keeping them apart
 * means a recipe can have any number of ingredients.
 *
 * The methods below the constructor are getters and setters, which let the rest
 * of the app (and Room) read and change each field.
 */
@Entity(tableName = "recipes")
public class Recipe {

    // Unique number for each recipe, picked by the database.
    @PrimaryKey(autoGenerate = true)
    private int id;

    // The recipe's title, e.g. "Tomato Omelette".
    @NonNull
    private String name;

    // A one-line summary shown under the title in the recipe list.
    @NonNull
    private String description;

    // All the cooking steps in one piece of text, with each step on its own line.
    // The detail screen splits it at the line breaks to number the steps.
    @NonNull
    private String instructions;

    public Recipe(@NonNull String name, @NonNull String description, @NonNull String instructions) {
        this.name = name;
        this.description = description;
        this.instructions = instructions;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    public void setDescription(@NonNull String description) {
        this.description = description;
    }

    @NonNull
    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(@NonNull String instructions) {
        this.instructions = instructions;
    }
}
