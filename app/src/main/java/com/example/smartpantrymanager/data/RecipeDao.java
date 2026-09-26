package com.example.smartpantrymanager.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * Every way the app reads or adds recipes and their ingredient lists.
 *
 * Like PantryDao, these are just method names with instructions attached; Room
 * writes the real code when the app is built. There's no update or delete here
 * because the user can't edit recipes, only cook them.
 */
@Dao
public interface RecipeDao {

    // Saves a recipe and gives back its new ID. The seeder needs that ID so it can
    // attach the recipe's ingredients to it.
    @Insert
    long insertRecipe(Recipe recipe);

    // Saves one line of a recipe's ingredient list, e.g. "2 pieces of tomato".
    @Insert
    void insertRecipeIngredient(RecipeIngredient ingredient);

    // Gets every recipe, sorted A to Z by name.
    @Query("SELECT * FROM recipes ORDER BY name ASC")
    List<Recipe> getAllRecipes();

    // Gets one recipe by its ID, or null if there isn't one. Used by the detail screen.
    @Query("SELECT * FROM recipes WHERE id = :id LIMIT 1")
    Recipe getRecipeById(int id);

    // Gets the ingredient list for one recipe.
    @Query("SELECT * FROM recipe_ingredients WHERE recipe_id = :recipeId")
    List<RecipeIngredient> getIngredientsForRecipe(int recipeId);

    // Counts the recipes. On launch, a count of 0 means the starter recipes
    // haven't been added yet.
    @Query("SELECT COUNT(*) FROM recipes")
    int getRecipeCount();
}
