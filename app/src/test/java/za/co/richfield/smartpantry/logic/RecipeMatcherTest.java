package za.co.richfield.smartpantry.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import za.co.richfield.smartpantry.data.Ingredient;
import za.co.richfield.smartpantry.data.IngredientNameNormalizer;
import za.co.richfield.smartpantry.data.Recipe;
import za.co.richfield.smartpantry.data.RecipeIngredient;
import za.co.richfield.smartpantry.data.SeedData;
import za.co.richfield.smartpantry.data.UnitConverter;

/**
 * Unit tests for the strict-matching rule (Section 2.3 of the assignment brief).
 *
 * <p>The matching algorithm is deliberately plain Java with no Android dependencies, which is what
 * allows it to be tested here with {@code ./gradlew test} instead of on an emulator. These tests are
 * the fastest way to prove the rule behaves as the brief demands - especially that a recipe missing
 * even one ingredient never appears in the suggestions.</p>
 */
public class RecipeMatcherTest {

    /** Helper: builds a pantry item quickly. */
    private static Ingredient item(String name, double quantity, String unit) {
        return new Ingredient(name, quantity, unit, null);
    }

    /** Helper: a pantry holding exactly what a recipe needs, with optional removals. */
    private static List<Ingredient> fullPantryFor(Recipe recipe, String... omittedNames) {
        List<String> omitted = Arrays.asList(omittedNames);
        List<Ingredient> pantry = new ArrayList<>();
        for (RecipeIngredient required : recipe.getIngredients()) {
            if (!omitted.contains(required.getName())) {
                pantry.add(item(required.getName(), required.getQuantity(), required.getUnit()));
            }
        }
        return pantry;
    }

    private static Recipe recipeNamed(String name) {
        for (Recipe recipe : SeedData.recipes()) {
            if (recipe.getName().equals(name)) {
                return recipe;
            }
        }
        throw new IllegalArgumentException("Test recipe not found: " + name);
    }

    // -------------------------------------------------------------------------------------
    // The rule itself
    // -------------------------------------------------------------------------------------

    @Test
    public void recipeWithEverythingInPantryIsCookable() {
        Recipe omelette = recipeNamed("Cheese Omelette");
        assertTrue("A complete pantry should make the recipe cookable",
                RecipeMatcher.isCookable(omelette, fullPantryFor(omelette)));
    }

    @Test
    public void recipeMissingOneIngredientIsNotSuggested() {
        // This is the exact scenario in the brief: five ingredients needed, four available.
        Recipe recipe = recipeNamed("Chicken Curry");
        List<Ingredient> pantry = fullPantryFor(recipe, "Salt");

        assertFalse("A recipe missing one ingredient must NOT be cookable",
                RecipeMatcher.isCookable(recipe, pantry));
        assertTrue("It should be reported as one ingredient short instead",
                RecipeMatcher.match(recipe, pantry).getStatus() == RecipeMatch.Status.ALMOST_THERE);
    }

    @Test
    public void suggestedListExcludesPartialMatches() {
        Recipe recipe = recipeNamed("Creamy Tomato Pasta");
        List<Ingredient> pantry = fullPantryFor(recipe, "Garlic");

        List<RecipeMatch> suggestions = RecipeMatcher.suggestedRecipes(SeedData.recipes(), pantry);
        for (RecipeMatch match : suggestions) {
            assertTrue("Only fully satisfied recipes may be suggested",
                    match.isCookable());
            assertTrue("The partial recipe must not sneak into the suggestions",
                    !match.getRecipe().getName().equals("Creamy Tomato Pasta"));
        }
        assertTrue("A recipe one ingredient short belongs in the Almost There list",
                RecipeMatcher.almostThere(SeedData.recipes(), pantry).stream()
                        .anyMatch(m -> m.getRecipe().getName().equals("Creamy Tomato Pasta")));
    }

