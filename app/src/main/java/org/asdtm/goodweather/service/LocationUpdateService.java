package org.asdtm.goodweather.service;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import org.asdtm.goodweather.MainActivity;
import org.asdtm.goodweather.utils.AppPreference;
import org.asdtm.goodweather.utils.Constants;
import org.asdtm.goodweather.utils.Utils;
import org.asdtm.goodweather.widget.ExtLocationWidgetService;
import org.asdtm.goodweather.widget.LessWidgetService;
import org.asdtm.goodweather.widget.MoreWidgetService;

import java.util.Calendar;
import java.util.Locale;

import static org.asdtm.goodweather.utils.LogToFile.appendLog;

public class LocationUpdateService extends Service implements LocationListener {

    private static final String TAG = "LocationUpdateService";

    private static final long LOCATION_TIMEOUT_IN_MS = 30000L;
    private static final long GPS_LOCATION_TIMEOUT_IN_MS = 180000L;
    private static final float LENGTH_UPDATE_LOCATION_LIMIT = 10000;
    private static final float LENGTH_UPDATE_LOCATION_LIMIT_NO_LOCATION = 800;
    private static final long UPDATE_WEATHER_ONLY_TIMEOUT = 900000; //15 min
    private static final long ACCELEROMETER_UPDATE_TIME_SPAN = 900000000000l; //15 min
    private static final long ACCELEROMETER_UPDATE_TIME_SPAN_NO_LOCATION = 300000000000l; //5 min

    private PowerManager powerManager;
    private LocationManager locationManager;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    private String updateSource;
    private String locationSource;

    private long lastLocationUpdateTime;
    private volatile long lastUpdatedWeather = 0;
    private long lastUpdatedPossition = 0;
    private long lastUpdate = 0;
    private float currentLength = 0;
    private volatile boolean noLocationFound;
    private float gravity[] = new float[3];
    private MoveVector lastMovement;

    private SensorEventListener sensorListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            try {
                Sensor mySensor = sensorEvent.sensor;

                if (mySensor.getType() != Sensor.TYPE_ACCELEROMETER) {
                    return;
                }
                processSensorEvent(sensorEvent);
            } catch (Exception e) {
                appendLog(getBaseContext(), TAG, "Exception on onSensorChanged", e);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };

