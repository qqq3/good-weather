package org.asdtm.goodweather.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import org.asdtm.goodweather.R;
import org.asdtm.goodweather.model.Weather;
import org.asdtm.goodweather.utils.AppPreference;
import org.asdtm.goodweather.utils.Constants;
import org.asdtm.goodweather.utils.Utils;
import java.util.Locale;
import static org.asdtm.goodweather.utils.LogToFile.appendLog;

public class MoreWidgetService extends IntentService {

    private static final String TAG = "UpdateMoreWidgetService";
    
    public MoreWidgetService() {
        super(TAG);
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
        appendLog(this, TAG, "updateWidgetstart");
        Weather weather = AppPreference.getWeather(this);
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
        ComponentName widgetComponent = new ComponentName(this, MoreWidgetProvider.class);

        int[] widgetIds = widgetManager.getAppWidgetIds(widgetComponent);
        for (int appWidgetId : widgetIds) {
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
                    .getLastUpdateTimeMillis(this));

            RemoteViews remoteViews = new RemoteViews(this.getPackageName(),
                                                      R.layout.widget_more_3x3);
            remoteViews.setTextViewText(R.id.widget_city, Utils.getCityAndCountry(this));
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
        appendLog(getBaseContext(), TAG, "updateWidgetend");
    }
}
