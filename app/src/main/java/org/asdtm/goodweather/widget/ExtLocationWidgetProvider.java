package org.asdtm.goodweather.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import org.asdtm.goodweather.R;
import org.asdtm.goodweather.utils.AppPreference;
import org.asdtm.goodweather.utils.Constants;
import org.asdtm.goodweather.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ExtLocationWidgetProvider extends AbstractWidgetProvider {

    private static final String TAG = "WidgetExtLocInfo";

    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

    private static final String WIDGET_NAME = "EXT_LOC_WIDGET";

    @Override
    protected void preLoadWeather(Context context, RemoteViews remoteViews) {
        SharedPreferences weatherPref = context.getSharedPreferences(Constants.PREF_WEATHER_NAME,
                Context.MODE_PRIVATE);
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

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(1000 * weatherPref.getLong(Constants.WEATHER_DATA_SUNRISE, 0));
        String sunrise = context.getString(R.string.sunrise_label,
                sdf.format(calendar.getTime()));
        calendar.setTimeInMillis(1000 * weatherPref.getLong(Constants.WEATHER_DATA_SUNSET, 0));
        String sunset = context.getString(R.string.sunset_label, sdf.format(calendar.getTime()));

        String lastUpdate = Utils.setLastUpdateTime(context,
                AppPreference.getLastUpdateTimeMillis(context));

        remoteViews.setTextViewText(R.id.widget_city, Utils.getCityAndCountry(context));
        remoteViews.setTextViewText(R.id.widget_temperature, temperature + temperatureScale);
        if(!AppPreference.hideDescription(context))
            remoteViews.setTextViewText(R.id.widget_description, description);
        else remoteViews.setTextViewText(R.id.widget_description, " ");
        remoteViews.setTextViewText(R.id.widget_wind, wind);
        remoteViews.setTextViewText(R.id.widget_humidity, humidity);
        remoteViews.setTextViewText(R.id.widget_sunrise, sunrise);
        remoteViews.setTextViewText(R.id.widget_sunset, sunset);
        remoteViews.setImageViewResource(R.id.widget_icon, Utils.getWeatherResourceIcon(weatherPref));
        remoteViews.setTextViewText(R.id.widget_last_update, lastUpdate);
    }

    @Override
    protected void setWidgetTheme(Context context, RemoteViews remoteViews) {
        int textColorId = AppPreference.getTextColor(context);
        int backgroundColorId = AppPreference.getBackgroundColor(context);
        int windowHeaderBackgroundColorId = AppPreference.getWindowHeaderBackgroundColorId(context);

        remoteViews.setInt(R.id.widget_root, "setBackgroundColor", backgroundColorId);
        remoteViews.setTextColor(R.id.widget_temperature, textColorId);
        remoteViews.setTextColor(R.id.widget_description, textColorId);
        remoteViews.setTextColor(R.id.widget_description, textColorId);
        remoteViews.setTextColor(R.id.widget_wind, textColorId);
        remoteViews.setTextColor(R.id.widget_humidity, textColorId);
        remoteViews.setTextColor(R.id.widget_sunrise, textColorId);
        remoteViews.setTextColor(R.id.widget_sunset, textColorId);
        remoteViews.setInt(R.id.header_layout, "setBackgroundColor", windowHeaderBackgroundColorId);
    }

    @Override
    protected int getWidgetLayout() {
        return R.layout.widget_ext_loc_3x3;
    }

    @Override
    protected Class<?> getWidgetClass() {
        return ExtLocationWidgetProvider.class;
    }

    @Override
    protected String getWidgetName() {
        return WIDGET_NAME;
    }
}
