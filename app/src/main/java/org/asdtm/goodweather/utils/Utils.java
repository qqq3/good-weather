package org.asdtm.goodweather.utils;

import android.app.AlarmManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Log;
import java.io.IOException;

import org.asdtm.goodweather.R;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;

public class Utils {

    public static Bitmap createWeatherIcon(Context context, String text) {
        Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        Typeface weatherFont = Typeface.createFromAsset(context.getAssets(),
                                                        "fonts/weathericons-regular-webfont.ttf");

        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setTypeface(weatherFont);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(AppPreference.getTextColor(context));
        paint.setTextSize(180);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(text, 128, 200, paint);
        return bitmap;
    }

    public static String getStrIcon(Context context, String iconId) {
        String icon;
        switch (iconId) {
            case "01d":
                icon = context.getString(R.string.icon_clear_sky_day);
                break;
            case "01n":
                icon = context.getString(R.string.icon_clear_sky_night);
                break;
            case "02d":
                icon = context.getString(R.string.icon_few_clouds_day);
                break;
            case "02n":
                icon = context.getString(R.string.icon_few_clouds_night);
                break;
            case "03d":
                icon = context.getString(R.string.icon_scattered_clouds);
                break;
            case "03n":
                icon = context.getString(R.string.icon_scattered_clouds);
                break;
            case "04d":
                icon = context.getString(R.string.icon_broken_clouds);
                break;
            case "04n":
                icon = context.getString(R.string.icon_broken_clouds);
                break;
            case "09d":
                icon = context.getString(R.string.icon_shower_rain);
                break;
            case "09n":
                icon = context.getString(R.string.icon_shower_rain);
                break;
            case "10d":
                icon = context.getString(R.string.icon_rain_day);
                break;
            case "10n":
                icon = context.getString(R.string.icon_rain_night);
                break;
            case "11d":
                icon = context.getString(R.string.icon_thunderstorm);
                break;
            case "11n":
                icon = context.getString(R.string.icon_thunderstorm);
                break;
            case "13d":
                icon = context.getString(R.string.icon_snow);
                break;
            case "13n":
                icon = context.getString(R.string.icon_snow);
                break;
            case "50d":
                icon = context.getString(R.string.icon_mist);
                break;
            case "50n":
                icon = context.getString(R.string.icon_mist);
                break;
            default:
                icon = context.getString(R.string.icon_weather_default);
        }

        return icon;
    }

    public static String getTemperatureScale(Context context) {
        String[] temperatureScaleArray = context.getResources().getStringArray(
                R.array.pref_temperature_entries);
        String unitPref = AppPreference.getTemperatureUnit(context);
        return unitPref.equals("metric") ?
                temperatureScaleArray[0] : temperatureScaleArray[1];
    }

    public static String getSpeedScale(Context context) {
        String unitPref = AppPreference.getTemperatureUnit(context);
        return unitPref.equals("metric") ?
                context.getString(R.string.wind_speed_meters) :
                context.getString(R.string.wind_speed_miles);
    }

    public static String setLastUpdateTime(Context context, long lastUpdate) {
        Date lastUpdateTime = new Date(lastUpdate);
        return DateFormat.getTimeFormat(context).format(lastUpdateTime);
    }

    public static long intervalMillisForAlarm(String intervalMinutes) {
        int interval = Integer.parseInt(intervalMinutes);
        switch (interval) {
            case 15:
                return AlarmManager.INTERVAL_FIFTEEN_MINUTES;
            case 30:
                return AlarmManager.INTERVAL_HALF_HOUR;
            case 60:
                return AlarmManager.INTERVAL_HOUR;
            case 720:
                return AlarmManager.INTERVAL_HALF_DAY;
            case 1440:
                return AlarmManager.INTERVAL_DAY;
            default:
                return interval * 60 * 1000;
        }
    }

    public static String unixTimeToFormatTime(Context context, long unixTime) {
        long unixTimeToMillis = unixTime * 1000;
        return DateFormat.getTimeFormat(context).format(unixTimeToMillis);
    }

    public static void copyToClipboard(Context context, String string) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(
                Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(string, string);
        clipboardManager.setPrimaryClip(clipData);
    }

