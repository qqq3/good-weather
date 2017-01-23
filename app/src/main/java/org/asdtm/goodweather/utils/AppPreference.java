package org.asdtm.goodweather.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.asdtm.goodweather.R;
import org.asdtm.goodweather.model.Weather;
import org.asdtm.goodweather.model.WeatherForecast;

import java.util.List;
import java.util.Locale;

public class AppPreference {

    public static String getTemperatureUnit(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(
                Constants.KEY_PREF_TEMPERATURE, "metric");
    }

    public static boolean hideDescription(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                Constants.KEY_PREF_HIDE_DESCRIPTION, false);
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

    public static void saveWeather(Context context, Weather weather) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.PREF_WEATHER_NAME,
                                                                     Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(Constants.WEATHER_DATA_TEMPERATURE, weather.temperature.getTemp());
        if(!hideDescription(context))
        {
            editor.putString(Constants.WEATHER_DATA_DESCRIPTION,
                    weather.currentWeather.getDescription());
        }
        else {
            editor.putString(Constants.WEATHER_DATA_DESCRIPTION, " ");
        }
        editor.putFloat(Constants.WEATHER_DATA_PRESSURE, weather.currentCondition.getPressure());
        editor.putInt(Constants.WEATHER_DATA_HUMIDITY, weather.currentCondition.getHumidity());
        editor.putFloat(Constants.WEATHER_DATA_WIND_SPEED, weather.wind.getSpeed());
        editor.putInt(Constants.WEATHER_DATA_CLOUDS, weather.cloud.getClouds());
        editor.putString(Constants.WEATHER_DATA_ICON, weather.currentWeather.getIdIcon());
        editor.putLong(Constants.WEATHER_DATA_SUNRISE, weather.sys.getSunrise());
        editor.putLong(Constants.WEATHER_DATA_SUNSET, weather.sys.getSunset());
        editor.apply();
    }

    public static String[] getCityAndCode(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.APP_SETTINGS_NAME,
                                                                     Context.MODE_PRIVATE);
        String[] result = new String[2];
        result[0] = preferences.getString(Constants.APP_SETTINGS_CITY, "London");
        result[1] = preferences.getString(Constants.APP_SETTINGS_COUNTRY_CODE, "UK");
        return result;
    }

    public static boolean isLightThemeEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                Constants.KEY_PREF_WIDGET_LIGHT_THEME, false);
    }

    public static long saveLastUpdateTimeMillis(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Constants.APP_SETTINGS_NAME,
                                                            Context.MODE_PRIVATE);
        long now = System.currentTimeMillis();
        sp.edit().putLong(Constants.LAST_UPDATE_TIME_IN_MS, now).apply();
        return now;
    }

    public static long getLastUpdateTimeMillis(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Constants.APP_SETTINGS_NAME,
                                                            Context.MODE_PRIVATE);
        return sp.getLong(Constants.LAST_UPDATE_TIME_IN_MS, 0);
    }

    public static String getWidgetUpdatePeriod(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(
                Constants.KEY_PREF_WIDGET_UPDATE_PERIOD, "60");
    }

    public static void saveWeatherForecast(Context context, List<WeatherForecast> forecastList) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.PREF_FORECAST_NAME,
                                                                     Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String weatherJson = new Gson().toJson(forecastList);
        editor.putString("daily_forecast", weatherJson);
        editor.apply();
    }

    public static List<WeatherForecast> loadWeatherForecast(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.PREF_FORECAST_NAME,
                                                                     Context.MODE_PRIVATE);
        String weather = preferences.getString("daily_forecast",
                                               context.getString(R.string.default_daily_forecast));
        return new Gson().fromJson(weather,
                                   new TypeToken<List<WeatherForecast>>() {
                                   }.getType());
    }
}
