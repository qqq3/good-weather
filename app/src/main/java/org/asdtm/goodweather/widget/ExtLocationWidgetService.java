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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static org.asdtm.goodweather.utils.LogToFile.appendLog;

public class ExtLocationWidgetService extends IntentService {

    private static final String TAG = "UpdateExtLocWidgetService";

    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

    public ExtLocationWidgetService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        appendLog(this, TAG, "updateWidgetstart");
        Weather weather = AppPreference.getWeather(this);
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
        ComponentName widgetComponent = new ComponentName(this, ExtLocationWidgetProvider.class);

        int[] widgetIds = widgetManager.getAppWidgetIds(widgetComponent);
        for (int appWidgetId : widgetIds) {
            String temperatureScale = Utils.getTemperatureScale(this);
            String speedScale = Utils.getSpeedScale(this);
            String percentSign = getString(R.string.percent_sign);

            String temperature = String.format(Locale.getDefault(), "%.0f",
                    weather.temperature.getTemp());
            String wind = getString(R.string.wind_label, String.format(Locale.getDefault(),
                    "%.1f",
                    weather.wind.getSpeed()),
                    speedScale);
            String humidity = getString(R.string.humidity_label,
                    String.valueOf(weather.currentCondition.getHumidity()),
                    percentSign);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(1000 * weather.sys.getSunrise());
            String sunrise = getString(R.string.sunrise_label,
                            sdf.format(calendar.getTime()));
            calendar.setTimeInMillis(1000 * weather.sys.getSunset());
            String sunset = getString(R.string.sunset_label, sdf.format(calendar.getTime()));

            String lastUpdate = Utils.setLastUpdateTime(this, AppPreference
                    .getLastUpdateTimeMillis(this));

            RemoteViews remoteViews = new RemoteViews(this.getPackageName(),
                    R.layout.widget_ext_loc_3x3);
            remoteViews.setTextViewText(R.id.widget_city, Utils.getCityAndCountry(this));
            remoteViews.setTextViewText(R.id.widget_temperature, temperature + temperatureScale);
            if(!AppPreference.hideDescription(this))
                remoteViews.setTextViewText(R.id.widget_description,
                        weather.currentWeather.getDescription());
            else remoteViews.setTextViewText(R.id.widget_description, " ");
            remoteViews.setTextViewText(R.id.widget_wind, wind);
            remoteViews.setTextViewText(R.id.widget_humidity, humidity);
            remoteViews.setTextViewText(R.id.widget_sunrise, sunrise);
            remoteViews.setTextViewText(R.id.widget_sunset, sunset);
            SharedPreferences weatherPref = this.getSharedPreferences(Constants.PREF_WEATHER_NAME,
                    Context.MODE_PRIVATE);
            remoteViews.setImageViewResource(R.id.widget_icon, Utils.getWeatherResourceIcon(weatherPref));
            remoteViews.setTextViewText(R.id.widget_last_update, lastUpdate);

            widgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
        appendLog(this, TAG, "updateWidgetend");
    }
}