    public static String windDegreeToDirections(Context context, double windDegree) {
        String[] directions = context.getResources().getStringArray(R.array.wind_directions);
        String[] arrows = context.getResources().getStringArray(R.array.wind_direction_arrows);
        int index = (int) Math.abs(Math.round(windDegree % 360) / 45);

        return directions[index] + " " + arrows[index];
    }

    public static URL getWeatherForecastUrl(String endpoint, String lat, String lon, String units, String lang) throws
                                                                                         MalformedURLException {
        String url = Uri.parse(endpoint)
                        .buildUpon()
                        .appendQueryParameter("appid", ApiKeys.OPEN_WEATHER_MAP_API_KEY)
                        .appendQueryParameter("lat", lat)
                        .appendQueryParameter("lon", lon)
                        .appendQueryParameter("units", units)
                        .appendQueryParameter("lang", "cs".equalsIgnoreCase(lang)?"cz":lang)
                        .build()
                        .toString();
        return new URL(url);
    }
    
    public static void getAndWriteAddressFromGeocoder(Geocoder geocoder, Address address, String latitude, String longitude, SharedPreferences.Editor editor) {
        try {
            String latitudeEn = latitude.replace(",", ".");
            String longitudeEn = longitude.replace(",", ".");
            if (address == null) {
                List<Address> addresses = geocoder.getFromLocation(Double.parseDouble(latitudeEn), Double.parseDouble(longitudeEn), 1);
                if((addresses != null) && (addresses.size() > 0)) {
                    address = addresses.get(0);
                }
            }
            if(address != null) {
                editor.putString(Constants.APP_SETTINGS_GEO_CITY, address.getLocality());
                editor.putString(Constants.APP_SETTINGS_GEO_COUNTRY_NAME, address.getCountryName());
                if(address.getAdminArea() != null) {
                    editor.putString(Constants.APP_SETTINGS_GEO_DISTRICT_OF_COUNTRY, address.getAdminArea());
                } else {
                    editor.putString(Constants.APP_SETTINGS_GEO_DISTRICT_OF_COUNTRY, null);
                }
            
                /*editor.putString(Constants.APP_SETTINGS_GEO_CITY, address.getSubAdminArea());*/
                editor.putString(Constants.APP_SETTINGS_GEO_DISTRICT_OF_CITY, address.getSubLocality());
                /*editor.putString(Constants.APP_SETTINGS_GEO_COUNTRY_NAME, address.getCountryName());*/
            }
        } catch (IOException | NumberFormatException ex) {
            Log.e(Utils.class.getName(), "Unable to get address from latitude and longitude", ex);
        }
    }
    
    public static String getCityAndCountry(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.APP_SETTINGS_NAME, 0);
        
        if(AppPreference.isGeocoderEnabled(context)) {
            return getCityAndCountryFromGeolocation(preferences);
        } else {
            return getCityAndCountryFromPreference(context);
        }
    }
        
    private static String getCityAndCountryFromGeolocation(SharedPreferences preferences) {
        String geoCountryName = preferences.getString(Constants.APP_SETTINGS_GEO_COUNTRY_NAME, "United Kingdom");
        String geoCity = preferences.getString(Constants.APP_SETTINGS_GEO_CITY, "");
        if("".equals(geoCity)) {
            return geoCountryName;
        }
        String geoDistrictOfCity = preferences.getString(Constants.APP_SETTINGS_GEO_DISTRICT_OF_CITY, "");
        if ("".equals(geoDistrictOfCity) || geoCity.equalsIgnoreCase(geoDistrictOfCity)) {
            String geoCountryDistrict = preferences.getString(Constants.APP_SETTINGS_GEO_DISTRICT_OF_COUNTRY, "");
            if ((geoCountryDistrict == null) || "".equals(geoCountryDistrict) || geoCity.equals(geoCountryDistrict)) {
            return geoCity + ", " + geoCountryName;
        }
            return geoCity + ", " + geoCountryDistrict + ", " + geoCountryName;
        }
        return geoCity + " - " + geoDistrictOfCity + ", " + geoCountryName;
    }

    private static String getCityAndCountryFromPreference(Context context) {
        String[] cityAndCountryArray = AppPreference.getCityAndCode(context);
        String cityAndCountry = cityAndCountryArray[0] + ", " + cityAndCountryArray[1];
        return cityAndCountry;
    }
}
