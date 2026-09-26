package com.example.smartpantrymanager.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;

import androidx.core.content.ContextCompat;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.ui.MainActivity;
import com.example.smartpantrymanager.ui.PantryActivity;
import com.example.smartpantrymanager.ui.SettingsActivity;
import com.example.smartpantrymanager.ui.SuggestedRecipesActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Sets up the bar along the bottom of the screen with the Home, Pantry, Recipes
 * and Settings tabs.
 *
 * Four screens show this bar, and they should all behave the same way, so the
 * setup lives here once instead of being copied into each screen. Each screen
 * calls setup() when it opens and says which tab belongs to it, so that tab is
 * highlighted.
 *
 * The tabs and their colours are set here in code rather than in the XML layout
 * files. Setting them in XML relies on extra attributes that don't always load
 * correctly, and doing it in code avoids that.
 */
public final class NavigationHelper {

    // Nobody needs to create a NavigationHelper; it only holds the setup method.
    private NavigationHelper() {
    }

    public static void setup(Activity activity, BottomNavigationView bottomNav, int selectedItemId) {
        // Load the four tabs from res/menu/bottom_nav_menu.xml.
        bottomNav.inflateMenu(R.menu.bottom_nav_menu);

        // Colour rules for the tabs: red for the tab you're on, grey for the rest.
        // "states" lists the two situations (selected, not selected) and "colors"
        // gives the colour for each, in the same order.
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        };
        int[] colors = new int[]{
                ContextCompat.getColor(activity, R.color.primary_red),
                ContextCompat.getColor(activity, R.color.text_secondary)
        };
        ColorStateList itemColors = new ColorStateList(states, colors);
        bottomNav.setItemIconTintList(itemColors);
        bottomNav.setItemTextColor(itemColors);

        // Highlight the tab for the screen we're on.
        bottomNav.setSelectedItemId(selectedItemId);

        // What happens when a tab is tapped.
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedItemId) {
                return true; // tapped the tab we're already on, so do nothing
            }

            // Work out which screen the tapped tab leads to.
            Class<?> destination = null;
            if (id == R.id.nav_home) {
                destination = MainActivity.class;
            } else if (id == R.id.nav_pantry) {
                destination = PantryActivity.class;
            } else if (id == R.id.nav_recipes) {
                destination = SuggestedRecipesActivity.class;
            } else if (id == R.id.nav_settings) {
                destination = SettingsActivity.class;
            }

            if (destination != null) {
                Intent intent = new Intent(activity, destination);
                // If that screen is already open further back, go back to it
                // instead of opening a second copy. Without this, hopping between
                // tabs would pile up screens and the Back button would walk
                // through all of them.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
                // Switch instantly with no slide animation, so it feels like
                // changing tabs rather than opening a new page.
                activity.overridePendingTransition(0, 0);
            }
            return true; // tells Android the tap was handled
        });
    }
}
