package org.asdtm.goodweather.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.asdtm.goodweather.model.Weather;

import static org.asdtm.goodweather.utils.PrefKeys.APP_SETTINGS_LOCALE;

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

    public static boolean isNotificationEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                PrefKeys.KEY_PREF_IS_NOTIFICATION_ENABLED, false);
    }

    public static String getLocale(Context context, String publicPrefName) {
        SharedPreferences preferences = context.getSharedPreferences(publicPrefName,
                                                                     Context.MODE_PRIVATE);
        return preferences.getString(APP_SETTINGS_LOCALE, "en");
    }

    public static void setLocale(Context context, String publicPrefName, String locale) {
        SharedPreferences preferences = context.getSharedPreferences(publicPrefName,
                                                                     Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(APP_SETTINGS_LOCALE, locale);
        editor.apply();
    }

    public static void setWeather(Context context, String publicPrefName, Weather weather) {
        SharedPreferences preferences = context.getSharedPreferences(publicPrefName,
                                                                     Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(PrefKeys.WEATHER_DATA_TEMPERATURE, weather.temperature.getTemp());
        editor.putString(PrefKeys.WEATHER_DATA_DESCRIPTION,
                         weather.currentWeather.getDescription());
        editor.putFloat(PrefKeys.WEATHER_DATA_PRESSURE, weather.currentCondition.getPressure());
        editor.putInt(PrefKeys.WEATHER_DATA_HUMIDITY, weather.currentCondition.getHumidity());
        editor.putFloat(PrefKeys.WEATHER_DATA_WIND_SPEED, weather.wind.getSpeed());
        editor.putInt(PrefKeys.WEATHER_DATA_CLOUDS, weather.cloud.getClouds());
        editor.putString(PrefKeys.WEATHER_DATA_ICON, weather.currentWeather.getIdIcon());
        editor.apply();
    }

    public static String[] getCityAndCode(Context context, String publicPrefName) {
        SharedPreferences preferences = context.getSharedPreferences(publicPrefName,
                                                                     Context.MODE_PRIVATE);
        String[] result = new String[2];
        result[0] = preferences.getString(PrefKeys.APP_SETTINGS_CITY, "London");
        result[1] = preferences.getString(PrefKeys.APP_SETTINGS_COUNTRY_CODE, "UK");
        return result;
    }
}
