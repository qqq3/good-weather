package org.asdtm.goodweather;

import org.asdtm.goodweather.model.CitySearch;
import org.asdtm.goodweather.model.Weather;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherJSONParser
{
    private static final String TAG = "WeatherJSONParser";

    public static Weather getWeather(String data) throws JSONException
    {
        Weather weather = new Weather();
        CitySearch location = new CitySearch();

        JSONObject jWeatherData = new JSONObject(data);

        JSONArray jWeatherArray = jWeatherData.getJSONArray("weather");

        JSONObject weatherObj = jWeatherArray.getJSONObject(0);
        //weather.currentWeather.setDescription(getString("description", weatherOdj));
        if (weatherObj.has("description")) {
            weather.currentWeather.setDescription(weatherObj.getString("description"));
        }
        if (weatherObj.has("icon")) {
            weather.currentWeather.setIdIcon(getString("icon", weatherObj));
        }

        JSONObject mainObj = jWeatherData.getJSONObject("main");
        if (mainObj.has("temp")) {
            weather.temperature.setTemp(getFloat("temp", mainObj));
        }
        //weather.temperature.setMinTemp(getFloat("temp_min", mainObj));
        //weather.temperature.setMaxTemp(getFloat("temp_max", mainObj));
        if (mainObj.has("pressure")) {
            weather.currentCondition.setPressure(getFloat("pressure", mainObj));
        }
        if (mainObj.has("humidity")) {
            weather.currentCondition.setHumidity(mainObj.getInt("humidity"));
        }

        JSONObject windObj = jWeatherData.getJSONObject("wind");
        if (windObj.has("speed")) {
            weather.wind.setSpeed(getFloat("speed", windObj));
        }
        if (windObj.has("deg")) {
            weather.wind.setDirection(getFloat("deg", windObj));
        }

        JSONObject cloudsObj = getObject("clouds", jWeatherData);
        if (cloudsObj.has("all")) {
            weather.cloud.setClouds(getInt("all", cloudsObj));
        }

        if (jWeatherData.has("name")) {
            location.setCityName(jWeatherData.getString("name"));
        }

        JSONObject sysObj = jWeatherData.getJSONObject("sys");
        if (sysObj.has("country")) {
            location.setCountryCode(sysObj.getString("country"));
        }
        weather.location = location;

        return weather;
    }

    private static JSONObject getObject(String tag, JSONObject jsonObject) throws JSONException
    {
        JSONObject jsonObj = jsonObject.getJSONObject(tag);

        return jsonObj;
    }

    private static String getString(String tag, JSONObject jsonObject) throws JSONException
    {
        return jsonObject.getString(tag);
    }

    private static int getInt(String tag, JSONObject jsonObject) throws JSONException
    {
        return jsonObject.getInt(tag);
    }

    private static float getFloat(String tag, JSONObject jsonObject) throws JSONException
    {
        return (float) jsonObject.getDouble(tag);
    }
}
