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
 * Shows the full detail of one recipe: name, description, every required
 * ingredient with its quantity, and the preparation method. Only the
 * recipe's id is received through the Intent (see EXTRA_RECIPE_ID); the
 * actual data is loaded from Room.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    private RecipeDao recipeDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        recipeDao = AppDatabase.getInstance(this).recipeDao();
        findViewById(R.id.button_back).setOnClickListener(v -> finish());

        int recipeId = getIntent().getIntExtra(SuggestedRecipesActivity.EXTRA_RECIPE_ID, -1);
        if (recipeId == -1) {
            Toast.makeText(this, "Recipe not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadRecipe(recipeId);
    }

    private void loadRecipe(int recipeId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Recipe recipe = recipeDao.getRecipeById(recipeId);
            if (recipe == null) {
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

    private void displayRecipe(Recipe recipe, List<RecipeIngredient> ingredients) {
        ((TextView) findViewById(R.id.text_recipe_name)).setText(recipe.getName());
        ((TextView) findViewById(R.id.text_recipe_description)).setText(recipe.getDescription());
        ((TextView) findViewById(R.id.text_recipe_instructions)).setText(recipe.getInstructions());

        LinearLayout container = findViewById(R.id.layout_ingredients_container);
        container.removeAllViews();

        if (ingredients.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No ingredients listed for this recipe.");
            empty.setTextColor(getColor(R.color.text_secondary));
            container.addView(empty);
            return;
        }

        for (RecipeIngredient ingredient : ingredients) {
            TextView line = new TextView(this);
            line.setText(String.format(Locale.getDefault(), "• %s %s %s",
                    formatQuantity(ingredient.getRequiredQuantity()), ingredient.getUnit(), ingredient.getIngredientName()));
            line.setTextColor(getColor(R.color.text_primary));
            line.setTextSize(15);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.topMargin = 6;
            line.setLayoutParams(params);
            container.addView(line);
        }
    }

    private static String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(quantity);
    }
}
