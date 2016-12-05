package org.asdtm.goodweather.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;

import org.asdtm.goodweather.MainActivity;
import org.asdtm.goodweather.R;
import org.asdtm.goodweather.utils.AppPreference;
import org.asdtm.goodweather.utils.AppWidgetProviderAlarm;
import org.asdtm.goodweather.utils.Constants;
import org.asdtm.goodweather.utils.Utils;

import java.util.Locale;

public class MoreWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "WidgetMoreInfo";

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        AppWidgetProviderAlarm appWidgetProviderAlarm =
                new AppWidgetProviderAlarm(context, MoreWidgetProvider.class);
        appWidgetProviderAlarm.setAlarm();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case Constants.ACTION_FORCED_APPWIDGET_UPDATE:
                context.startService(new Intent(context, MoreWidgetService.class));
                break;
            case Intent.ACTION_LOCALE_CHANGED:
                AppPreference.setLocale(context, Constants.APP_SETTINGS_NAME);
                context.startService(new Intent(context, MoreWidgetService.class));
                break;
            case Constants.ACTION_APPWIDGET_THEME_CHANGED:
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                ComponentName componentName = new ComponentName(context, MoreWidgetProvider.class);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
                onUpdate(context, appWidgetManager, appWidgetIds);
                break;
            case Constants.ACTION_APPWIDGET_UPDATE_PERIOD_CHANGED:
                AppWidgetProviderAlarm widgetProviderAlarm =
                        new AppWidgetProviderAlarm(context, MoreWidgetProvider.class);
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
                                                      R.layout.widget_more_3x3);

            setWidgetTheme(context, remoteViews);
            preLoadWeather(context, remoteViews);

            Intent intentRefreshService = new Intent(context, MoreWidgetProvider.class);
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

        context.startService(new Intent(context, MoreWidgetService.class));
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        AppWidgetProviderAlarm appWidgetProviderAlarm =
                new AppWidgetProviderAlarm(context, MoreWidgetProvider.class);
        appWidgetProviderAlarm.cancelAlarm();
    }

    private void preLoadWeather(Context context, RemoteViews remoteViews) {
        SharedPreferences weatherPref = context.getSharedPreferences(Constants.PREF_WEATHER_NAME,
                                                                     Context.MODE_PRIVATE);
        String[] cityAndCountryArray = AppPreference.getCityAndCode(context);
        String cityAndCountry = cityAndCountryArray[0] + ", " + cityAndCountryArray[1];
        String temperatureScale = Utils.getTemperatureScale(context);
        String speedScale = Utils.getSpeedScale(context);
        String percentSign = context.getString(R.string.percent_sign);
        String pressureMeasurement = context.getString(R.string.pressure_measurement);

        String temperature = String.format(Locale.getDefault(), "%.0f", weatherPref
                .getFloat(Constants.WEATHER_DATA_TEMPERATURE, 0));
        String description = weatherPref.getString(Constants.WEATHER_DATA_DESCRIPTION, "clear sky");
        String wind = context.getString(R.string.wind_label,
                                        String.format(Locale.getDefault(), "%.0f", weatherPref
                                                .getFloat(Constants.WEATHER_DATA_WIND_SPEED, 0)),
                                        speedScale);
        String humidity =
                context.getString(R.string.humidity_label,
                                  String.valueOf(
                                          weatherPref.getInt(Constants.WEATHER_DATA_HUMIDITY, 0)),
                                  percentSign);
        String pressure =
                context.getString(R.string.pressure_label,
                                  String.format(Locale.getDefault(),
                                                "%.1f",
                                                weatherPref
                                                        .getFloat(Constants.WEATHER_DATA_PRESSURE,
                                                                  0)),
                                  pressureMeasurement);
        String cloudiness =
                context.getString(R.string.cloudiness_label,
                                  String.valueOf(
                                          weatherPref.getInt(Constants.WEATHER_DATA_CLOUDS, 0)),
                                  percentSign);
        String iconId = weatherPref.getString(Constants.WEATHER_DATA_ICON, "01d");
        String weatherIcon = Utils.getStrIcon(context, iconId);
        String lastUpdate = Utils.setLastUpdateTime(context,
                                                    AppPreference.getLastUpdateTimeMillis(context));

        remoteViews.setTextViewText(R.id.widget_city, cityAndCountry);
        remoteViews.setTextViewText(R.id.widget_temperature, temperature + temperatureScale);
        if(!AppPreference.hideDescription(context))
            remoteViews.setTextViewText(R.id.widget_description, description);
        else remoteViews.setTextViewText(R.id.widget_description, " ");
        remoteViews.setTextViewText(R.id.widget_wind, wind);
        remoteViews.setTextViewText(R.id.widget_humidity, humidity);
        remoteViews.setTextViewText(R.id.widget_pressure, pressure);
        remoteViews.setTextViewText(R.id.widget_clouds, cloudiness);
        remoteViews.setImageViewBitmap(R.id.widget_icon,
                                       Utils.createWeatherIcon(context, weatherIcon));
        remoteViews.setTextViewText(R.id.widget_last_update, lastUpdate);
    }

    private void setWidgetTheme(Context context, RemoteViews remoteViews) {
        int textColorId;
        int backgroundColorId;

        if (!AppPreference.isLightThemeEnabled(context)) {
            backgroundColorId = ContextCompat.getColor(context,
                                                       R.color.widget_darkTheme_colorBackground);
            textColorId = ContextCompat.getColor(context,
                                                 R.color.widget_darkTheme_textColorPrimary);
        } else {
            backgroundColorId = ContextCompat.getColor(context,
                                                       R.color.widget_lightTheme_colorBackground);
            textColorId = ContextCompat.getColor(context,
                                                 R.color.widget_lightTheme_textColorPrimary);
        }

        remoteViews.setInt(R.id.widget_root, "setBackgroundColor", backgroundColorId);
        remoteViews.setTextColor(R.id.widget_temperature, textColorId);
        remoteViews.setTextColor(R.id.widget_description, textColorId);
        remoteViews.setTextColor(R.id.widget_description, textColorId);
        remoteViews.setTextColor(R.id.widget_wind, textColorId);
        remoteViews.setTextColor(R.id.widget_humidity, textColorId);
        remoteViews.setTextColor(R.id.widget_pressure, textColorId);
        remoteViews.setTextColor(R.id.widget_clouds, textColorId);
    }
}
