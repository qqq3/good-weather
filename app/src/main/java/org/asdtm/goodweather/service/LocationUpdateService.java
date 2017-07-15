package org.asdtm.goodweather.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import java.util.Locale;
import org.asdtm.goodweather.MainActivity;
import org.asdtm.goodweather.utils.AppPreference;
import org.asdtm.goodweather.utils.Constants;
import org.asdtm.goodweather.utils.Utils;
import org.asdtm.goodweather.widget.LessWidgetService;
import org.asdtm.goodweather.widget.MoreWidgetService;

public class LocationUpdateService extends Service implements LocationListener {
    
    private static final String TAG = "LocationUpdateService";
    
    private static final long LOCATION_TIMEOUT_IN_MS = 30000l;

    private LocationManager locationManager;
    
    private String updateSource;
    
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
    public int onStartCommand(Intent intent, int flags, int startId) {
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
            Looper locationLooper = Looper.myLooper();
                requestSingleUpdate(locationLooper);
            final LocationListener locationListener = this;
            final Handler locationHandler = new Handler(locationLooper);
            locationHandler.postDelayed(new Runnable() {
                 @Override
                 public void run() {
                         removeUpdates(locationListener);
                         lastNetworkAndGpsLocations(locationListener);
                     }
                }, LOCATION_TIMEOUT_IN_MS);
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
        
        String latitude = String.format("%1$.2f", location.getLatitude());
        String longitude = String.format("%1$.2f", location.getLongitude());
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
}
