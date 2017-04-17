package org.asdtm.goodweather.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import org.asdtm.goodweather.MainActivity;
import org.asdtm.goodweather.R;
import org.asdtm.goodweather.service.LocationUpdateService;
import org.asdtm.goodweather.utils.AppPreference;
import org.asdtm.goodweather.utils.AppWidgetProviderAlarm;
import org.asdtm.goodweather.utils.Constants;
import org.asdtm.goodweather.utils.Utils;

import java.util.Locale;


public class LessWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "WidgetLessInfo";

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        AppWidgetProviderAlarm widgetProviderAlarm =
                new AppWidgetProviderAlarm(context, LessWidgetProvider.class);
        widgetProviderAlarm.setAlarm();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case Constants.ACTION_FORCED_APPWIDGET_UPDATE:
                if(AppPreference.isUpdateLocationEnabled(context)) {
                    context.startService(new Intent(context, LocationUpdateService.class));
                } else {
                    context.startService(new Intent(context, LessWidgetService.class));
                }
                break;
            case Intent.ACTION_LOCALE_CHANGED:
                context.startService(new Intent(context, LessWidgetService.class));
                break;
            case Constants.ACTION_APPWIDGET_THEME_CHANGED:
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                ComponentName componentName = new ComponentName(context, LessWidgetProvider.class);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
                onUpdate(context, appWidgetManager, appWidgetIds);
                break;
            case Constants.ACTION_APPWIDGET_UPDATE_PERIOD_CHANGED:
                AppWidgetProviderAlarm widgetProviderAlarm =
                        new AppWidgetProviderAlarm(context, LessWidgetProvider.class);
                if (widgetProviderAlarm.isAlarmOff()) {
                    break;
                } else {
                    widgetProviderAlarm.setAlarm();
                }
                break;
            default:
                super.onReceive(context, intent);
        }
    }
    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        for (int appWidgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                                                      R.layout.widget_less_3x1);

            setWidgetTheme(context, remoteViews);
            preLoadWeather(context, remoteViews);

            Intent intent = new Intent(context, LessWidgetProvider.class);
            intent.setAction(Constants.ACTION_FORCED_APPWIDGET_UPDATE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            remoteViews.setOnClickPendingIntent(R.id.widget_button_refresh, pendingIntent);

            Intent intentStartActivity = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent2 = PendingIntent.getActivity(context, 0,
                                                                     intentStartActivity, 0);
            remoteViews.setOnClickPendingIntent(R.id.widget_root, pendingIntent2);

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }

        context.startService(new Intent(context, LessWidgetService.class));
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        AppWidgetProviderAlarm appWidgetProviderAlarm =
                new AppWidgetProviderAlarm(context, LessWidgetProvider.class);
        appWidgetProviderAlarm.cancelAlarm();
    }

    private void preLoadWeather(Context context, RemoteViews remoteViews) {
        SharedPreferences weatherPref = context.getSharedPreferences(Constants.PREF_WEATHER_NAME,
                                                                     Context.MODE_PRIVATE);
        String temperatureScale = Utils.getTemperatureScale(context);

        String temperature = String.format(Locale.getDefault(), "%.0f", weatherPref
                .getFloat(Constants.WEATHER_DATA_TEMPERATURE, 0));
        String description = weatherPref.getString(Constants.WEATHER_DATA_DESCRIPTION, "clear sky");
        String iconId = weatherPref.getString(Constants.WEATHER_DATA_ICON, "01d");
        String weatherIcon = Utils.getStrIcon(context, iconId);
        String lastUpdate = Utils.setLastUpdateTime(context,
                                                    AppPreference.getLastUpdateTimeMillis(context));
        remoteViews.setTextViewText(R.id.widget_city, Utils.getCityAndCountry(context));
        remoteViews.setTextViewText(R.id.widget_temperature,
                                    temperature + temperatureScale);
        if(!AppPreference.hideDescription(context))
            remoteViews.setTextViewText(R.id.widget_description, description);
        else remoteViews.setTextViewText(R.id.widget_description, " ");
        remoteViews.setImageViewBitmap(R.id.widget_icon,
                                       Utils.createWeatherIcon(context, weatherIcon));
        remoteViews.setTextViewText(R.id.widget_last_update, lastUpdate);
    }

    private void setWidgetTheme(Context context, RemoteViews remoteViews) {

        int textColorId = AppPreference.getTextColor(context);
        int backgroundColorId = AppPreference.getBackgroundColor(context);
        int windowHeaderBackgroundColorId = AppPreference.getWindowHeaderBackgroundColorId(context);
        
        remoteViews.setInt(R.id.widget_root, "setBackgroundColor", backgroundColorId);
        remoteViews.setTextColor(R.id.widget_temperature, textColorId);
        remoteViews.setTextColor(R.id.widget_description, textColorId);
        remoteViews.setInt(R.id.header_layout, "setBackgroundColor", windowHeaderBackgroundColorId);
    }
}
