package za.co.richfield.smartpantry.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Stores the user's preferences from the Settings screen.
 *
 * <p>Preferences are simple key/value pairs that never need to be queried or joined, so they are
 * kept in {@link SharedPreferences} rather than in SQLite. That keeps the relational schema
 * focused on the data that actually has relationships (pantry items and recipes).</p>
 */
public class SettingsManager {

    private static final String PREFS_NAME = "smart_pantry_settings";

    private static final String KEY_EXPIRY_ALERTS = "expiry_alerts_enabled";
    private static final String KEY_EXPIRY_WINDOW = "expiry_window_days";
    private static final String KEY_UNIT_SYSTEM = "preferred_unit_system";
    private static final String KEY_SHOW_ALMOST_THERE = "show_almost_there";

    /** Unit system values used by the Settings spinner. */
    public static final String UNIT_SYSTEM_METRIC = "Metric (g, kg, ml)";
    public static final String UNIT_SYSTEM_ORIGINAL = "As captured";
    public static final String UNIT_SYSTEM_IMPERIAL = "Imperial (oz, lb, cups)";

    private final SharedPreferences preferences;

    public SettingsManager(Context context) {
        this.preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** When true, the pantry list highlights items that are about to expire. */
    public boolean isExpiryAlertsEnabled() {
        return preferences.getBoolean(KEY_EXPIRY_ALERTS, true);
    }

    public void setExpiryAlertsEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_EXPIRY_ALERTS, enabled).apply();
    }

    /** How many days ahead counts as "expiring soon". Default: 3 days. */
    public int getExpiryWindowDays() {
        return preferences.getInt(KEY_EXPIRY_WINDOW, 3);
    }

    public void setExpiryWindowDays(int days) {
        preferences.edit().putInt(KEY_EXPIRY_WINDOW, days).apply();
    }

    /** Display preference for quantities. Matching always uses the stored metric values. */
    public String getUnitSystem() {
        return preferences.getString(KEY_UNIT_SYSTEM, UNIT_SYSTEM_METRIC);
    }

    public void setUnitSystem(String unitSystem) {
        preferences.edit().putString(KEY_UNIT_SYSTEM, unitSystem).apply();
    }

    /** When true, the "Almost There" list is shown next to the strict suggestions. */
    public boolean isAlmostThereEnabled() {
        return preferences.getBoolean(KEY_SHOW_ALMOST_THERE, true);
    }

    public void setAlmostThereEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_SHOW_ALMOST_THERE, enabled).apply();
    }

    /** Returns every setting to its default value. */
    public void resetToDefaults() {
        preferences.edit().clear().apply();
    }
}