    @Test
    public void addingTheMissingIngredientMakesTheRecipeAppear() {
        // Mirrors the add/remove demonstration required in the video.
        Recipe recipe = recipeNamed("Cheese Omelette");
        List<Ingredient> pantry = fullPantryFor(recipe, "Butter");
        assertFalse(RecipeMatcher.isCookable(recipe, pantry));

        pantry.add(item("Butter", 250, UnitConverter.GRAM));
        assertTrue("Adding the missing ingredient should unlock the recipe",
                RecipeMatcher.isCookable(recipe, pantry));
    }

    @Test
    public void removingAnIngredientMakesTheRecipeDisappear() {
        Recipe recipe = recipeNamed("Cheese Omelette");
        List<Ingredient> pantry = fullPantryFor(recipe);
        assertTrue(RecipeMatcher.isCookable(recipe, pantry));

        pantry.removeIf(ingredient -> ingredient.getName().equals("Eggs"));
        assertFalse("Removing an ingredient should hide the recipe again",
                RecipeMatcher.isCookable(recipe, pantry));
    }

    // -------------------------------------------------------------------------------------
    // Quantities
    // -------------------------------------------------------------------------------------

    @Test
    public void quantityMustBeSufficient() {
        Recipe omelette = recipeNamed("Cheese Omelette");      // needs 3 eggs
        List<Ingredient> pantry = Arrays.asList(
                item("Eggs", 1, "count"),
                item("Cheese", 500, "g"),
                item("Butter", 500, "g"),
                item("Salt", 1, "kg"));
        assertFalse("One egg cannot satisfy a three-egg requirement",
                RecipeMatcher.isCookable(omelette, pantry));

        pantry = Arrays.asList(
                item("Eggs", 3, "count"),
                item("Cheese", 50, "g"),
                item("Butter", 10, "g"),
                item("Salt", 1, "tsp"));
        assertTrue("Exactly the required amount should be enough",
                RecipeMatcher.isCookable(omelette, pantry));
    }

    @Test
    public void kilogramInPantrySatisfiesGramRequirement() {
        Recipe recipe = recipeNamed("Chicken Curry");          // needs 0.5 kg chicken
        List<Ingredient> pantry = fullPantryFor(recipe, "Chicken");
        pantry.add(item("Chicken", 600, UnitConverter.GRAM));  // 600 g = 0.6 kg

        assertTrue("0.6 kg should satisfy a 0.5 kg requirement",
                RecipeMatcher.isCookable(recipe, pantry));
    }

    @Test
    public void tablespoonsAndTeaspoonsAreConverted() {
        Recipe recipe = recipeNamed("Chicken Curry");          // needs 2 tbsp curry powder
        List<Ingredient> pantry = fullPantryFor(recipe, "Curry Powder");
        pantry.add(item("Curry Powder", 7, UnitConverter.TEASPOON));   // 7 tsp = 2.33 tbsp

        assertTrue("7 tsp should satisfy a 2 tbsp requirement",
                RecipeMatcher.isCookable(recipe, pantry));
    }

    @Test
    public void litreInPantrySatisfiesMillilitreRequirement() {
        Recipe soup = recipeNamed("Creamy Butternut Soup");     // needs 0.5 l stock
        List<Ingredient> pantry = fullPantryFor(soup, "Vegetable Stock");
        pantry.add(item("Vegetable Stock", 1, UnitConverter.LITRE));

        assertTrue("1 litre should satisfy a 0.5 litre requirement",
                RecipeMatcher.isCookable(soup, pantry));
    }

    // -------------------------------------------------------------------------------------
    // Messy real-world input
    // -------------------------------------------------------------------------------------

    @Test
    public void pluralAndSingularNamesMatch() {
        assertTrue(IngredientNameNormalizer.sameIngredient("Tomatoes", "tomato"));
        assertTrue(IngredientNameNormalizer.sameIngredient("Eggs", "egg"));
        assertTrue(IngredientNameNormalizer.sameIngredient("Potatoes", "potato"));
        assertFalse(IngredientNameNormalizer.sameIngredient("Salt", "Sugar"));
    }

