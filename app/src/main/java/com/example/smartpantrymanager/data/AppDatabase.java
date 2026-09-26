package com.example.smartpantrymanager.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The app's database, saved on the phone as a file called SmartPantryDatabase.
 * Because it's a real file, the pantry and recipes are still there after the app
 * is closed and opened again.
 *
 * The line starting with @Database lists the three tables (pantry items, recipes,
 * and recipe ingredients). Room, the library that manages the database, reads that
 * line and builds the tables for us.
 *
 * Reading from or writing to the database can take a moment, and Android doesn't
 * allow that on the thread that draws the screen (the app would stutter or freeze).
 * So every screen hands its database work to databaseWriteExecutor, a small group
 * of background workers defined below.
 */
@Database(entities = {PantryItem.class, Recipe.class, RecipeIngredient.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // The name of the database file on the phone.
    private static final String DATABASE_NAME = "SmartPantryDatabase";

    // These give the rest of the app access to the pantry and recipe queries.
    // Room writes the actual code behind them when the app is built.
    public abstract PantryDao pantryDao();

    public abstract RecipeDao recipeDao();

    // The one shared copy of the database. "volatile" makes sure every background
    // worker sees the same value as soon as it's set.
    private static volatile AppDatabase instance;

    // Four background workers that take turns running database jobs.
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    /**
     * Returns the shared database, creating it the first time it's asked for.
     *
     * Opening a database is slow, so the app only ever opens one and reuses it.
     * The null check happens twice: the first check skips the locked section in the
     * normal case where the database already exists, and the second check (inside
     * the lock) stops two workers from both creating one if they arrive at the
     * same moment.
     */
    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    // getApplicationContext() ties the database to the whole app
                    // rather than to one screen, so closing a screen doesn't break it.
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
