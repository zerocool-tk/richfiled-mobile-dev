package za.co.richfield.smartpantry.logic;

import java.util.List;

import za.co.richfield.smartpantry.data.Ingredient;
import za.co.richfield.smartpantry.data.IngredientNameNormalizer;
import za.co.richfield.smartpantry.data.QuantityFormatter;
import za.co.richfield.smartpantry.data.RecipeIngredient;
import za.co.richfield.smartpantry.data.UnitConverter;

/**
 * Decides whether <em>one</em> recipe requirement is satisfied by the user's pantry.
 *
 * <p>This class isolates the two things that make matching "robust to simple real-world messiness"
 * as the brief demands:</p>
 * <ol>
 *     <li><b>Names</b> are compared through {@link IngredientNameNormalizer}, so "Tomatoes" in the
 *         pantry satisfies a recipe that asks for "tomato".</li>
 *     <li><b>Quantities</b> are compared through {@link UnitConverter}, so a pantry holding
 *         "0.5 kg" of chicken satisfies a recipe that needs "500 g" of chicken.</li>
 * </ol>
 *
 * <p>A quantity comparison also allows small rounding differences (2% or 1 gram/millilitre,
 * whichever is larger) so that something like "0.9 l" still counts as "1 l" after a user typed a
 * slightly rounded number.</p>
 */
public final class IngredientMatcher {

    /** Tolerance for floating point comparisons: 2%, but never less than 1 base unit. */
    private static final double RELATIVE_TOLERANCE = 0.02;
    private static final double ABSOLUTE_TOLERANCE = 1.0;

    private IngredientMatcher() {
        // Utility class - never instantiated.
    }

    /** The outcome of comparing a recipe requirement against the pantry. */
    public static class Comparison {

        /** True when the pantry covers the required quantity of this ingredient. */
        public final boolean satisfied;
        /** The pantry row that was matched (may be {@code null} when nothing matched). */
        public final Ingredient pantryItem;
        /** Short, human readable explanation used by the "Almost There" screen. */
        public final String note;

        private Comparison(boolean satisfied, Ingredient pantryItem, String note) {
            this.satisfied = satisfied;
            this.pantryItem = pantryItem;
            this.note = note;
        }

        static Comparison no(String note) {
            return new Comparison(false, null, note);
        }

        static Comparison yes(Ingredient pantryItem) {
            return new Comparison(true, pantryItem, "Available");
        }
    }

    /**
     * Finds the pantry row (if any) that provides a required ingredient.
     *
     * <p>Because the pantry may hold the same ingredient more than once ("Eggs" 2 and "egg" 6),
     * the <em>best</em> candidate is chosen: the first row that satisfies the required quantity,
     * otherwise the row with the largest available quantity.</p>
     */
    public static Ingredient findInPantry(RecipeIngredient required, List<Ingredient> pantry) {
        String requiredKey = IngredientNameNormalizer.normalize(required.getName());
        Ingredient bestByName = null;
        for (Ingredient item : pantry) {
            if (!requiredKey.equals(IngredientNameNormalizer.normalize(item.getName()))) {
                continue;
            }
            Comparison comparison = compareQuantities(required, item);
            if (comparison.satisfied) {
                return item;                       // A row that covers the requirement wins outright.
            }
            if (bestByName == null || item.getQuantity() > bestByName.getQuantity()) {
                bestByName = item;                 // Otherwise remember the largest partial amount.
            }
        }
        return bestByName;
    }

    /**
     * The single test used by the strict-matching rule: does the pantry satisfy this requirement?
     */
    public static Comparison compare(RecipeIngredient required, List<Ingredient> pantry) {
        Ingredient match = findInPantry(required, pantry);
        if (match == null) {
            return Comparison.no("Not in your pantry");
        }
        Comparison comparison = compareQuantities(required, match);
        if (comparison.satisfied) {
            return comparison;
        }
        return Comparison.no("Only " + match.getQuantityLabel() + " in the pantry, "
                + "recipe needs " + required.getQuantityLabel());
    }

    /**
     * Compares the quantities of a matched pair of ingredients.
     *
     * <p>Rules, in order:</p>
     * <ul>
     *     <li>Convertible units (g/kg, ml/l, tsp/tbsp, countable items): convert the pantry amount
     *         into the recipe's unit and require {@code pantry >= needed} (within tolerance).</li>
     *     <li>Identical unit text that we cannot convert (for example "pinch"): compare directly.</li>
     *     <li>Units from different families (for example "1 tin" vs "400 g"): the app cannot convert
     *         them, so presence is accepted and the item is flagged as an assumed match. The
     *         alternative - failing the recipe outright - would be worse for the user.</li>
     * </ul>
     */
    private static Comparison compareQuantities(RecipeIngredient required, Ingredient pantryItem) {
        String requiredUnit = UnitConverter.canonicalUnit(required.getUnit());
        String pantryUnit = UnitConverter.canonicalUnit(pantryItem.getUnit());

        if (UnitConverter.isConvertible(requiredUnit, pantryUnit)) {
            double availableInRecipeUnit = UnitConverter.convert(
                    pantryItem.getQuantity(), pantryUnit, requiredUnit);
            double needed = required.getQuantity();
            double tolerance = Math.max(ABSOLUTE_TOLERANCE, needed * RELATIVE_TOLERANCE);
            if (availableInRecipeUnit + tolerance >= needed) {
                return Comparison.yes(pantryItem);
            }
            return Comparison.no("Only " + pantryItem.getQuantityLabel() + " available, need "
                    + QuantityFormatter.format(needed, requiredUnit));
        }

        if (requiredUnit.equalsIgnoreCase(pantryUnit)) {
            // Same unit text but an unknown family: a direct quantity comparison is still valid.
            if (pantryItem.getQuantity() + ABSOLUTE_TOLERANCE >= required.getQuantity()) {
                return Comparison.yes(pantryItem);
            }
            return Comparison.no("Only " + pantryItem.getQuantityLabel() + " available");
        }

        // Different, non-convertible units: assume the amount is sufficient, but say so honestly.
        return new Comparison(true, pantryItem,
                "Unit mismatch (" + pantryItem.getQuantityLabel() + " vs "
                        + required.getQuantityLabel() + ") - presence assumed");
    }
}
