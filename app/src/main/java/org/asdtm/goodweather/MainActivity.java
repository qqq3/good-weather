package org.asdtm.goodweather;

import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends SingleFragmentActivity
{
    protected static String stateFragment = "MainActivity";

    @Override
    protected Fragment createNewFragment()
    {
        return new WeatherPageFragment();
    }

    @Override
    public void onBackPressed()
    {
        switch (stateFragment) {
            case "WeatherPageFragment":
                DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    super.onBackPressed();
                }
                break;
        }
    }
}
