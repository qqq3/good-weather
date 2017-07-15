package org.asdtm.goodweather;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.asdtm.goodweather.service.NotificationService;
import org.asdtm.goodweather.utils.Constants;

import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity {

    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((GoodWeatherApp) getApplication()).applyTheme(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
        super.onCreate(savedInstanceState);
        setupActionBar();

        int horizontalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
        int verticalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
        int topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, getResources().getDisplayMetrics());
        getListView().setPadding(horizontalMargin, topMargin, horizontalMargin, verticalMargin);
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
        getLayoutInflater().inflate(R.layout.activity_settings, (ViewGroup)findViewById(android.R.id.content));
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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

        private final String[] SUMMARIES_TO_UPDATE = {
                Constants.KEY_PREF_TEMPERATURE,
                Constants.KEY_PREF_HIDE_DESCRIPTION,
                Constants.KEY_PREF_INTERVAL_NOTIFICATION,
                Constants.PREF_LANGUAGE,
                Constants.PREF_THEME,
        };

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            final SwitchPreference notificationSwitch = (SwitchPreference) findPreference(
                    Constants.KEY_PREF_IS_NOTIFICATION_ENABLED);
            notificationSwitch.setOnPreferenceChangeListener(notificationListener);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            int horizontalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
            int verticalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
            int topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, getResources().getDisplayMetrics());

            if (view != null) {
                view.setPadding(horizontalMargin, topMargin, horizontalMargin, verticalMargin);
            }
            return view;
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

        private void entrySummary(String key) {
            ListPreference preference = (ListPreference) findPreference(key);
            preference.setSummary(preference.getEntry());
        }

        private void updateSummary(String key, boolean changing) {
            switch (key) {
                case Constants.KEY_PREF_TEMPERATURE:
                    entrySummary(key);
                    if (changing) {
                        getActivity().sendBroadcast(new Intent(Constants.ACTION_FORCED_APPWIDGET_UPDATE));
                    }
                    break;
                case Constants.KEY_PREF_HIDE_DESCRIPTION:
                    if (changing) {
                        getActivity().sendBroadcast(new Intent(Constants.ACTION_FORCED_APPWIDGET_UPDATE));
                    }
                    break;
                case Constants.KEY_PREF_INTERVAL_NOTIFICATION:
                    entrySummary(key);
                    if (changing) {
                        Preference pref = findPreference(key);
                        NotificationService.setNotificationServiceAlarm(getActivity(), pref.isEnabled());
                    }
                    break;
                case Constants.PREF_LANGUAGE:
                    entrySummary(key);
                    if (changing) {
                        DialogFragment dialog = new SettingsAlertDialog().newInstance(R.string.restart_dialog_message);
                        dialog.show(getActivity().getFragmentManager(), "restartApp");
                    }
                    break;
                case Constants.PREF_THEME:
                    entrySummary(key);
                    if (changing) {
                        GoodWeatherApp app = (GoodWeatherApp) getActivity().getApplication();
                        app.reloadTheme();
                        app.applyTheme(getActivity());
                        restartApp(getActivity());
                    }
                    break;
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updateSummary(key, true);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                                 .registerOnSharedPreferenceChangeListener(this);

            for (String key : SUMMARIES_TO_UPDATE) {
                updateSummary(key, false);
            }
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
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            int horizontalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
            int verticalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
            int topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, getResources().getDisplayMetrics());

            if (view != null) {
                view.setPadding(horizontalMargin, topMargin, horizontalMargin, verticalMargin);
            }
            return view;
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case Constants.KEY_PREF_WIDGET_THEME:
                    Intent intent = new Intent(Constants.ACTION_APPWIDGET_THEME_CHANGED);
                    getActivity().sendBroadcast(intent);
                    break;
                case Constants.KEY_PREF_WIDGET_UPDATE_PERIOD:
                    Intent intent1 = new Intent(Constants.ACTION_APPWIDGET_UPDATE_PERIOD_CHANGED);
                    getActivity().sendBroadcast(intent1);
                    setSummary();
                    break;
                case Constants.KEY_PREF_WIDGET_UPDATE_LOCATION:
                    int fineLocationPermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
                    if (fineLocationPermission != PackageManager.PERMISSION_GRANTED) {
                        Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.permission_location_need, Snackbar.LENGTH_SHORT).show();
                        CheckBoxPreference updateLocation = (CheckBoxPreference) findPreference(key);
                        updateLocation.setChecked(false);
                    }
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
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            int horizontalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
            int verticalMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
            int topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, getResources().getDisplayMetrics());

            if (view != null) {
                view.setPadding(horizontalMargin, topMargin, horizontalMargin, verticalMargin);
            }
            return view;
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

    public static class SettingsAlertDialog extends DialogFragment {

        private static final String ARG_MESSAGE_RES_ID = "org.asdtm.goodweather.message_res_id";

        public SettingsAlertDialog newInstance(int messageResId) {
            SettingsAlertDialog fragment = new SettingsAlertDialog();
            Bundle args = new Bundle();
            args.putInt(ARG_MESSAGE_RES_ID, messageResId);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int messageResId = getArguments().getInt(ARG_MESSAGE_RES_ID);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(messageResId);
            builder.setPositiveButton(android.R.string.ok, null);
            return builder.create();
        }
    }

    public static void restartApp(Activity activity) {
        Intent intent = activity.getIntent();
        if (intent == null) {
            return;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.finish();
        activity.overridePendingTransition(0, 0);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }
}
