package za.co.richfield.smartpantry.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The recipe collection that is pre-loaded into the database the first time the app runs
 * (see {@link DatabaseHelper#onCreate}).
 *
 * <p>The brief requires 15-20 recipes; 23 are provided so that the pantry can be filled in many
 * different combinations and so that the strict-matching rule produces a useful spread of
 * "can cook now", "almost there" and "not yet" results. The list deliberately mixes:</p>
 * <ul>
 *     <li>South African favourites (Chakalaka, Beef Stew, Butternut Soup)</li>
 *     <li>cheap student staples (Egg Fried Rice, Grilled Cheese Sandwich)</li>
 *     <li>recipes that use different measurement families, so unit conversion is exercised</li>
 * </ul>
 */
public final class SeedData {

    private SeedData() {
        // Utility class - never instantiated.
    }

    /** Builds a fresh copy of the seed recipes (never shared, so callers may mutate them). */
    public static List<Recipe> recipes() {
        List<Recipe> recipes = new ArrayList<>();

        recipes.add(recipe("Cheese Omelette", "Breakfast",
                "1. Beat the eggs with the salt in a bowl until light and foamy.\n"
                        + "2. Melt the butter in a non-stick pan over medium heat.\n"
                        + "3. Pour in the eggs and cook without stirring for 1 minute.\n"
                        + "4. Sprinkle the grated cheese over one half and fold the omelette over.\n"
                        + "5. Slide onto a plate and serve immediately.",
                ing("Eggs", 3, "count"), ing("Cheese", 50, "g"),
                ing("Butter", 10, "g"), ing("Salt", 1, "tsp")));

        recipes.add(recipe("Scrambled Eggs on Toast", "Breakfast",
                "1. Whisk the eggs with salt and pepper.\n"
                        + "2. Melt the butter in a pan and pour in the eggs.\n"
                        + "3. Stir gently until softly scrambled, then take the pan off the heat.\n"
                        + "4. Toast the bread and pile the eggs on top.",
                ing("Eggs", 2, "count"), ing("Bread", 2, "slice"),
                ing("Butter", 10, "g"), ing("Salt", 1, "tsp"), ing("Black Pepper", 1, "tsp")));

        recipes.add(recipe("Tomato and Onion Scramble", "Breakfast",
                "1. Chop the onion and tomatoes.\n"
                        + "2. Fry the onion in the oil until soft, then add the tomatoes and cook for 3 minutes.\n"
                        + "3. Beat the eggs with the salt and pour into the pan.\n"
                        + "4. Stir until the eggs are just set, then serve.",
                ing("Eggs", 3, "count"), ing("Tomatoes", 2, "count"),
                ing("Onion", 1, "count"), ing("Oil", 1, "tbsp"), ing("Salt", 1, "tsp")));

        recipes.add(recipe("Grilled Cheese Sandwich", "Light Meal",
                "1. Butter one side of each slice of bread.\n"
                        + "2. Put the cheese between the unbuttered sides.\n"
                        + "3. Fry in a pan over medium heat for 3 minutes per side until golden and melting.",
                ing("Bread", 2, "slice"), ing("Cheese", 50, "g"), ing("Butter", 15, "g")));

        recipes.add(recipe("Chicken Mayo Sandwich", "Light Meal",
                "1. Cook and shred the chicken, then allow it to cool.\n"
                        + "2. Mix the chicken with the mayonnaise and black pepper.\n"
                        + "3. Spoon onto the bread and close the sandwiches.",
                ing("Bread", 4, "slice"), ing("Chicken", 250, "g"),
                ing("Mayonnaise", 3, "tbsp"), ing("Black Pepper", 1, "tsp")));

        recipes.add(recipe("Creamy Tomato Pasta", "Pasta",
                "1. Boil the pasta in salted water until al dente, then drain.\n"
                        + "2. Fry the onion and garlic in the olive oil until soft.\n"
                        + "3. Add the chopped tomatoes and simmer for 10 minutes.\n"
                        + "4. Stir the sauce through the pasta and season with salt.",
                ing("Pasta", 250, "g"), ing("Tomatoes", 4, "count"), ing("Onion", 1, "count"),
                ing("Garlic", 3, "clove"), ing("Olive Oil", 2, "tbsp"), ing("Salt", 1, "tsp")));

        recipes.add(recipe("Garlic Butter Pasta", "Pasta",
                "1. Boil the pasta until al dente and keep 2 tbsp of the cooking water.\n"
                        + "2. Melt the butter and fry the crushed garlic for 30 seconds.\n"
                        + "3. Toss the pasta through the garlic butter with a splash of pasta water.\n"
                        + "4. Season with salt and black pepper.",
                ing("Pasta", 250, "g"), ing("Garlic", 4, "clove"),
                ing("Butter", 40, "g"), ing("Salt", 1, "tsp"), ing("Black Pepper", 1, "tsp")));

        recipes.add(recipe("Spaghetti Aglio e Olio", "Pasta",
                "1. Cook the spaghetti in salted water until al dente.\n"
                        + "2. Gently fry the sliced garlic and chilli flakes in the olive oil.\n"
                        + "3. Add the drained spaghetti to the pan with a little pasta water and toss well.\n"
                        + "4. Serve straight away.",
                ing("Spaghetti", 250, "g"), ing("Garlic", 4, "clove"),
                ing("Olive Oil", 4, "tbsp"), ing("Chilli Flakes", 1, "tsp"), ing("Salt", 1, "tsp")));

        recipes.add(recipe("Macaroni and Cheese", "Pasta",
                "1. Boil the macaroni until just tender and drain.\n"
                        + "2. Melt the butter, stir in the flour and cook for 1 minute.\n"
                        + "3. Slowly whisk in the milk until the sauce thickens.\n"
                        + "4. Stir in most of the cheese, season with salt and mix through the macaroni.\n"
                        + "5. Top with the rest of the cheese and grill until bubbling.",
                ing("Macaroni", 250, "g"), ing("Milk", 200, "ml"), ing("Cheese", 150, "g"),
                ing("Butter", 30, "g"), ing("Flour", 2, "tbsp"), ing("Salt", 1, "tsp")));

        recipes.add(recipe("Tuna Pasta Bake", "Pasta",
                "1. Cook the pasta until just tender and drain.\n"
                        + "2. Make a white sauce by melting the butter, adding the flour and whisking in the milk.\n"
                        + "3. Flake in the tuna and stir in half the cheese.\n"
                        + "4. Mix the pasta through the sauce, top with the remaining cheese and bake at 200 C for 20 minutes.",
                ing("Pasta", 250, "g"), ing("Tuna", 200, "g"), ing("Milk", 200, "ml"),
                ing("Cheese", 100, "g"), ing("Flour", 2, "tbsp"), ing("Butter", 30, "g")));

        recipes.add(recipe("Egg Fried Rice", "Rice",
                "1. Cook the rice and spread it out to cool.\n"
                        + "2. Heat the oil in a wok and scramble the eggs, then set them aside.\n"
                        + "3. Fry the onion until soft, add the rice and soy sauce and stir-fry for 3 minutes.\n"
                        + "4. Return the egg to the pan, mix through and serve.",
                ing("Rice", 300, "g"), ing("Eggs", 3, "count"), ing("Onion", 1, "count"),
                ing("Soy Sauce", 2, "tbsp"), ing("Oil", 2, "tbsp")));

        recipes.add(recipe("Vegetable Fried Rice", "Rice",
                "1. Cook the rice and allow it to cool completely.\n"
                        + "2. Stir-fry the chopped carrots, peas and onion in the oil for 4 minutes.\n"
                        + "3. Push the vegetables aside and scramble the eggs in the pan.\n"
                        + "4. Add the rice and soy sauce and toss everything together over high heat.",
                ing("Rice", 300, "g"), ing("Carrots", 2, "count"), ing("Peas", 100, "g"),
                ing("Onion", 1, "count"), ing("Soy Sauce", 2, "tbsp"), ing("Oil", 2, "tbsp"),
                ing("Eggs", 2, "count")));

        recipes.add(recipe("Chicken and Rice Bowl", "Rice",
                "1. Boil the rice in salted water until tender.\n"
                        + "2. Cube the chicken and fry it in the oil until golden and cooked through.\n"
                        + "3. Add the sliced onion and grated carrot and cook for 4 minutes.\n"
                        + "4. Serve the chicken and vegetables over the rice.",
                ing("Chicken", 300, "g"), ing("Rice", 250, "g"), ing("Onion", 1, "count"),
                ing("Carrots", 1, "count"), ing("Oil", 2, "tbsp"), ing("Salt", 1, "tsp")));

        recipes.add(recipe("Chicken Curry", "Chicken",
                "1. Fry the chopped onions in the oil until golden.\n"
                        + "2. Add the garlic and curry powder and cook for 1 minute until fragrant.\n"
                        + "3. Add the chicken pieces and brown them all over.\n"
                        + "4. Stir in the chopped tomatoes, season with salt and simmer for 25 minutes.\n"
                        + "5. Serve with rice, bread or roti.",
                ing("Chicken", 0.5, UnitConverter.KILOGRAM), ing("Onion", 2, "count"),
                ing("Tomatoes", 3, "count"), ing("Garlic", 4, "clove"),
                ing("Curry Powder", 2, "tbsp"), ing("Oil", 3, "tbsp"), ing("Salt", 1, "tsp")));

        recipes.add(recipe("Chicken Stir-Fry", "Chicken",
                "1. Slice the chicken into thin strips.\n"
                        + "2. Heat the oil in a wok and fry the garlic for 30 seconds.\n"
                        + "3. Add the chicken and stir-fry until sealed, about 4 minutes.\n"
                        + "4. Add the mixed vegetables and soy sauce and stir-fry for a further 5 minutes.",
                ing("Chicken", 400, "g"), ing("Mixed Vegetables", 300, "g"),
                ing("Soy Sauce", 3, "tbsp"), ing("Garlic", 3, "clove"), ing("Oil", 2, "tbsp")));

        recipes.add(recipe("Beef Stew", "Beef",
                "1. Toss the beef cubes in the flour.\n"
                        + "2. Brown the beef in the oil in batches, then set aside.\n"
                        + "3. Fry the chopped onion and carrots, then return the beef to the pot.\n"
                        + "4. Add the stock, season with salt and simmer covered for 90 minutes.\n"
                        + "5. Add the cubed potatoes and cook for a further 30 minutes until tender.",
                ing("Beef", 0.5, UnitConverter.KILOGRAM), ing("Potatoes", 4, "count"),
                ing("Carrots", 3, "count"), ing("Onion", 2, "count"), ing("Beef Stock", 500, "ml"),
                ing("Flour", 2, "tbsp"), ing("Oil", 2, "tbsp"), ing("Salt", 1, "tsp")));

        recipes.add(recipe("Beef Burger Patties", "Beef",
                "1. Soak the bread in a little water and squeeze it out.\n"
                        + "2. Mix the beef, grated onion, bread and egg together and season well.\n"
                        + "3. Shape into four patties and chill for 20 minutes.\n"
                        + "4. Fry or grill the patties for 4 minutes per side.",
                ing("Beef", 500, "g"), ing("Onion", 1, "count"), ing("Bread", 2, "slice"),
                ing("Eggs", 1, "count"), ing("Salt", 1, "tsp"), ing("Black Pepper", 1, "tsp")));

        recipes.add(recipe("Shepherd's Pie", "Beef",
                "1. Peel and boil the potatoes, then mash with the milk and butter.\n"
                        + "2. Brown the beef with the chopped onion and season with salt.\n"
                        + "3. Stir the peas through the mince and spoon into an oven dish.\n"
                        + "4. Top with the mash and bake at 200 C for 25 minutes until golden.",
                ing("Beef", 500, "g"), ing("Potatoes", 6, "count"), ing("Milk", 100, "ml"),
                ing("Butter", 40, "g"), ing("Onion", 1, "count"), ing("Peas", 150, "g"),
                ing("Salt", 1, "tsp")));

        recipes.add(recipe("Chakalaka", "Vegetarian",
                "1. Fry the chopped onions in the oil until soft.\n"
                        + "2. Add the grated carrots and curry powder and cook for 5 minutes.\n"
                        + "3. Stir in the chopped tomatoes and cook until they break down.\n"
                        + "4. Add the baked beans and salt, then simmer for 10 minutes.",
                ing("Carrots", 3, "count"), ing("Onion", 2, "count"), ing("Tomatoes", 3, "count"),
                ing("Baked Beans", 400, "g"), ing("Curry Powder", 1, "tbsp"),
                ing("Oil", 2, "tbsp"), ing("Salt", 1, "tsp")));

        recipes.add(recipe("Lentil Curry (Dhal)", "Vegetarian",
                "1. Rinse the lentils and boil them in 1 litre of water until soft, about 25 minutes.\n"
                        + "2. Fry the onion and garlic in the oil until golden.\n"
                        + "3. Add the curry powder and chopped tomatoes and cook for 5 minutes.\n"
                        + "4. Stir the fried mixture into the lentils, season with salt and simmer for 10 minutes.",
                ing("Lentils", 250, "g"), ing("Onion", 1, "count"), ing("Tomatoes", 2, "count"),
                ing("Garlic", 3, "clove"), ing("Curry Powder", 2, "tbsp"),
                ing("Oil", 2, "tbsp"), ing("Salt", 1, "tsp")));

        recipes.add(recipe("Creamy Butternut Soup", "Soup",
                "1. Peel and cube the butternut, then chop the onion.\n"
                        + "2. Fry the onion and garlic in the butter for 3 minutes.\n"
                        + "3. Add the butternut and stock and simmer for 25 minutes until soft.\n"
                        + "4. Blend until smooth, stir in the cream and warm through without boiling.",
                ing("Butternut", 800, "g"), ing("Onion", 1, "count"), ing("Garlic", 3, "clove"),
                ing("Cream", 200, "ml"), ing("Vegetable Stock", 0.5, UnitConverter.LITRE),
                ing("Butter", 30, "g")));

        recipes.add(recipe("Vegetable Soup", "Soup",
                "1. Chop the carrots, potatoes, onion and tomatoes.\n"
                        + "2. Fry the onion in a large pot for 3 minutes.\n"
                        + "3. Add the remaining vegetables and the stock, then season with salt.\n"
                        + "4. Simmer for 35 minutes until all the vegetables are tender.",
                ing("Carrots", 3, "count"), ing("Potatoes", 3, "count"), ing("Onion", 1, "count"),
                ing("Vegetable Stock", 1, UnitConverter.LITRE), ing("Tomatoes", 2, "count"),
                ing("Salt", 1, "tsp")));

        recipes.add(recipe("Creamy Mushroom Pasta", "Pasta",
                "1. Boil the pasta until al dente and drain.\n"
                        + "2. Fry the sliced mushrooms and garlic in the butter for 5 minutes.\n"
                        + "3. Pour in the cream, season with salt and pepper and simmer for 3 minutes.\n"
                        + "4. Toss the sauce through the pasta and serve.",
                ing("Pasta", 250, "g"), ing("Mushrooms", 250, "g"), ing("Garlic", 2, "clove"),
                ing("Cream", 200, "ml"), ing("Butter", 30, "g"), ing("Salt", 1, "tsp"),
                ing("Black Pepper", 1, "tsp")));

        return recipes;
    }

    /**
     * Every ingredient name that appears in the seed recipes plus a set of common household
     * staples. Used to offer autocomplete suggestions on the Add/Edit Ingredient screen so that
     * the user's spelling has a good chance of matching the recipe spelling.
     */
    public static List<String> ingredientCatalogue() {
        Set<String> names = new LinkedHashSet<>();
        for (Recipe recipe : recipes()) {
            for (RecipeIngredient ingredient : recipe.getIngredients()) {
                names.add(ingredient.getName());
            }
        }
        names.addAll(Arrays.asList(
                "Baked Beans", "Bananas", "Beef Mince", "Brown Bread", "Cabbage", "Canned Tomatoes",
                "Cauliflower", "Celery", "Chicken Stock", "Chilli Flakes", "Cinnamon", "Coriander",
                "Cucumber", "Custard", "Egg Noodles", "Frozen Peas", "Green Beans", "Green Pepper",
                "Honey", "Iceberg Lettuce", "Jam", "Lemon", "Lettuce", "Maize Meal", "Mushrooms",
                "Mustard", "Noodles", "Oats", "Oats Porridge", "Olive Oil", "Orange", "Oregano",
                "Paprika", "Peanut Butter", "Pork Chops", "Red Lentils", "Samp", "Sardines",
                "Sour Cream", "Spinach", "Stock Cube", "Sunflower Seeds", "Sweet Potato",
                "Tomato Sauce", "Vinegar", "Worcestershire Sauce", "Yoghurt", "Zucchini"));
        return new ArrayList<>(names);
    }

    // ---------------------------------------------------------------------------------------
    // Small builders that keep the list above readable.
    // ---------------------------------------------------------------------------------------

    private static Recipe recipe(String name, String category, String steps, RecipeIngredient... ingredients) {
        Recipe recipe = new Recipe(0L, name, category, steps);
        for (RecipeIngredient ingredient : ingredients) {
            recipe.addIngredient(ingredient);
        }
        return recipe;
    }

    private static RecipeIngredient ing(String name, double quantity, String unit) {
        return new RecipeIngredient(name, quantity, unit);
    }
}
