package org.asdtm.goodweather.utils;

public class Constants {

    /**
     * SharedPreference names
     */
    public static final String APP_SETTINGS_NAME = "config";
    public static final String PREF_WEATHER_NAME = "weather_pref";
    public static final String PREF_FORECAST_NAME = "weather_forecast";

    /**
     * Preferences constants
     */
    public static final String APP_SETTINGS_LOCALE = "locale";
    public static final String APP_SETTINGS_LATITUDE = "latitude";
    public static final String APP_SETTINGS_LONGITUDE = "longitude";
    public static final String APP_SETTINGS_CITY = "city";
    public static final String APP_SETTINGS_COUNTRY_CODE = "country_code";
    public static final String LAST_UPDATE_TIME_IN_MS = "last_update";

    public static final String KEY_PREF_IS_NOTIFICATION_ENABLED = "notification_pref_key";
    public static final String KEY_PREF_TEMPERATURE = "temperature_pref_key";
    public static final String KEY_PREF_HIDE_DESCRIPTION = "hide_desc_pref_key";
    public static final String KEY_PREF_INTERVAL_NOTIFICATION = "notification_interval_pref_key";
    public static final String KEY_PREF_VIBRATE = "notification_vibrate_pref_key";
    public static final String KEY_PREF_WIDGET_LIGHT_THEME = "widget_light_theme_pref_key";
    public static final String KEY_PREF_WIDGET_UPDATE_PERIOD = "widget_update_period_pref_key";

    /**
     * About preference screen constants
     */
    public static final String KEY_PREF_ABOUT_VERSION = "about_version_pref_key";
    public static final String KEY_PREF_ABOUT_F_DROID = "about_f_droid_pref_key";
    public static final String KEY_PREF_ABOUT_GOOGLE_PLAY = "about_google_play_pref_key";
    public static final String KEY_PREF_ABOUT_OPEN_SOURCE_LICENSES =
            "about_open_source_licenses_pref_key";

    public static final String WEATHER_DATA_TEMPERATURE = "temperature";
    public static final String WEATHER_DATA_DESCRIPTION = "description";
    public static final String WEATHER_DATA_PRESSURE = "pressure";
    public static final String WEATHER_DATA_HUMIDITY = "humidity";
    public static final String WEATHER_DATA_WIND_SPEED = "wind_speed";
    public static final String WEATHER_DATA_CLOUDS = "clouds";
    public static final String WEATHER_DATA_ICON = "icon";
    public static final String WEATHER_DATA_SUNRISE = "sunrise";
    public static final String WEATHER_DATA_SUNSET = "sunset";

    /**
     * Widget action constants
     */
    public static final String ACTION_FORCED_APPWIDGET_UPDATE =
            "org.asdtm.goodweather.action.FORCED_APPWIDGET_UPDATE";
    public static final String ACTION_APPWIDGET_THEME_CHANGED =
            "org.asdtm.goodweather.action.APPWIDGET_THEME_CHANGED";
    public static final String ACTION_APPWIDGET_UPDATE_PERIOD_CHANGED =
            "org.asdtm.goodweather.action.APPWIDGET_UPDATE_PERIOD_CHANGED";

    /**
     * URIs constants
     */
    public static final String SOURCE_CODE_URI = "https://github.com/qqq3/good-weather";
    public static final String GOOGLE_PLAY_APP_URI = "market://details?id=%s";
    public static final String GOOGLE_PLAY_WEB_URI =
            "http://play.google.com/store/apps/details?id=%s";
    public static final String F_DROID_WEB_URI = "https://f-droid.org/repository/browse/?fdid=%s";
    public static final String WEATHER_ENDPOINT = "http://api.openweathermap.org/data/2.5/weather";
    public static final String WEATHER_FORECAST_ENDPOINT = "http://api.openweathermap.org/data/2.5/forecast/daily/";

    public static final String BITCOIN_ADDRESS = "16rSzigYf3roULrPuMFe6mb5xxHE3iZspM";

    public static final int PARSE_RESULT_SUCCESS = 0;
    public static final int TASK_RESULT_ERROR = -1;
    public static final int PARSE_RESULT_ERROR = -2;
}
