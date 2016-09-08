package org.asdtm.goodweather.model;

public class CitySearch
{
    private String mCityName;
    private String mCountry;
    private String mLatitude;
    private String mLongitude;
    private String mCountryCode;

    public CitySearch(){}
    public CitySearch(String cityName, String countryCode, String latitude, String longitude)
    {
        mCityName = cityName;
        mCountryCode = countryCode;
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

    public String getCountryCode()
    {
        return mCountryCode;
    }

    public void setCountryCode(String countryCode)
    {
        mCountryCode = countryCode;
    }

    @Override
    public String toString()
    {
        return mCityName + ", " + mCountryCode;
    }
}
