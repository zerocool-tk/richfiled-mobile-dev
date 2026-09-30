package za.co.richfield.smartpantry.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import za.co.richfield.smartpantry.R;
import za.co.richfield.smartpantry.adapter.RecipeAdapter;
import za.co.richfield.smartpantry.data.DatabaseHelper;
import za.co.richfield.smartpantry.data.SettingsManager;
import za.co.richfield.smartpantry.data.RecipeIngredient;
import za.co.richfield.smartpantry.logic.RecipeMatch;
import za.co.richfield.smartpantry.logic.RecipeMatcher;

/**
 * Bonus screen (Section 2.3 of the brief): recipes that are missing exactly ONE ingredient.
 *
 * <p>The brief is explicit that partial matches must never appear in the strict suggestions list, so
 * these recipes are shown only here, on their own screen, and every row states which ingredient is
 * missing. {@link RecipeMatch#getMissingIngredients()} is guaranteed to hold exactly one entry
 * because {@link RecipeMatcher#almostThere(List, List)} filters on that condition.</p>
 */
public class AlmostThereFragment extends Fragment implements RecipeAdapter.RecipeClickListener {

    private DatabaseHelper databaseHelper;
    private RecipeAdapter adapter;
    private LinearLayout groupEmpty;
    private TextView textEmptyTitle;
    private RecyclerView recyclerAlmost;
    private TextView textIntro;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_almost_there, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        databaseHelper = DatabaseHelper.getInstance(requireContext());

        recyclerAlmost = view.findViewById(R.id.recycler_almost);
        groupEmpty = view.findViewById(R.id.group_almost_empty);
        textIntro = view.findViewById(R.id.text_almost_intro);
        textEmptyTitle = view.findViewById(R.id.text_almost_empty_title);

        adapter = new RecipeAdapter(this);
        recyclerAlmost.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerAlmost.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAlmostThere();
    }

    /** Loads the one-ingredient-short recipes and hides the list when there are none. */
    private void loadAlmostThere() {
        SettingsManager settingsManager = new SettingsManager(requireContext());

        if (!settingsManager.isAlmostThereEnabled()) {
            // The user switched this bonus list off in Settings.
            adapter.submitList(null);
            recyclerAlmost.setVisibility(View.GONE);
            groupEmpty.setVisibility(View.VISIBLE);
            textIntro.setText(R.string.settings_almost_summary);
            textEmptyTitle.setText(R.string.settings_almost_title);
            return;
        }

        List<RecipeMatch> almost = RecipeMatcher.almostThere(
                databaseHelper.getAllRecipes(), databaseHelper.getAllIngredients());

        adapter.submitList(almost);
        textIntro.setText(R.string.almost_intro);

        boolean hasItems = !almost.isEmpty();
        recyclerAlmost.setVisibility(hasItems ? View.VISIBLE : View.GONE);
        groupEmpty.setVisibility(hasItems ? View.GONE : View.VISIBLE);

        if (hasItems) {
            // Spell out what is missing, because that is the whole point of this screen.
            StringBuilder builder = new StringBuilder(getString(R.string.almost_intro));
            builder.append('\n').append(getString(R.string.almost_missing_label, describeFirstMissing(almost)));
            textIntro.setText(builder.toString());
        }
    }

    private String describeFirstMissing(List<RecipeMatch> almost) {
        RecipeMatch first = almost.get(0);
        RecipeIngredient missing = first.getMissingIngredients().get(0);
        return first.getRecipe().getName() + " - " + missing.getName();
    }

    @Override
    public void onRecipeSelected(RecipeMatch match) {
        Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, match.getRecipe().getId());
        startActivity(intent);
    }
}
