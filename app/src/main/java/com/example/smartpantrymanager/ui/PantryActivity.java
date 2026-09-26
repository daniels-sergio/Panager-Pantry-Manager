package com.example.smartpantrymanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.adapter.PantryAdapter;
import com.example.smartpantrymanager.data.AppDatabase;
import com.example.smartpantrymanager.data.PantryDao;
import com.example.smartpantrymanager.data.PantryItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.smartpantrymanager.utils.NavigationHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * The Pantry screen: a scrolling list of everything the user has at home.
 *
 * From here the user can add an ingredient (the round + button), edit one (the
 * pencil on a row) or delete one (the bin on a row). Adding and editing happen on
 * a separate screen, AddEditIngredientActivity.
 *
 * "implements PantryAdapter.OnPantryItemActionListener" means this screen promises
 * to provide onEdit() and onDelete(). The list rows call those methods when their
 * buttons are tapped, and this screen decides what happens next.
 */
public class PantryActivity extends AppCompatActivity implements PantryAdapter.OnPantryItemActionListener {

    // The label used when passing an item's ID to the edit screen. Both screens
    // use this same constant so the label always matches.
    public static final String EXTRA_PANTRY_ITEM_ID = "extra_pantry_item_id";

    private PantryDao pantryDao;
    private PantryAdapter adapter;
    // The items currently shown in the list. The adapter reads from this same list.
    private final List<PantryItem> pantryItems = new ArrayList<>();

    private RecyclerView recyclerView;
    // The "your pantry is empty" message, shown instead of an empty list.
    private View emptyStateView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);

        pantryDao = AppDatabase.getInstance(this).pantryDao();

        recyclerView = findViewById(R.id.recycler_pantry);
        emptyStateView = findViewById(R.id.layout_empty_state);

        // A RecyclerView is Android's scrolling list. The adapter turns each
        // PantryItem into a row on screen, and the layout manager stacks those
        // rows top to bottom.
        adapter = new PantryAdapter(pantryItems, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // The round + button opens the add screen (no ID given means "add new").
        ImageButton fabAdd = findViewById(R.id.fab_add_ingredient);
        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditIngredientActivity.class)));

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        NavigationHelper.setup(this, bottomNav, R.id.nav_pantry);
    }

    // Reload the list every time the screen comes back into view, so anything
    // added or edited on the other screen shows up straight away.
    @Override
    protected void onResume() {
        super.onResume();
        loadPantryItems();
    }

    /**
     * Reads every pantry item from the database in the background, then swaps the
     * new list into the screen on the main thread.
     */
    private void loadPantryItems() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<PantryItem> items = pantryDao.getAllPantryItems();
            runOnUiThread(() -> {
                pantryItems.clear();
                pantryItems.addAll(items);
                // Tell the list its data changed so it redraws the rows.
                adapter.notifyDataSetChanged();
                updateEmptyState();
            });
        });
    }

    // Show the "empty pantry" message when there's nothing to list, otherwise show
    // the list. GONE hides a view and gives its space back to the layout.
    private void updateEmptyState() {
        boolean isEmpty = pantryItems.isEmpty();
        emptyStateView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    /**
     * Called when the pencil on a row is tapped. Opens the edit screen and passes
     * along only the item's ID. The edit screen loads the rest from the database,
     * so it always works with the latest saved version.
     */
    @Override
    public void onEdit(PantryItem item) {
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(EXTRA_PANTRY_ITEM_ID, item.getId());
        startActivity(intent);
    }

    /**
     * Called when the bin on a row is tapped. Asks the user to confirm first, and
     * only deletes if they tap "Delete". Cancel just closes the pop-up.
     */
    @Override
    public void onDelete(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete this ingredient?")
                .setMessage(item.getIngredientName() + " will be removed from your pantry.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Delete in the background, then reload the list on screen.
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        pantryDao.deletePantryItem(item);
                        runOnUiThread(this::loadPantryItems);
                    });
                })
                .show();
    }
}
