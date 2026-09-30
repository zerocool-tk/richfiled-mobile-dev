package za.co.richfield.smartpantry.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.List;

import za.co.richfield.smartpantry.R;
import za.co.richfield.smartpantry.data.DatabaseHelper;
import za.co.richfield.smartpantry.data.Ingredient;
import za.co.richfield.smartpantry.data.QuantityFormatter;
import za.co.richfield.smartpantry.data.UnitConverter;
import za.co.richfield.smartpantry.util.DateUtils;
import za.co.richfield.smartpantry.util.ValidationUtils;

/**
 * The Add / Edit Ingredient form - the "Create" and "Update" parts of CRUD, plus validation.
 *
 * <p>The same Activity serves both purposes; whether it is an insert or an update depends on the
 * {@link #EXTRA_INGREDIENT_ID} extra that arrives with the Intent. That extra is the clearest
 * example in the app of passing data between screens with an Intent:</p>
 * <pre>
 *   PantryFragment -> new Intent(context, AddEditIngredientActivity.class)
 *                     .putExtra(EXTRA_INGREDIENT_ID, ingredient.getId())
 *   AddEditIngredientActivity -> getIntent().getLongExtra(EXTRA_INGREDIENT_ID, Ingredient.NO_ID)
 * </pre>
 *
 * <p>Every field is validated before anything is written to SQLite, and the error messages are shown
 * underneath the offending field by the TextInputLayouts.</p>
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    /** Row id of the ingredient being edited; {@link Ingredient#NO_ID} means "add a new one". */
    public static final String EXTRA_INGREDIENT_ID = "extra_ingredient_id";
    /** Optional: pre-fills the name field, used when adding a missing recipe ingredient. */
    public static final String EXTRA_PREFILL_NAME = "extra_prefill_name";

    private DatabaseHelper databaseHelper;

    private TextInputLayout layoutName;
    private TextInputLayout layoutQuantity;
    private TextInputLayout layoutUnit;
    private TextInputLayout layoutExpiry;
    private MaterialAutoCompleteTextView inputName;
    private TextInputEditText inputQuantity;
    private MaterialAutoCompleteTextView inputUnit;
    private TextInputEditText inputExpiry;

    /** The item being edited, or a new empty model object when adding. */
    private Ingredient editingIngredient;
    private boolean isEditMode;

    /** Unit tokens in spinner order, with the matching labels the user sees. */
    private final String[] unitValues = UnitConverter.spinnerUnits();
    private final String[] unitLabels = new String[unitValues.length];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        databaseHelper = DatabaseHelper.getInstance(this);

        layoutName = findViewById(R.id.layout_name);
        layoutQuantity = findViewById(R.id.layout_quantity);
        layoutUnit = findViewById(R.id.layout_unit);
        layoutExpiry = findViewById(R.id.layout_expiry);
        inputName = findViewById(R.id.input_name);
        inputQuantity = findViewById(R.id.input_quantity);
        inputUnit = findViewById(R.id.input_unit);
        inputExpiry = findViewById(R.id.input_expiry);

        MaterialButton buttonSave = findViewById(R.id.button_save);
        MaterialButton buttonCancel = findViewById(R.id.button_cancel);
        MaterialButton buttonClearExpiry = findViewById(R.id.button_clear_expiry);

        long ingredientId = getIntent().getLongExtra(EXTRA_INGREDIENT_ID, Ingredient.NO_ID);
        isEditMode = ingredientId != Ingredient.NO_ID;

        if (isEditMode) {
            editingIngredient = databaseHelper.getIngredientById(ingredientId);
        }
        if (editingIngredient == null) {
            editingIngredient = new Ingredient();
            isEditMode = false;
        }

        setTitle(isEditMode ? R.string.title_edit_ingredient : R.string.title_add_ingredient);

        // The toolbar in this screen is created here (the layout is a ScrollView), so the back arrow
        // is added in code rather than in XML.
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setupNameSuggestions();
        setupUnitSpinner();
        setupExpiryPicker();
        populateFields();

        buttonSave.setOnClickListener(v -> saveIngredient());
        buttonCancel.setOnClickListener(v -> finish());
        buttonClearExpiry.setOnClickListener(v -> inputExpiry.setText(""));
    }

    /** Offers the ingredient names the recipe library actually uses, so spellings line up. */
    private void setupNameSuggestions() {
        List<String> catalogue = databaseHelper.getAllRecipeIngredientNames();
        ArrayAdapter<String> nameAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, catalogue);
        inputName.setAdapter(nameAdapter);
        inputName.setThreshold(1);    // suggest from the first character typed

        String prefill = getIntent().getStringExtra(EXTRA_PREFILL_NAME);
        if (prefill != null && !prefill.trim().isEmpty()) {
            inputName.setText(prefill, false);
        }
    }

    private void setupUnitSpinner() {
        for (int i = 0; i < unitValues.length; i++) {
            unitLabels[i] = UnitConverter.displayUnit(unitValues[i]);
        }
        inputUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, unitLabels));
        // Default to grams for a new item; the user can change it with one tap.
        if (!isEditMode) {
            inputUnit.setText(UnitConverter.displayUnit(UnitConverter.GRAM), false);
        }
    }

    private void setupExpiryPicker() {
        // The field is not directly editable: tapping it (or the calendar icon) opens a DatePicker.
        inputExpiry.setOnClickListener(v -> showDatePicker());
        layoutExpiry.setEndIconOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        Calendar calendar = DateUtils.parseIso(text(inputExpiry));
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar chosen = Calendar.getInstance();
            chosen.set(year, month, dayOfMonth);
            inputExpiry.setText(DateUtils.formatIso(chosen));
            layoutExpiry.setError(null);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void populateFields() {
        if (!isEditMode) {
            return;
        }
        inputName.setText(editingIngredient.getName(), false);
        inputQuantity.setText(QuantityFormatter.formatNumber(editingIngredient.getQuantity()));
        inputUnit.setText(UnitConverter.displayUnit(editingIngredient.getUnit()), false);
        if (editingIngredient.getExpiryDate() != null) {
            inputExpiry.setText(editingIngredient.getExpiryDate());
        }
        // The delete action is only offered when an existing record is on screen.
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ingredient_form, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem deleteItem = menu.findItem(R.id.action_delete);
        if (deleteItem != null) {
            deleteItem.setVisible(isEditMode);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        if (itemId == R.id.action_save) {
            saveIngredient();
            return true;
        }
        if (itemId == R.id.action_delete) {
            confirmDelete();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ------------------------------------------------------------------ validation and saving

    /**
     * Validates every field, reports the first problem on the matching TextInputLayout, and only
     * writes to SQLite when the whole form is valid.
     */
    private void saveIngredient() {
        layoutName.setError(null);
        layoutQuantity.setError(null);
        layoutUnit.setError(null);
        layoutExpiry.setError(null);

        String name = text(inputName);
        String quantityText = text(inputQuantity);
        String unitLabel = text(inputUnit);
        String expiry = text(inputExpiry);

        String nameError = ValidationUtils.validateName(this, name);
        String quantityError = ValidationUtils.validateQuantity(this, quantityText);
        String unitError = ValidationUtils.validateUnit(this, unitLabel);
        String expiryError = ValidationUtils.validateExpiryDate(this, expiry);

        layoutName.setError(nameError);
        layoutQuantity.setError(quantityError);
        layoutUnit.setError(unitError);
        layoutExpiry.setError(expiryError);

        if (nameError != null) {
            inputName.requestFocus();
            return;
        }
        if (quantityError != null) {
            inputQuantity.requestFocus();
            return;
        }
        if (unitError != null) {
            inputUnit.requestFocus();
            return;
        }
        if (expiryError != null) {
            inputExpiry.requestFocus();
            return;
        }

        editingIngredient.setName(name.trim());
        editingIngredient.setQuantity(ValidationUtils.parseQuantity(quantityText));
        editingIngredient.setUnit(UnitConverter.canonicalUnit(unitLabel));
        editingIngredient.setExpiryDate(expiry.trim().isEmpty() ? null : expiry.trim());

        if (isEditMode) {
            databaseHelper.updateIngredient(editingIngredient);
            Toast.makeText(this, getString(R.string.toast_saved_updated, editingIngredient.getName()),
                    Toast.LENGTH_SHORT).show();
        } else {
            databaseHelper.addIngredient(editingIngredient);
            Toast.makeText(this, getString(R.string.toast_saved_added, editingIngredient.getName()),
                    Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(getString(R.string.dialog_delete_message, editingIngredient.getName()))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_confirm_delete, (dialog, which) -> {
                    databaseHelper.deleteIngredient(editingIngredient.getId());
                    Toast.makeText(this,
                            getString(R.string.toast_deleted, editingIngredient.getName()),
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .show();
    }

    /** Null-safe text getter for the form fields. */
    private String text(android.widget.TextView view) {
        return view.getText() == null ? "" : view.getText().toString();
    }
}
