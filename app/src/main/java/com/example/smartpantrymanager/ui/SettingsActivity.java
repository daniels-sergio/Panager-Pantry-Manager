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
 * The Settings screen. It has two on/off switches (expiry alerts and dark mode)
 * and a Metric/Imperial choice.
 *
 * Each control is set to the saved value when the screen opens, and saves its
 * new value the moment it's changed, so there's no Save button. The saving itself
 * is handled by SettingsManager.
 */
public class SettingsActivity extends AppCompatActivity {

    private SettingsManager settingsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settingsManager = new SettingsManager(this);

        // Expiry alerts switch: show the saved value, and save any change.
        Switch expiryAlertsSwitch = findViewById(R.id.switch_expiry_alerts);
        expiryAlertsSwitch.setChecked(settingsManager.isExpiryAlertsEnabled());
        expiryAlertsSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                settingsManager.setExpiryAlertsEnabled(isChecked));

        // Dark mode switch: save the choice, then switch the colour scheme straight
        // away. Android redraws the open screens in the new colours.
        Switch darkModeSwitch = findViewById(R.id.switch_dark_mode);
        darkModeSwitch.setChecked(settingsManager.isDarkModeEnabled());
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setDarkModeEnabled(isChecked);
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        // Metric / Imperial: two radio buttons in a group, so picking one
        // automatically un-picks the other.
        RadioGroup unitGroup = findViewById(R.id.radio_group_unit);
        RadioButton metricButton = findViewById(R.id.radio_metric);
        RadioButton imperialButton = findViewById(R.id.radio_imperial);

        // Tick whichever one was saved last time.
        boolean isMetric = SettingsManager.UNIT_METRIC.equals(settingsManager.getPreferredUnit());
        metricButton.setChecked(isMetric);
        imperialButton.setChecked(!isMetric);

        // When the user picks one, save it.
        unitGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String unit = checkedId == R.id.radio_metric ? SettingsManager.UNIT_METRIC : SettingsManager.UNIT_IMPERIAL;
            settingsManager.setPreferredUnit(unit);
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        NavigationHelper.setup(this, bottomNav, R.id.nav_settings);
    }
}
