package org.asdtm.goodweather.adapter;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.asdtm.goodweather.R;
import org.asdtm.goodweather.model.WeatherForecast;

import java.util.List;

public class WeatherForecastAdapter extends RecyclerView.Adapter<WeatherForecastViewHolder> {

    private Context mContext;
    private List<WeatherForecast> mWeatherList;
    private FragmentManager mFragmentManager;

    public WeatherForecastAdapter(Context context, List<WeatherForecast> weather, FragmentManager fragmentManager) {
        mContext = context;
        mWeatherList = weather;
        mFragmentManager = fragmentManager;
    }

    @Override
    public WeatherForecastViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.forecast_item, parent, false);
        return new WeatherForecastViewHolder(v, mContext, mFragmentManager);
    }

    @Override
    public void onBindViewHolder(WeatherForecastViewHolder holder, int position) {
        WeatherForecast weather = mWeatherList.get(position);
        holder.bindWeather(weather);
    }

    @Override
    public int getItemCount() {
        return (mWeatherList != null ? mWeatherList.size() : 0);
    }
}

