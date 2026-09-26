package com.example.smartpantrymanager.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.data.AppDatabase;
import com.example.smartpantrymanager.data.Recipe;
import com.example.smartpantrymanager.data.RecipeDao;
import com.example.smartpantrymanager.data.RecipeIngredient;

import java.util.List;
import java.util.Locale;

/**
 * Shows one recipe in full: its name, description, the ingredient list with
 * amounts, and the cooking steps.
 *
 * The screen is opened with just the recipe's ID. It uses that ID to load the
 * recipe and its ingredients from the database.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    private RecipeDao recipeDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        recipeDao = AppDatabase.getInstance(this).recipeDao();
        // The back arrow at the top closes this screen.
        findViewById(R.id.button_back).setOnClickListener(v -> finish());

        // Read the recipe ID that was passed in. -1 means none was sent, which
        // shouldn't happen, but if it does we close rather than show a blank page.
        int recipeId = getIntent().getIntExtra(SuggestedRecipesActivity.EXTRA_RECIPE_ID, -1);
        if (recipeId == -1) {
            Toast.makeText(this, "Recipe not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadRecipe(recipeId);
    }

    /**
     * Loads the recipe and its ingredient list in the background, then passes
     * them to displayRecipe() to fill in the screen.
     */
    private void loadRecipe(int recipeId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Recipe recipe = recipeDao.getRecipeById(recipeId);
            if (recipe == null) {
                // No recipe with that ID: tell the user and close the screen.
                runOnUiThread(() -> {
                    Toast.makeText(this, "This recipe no longer exists.", Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }
            List<RecipeIngredient> ingredients = recipeDao.getIngredientsForRecipe(recipeId);
            runOnUiThread(() -> displayRecipe(recipe, ingredients));
        });
    }

    /**
     * Puts the recipe on screen. The title, description and steps go into text
     * boxes that already exist in the layout. The ingredient list is built here in
     * code, one line per ingredient, because the number of lines changes from
     * recipe to recipe.
     */
    private void displayRecipe(Recipe recipe, List<RecipeIngredient> ingredients) {
        ((TextView) findViewById(R.id.text_recipe_name)).setText(recipe.getName());
        ((TextView) findViewById(R.id.text_recipe_description)).setText(recipe.getDescription());
        ((TextView) findViewById(R.id.text_recipe_instructions)).setText(recipe.getInstructions());

        // The empty box in the layout that the ingredient lines go into.
        // Clear it first in case this runs more than once.
        LinearLayout container = findViewById(R.id.layout_ingredients_container);
        container.removeAllViews();

        // If a recipe somehow has no ingredients, say so instead of leaving a gap.
        if (ingredients.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No ingredients listed for this recipe.");
            empty.setTextColor(getColor(R.color.text_secondary));
            container.addView(empty);
            return;
        }

        // Make one line of text per ingredient, e.g. "• 500 g beef mince".
        for (RecipeIngredient ingredient : ingredients) {
            TextView line = new TextView(this);
            line.setText(String.format(Locale.getDefault(), "• %s %s %s",
                    formatQuantity(ingredient.getRequiredQuantity()), ingredient.getUnit(), ingredient.getIngredientName()));
            line.setTextColor(getColor(R.color.text_primary));
            line.setTextSize(15);
            // Stretch the line across the full width, as tall as its text, with a
            // small gap above each line.
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.topMargin = 6;
            line.setLayoutParams(params);
            container.addView(line);
        }
    }

    // Shows whole numbers without a decimal point: 2.0 becomes "2", but 1.5
    // stays "1.5".
    private static String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(quantity);
    }
}
