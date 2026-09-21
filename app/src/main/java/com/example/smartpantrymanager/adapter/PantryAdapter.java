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
 * Displays the user's pantry items in a RecyclerView. Each row shows the
 * ingredient's name, quantity/unit, and an expiry badge colour-coded using
 * ExpiryHelper, plus Edit and Delete buttons.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

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

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = pantryItems.get(position);

        holder.name.setText(item.getIngredientName());
        holder.quantity.setText(String.format(Locale.getDefault(), "Quantity: %s %s",
                formatQuantity(item.getQuantity()), item.getUnit()));

        holder.expiry.setText(ExpiryHelper.formatForDisplay(item.getExpiryDate()));
        switch (ExpiryHelper.getStatus(item.getExpiryDate())) {
            case EXPIRED:
                holder.expiry.setBackgroundResource(R.drawable.bg_badge_expired);
                holder.expiry.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.off_white));
                break;
            case EXPIRING_SOON:
                holder.expiry.setBackgroundResource(R.drawable.bg_badge_soon);
                // The "soon" badge is bright yellow - dark text reads far better on it
                // than the light text used on the red/blue badges.
                holder.expiry.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_on_accent));
                break;
            default:
                holder.expiry.setBackgroundResource(R.drawable.bg_badge_normal);
                holder.expiry.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.off_white));
                break;
        }

        holder.editButton.setOnClickListener(v -> listener.onEdit(item));
        holder.deleteButton.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return pantryItems.size();
    }

    private static String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(quantity);
    }

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