    @Test
    public void descriptiveWordsAreIgnored() {
        Recipe pasta = recipeNamed("Creamy Tomato Pasta");
        List<Ingredient> pantry = fullPantryFor(pasta, "Tomatoes", "Onion", "Garlic");
        // The user typed the names the way a shop label might read.
        pantry.add(item("Fresh Chopped Tomatoes", 5, "count"));
        pantry.add(item("Red Onions", 2, "count"));
        pantry.add(item("Garlic Cloves", 4, "count"));

        assertTrue("Descriptive words should not prevent a match",
                RecipeMatcher.isCookable(pasta, pantry));
    }

    @Test
    public void synonymsAreRecognised() {
        assertTrue(IngredientNameNormalizer.sameIngredient("Aubergine", "brinjal"));
        assertTrue(IngredientNameNormalizer.sameIngredient("Coriander", "cilantro"));
        assertTrue(IngredientNameNormalizer.sameIngredient("Cooking Oil", "oil"));
        assertTrue(IngredientNameNormalizer.sameIngredient("Mayo", "mayonnaise"));
    }

    @Test
    public void chilliAndChilliFlakesStaySeparate() {
        // A fresh chilli must not satisfy a recipe that specifically asks for chilli flakes.
        assertFalse(IngredientNameNormalizer.sameIngredient("chilli", "chilli flakes"));
    }

    @Test
    public void unitMismatchOnUnconvertibleUnitsIsAssumedAvailable() {
        // A recipe asking for "1 tin" of tomatoes cannot be compared with 400 g, so the app accepts
        // the presence of the ingredient instead of wrongly blocking the whole recipe.
        Recipe dummy = new Recipe(1L, "Test", "Test",
                "1. Do the thing.");
        dummy.addIngredient(new RecipeIngredient("Tomatoes", 1, "tin"));
        List<Ingredient> pantry = Arrays.asList(item("Tomatoes", 400, "g"));

        assertTrue(RecipeMatcher.isCookable(dummy, pantry));
        assertFalse("The assumption should be reported to the user",
                RecipeMatcher.match(dummy, pantry).getNotes().isEmpty());
    }

    // -------------------------------------------------------------------------------------
    // Edge cases
    // -------------------------------------------------------------------------------------

    @Test
    public void emptyPantrySuggestsNothingAndDoesNotCrash() {
        assertTrue(RecipeMatcher.suggestedRecipes(SeedData.recipes(), new ArrayList<>()).isEmpty());
    }

    @Test
    public void nullPantryIsHandled() {
        assertTrue(RecipeMatcher.suggestedRecipes(SeedData.recipes(), null).isEmpty());
        assertFalse(RecipeMatcher.isCookable(SeedData.recipes().get(0), null));
    }

    @Test
    public void duplicatePantryRowsAreCombinedByBestMatch() {
        // The user added eggs twice; the larger amount must be used.
        Recipe omelette = recipeNamed("Cheese Omelette");
        List<Ingredient> pantry = Arrays.asList(
                item("Eggs", 1, "count"),
                item("Eggs", 6, "count"),
                item("Cheese", 50, "g"),
                item("Butter", 10, "g"),
                item("Salt", 1, "tsp"));

        assertTrue("The best matching pantry row should be used",
                RecipeMatcher.isCookable(omelette, pantry));
    }

    @Test
    public void seedLibraryMeetsTheBriefRequirements() {
        List<Recipe> recipes = SeedData.recipes();
        assertTrue("The brief requires at least 15 recipes, found " + recipes.size(),
                recipes.size() >= 15);
        for (Recipe recipe : recipes) {
            assertFalse("Every recipe needs ingredients: " + recipe.getName(),
                    recipe.getIngredients().isEmpty());
            assertTrue("Every recipe needs method steps: " + recipe.getName(),
                    recipe.getSteps() != null && !recipe.getSteps().trim().isEmpty());
        }
    }
}
