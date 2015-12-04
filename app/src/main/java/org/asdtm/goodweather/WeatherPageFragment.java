package org.asdtm.goodweather;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import android.widget.TextView;

import org.asdtm.goodweather.model.Weather;
import org.json.JSONException;

import java.io.IOException;
import java.text.NumberFormat;

public class WeatherPageFragment extends Fragment
{
    private static final String TAG = "WeatherPageFragment";

    private TextView mTemperatureView;
    private TextView mDescription;
    private TextView mMinMaxTemperature;
    private TextView mHumidity;
    private TextView mWindSpeed;
    private TextView mPressure;
    private TextView mClouds;
    private SwipeRefreshLayout mNewRequest;
    private Toolbar mToolbar;


    private SharedPreferences mPrefWeather;
    final String WEATHER_DATA = "weather";
    final String WEATHER_DATA_TEMPERATURE = "temperature";
    final String WEATHER_DATA_DESCRIPTION = "description";
    final String WEATHER_DATA_PRESSURE = "pressure";
    final String WEATHER_DATA_HUMIDITY = "humidity";
    final String WEATHER_DATA_WIND_SPEED = "wind_speed";
    final String WEATHER_DATA_CLOUDS = "clouds";

    final String APP_SETTINGS = "config";
    final String APP_SETTINGS_CITY = "City";

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

        final SharedPreferences mSharedPreferences
                = getActivity().getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);


        mToolbar = (Toolbar) v.findViewById(R.id.toolbar);
        String title = mSharedPreferences.getString(APP_SETTINGS_CITY, "Good Weather");

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
                String city = mSharedPreferences.getString(APP_SETTINGS_CITY, "Sydney");
                mLoadWeather = new BackgroundLoadWeather();
                mLoadWeather.execute(city);
            }
        });

        Log.i(TAG, "onCreateView!!!");
        return v;
    }

    class BackgroundLoadWeather extends AsyncTask<String , Void, Weather>
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
                String data = new WeatherRequest().getItems(params[0]);
                weather = WeatherJSONParser.getWeather(data);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return weather;
        }

        @Override
        protected void onPostExecute(Weather weather)
        {
            super.onPostExecute(weather);
            mNewRequest.setRefreshing(false);

            mPrefWeather = getActivity().getSharedPreferences(WEATHER_DATA, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mPrefWeather.edit();

            float getTemp = weather.temperature.getTemp();
            NumberFormat oneDigit = NumberFormat.getNumberInstance();
            oneDigit.setMinimumFractionDigits(1);
            oneDigit.setMaximumFractionDigits(1);
            String setTemp = oneDigit.format(getTemp);

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
                    .setText(weather.currentCondition.getPressure() + "hpa");
            editor.putFloat(WEATHER_DATA_PRESSURE, weather.currentCondition.getPressure());

            mWindSpeed
                    .setText(weather.wind.getSpeed() + "m/s");
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
        mPrefWeather = getActivity().getSharedPreferences(WEATHER_DATA, Context.MODE_PRIVATE);

        mTemperatureView = (TextView) getActivity().findViewById(R.id.temperature);
        mDescription = (TextView) getActivity().findViewById(R.id.weather_description);
        mPressure = (TextView) getActivity().findViewById(R.id.pressure);
        mHumidity = (TextView) getActivity().findViewById(R.id.humidity);
        mWindSpeed = (TextView) getActivity().findViewById(R.id.wind_speed);
        mClouds = (TextView) getActivity().findViewById(R.id.clouds);

        String temperature = mPrefWeather.getString(WEATHER_DATA_TEMPERATURE, null);
        mTemperatureView.setText(temperature + "\u00B0");

        String description = mPrefWeather.getString(WEATHER_DATA_DESCRIPTION, null);
        mDescription.setText(description);

        int humidity = mPrefWeather.getInt(WEATHER_DATA_HUMIDITY, 0);
        mHumidity.setText(humidity + "%");

        float pressure = mPrefWeather.getFloat(WEATHER_DATA_PRESSURE, 0);
        mPressure.setText(pressure + "hpa");

        float wind_speed = mPrefWeather.getFloat(WEATHER_DATA_WIND_SPEED, 0);
        mWindSpeed.setText(wind_speed + "m/s");

        int clouds = mPrefWeather.getInt(WEATHER_DATA_CLOUDS, 0);
        mClouds.setText(clouds + "%");

        SharedPreferences mSharedPreferences
                = getActivity().getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        String city = mSharedPreferences.getString(APP_SETTINGS_CITY, "Sydney");
        mLoadWeather = new BackgroundLoadWeather();
        mLoadWeather.execute(city);

        TextView currentCity = (TextView) getActivity().findViewById(R.id.currentCity);
        currentCity.setText(city);
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
}
