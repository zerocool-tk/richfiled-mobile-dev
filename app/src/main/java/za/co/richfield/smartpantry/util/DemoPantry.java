package za.co.richfield.smartpantry.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import za.co.richfield.smartpantry.data.Ingredient;

/**
 * A small "demo pantry" that the Settings screen can insert with one tap.
 *
 * <p>It exists so that the strict-matching rule can be demonstrated quickly (and so a marker can
 * try the app without typing twelve ingredients first). Two of the items deliberately have expiry
 * dates close to today so that the expiring-soon alert is visible immediately.</p>
 */
public final class DemoPantry {

    private DemoPantry() {
        // Utility class - never instantiated.
    }

    /** @return twelve common pantry items with realistic quantities. */
    public static List<Ingredient> items() {
        List<Ingredient> items = new ArrayList<>();
        items.add(new Ingredient("Eggs", 12, "count", inDays(14)));
        items.add(new Ingredient("Bread", 8, "slice", inDays(2)));
        items.add(new Ingredient("Cheese", 400, "g", inDays(10)));
        items.add(new Ingredient("Butter", 500, "g", inDays(30)));
        items.add(new Ingredient("Milk", 2, "l", inDays(4)));
        items.add(new Ingredient("Tomatoes", 6, "count", inDays(3)));
        items.add(new Ingredient("Onion", 5, "count", inDays(21)));
        items.add(new Ingredient("Potatoes", 8, "count", inDays(28)));
        items.add(new Ingredient("Rice", 1, "kg", null));
        items.add(new Ingredient("Pasta", 500, "g", null));
        items.add(new Ingredient("Salt", 1, "kg", null));
        items.add(new Ingredient("Oil", 750, "ml", null));
        return items;
    }

    /** ISO date {@code days} from today, used for the demo expiry dates. */
    private static String inDays(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return DateUtils.formatIso(calendar);
    }
}
