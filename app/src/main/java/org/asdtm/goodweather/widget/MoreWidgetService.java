package org.asdtm.goodweather.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

import org.asdtm.goodweather.ConnectionDetector;
import org.asdtm.goodweather.R;
import org.asdtm.goodweather.WeatherJSONParser;
import org.asdtm.goodweather.WeatherRequest;
import org.asdtm.goodweather.model.Weather;
import org.asdtm.goodweather.utils.AppPreference;
import org.asdtm.goodweather.utils.Constants;
import org.asdtm.goodweather.utils.Utils;
import org.json.JSONException;

import java.io.IOException;
import java.util.Locale;

public class MoreWidgetService extends IntentService {

    private static final String TAG = "UpdateMoreWidgetService";

    public MoreWidgetService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ConnectionDetector checkNetwork = new ConnectionDetector(this);
        if (!checkNetwork.isNetworkAvailableAndConnected()) {
            return;
        }

        SharedPreferences preferences = getSharedPreferences(Constants.APP_SETTINGS_NAME, 0);
        String latitude = preferences.getString(Constants.APP_SETTINGS_LATITUDE, "51.51");
        String longitude = preferences.getString(Constants.APP_SETTINGS_LONGITUDE, "-0.13");
        String locale = AppPreference.getLocale(this, Constants.APP_SETTINGS_NAME);
        String units = AppPreference.getTemperatureUnit(this);

        try {
            String weatherRaw = new WeatherRequest().getItems(latitude, longitude, units,
                                                              locale);
            Weather weather;
            weather = WeatherJSONParser.getWeather(weatherRaw);
            AppPreference.saveWeather(this, weather);
            updateWidget(weather);
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error get weather", e);
            stopSelf();
        }
    }

    private void updateWidget(Weather weather) {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
        ComponentName widgetComponent = new ComponentName(this, MoreWidgetProvider.class);

        int[] widgetIds = widgetManager.getAppWidgetIds(widgetComponent);
        for (int appWidgetId : widgetIds) {
            String[] cityAndCountryArray = AppPreference.getCityAndCode(this);
            String cityAndCountry = cityAndCountryArray[0] + ", " + cityAndCountryArray[1];
            String temperatureScale = Utils.getTemperatureScale(this);
            String speedScale = Utils.getSpeedScale(this);
            String percentSign = getString(R.string.percent_sign);
            String pressureMeasurement = getString(R.string.pressure_measurement);

            String temperature = String.format(Locale.getDefault(), "%.0f",
                                               weather.temperature.getTemp());
            String wind = getString(R.string.wind_label, String.format(Locale.getDefault(),
                                                                       "%.1f",
                                                                       weather.wind.getSpeed()),
                                    speedScale);
            String humidity = getString(R.string.humidity_label,
                                        String.valueOf(weather.currentCondition.getHumidity()),
                                        percentSign);
            String pressure = getString(R.string.pressure_label,
                                        String.format(Locale.getDefault(), "%.1f",
                                                      weather.currentCondition.getPressure()),
                                        pressureMeasurement);
            String cloudiness = getString(R.string.cloudiness_label,
                                          String.valueOf(weather.cloud.getClouds()),
                                          percentSign);
            String iconId = weather.currentWeather.getIdIcon();
            String weatherIcon = Utils.getStrIcon(this, iconId);
            String lastUpdate = Utils.setLastUpdateTime(this, AppPreference
                    .saveLastUpdateTimeMillis(this));

            RemoteViews remoteViews = new RemoteViews(this.getPackageName(),
                                                      R.layout.widget_more_3x3);
            remoteViews.setTextViewText(R.id.widget_city, cityAndCountry);
            remoteViews.setTextViewText(R.id.widget_temperature, temperature + temperatureScale);
            if(!AppPreference.hideDescription(this))
                remoteViews.setTextViewText(R.id.widget_description,
                                        weather.currentWeather.getDescription());
            else remoteViews.setTextViewText(R.id.widget_description, " ");
            remoteViews.setTextViewText(R.id.widget_wind, wind);
            remoteViews.setTextViewText(R.id.widget_humidity, humidity);
            remoteViews.setTextViewText(R.id.widget_pressure, pressure);
            remoteViews.setTextViewText(R.id.widget_clouds, cloudiness);
            remoteViews.setImageViewBitmap(R.id.widget_icon,
                                           Utils.createWeatherIcon(this, weatherIcon));
            remoteViews.setTextViewText(R.id.widget_last_update, lastUpdate);

            widgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }
}
