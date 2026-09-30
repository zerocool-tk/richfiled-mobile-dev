package za.co.richfield.smartpantry.data;

import java.util.Locale;

/**
 * Small helper that turns a quantity plus unit into a human readable label.
 *
 * <p>Kept in one place so that the pantry list, the recipe detail screen and the suggestion list
 * all render quantities identically ("3", "2.5 kg", "1 tbsp").</p>
 */
public final class QuantityFormatter {

    private QuantityFormatter() {
        // Utility class - never instantiated.
    }

    /**
     * Builds a display label for a quantity.
     *
     * @param quantity e.g. 2.5
     * @param unit     a unit token such as "kg", "g", "ml", "tbsp" or {@code "count"}
     * @return e.g. "2.5 kg"; the internal token "count" renders as a bare number, e.g. "3" for 3 eggs
     */
    public static String format(double quantity, String unit) {
        String number = formatNumber(quantity);
        if (unit == null || unit.trim().isEmpty() || UnitConverter.COUNT.equalsIgnoreCase(unit.trim())) {
            return number;
        }
        return number + " " + unit.trim().toLowerCase(Locale.US);
    }

    /** Formats 2.0 as "2" and 2.5 as "2.5" so the UI never shows a trailing ".0". */
    public static String formatNumber(double value) {
        if (Math.abs(value - Math.rint(value)) < 1e-9) {
            return String.valueOf((long) Math.rint(value));
        }
        String text = String.format(Locale.US, "%.2f", value);
        while (text.endsWith("0")) {
            text = text.substring(0, text.length() - 1);
        }
        if (text.endsWith(".")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }
}
