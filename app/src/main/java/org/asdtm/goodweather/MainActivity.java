package org.asdtm.goodweather;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.asdtm.goodweather.utils.PrefKeys;
import org.asdtm.goodweather.model.Weather;
import org.json.JSONException;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "WeatherPageFragment";

    private ImageView mIconWeather;
    private TextView mTemperatureView;
    private TextView mDescription;
    private TextView mMinMaxTemperature;
    private TextView mHumidity;
    private TextView mWindSpeed;
    private TextView mPressure;
    private TextView mClouds;
    private String mTitle;
    private SwipeRefreshLayout mNewRequest;
    private Toolbar mToolbar;
    private ConnectionDetector connectionDetector;
    private Boolean isInternetConnection;
    private ProgressDialog mProgressDialog;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private LocationManager locationManager;
    private String mUnits;

    private SharedPreferences mPrefWeather;
    private SharedPreferences mSharedPreferences;
    private PrefKeys mDefaultSharedPref;

    final String WEATHER_DATA = "weather";
    final String WEATHER_DATA_TEMPERATURE = "temperature";
    final String WEATHER_DATA_DESCRIPTION = "description";
    final String WEATHER_DATA_PRESSURE = "pressure";
    final String WEATHER_DATA_HUMIDITY = "humidity";
    final String WEATHER_DATA_WIND_SPEED = "wind_speed";
    final String WEATHER_DATA_CLOUDS = "clouds";
    final String WEATHER_DATA_ICON = "icon";

    final String APP_SETTINGS = "config";
    final String APP_SETTINGS_CITY = "city";
    final String APP_SETTINGS_COUNTRY_CODE = "country_code";
    final String APP_SETTINGS_UNITS = "units";
    final String APP_SETTINGS_LATITUDE = "latitude";
    final String APP_SETTINGS_LONGITUDE = "longitude";
    final String APP_SETTINGS_LOCALE = "locale";

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private BackgroundLoadWeather mLoadWeather;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mSharedPreferences
                = getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        mDefaultSharedPref = new PrefKeys(MainActivity.this, APP_SETTINGS);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        final String title = mSharedPreferences.getString(APP_SETTINGS_CITY, "London");
        setTitle(title);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this,
                                                  mDrawerLayout,
                                                  mToolbar,
                                                  R.string.navigation_drawer_open,
                                                  R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView
                .OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int itemId = item.getItemId();
                switch (itemId) {
                    case R.id.nav_settings:
                        Intent goToSettings = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(goToSettings);
                        break;
                    case R.id.nav_about:
                        Intent goToAbout = new Intent(MainActivity.this, AboutActivity.class);
                        startActivity(goToAbout);
                        break;
                    case R.id.nav_feedback:
                        Intent sendMessage = new Intent(Intent.ACTION_SEND);
                        sendMessage.setType("message/rfc822");
                        sendMessage.putExtra(Intent.EXTRA_EMAIL,
                                             new String[]{getResources().getString(
                                                     R.string.feedback_email)});
                        try {
                            startActivity(Intent.createChooser(sendMessage, "Send feedback"));
                        } catch (android.content.ActivityNotFoundException e) {
                            Toast.makeText(MainActivity.this,
                                           "Communication app not found",
                                           Toast.LENGTH_SHORT).show();
                        }
                        break;
                }

                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        mTemperatureView = (TextView) findViewById(R.id.temperature);
        mDescription = (TextView) findViewById(R.id.weather_description);
        mPressure = (TextView) findViewById(R.id.pressure);
        mHumidity = (TextView) findViewById(R.id.humidity);
        mWindSpeed = (TextView) findViewById(R.id.wind_speed);
        mClouds = (TextView) findViewById(R.id.clouds);

        isInternetConnection = false;
        connectionDetector = new ConnectionDetector(MainActivity.this);

        mNewRequest = (SwipeRefreshLayout) findViewById(R.id.new_request);
        int top_to_padding = 150;
        mNewRequest.setProgressViewOffset(false, 0, top_to_padding);
        mNewRequest.setColorSchemeResources(R.color.swipe_red,
                                            R.color.swipe_green,
                                            R.color.swipe_blue);

        mNewRequest.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isInternetConnection = connectionDetector.isNetworkAvailableAndConnected();

                String latitude = mSharedPreferences.getString(APP_SETTINGS_LATITUDE, "51.51");
                String longitude = mSharedPreferences.getString(APP_SETTINGS_LONGITUDE, "-0.13");
                String currentLocale = mSharedPreferences.getString(APP_SETTINGS_LOCALE, "en");

                mUnits = mDefaultSharedPref.getUnit();

                if (isInternetConnection) {
                    mLoadWeather = new BackgroundLoadWeather();
                    mLoadWeather.execute(latitude, longitude, mUnits, currentLocale);
                } else {
                    Toast.makeText(MainActivity.this,
                                   R.string.connection_not_found,
                                   Toast.LENGTH_SHORT).show();
                    mNewRequest.setRefreshing(false);
                }
            }
        });
    }

    private class BackgroundLoadWeather extends AsyncTask<String, Void, Weather> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //mNewRequest.setRefreshing(true);
        }

        @Override
        protected Weather doInBackground(String... params) {
            Weather weather = new Weather();

            try {
                String data = new WeatherRequest().getItems(params[0], params[1], params[2],
                                                            params[3]);
                weather = WeatherJSONParser.getWeather(data);
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error get weather", e);
            }
            return weather;
        }

        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);
            mNewRequest.setRefreshing(false);

            mSharedPreferences =
                    getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
            SharedPreferences.Editor configEditor = mSharedPreferences.edit();

            mPrefWeather =
                    getSharedPreferences(WEATHER_DATA, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mPrefWeather.edit();

            float getTemp = weather.temperature.getTemp();
            NumberFormat oneDigit = NumberFormat.getNumberInstance();
            oneDigit.setMinimumFractionDigits(1);
            oneDigit.setMaximumFractionDigits(1);
            String setTemp = oneDigit.format(getTemp);

            setIconWeather(weather.currentWeather.getIdIcon());
            editor.putString(WEATHER_DATA_ICON, weather.currentWeather.getIdIcon());

            mTemperatureView
                    .setText(setTemp + "\u00B0");
            editor.putString(WEATHER_DATA_TEMPERATURE, setTemp);

            mDescription
                    .setText(weather.currentWeather.getDescription());
            editor.putString(WEATHER_DATA_DESCRIPTION, weather.currentWeather.getDescription());

            mHumidity
                    .setText(weather.currentCondition.getHumidity() + "%");
            editor.putInt(WEATHER_DATA_HUMIDITY, weather.currentCondition.getHumidity());

            mPressure
                    .setText(weather.currentCondition.getPressure() + " hpa");
            editor.putFloat(WEATHER_DATA_PRESSURE, weather.currentCondition.getPressure());

            String weather_unit =
                    mSharedPreferences.getString(APP_SETTINGS_UNITS, "metric");

            if (weather_unit.equals("metric")) {
                mWindSpeed
                        .setText(weather.wind.getSpeed() + getResources().getString(
                                R.string.wind_speed_meters));
            } else if (weather_unit.equals("imperial")) {
                mWindSpeed.setText(weather.wind.getSpeed() + getResources().getString(
                        R.string.wind_speed_miles));
            }
            editor.putFloat(WEATHER_DATA_WIND_SPEED, weather.wind.getSpeed());

            mClouds
                    .setText(weather.cloud.getClouds() + "%");
            editor.putInt(WEATHER_DATA_CLOUDS, weather.cloud.getClouds());

            setTitle(weather.location.getCityName());
            configEditor.putString(APP_SETTINGS_CITY, weather.location.getCityName());

            configEditor.putString(APP_SETTINGS_COUNTRY_CODE, weather.location.getCountryCode());

            editor.apply();
            configEditor.apply();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        isInternetConnection = false;
        connectionDetector = new ConnectionDetector(MainActivity.this);
        isInternetConnection = connectionDetector.isNetworkAvailableAndConnected();

        mSharedPreferences
                = getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        mPrefWeather =
                getSharedPreferences(WEATHER_DATA, Context.MODE_PRIVATE);

        mTemperatureView = (TextView) findViewById(R.id.temperature);
        mDescription = (TextView) findViewById(R.id.weather_description);
        mPressure = (TextView) findViewById(R.id.pressure);
        mHumidity = (TextView) findViewById(R.id.humidity);
        mWindSpeed = (TextView) findViewById(R.id.wind_speed);
        mClouds = (TextView) findViewById(R.id.clouds);
        mIconWeather = (ImageView) findViewById(R.id.weather_icon);

        String iconId = mPrefWeather.getString(WEATHER_DATA_ICON, "01n");
        setIconWeather(iconId);

        String temperature = mPrefWeather.getString(WEATHER_DATA_TEMPERATURE, "0");
        mTemperatureView.setText(temperature + "\u00B0");

        String description = mPrefWeather.getString(WEATHER_DATA_DESCRIPTION, null);
        mDescription.setText(description);

        int humidity = mPrefWeather.getInt(WEATHER_DATA_HUMIDITY, 0);
        mHumidity.setText(humidity + "%");

        float pressure = mPrefWeather.getFloat(WEATHER_DATA_PRESSURE, 0);
        mPressure.setText(pressure + " hpa");

        mUnits = mDefaultSharedPref.getUnit();

        float wind_speed = mPrefWeather.getFloat(WEATHER_DATA_WIND_SPEED, 0);
        if (mUnits.equals("metric")) {
            mWindSpeed
                    .setText(wind_speed + getResources().getString(R.string.wind_speed_meters));
        } else if (mUnits.equals("imperial")) {
            mWindSpeed.setText(wind_speed + getResources().getString(R.string.wind_speed_miles));
        }

        int clouds = mPrefWeather.getInt(WEATHER_DATA_CLOUDS, 0);
        mClouds.setText(clouds + "%");

        mTitle = mSharedPreferences.getString(APP_SETTINGS_CITY, "London");
        setTitle(mTitle);

        String currentLocale = Locale.getDefault().getLanguage();
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        editor.putString(APP_SETTINGS_LOCALE, currentLocale);
        editor.apply();

        String latitude = mSharedPreferences.getString(APP_SETTINGS_LATITUDE, "51.51");
        String longitude = mSharedPreferences.getString(APP_SETTINGS_LONGITUDE, "-0.13");

        if (isInternetConnection) {
            mLoadWeather = new BackgroundLoadWeather();
            mLoadWeather.execute(latitude, longitude, mUnits, currentLocale);
        } else {
            Toast.makeText(MainActivity.this,
                           R.string.connection_not_found,
                           Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void setIconWeather(String iconId) {
        mIconWeather = (ImageView) findViewById(R.id.weather_icon);
        HashMap<String, String> iconMap = new HashMap<String, String>();
        iconMap.put("01d", "ic_clear_sky_01d");
        iconMap.put("01n", "ic_clear_sky_01n");
        iconMap.put("02d", "ic_few_clouds_02d");
        iconMap.put("02n", "ic_few_clouds_02n");
        iconMap.put("03d", "ic_scattered_clouds_03");
        iconMap.put("03n", "ic_scattered_clouds_03");
        iconMap.put("04d", "ic_broken_clouds_04");
        iconMap.put("04n", "ic_broken_clouds_04");
        iconMap.put("09d", "ic_shower_rain09");
        iconMap.put("09n", "ic_shower_rain09");
        iconMap.put("10d", "ic_rain_10d");
        iconMap.put("10n", "ic_rain_10n");
        iconMap.put("11d", "ic_thunderstorm_11");
        iconMap.put("11n", "ic_thunderstorm_11");
        iconMap.put("13d", "ic_snow_13d");
        iconMap.put("13n", "ic_snow_13n");
        iconMap.put("50d", "ic_mist_50");
        iconMap.put("50n", "ic_mist_50");

        String icon = null;
        for (String iconKey : iconMap.keySet()) {
            if (iconKey.equals(iconId)) {
                icon = iconMap.get(iconKey);
            }
        }

        if (iconId != null) {
            mIconWeather.setImageResource(
                    getResources().getIdentifier(icon, "drawable", getPackageName()));
        } else {
            Drawable defaultIconId = ResourcesCompat.getDrawable(getResources(),
                                                                 R.drawable.ic_clear_sky_01d, null);
            mIconWeather.setImageDrawable(defaultIconId);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        isGPSEnabled = locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER)
                && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.getAllProviders().contains(
                LocationManager.NETWORK_PROVIDER)
                && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setMessage(getString(R.string.progressDialog_gps_locate));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);

        switch (item.getItemId()) {
            case R.id.main_menu_find_location:
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
                        .checkSelfPermission(
                                MainActivity.this,
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

            isInternetConnection = false;
            connectionDetector = new ConnectionDetector(MainActivity.this);
            isInternetConnection = connectionDetector.isNetworkAvailableAndConnected();

            mSharedPreferences = getSharedPreferences(APP_SETTINGS,
                                                      Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(APP_SETTINGS_LATITUDE, latitude);
            editor.putString(APP_SETTINGS_LONGITUDE, longitude);
            editor.apply();

            String currentLocal = mSharedPreferences.getString(APP_SETTINGS_LOCALE, "en");
            if (isInternetConnection) {
                mLoadWeather = new BackgroundLoadWeather();
                mLoadWeather.execute(latitude, longitude, mUnits, currentLocal);
            } else {
                Toast.makeText(MainActivity.this,
                               R.string.connection_not_found,
                               Toast.LENGTH_SHORT).show();
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
                    .checkSelfPermission(
                            MainActivity.this,
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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                               0,
                                               0,
                                               mLocationListener);
    }

    public void networkRequestLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this,
                                                   Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                    .checkSelfPermission(
                            MainActivity.this,
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
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                               0,
                                               0,
                                               mLocationListener);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert mDrawerLayout != null;
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }
}
