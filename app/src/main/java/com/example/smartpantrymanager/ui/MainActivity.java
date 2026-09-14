package com.example.smartpantrymanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.data.AppDatabase;
import com.example.smartpantrymanager.data.PantryItem;
import com.example.smartpantrymanager.data.Recipe;
import com.example.smartpantrymanager.data.RecipeIngredient;
import com.example.smartpantrymanager.utils.ExpiryHelper;
import com.example.smartpantrymanager.utils.IngredientMatcher;
import com.example.smartpantrymanager.utils.NavigationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

/**
 * Home / Dashboard screen. Shows quick summary counts (total pantry items,
 * how many recipes can currently be made, items expiring soon) and shortcuts
 * to the other screens.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.card_view_pantry).setOnClickListener(v ->
                startActivity(new Intent(this, PantryActivity.class)));
        findViewById(R.id.card_suggested_recipes).setOnClickListener(v ->
                startActivity(new Intent(this, SuggestedRecipesActivity.class)));
        findViewById(R.id.card_add_ingredient).setOnClickListener(v ->
                startActivity(new Intent(this, AddEditIngredientActivity.class)));

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        NavigationHelper.setup(this, bottomNav, R.id.nav_home);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSummary();
    }

    private void loadSummary() {
        AppDatabase db = AppDatabase.getInstance(this);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<PantryItem> pantryItems = db.pantryDao().getAllPantryItems();
            List<Recipe> allRecipes = db.recipeDao().getAllRecipes();

            int expiringSoonCount = 0;
            for (PantryItem item : pantryItems) {
                if (ExpiryHelper.getStatus(item.getExpiryDate()) == ExpiryHelper.Status.EXPIRING_SOON) {
                    expiringSoonCount++;
                }
            }

            int makeableRecipeCount = 0;
            for (Recipe recipe : allRecipes) {
                List<RecipeIngredient> required = db.recipeDao().getIngredientsForRecipe(recipe.getId());
                if (IngredientMatcher.canMakeRecipe(required, pantryItems)) {
                    makeableRecipeCount++;
                }
            }

            int totalItems = pantryItems.size();
            int finalExpiringSoonCount = expiringSoonCount;
            int finalMakeableRecipeCount = makeableRecipeCount;
            runOnUiThread(() -> {
                ((TextView) findViewById(R.id.text_total_items)).setText(String.valueOf(totalItems));
                ((TextView) findViewById(R.id.text_recipes_available)).setText(String.valueOf(finalMakeableRecipeCount));
                ((TextView) findViewById(R.id.text_expiring_soon)).setText(String.valueOf(finalExpiringSoonCount));
            });
        });
    }
}
