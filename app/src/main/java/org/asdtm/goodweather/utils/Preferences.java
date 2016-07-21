package org.asdtm.goodweather.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.asdtm.goodweather.SettingsActivity;

public class Preferences {

    private final SharedPreferences mPreferences;
    private final SharedPreferences mPublicPreferences;

    private static final String APP_SETTINGS_LOCALE = "locale";
    public static final String APP_SETTINGS_NAME = "config";

    private Context mContext;

    public Preferences(Context context, String publicPrefName) {
        this.mContext = context;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mPublicPreferences = mContext.getSharedPreferences(publicPrefName, Context.MODE_PRIVATE);
    }

    public String getLocale() {
        return mPublicPreferences.getString(APP_SETTINGS_LOCALE, "en");
    }

    public String getUnit() {
        return mPreferences.getString(SettingsActivity.KEY_PREF_TEMPERATURE, "metric");
    }

    public void setLocale(String locale) {
        SharedPreferences.Editor editor = mPublicPreferences.edit();
        editor.putString(APP_SETTINGS_LOCALE, locale);
        editor.apply();
    }
}
