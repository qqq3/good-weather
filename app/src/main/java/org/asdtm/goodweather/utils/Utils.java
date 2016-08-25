package org.asdtm.goodweather.utils;//parse("File Header.java")

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;

import org.asdtm.goodweather.R;

public class Utils {

    public static Bitmap createWeatherIcon(Context context, String text) {
        Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        Typeface weatherFont = Typeface.createFromAsset(context.getAssets(),
                                                        "fonts/weathericons-regular-webfont.ttf");
        int textColor;
        if (!AppPreference.isLightThemeEnabled(context)) {
            textColor = ContextCompat.getColor(context, R.color.widget_darkTheme_textColorPrimary);
        } else {
            textColor = ContextCompat.getColor(context, R.color.widget_lightTheme_textColorPrimary);
        }

        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setTypeface(weatherFont);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(textColor);
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
            case "05d":
                icon = context.getString(R.string.icon_shower_rain);
                break;
            case "05n":
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
}
