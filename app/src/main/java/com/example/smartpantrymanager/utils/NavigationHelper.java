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
 * Wires up the shared bottom navigation bar (Home / Pantry / Recipes / Settings)
 * so every top-level screen navigates the same way. Each Activity calls
 * setup() once in onCreate() and passes which tab represents itself.
 *
 * The menu and its colours are set up here in Java (instead of XML app:menu /
 * app:itemIconTint attributes) so the bar never depends on a custom AndroidX
 * XML attribute resolving correctly.
 */
public final class NavigationHelper {

    private NavigationHelper() {
    }

    public static void setup(Activity activity, BottomNavigationView bottomNav, int selectedItemId) {
        bottomNav.inflateMenu(R.menu.bottom_nav_menu);

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

        bottomNav.setSelectedItemId(selectedItemId);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedItemId) {
                return true; // already on this screen
            }
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
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
            }
            return true;
        });
    }
}
