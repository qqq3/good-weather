package org.asdtm.goodweather.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.RemoteViews;

import org.asdtm.goodweather.MainActivity;
import org.asdtm.goodweather.R;
import org.asdtm.goodweather.service.LocationUpdateService;
import org.asdtm.goodweather.utils.AppPreference;
import org.asdtm.goodweather.utils.AppWidgetProviderAlarm;
import org.asdtm.goodweather.utils.Constants;

public abstract class AbstractWidgetProvider extends AppWidgetProvider {

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        AppWidgetProviderAlarm appWidgetProviderAlarm =
                new AppWidgetProviderAlarm(context, getWidgetClass());
        appWidgetProviderAlarm.setAlarm();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        context.getApplicationContext().registerReceiver(this, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case Constants.ACTION_FORCED_APPWIDGET_UPDATE:
                if(AppPreference.isUpdateLocationEnabled(context)) {
                    Intent startLocationUpdateIntent = new Intent(context, LocationUpdateService.class);
                    startLocationUpdateIntent.putExtra("updateSource", getWidgetName());
                    context.startService(startLocationUpdateIntent);
                } else {
                    context.startService(new Intent(context, getWidgetClass()));
                }
                break;
            case Intent.ACTION_LOCALE_CHANGED:
                context.startService(new Intent(context, getWidgetClass()));
                break;
            case Intent.ACTION_SCREEN_ON:
            case Constants.ACTION_APPWIDGET_THEME_CHANGED:
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                ComponentName componentName = new ComponentName(context, getWidgetClass());
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
                onUpdate(context, appWidgetManager, appWidgetIds);
                break;
            case Constants.ACTION_APPWIDGET_UPDATE_PERIOD_CHANGED:
                AppWidgetProviderAlarm widgetProviderAlarm =
                        new AppWidgetProviderAlarm(context, getWidgetClass());
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
                    getWidgetLayout());

            setWidgetTheme(context, remoteViews);
            preLoadWeather(context, remoteViews);

            Intent intentRefreshService = new Intent(context, getWidgetClass());
            intentRefreshService.setAction(Constants.ACTION_FORCED_APPWIDGET_UPDATE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                    intentRefreshService, 0);
            remoteViews.setOnClickPendingIntent(R.id.widget_button_refresh, pendingIntent);

            Intent intentStartActivity = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent2 = PendingIntent.getActivity(context, 0,
                    intentStartActivity, 0);
            remoteViews.setOnClickPendingIntent(R.id.widget_root, pendingIntent2);

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }

        context.startService(new Intent(context, getWidgetClass()));
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        AppWidgetProviderAlarm appWidgetProviderAlarm =
                new AppWidgetProviderAlarm(context, getWidgetClass());
        appWidgetProviderAlarm.cancelAlarm();
    }

    protected abstract void preLoadWeather(Context context, RemoteViews remoteViews);

    protected abstract void setWidgetTheme(Context context, RemoteViews remoteViews);

    protected abstract Class<?> getWidgetClass();

    protected abstract String getWidgetName();

    protected abstract int getWidgetLayout();
}
