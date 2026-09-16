package com.example.smartpantrymanager.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Thin wrapper around SharedPreferences for the app's Settings screen:
 * whether "expiring soon" alerts are shown, and the user's preferred unit system.
 */
public final class SettingsManager {

    private static final String PREFS_NAME = "panager_settings";
    private static final String KEY_EXPIRY_ALERTS_ENABLED = "expiry_alerts_enabled";
    private static final String KEY_PREFERRED_UNIT = "preferred_unit";
    private static final String KEY_DARK_MODE_ENABLED = "dark_mode_enabled";

    public static final String UNIT_METRIC = "Metric";
    public static final String UNIT_IMPERIAL = "Imperial";

    private final SharedPreferences prefs;

    public SettingsManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isExpiryAlertsEnabled() {
        return prefs.getBoolean(KEY_EXPIRY_ALERTS_ENABLED, true);
    }

    public void setExpiryAlertsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_EXPIRY_ALERTS_ENABLED, enabled).apply();
    }

    public String getPreferredUnit() {
        return prefs.getString(KEY_PREFERRED_UNIT, UNIT_METRIC);
    }

    public void setPreferredUnit(String unit) {
        prefs.edit().putString(KEY_PREFERRED_UNIT, unit).apply();
    }

    public boolean isDarkModeEnabled() {
        return prefs.getBoolean(KEY_DARK_MODE_ENABLED, false);
    }

    public void setDarkModeEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_DARK_MODE_ENABLED, enabled).apply();
    }
}
