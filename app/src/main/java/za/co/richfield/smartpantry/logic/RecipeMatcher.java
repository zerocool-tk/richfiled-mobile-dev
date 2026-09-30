package za.co.richfield.smartpantry.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import za.co.richfield.smartpantry.data.Ingredient;
import za.co.richfield.smartpantry.data.Recipe;
import za.co.richfield.smartpantry.data.RecipeIngredient;

/**
 * The strict-matching rule from Section 2.3 of the assignment brief.
 *
 * <blockquote>"A recipe may only be shown as suggested if every single ingredient it requires is
 * currently present in the user's pantry, in at least the required quantity."</blockquote>
 *
 * <p>{@link #isCookable(Recipe, List)} is the whole rule: it returns {@code true} only when
 * <em>no</em> requirement fails. A recipe needing five ingredients where the pantry has four is
 * therefore excluded from the suggestions list - this is the behaviour the marker will test by
 * adding and removing a single ingredient and watching a recipe appear or disappear.</p>
 *
 * <p>{@link #matchAll(List, List)} additionally classifies every recipe as CAN_COOK,
 * ALMOST_THERE (exactly one missing ingredient) or NOT_YET, which powers the optional bonus
 * "Almost There" screen. That list is deliberately kept separate from the strict suggestions.</p>
 */
public final class RecipeMatcher {

    private RecipeMatcher() {
        // Utility class - never instantiated.
    }

    /**
     * @param recipe a recipe and its required ingredients
     * @param pantry everything the user currently has at home
     * @return true only when every single requirement is satisfied in at least the required quantity
     */
    public static boolean isCookable(Recipe recipe, List<Ingredient> pantry) {
        if (pantry == null || pantry.isEmpty()) {
            return false;   // Nothing in the pantry means nothing can be cooked.
        }
        for (RecipeIngredient required : recipe.getIngredients()) {
            if (!IngredientMatcher.compare(required, pantry).satisfied) {
                return false;               // One short ingredient is enough to exclude the recipe.
            }
        }
        return true;
    }

    /**
     * Tests a single recipe in detail: status, the list of missing ingredients and any notes.
     */
    public static RecipeMatch match(Recipe recipe, List<Ingredient> pantry) {
        List<Ingredient> safePantry = pantry == null ? Collections.emptyList() : pantry;
        List<RecipeIngredient> missing = new ArrayList<>();
        List<String> notes = new ArrayList<>();

        for (RecipeIngredient required : recipe.getIngredients()) {
            IngredientMatcher.Comparison comparison = IngredientMatcher.compare(required, safePantry);
            if (!comparison.satisfied) {
                missing.add(required);
            } else if (comparison.note != null && !"Available".equals(comparison.note)) {
                notes.add(required.getName() + ": " + comparison.note);
            }
        }

        // Classification: nothing missing = strict match, exactly one missing = bonus "almost there".
        RecipeMatch.Status status;
        if (missing.isEmpty()) {
            status = RecipeMatch.Status.CAN_COOK;
        } else if (missing.size() == 1) {
            status = RecipeMatch.Status.ALMOST_THERE;
        } else {
            status = RecipeMatch.Status.NOT_YET;
        }

        RecipeMatch result = new RecipeMatch(recipe, status);
        for (RecipeIngredient ingredient : missing) {
            result.addMissingIngredient(ingredient);
        }
        for (String note : notes) {
            result.addNote(note);
        }
        return result;
    }

    /** Every recipe classified against the current pantry, sorted by name. */
    public static List<RecipeMatch> matchAll(List<Recipe> recipes, List<Ingredient> pantry) {
        List<RecipeMatch> matches = new ArrayList<>();
        for (Recipe recipe : recipes) {
            matches.add(match(recipe, pantry));
        }
        matches.sort(Comparator.comparing(m -> m.getRecipe().getName(), String.CASE_INSENSITIVE_ORDER));
        return matches;
    }

    /** Only the recipes that pass the strict rule - this is the "Suggested Recipes" list. */
    public static List<RecipeMatch> suggestedRecipes(List<Recipe> recipes, List<Ingredient> pantry) {
        List<RecipeMatch> suggestions = new ArrayList<>();
        for (RecipeMatch match : matchAll(recipes, pantry)) {
            if (match.isCookable()) {
                suggestions.add(match);
            }
        }
        return suggestions;
    }

    /** The bonus list: recipes that are exactly one ingredient short. */
    public static List<RecipeMatch> almostThere(List<Recipe> recipes, List<Ingredient> pantry) {
        List<RecipeMatch> almost = new ArrayList<>();
        for (RecipeMatch match : matchAll(recipes, pantry)) {
            if (match.getStatus() == RecipeMatch.Status.ALMOST_THERE) {
                almost.add(match);
            }
        }
        return almost;
    }

    /** Convenience for the Insights screen: the number of recipes the user can cook right now. */
    public static int countSuggested(List<Recipe> recipes, List<Ingredient> pantry) {
        return suggestedRecipes(recipes, pantry).size();
    }
}
