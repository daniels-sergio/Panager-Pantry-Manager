package com.example.smartpantrymanager;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.smartpantrymanager.data.AppDatabase;
import com.example.smartpantrymanager.utils.DatabaseSeeder;
import com.example.smartpantrymanager.utils.SettingsManager;

/**
 * The very first piece of the app that runs, before any screen appears.
 *
 * It does two jobs:
 *  1. Checks whether the user turned dark mode on last time and applies it now,
 *     so the first screen doesn't flash white and then switch to dark.
 *  2. Makes sure the 20 starter recipes are in the database. This happens in the
 *     background so the app doesn't freeze while it works.
 *
 * Android knows to run this class because it's named in AndroidManifest.xml.
 */
public class PanagerApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Read the saved dark mode choice and tell Android which colour scheme to use
        // for every screen from now on.
        boolean darkModeEnabled = new SettingsManager(this).isDarkModeEnabled();
        AppCompatDelegate.setDefaultNightMode(
                darkModeEnabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        // Open the database and, on a background worker, add the starter recipes.
        // seedIfEmpty only adds them if the recipe table is still empty, so this is
        // safe to run on every launch.
        AppDatabase db = AppDatabase.getInstance(this);
        AppDatabase.databaseWriteExecutor.execute(() -> DatabaseSeeder.seedIfEmpty(db.recipeDao()));
    }
}
