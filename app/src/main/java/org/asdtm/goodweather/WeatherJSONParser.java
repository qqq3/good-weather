package org.asdtm.goodweather;

import org.asdtm.goodweather.model.CitySearch;
import org.asdtm.goodweather.model.Weather;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherJSONParser {

    private static final String TAG = "WeatherJSONParser";

    public static Weather getWeather(String data) throws JSONException {
        Weather weather = new Weather();
        CitySearch location = new CitySearch();

        JSONObject weatherData = new JSONObject(data);

        JSONArray weatherArray = weatherData.getJSONArray("weather");

        JSONObject weatherObj = weatherArray.getJSONObject(0);
        if (weatherObj.has("description")) {
            weather.currentWeather.setDescription(weatherObj.getString("description"));
        }
        if (weatherObj.has("icon")) {
            weather.currentWeather.setIdIcon(weatherObj.getString("icon"));
        }

        JSONObject mainObj = weatherData.getJSONObject("main");
        if (mainObj.has("temp")) {
            weather.temperature.setTemp(Float.parseFloat(mainObj.getString("temp")));
        }
        if (mainObj.has("pressure")) {
            weather.currentCondition.setPressure(Float.parseFloat(mainObj.getString("pressure")));
        }
        if (mainObj.has("humidity")) {
            weather.currentCondition.setHumidity(mainObj.getInt("humidity"));
        }

        JSONObject windObj = weatherData.getJSONObject("wind");
        if (windObj.has("speed")) {
            weather.wind.setSpeed(Float.parseFloat(windObj.getString("speed")));
        }
        if (windObj.has("deg")) {
            weather.wind.setDirection(Float.parseFloat(windObj.getString("deg")));
        }

        JSONObject cloudsObj = weatherData.getJSONObject("clouds");
        if (cloudsObj.has("all")) {
            weather.cloud.setClouds(cloudsObj.getInt("all"));
        }

        if (weatherData.has("name")) {
            location.setCityName(weatherData.getString("name"));
        }

        JSONObject sysObj = weatherData.getJSONObject("sys");
        if (sysObj.has("country")) {
            location.setCountryCode(sysObj.getString("country"));
        }
        weather.sys.setSunrise(sysObj.getLong("sunrise"));
        weather.sys.setSunset(sysObj.getLong("sunset"));

        weather.location = location;

        return weather;
    }
}
