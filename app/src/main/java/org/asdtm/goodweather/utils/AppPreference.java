package org.asdtm.goodweather.utils;

import android.content.Context;
import android.preference.PreferenceManager;

public class AppPreference {

    public static String getTemperatureUnit(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(
                PrefKeys.KEY_PREF_TEMPERATURE, "metric");
    }


    public static String getInterval(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                                .getString(PrefKeys.KEY_PREF_INTERVAL_NOTIFICATION, "60");
    }

    public static boolean isVibrateEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                PrefKeys.KEY_PREF_VIBRATE,
                false);
    }
}
