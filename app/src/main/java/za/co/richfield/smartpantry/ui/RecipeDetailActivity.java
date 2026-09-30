package za.co.richfield.smartpantry.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

import java.util.List;

import za.co.richfield.smartpantry.R;
import za.co.richfield.smartpantry.data.DatabaseHelper;
import za.co.richfield.smartpantry.data.Ingredient;
import za.co.richfield.smartpantry.data.Recipe;
import za.co.richfield.smartpantry.data.RecipeIngredient;
import za.co.richfield.smartpantry.logic.IngredientMatcher;
import za.co.richfield.smartpantry.logic.RecipeMatch;
import za.co.richfield.smartpantry.logic.RecipeMatcher;

/**
 * Recipe detail screen: the full ingredient list, the method, and - most importantly - live feedback
 * on whether the pantry can actually cover the recipe.
 *
 * <p>The screen receives only a recipe id through its Intent and re-reads everything else from
 * SQLite, so it always reflects the current state of the pantry. Each ingredient row is marked
 * "In pantry" or "Missing", and the button at the bottom changes meaning accordingly:</p>
 * <ul>
 *     <li>cookable recipe - "I cooked this", which subtracts the used quantities from the pantry;</li>
 *     <li>recipe that is short of something - "Add missing ingredients to pantry", which opens the
 *         Add Ingredient form pre-filled with the first missing item.</li>
 * </ul>
 */
public class RecipeDetailActivity extends AppCompatActivity {

    /** Row id of the recipe to display. */
    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";

    private DatabaseHelper databaseHelper;
    private Recipe recipe;
    private RecipeMatch match;

    private TextView textName;
    private TextView textCategory;
    private TextView textStatus;
    private TextView textSteps;
    private TextView textNotes;
    private LinearLayout containerIngredients;
    private MaterialButton buttonAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        databaseHelper = DatabaseHelper.getInstance(this);

        textName = findViewById(R.id.text_detail_name);
        textCategory = findViewById(R.id.text_detail_category);
        textStatus = findViewById(R.id.text_detail_status);
        textSteps = findViewById(R.id.text_detail_steps);
        textNotes = findViewById(R.id.text_detail_notes);
        containerIngredients = findViewById(R.id.container_detail_ingredients);
        buttonAction = findViewById(R.id.button_detail_action);

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1L);
        recipe = databaseHelper.getRecipeById(recipeId);

        if (recipe == null) {
            // Defensive: the recipe list and the detail screen can never get out of step, but the app
            // must not crash if a stale Intent arrives.
            Toast.makeText(this, R.string.toast_missing_recipe, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setTitle(recipe.getName());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        bindRecipe();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Coming back from the Add Ingredient screen may have changed the pantry, so re-evaluate the
        // recipe: this is the same strict rule the suggestions screen uses.
        if (recipe != null) {
            bindRecipe();
        }
    }

    /** Fills the screen from the recipe and the current pantry. */
    private void bindRecipe() {
        List<Ingredient> pantry = databaseHelper.getAllIngredients();
        match = RecipeMatcher.match(recipe, pantry);

        textName.setText(recipe.getName());
        textCategory.setText(recipe.getCategory() + " - " + recipe.getIngredientCount() + " ingredients");
        textSteps.setText(recipe.getSteps());

        // ---- status line -----------------------------------------------------------------
        if (match.isCookable()) {
            textStatus.setText(R.string.recipe_status_can_cook);
        } else if (match.getStatus() == RecipeMatch.Status.ALMOST_THERE) {
            textStatus.setText(getString(R.string.recipe_status_almost,
                    match.getMissingIngredients().get(0).getName()));
        } else {
            textStatus.setText(getString(R.string.recipe_status_not_yet,
                    match.getMissingIngredients().size()));
        }

        // ---- ingredient checklist ---------------------------------------------------------
        containerIngredients.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (RecipeIngredient required : recipe.getIngredients()) {
            View row = inflater.inflate(R.layout.item_detail_ingredient, containerIngredients, false);
            TextView textIngredient = row.findViewById(R.id.text_detail_ingredient);
            TextView textAvailability = row.findViewById(R.id.text_detail_availability);
            ImageView imageStatus = row.findViewById(R.id.image_detail_status);

            IngredientMatcher.Comparison comparison = IngredientMatcher.compare(required, pantry);
            textIngredient.setText(required.getQuantityLabel() + " " + required.getName());

            if (comparison.satisfied) {
                textAvailability.setText(R.string.recipe_have_it);
                textAvailability.setTextColor(getColor(R.color.green_700));
                imageStatus.setImageResource(R.drawable.ic_check);
                imageStatus.setColorFilter(getColor(R.color.green_700));
            } else {
                textAvailability.setText(R.string.recipe_short_it);
                textAvailability.setTextColor(getColor(R.color.red_700));
                imageStatus.setImageResource(R.drawable.ic_expiring);
                imageStatus.setColorFilter(getColor(R.color.red_700));
            }
            containerIngredients.addView(row);
        }

        // ---- notes (for example a unit mismatch that was assumed to be fine) -----------------
        if (match.getNotes().isEmpty()) {
            textNotes.setVisibility(View.GONE);
        } else {
            textNotes.setVisibility(View.VISIBLE);
            StringBuilder builder = new StringBuilder();
            for (String note : match.getNotes()) {
                if (builder.length() > 0) {
                    builder.append('\n');
                }
                builder.append(note);
            }
            textNotes.setText(builder.toString());
        }

        // ---- action button ----------------------------------------------------------------
        if (match.isCookable()) {
            buttonAction.setText(R.string.recipe_cook_it_button);
            buttonAction.setOnClickListener(v -> confirmCooked());
        } else {
            buttonAction.setText(R.string.recipe_not_cookable_button);
            buttonAction.setOnClickListener(v -> addMissingIngredient());
        }
    }

    /** Asks for confirmation, then subtracts the recipe's ingredients from the pantry. */
    private void confirmCooked() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.cook_dialog_title)
                .setMessage(R.string.cook_dialog_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.cook_dialog_confirm, (dialog, which) -> {
                    int changed = databaseHelper.consumeIngredients(recipe);
                    Toast.makeText(this, getString(R.string.cook_done, changed), Toast.LENGTH_LONG).show();
                    bindRecipe();     // the recipe is usually no longer cookable afterwards
                })
                .show();
    }

    /** Opens the Add Ingredient form pre-filled with the first ingredient the pantry is missing. */
    private void addMissingIngredient() {
        List<RecipeIngredient> missing = match.getMissingIngredients();
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        if (!missing.isEmpty()) {
            intent.putExtra(AddEditIngredientActivity.EXTRA_PREFILL_NAME, missing.get(0).getName());
        }
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
