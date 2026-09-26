package com.example.smartpantrymanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.adapter.RecipeAdapter;
import com.example.smartpantrymanager.data.AppDatabase;
import com.example.smartpantrymanager.data.PantryItem;
import com.example.smartpantrymanager.data.Recipe;
import com.example.smartpantrymanager.data.RecipeDao;
import com.example.smartpantrymanager.data.RecipeIngredient;
import com.example.smartpantrymanager.utils.IngredientMatcher;
import com.example.smartpantrymanager.utils.NavigationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Recipes screen. Lists only the recipes the user can cook right now with
 * what's in their pantry.
 *
 * Every time the screen opens, it goes through all the stored recipes and asks
 * IngredientMatcher whether the pantry has everything each one needs. Only the
 * recipes that pass are shown. If none pass, a message appears with a button to
 * add more ingredients.
 */
public class SuggestedRecipesActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {

    // The label used when passing a recipe's ID to the detail screen.
    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";

    private RecipeDao recipeDao;
    private RecipeAdapter adapter;
    // The recipes that passed the check and are shown in the list.
    private final List<Recipe> suggestedRecipes = new ArrayList<>();
    // How many ingredients each recipe has, looked up by recipe ID, for the
    // "5 ingredients" label on each card.
    private final Map<Integer, Integer> ingredientCounts = new HashMap<>();

    private RecyclerView recyclerView;
    private View emptyStateView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        recipeDao = AppDatabase.getInstance(this).recipeDao();

        recyclerView = findViewById(R.id.recycler_recipes);
        emptyStateView = findViewById(R.id.layout_empty_state);

        // Hook up the scrolling list. The adapter builds one card per recipe.
        adapter = new RecipeAdapter(suggestedRecipes, ingredientCounts, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // The button on the "no recipes yet" message opens the add ingredient form.
        findViewById(R.id.button_add_ingredient).setOnClickListener(v ->
                startActivity(new Intent(this, AddEditIngredientActivity.class)));

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        NavigationHelper.setup(this, bottomNav, R.id.nav_recipes);
    }

    // Recheck every time the screen comes into view, so a recipe appears as soon
    // as its last missing ingredient is added.
    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestedRecipes();
    }

    /**
     * Checks every recipe against the current pantry and keeps the ones that can
     * be made. The work happens in the background and the finished list is handed
     * to the screen afterwards.
     */
    private void loadSuggestedRecipes() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<PantryItem> pantryItems = AppDatabase.getInstance(this).pantryDao().getAllPantryItems();
            List<Recipe> allRecipes = recipeDao.getAllRecipes();

            // Build the results in fresh lists first, so the list on screen is
            // never half-updated while this runs.
            List<Recipe> matches = new ArrayList<>();
            Map<Integer, Integer> counts = new HashMap<>();
            for (Recipe recipe : allRecipes) {
                List<RecipeIngredient> required = recipeDao.getIngredientsForRecipe(recipe.getId());
                counts.put(recipe.getId(), required.size());
                // The key check: keep the recipe only if every ingredient is covered.
                if (IngredientMatcher.canMakeRecipe(required, pantryItems)) {
                    matches.add(recipe);
                }
            }

            // Back on the main thread: swap the new results in and redraw.
            runOnUiThread(() -> {
                suggestedRecipes.clear();
                suggestedRecipes.addAll(matches);
                ingredientCounts.clear();
                ingredientCounts.putAll(counts);
                adapter.notifyDataSetChanged();
                updateEmptyState();
            });
        });
    }

    // Show the "nothing you can make yet" message when the list is empty,
    // otherwise show the list.
    private void updateEmptyState() {
        boolean isEmpty = suggestedRecipes.isEmpty();
        emptyStateView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    /**
     * Called when a recipe card is tapped. Opens the detail screen and passes
     * only the recipe's ID; the detail screen loads the rest itself.
     */
    @Override
    public void onViewRecipe(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
