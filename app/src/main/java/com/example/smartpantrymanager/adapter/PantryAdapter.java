package com.example.smartpantrymanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.data.PantryItem;
import com.example.smartpantrymanager.utils.ExpiryHelper;

import java.util.List;
import java.util.Locale;

/**
 * Turns the list of pantry items into rows on the Pantry screen.
 *
 * A scrolling list in Android (RecyclerView) doesn't know how to draw our data
 * by itself. It asks an adapter like this one three questions:
 *  - How many rows are there? (getItemCount)
 *  - Make me an empty row. (onCreateViewHolder)
 *  - Fill this row with item number X. (onBindViewHolder)
 *
 * The list only makes enough rows to fill the screen. As you scroll, rows that
 * move off screen are refilled with new items and reused, which keeps scrolling
 * smooth even with a long pantry.
 *
 * Each row shows the ingredient's name, how much there is, a coloured expiry
 * label, and edit and delete buttons.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    /**
     * What the screen using this adapter has to provide. The adapter only reports
     * that a button was tapped; the screen decides what happens (open the edit
     * form, show the delete confirmation).
     */
    public interface OnPantryItemActionListener {
        void onEdit(PantryItem item);

        void onDelete(PantryItem item);
    }

    private final List<PantryItem> pantryItems;
    private final OnPantryItemActionListener listener;

    public PantryAdapter(List<PantryItem> pantryItems, OnPantryItemActionListener listener) {
        this.pantryItems = pantryItems;
        this.listener = listener;
    }

    // Build one empty row from the layout in res/layout/item_pantry.xml.
    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    // Fill a row with the details of the item at this position in the list.
    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = pantryItems.get(position);

        holder.name.setText(item.getIngredientName());
        holder.quantity.setText(String.format(Locale.getDefault(), "Quantity: %s %s",
                formatQuantity(item.getQuantity()), item.getUnit()));

        // Expiry label: the text ("Expires: 15 September 2026") plus a background
        // colour based on how close the date is.
        holder.expiry.setText(ExpiryHelper.formatForDisplay(item.getExpiryDate()));
        switch (ExpiryHelper.getStatus(item.getExpiryDate())) {
            case EXPIRED:
                // Red label, light text.
                holder.expiry.setBackgroundResource(R.drawable.bg_badge_expired);
                holder.expiry.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.off_white));
                break;
            case EXPIRING_SOON:
                // Yellow label. Light text is hard to read on bright yellow, so
                // this one uses dark text.
                holder.expiry.setBackgroundResource(R.drawable.bg_badge_soon);
                holder.expiry.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_on_accent));
                break;
            default:
                // Blue label for items that are fine or have no date.
                holder.expiry.setBackgroundResource(R.drawable.bg_badge_normal);
                holder.expiry.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.off_white));
                break;
        }

        // Pass button taps on to the screen, along with which item was tapped.
        holder.editButton.setOnClickListener(v -> listener.onEdit(item));
        holder.deleteButton.setOnClickListener(v -> listener.onDelete(item));
    }

    // How many rows the list should have.
    @Override
    public int getItemCount() {
        return pantryItems.size();
    }

    // Shows whole numbers without a decimal point: 4.0 becomes "4", but 0.5
    // stays "0.5".
    private static String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(quantity);
    }

    /**
     * Holds onto the parts of one row (name, quantity, expiry label, buttons).
     * Finding them in the layout is slow, so it's done once when the row is made
     * and remembered here for every time the row is reused.
     */
    static class PantryViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView quantity;
        final TextView expiry;
        final ImageButton editButton;
        final ImageButton deleteButton;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_ingredient_name);
            quantity = itemView.findViewById(R.id.text_quantity);
            expiry = itemView.findViewById(R.id.text_expiry);
            editButton = itemView.findViewById(R.id.button_edit);
            deleteButton = itemView.findViewById(R.id.button_delete);
        }
    }
}
