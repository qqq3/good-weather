package org.asdtm.goodweather.fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.asdtm.goodweather.R;
import org.asdtm.goodweather.model.WeatherForecast;
import org.asdtm.goodweather.utils.Utils;

import java.util.Locale;

public class ForecastBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private WeatherForecast mWeather;

    public ForecastBottomSheetDialogFragment newInstance(WeatherForecast weather) {
        ForecastBottomSheetDialogFragment fragment = new ForecastBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("weatherForecast", weather);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWeather = (WeatherForecast) getArguments().getSerializable("weatherForecast");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_forecast_bottom_sheet, parent, false);

        String speedScale = Utils.getSpeedScale(getActivity());
        String percentSign = getActivity().getString(R.string.percent_sign);
        String pressureMeasurement = getActivity().getString(R.string.pressure_measurement);
        String mmLabel = getString(R.string.millimetre_label);

        Float temperatureMorning = mWeather.getTemperatureMorning();
        Float temperatureDay = mWeather.getTemperatureDay();
        Float temperatureEvening = mWeather.getTemperatureEvening();
        Float temperatureNight = mWeather.getTemperatureNight();

        String description = mWeather.getDescription();
        String temperatureMorningStr = getActivity().getString(R.string.temperature_with_degree,
                                                               String.format(Locale.getDefault(),
                                                                             "%.0f",
                                                                             temperatureMorning));
        String temperatureDayStr = getActivity().getString(R.string.temperature_with_degree,
                                                           String.format(Locale.getDefault(),
                                                                         "%.0f",
                                                                         temperatureDay));
        String temperatureEveningStr = getActivity().getString(R.string.temperature_with_degree,
                                                               String.format(Locale.getDefault(),
                                                                             "%.0f",
                                                                             temperatureEvening));
        String temperatureNightStr = getActivity().getString(R.string.temperature_with_degree,
                                                             String.format(Locale.getDefault(),
                                                                           "%.0f",
                                                                           temperatureNight));
        String wind = getActivity().getString(R.string.wind_label, mWeather.getWindSpeed(), speedScale);
        String windDegree = mWeather.getWindDegree();
        String windDirection = Utils.windDegreeToDirections(getActivity(),
                                                            Double.parseDouble(windDegree));
        String rain = getString(R.string.rain_label, mWeather.getRain(), mmLabel);
        String snow = getString(R.string.snow_label, mWeather.getSnow(), mmLabel);
        String pressure = getActivity().getString(R.string.pressure_label, mWeather.getPressure(), pressureMeasurement);
        String humidity = getActivity().getString(R.string.humidity_label, mWeather.getHumidity(), percentSign);

        TextView descriptionView = (TextView) v.findViewById(R.id.forecast_description);
        TextView windView = (TextView) v.findViewById(R.id.forecast_wind);
        TextView rainView = (TextView) v.findViewById(R.id.forecast_rain);
        TextView snowView = (TextView) v.findViewById(R.id.forecast_snow);
        TextView humidityView = (TextView) v.findViewById(R.id.forecast_humidity);
        TextView pressureView = (TextView) v.findViewById(R.id.forecast_pressure);

        TextView temperatureMorningView = (TextView) v.findViewById(
                R.id.forecast_morning_temperature);
        TextView temperatureDayView = (TextView) v.findViewById(
                R.id.forecast_day_temperature);
        TextView temperatureEveningView = (TextView) v.findViewById(
                R.id.forecast_evening_temperature);
        TextView temperatureNightView = (TextView) v.findViewById(
                R.id.forecast_night_temperature);
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(),
                                                     "fonts/weathericons-regular-webfont.ttf");

        descriptionView.setText(description);
        windView.setTypeface(typeface);
        windView.setText(wind + " " + windDirection);
        rainView.setText(rain);
        snowView.setText(snow);
        humidityView.setText(humidity);
        pressureView.setText(pressure);
        if (temperatureMorning > 0) {
            temperatureMorningStr = "+" + temperatureMorningStr;
        }
        if (temperatureDay > 0) {
            temperatureDayStr = "+" + temperatureDayStr;
        }
        if (temperatureEvening > 0) {
            temperatureEveningStr = "+" + temperatureEveningStr;
        }
        if (temperatureNight > 0) {
            temperatureNightStr = "+" + temperatureNightStr;
        }
        temperatureMorningView.setText(temperatureMorningStr);
        temperatureDayView.setText(temperatureDayStr);
        temperatureEveningView.setText(temperatureEveningStr);
        temperatureNightView.setText(temperatureNightStr);

        return v;
    }
}
