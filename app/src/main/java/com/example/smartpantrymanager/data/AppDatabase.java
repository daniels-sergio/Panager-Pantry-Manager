package com.example.smartpantrymanager.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The single Room database for the app, named SmartPantryDatabase on disk.
 * Room persists this to a real SQLite file, so pantry data and recipes survive
 * the app being closed and reopened.
 *
 * All database work must happen off the main thread. Rather than pulling in
 * LiveData/ViewModel, this app uses a simple shared background executor
 * (databaseWriteExecutor) and Activities post their DAO calls to it directly -
 * easy to follow and easy to explain.
 */
@Database(entities = {PantryItem.class, Recipe.class, RecipeIngredient.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "SmartPantryDatabase";

    public abstract PantryDao pantryDao();

    public abstract RecipeDao recipeDao();

    private static volatile AppDatabase instance;

    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME)
                            .build();
                }
            }
        }
        return instance;
    }
}
