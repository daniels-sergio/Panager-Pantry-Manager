package com.example.smartpantrymanager.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Saves and loads the choices made on the Settings screen: expiry alerts on or
 * off, Metric or Imperial units, and dark mode on or off.
 *
 * These are stored with SharedPreferences, which is Android's built-in way to
 * keep small bits of data (like on/off switches) on the phone. It works like a
 * labelled drawer: each value is saved under a name (a "key") and read back
 * using the same name. The values stay saved after the app closes.
 */
public final class SettingsManager {

    // The name of the storage file, and the label each setting is saved under.
    // Keeping them in one place means a typo can't make a setting save under one
    // name and load from another.
    private static final String PREFS_NAME = "panager_settings";
    private static final String KEY_EXPIRY_ALERTS_ENABLED = "expiry_alerts_enabled";
    private static final String KEY_PREFERRED_UNIT = "preferred_unit";
    private static final String KEY_DARK_MODE_ENABLED = "dark_mode_enabled";

    // The two unit choices, shared with the Settings screen so both use the same spelling.
    public static final String UNIT_METRIC = "Metric";
    public static final String UNIT_IMPERIAL = "Imperial";

    private final SharedPreferences prefs;

    public SettingsManager(Context context) {
        // MODE_PRIVATE means only this app can read these settings.
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Expiry alerts are ON the first time the app runs (the "true" is the default
    // used when nothing has been saved yet).
    public boolean isExpiryAlertsEnabled() {
        return prefs.getBoolean(KEY_EXPIRY_ALERTS_ENABLED, true);
    }

    // apply() saves in the background, so the switch responds instantly.
    public void setExpiryAlertsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_EXPIRY_ALERTS_ENABLED, enabled).apply();
    }

    // Metric is the default until the user picks something else.
    public String getPreferredUnit() {
        return prefs.getString(KEY_PREFERRED_UNIT, UNIT_METRIC);
    }

    public void setPreferredUnit(String unit) {
        prefs.edit().putString(KEY_PREFERRED_UNIT, unit).apply();
    }

    // Dark mode is off by default.
    public boolean isDarkModeEnabled() {
        return prefs.getBoolean(KEY_DARK_MODE_ENABLED, false);
    }

    public void setDarkModeEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_DARK_MODE_ENABLED, enabled).apply();
    }
}
