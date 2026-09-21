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
 * Shows every ingredient currently in the pantry (READ) and lets the user
 * add (CREATE, via AddEditIngredientActivity), edit (UPDATE) and delete
 * ingredients. All database work runs on AppDatabase.databaseWriteExecutor
 * and results are posted back to the UI thread with runOnUiThread.
 */
public class PantryActivity extends AppCompatActivity implements PantryAdapter.OnPantryItemActionListener {

    public static final String EXTRA_PANTRY_ITEM_ID = "extra_pantry_item_id";

    private PantryDao pantryDao;
    private PantryAdapter adapter;
    private final List<PantryItem> pantryItems = new ArrayList<>();

    private RecyclerView recyclerView;
    private View emptyStateView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);

        pantryDao = AppDatabase.getInstance(this).pantryDao();

        recyclerView = findViewById(R.id.recycler_pantry);
        emptyStateView = findViewById(R.id.layout_empty_state);

        adapter = new PantryAdapter(pantryItems, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        ImageButton fabAdd = findViewById(R.id.fab_add_ingredient);
        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditIngredientActivity.class)));

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        NavigationHelper.setup(this, bottomNav, R.id.nav_pantry);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPantryItems(); // refresh every time the screen becomes visible (after add/edit/delete)
    }

    private void loadPantryItems() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<PantryItem> items = pantryDao.getAllPantryItems();
            runOnUiThread(() -> {
                pantryItems.clear();
                pantryItems.addAll(items);
                adapter.notifyDataSetChanged();
                updateEmptyState();
            });
        });
    }

    private void updateEmptyState() {
        boolean isEmpty = pantryItems.isEmpty();
        emptyStateView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onEdit(PantryItem item) {
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(EXTRA_PANTRY_ITEM_ID, item.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete this ingredient?")
                .setMessage(item.getIngredientName() + " will be removed from your pantry.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        pantryDao.deletePantryItem(item);
                        runOnUiThread(this::loadPantryItems);
                    });
                })
                .show();
    }
}
