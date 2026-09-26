package com.example.smartpantrymanager.ui;

import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.utils.NavigationHelper;
import com.example.smartpantrymanager.utils.SettingsManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Lets the user toggle "expiring soon" alerts, dark mode, and pick a
 * preferred unit system. All are persisted through SettingsManager
 * (SharedPreferences), so they survive the app being closed and reopened.
 */
public class SettingsActivity extends AppCompatActivity {

    private SettingsManager settingsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settingsManager = new SettingsManager(this);

        Switch expiryAlertsSwitch = findViewById(R.id.switch_expiry_alerts);
        expiryAlertsSwitch.setChecked(settingsManager.isExpiryAlertsEnabled());
        expiryAlertsSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                settingsManager.setExpiryAlertsEnabled(isChecked));

        Switch darkModeSwitch = findViewById(R.id.switch_dark_mode);
        darkModeSwitch.setChecked(settingsManager.isDarkModeEnabled());
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setDarkModeEnabled(isChecked);
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        RadioGroup unitGroup = findViewById(R.id.radio_group_unit);
        RadioButton metricButton = findViewById(R.id.radio_metric);
        RadioButton imperialButton = findViewById(R.id.radio_imperial);

        boolean isMetric = SettingsManager.UNIT_METRIC.equals(settingsManager.getPreferredUnit());
        metricButton.setChecked(isMetric);
        imperialButton.setChecked(!isMetric);

        unitGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String unit = checkedId == R.id.radio_metric ? SettingsManager.UNIT_METRIC : SettingsManager.UNIT_IMPERIAL;
            settingsManager.setPreferredUnit(unit);
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        NavigationHelper.setup(this, bottomNav, R.id.nav_settings);
    }
}
