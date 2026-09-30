package za.co.richfield.smartpantry.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

import za.co.richfield.smartpantry.R;
import za.co.richfield.smartpantry.data.DatabaseHelper;
import za.co.richfield.smartpantry.data.Ingredient;
import za.co.richfield.smartpantry.data.Recipe;
import za.co.richfield.smartpantry.data.SettingsManager;
import za.co.richfield.smartpantry.logic.RecipeMatcher;
import za.co.richfield.smartpantry.util.DateUtils;

/**
 * Insights screen: a read-only summary that turns the same database reads into numbers the user can
 * act on - how many items are in the pantry, how many recipes that unlocks, how many are one
 * ingredient away and how many items are about to expire.
 */
public class InsightsFragment extends Fragment {

    private DatabaseHelper databaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_insights, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        databaseHelper = DatabaseHelper.getInstance(requireContext());
        refresh(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) {
            refresh(getView());
        }
    }

    private void refresh(View view) {
        SettingsManager settingsManager = new SettingsManager(requireContext());

        List<Ingredient> pantry = databaseHelper.getAllIngredients();
        List<Recipe> recipes = databaseHelper.getAllRecipes();
        int canCook = RecipeMatcher.suggestedRecipes(recipes, pantry).size();
        int almost = RecipeMatcher.almostThere(recipes, pantry).size();

        ((TextView) view.findViewById(R.id.text_stat_pantry)).setText(String.valueOf(pantry.size()));
        ((TextView) view.findViewById(R.id.text_stat_recipes)).setText(String.valueOf(recipes.size()));
        ((TextView) view.findViewById(R.id.text_stat_can_cook)).setText(String.valueOf(canCook));
        ((TextView) view.findViewById(R.id.text_stat_almost)).setText(String.valueOf(almost));

        int percent = recipes.isEmpty() ? 0 : Math.round(canCook * 100f / recipes.size());
        LinearProgressIndicator progress = view.findViewById(R.id.progress_potential);
        progress.setProgressCompat(percent, true);
        ((TextView) view.findViewById(R.id.text_progress_label)).setText(
                "You can cook " + canCook + " of " + recipes.size() + " recipes (" + percent + "%).");

        int expiringCount = 0;
        for (Ingredient ingredient : pantry) {
            if (DateUtils.isExpiringWithin(ingredient.getExpiryDate(),
                    settingsManager.getExpiryWindowDays())) {
                expiringCount++;
            }
        }
        TextView textExpiring = view.findViewById(R.id.text_stat_expiring);
        textExpiring.setText(getString(R.string.insights_expiring,
                settingsManager.getExpiryWindowDays()) + ": " + expiringCount);
    }
}
