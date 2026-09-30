package za.co.richfield.smartpantry.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import za.co.richfield.smartpantry.R;
import za.co.richfield.smartpantry.adapter.PantryAdapter;
import za.co.richfield.smartpantry.data.DatabaseHelper;
import za.co.richfield.smartpantry.data.Ingredient;
import za.co.richfield.smartpantry.data.SettingsManager;
import za.co.richfield.smartpantry.logic.RecipeMatcher;

/**
 * The pantry list screen - the "Read" half of CRUD and the screen that drives the app.
 *
 * <p>It shows every ingredient stored in SQLite in a RecyclerView with a custom adapter, offers a
 * live search, summarises how many recipes the current pantry unlocks, and provides edit and delete
 * actions (the "Update" and "Delete" parts of CRUD).</p>
 *
 * <p>{@link #onResume()} reloads from the database, so returning from the Add/Edit screen always
 * shows the newest data.</p>
 */
public class PantryFragment extends Fragment implements PantryAdapter.PantryItemListener {

    private DatabaseHelper databaseHelper;
    private SettingsManager settingsManager;
    private PantryAdapter adapter;

    private RecyclerView recyclerPantry;
    private LinearLayout groupEmpty;
    private TextView textEmptyTitle;
    private TextView textEmptyBody;
    private TextView textPantrySummary;
    private TextView textMatchSummary;
    private TextInputEditText inputSearch;
    private MaterialButton buttonEmptyAction;

    private String currentSearch = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pantry, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        databaseHelper = DatabaseHelper.getInstance(requireContext());
        settingsManager = new SettingsManager(requireContext());

        recyclerPantry = view.findViewById(R.id.recycler_pantry);
        groupEmpty = view.findViewById(R.id.group_pantry_empty);
        textEmptyTitle = view.findViewById(R.id.text_pantry_empty_title);
        textEmptyBody = view.findViewById(R.id.text_pantry_empty_body);
        textPantrySummary = view.findViewById(R.id.text_pantry_summary);
        textMatchSummary = view.findViewById(R.id.text_match_summary);
        inputSearch = view.findViewById(R.id.input_search);
        buttonEmptyAction = view.findViewById(R.id.button_pantry_empty_action);

        adapter = new PantryAdapter(this,
                settingsManager.isExpiryAlertsEnabled(),
                settingsManager.getExpiryWindowDays());
        recyclerPantry.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerPantry.setAdapter(adapter);
        recyclerPantry.setHasFixedSize(false);

        buttonEmptyAction.setOnClickListener(v -> openIngredientForm(null));

        // Live search: every keystroke re-queries SQLite rather than filtering in memory.
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed.
            }

            @Override
            public void afterTextChanged(Editable editable) {
                currentSearch = editable == null ? "" : editable.toString().trim();
                loadPantryItems();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Rebuild the adapter in case the expiry window or the alert switch changed in Settings.
        if (settingsManager != null) {
            adapter = new PantryAdapter(this,
                    settingsManager.isExpiryAlertsEnabled(),
                    settingsManager.getExpiryWindowDays());
            recyclerPantry.setAdapter(adapter);
        }
        loadPantryItems();
    }

    /** Reads the pantry from SQLite and decides whether to show the list or the empty state. */
    private void loadPantryItems() {
        List<Ingredient> items = databaseHelper.searchIngredients(currentSearch);
        adapter.submitList(items);

        int totalItems = databaseHelper.getPantryItemCount();
        textPantrySummary.setText(getString(R.string.pantry_item_count, totalItems));

        int recipeCount = databaseHelper.getRecipeCount();
        int canCook = RecipeMatcher.suggestedRecipes(databaseHelper.getAllRecipes(),
                databaseHelper.getAllIngredients()).size();
        textMatchSummary.setText(getString(R.string.pantry_summary_text, canCook, recipeCount));

        boolean hasItems = !items.isEmpty();
        recyclerPantry.setVisibility(hasItems ? View.VISIBLE : View.GONE);
        groupEmpty.setVisibility(hasItems ? View.GONE : View.VISIBLE);

        if (!hasItems) {
            boolean searching = !currentSearch.isEmpty();
            // A search that found nothing is a different situation from an empty pantry, so the
            // user is told which one they are looking at.
            textEmptyTitle.setText(searching
                    ? R.string.pantry_no_search_results : R.string.pantry_empty_title);
            textEmptyBody.setText(searching
                    ? R.string.pantry_search_hint : R.string.pantry_empty_body);
            buttonEmptyAction.setVisibility(searching ? View.GONE : View.VISIBLE);
        }
    }

    private void openIngredientForm(@Nullable Ingredient ingredient) {
        Intent intent = new Intent(requireContext(), AddEditIngredientActivity.class);
        if (ingredient != null) {
            intent.putExtra(AddEditIngredientActivity.EXTRA_INGREDIENT_ID, ingredient.getId());
        }
        startActivity(intent);
    }

    // ------------------------------------------------------------------ adapter callbacks

    @Override
    public void onEditIngredient(Ingredient ingredient) {
        openIngredientForm(ingredient);
    }

    @Override
    public void onDeleteIngredient(final Ingredient ingredient) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_delete_title)
                .setMessage(getString(R.string.dialog_delete_message, ingredient.getName()))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_confirm_delete, (dialog, which) -> {
                    boolean deleted = databaseHelper.deleteIngredient(ingredient.getId());
                    if (deleted) {
                        Toast.makeText(requireContext(),
                                getString(R.string.toast_deleted, ingredient.getName()),
                                Toast.LENGTH_SHORT).show();
                        loadPantryItems();
                    }
                })
                .show();
    }
}
