package com.example.smartpantrymanager.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * One ingredient the user has at home, for example "4 pieces of tomato, expiring
 * 2026-09-15". Each PantryItem is one row in the pantry_items table.
 *
 * The expiry date is kept as text in year-month-day order ("2026-09-15"). Written
 * that way, dates sort correctly even as plain text, and it can be left empty
 * (null) if the user didn't enter one.
 *
 * The rest of this class is getters and setters: small methods that let other
 * parts of the app read and change each field. Room also uses them to copy data
 * in and out of the database.
 */
@Entity(tableName = "pantry_items")
public class PantryItem {

    // A unique number for each row. The database picks it automatically (1, 2, 3...).
    @PrimaryKey(autoGenerate = true)
    private int id;

    // The ingredient's name as the user typed it, e.g. "Tomatoes".
    // @NonNull means this can never be empty. @ColumnInfo sets the column's name
    // in the table.
    @NonNull
    @ColumnInfo(name = "ingredient_name")
    private String ingredientName;

    // How much the user has. A decimal number so amounts like 0.5 kg work.
    private double quantity;

    // What the quantity is measured in, e.g. "g", "kg", "pieces".
    @NonNull
    private String unit;

    // Optional. Left as null when the user doesn't set an expiry date.
    @ColumnInfo(name = "expiry_date")
    private String expiryDate;

    // Builds a new pantry item. The ID isn't passed in because the database
    // assigns it when the item is saved.
    public PantryItem(@NonNull String ingredientName, double quantity, @NonNull String unit, String expiryDate) {
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(@NonNull String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    @NonNull
    public String getUnit() {
        return unit;
    }

    public void setUnit(@NonNull String unit) {
        this.unit = unit;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }
}
