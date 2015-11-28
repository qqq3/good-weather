package org.asdtm.goodweather;

import android.app.Fragment;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.w3c.dom.Text;

import java.io.IOException;

public class WeatherPageFragment extends Fragment
{
    private static final String TAG = "WeatherPageFragment";

    private TextView mTemperatureView;
    private TextView mDescription;
    private BackgroundLoadWeather mLoadWeather;
    private SwipeRefreshLayout mNewRequest;

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

        mTemperatureView = (TextView) v.findViewById(R.id.temperature);
        mDescription = (TextView) v.findViewById(R.id.weather_description);


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

            mTemperatureView
                    .setText(Math.round(weather.temperature.getTemp() * 100.0) / 100.0 + "\u00B0");
            mDescription
                    .setText(weather.currentWeather.getDescription());
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
