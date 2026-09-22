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
 * Shows only the recipes the user can make right now, i.e. those that pass
 * IngredientMatcher.canMakeRecipe() for every one of their required
 * ingredients. This is where the app's strict matching business rule is
 * actually applied to real pantry + recipe data.
 */
public class SuggestedRecipesActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {

    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";

    private RecipeDao recipeDao;
    private RecipeAdapter adapter;
    private final List<Recipe> suggestedRecipes = new ArrayList<>();
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

        adapter = new RecipeAdapter(suggestedRecipes, ingredientCounts, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.button_add_ingredient).setOnClickListener(v ->
                startActivity(new Intent(this, AddEditIngredientActivity.class)));

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        NavigationHelper.setup(this, bottomNav, R.id.nav_recipes);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestedRecipes();
    }

    private void loadSuggestedRecipes() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<PantryItem> pantryItems = AppDatabase.getInstance(this).pantryDao().getAllPantryItems();
            List<Recipe> allRecipes = recipeDao.getAllRecipes();

            List<Recipe> matches = new ArrayList<>();
            Map<Integer, Integer> counts = new HashMap<>();
            for (Recipe recipe : allRecipes) {
                List<RecipeIngredient> required = recipeDao.getIngredientsForRecipe(recipe.getId());
                counts.put(recipe.getId(), required.size());
                if (IngredientMatcher.canMakeRecipe(required, pantryItems)) {
                    matches.add(recipe);
                }
            }

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

    private void updateEmptyState() {
        boolean isEmpty = suggestedRecipes.isEmpty();
        emptyStateView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onViewRecipe(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
