package org.asdtm.goodweather.model;

public class CitySearch
{
    private String mCityName;
    private String mCountry;
    private String mLatitude;
    private String mLongitude;

    public CitySearch(){}
    public CitySearch(String cityName, String country, String latitude, String longitude)
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

    public String getLatitude()
    {
        return mLatitude;
    }

    public void setLatitude(String latitude)
    {
        mLatitude = latitude;
    }

    public String getLongitude()
    {
        return mLongitude;
    }

    public void setLongitude(String longitude)
    {
        mLongitude = longitude;
    }

    @Override
    public String toString()
    {
        return mCityName + ", " + mCountry;
    }
}
