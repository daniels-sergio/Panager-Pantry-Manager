package com.example.smartpantrymanager.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Every way the app reads or changes the pantry table.
 *
 * This is only a list of method names. The @Insert, @Query and so on above each
 * one tell Room what the method should do, and Room writes the working code when
 * the app is built. If one of the SQL queries has a typo, the build fails, which
 * catches mistakes early.
 *
 * None of these may be called from the screen-drawing thread; see AppDatabase.
 */
@Dao
public interface PantryDao {

    // Saves a new ingredient and gives back the ID the database assigned to it.
    @Insert
    long insertPantryItem(PantryItem item);

    // Gets every ingredient in the pantry, sorted A to Z by name.
    @Query("SELECT * FROM pantry_items ORDER BY ingredient_name ASC")
    List<PantryItem> getAllPantryItems();

    // Gets one ingredient by its ID, or null if nothing has that ID.
    // Used by the edit screen, which is only given the ID when it opens.
    @Query("SELECT * FROM pantry_items WHERE id = :id LIMIT 1")
    PantryItem getPantryItemById(int id);

    // Saves changes to an ingredient that already exists. Room finds the row by ID.
    @Update
    void updatePantryItem(PantryItem item);

    // Removes one ingredient. Room finds the row by ID.
    @Delete
    void deletePantryItem(PantryItem item);

    // Empties the whole pantry.
    @Query("DELETE FROM pantry_items")
    void deleteAllPantryItems();
}
