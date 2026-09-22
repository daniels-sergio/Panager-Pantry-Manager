package com.example.smartpantrymanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.data.Recipe;

import java.util.List;
import java.util.Map;
import java.util.Locale;

/**
 * Displays recipes (already filtered by IngredientMatcher) in a RecyclerView.
 * ingredientCounts maps recipeId -> number of ingredients, so each card can show
 * "N ingredients" without the adapter needing direct database access.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    public interface OnRecipeClickListener {
        void onViewRecipe(Recipe recipe);
    }

    private final List<Recipe> recipes;
    private final Map<Integer, Integer> ingredientCounts;
    private final OnRecipeClickListener listener;

    public RecipeAdapter(List<Recipe> recipes, Map<Integer, Integer> ingredientCounts, OnRecipeClickListener listener) {
        this.recipes = recipes;
        this.ingredientCounts = ingredientCounts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.name.setText(recipe.getName());
        holder.description.setText(recipe.getDescription());

        int count = ingredientCounts.getOrDefault(recipe.getId(), 0);
        holder.ingredientCount.setText(String.format(Locale.getDefault(), "%d ingredient%s", count, count == 1 ? "" : "s"));

        holder.viewButton.setOnClickListener(v -> listener.onViewRecipe(recipe));
        holder.itemView.setOnClickListener(v -> listener.onViewRecipe(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView description;
        final TextView ingredientCount;
        final Button viewButton;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_recipe_name);
            description = itemView.findViewById(R.id.text_recipe_description);
            ingredientCount = itemView.findViewById(R.id.text_ingredient_count);
            viewButton = itemView.findViewById(R.id.button_view_recipe);
        }
    }
}
