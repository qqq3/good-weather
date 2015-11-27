package org.asdtm.goodweather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherJSONParser
{
    public static Weather getWeather(String weatherData) throws JSONException
    {
        Weather weather = new Weather();

        JSONObject jWeatherData = new JSONObject(weatherData);


        JSONArray jWeatherArray = jWeatherData.getJSONArray("weather");

        JSONObject weatherOdj = jWeatherArray.getJSONObject(0);
        weather.currentWeather.setDescription(getString("description", weatherOdj));
        weather.currentWeather.setIdIcon(getString("icon", weatherOdj));

        JSONObject mainObj = jWeatherData.getJSONObject("main");
        weather.temperature.setTemp(getFloat("temp", mainObj));
        weather.currentCondition.setPressure(getFloat("pressure", mainObj));
        weather.currentCondition.setHumidity(mainObj.getInt("humidity"));

        JSONObject windObj = jWeatherData.getJSONObject("wind");
        weather.wind.setSpeed(getFloat("speed", windObj));
        weather.wind.setDirection(getFloat("deg", windObj));

        JSONObject cloudsObj = jWeatherData.getJSONObject("clouds");
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
