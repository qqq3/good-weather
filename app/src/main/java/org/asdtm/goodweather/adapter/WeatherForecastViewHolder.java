package org.asdtm.goodweather.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.asdtm.goodweather.R;
import org.asdtm.goodweather.fragment.ForecastBottomSheetDialogFragment;
import org.asdtm.goodweather.model.WeatherForecast;
import org.asdtm.goodweather.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WeatherForecastViewHolder extends RecyclerView.ViewHolder implements
        View.OnClickListener {

    private final String TAG = "ForecastViewHolder";

    private WeatherForecast mWeatherForecast;
    private Context mContext;
    private FragmentManager mFragmentManager;

    private TextView mDateTime;
    private TextView mIcon;
    private TextView mTemperatureMin;
    private TextView mTemperatureMax;

    public WeatherForecastViewHolder(View itemView, Context context,
                                     FragmentManager fragmentManager) {
        super(itemView);
        mContext = context;
        mFragmentManager = fragmentManager;
        itemView.setOnClickListener(this);

        mDateTime = (TextView) itemView.findViewById(R.id.forecast_date_time);
        mIcon = (TextView) itemView.findViewById(R.id.forecast_icon);
        mTemperatureMin = (TextView) itemView.findViewById(R.id.forecast_temperature_min);
        mTemperatureMax = (TextView) itemView.findViewById(R.id.forecast_temperature_max);
    }

    void bindWeather(WeatherForecast weather) {
        mWeatherForecast = weather;

        Typeface typeface = Typeface.createFromAsset(mContext.getAssets(),
                                                     "fonts/weathericons-regular-webfont.ttf");
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMMM", Locale.getDefault());
        Date date = new Date(weather.getDateTime() * 1000);
        String temperatureMin = mContext.getString(R.string.temperature_with_degree,
                                                   String.format(Locale.getDefault(), "%.0f",
                                                                 weather.getTemperatureMin()));
        String temperatureMax = mContext.getString(R.string.temperature_with_degree,
                                                   String.format(Locale.getDefault(), "%.0f",
                                                                 weather.getTemperatureMax()));

        mDateTime.setText(format.format(date));
        mIcon.setTypeface(typeface);
        mIcon.setText(Utils.getStrIcon(mContext, weather.getIcon()));
        if (weather.getTemperatureMin() > 0) {
            temperatureMin = "+" + temperatureMin;
        }
        mTemperatureMin.setText(temperatureMin);
        if (weather.getTemperatureMax() > 0) {
            temperatureMax = "+" + temperatureMax;
        }
        mTemperatureMax.setText(temperatureMax);
    }

    @Override
    public void onClick(View view) {
        new ForecastBottomSheetDialogFragment()
                .newInstance(mWeatherForecast)
                .show(mFragmentManager, "forecastBottomSheet");
    }
}