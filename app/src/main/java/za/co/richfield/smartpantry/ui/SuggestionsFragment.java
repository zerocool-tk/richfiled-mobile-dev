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

import com.google.android.material.button.MaterialButton;

import java.util.List;

import za.co.richfield.smartpantry.R;
import za.co.richfield.smartpantry.adapter.RecipeAdapter;
import za.co.richfield.smartpantry.data.DatabaseHelper;
import za.co.richfield.smartpantry.data.Recipe;
import za.co.richfield.smartpantry.logic.RecipeMatch;
import za.co.richfield.smartpantry.logic.RecipeMatcher;

/**
 * The Suggested Recipes screen - the home of the strict-matching rule.
 *
 * <p>It loads every recipe from SQLite and passes the list through
 * {@link RecipeMatcher#suggestedRecipes(List, List)}, which keeps only the recipes for which
 * <em>every</em> ingredient is present in the pantry in at least the required quantity. Recipes that
 * are one ingredient short are deliberately excluded here and appear instead on the separate
 * "Almost There" screen.</p>
 *
 * <p>When the list is empty the screen explains why rather than showing a blank area.</p>
 */
public class SuggestionsFragment extends Fragment implements RecipeAdapter.RecipeClickListener {

    private DatabaseHelper databaseHelper;
    private RecipeAdapter adapter;
    private LinearLayout groupEmpty;
    private TextView textCount;
    private RecyclerView recyclerSuggestions;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_suggestions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        databaseHelper = DatabaseHelper.getInstance(requireContext());

        recyclerSuggestions = view.findViewById(R.id.recycler_suggestions);
        groupEmpty = view.findViewById(R.id.group_suggestions_empty);
        textCount = view.findViewById(R.id.text_suggestions_count);
        MaterialButton buttonEmptyAction = view.findViewById(R.id.button_suggestions_empty_action);

        adapter = new RecipeAdapter(this);
        recyclerSuggestions.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerSuggestions.setAdapter(adapter);

        buttonEmptyAction.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openAddIngredientForm();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSuggestions();
    }

    /** Runs the strict-matching rule and updates the list, the counter and the empty state. */
    private void loadSuggestions() {
        List<Recipe> recipes = databaseHelper.getAllRecipes();
        List<RecipeMatch> suggestions = RecipeMatcher.suggestedRecipes(
                recipes, databaseHelper.getAllIngredients());

        adapter.submitList(suggestions);
        textCount.setText(getString(R.string.suggestions_count, suggestions.size()));

        boolean hasSuggestions = !suggestions.isEmpty();
        recyclerSuggestions.setVisibility(hasSuggestions ? View.VISIBLE : View.GONE);
        groupEmpty.setVisibility(hasSuggestions ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onRecipeSelected(RecipeMatch match) {
        Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, match.getRecipe().getId());
        startActivity(intent);
    }
}
