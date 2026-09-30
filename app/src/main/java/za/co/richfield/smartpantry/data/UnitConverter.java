package za.co.richfield.smartpantry.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Converts between units that describe the same kind of measurement so that the strict-matching
 * rule can compare "0.5 kg" of chicken in the pantry with "500 g" of chicken in a recipe.
 *
 * <p>Four families are supported:</p>
 * <ul>
 *     <li>{@code g}  - mass (base unit: gram)</li>
 *     <li>{@code ml} - volume (base unit: millilitre)</li>
 *     <li>{@code tsp} - spoons (base unit: teaspoon; 1 tbsp = 3 tsp)</li>
 *     <li>{@code count} - countable pieces (each, slice, clove, piece)</li>
 * </ul>
 *
 * <p>Units outside a family (for example "pinch" or "tin") are only ever compared with an
 * identical unit, and a unit that the pantry and the recipe disagree on is reported as
 * "unit mismatch" rather than being silently treated as a match.</p>
 */
public final class UnitConverter {

    public static final String GRAM = "g";
    public static final String KILOGRAM = "kg";
    public static final String MILLILITRE = "ml";
    public static final String LITRE = "l";
    public static final String TEASPOON = "tsp";
    public static final String TABLESPOON = "tbsp";
    public static final String COUNT = "count";

    /** Canonical family names. */
    public static final String FAMILY_MASS = "mass";
    public static final String FAMILY_VOLUME = "volume";
    public static final String FAMILY_SPOON = "spoon";
    public static final String FAMILY_COUNT = "count";

    /** Maps every accepted unit spelling onto itself in canonical form. */
    private static final Map<String, String> CANONICAL = new HashMap<>();
    /** Maps a canonical unit onto the family it belongs to. */
    private static final Map<String, String> FAMILY = new HashMap<>();
    /** How many base units one of this unit contains (base units: g, ml, tsp, count). */
    private static final Map<String, Double> TO_BASE = new HashMap<>();

    static {
        // Mass
        canonical(GRAM, FAMILY_MASS, 1d, "g", "gram", "grams", "gr");
        canonical(KILOGRAM, FAMILY_MASS, 1000d, "kg", "kilo", "kilos", "kilogram", "kilograms");
        // Volume
        canonical(MILLILITRE, FAMILY_VOLUME, 1d, "ml", "millilitre", "millilitres", "milliliter", "milliliters");
        canonical(LITRE, FAMILY_VOLUME, 1000d, "l", "litre", "litres", "liter", "liters");
        // Spoons
        canonical(TEASPOON, FAMILY_SPOON, 1d, "tsp", "teaspoon", "teaspoons", "t");
        canonical(TABLESPOON, FAMILY_SPOON, 3d, "tbsp", "tablespoon", "tablespoons", "tbs", "tbl", "T");
        // Countable pieces
        canonical(COUNT, FAMILY_COUNT, 1d, "count", "each", "ea", "unit", "units", "no");
        canonical(COUNT, FAMILY_COUNT, 1d, "piece", "pieces");
        canonical(COUNT, FAMILY_COUNT, 1d, "slice", "slices");
        canonical(COUNT, FAMILY_COUNT, 1d, "clove", "cloves");
    }

    private UnitConverter() {
        // Utility class - never instantiated.
    }

    private static void canonical(String canonicalUnit, String family, double toBase, String... aliases) {
        CANONICAL.put(canonicalUnit, canonicalUnit);
        FAMILY.put(canonicalUnit, family);
        TO_BASE.put(canonicalUnit, toBase);
        for (String alias : aliases) {
            String key = alias.trim().toLowerCase(Locale.US);
            CANONICAL.put(key, canonicalUnit);
            FAMILY.put(key, family);
            TO_BASE.put(key, toBase);
        }
    }

    /** @return the canonical spelling of a unit, or a cleaned-up version of unknown units. */
    public static String canonicalUnit(String unit) {
        if (unit == null) {
            return "";
        }
        String key = unit.trim().toLowerCase(Locale.US);
        String mapped = CANONICAL.get(key);
        return mapped != null ? mapped : key;
    }

    /** @return the measurement family of a unit, or {@code null} for units we cannot convert. */
    public static String familyOf(String unit) {
        return FAMILY.get(canonicalUnit(unit));
    }

    /** @return true when both units belong to the same convertible family. */
    public static boolean isConvertible(String unitA, String unitB) {
        String familyA = familyOf(unitA);
        return familyA != null && familyA.equals(familyOf(unitB));
    }

    /**
     * Converts a quantity into the base unit of its family (g, ml, tsp or count).
     *
     * @return the converted quantity, or the original quantity when the unit is unknown
     */
    public static double toBaseUnit(double quantity, String unit) {
        Double factor = TO_BASE.get(canonicalUnit(unit));
        return factor == null ? quantity : quantity * factor;
    }

    /**
     * Converts a quantity from one unit into another unit of the same family, used to explain the
     * comparison on screen (for example how much is still needed).
     *
     * @return the converted quantity, or the original quantity when conversion is not possible
     */
    public static double convert(double quantity, String fromUnit, String toUnit) {
        if (!isConvertible(fromUnit, toUnit)) {
            return quantity;
        }
        double base = toBaseUnit(quantity, fromUnit);
        Double targetFactor = TO_BASE.get(canonicalUnit(toUnit));
        return targetFactor == null || targetFactor == 0d ? base : base / targetFactor;
    }

    /** All unit tokens the Add/Edit Ingredient spinner offers, in display order. */
    public static String[] spinnerUnits() {
        return new String[]{COUNT, GRAM, KILOGRAM, MILLILITRE, LITRE, TEASPOON, TABLESPOON,
                "piece", "slice", "clove", "pinch", "tin"};
    }

    /** Human readable name of a unit for the UI (the internal token "count" shows as "items"). */
    public static String displayUnit(String unit) {
        String canonical = canonicalUnit(unit);
        return COUNT.equals(canonical) ? "items" : canonical;
    }

    /** Handy for validation: the set of units a pantry item is allowed to use. */
    public static Set<String> knownUnits() {
        return Collections.unmodifiableSet(new HashSet<>(CANONICAL.keySet()));
    }
}
