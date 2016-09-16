package org.asdtm.goodweather;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.asdtm.goodweather.model.CitySearch;
import org.asdtm.goodweather.model.Weather;
import org.asdtm.goodweather.service.CurrentWeatherService;
import org.asdtm.goodweather.utils.AppPreference;
import org.asdtm.goodweather.utils.Constants;
import org.asdtm.goodweather.utils.Utils;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "WeatherPageFragment";

    private TextView mIconWeatherView;
    private TextView mTemperatureView;
    private TextView mDescriptionView;
    private TextView mHumidityView;
    private TextView mWindSpeedView;
    private TextView mPressureView;
    private TextView mCloudinessView;
    private TextView mLastUpdateView;
    private TextView mSunriseView;
    private TextView mSunsetView;

    private Toolbar mToolbar;
    private ConnectionDetector connectionDetector;
    private Boolean isNetworkAvailable;
    private ProgressDialog mProgressDialog;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private LocationManager locationManager;
    private SwipeRefreshLayout mSwipeRefresh;
    private Menu mToolbarMenu;
    private BroadcastReceiver mWeatherUpdateReceiver;

    private String mUnits;
    private String mSpeedScale;
    private String mIconWind;
    private String mIconHumidity;
    private String mIconPressure;
    private String mIconCloudiness;
    private String mIconSunrise;
    private String mIconSunset;
    private String mPercentSign;
    private String mPressureMeasurement;

    private SharedPreferences mPrefWeather;
    private SharedPreferences mSharedPreferences;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    public static Weather mWeather;
    public static CitySearch mCitySearch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWeather = new Weather();
        mCitySearch = new CitySearch();

        weatherConditionsIcons();
        initializeTextView();
        initializeWeatherReceiver();

        connectionDetector = new ConnectionDetector(MainActivity.this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        AppPreference.setLocale(this, Constants.APP_SETTINGS_NAME);

        mPrefWeather = getSharedPreferences(Constants.PREF_WEATHER_NAME, Context.MODE_PRIVATE);
        mSharedPreferences = getSharedPreferences(Constants.APP_SETTINGS_NAME,
                                                  Context.MODE_PRIVATE);

        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        final String title = mSharedPreferences.getString(Constants.APP_SETTINGS_CITY, "London");
        setTitle(title);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                                                  R.string.navigation_drawer_open,
                                                  R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(navigationViewListener);

        /**
         * Configure SwipeRefreshLayout
         */
        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.main_swipe_refresh);
        int top_to_padding = 150;
        mSwipeRefresh.setProgressViewOffset(false, 0, top_to_padding);
        mSwipeRefresh.setColorSchemeResources(R.color.swipe_red, R.color.swipe_green,
                                              R.color.swipe_blue);
        mSwipeRefresh.setOnRefreshListener(swipeRefreshListener);
    }

    private void updateCurrentWeather() {
        AppPreference.saveWeather(MainActivity.this, mWeather);
        mSharedPreferences = getSharedPreferences(Constants.APP_SETTINGS_NAME,
                                                  Context.MODE_PRIVATE);
        SharedPreferences.Editor configEditor = mSharedPreferences.edit();

        mSpeedScale = Utils.getSpeedScale(MainActivity.this);
        String temperature = String.format(Locale.getDefault(), "%.1f",
                                           mWeather.temperature.getTemp());
        String pressure = String.format(Locale.getDefault(), "%.1f",
                                        mWeather.currentCondition.getPressure());
        String wind = String.format(Locale.getDefault(), "%.1f", mWeather.wind.getSpeed());

        String lastUpdate = Utils.setLastUpdateTime(MainActivity.this, AppPreference
                .saveLastUpdateTimeMillis(MainActivity.this));
        String sunrise = Utils.unixTimeToFormatTime(MainActivity.this, mWeather.sys.getSunrise());
        String sunset = Utils.unixTimeToFormatTime(MainActivity.this, mWeather.sys.getSunset());

        mIconWeatherView.setText(
                Utils.getStrIcon(MainActivity.this, mWeather.currentWeather.getIdIcon()));
        mTemperatureView.setText(getString(R.string.temperature_with_degree, temperature));
        mDescriptionView.setText(mWeather.currentWeather.getDescription());
        mHumidityView.setText(getString(R.string.humidity_with_icon_label, mIconHumidity,
                                        mWeather.currentCondition.getHumidity(), mPercentSign));
        mPressureView.setText(getString(R.string.pressure_with_icon_label, mIconPressure, pressure,
                                        mPressureMeasurement));
        mWindSpeedView.setText(
                getString(R.string.wind_with_icon_label, mIconWind, wind, mSpeedScale));
        mCloudinessView.setText(getString(R.string.cloudiness_with_icon_label, mIconCloudiness,
                                          mWeather.cloud.getClouds(), mPercentSign));
        mLastUpdateView.setText(getString(R.string.last_update_label, lastUpdate));
        mSunriseView.setText(getString(R.string.sunrise_with_icon_label, mIconSunrise, sunrise));
        mSunsetView.setText(getString(R.string.sunset_with_icon_label, mIconSunset, sunset));
        setTitle(mWeather.location.getCityName());

        configEditor.putString(Constants.APP_SETTINGS_CITY, mWeather.location.getCityName());
        configEditor.putString(Constants.APP_SETTINGS_COUNTRY_CODE,
                               mWeather.location.getCountryCode());
        configEditor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        preLoadWeather();

        LocalBroadcastManager.getInstance(this).registerReceiver(mWeatherUpdateReceiver, new IntentFilter(CurrentWeatherService.ACTION_WEATHER_UPDATE_RESULT));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mWeatherUpdateReceiver);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.mToolbarMenu = menu;
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        isGPSEnabled = locationManager.getAllProviders().contains(
                LocationManager.GPS_PROVIDER) && locationManager.isProviderEnabled(
                LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.getAllProviders().contains(
                LocationManager.NETWORK_PROVIDER) && locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER);

        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setMessage(getString(R.string.progressDialog_gps_locate));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);

        switch (item.getItemId()) {
            case R.id.main_menu_refresh:
                if (connectionDetector.isNetworkAvailableAndConnected()) {
                    startService(new Intent(this, CurrentWeatherService.class));
                    setUpdateButtonState(true);
                } else {
                    Toast.makeText(MainActivity.this,
                                   R.string.connection_not_found,
                                   Toast.LENGTH_SHORT).show();
                    setUpdateButtonState(false);
                }
                return true;
            case R.id.main_menu_detect_location:
                if (isGPSEnabled) {
                    gpsRequestLocation();
                    mProgressDialog.show();
                } else {
                    showSettingsAlert();
                }

                if (isNetworkEnabled) {
                    networkRequestLocation();
                    mProgressDialog.show();
                }
                return true;
            case R.id.main_menu_search_city:
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mProgressDialog.cancel();
            String latitude = String.format("%1$.2f", location.getLatitude());
            String longitude = String.format("%1$.2f", location.getLongitude());

            Log.d(TAG, "Current location: " + latitude + ";" + longitude);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this,
                                                       Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                        .checkSelfPermission(MainActivity.this,
                                             Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
            }
            locationManager.removeUpdates(mLocationListener);

            connectionDetector = new ConnectionDetector(MainActivity.this);
            isNetworkAvailable = connectionDetector.isNetworkAvailableAndConnected();

            mSharedPreferences = getSharedPreferences(Constants.APP_SETTINGS_NAME,
                                                      Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(Constants.APP_SETTINGS_LATITUDE, latitude);
            editor.putString(Constants.APP_SETTINGS_LONGITUDE, longitude);
            editor.apply();

            if (isNetworkAvailable) {
                startService(new Intent(MainActivity.this, CurrentWeatherService.class));
                sendBroadcast(new Intent(Constants.ACTION_FORCED_APPWIDGET_UPDATE));
            } else {
                Toast.makeText(MainActivity.this, R.string.connection_not_found, Toast.LENGTH_SHORT)
                     .show();
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

        }
    };

    private NavigationView.OnNavigationItemSelectedListener navigationViewListener =
            new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem item) {
                    int itemId = item.getItemId();
                    switch (itemId) {
                        case R.id.nav_settings:
                            Intent goToSettings = new Intent(MainActivity.this,
                                                             SettingsActivity.class);
                            startActivity(goToSettings);
                            break;
                        case R.id.nav_feedback:
                            Intent sendMessage = new Intent(Intent.ACTION_SEND);
                            sendMessage.setType("message/rfc822");
                            sendMessage.putExtra(Intent.EXTRA_EMAIL, new String[]{
                                    getResources().getString(R.string.feedback_email)});
                            try {
                                startActivity(Intent.createChooser(sendMessage, "Send feedback"));
                            } catch (android.content.ActivityNotFoundException e) {
                                Toast.makeText(MainActivity.this, "Communication app not found",
                                               Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case R.id.nav_menu_bitcoin_donation:
                            BitcoinDonationDialog dialog = BitcoinDonationDialog.newInstance();
                            dialog.show(getFragmentManager(), "bitcoinDonationDialog");
                            break;
                    }

                    mDrawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }
            };

    private SwipeRefreshLayout.OnRefreshListener swipeRefreshListener =
            new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    isNetworkAvailable = connectionDetector.isNetworkAvailableAndConnected();
                    if (isNetworkAvailable) {
                        startService(new Intent(MainActivity.this, CurrentWeatherService.class));
                    } else {
                        Toast.makeText(MainActivity.this,
                                       R.string.connection_not_found,
                                       Toast.LENGTH_SHORT).show();
                        mSwipeRefresh.setRefreshing(false);
                    }
                }
            };

    public void showSettingsAlert() {
        AlertDialog.Builder settingsAlert = new AlertDialog.Builder(MainActivity.this);
        settingsAlert.setTitle(R.string.alertDialog_gps_title);
        settingsAlert.setMessage(R.string.alertDialog_gps_message);

        settingsAlert.setPositiveButton(R.string.alertDialog_gps_positiveButton,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent goToSettings = new Intent(
                                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                                startActivity(goToSettings);
                                            }
                                        });

        settingsAlert.setNegativeButton(R.string.alertDialog_gps_negativeButton,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });

        settingsAlert.show();
    }

    public void gpsRequestLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this,
                                                   Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                    .checkSelfPermission(MainActivity.this,
                                         Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                                               mLocationListener);
    }

    public void networkRequestLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this,
                                                   Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                    .checkSelfPermission(MainActivity.this,
                                         Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
                                               mLocationListener);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            finish();
        }
    }

    private void preLoadWeather() {
        mUnits = AppPreference.getTemperatureUnit(this);
        mSpeedScale = Utils.getSpeedScale(this);
        String lastUpdate = Utils.setLastUpdateTime(this,
                                                    AppPreference.getLastUpdateTimeMillis(this));

        String iconId = mPrefWeather.getString(Constants.WEATHER_DATA_ICON, "01d");
        float temperaturePref = mPrefWeather.getFloat(Constants.WEATHER_DATA_TEMPERATURE, 0);
        String description = mPrefWeather.getString(Constants.WEATHER_DATA_DESCRIPTION,
                                                    "clear sky");
        int humidity = mPrefWeather.getInt(Constants.WEATHER_DATA_HUMIDITY, 0);
        float pressurePref = mPrefWeather.getFloat(Constants.WEATHER_DATA_PRESSURE, 0);
        float windPref = mPrefWeather.getFloat(Constants.WEATHER_DATA_WIND_SPEED, 0);
        int clouds = mPrefWeather.getInt(Constants.WEATHER_DATA_CLOUDS, 0);
        String title = mSharedPreferences.getString(Constants.APP_SETTINGS_CITY, "London");
        long sunrisePref = mPrefWeather.getLong(Constants.WEATHER_DATA_SUNRISE, -1);
        long sunsetPref = mPrefWeather.getLong(Constants.WEATHER_DATA_SUNSET, -1);

        String temperature = String.format(Locale.getDefault(), "%.1f", temperaturePref);
        String pressure = String.format(Locale.getDefault(), "%.1f", pressurePref);
        String wind = String.format(Locale.getDefault(), "%.1f", windPref);
        String sunrise = Utils.unixTimeToFormatTime(this, sunrisePref);
        String sunset = Utils.unixTimeToFormatTime(this, sunsetPref);

        mIconWeatherView.setText(Utils.getStrIcon(this, iconId));
        mTemperatureView.setText(getString(R.string.temperature_with_degree, temperature));
        mDescriptionView.setText(description);
        mLastUpdateView.setText(getString(R.string.last_update_label, lastUpdate));
        mHumidityView.setText(getString(R.string.humidity_with_icon_label, mIconHumidity, humidity,
                                        mPercentSign));
        mPressureView.setText(getString(R.string.pressure_with_icon_label, mIconPressure, pressure,
                                        mPressureMeasurement));
        mWindSpeedView.setText(
                getString(R.string.wind_with_icon_label, mIconWind, wind, mSpeedScale));
        mCloudinessView.setText(
                getString(R.string.cloudiness_with_icon_label, mIconCloudiness, clouds,
                          mPercentSign));
        mSunriseView.setText(getString(R.string.sunrise_with_icon_label, mIconSunrise, sunrise));
        mSunsetView.setText(getString(R.string.sunset_with_icon_label, mIconSunset, sunset));
        setTitle(title);
    }

    private void initializeTextView() {
        Typeface weatherFontIcon = Typeface.createFromAsset(this.getAssets(),
                                                            "fonts/weathericons-regular-webfont.ttf");
        mIconWeatherView = (TextView) findViewById(R.id.main_weather_icon);
        mIconWeatherView.setTypeface(weatherFontIcon);
        mTemperatureView = (TextView) findViewById(R.id.main_temperature);
        mTemperatureView.setTypeface(weatherFontIcon);
        mDescriptionView = (TextView) findViewById(R.id.main_description);
        mPressureView = (TextView) findViewById(R.id.main_pressure);
        mPressureView.setTypeface(weatherFontIcon);
        mHumidityView = (TextView) findViewById(R.id.main_humidity);
        mHumidityView.setTypeface(weatherFontIcon);
        mWindSpeedView = (TextView) findViewById(R.id.main_wind_speed);
        mWindSpeedView.setTypeface(weatherFontIcon);
        mCloudinessView = (TextView) findViewById(R.id.main_cloudiness);
        mCloudinessView.setTypeface(weatherFontIcon);
        mLastUpdateView = (TextView) findViewById(R.id.main_last_update);
        mSunriseView = (TextView) findViewById(R.id.main_sunrise);
        mSunriseView.setTypeface(weatherFontIcon);
        mSunsetView = (TextView) findViewById(R.id.main_sunset);
        mSunsetView.setTypeface(weatherFontIcon);
    }

    private void weatherConditionsIcons() {
        mIconWind = getString(R.string.icon_wind);
        mIconHumidity = getString(R.string.icon_humidity);
        mIconPressure = getString(R.string.icon_barometer);
        mIconCloudiness = getString(R.string.icon_cloudiness);
        mPercentSign = getString(R.string.percent_sign);
        mPressureMeasurement = getString(R.string.pressure_measurement);
        mIconSunrise = getString(R.string.icon_sunrise);
        mIconSunset = getString(R.string.icon_sunset);
    }

    private void setUpdateButtonState(boolean isUpdate) {
        if (mToolbarMenu != null) {
            MenuItem updateItem = mToolbarMenu.findItem(R.id.main_menu_refresh);
            ProgressBar progressUpdate = (ProgressBar) findViewById(R.id.toolbar_progress_bar);
            if (isUpdate) {
                updateItem.setVisible(false);
                progressUpdate.setVisibility(View.VISIBLE);
            } else {
                progressUpdate.setVisibility(View.GONE);
                updateItem.setVisible(true);
            }
        }
    }

    private void initializeWeatherReceiver() {
        mWeatherUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getStringExtra(CurrentWeatherService.ACTION_WEATHER_UPDATE_RESULT)) {
                    case CurrentWeatherService.ACTION_WEATHER_UPDATE_OK:
                        mSwipeRefresh.setRefreshing(false);
                        setUpdateButtonState(false);
                        updateCurrentWeather();
                        break;
                    case CurrentWeatherService.ACTION_WEATHER_UPDATE_FAIL:
                        mSwipeRefresh.setRefreshing(false);
                        setUpdateButtonState(false);
                        Toast.makeText(MainActivity.this,
                                       getString(R.string.toast_parse_error),
                                       Toast.LENGTH_SHORT).show();
                }
            }
        };
    }
}
