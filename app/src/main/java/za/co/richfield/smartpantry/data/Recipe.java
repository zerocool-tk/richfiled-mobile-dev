package za.co.richfield.smartpantry.data;

import java.util.ArrayList;
import java.util.List;

/**
 * A recipe together with all of the ingredients it requires (rows of the "recipe" table joined
 * with its "recipe_ingredient" rows).
 */
public class Recipe {

    private long id;
    private String name;
    private String category;
    private String steps;
    private final List<RecipeIngredient> ingredients = new ArrayList<>();

    public Recipe(long id, String name, String category, String steps) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.steps = steps;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getSteps() {
        return steps;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    public void addIngredient(RecipeIngredient ingredient) {
        ingredients.add(ingredient);
    }

    /** Number of ingredients this recipe needs - shown on the suggestions list. */
    public int getIngredientCount() {
        return ingredients.size();
    }

    @Override
    public String toString() {
        return name + " (" + getIngredientCount() + " ingredients)";
    }
}
