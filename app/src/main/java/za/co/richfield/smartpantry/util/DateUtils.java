package za.co.richfield.smartpantry.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Expiry date helpers.
 *
 * <p>Expiry dates are stored in SQLite as ISO text ({@code yyyy-MM-dd}) because that format sorts
 * correctly as a string and stays readable when the database is inspected with a tool such as
 * DB Browser for SQLite.</p>
 *
 * <p>{@link SimpleDateFormat} with the {@link Locale#US} locale is used on purpose rather than
 * {@code java.time}: the app supports API 24 and {@code java.time} needs API 26 unless library
 * desugaring is switched on.</p>
 */
public final class DateUtils {

    public static final String ISO_PATTERN = "yyyy-MM-dd";
    private static final String DISPLAY_PATTERN = "dd MMM yyyy";

    private DateUtils() {
        // Utility class - never instantiated.
    }

    /** @return today's date as {@code yyyy-MM-dd}. */
    public static String todayIso() {
        return formatIso(Calendar.getInstance());
    }

    /** Formats a calendar value as {@code yyyy-MM-dd}. */
    public static String formatIso(Calendar calendar) {
        return new SimpleDateFormat(ISO_PATTERN, Locale.US).format(calendar.getTime());
    }

    /** Formats a calendar value for display, for example "02 Oct 2026". */
    public static String formatForDisplay(Calendar calendar) {
        return new SimpleDateFormat(DISPLAY_PATTERN, Locale.US).format(calendar.getTime());
    }

    /** Re-formats a stored ISO date for display; returns the original text when it cannot be parsed. */
    public static String toDisplayFormat(String isoDate) {
        Calendar calendar = parseIso(isoDate);
        return calendar == null ? isoDate : formatForDisplay(calendar);
    }

    /** @return the calendar for an ISO date string, or {@code null} when it is not a valid date. */
    public static Calendar parseIso(String isoDate) {
        if (isoDate == null || isoDate.trim().isEmpty()) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat(ISO_PATTERN, Locale.US);
        format.setLenient(false);          // reject impossible dates such as 2026-02-30
        try {
            Date date = format.parse(isoDate.trim());
            if (date == null) {
                return null;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar;
        } catch (ParseException e) {
            return null;
        }
    }

    /** @return true when the text is a real calendar date in {@code yyyy-MM-dd} form. */
    public static boolean isValidIsoDate(String isoDate) {
        return parseIso(isoDate) != null;
    }

    /**
     * Days from today until the given date.
     *
     * @return 0 for today, a positive number for a future date, a negative number for a past date,
     *         and {@link Integer#MAX_VALUE} when the date is missing or unreadable
     */
    public static int daysUntil(String isoDate) {
        Calendar target = parseIso(isoDate);
        if (target == null) {
            return Integer.MAX_VALUE;
        }
        return daysBetween(startOfToday(), startOfDay(target));
    }

    /** @return true when the item expires within {@code windowDays} days (or has already expired). */
    public static boolean isExpiringWithin(String isoDate, int windowDays) {
        int days = daysUntil(isoDate);
        return days != Integer.MAX_VALUE && days <= windowDays;
    }

    /** @return true when the date is before today. */
    public static boolean isExpired(String isoDate) {
        int days = daysUntil(isoDate);
        return days != Integer.MAX_VALUE && days < 0;
    }

    private static Calendar startOfToday() {
        return startOfDay(Calendar.getInstance());
    }

    private static Calendar startOfDay(Calendar calendar) {
        Calendar copy = (Calendar) calendar.clone();
        copy.set(Calendar.HOUR_OF_DAY, 0);
        copy.set(Calendar.MINUTE, 0);
        copy.set(Calendar.SECOND, 0);
        copy.set(Calendar.MILLISECOND, 0);
        return copy;
    }

    /** Whole days between two dates (b is expected to be later than a for a positive result). */
    private static int daysBetween(Calendar from, Calendar to) {
        long millis = to.getTimeInMillis() - from.getTimeInMillis();
        return (int) Math.round(millis / (double) (24L * 60L * 60L * 1000L));
    }
}
