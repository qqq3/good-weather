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
import android.text.TextUtils;
import java.util.Locale;
import org.asdtm.goodweather.MainActivity;
import org.asdtm.goodweather.utils.AppPreference;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.asdtm.goodweather.utils.Constants;
import org.asdtm.goodweather.utils.Utils;
import org.asdtm.goodweather.widget.LessWidgetService;
import org.asdtm.goodweather.widget.MoreWidgetService;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

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
        if ("android.intent.action.LOCATION_UPDATE".equals(intent.getAction()) && (intent.getExtras() != null)) {
            Location location = (Location) intent.getExtras().getParcelable("location");
            Address addresses = (Address) intent.getExtras().getParcelable("addresses");
            onLocationChanged(location, addresses);
            return ret;
        }
        
        String currentUpdateSource = intent.getExtras().getString("updateSource");
        if(!TextUtils.isEmpty(currentUpdateSource)) {
            updateSource = intent.getExtras().getString("updateSource");
        }
        if(AppPreference.isUpdateLocationEnabled(this)) {
            if("location_geocoder_unifiednlp".equals(AppPreference.getLocationGeocoderSource(this))) {
                updateNetworkLocation();
            } else {
                requestLocation();
            }
        } else {
            switch (updateSource) {
                case "MAIN" : startService(new Intent(getBaseContext(), CurrentWeatherService.class));MainActivity.mProgressDialog.cancel();break;
                case "LESS_WIDGET" : startService(new Intent(getBaseContext(), LessWidgetService.class));break;
                case "MORE_WIDGET" : startService(new Intent(getBaseContext(), MoreWidgetService.class));break;
        }
        }
        
        return ret;
    }

    @Override
    public void onLocationChanged(Location location) {
        onLocationChanged(location, null);
    }
    
    public void onLocationChanged(Location location, Address address) {
        
        if(location == null) {
            return;
        }
        

        lastLocationUpdateTime = System.currentTimeMillis();
        String latitude = String.format("%1$.2f", location.getLatitude());
        String longitude = String.format("%1$.2f", location.getLongitude());
        Log.d(TAG, "Lat: " + latitude + "; Long: " + longitude);
        locationManager.removeUpdates(this);
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.APP_SETTINGS_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(Constants.APP_SETTINGS_LATITUDE, latitude);
        editor.putString(Constants.APP_SETTINGS_LONGITUDE, longitude);
        Utils.getAndWriteAddressFromGeocoder(new Geocoder(this, Locale.getDefault()), address, latitude, longitude, editor);
        editor.apply();
        
        removeUpdates(this);
        switch (updateSource) {
            case "MAIN" : startService(new Intent(getBaseContext(), CurrentWeatherService.class));MainActivity.mProgressDialog.cancel();break;
            case "LESS_WIDGET" : startService(new Intent(getBaseContext(), LessWidgetService.class));break;
            case "MORE_WIDGET" : startService(new Intent(getBaseContext(), MoreWidgetService.class));break;
    }
    }

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
    
    private void updateNetworkLocation() {
        Intent sendIntent = new Intent("android.intent.action.START_LOCATION_UPDATE");
        sendIntent.setPackage("org.microg.nlp");
        sendIntent.putExtra("destinationPackageName", "org.asdtm.goodweather");
        startService(sendIntent);
            }
    
    private void requestSingleUpdate(Looper locationLooper) {
        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, locationLooper);
        
        }
    
    private void removeUpdates(LocationListener locationListener) {
        if("location_geocoder_system".equals(AppPreference.getLocationGeocoderSource(this))) {
            locationManager.removeUpdates(locationListener);
    }
}
    
    private void lastNetworkAndGpsLocations(LocationListener locationListener) {
        Location lastNetworkLocation = null;
        Location lastGpsLocation = null;
        
        lastNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        lastGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        
        if((lastGpsLocation == null) && (lastNetworkLocation != null)) {
            locationListener.onLocationChanged(lastNetworkLocation);
        } else if ((lastGpsLocation != null) && (lastNetworkLocation == null)) {
            locationListener.onLocationChanged(lastGpsLocation);
        } else if ((lastGpsLocation != null) && (lastNetworkLocation != null)) {
            locationListener.onLocationChanged((lastGpsLocation.getElapsedRealtimeNanos() > lastNetworkLocation.getElapsedRealtimeNanos())?lastGpsLocation:lastNetworkLocation);
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
                    startService(new Intent(getBaseContext(), LessWidgetService.class));
                    startService(new Intent(getBaseContext(), MoreWidgetService.class));
                }
            }, LOCATION_TIMEOUT_IN_MS);
        }
    }
}
