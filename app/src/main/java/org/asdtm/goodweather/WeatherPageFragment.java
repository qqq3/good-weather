package org.asdtm.goodweather;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.w3c.dom.Text;

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

    private BackgroundLoadWeather mLoadWeather;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Save fragment
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_main, parent, false);

        mToolbar = (Toolbar) v.findViewById(R.id.toolbar);
        ((ActionBarActivity)getActivity()).setSupportActionBar(mToolbar);

        mTemperatureView = (TextView) v.findViewById(R.id.temperature);
        mDescription = (TextView) v.findViewById(R.id.weather_description);
        mPressure = (TextView) v.findViewById(R.id.pressure);
        mHumidity = (TextView) v.findViewById(R.id.humidity);
        mWindSpeed = (TextView) v.findViewById(R.id.wind_speed);
        mClouds = (TextView) v.findViewById(R.id.clouds);

        mNewRequest = (SwipeRefreshLayout) v.findViewById(R.id.new_request);
        mNewRequest.setColorSchemeResources(R.color.swipe_red,
                R.color.swipe_green,
                R.color.swipe_blue);

        mNewRequest.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                mLoadWeather = new BackgroundLoadWeather();
                mLoadWeather.execute();
            }
        });

        return v;
    }

    class BackgroundLoadWeather extends AsyncTask<Void, Void, Weather>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            mNewRequest.setRefreshing(true);
        }

        @Override
        protected Weather doInBackground(Void... params)
        {
            Weather weather = new Weather();

            try {
                String data = new WeatherRequest().getItems();
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

            float getTemp = weather.temperature.getTemp();
            NumberFormat oneDigit = NumberFormat.getNumberInstance();
            oneDigit.setMinimumFractionDigits(1);
            oneDigit.setMaximumFractionDigits(1);
            String setTemp = oneDigit.format(getTemp);

            mTemperatureView
                    .setText(setTemp + "\u00B0");

            mDescription
                    .setText(weather.currentWeather.getDescription());
            mHumidity
                    .setText(weather.currentCondition.getHumidity() + "%");
            mPressure
                    .setText(weather.currentCondition.getPressure() + "hpa");
            mWindSpeed
                    .setText(weather.wind.getSpeed() + "m/s");
            mClouds
                    .setText(weather.cloud.getClouds() + "%");
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        mLoadWeather = new BackgroundLoadWeather();
        mLoadWeather.execute();
    }
}
