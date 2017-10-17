package org.asdtm.goodweather.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import org.asdtm.goodweather.R;
import org.asdtm.goodweather.model.Weather;
import org.asdtm.goodweather.model.WeatherForecast;

import java.util.List;

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

    public static void saveWeather(Context context, Weather weather) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.PREF_WEATHER_NAME,
                                                                     Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(Constants.WEATHER_DATA_WEATHER_ID, weather.currentWeather.getWeatherId());
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
    
    public static Weather getWeather(Context context) {
        Weather weather = new Weather();
        SharedPreferences preferences = context.getSharedPreferences(Constants.PREF_WEATHER_NAME,
                                                                     Context.MODE_PRIVATE);
        
        weather.temperature.setTemp(preferences.getFloat(Constants.WEATHER_DATA_TEMPERATURE, 0));
        if(!hideDescription(context)) {
            weather.currentWeather.setDescription(preferences.getString(Constants.WEATHER_DATA_DESCRIPTION, ""));
        }
        else {
            weather.currentWeather.setDescription(" ");
        }
        
        weather.sys.setSunset(preferences.getLong(Constants.WEATHER_DATA_SUNSET, 0));
        weather.sys.setSunrise(preferences.getLong(Constants.WEATHER_DATA_SUNRISE, 0));
        weather.currentWeather.setIdIcon(preferences.getString(Constants.WEATHER_DATA_ICON, ""));
        weather.cloud.setClouds(preferences.getInt(Constants.WEATHER_DATA_CLOUDS, 0));
        weather.wind.setSpeed(preferences.getFloat(Constants.WEATHER_DATA_WIND_SPEED, 0));
        weather.currentCondition.setHumidity(preferences.getInt(Constants.WEATHER_DATA_HUMIDITY, 0));
        weather.currentCondition.setPressure(preferences.getFloat(Constants.WEATHER_DATA_PRESSURE, 0));
        return weather;
    }

    public static String[] getCityAndCode(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.APP_SETTINGS_NAME,
                                                                     Context.MODE_PRIVATE);
        String[] result = new String[2];
        result[0] = preferences.getString(Constants.APP_SETTINGS_CITY, "London");
        result[1] = preferences.getString(Constants.APP_SETTINGS_COUNTRY_CODE, "UK");
        return result;
    }

    public static boolean isGeocoderEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                Constants.KEY_PREF_WIDGET_USE_GEOCODER, false);
    }
    
    public static boolean isUpdateLocationEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                Constants.KEY_PREF_WIDGET_UPDATE_LOCATION, false);
    }
        
    public static String getLocationGeocoderSource(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(
                Constants.KEY_PREF_LOCATION_GEOCODER_SOURCE, "location_geocoder_system");
    }
    
    public static String getUpdateSource(Context context) {
        String updateDetailLevel = PreferenceManager.getDefaultSharedPreferences(context).getString(
                Constants.KEY_PREF_UPDATE_DETAIL, "preference_display_update_nothing");
        switch (updateDetailLevel) {
            case "preference_display_update_value":
            case "preference_display_update_location_source":
                return context.getSharedPreferences(Constants.APP_SETTINGS_NAME, 0).getString(Constants.APP_SETTINGS_UPDATE_SOURCE, "W");
            case "preference_display_update_nothing":
            default:
                return "";
        }
    }
    
    public static String getTheme(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(
                Constants.KEY_PREF_WIDGET_THEME, "dark");
    }

    public static int getTextColor(Context context) {
        String theme = getTheme(context);
        if (null == theme) {
            return ContextCompat.getColor(context, R.color.widget_transparentTheme_textColorPrimary);
        } else switch (theme) {
            case "dark":
                return ContextCompat.getColor(context, R.color.widget_darkTheme_textColorPrimary);
            case "light":
                return ContextCompat.getColor(context, R.color.widget_lightTheme_textColorPrimary);
            default:
                return ContextCompat.getColor(context, R.color.widget_transparentTheme_textColorPrimary);
        }
    }
    
    public static int getBackgroundColor(Context context) {
        String theme = getTheme(context);
        if (null == theme) {
            return ContextCompat.getColor(context,
                    R.color.widget_transparentTheme_colorBackground);
        } else switch (theme) {
            case "dark":
                return ContextCompat.getColor(context,
                        R.color.widget_darkTheme_colorBackground);
            case "light":
                return ContextCompat.getColor(context,
                        R.color.widget_lightTheme_colorBackground);
            default:
                return ContextCompat.getColor(context,
                        R.color.widget_transparentTheme_colorBackground);
        }
    }
    
    public static int getWindowHeaderBackgroundColorId(Context context) {
        String theme = getTheme(context);
        if (null == theme) {
            return ContextCompat.getColor(context,
                    R.color.widget_transparentTheme_window_colorBackground);
        } else switch (theme) {
            case "dark":
                return ContextCompat.getColor(context,
                        R.color.widget_darkTheme_window_colorBackground);
            case "light":
                return ContextCompat.getColor(context,
                        R.color.widget_lightTheme_window_colorBackground);
            default:
                return ContextCompat.getColor(context,
                        R.color.widget_transparentTheme_window_colorBackground);
        }
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
