package org.asdtm.goodweather.model;

import java.io.Serializable;

public class WeatherForecast implements Serializable {

    private long dateTime;
    private float temperatureMin;
    private float temperatureMax;
    private float temperatureMorning;
    private float temperatureDay;
    private float temperatureEvening;
    private float temperatureNight;
    private String pressure;
    private String humidity;
    private String icon;
    private String description;
    private String windSpeed;
    private String windDegree;
    private String cloudiness;
    private String rain;
    private String snow;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description.substring(0, 1).toUpperCase() + description.substring(1);
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getRain() {
        return rain;
    }

    public void setRain(String rain) {
        this.rain = rain;
    }

    public String getSnow() {
        return snow;
    }

    public void setSnow(String snow) {
        this.snow = snow;
    }

    public String getWindDegree() {
        return windDegree;
    }

    public void setWindDegree(String windDegree) {
        this.windDegree = windDegree;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(String windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getCloudiness() {
        return cloudiness;
    }

    public void setCloudiness(String cloudiness) {
        this.cloudiness = cloudiness;
    }

    public float getTemperatureDay() {
        return temperatureDay;
    }

    public void setTemperatureDay(float temperatureDay) {
        this.temperatureDay = temperatureDay;
    }

    public float getTemperatureEvening() {
        return temperatureEvening;
    }

    public void setTemperatureEvening(float temperatureEvening) {
        this.temperatureEvening = temperatureEvening;
    }

    public float getTemperatureMax() {
        return temperatureMax;
    }

    public void setTemperatureMax(float temperatureMax) {
        this.temperatureMax = temperatureMax;
    }

    public float getTemperatureMin() {
        return temperatureMin;
    }

    public void setTemperatureMin(float temperatureMin) {
        this.temperatureMin = temperatureMin;
    }

    public float getTemperatureMorning() {
        return temperatureMorning;
    }

    public void setTemperatureMorning(float temperatureMorning) {
        this.temperatureMorning = temperatureMorning;
    }

    public float getTemperatureNight() {
        return temperatureNight;
    }

    public void setTemperatureNight(float temperatureNight) {
        this.temperatureNight = temperatureNight;
    }
}
