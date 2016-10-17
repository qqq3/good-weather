package org.asdtm.goodweather;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.asdtm.goodweather.adapter.WeatherForecastAdapter;
import org.asdtm.goodweather.model.WeatherForecast;
import org.asdtm.goodweather.utils.AppPreference;
import org.asdtm.goodweather.utils.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.asdtm.goodweather.utils.Utils.getWeatherForecastUrl;

public class WeatherForecastActivity extends BaseActivity {

    private final String TAG = "WeatherForecastActivity";

    private List<WeatherForecast> mWeatherForecastList;
    private ConnectionDetector mConnectionDetector;
    private RecyclerView mRecyclerView;
    private static Handler mHandler;
    private ProgressDialog mGetWeatherProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_forecast);

        mConnectionDetector = new ConnectionDetector(this);
        mWeatherForecastList = new ArrayList<>();
        mGetWeatherProgress = getProgressDialog();

        mRecyclerView = (RecyclerView) findViewById(R.id.forecast_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        updateUI();

        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case Constants.TASK_RESULT_ERROR:
                        Toast.makeText(WeatherForecastActivity.this,
                                       R.string.toast_parse_error,
                                       Toast.LENGTH_SHORT).show();
                        setVisibleUpdating(false);
                        break;
                    case Constants.PARSE_RESULT_ERROR:
                        Toast.makeText(WeatherForecastActivity.this,
                                       R.string.toast_parse_error,
                                       Toast.LENGTH_SHORT).show();
                        setVisibleUpdating(false);
                        break;
                    case Constants.PARSE_RESULT_SUCCESS:
                        setVisibleUpdating(false);
                        updateUI();
                        if (!mWeatherForecastList.isEmpty()) {
                            AppPreference.saveWeatherForecast(WeatherForecastActivity.this,
                                                              mWeatherForecastList);
                        }
                        break;
                }
            }
        };
    }

    private void updateUI() {
        ImageView android = (ImageView) findViewById(R.id.android);
        if (mWeatherForecastList.size() < 5) {
            mRecyclerView.setVisibility(View.INVISIBLE);
            android.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            android.setVisibility(View.GONE);
        }
        WeatherForecastAdapter adapter = new WeatherForecastAdapter(this,
                                                                    mWeatherForecastList,
                                                                    getSupportFragmentManager());
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mWeatherForecastList.isEmpty()) {
            mWeatherForecastList = AppPreference.loadWeatherForecast(this);
        }
        updateUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.weather_forecast_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_forecast_refresh:
                if (mConnectionDetector.isNetworkAvailableAndConnected()) {
                    getWeather();
                    setVisibleUpdating(true);
                } else {
                    Toast.makeText(WeatherForecastActivity.this,
                                   R.string.connection_not_found,
                                   Toast.LENGTH_SHORT).show();
                }
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setVisibleUpdating(boolean visible) {
        if (visible) {
            mGetWeatherProgress.show();
        } else {
            mGetWeatherProgress.cancel();
        }
    }

    private void getWeather() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences pref = getSharedPreferences(Constants.APP_SETTINGS_NAME, 0);
                String latitude = pref.getString(Constants.APP_SETTINGS_LATITUDE, "51.51");
                String longitude = pref.getString(Constants.APP_SETTINGS_LONGITUDE, "-0.13");
                String locale = AppPreference.getLocale(WeatherForecastActivity.this,
                                                        Constants.APP_SETTINGS_NAME);
                String units = AppPreference.getTemperatureUnit(WeatherForecastActivity.this);

                String requestResult = "";
                HttpURLConnection connection = null;
                try {
                    URL url = getWeatherForecastUrl(latitude, longitude, units, locale);
                    connection = (HttpURLConnection) url.openConnection();

                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                        InputStream inputStream = connection.getInputStream();

                        int bytesRead;
                        byte[] buffer = new byte[1024];
                        while ((bytesRead = inputStream.read(buffer)) > 0) {
                            byteArray.write(buffer, 0, bytesRead);
                        }
                        byteArray.close();
                        requestResult = byteArray.toString();
                        AppPreference.saveLastUpdateTimeMillis(WeatherForecastActivity.this);
                    }
                } catch (IOException e) {
                    mHandler.sendEmptyMessage(Constants.TASK_RESULT_ERROR);
                    Log.e(TAG, "IOException: " + requestResult);
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
                parseWeatherForecast(requestResult);
            }
        });
        t.start();
    }

    private void parseWeatherForecast(String data) {
        try {
            if (!mWeatherForecastList.isEmpty()) {
                mWeatherForecastList.clear();
            }

            JSONObject jsonObject = new JSONObject(data);
            JSONArray listArray = jsonObject.getJSONArray("list");

            int listArrayCount = listArray.length();
            for (int i = 0; i < listArrayCount; i++) {
                WeatherForecast weatherForecast = new WeatherForecast();
                JSONObject resultObject = listArray.getJSONObject(i);
                weatherForecast.setDateTime(resultObject.getLong("dt"));
                weatherForecast.setPressure(resultObject.getString("pressure"));
                weatherForecast.setHumidity(resultObject.getString("humidity"));
                weatherForecast.setWindSpeed(resultObject.getString("speed"));
                weatherForecast.setWindDegree(resultObject.getString("deg"));
                weatherForecast.setCloudiness(resultObject.getString("clouds"));
                if (resultObject.has("rain")) {
                    weatherForecast.setRain(resultObject.getString("rain"));
                } else {
                    weatherForecast.setRain("0");
                }
                if (resultObject.has("snow")) {
                    weatherForecast.setSnow(resultObject.getString("snow"));
                } else {
                    weatherForecast.setSnow("0");
                }
                JSONObject temperatureObject = resultObject.getJSONObject("temp");
                weatherForecast.setTemperatureMin(
                        Float.parseFloat(temperatureObject.getString("min")));
                weatherForecast.setTemperatureMax(
                        Float.parseFloat(temperatureObject.getString("max")));
                weatherForecast.setTemperatureMorning(
                        Float.parseFloat(temperatureObject.getString("morn")));
                weatherForecast.setTemperatureDay(
                        Float.parseFloat(temperatureObject.getString("day")));
                weatherForecast.setTemperatureEvening(
                        Float.parseFloat(temperatureObject.getString("eve")));
                weatherForecast.setTemperatureNight(
                        Float.parseFloat(temperatureObject.getString("night")));
                JSONArray weatherArray = resultObject.getJSONArray("weather");
                JSONObject weatherObject = weatherArray.getJSONObject(0);
                weatherForecast.setDescription(weatherObject.getString("description"));
                weatherForecast.setIcon(weatherObject.getString("icon"));

                mWeatherForecastList.add(weatherForecast);
                mHandler.sendEmptyMessage(Constants.PARSE_RESULT_SUCCESS);
            }
        } catch (JSONException e) {
            mHandler.sendEmptyMessage(Constants.TASK_RESULT_ERROR);
            e.printStackTrace();
        }
    }
}
