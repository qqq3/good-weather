package org.asdtm.goodweather;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class SettingsFragment extends Fragment
{
    private Toolbar mToolbar;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_settings, parent, false);

        mToolbar = (Toolbar) v.findViewById(R.id.toolbar);

        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.setSupportActionBar(mToolbar);
        appCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return v;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
