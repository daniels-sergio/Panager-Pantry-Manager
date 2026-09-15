package com.example.smartpantrymanager.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * A single ingredient the user currently has at home, e.g. "4 pieces of Tomatoes,
 * expiring 2026-09-15". expiryDate is stored as an ISO-8601 "yyyy-MM-dd" string
 * (or null if the user did not set one) so it sorts and compares as plain text.
 */
@Entity(tableName = "pantry_items")
public class PantryItem {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "ingredient_name")
    private String ingredientName;

    private double quantity;

    @NonNull
    private String unit;

    @ColumnInfo(name = "expiry_date")
    private String expiryDate; // nullable, format yyyy-MM-dd

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
