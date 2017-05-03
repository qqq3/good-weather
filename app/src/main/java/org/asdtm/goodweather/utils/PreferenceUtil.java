package org.asdtm.goodweather.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceUtil {

    public enum Theme {
        light,
        dark,
    }

    public static SharedPreferences getDefaultSharedPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String getLanguage(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.PREF_LANGUAGE, "en");
    }

    public static Theme getTheme(Context context) {
        return Theme.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.PREF_THEME, "light"));
    }
}
