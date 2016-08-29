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

public class LessWidgetService extends IntentService {

    private static final String TAG = "UpdateLessWidgetService";

    public LessWidgetService() {
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
            AppPreference.setWeather(this, Constants.PREF_WEATHER_NAME, weather);
            updateWidget(weather);
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error get weather", e);
            stopSelf();
        }
    }

    private void updateWidget(Weather weather) {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
        ComponentName widgetComponent = new ComponentName(this,
                                                          LessWidgetProvider.class);

        int[] widgetIds = widgetManager.getAppWidgetIds(widgetComponent);
        for (int appWidgetId : widgetIds) {
            RemoteViews remoteViews = new RemoteViews(this.getPackageName(),
                                                      R.layout.widget_less_3x1);

            String temperatureScale = Utils.getTemperatureScale(this);
            String temperature = String.format(Locale.getDefault(), "%.0f",
                                               weather.temperature.getTemp());
            remoteViews.setTextViewText(R.id.widget_temperature,
                                        temperature + temperatureScale);

            remoteViews.setTextViewText(R.id.widget_description,
                                        weather.currentWeather.getDescription());

            String[] cityAndCountryArray = AppPreference.getCityAndCode(this,
                                                                        Constants.APP_SETTINGS_NAME);
            String cityAndCountry = cityAndCountryArray[0] + ", " + cityAndCountryArray[1];
            remoteViews.setTextViewText(R.id.widget_city, cityAndCountry);
            String lastUpdate = Utils.setLastUpdateTime(this, AppPreference
                    .saveLastUpdateTimeMillis(this));
            remoteViews.setTextViewText(R.id.last_update, lastUpdate);

            String iconId = weather.currentWeather.getIdIcon();
            String weatherIcon = Utils.getStrIcon(this, iconId);
            remoteViews.setImageViewBitmap(R.id.widget_icon,
                                           Utils.createWeatherIcon(this, weatherIcon));

            widgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }
}
