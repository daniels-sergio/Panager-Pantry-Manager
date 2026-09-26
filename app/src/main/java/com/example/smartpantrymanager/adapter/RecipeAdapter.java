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
 * Turns the list of suggested recipes into cards on the Recipes screen.
 * It works the same way as PantryAdapter: the scrolling list asks it how many
 * cards there are, to make an empty card, and to fill a card with a recipe.
 *
 * The recipes it receives have already been checked, so every one of them can
 * be made. This class only displays them.
 *
 * Each card shows the ingredient count ("5 ingredients"). Rather than asking the
 * database while drawing, the screen passes in a ready-made lookup of recipe ID
 * to ingredient count.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    // What the screen has to provide: a method to run when a recipe is tapped.
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

    // Build one empty card from res/layout/item_recipe.xml.
    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    // Fill a card with the recipe at this position in the list.
    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.name.setText(recipe.getName());
        holder.description.setText(recipe.getDescription());

        // Look up the ingredient count, using 0 if it's missing. Say "ingredient"
        // for exactly one and "ingredients" otherwise.
        int count = ingredientCounts.getOrDefault(recipe.getId(), 0);
        holder.ingredientCount.setText(String.format(Locale.getDefault(), "%d ingredient%s", count, count == 1 ? "" : "s"));

        // Both the View button and a tap anywhere on the card open the recipe.
        holder.viewButton.setOnClickListener(v -> listener.onViewRecipe(recipe));
        holder.itemView.setOnClickListener(v -> listener.onViewRecipe(recipe));
    }

    // How many cards the list should have.
    @Override
    public int getItemCount() {
        return recipes.size();
    }

    /**
     * Holds onto the parts of one card so they only have to be found in the
     * layout once, not every time the card is reused while scrolling.
     */
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
