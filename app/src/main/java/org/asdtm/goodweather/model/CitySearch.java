package org.asdtm.goodweather.model;

public class CitySearch
{
    private String mCityName;
    private String mCountry;
    private float mLatitude;
    private float mLongitude;

    public CitySearch(){}
    public CitySearch(String cityName, String country, float latitude, float longitude)
    {
        mCityName = cityName;
        mCountry = country;
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public String getCityName()
    {
        return mCityName;
    }

    public void setCityName(String cityName)
    {
        mCityName = cityName;
    }

    public String getCountry()
    {
        return mCountry;
    }

    public void setCountry(String country)
    {
        mCountry = country;
    }

    public float getLatitude()
    {
        return mLatitude;
    }

    public void setLatitude(float latitude)
    {
        mLatitude = latitude;
    }

    public float getLongitude()
    {
        return mLongitude;
    }

    public void setLongitude(float longitude)
    {
        mLongitude = longitude;
    }

    @Override
    public String toString()
    {
        return mCityName + ", " + mCountry;
    }
}
