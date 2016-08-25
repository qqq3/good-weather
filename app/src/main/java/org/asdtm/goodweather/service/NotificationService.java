package org.asdtm.goodweather.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import org.asdtm.goodweather.ConnectionDetector;
import org.asdtm.goodweather.MainActivity;
import org.asdtm.goodweather.R;
import org.asdtm.goodweather.WeatherJSONParser;
import org.asdtm.goodweather.WeatherRequest;
import org.asdtm.goodweather.model.Weather;
import org.asdtm.goodweather.utils.AppPreference;
import org.asdtm.goodweather.utils.Constants;
import org.json.JSONException;

import java.io.IOException;
import java.util.Locale;

public class NotificationService extends IntentService {

    private static final String TAG = "NotificationsService";

    public NotificationService() {
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

        Weather weather = new Weather();
        try {
            String weatherRaw = new WeatherRequest().getItems(latitude, longitude, units, locale);
            weather = WeatherJSONParser.getWeather(weatherRaw);
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error get weather", e);
        }

        weatherNotification(weather);
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, NotificationService.class);
    }

    public static void setNotificationServiceAlarm(Context context,
                                                   boolean isNotificationEnable) {
        Intent intent = NotificationService.newIntent(context);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent,
                                                               PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        String intervalPref = AppPreference.getInterval(context);
        long intervalMillis = intervalMillisForAlarm(intervalPref);
        if (isNotificationEnable) {

            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                             SystemClock.elapsedRealtime() + intervalMillis,
                                             intervalMillis,
                                             pendingIntent);
        } else {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    private static long intervalMillisForAlarm(String intervalPref) {
        int interval = Integer.parseInt(intervalPref);
        switch (interval) {
            case 15:
                return AlarmManager.INTERVAL_FIFTEEN_MINUTES;
            case 30:
                return AlarmManager.INTERVAL_HALF_HOUR;
            case 60:
                return AlarmManager.INTERVAL_HOUR;
            case 720:
                return AlarmManager.INTERVAL_HALF_DAY;
            case 1440:
                return AlarmManager.INTERVAL_DAY;
            default:
                return AlarmManager.INTERVAL_HOUR;
        }
    }

    private void weatherNotification(Weather weather) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent launchIntent = PendingIntent.getActivity(this, 0, intent, 0);

        String[] temperatureUnitsArray = getResources().getStringArray(
                R.array.pref_temperature_entries);
        String temperatureUnitPref = AppPreference.getTemperatureUnit(this);
        String temperatureUnit = temperatureUnitPref.equals("metric") ?
                temperatureUnitsArray[0] : temperatureUnitsArray[1];
        String windUnit = temperatureUnitPref.equals("metric") ?
                getString(R.string.wind_speed_meters) : getString(R.string.wind_speed_miles);

        String temperature = String.format(Locale.getDefault(), "%.1f",
                                           weather.temperature.getTemp());

        String wind = getString(R.string.wind_label, String.format(Locale.getDefault(),
                                                                   "%.1f",
                                                                   weather.wind.getSpeed()));
        String humidity = getString(R.string.humidity_label,
                                    String.valueOf(weather.currentCondition.getHumidity()));
        String pressure = getString(R.string.pressure_label,
                                    String.format(Locale.getDefault(), "%.1f",
                                                  weather.currentCondition.getPressure()));
        String cloudiness = getString(R.string.pressure_label,
                                      String.valueOf(weather.cloud.getClouds()));

        StringBuilder notificationText = new StringBuilder(wind)
                .append(" ")
                .append(windUnit)
                .append("  ")
                .append(humidity)
                .append(getString(R.string.percent_sign))
                .append("  ")
                .append(pressure)
                .append(" ")
                .append(getString(R.string.pressure_measurement))
                .append("  ")
                .append(cloudiness)
                .append(getString(R.string.percent_sign));

        Notification notification = new NotificationCompat.Builder(this)
                .setContentIntent(launchIntent)
                .setSmallIcon(R.drawable.small_icon)
                .setTicker(temperature
                                   + temperatureUnit
                                   + "  "
                                   + weather.location.getCityName()
                                   + ", "
                                   + weather.location.getCountryCode())
                .setContentTitle(temperature
                                         + temperatureUnit
                                         + "  "
                                         + weather.currentWeather.getDescription())
                .setContentText(notificationText)
                .setVibrate(isVibrateEnabled())
                .setAutoCancel(true)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, notification);
    }

    private long[] isVibrateEnabled() {
        if (!AppPreference.isVibrateEnabled(this)) {
            return null;
        }
        return new long[]{500, 500, 500};
    }
}
