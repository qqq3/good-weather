package org.asdtm.goodweather.service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import org.asdtm.goodweather.MainActivity;
import org.asdtm.goodweather.R;
import org.asdtm.goodweather.utils.AppPreference;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import java.util.Calendar;
import java.util.Locale;

import org.asdtm.goodweather.utils.Constants;
import org.asdtm.goodweather.utils.Utils;
import org.asdtm.goodweather.widget.ExtLocationWidgetProvider;
import org.asdtm.goodweather.widget.ExtLocationWidgetService;
import org.asdtm.goodweather.widget.LessWidgetService;
import org.asdtm.goodweather.widget.MoreWidgetService;

import static org.asdtm.goodweather.utils.LogToFile.appendLog;

public class LocationUpdateService extends Service implements LocationListener {

    private static final String TAG = "LocationUpdateService";

    private static final long LOCATION_TIMEOUT_IN_MS = 30000L;

    private LocationManager locationManager;
    
    private String updateSource;

    private long lastLocationUpdateTime;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        int ret = super.onStartCommand(intent, flags, startId);
        
        if (intent == null) {
            return ret;
        }

        if ("android.intent.action.LOCATION_UPDATE".equals(intent.getAction()) && (intent.getExtras() != null)) {
            Location location = (Location) intent.getExtras().getParcelable("location");
            Address addresses = (Address) intent.getExtras().getParcelable("addresses");
            appendLog(getBaseContext(), TAG, "LOCATION_UPDATE recieved:" + location + ":" + addresses);
            onLocationChanged(location, addresses);
            return ret;
        }
        
        String currentUpdateSource = intent.getExtras().getString("updateSource");
        if(!TextUtils.isEmpty(currentUpdateSource)) {
            updateSource = currentUpdateSource;
        }
        if(AppPreference.isUpdateLocationEnabled(this)) {
            if("location_geocoder_unifiednlp".equals(AppPreference.getLocationGeocoderSource(this))) {
                appendLog(getBaseContext(), TAG, "Widget calls to update location");
                updateNetworkLocation();
            } else {
                requestLocation();
            }
        } else {
            requestWeatherCheck();
        }
        
        return ret;
    }

    @Override
    public void onLocationChanged(Location location) {
        onLocationChanged(location, null);
    }
    
    public void onLocationChanged(Location location, Address address) {
        
        lastLocationUpdateTime = System.currentTimeMillis();
        timerHandler.removeCallbacks(timerRunnable);
        removeUpdates(this);
        
        if(location == null) {
            setNoLocationFound();
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
                editor.putString(Constants.APP_SETTINGS_UPDATE_SOURCE, networkSourceBuilder.toString());
            }
        }
        boolean resolveAddressByOS = !"location_geocoder_unifiednlp".equals(AppPreference.getLocationGeocoderSource(this));
        Utils.getAndWriteAddressFromGeocoder(new Geocoder(this, Locale.getDefault()),
                                             address,
                                             latitude,
                                             longitude,
                                             resolveAddressByOS,
                                             editor);
        editor.apply();
        
        appendLog(getBaseContext(), TAG, "send intent to get weather, updateSource " + updateSource);
        requestWeatherCheck();
    }

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {            
            SharedPreferences mSharedPreferences = getSharedPreferences(Constants.APP_SETTINGS_NAME,
                Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(Constants.APP_SETTINGS_UPDATE_SOURCE, "W");
            editor.apply();
            appendLog(getBaseContext(), TAG, "send update source to W - update weather only");
            requestWeatherCheck();
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
    
    private void setNoLocationFound() {
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.APP_SETTINGS_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(Constants.APP_SETTINGS_GEO_CITY, getString(R.string.location_not_found));
        editor.putString(Constants.APP_SETTINGS_GEO_COUNTRY_NAME, "");
        editor.putString(Constants.APP_SETTINGS_GEO_DISTRICT_OF_COUNTRY, "");
        editor.putString(Constants.APP_SETTINGS_GEO_DISTRICT_OF_CITY, "");
        long now = System.currentTimeMillis();
        editor.putLong(Constants.LAST_UPDATE_TIME_IN_MS, now);
        editor.apply();
        updateWidgets();
    }
    
    private void updateNetworkLocation() {
        Intent sendIntent = new Intent("android.intent.action.START_LOCATION_UPDATE");
        sendIntent.setPackage("org.microg.nlp");
        sendIntent.putExtra("destinationPackageName", "org.asdtm.goodweather");

        if (!checkLocationProviderPermission()) {
            return;
        }
        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, -5);
        
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.APP_SETTINGS_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        if ((lastLocation != null) && lastLocation.getTime() > now.getTimeInMillis()) {
            sendIntent.putExtra("location", lastLocation);
            editor.putString(Constants.APP_SETTINGS_UPDATE_SOURCE, "G");
        } else {
            editor.putString(Constants.APP_SETTINGS_UPDATE_SOURCE, "N");
        }
        editor.apply();
        
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
                    if (ContextCompat.checkSelfPermission(LocationUpdateService.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Location lastNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        Location lastGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if ((lastGpsLocation == null) && (lastNetworkLocation != null)) {
                            locationListener.onLocationChanged(lastNetworkLocation);
                        } else if ((lastGpsLocation != null) && (lastNetworkLocation == null)) {
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
        Intent intentToCheckWeather = new Intent(getBaseContext(), CurrentWeatherService.class);
        intentToCheckWeather.putExtra("updateSource", updateSource);
        startService(intentToCheckWeather);
        if ("MAIN".equals(updateSource)) {
            MainActivity.mProgressDialog.cancel();
        }
    }
    
    private void updateWidgets() {
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
}
