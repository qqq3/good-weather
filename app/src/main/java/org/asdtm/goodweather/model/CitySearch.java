package org.asdtm.goodweather.model;

public class CitySearch
{
    private String mCityName;
    private String mCountry;

    public CitySearch(){}
    public CitySearch(String cityName, String country)
    {
        mCityName = cityName;
        mCountry = country;
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

    @Override
    public String toString()
    {
        return mCityName + ", " + mCountry;
    }
}
