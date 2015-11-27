package org.asdtm.goodweather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherJSONParser
{
    private static final String TAG = "WeatherJSONParser";

    public static Weather getWeather(String data) throws JSONException
    {
        Weather weather = new Weather();

        JSONObject jWeatherData = new JSONObject(data);


        JSONArray jWeatherArray = jWeatherData.getJSONArray("weather");

        JSONObject weatherObj = jWeatherArray.getJSONObject(0);
        //weather.currentWeather.setDescription(getString("description", weatherOdj));
        weather.currentWeather.setDescription(weatherObj.getString("description"));
        weather.currentWeather.setIdIcon(getString("icon", weatherObj));

        JSONObject mainObj = jWeatherData.getJSONObject("main");
        weather.temperature.setTemp(getFloat("temp", mainObj));
        weather.currentCondition.setPressure(getFloat("pressure", mainObj));
        weather.currentCondition.setHumidity(mainObj.getInt("humidity"));

        JSONObject windObj = jWeatherData.getJSONObject("wind");
        weather.wind.setSpeed(getFloat("speed", windObj));
        weather.wind.setDirection(getFloat("deg", windObj));

        JSONObject cloudsObj = getObject("clouds", jWeatherData);
        weather.cloud.setClouds(getInt("all", cloudsObj));

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
