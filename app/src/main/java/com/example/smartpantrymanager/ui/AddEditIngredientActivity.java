package com.example.smartpantrymanager.ui;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.data.AppDatabase;
import com.example.smartpantrymanager.data.PantryDao;
import com.example.smartpantrymanager.data.PantryItem;
import com.example.smartpantrymanager.utils.ExpiryHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

/**
 * One screen used for both CREATE and UPDATE of a pantry ingredient.
 * If EXTRA_PANTRY_ITEM_ID is present in the launching Intent, the screen loads
 * that existing item and edits it; otherwise it creates a brand new one.
 * Only the ingredient's id is ever passed through the Intent - the full
 * record is loaded from Room.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    private PantryDao pantryDao;
    private int editingItemId = -1; // -1 means "adding a new ingredient"
    private PantryItem editingItem;

    private TextInputLayout layoutName;
    private TextInputLayout layoutQuantity;
    private TextInputLayout layoutExpiryDate;
    private TextInputEditText inputName;
    private TextInputEditText inputQuantity;
    private TextInputEditText inputExpiryDate;
    private Spinner spinnerUnit;
    private String[] unitOptions;
    private String selectedExpiryDate; // ISO yyyy-MM-dd, may be null

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        pantryDao = AppDatabase.getInstance(this).pantryDao();

        TextView title = findViewById(R.id.text_screen_title);
        layoutName = findViewById(R.id.layout_ingredient_name);
        layoutQuantity = findViewById(R.id.layout_quantity);
        layoutExpiryDate = findViewById(R.id.layout_expiry_date);
        inputName = findViewById(R.id.input_ingredient_name);
        inputQuantity = findViewById(R.id.input_quantity);
        inputExpiryDate = findViewById(R.id.input_expiry_date);
        spinnerUnit = findViewById(R.id.spinner_unit);

        unitOptions = getResources().getStringArray(R.array.unit_options);
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, unitOptions);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);

        // Styled here in Java (box colour, hint colour, calendar end-icon) rather than
        // through TextInputLayout's app: XML attributes, so the field colours are still
        // fully under our control.
        int blue = ContextCompat.getColor(this, R.color.primary_blue);
        for (TextInputLayout layout : new TextInputLayout[]{layoutName, layoutQuantity, layoutExpiryDate}) {
            layout.setBoxStrokeColor(blue);
            layout.setDefaultHintTextColor(ColorStateList.valueOf(blue));
        }
        layoutExpiryDate.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
        layoutExpiryDate.setEndIconDrawable(R.drawable.ic_calendar);

        findViewById(R.id.button_back).setOnClickListener(v -> finish());
        findViewById(R.id.button_cancel).setOnClickListener(v -> finish());
        findViewById(R.id.button_save).setOnClickListener(v -> saveIngredient());
        inputExpiryDate.setOnClickListener(v -> showDatePicker());

        editingItemId = getIntent().getIntExtra(PantryActivity.EXTRA_PANTRY_ITEM_ID, -1);
        if (editingItemId != -1) {
            title.setText("Edit Ingredient");
            loadExistingItem();
        }
    }

    private void loadExistingItem() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            PantryItem item = pantryDao.getPantryItemById(editingItemId);
            runOnUiThread(() -> {
                if (item == null) {
                    Toast.makeText(this, "This ingredient no longer exists.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                editingItem = item;
                inputName.setText(item.getIngredientName());
                inputQuantity.setText(String.valueOf(item.getQuantity()));
                spinnerUnit.setSelection(indexOfUnit(item.getUnit()));
                selectedExpiryDate = item.getExpiryDate();
                inputExpiryDate.setText(ExpiryHelper.isValidDate(selectedExpiryDate)
                        ? selectedExpiryDate : "");
            });
        });
    }

    /** Finds the given unit in unitOptions (case-insensitive), defaulting to the first entry. */
    private int indexOfUnit(String unit) {
        if (unit != null) {
            for (int i = 0; i < unitOptions.length; i++) {
                if (unitOptions[i].equalsIgnoreCase(unit.trim())) {
                    return i;
                }
            }
        }
        return 0;
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (ExpiryHelper.isValidDate(selectedExpiryDate)) {
            String[] parts = selectedExpiryDate.split("-");
            calendar.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
        }
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedExpiryDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            inputExpiryDate.setText(selectedExpiryDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * Validates the form and, if valid, saves via Room (insert or update).
     * Shows a specific, human-readable error for the first problem found.
     */
    private void saveIngredient() {
        layoutName.setError(null);
        layoutQuantity.setError(null);

        String name = safeText(inputName);
        String quantityText = safeText(inputQuantity);
        String unit = spinnerUnit.getSelectedItem().toString();

        if (name.isEmpty()) {
            layoutName.setError("Please enter an ingredient name.");
            return;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException e) {
            layoutQuantity.setError("Please enter a valid quantity.");
            return;
        }
        if (quantity <= 0) {
            layoutQuantity.setError("Quantity must be greater than 0.");
            return;
        }

        if (selectedExpiryDate != null && !ExpiryHelper.isValidDate(selectedExpiryDate)) {
            Toast.makeText(this, "Please select a valid expiry date.", Toast.LENGTH_SHORT).show();
            return;
        }

        double finalQuantity = quantity;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (editingItem != null) {
                editingItem.setIngredientName(name);
                editingItem.setQuantity(finalQuantity);
                editingItem.setUnit(unit);
                editingItem.setExpiryDate(selectedExpiryDate);
                pantryDao.updatePantryItem(editingItem);
            } else {
                pantryDao.insertPantryItem(new PantryItem(name, finalQuantity, unit, selectedExpiryDate));
            }
            runOnUiThread(this::finish);
        });
    }

    private static String safeText(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }
}
