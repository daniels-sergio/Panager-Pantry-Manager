package com.example.smartpantrymanager;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.smartpantrymanager.data.AppDatabase;
import com.example.smartpantrymanager.utils.DatabaseSeeder;
import com.example.smartpantrymanager.utils.SettingsManager;

/**
 * Runs once when the app process starts. Applies the user's persisted dark
 * mode preference before any screen is created (so there's no light-to-dark
 * flash), and kicks off the one-time recipe seeding on a background thread so
 * the 20 starter recipes exist before the user opens the Suggested Recipes
 * screen, without ever blocking the UI thread.
 */
public class PanagerApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        boolean darkModeEnabled = new SettingsManager(this).isDarkModeEnabled();
        AppCompatDelegate.setDefaultNightMode(
                darkModeEnabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        AppDatabase db = AppDatabase.getInstance(this);
        AppDatabase.databaseWriteExecutor.execute(() -> DatabaseSeeder.seedIfEmpty(db.recipeDao()));
    }
}
