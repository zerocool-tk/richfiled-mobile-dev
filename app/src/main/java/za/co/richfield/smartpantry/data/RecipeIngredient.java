package za.co.richfield.smartpantry.data;

/**
 * One required ingredient of a recipe (a row of the "recipe_ingredient" table).
 *
 * <p>Kept deliberately separate from {@link Ingredient}: an {@code Ingredient} is something the
 * user <em>owns</em> and has a row id plus an expiry date, while a {@code RecipeIngredient} is a
 * <em>requirement</em> that is only ever compared against the pantry.</p>
 */
public class RecipeIngredient {

    private String name;
    private double quantity;
    private String unit;

    public RecipeIngredient(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    /** "3 eggs" style label, e.g. for the recipe detail screen. */
    public String getQuantityLabel() {
        return QuantityFormatter.format(quantity, unit);
    }

    @Override
    public String toString() {
        return getQuantityLabel() + " " + name;
    }
}
