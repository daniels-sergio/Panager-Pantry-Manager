package com.example.smartpantrymanager.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RecipeDao {

    @Insert
    long insertRecipe(Recipe recipe);

    @Insert
    void insertRecipeIngredient(RecipeIngredient ingredient);

    @Query("SELECT * FROM recipes ORDER BY name ASC")
    List<Recipe> getAllRecipes();

    @Query("SELECT * FROM recipes WHERE id = :id LIMIT 1")
    Recipe getRecipeById(int id);

    @Query("SELECT * FROM recipe_ingredients WHERE recipe_id = :recipeId")
    List<RecipeIngredient> getIngredientsForRecipe(int recipeId);

    /** Used on first launch to decide whether the 20 starter recipes still need to be seeded. */
    @Query("SELECT COUNT(*) FROM recipes")
    int getRecipeCount();
}
