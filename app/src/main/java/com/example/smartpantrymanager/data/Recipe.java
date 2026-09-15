package com.example.smartpantrymanager.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * A recipe stored in the app database. The ingredients for a recipe are stored
 * separately in {@link RecipeIngredient}, linked by recipeId.
 */
@Entity(tableName = "recipes")
public class Recipe {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String name;

    @NonNull
    private String description;

    /** Numbered preparation steps, stored as one string with newline-separated steps. */
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
