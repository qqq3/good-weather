package org.asdtm.goodweather;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import org.asdtm.goodweather.service.NotificationService;
import org.asdtm.goodweather.utils.Constants;

import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity {

    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        super.onBuildHeaders(target);
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    @Override
    public boolean hasHeaders() {
        return super.hasHeaders();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || WidgetPreferenceFragment.class.getName().equals(fragmentName)
                || AboutPreferenceFragment.class.getName().equals(fragmentName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class GeneralPreferenceFragment extends PreferenceFragment implements
            SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            final SwitchPreference notificationSwitch = (SwitchPreference) findPreference(
                    Constants.KEY_PREF_IS_NOTIFICATION_ENABLED);
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
                case Constants.KEY_PREF_TEMPERATURE:
                    setSummary();
                    getActivity().sendBroadcast(
                            new Intent(Constants.ACTION_FORCED_APPWIDGET_UPDATE));
                    break;
                case Constants.KEY_PREF_HIDE_DESCRIPTION:
                    Intent intent = new Intent(Constants.ACTION_FORCED_APPWIDGET_UPDATE);
                    getActivity().sendBroadcast(intent);
                    break;
                case Constants.KEY_PREF_INTERVAL_NOTIFICATION:
                    Preference pref = findPreference(key);
                    NotificationService.setNotificationServiceAlarm(getActivity(),
                                                                    pref.isEnabled());
                    setSummary();
                    break;
            }
        }

        private void setSummary() {
            Preference temperaturePref = findPreference(Constants.KEY_PREF_TEMPERATURE);
            ListPreference temperatureListPref = (ListPreference) temperaturePref;
            temperaturePref.setSummary(temperatureListPref.getEntry());

            Preference notificationIntervalPref = findPreference(
                    Constants.KEY_PREF_INTERVAL_NOTIFICATION);
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
    }

    public static class WidgetPreferenceFragment extends PreferenceFragment implements
            SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_widget);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case Constants.KEY_PREF_WIDGET_LIGHT_THEME:
                    Intent intent = new Intent(Constants.ACTION_APPWIDGET_THEME_CHANGED);
                    getActivity().sendBroadcast(intent);
                    break;
                case Constants.KEY_PREF_WIDGET_UPDATE_PERIOD:
                    Intent intent1 = new Intent(Constants.ACTION_APPWIDGET_UPDATE_PERIOD_CHANGED);
                    getActivity().sendBroadcast(intent1);
                    setSummary();
                    break;
            }
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

        private void setSummary() {
            Preference updatePeriodPref = findPreference(Constants.KEY_PREF_WIDGET_UPDATE_PERIOD);
            ListPreference updatePeriodListPref = (ListPreference) updatePeriodPref;
            updatePeriodPref.setSummary(updatePeriodListPref.getEntry());
        }
    }

    public static class AboutPreferenceFragment extends PreferenceFragment {

        PackageManager mPackageManager;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_about);

            mPackageManager = getActivity().getPackageManager();
            findPreference(Constants.KEY_PREF_ABOUT_VERSION).setSummary(getVersionName());
            findPreference(Constants.KEY_PREF_ABOUT_F_DROID).setIntent(fDroidIntent());
            findPreference(Constants.KEY_PREF_ABOUT_GOOGLE_PLAY).setIntent(googlePlayIntent());
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                             Preference preference) {
            if (preference.equals(findPreference(Constants.KEY_PREF_ABOUT_OPEN_SOURCE_LICENSES))) {
                LicensesDialogFragment licensesDialog = LicensesDialogFragment.newInstance();
                licensesDialog.show(getFragmentManager(), "LicensesDialog");
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        private String getVersionName() {
            String versionName;
            try {
                versionName = mPackageManager.getPackageInfo(getActivity().getPackageName(),
                                                             0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Get version name error", e);
                versionName = "666";
            }
            return versionName;
        }

        private Intent fDroidIntent() {
            String ACTION_VIEW = Intent.ACTION_VIEW;
            String fDroidWebUri = String.format(Constants.F_DROID_WEB_URI,
                                                getActivity().getPackageName());

            return new Intent(ACTION_VIEW, Uri.parse(fDroidWebUri));
        }

        private Intent googlePlayIntent() {
            String ACTION_VIEW = Intent.ACTION_VIEW;
            String googlePlayAppUri = String.format(Constants.GOOGLE_PLAY_APP_URI,
                                                    getActivity().getPackageName());
            String googlePlayWebUri = String.format(Constants.GOOGLE_PLAY_WEB_URI,
                                                    getActivity().getPackageName());

            Intent intent = new Intent(ACTION_VIEW, Uri.parse(googlePlayAppUri));
            if (mPackageManager.resolveActivity(intent, 0) == null) {
                intent = new Intent(ACTION_VIEW, Uri.parse(googlePlayWebUri));
            }

            return intent;
        }

        public static class LicensesDialogFragment extends DialogFragment {

            static LicensesDialogFragment newInstance() {
                return new LicensesDialogFragment();
            }

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                final TextView textView = new TextView(getActivity());
                int padding = (int) getResources().getDimension(R.dimen.activity_horizontal_margin);
                textView.setPadding(padding, padding, padding, padding);
                textView.setLineSpacing(0, 1.2f);
                textView.setLinkTextColor(ContextCompat.getColor(getActivity(), R.color.link_color));
                textView.setText(R.string.licenses);
                textView.setMovementMethod(LinkMovementMethod.getInstance());
                return new AlertDialog.Builder(getActivity())
                        .setTitle("Open source licenses")
                        .setView(textView)
                        .setPositiveButton(android.R.string.ok, null)
                        .create();
            }
        }
    }
}
