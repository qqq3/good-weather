package org.asdtm.goodweather.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import org.asdtm.goodweather.model.Weather;

import java.util.Locale;

public class AppPreference {

    public static String getTemperatureUnit(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(
                Constants.KEY_PREF_TEMPERATURE, "metric");
    }

    public static String getInterval(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                                .getString(Constants.KEY_PREF_INTERVAL_NOTIFICATION, "60");
    }

    public static boolean isVibrateEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                Constants.KEY_PREF_VIBRATE,
                false);
    }

    public static boolean isNotificationEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                Constants.KEY_PREF_IS_NOTIFICATION_ENABLED, false);
    }

    public static String getLocale(Context context, String publicPrefName) {
        SharedPreferences preferences = context.getSharedPreferences(publicPrefName,
                                                                     Context.MODE_PRIVATE);
        return preferences.getString(Constants.APP_SETTINGS_LOCALE, "en");
    }

    public static void setLocale(Context context, String publicPrefName) {
        String locale;
        /**
         * Check API version and based on this gets the current value of the default locale
         * with specified Category or without
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = Locale.getDefault(Locale.Category.DISPLAY).getLanguage();
        } else {
            locale = Locale.getDefault().getLanguage();
        }

        SharedPreferences preferences = context.getSharedPreferences(publicPrefName,
                                                                     Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.APP_SETTINGS_LOCALE, locale);
        editor.apply();
    }

    public static void setWeather(Context context, String publicPrefName, Weather weather) {
        SharedPreferences preferences = context.getSharedPreferences(publicPrefName,
                                                                     Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(Constants.WEATHER_DATA_TEMPERATURE, weather.temperature.getTemp());
        editor.putString(Constants.WEATHER_DATA_DESCRIPTION,
                         weather.currentWeather.getDescription());
        editor.putFloat(Constants.WEATHER_DATA_PRESSURE, weather.currentCondition.getPressure());
        editor.putInt(Constants.WEATHER_DATA_HUMIDITY, weather.currentCondition.getHumidity());
        editor.putFloat(Constants.WEATHER_DATA_WIND_SPEED, weather.wind.getSpeed());
        editor.putInt(Constants.WEATHER_DATA_CLOUDS, weather.cloud.getClouds());
        editor.putString(Constants.WEATHER_DATA_ICON, weather.currentWeather.getIdIcon());
        editor.apply();
    }

    public static String[] getCityAndCode(Context context, String publicPrefName) {
        SharedPreferences preferences = context.getSharedPreferences(publicPrefName,
                                                                     Context.MODE_PRIVATE);
        String[] result = new String[2];
        result[0] = preferences.getString(Constants.APP_SETTINGS_CITY, "London");
        result[1] = preferences.getString(Constants.APP_SETTINGS_COUNTRY_CODE, "UK");
        return result;
    }
}
