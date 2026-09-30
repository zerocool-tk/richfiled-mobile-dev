package za.co.richfield.smartpantry;

import android.app.Application;

/**
 * Application class: the very first object Android creates for this process.
 *
 * <p>It does nothing expensive - it simply makes the {@code Application} context available to the
 * data layer through {@link #getInstance()} so that helpers can obtain a context without an
 * Activity leaking into a long-lived object.</p>
 */
public class PantryApp extends Application {

    private static PantryApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static PantryApp getInstance() {
        return instance;
    }
}
