package com.example.smartpantrymanager.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PantryDao {

    @Insert
    long insertPantryItem(PantryItem item);

    @Query("SELECT * FROM pantry_items ORDER BY ingredient_name ASC")
    List<PantryItem> getAllPantryItems();

    @Query("SELECT * FROM pantry_items WHERE id = :id LIMIT 1")
    PantryItem getPantryItemById(int id);

    @Update
    void updatePantryItem(PantryItem item);

    @Delete
    void deletePantryItem(PantryItem item);

    @Query("DELETE FROM pantry_items")
    void deleteAllPantryItems();
}
