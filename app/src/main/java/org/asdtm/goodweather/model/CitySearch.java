package org.asdtm.goodweather.model;

public class CitySearch {
    private String mCityName;
    private String mCountryName;
    private String mCountryCode;
    private String mAdminName;
    private String mToponymName;
    private String mLatitude;
    private String mLongitude;

    public CitySearch() {
    }

    public CitySearch(String cityName, String toponymName, String adminName, String countryName, String latitude, String longitude) {
        mCityName = cityName;
        mToponymName = toponymName;
        mAdminName = adminName;
        mCountryName = countryName;
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public String getCityName() {
        return mCityName;
    }

    public void setCityName(String cityName) {
        mCityName = cityName;
    }

    public String getCountryName() {
        return mCountryName;
    }

    public void setCountryName(String countryName) {
        mCountryName = countryName;
    }

    public String getLatitude() {
        return mLatitude;
    }

    public void setLatitude(String latitude) {
        mLatitude = latitude;
    }

    public String getLongitude() {
        return mLongitude;
    }

    public void setLongitude(String longitude) {
        mLongitude = longitude;
    }

    public String getCountryCode() {
        return mCountryCode;
    }

    public void setCountryCode(String countryCode) {
        mCountryCode = countryCode;
    }

    public String getAdminName() {
        return mAdminName;
    }

    public void setAdminName(String adminName) {
        mAdminName = adminName;
    }

    public String getToponymName() {
        return mToponymName;
    }

    public void setToponymName(String toponymName) {
        mToponymName = toponymName;
    }

    @Override
    public String toString() {
        return mCityName + ", " + mAdminName + " (" + mCountryName + ")";
    }

    public String toCoordinates() {
        return "[" + mLatitude + ", " + mLongitude + "]";
    }
}
