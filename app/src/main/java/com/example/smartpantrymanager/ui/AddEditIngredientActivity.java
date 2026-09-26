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
 * The form for adding a new ingredient or editing an existing one. The same
 * screen does both jobs.
 *
 * How it knows which job to do: whoever opens this screen can attach an item ID.
 *  - No ID attached: the form starts empty and Save creates a new item.
 *  - ID attached: the screen loads that item, fills in the form, and Save
 *    updates it instead.
 *
 * Only the ID is passed in, not the whole item, so the form always shows what's
 * actually saved in the database.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    private PantryDao pantryDao;
    // -1 is a stand-in meaning "no ID was given", so we're adding, not editing.
    private int editingItemId = -1;
    // The item being edited, once it's loaded. Stays null when adding.
    private PantryItem editingItem;

    // Each form field has two parts: the outer box (TextInputLayout), which can
    // show an error message under the field, and the text box inside it
    // (TextInputEditText), which holds what the user typed.
    private TextInputLayout layoutName;
    private TextInputLayout layoutQuantity;
    private TextInputLayout layoutExpiryDate;
    private TextInputEditText inputName;
    private TextInputEditText inputQuantity;
    private TextInputEditText inputExpiryDate;
    // The drop-down list of units (g, kg, pieces, ...).
    private Spinner spinnerUnit;
    private String[] unitOptions;
    // The chosen expiry date as "yyyy-MM-dd", or null if none has been picked.
    private String selectedExpiryDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        pantryDao = AppDatabase.getInstance(this).pantryDao();

        // Find each part of the form in the layout so we can read and change it.
        TextView title = findViewById(R.id.text_screen_title);
        layoutName = findViewById(R.id.layout_ingredient_name);
        layoutQuantity = findViewById(R.id.layout_quantity);
        layoutExpiryDate = findViewById(R.id.layout_expiry_date);
        inputName = findViewById(R.id.input_ingredient_name);
        inputQuantity = findViewById(R.id.input_quantity);
        inputExpiryDate = findViewById(R.id.input_expiry_date);
        spinnerUnit = findViewById(R.id.spinner_unit);

        // Fill the unit drop-down with the choices listed in res/values/arrays.xml.
        // The adapter turns each unit name into a row in the drop-down.
        unitOptions = getResources().getStringArray(R.array.unit_options);
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, unitOptions);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);

        // Colour the field outlines and labels blue, and put a calendar icon at the
        // end of the expiry field. This is done in code instead of the XML layout so
        // the colours come out right on every device.
        int blue = ContextCompat.getColor(this, R.color.primary_blue);
        for (TextInputLayout layout : new TextInputLayout[]{layoutName, layoutQuantity, layoutExpiryDate}) {
            layout.setBoxStrokeColor(blue);
            layout.setDefaultHintTextColor(ColorStateList.valueOf(blue));
        }
        layoutExpiryDate.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
        layoutExpiryDate.setEndIconDrawable(R.drawable.ic_calendar);

        // Back and Cancel both close the form without saving. finish() closes
        // this screen and returns to the one before it.
        findViewById(R.id.button_back).setOnClickListener(v -> finish());
        findViewById(R.id.button_cancel).setOnClickListener(v -> finish());
        findViewById(R.id.button_save).setOnClickListener(v -> saveIngredient());
        // Tapping the expiry field opens a calendar instead of the keyboard, so the
        // date always comes out in the right format.
        inputExpiryDate.setOnClickListener(v -> showDatePicker());

        // Check whether an item ID was passed in. If not, we get -1 back.
        editingItemId = getIntent().getIntExtra(PantryActivity.EXTRA_PANTRY_ITEM_ID, -1);
        if (editingItemId != -1) {
            title.setText("Edit Ingredient");
            loadExistingItem();
        }
    }

    /**
     * Loads the item being edited from the database (in the background), then fills
     * in the form with its current values.
     */
    private void loadExistingItem() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            PantryItem item = pantryDao.getPantryItemById(editingItemId);
            runOnUiThread(() -> {
                // The item may have been deleted since the edit button was tapped.
                // If so, say so and close the screen rather than show an empty form.
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
                // Only show the saved date if it's a real date; otherwise leave it blank.
                inputExpiryDate.setText(ExpiryHelper.isValidDate(selectedExpiryDate)
                        ? selectedExpiryDate : "");
            });
        });
    }

    /**
     * Finds where a unit sits in the drop-down list so it can be pre-selected when
     * editing. Upper and lower case are treated the same. If the unit isn't in the
     * list, the first option is picked.
     */
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

    /**
     * Opens a pop-up calendar. It starts on the date already chosen, or on today
     * if there isn't one.
     */
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance(); // today by default
        if (ExpiryHelper.isValidDate(selectedExpiryDate)) {
            // Split "2026-09-15" into year, month and day. Java counts months from
            // 0 (January is 0), so 1 is taken off the month.
            String[] parts = selectedExpiryDate.split("-");
            calendar.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
        }
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            // The user picked a date. Turn it back into "yyyy-MM-dd" text (adding
            // the 1 back to the month) and show it in the field. %02d pads single
            // digits with a zero, so 9 becomes "09".
            selectedExpiryDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            inputExpiryDate.setText(selectedExpiryDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * Runs when Save is tapped. Checks the form, and if everything is fine, saves
     * the ingredient and closes the screen.
     *
     * The checks run in order, and the first problem found is shown under the
     * field it belongs to. Nothing is saved until every check passes.
     */
    private void saveIngredient() {
        // Clear error messages left over from the last attempt.
        layoutName.setError(null);
        layoutQuantity.setError(null);

        String name = safeText(inputName);
        String quantityText = safeText(inputQuantity);
        String unit = spinnerUnit.getSelectedItem().toString();

        // Check 1: the name can't be blank.
        if (name.isEmpty()) {
            layoutName.setError("Please enter an ingredient name.");
            return;
        }

        // Check 2: the quantity has to be a number. parseDouble fails on things
        // like "abc" or an empty box, and we show an error instead of crashing.
        double quantity;
        try {
            quantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException e) {
            layoutQuantity.setError("Please enter a valid quantity.");
            return;
        }
        // Check 3: the quantity has to be more than zero.
        if (quantity <= 0) {
            layoutQuantity.setError("Quantity must be greater than 0.");
            return;
        }

        // Check 4: an expiry date is optional, but if there is one it has to be real.
        if (selectedExpiryDate != null && !ExpiryHelper.isValidDate(selectedExpiryDate)) {
            Toast.makeText(this, "Please select a valid expiry date.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Every check passed, so save in the background.
        double finalQuantity = quantity;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (editingItem != null) {
                // Editing: change the existing item's values and save over it.
                editingItem.setIngredientName(name);
                editingItem.setQuantity(finalQuantity);
                editingItem.setUnit(unit);
                editingItem.setExpiryDate(selectedExpiryDate);
                pantryDao.updatePantryItem(editingItem);
            } else {
                // Adding: create a brand new item.
                pantryDao.insertPantryItem(new PantryItem(name, finalQuantity, unit, selectedExpiryDate));
            }
            // Close the form. The pantry list refreshes itself when it reappears.
            runOnUiThread(this::finish);
        });
    }

    // Gets the text from a field with the spaces at either end removed.
    // Returns "" rather than null if the field has nothing in it.
    private static String safeText(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }
}
