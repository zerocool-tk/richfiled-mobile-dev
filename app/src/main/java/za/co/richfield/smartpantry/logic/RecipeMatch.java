package za.co.richfield.smartpantry.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import za.co.richfield.smartpantry.data.Recipe;
import za.co.richfield.smartpantry.data.RecipeIngredient;

/**
 * The result of testing one recipe against the pantry.
 *
 * <p>Bundling the recipe, its status and the missing ingredients together means the UI only has to
 * ask one object what to display, instead of recalculating matches in every Activity.</p>
 */
public class RecipeMatch {

    /** How well a recipe fits what the user currently has at home. */
    public enum Status {
        /** Every required ingredient is present in at least the required quantity. */
        CAN_COOK,
        /** Exactly one ingredient is missing or short - the optional "Almost There" list. */
        ALMOST_THERE,
        /** Two or more ingredients are missing, so the recipe is not shown to the user. */
        NOT_YET
    }

    private final Recipe recipe;
    private final Status status;
    private final List<RecipeIngredient> missingIngredients = new ArrayList<>();
    private final List<String> notes = new ArrayList<>();

    public RecipeMatch(Recipe recipe, Status status) {
        this.recipe = recipe;
        this.status = status;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public Status getStatus() {
        return status;
    }

    /** True only for the strict case: everything needed is in the pantry. */
    public boolean isCookable() {
        return status == Status.CAN_COOK;
    }

    /** The requirements that were not satisfied (empty for {@link Status#CAN_COOK}). */
    public List<RecipeIngredient> getMissingIngredients() {
        return Collections.unmodifiableList(missingIngredients);
    }

    /** Extra explanations, for example an assumed match caused by a unit mismatch. */
    public List<String> getNotes() {
        return Collections.unmodifiableList(notes);
    }

    public void addMissingIngredient(RecipeIngredient ingredient) {
        missingIngredients.add(ingredient);
    }

    public void addNote(String note) {
        notes.add(note);
    }

    public int getAvailableIngredientCount() {
        return recipe.getIngredientCount() - missingIngredients.size();
    }

    /** A short sentence describing the match, used as the subtitle on both suggestion lists. */
    public String getSummary() {
        if (status == Status.CAN_COOK) {
            return "You have all " + recipe.getIngredientCount() + " ingredients";
        }
        if (missingIngredients.size() == 1) {
            return "You only need " + missingIngredients.get(0).getName() + " to cook this";
        }
        return getAvailableIngredientCount() + " of " + recipe.getIngredientCount()
                + " ingredients available";
    }
}
