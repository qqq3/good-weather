package org.asdtm.goodweather.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.widget.RemoteViews;
import org.asdtm.goodweather.R;
import org.asdtm.goodweather.model.Weather;
import org.asdtm.goodweather.utils.AppPreference;
import org.asdtm.goodweather.utils.Utils;

import java.util.Locale;

import static org.asdtm.goodweather.utils.LogToFile.appendLog;

public class LessWidgetService extends IntentService {

    private static final String TAG = "UpdateLessWidgetService:";
    
    public LessWidgetService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        appendLog(this, TAG, "updateWidgetstart");
        Weather weather = AppPreference.getWeather(this);
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
        ComponentName widgetComponent = new ComponentName(this,
                                                          LessWidgetProvider.class);

        int[] widgetIds = widgetManager.getAppWidgetIds(widgetComponent);
        for (int appWidgetId : widgetIds) {
            RemoteViews remoteViews = new RemoteViews(this.getPackageName(),
                                                      R.layout.widget_less_3x1);

            String iconId = weather.currentWeather.getIdIcon();
            String weatherIcon = Utils.getStrIcon(this, iconId);
            String lastUpdate = Utils.setLastUpdateTime(this, AppPreference
                    .getLastUpdateTimeMillis(this));
            String temperatureScale = Utils.getTemperatureScale(this);
            String temperature = String.format(Locale.getDefault(), "%.0f",
                                               weather.temperature.getTemp());

            remoteViews.setTextViewText(R.id.widget_temperature,
                                        temperature + temperatureScale);
            if(!AppPreference.hideDescription(this))
                remoteViews.setTextViewText(R.id.widget_description,
                                        weather.currentWeather.getDescription());
            else remoteViews.setTextViewText(R.id.widget_description, " ");
            remoteViews.setTextViewText(R.id.widget_city, Utils.getCityAndCountry(this));
            remoteViews.setTextViewText(R.id.widget_last_update, lastUpdate);
            remoteViews.setImageViewBitmap(R.id.widget_icon,
                                           Utils.createWeatherIcon(this, weatherIcon));
            widgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
        appendLog(this, TAG, "updateWidgetend");
    }
}
