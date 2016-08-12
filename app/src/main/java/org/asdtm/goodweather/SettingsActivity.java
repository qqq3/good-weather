package org.asdtm.goodweather;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;

import org.asdtm.goodweather.service.NotificationService;
import org.asdtm.goodweather.utils.PrefKeys;

public class SettingsActivity extends AppCompatPreferenceActivity {

    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        getFragmentManager().beginTransaction()
                            .replace(android.R.id.content, new GeneralPreferenceFragment())
                            .commit();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment implements
            SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            final SwitchPreference notificationSwitch = (SwitchPreference) findPreference(
                    PrefKeys.KEY_PREF_IS_NOTIFICATION_ENABLED);
            notificationSwitch.setOnPreferenceChangeListener(notificationListener);

        }

        Preference.OnPreferenceChangeListener notificationListener =
                new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        boolean isEnabled = (boolean) o;
                        NotificationService.setNotificationServiceAlarm(getActivity(),
                                                                        isEnabled);
                        return true;
                    }
                };


        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case PrefKeys.KEY_PREF_TEMPERATURE:
                    setSummary();
                    break;
                case PrefKeys.KEY_PREF_INTERVAL_NOTIFICATION:
                    Preference pref = findPreference(key);
                    NotificationService.setNotificationServiceAlarm(getActivity(),
                                                                    pref.isEnabled());
                    Log.i(TAG, "Interval was enabled: " + pref.isEnabled());
                    setSummary();
                    break;
            }
        }

        private void setSummary() {
            Preference temperaturePref = findPreference(PrefKeys.KEY_PREF_TEMPERATURE);
            ListPreference temperatureListPref = (ListPreference) temperaturePref;
            temperaturePref.setSummary(temperatureListPref.getEntry());

            Preference notificationIntervalPref = findPreference(
                    PrefKeys.KEY_PREF_INTERVAL_NOTIFICATION);
            ListPreference notificationIntervalListPref = (ListPreference) notificationIntervalPref;
            notificationIntervalPref.setSummary(notificationIntervalListPref.getEntry());
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                                 .registerOnSharedPreferenceChangeListener(this);
            setSummary();
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                                 .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    if (NavUtils.getParentActivityName(getActivity()) != null) {
                        NavUtils.navigateUpFromSameTask(getActivity());
                        return true;
                    }
            }

            return super.onOptionsItemSelected(item);
        }
    }
}