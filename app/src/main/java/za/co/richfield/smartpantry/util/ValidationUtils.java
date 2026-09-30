package za.co.richfield.smartpantry.util;

import android.content.Context;

import java.util.Locale;

import za.co.richfield.smartpantry.R;

/**
 * Input validation for the Add/Edit Ingredient form.
 *
 * <p>Every rule returns {@code null} when the input is acceptable, or a ready-to-display message
 * when it is not, so the Activity can hand the result straight to
 * {@code TextInputLayout.setError(...)}. Keeping the rules here instead of inside the Activity means
 * the form logic stays readable and the rules can be reviewed in one place.</p>
 *
 * <p>A {@link Context} is passed in so that every message comes from {@code strings.xml} rather than
 * being hard-coded in Java.</p>
 */
public final class ValidationUtils {

    /** Guards against accidental input such as 999999999. */
    public static final double MAX_QUANTITY = 100000d;
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 40;

    private ValidationUtils() {
        // Utility class - never instantiated.
    }

    /**
     * Validates an ingredient name.
     *
     * @return an error message, or {@code null} when the name is valid
     */
    public static String validateName(Context context, String name) {
        if (name == null || name.trim().isEmpty()) {
            return context.getString(R.string.error_name_required);
        }
        String trimmed = name.trim();
        if (trimmed.length() < MIN_NAME_LENGTH) {
            return context.getString(R.string.error_name_too_short, MIN_NAME_LENGTH);
        }
        if (trimmed.length() > MAX_NAME_LENGTH) {
            return context.getString(R.string.error_name_too_long, MAX_NAME_LENGTH);
        }
        // Letters, digits, spaces, apostrophes and hyphens only - blocks accidental symbol soup
        // while still allowing names such as "Shepherd's pie" or "Semi-skimmed milk".
        if (!trimmed.matches("[\\p{L}\\p{N}][\\p{L}\\p{N} \\-']*")) {
            return context.getString(R.string.error_name_not_letters);
        }
        return null;
    }

    /**
     * Validates a quantity.
     *
     * @return an error message, or {@code null} when the value is valid
     */
    public static String validateQuantity(Context context, String rawQuantity) {
        if (rawQuantity == null || rawQuantity.trim().isEmpty()) {
            return context.getString(R.string.error_quantity_required);
        }
        double value;
        try {
            value = Double.parseDouble(rawQuantity.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return context.getString(R.string.error_quantity_invalid);
        }
        if (Double.isNaN(value) || Double.isInfinite(value) || value <= 0d) {
            return context.getString(R.string.error_quantity_invalid);
        }
        if (value > MAX_QUANTITY) {
            return context.getString(R.string.error_quantity_too_large, (long) MAX_QUANTITY);
        }
        return null;
    }

    /** Parses a quantity that has already passed {@link #validateQuantity(Context, String)}. */
    public static double parseQuantity(String rawQuantity) {
        return Double.parseDouble(rawQuantity.trim().replace(',', '.'));
    }

    /**
     * Validates an optional expiry date.
     *
     * @return an error message, or {@code null} when the date is blank or a real ISO date
     */
    public static String validateExpiryDate(Context context, String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) {
            return null;                     // The expiry date is optional.
        }
        String trimmed = rawDate.trim();
        if (!trimmed.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return context.getString(R.string.error_expiry_format);
        }
        if (!DateUtils.isValidIsoDate(trimmed)) {
            return context.getString(R.string.error_expiry_invalid_date);
        }
        return null;
    }

    /** @return an error message, or {@code null} when a unit has been chosen. */
    public static String validateUnit(Context context, String unit) {
        if (unit == null || unit.trim().isEmpty()) {
            return context.getString(R.string.error_unit_required);
        }
        return null;
    }

    /** Normalises typed unit text to lower case without surrounding spaces. */
    public static String normaliseUnit(String unit) {
        return unit == null ? "" : unit.trim().toLowerCase(Locale.US);
    }
}
