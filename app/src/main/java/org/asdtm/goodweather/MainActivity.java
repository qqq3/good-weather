package org.asdtm.goodweather;

import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends SingleFragmentActivity
{
    @Override
    protected Fragment createNewFragment()
    {
        return new WeatherPageFragment();
    }
}
