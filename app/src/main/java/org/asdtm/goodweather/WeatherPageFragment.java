package org.asdtm.goodweather;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.asdtm.goodweather.model.Weather;
import org.json.JSONException;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;

public class WeatherPageFragment extends Fragment
{
    private static final String TAG = "WeatherPageFragment";

    private ImageView mIconWeather;
    private TextView mTemperatureView;
    private TextView mDescription;
    private TextView mMinMaxTemperature;
    private TextView mHumidity;
    private TextView mWindSpeed;
    private TextView mPressure;
    private TextView mClouds;
    private SwipeRefreshLayout mNewRequest;
    private Toolbar mToolbar;
    private ConnectionDetector connectionDetector;
    private Boolean isInternetConnection;

    private SharedPreferences mPrefWeather;
    private SharedPreferences mSharedPreferences;
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
    final String APP_SETTINGS_UNITS = "units";

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private BackgroundLoadWeather mLoadWeather;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        MainActivity.stateFragment = "WeatherPageFragment";

        Log.i(TAG, "onCreate!!!");
        // Save fragment
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fargment_main, parent, false);

        mSharedPreferences
                = getActivity().getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);

        mToolbar = (Toolbar) v.findViewById(R.id.toolbar);
        String title = mSharedPreferences.getString(APP_SETTINGS_CITY, "London");

        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.setTitle(title);
        appCompatActivity.setSupportActionBar(mToolbar);

        mDrawerLayout = (DrawerLayout) v.findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(),
                mDrawerLayout,
                mToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        NavigationView navigationView = (NavigationView) v.findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView
                .OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(MenuItem item)
            {
                int itemId = item.getItemId();
                switch (itemId) {
                    case R.id.nav_settings:
                        Intent goToSettings = new Intent(getActivity(), SettingsActivity.class);
                        startActivity(goToSettings);
                        break;
                    case R.id.nav_about:
                        Intent goToAbout = new Intent(getActivity(), AboutActivity.class);
                        startActivity(goToAbout);
                        break;
                }

                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        mTemperatureView = (TextView) v.findViewById(R.id.temperature);
        mDescription = (TextView) v.findViewById(R.id.weather_description);
        mPressure = (TextView) v.findViewById(R.id.pressure);
        mHumidity = (TextView) v.findViewById(R.id.humidity);
        mWindSpeed = (TextView) v.findViewById(R.id.wind_speed);
        mClouds = (TextView) v.findViewById(R.id.clouds);

        isInternetConnection = false;
        connectionDetector = new ConnectionDetector(getContext());

        mNewRequest = (SwipeRefreshLayout) v.findViewById(R.id.new_request);
        int top_to_padding = 150;
        mNewRequest.setProgressViewOffset(false, 0, top_to_padding);
        mNewRequest.setColorSchemeResources(R.color.swipe_red,
                R.color.swipe_green,
                R.color.swipe_blue);


        mNewRequest.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                isInternetConnection = connectionDetector.connectToInternet();

                String city = mSharedPreferences.getString(APP_SETTINGS_CITY, "London");
                String units = mSharedPreferences.getString(APP_SETTINGS_UNITS, "metric");

                if (isInternetConnection) {
                    mLoadWeather = new BackgroundLoadWeather();
                    mLoadWeather.execute(city, units);
                } else {
                    Toast.makeText(getActivity(),
                            R.string.connection_not_found,
                            Toast.LENGTH_SHORT).show();
                    mNewRequest.setRefreshing(false);
                }
            }
        });

        Log.i(TAG, "onCreateView!!!");
        return v;
    }

    class BackgroundLoadWeather extends AsyncTask<String, Void, Weather>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            //mNewRequest.setRefreshing(true);
        }

        @Override
        protected Weather doInBackground(String... params)
        {
            Weather weather = new Weather();

            try {
                String data = new WeatherRequest().getItems(params[0], params[1]);
                weather = WeatherJSONParser.getWeather(data);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return weather;
        }

        @Override
        protected void onPostExecute(Weather weather)
        {
            super.onPostExecute(weather);
            mNewRequest.setRefreshing(false);

            mSharedPreferences =
                    getActivity().getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
            mPrefWeather =
                    getActivity().getSharedPreferences(WEATHER_DATA, Context.MODE_PRIVATE);
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
                        .setText(weather.wind.getSpeed() + getResources().getString(R.string.wind_speed_meters));
            } else if (weather_unit.equals("imperial")){
                mWindSpeed.setText(weather.wind.getSpeed() + getResources().getString(R.string.wind_speed_miles));
            }
            editor.putFloat(WEATHER_DATA_WIND_SPEED, weather.wind.getSpeed());

            mClouds
                    .setText(weather.cloud.getClouds() + "%");
            editor.putInt(WEATHER_DATA_CLOUDS, weather.cloud.getClouds());

            editor.apply();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        isInternetConnection = false;
        connectionDetector = new ConnectionDetector(getContext());
        isInternetConnection = connectionDetector.connectToInternet();

        mSharedPreferences
                = getActivity().getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        mPrefWeather =
                getActivity().getSharedPreferences(WEATHER_DATA, Context.MODE_PRIVATE);

        mTemperatureView = (TextView) getActivity().findViewById(R.id.temperature);
        mDescription = (TextView) getActivity().findViewById(R.id.weather_description);
        mPressure = (TextView) getActivity().findViewById(R.id.pressure);
        mHumidity = (TextView) getActivity().findViewById(R.id.humidity);
        mWindSpeed = (TextView) getActivity().findViewById(R.id.wind_speed);
        mClouds = (TextView) getActivity().findViewById(R.id.clouds);
        mIconWeather = (ImageView) getActivity().findViewById(R.id.weather_icon);

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

        String weather_unit =
                mSharedPreferences.getString(APP_SETTINGS_UNITS, "metric");

        float wind_speed = mPrefWeather.getFloat(WEATHER_DATA_WIND_SPEED, 0);
        if (weather_unit.equals("metric")) {
            mWindSpeed
                    .setText(wind_speed + getResources().getString(R.string.wind_speed_meters));
        } else if (weather_unit.equals("imperial")){
            mWindSpeed.setText(wind_speed + getResources().getString(R.string.wind_speed_miles));
        }

        int clouds = mPrefWeather.getInt(WEATHER_DATA_CLOUDS, 0);
        mClouds.setText(clouds + "%");

        String city = mSharedPreferences.getString(APP_SETTINGS_CITY, "London");
        String units = mSharedPreferences.getString(APP_SETTINGS_UNITS, "metric");

        if (isInternetConnection) {
            mLoadWeather = new BackgroundLoadWeather();
            mLoadWeather.execute(city, units);
        } else {
            Toast.makeText(getActivity(),
                    R.string.connection_not_found,
                    Toast.LENGTH_SHORT).show();
        }
        Log.i(TAG, "onResume!!!");
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Log.i(TAG, "onStart!!!");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public String setIconWeather(String iconId)
    {
        mIconWeather = (ImageView) getActivity().findViewById(R.id.weather_icon);
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
        mIconWeather.setImageResource(getResources().getIdentifier(icon, "drawable", getActivity().getPackageName()));

        return "";
    }
}
