package org.asdtm.goodweather.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefKeys {

    private final SharedPreferences mPreferences;
    private final SharedPreferences mPublicPreferences;

    private static final String APP_SETTINGS_LOCALE = "locale";
    public static final String APP_SETTINGS_NAME = "config";
    public static final String APP_SETTINGS_LATITUDE = "latitude";
    public static final String APP_SETTINGS_LONGITUDE = "longitude";
    public static final String KEY_PREF_IS_NOTIFICATION_ENABLED = "notification_pref_key";
    public static final String KEY_PREF_TEMPERATURE = "temperature_pref_key";
    public static final String KEY_PREF_INTERVAL_NOTIFICATION = "notification_interval_pref_key";
    public static final String KEY_PREF_VIBRATE = "notification_vibrate_pref_key";

    private Context mContext;

    public PrefKeys(Context context, String publicPrefName) {
        this.mContext = context;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mPublicPreferences = mContext.getSharedPreferences(publicPrefName, Context.MODE_PRIVATE);
    }

    public String getLocale() {
        return mPublicPreferences.getString(APP_SETTINGS_LOCALE, "en");
    }

    public String getUnit() {
        return mPreferences.getString(KEY_PREF_TEMPERATURE, "metric");
    }

    public void setLocale(String locale) {
        SharedPreferences.Editor editor = mPublicPreferences.edit();
        editor.putString(APP_SETTINGS_LOCALE, locale);
        editor.apply();
    }

    public boolean isNotificationEnabled() {
        return mPreferences.getBoolean(KEY_PREF_IS_NOTIFICATION_ENABLED, false);
    }
}