    private BroadcastReceiver screenOnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            appendLog(context, TAG, "receive intent: " + intent);
            long now = Calendar.getInstance().getTimeInMillis();
            appendLog(context, TAG, "SCREEN_ON called, lastUpdate=" + lastUpdatedWeather + ", now=" + now);
            if (now < (lastUpdatedWeather + UPDATE_WEATHER_ONLY_TIMEOUT)) {
                timerScreenOnHandler.postDelayed(timerScreenOnRunnable, UPDATE_WEATHER_ONLY_TIMEOUT - (now - lastUpdatedWeather));
                return;
            }
            locationSource = "-";
            requestWeatherCheck();
            timerScreenOnHandler.postDelayed(timerScreenOnRunnable, UPDATE_WEATHER_ONLY_TIMEOUT);
        }
    };

    private BroadcastReceiver screenOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            appendLog(context, TAG, "receive intent: " + intent);
            long now = Calendar.getInstance().getTimeInMillis();
            appendLog(context, TAG, "SCREEN_OFF called, lastUpdate=" + lastUpdatedWeather + ", now=" + now);
            timerScreenOnHandler.removeCallbacksAndMessages(null);
        }
    };

    Handler timerScreenOnHandler = new Handler();
    Runnable timerScreenOnRunnable = new Runnable() {

        @Override
        public void run() {
            if (!powerManager.isScreenOn()) {
                return;
            }
            locationSource = "-";
            requestWeatherCheck();
            timerScreenOnHandler.postDelayed(timerScreenOnRunnable, UPDATE_WEATHER_ONLY_TIMEOUT);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        currentLength = 0;
        lastUpdate = 0;
        lastUpdatedPossition = 0;
        gravity[0] = 0;
        gravity[1] = 0;
        gravity[2] = 0;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        int ret = super.onStartCommand(intent, flags, startId);
        
        if (intent == null) {
            return ret;
        }

        appendLog(getBaseContext(), TAG, "onStartCommand:intent.getAction():" + intent.getAction());
        if ("android.intent.action.START_SENSOR_BASED_UPDATES".equals(intent.getAction())) {
            if (senSensorManager != null) {
                return ret;
            }
            appendLog(getBaseContext(), TAG, "START_SENSOR_BASED_UPDATES recieved");
            String updateSourceForSensors = intent.getExtras().getString("updateSource");
            if(!TextUtils.isEmpty(updateSourceForSensors)) {
                updateSource = updateSourceForSensors;
            }
            senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            senSensorManager.registerListener(sensorListener, senAccelerometer , SensorManager.SENSOR_DELAY_FASTEST);
            IntentFilter filterScreenOn = new IntentFilter(Intent.ACTION_SCREEN_ON);
            IntentFilter filterScreenOff = new IntentFilter(Intent.ACTION_SCREEN_OFF);
            getApplication().registerReceiver(screenOnReceiver, filterScreenOn);
            getApplication().registerReceiver(screenOffReceiver, filterScreenOff);
            noLocationFound = !getSharedPreferences(Constants.APP_SETTINGS_NAME, Context.MODE_PRIVATE).getBoolean(Constants.APP_SETTINGS_ADDRESS_FOUND, true);
            return START_STICKY;
        }

        if ("android.intent.action.STOP_SENSOR_BASED_UPDATES".equals(intent.getAction())) {
            if (senSensorManager == null) {
                return ret;
            }
            appendLog(getBaseContext(), TAG, "STOP_SENSOR_BASED_UPDATES recieved");
            getApplication().unregisterReceiver(screenOnReceiver);
            getApplication().unregisterReceiver(screenOffReceiver);
            senSensorManager.unregisterListener(sensorListener);
            senSensorManager = null;
            senAccelerometer = null;
            return ret;
        }

        if ("android.intent.action.LOCATION_UPDATE".equals(intent.getAction()) && (intent.getExtras() != null)) {
            Location location = (Location) intent.getExtras().getParcelable("location");
            Address addresses = (Address) intent.getExtras().getParcelable("addresses");
            appendLog(getBaseContext(), TAG, "LOCATION_UPDATE recieved:" + location + ":" + addresses);
            onLocationChanged(location, addresses);
            return ret;
        }

        if ("android.intent.action.START_LOCATION_AND_WEATHER_UPDATE".equals(intent.getAction()) && (intent.getExtras() != null)) {
            String currentUpdateSource = intent.getExtras().getString("updateSource");
            if (!TextUtils.isEmpty(currentUpdateSource)) {
                updateSource = currentUpdateSource;
            }
            locationSource = "-";
            if (AppPreference.isUpdateLocationEnabled(this)) {
                if ("location_geocoder_unifiednlp".equals(AppPreference.getLocationGeocoderSource(this))) {
                    appendLog(getBaseContext(), TAG, "Widget calls to update location");
                    updateNetworkLocation();
                } else {
                    requestLocation();
                }
            } else {
                requestWeatherCheck();
            }
        }
        
        return ret;
    }

    @Override
    public void onLocationChanged(Location location) {
        locationSource = "G";
        onLocationChanged(location, null);
    }
    
    public void onLocationChanged(Location location, Address address) {
        
        lastLocationUpdateTime = System.currentTimeMillis();
        timerHandler.removeCallbacksAndMessages(null);
        removeUpdates(this);
        
        if(location == null) {
            gpsRequestLocation();
            return;
        }
        
        String latitude = String.format("%1$.2f", location.getLatitude());
        String longitude = String.format("%1$.2f", location.getLongitude());
        Log.d(TAG, "Lat: " + latitude + "; Long: " + longitude);
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.APP_SETTINGS_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(Constants.APP_SETTINGS_LATITUDE, latitude);
        editor.putString(Constants.APP_SETTINGS_LONGITUDE, longitude);
        
        if ((location.getExtras() != null) && (location.getExtras().containsKey("source"))) {
            String networkSource = location.getExtras().getString("source");
            StringBuilder networkSourceBuilder = new StringBuilder();
            networkSourceBuilder.append("N");
            
            String updateDetailLevel = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(
                Constants.KEY_PREF_UPDATE_DETAIL, "preference_display_update_nothing");
            
            if ((networkSource != null) && (updateDetailLevel.equals("preference_display_update_location_source"))) {
                if (networkSource.contains("cells")) {
                    networkSourceBuilder.append("c");
                }
                if (networkSource.contains("wifis")) {
                    networkSourceBuilder.append("w");
                }
                appendLog(getBaseContext(), TAG, "send update source to " + networkSourceBuilder.toString());
                locationSource = networkSourceBuilder.toString();
            }
        } else if ("-".equals(locationSource)) {
            locationSource = "N";
        }
        editor.apply();
        boolean resolveAddressByOS = !"location_geocoder_unifiednlp".equals(AppPreference.getLocationGeocoderSource(this));
        noLocationFound = false;
        Utils.getAndWriteAddressFromGeocoder(new Geocoder(this, Locale.getDefault()),
                                             address,
                                             latitude,
                                             longitude,
                                             resolveAddressByOS,
                                             this);
        appendLog(getBaseContext(), TAG, "send intent to get weather, updateSource " + updateSource);
        requestWeatherCheck();
    }

    Handler lastKnownLocationTimerHandler = new Handler();
    Runnable lastKnownLocationTimerRunnable = new Runnable() {

        @Override
        public void run() {
            appendLog(getBaseContext(), TAG, "send update source to N - update location by network, lastKnownLocation timeouted");
            updateNetworkLocationByNetwork(null);
        }
    };

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            locationSource = "-";
            requestWeatherCheck();
        }
    };

    Handler timerHandlerGpsLocation = new Handler();
    Runnable timerRunnableGpsLocation = new Runnable() {

        @Override
        public void run() {
            locationManager.removeUpdates(gpsLocationListener);
            setNoLocationFound();
        }
    };

    final LocationListener gpsLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            locationManager.removeUpdates(gpsLocationListener);
            timerHandlerGpsLocation.removeCallbacksAndMessages(null);
            Intent sendIntent = new Intent("android.intent.action.START_LOCATION_UPDATE");
            sendIntent.setPackage("org.microg.nlp");
            sendIntent.putExtra("destinationPackageName", "org.asdtm.goodweather");
            sendIntent.putExtra("location", location);
            sendIntent.putExtra("resolveAddress", true);
            startService(sendIntent);
            appendLog(getBaseContext(), TAG, "send intent START_LOCATION_UPDATE:locationSource G:" + sendIntent);
            locationSource = "G";
            timerHandler.postDelayed(timerRunnable, LOCATION_TIMEOUT_IN_MS);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {}

        @Override
        public void onProviderEnabled(String s) {}

        @Override
        public void onProviderDisabled(String s) {
            locationManager.removeUpdates(gpsLocationListener);
        }
    };
    
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
        removeUpdates(this);
    }

    public void gpsRequestLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Looper locationLooper = Looper.myLooper();
            appendLog(getBaseContext(), TAG, "get location from GPS");
            timerHandlerGpsLocation.postDelayed(timerRunnableGpsLocation, GPS_LOCATION_TIMEOUT_IN_MS);
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, gpsLocationListener, locationLooper);
        }
    }

    private void setNoLocationFound() {
        noLocationFound = true;
        Utils.setNoLocationFound(this);
        updateWidgets();
    }

    private void updateNetworkLocation() {

        if (!checkLocationProviderPermission()) {
            return;
        }
        startRefreshRotation();
        try {
            lastKnownLocationTimerHandler.postDelayed(lastKnownLocationTimerRunnable, LOCATION_TIMEOUT_IN_MS);
            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            lastKnownLocationTimerHandler.removeCallbacksAndMessages(null);
            updateNetworkLocationByNetwork(lastLocation);
        } catch (Exception e) {
            appendLog(getBaseContext(), TAG, "Exception during update of network location", e);
        }
    }

    private void startRefreshRotation() {
        Intent sendIntent = new Intent("android.intent.action.START_ROTATING_UPDATE");
        sendIntent.setPackage("org.asdtm.goodweather");
        startService(sendIntent);
    }

    private void stopRefreshRotation() {
        Intent sendIntent = new Intent("android.intent.action.STOP_ROTATING_UPDATE");
        sendIntent.setPackage("org.asdtm.goodweather");
        startService(sendIntent);
    }

    private void updateNetworkLocationByNetwork(Location lastLocation) {
        Intent sendIntent = new Intent("android.intent.action.START_LOCATION_UPDATE");
        sendIntent.setPackage("org.microg.nlp");
        sendIntent.putExtra("destinationPackageName", "org.asdtm.goodweather");

        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, -5);

        if ((lastLocation != null) && lastLocation.getTime() > now.getTimeInMillis()) {
            sendIntent.putExtra("location", lastLocation);
            locationSource = "G";
        }

        sendIntent.putExtra("resolveAddress", true);
        startService(sendIntent);
        appendLog(getBaseContext(), TAG, "send intent START_LOCATION_UPDATE:updatesource is N or G:" + sendIntent);
        timerHandler.postDelayed(timerRunnable, LOCATION_TIMEOUT_IN_MS);
    }

    private boolean checkLocationProviderPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    private void removeUpdates(LocationListener locationListener) {
        if("location_geocoder_system".equals(AppPreference.getLocationGeocoderSource(this))) {
            locationManager.removeUpdates(locationListener);
        }
    }
    
    private void requestLocation() {
        int fineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (fineLocationPermission != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
        } else {
            detectLocation();
        }
    }

    private void detectLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            final Looper locationLooper = Looper.myLooper();
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, locationLooper);
            final LocationListener locationListener = this;
            final Handler locationHandler = new Handler(locationLooper);
            locationHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    locationManager.removeUpdates(locationListener);
                    if ((System.currentTimeMillis() - (2 * LOCATION_TIMEOUT_IN_MS)) < lastLocationUpdateTime) {
                        return;
                    }
                    locationSource = "-";
                    if (ContextCompat.checkSelfPermission(LocationUpdateService.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Location lastNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        Location lastGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if ((lastGpsLocation == null) && (lastNetworkLocation != null)) {
                            locationSource = "N";
                            locationListener.onLocationChanged(lastNetworkLocation);
                        } else if ((lastGpsLocation != null) && (lastNetworkLocation == null)) {
                            locationSource = "G";
                            locationListener.onLocationChanged(lastGpsLocation);
                        } else {
                            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                            new CountDownTimer(30000, 10000) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                }

                                @Override
                                public void onFinish() {
                                    locationManager.removeUpdates(LocationUpdateService.this);
                                    stopSelf();
                                }
                            }.start();
                        }
                    }
                    requestWeatherCheck();
                }
            }, LOCATION_TIMEOUT_IN_MS);
        }
    }
    
    private void requestWeatherCheck() {
        startRefreshRotation();
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.APP_SETTINGS_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        appendLog(getBaseContext(), TAG, "send update source to " + locationSource);
        editor.putString(Constants.APP_SETTINGS_UPDATE_SOURCE, locationSource);
        editor.apply();
        lastUpdatedWeather = Calendar.getInstance().getTimeInMillis();
        Intent intentToCheckWeather = new Intent(getBaseContext(), CurrentWeatherService.class);
        intentToCheckWeather.putExtra("updateSource", updateSource);
        startService(intentToCheckWeather);
        if ("MAIN".equals(updateSource)) {
            MainActivity.mProgressDialog.cancel();
        }
    }
    
    private void updateWidgets() {
        stopRefreshRotation();
        if (updateSource == null) {
            return;
        }
        
        switch (updateSource) {
            case "MAIN" : sendIntentToMain();break;
            case "LESS_WIDGET" : startService(new Intent(getBaseContext(), LessWidgetService.class));break;
            case "MORE_WIDGET" : startService(new Intent(getBaseContext(), MoreWidgetService.class));break;
            case "EXT_LOC_WIDGET" : startService(new Intent(getBaseContext(), ExtLocationWidgetService.class));break;
        }
    }
    
    private void sendIntentToMain() {
        Intent intent = new Intent(CurrentWeatherService.ACTION_WEATHER_UPDATE_RESULT);
        intent.putExtra(CurrentWeatherService.ACTION_WEATHER_UPDATE_RESULT, CurrentWeatherService.ACTION_WEATHER_UPDATE_FAIL);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void processSensorEvent(SensorEvent sensorEvent) {
        double countedtLength = 0;
        double countedAcc = 0;
        long now = sensorEvent.timestamp;
        try {
            final float dT = (float) (now - lastUpdate) / 1000000000.0f;
            lastUpdate = now;

            if (lastMovement != null) {
                countedAcc = (float) Math.sqrt((lastMovement.getX() * lastMovement.getX()) + (lastMovement.getY() * lastMovement.getY()) + (lastMovement.getZ() * lastMovement.getZ()));
                countedtLength = countedAcc * dT *dT;

                float lowPassConst = 0.1f;

                if (countedAcc < lowPassConst) {
                    if (dT > 1.0f) {
                        appendLog(getBaseContext(), TAG, "acc under limit, currentLength = " + String.format("%.8f", currentLength) +
                                ":counted length = " + String.format("%.8f", countedtLength) + ":countedAcc = " + countedAcc +
                                ", dT = " + String.format("%.8f", dT));
                    }
                    lastMovement = highPassFilter(sensorEvent);
                    return;
                }
                currentLength += countedtLength;
            } else {
                countedtLength = 0;
                countedAcc = 0;
            }
            lastMovement = highPassFilter(sensorEvent);

            if ((lastUpdate%1000 < 5) || (countedtLength > 10)) {
                appendLog(getBaseContext(), TAG, "current currentLength = " + String.format("%.8f", currentLength) +
                        ":counted length = " + String.format("%.8f", countedtLength) + ":countedAcc = " + countedAcc +
                        ", dT = " + String.format("%.8f", dT));
            }
            float absCurrentLength = Math.abs(currentLength);

            if (((lastUpdate < (lastUpdatedPossition + ACCELEROMETER_UPDATE_TIME_SPAN)) || (absCurrentLength < LENGTH_UPDATE_LOCATION_LIMIT))
                    && (!noLocationFound || (lastUpdate < (lastUpdatedPossition + ACCELEROMETER_UPDATE_TIME_SPAN_NO_LOCATION)) || (absCurrentLength < LENGTH_UPDATE_LOCATION_LIMIT_NO_LOCATION))) {
                return;
            }

            appendLog(getBaseContext(), TAG, "end currentLength = " + String.format("%.8f", absCurrentLength));
        } catch (Exception e) {
            appendLog(getBaseContext(), TAG, "Exception when processSensorQueue", e);
            return;
        }

        noLocationFound = false;
        gravity[0] = 0;
        gravity[1] = 0;
        gravity[2] = 0;
        lastUpdatedPossition = lastUpdate;
        currentLength = 0;

        lastUpdatedWeather = Calendar.getInstance().getTimeInMillis();
        locationSource = "-";
        updateNetworkLocation();
    }

    private MoveVector highPassFilter(SensorEvent sensorEvent) {
        final float alpha = 0.8f;

        gravity[0] = alpha * gravity[0] + (1 - alpha) * sensorEvent.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * sensorEvent.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * sensorEvent.values[2];

        return new MoveVector(sensorEvent.values[0] - gravity[0], sensorEvent.values[1] - gravity[1], sensorEvent.values[2] - gravity[2]);
    }

    private class MoveVector {
        private final float x;
        private final float y;
        private final float z;

        public MoveVector(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public float getX() {
            return x;
        }
        public float getY() {
            return y;
        }
        public float getZ() {
            return z;
        }
    }
}
