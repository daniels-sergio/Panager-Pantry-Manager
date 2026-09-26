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
 * The Home screen, and the first thing the user sees.
 *
 * It shows three numbers (items in the pantry, recipes you can make right now,
 * and items expiring soon) and three shortcut cards that open other screens.
 *
 * In Android, each full screen is called an Activity. This one is set as the
 * app's starting screen in AndroidManifest.xml.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Runs once when the screen is first created. Sets up the layout and decides
     * what each button does. The numbers are filled in later, in onResume().
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Show the layout from res/layout/activity_main.xml.
        setContentView(R.layout.activity_main);

        // Each shortcut card opens a different screen when tapped. An Intent is
        // Android's way of saying "open this screen".
        findViewById(R.id.card_view_pantry).setOnClickListener(v ->
                startActivity(new Intent(this, PantryActivity.class)));
        findViewById(R.id.card_suggested_recipes).setOnClickListener(v ->
                startActivity(new Intent(this, SuggestedRecipesActivity.class)));
        // Opening the add/edit screen without an item ID puts it in "add new" mode.
        findViewById(R.id.card_add_ingredient).setOnClickListener(v ->
                startActivity(new Intent(this, AddEditIngredientActivity.class)));

        // Set up the tab bar at the bottom, with Home highlighted.
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        NavigationHelper.setup(this, bottomNav, R.id.nav_home);
    }

    /**
     * Runs every time the screen comes into view, including when the user comes
     * back from another screen. Reloading the numbers here keeps them correct
     * after the user adds or deletes something elsewhere.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadSummary();
    }

    /**
     * Works out the three numbers and shows them.
     *
     * The counting happens on a background worker because it reads the database.
     * Android only lets the main thread change what's on screen, so the final
     * step hands the results back with runOnUiThread().
     */
    private void loadSummary() {
        AppDatabase db = AppDatabase.getInstance(this);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<PantryItem> pantryItems = db.pantryDao().getAllPantryItems();
            List<Recipe> allRecipes = db.recipeDao().getAllRecipes();

            // Count pantry items whose expiry date is within the next 3 days.
            int expiringSoonCount = 0;
            for (PantryItem item : pantryItems) {
                if (ExpiryHelper.getStatus(item.getExpiryDate()) == ExpiryHelper.Status.EXPIRING_SOON) {
                    expiringSoonCount++;
                }
            }

            // Count recipes the pantry fully covers, using the same check as the
            // Suggested Recipes screen so the two numbers always agree.
            int makeableRecipeCount = 0;
            for (Recipe recipe : allRecipes) {
                List<RecipeIngredient> required = db.recipeDao().getIngredientsForRecipe(recipe.getId());
                if (IngredientMatcher.canMakeRecipe(required, pantryItems)) {
                    makeableRecipeCount++;
                }
            }

            // Java only lets the code inside runOnUiThread use variables that won't
            // change again, so the counts are copied into "final" versions first.
            int totalItems = pantryItems.size();
            int finalExpiringSoonCount = expiringSoonCount;
            int finalMakeableRecipeCount = makeableRecipeCount;

            // Back on the main thread: put the numbers on screen.
            runOnUiThread(() -> {
                ((TextView) findViewById(R.id.text_total_items)).setText(String.valueOf(totalItems));
                ((TextView) findViewById(R.id.text_recipes_available)).setText(String.valueOf(finalMakeableRecipeCount));
                ((TextView) findViewById(R.id.text_expiring_soon)).setText(String.valueOf(finalExpiringSoonCount));
            });
        });
    }
}
