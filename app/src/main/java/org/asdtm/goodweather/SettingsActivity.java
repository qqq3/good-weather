package org.asdtm.goodweather;

import android.support.v4.app.Fragment;

public class SettingsActivity extends SingleFragmentActivity
{
    @Override
    protected Fragment createNewFragment()
    {
        return new SettingsFragment();
    }
}
