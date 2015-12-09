package org.asdtm.goodweather;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.asdtm.goodweather.model.CitySearch;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment
{
    private static final String TAG = "SettingsFragment";

    private Toolbar mToolbar;
    private SharedPreferences mPreferences;
    private AutoCompleteTextView mSearchCity;
    private TextView mCurrentCity;
    private LinearLayout mSettingsLayout;
    private RadioGroup mTemperatureGroup;
    private RadioButton mCelUnit;
    private RadioButton mFahrUnit;

    final String APP_SETTINGS = "config";
    final String APP_SETTINGS_CITY = "city";
    final String APP_SETTINGS_COUNTRY = "country";
    final String APP_SETTINGS_UNITS = "units";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate!!!");
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_settings, parent, false);
        mPreferences = getActivity().getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);

        mToolbar = (Toolbar) v.findViewById(R.id.toolbar);
        mSettingsLayout = (LinearLayout) v.findViewById(R.id.settings);

        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.setSupportActionBar(mToolbar);
        appCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSearchCity = (AutoCompleteTextView) v.findViewById(R.id.autoComplete_search_city);
        CityAdapter cityAdapter = new CityAdapter(getActivity(), null);
        mSearchCity.setAdapter(cityAdapter);

        mCurrentCity = (TextView) v.findViewById(R.id.currentCity);
        String city = mPreferences.getString(APP_SETTINGS_CITY, "London");
        String country = mPreferences.getString(APP_SETTINGS_COUNTRY, "United Kingdom");
        mCurrentCity.setText(city + ", " + country);

        mSearchCity.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                CitySearch result = (CitySearch) parent.getItemAtPosition(position);
                mCurrentCity.setText("" + result);
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putString(APP_SETTINGS_CITY, result.getCityName());
                editor.putString(APP_SETTINGS_COUNTRY, result.getCountry());
                editor.apply();

                mSearchCity.clearFocus();
                mCurrentCity.requestFocus();

                mSearchCity.setText("");

                InputMethodManager iMM = ((InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE));
                iMM.hideSoftInputFromWindow(mCurrentCity.getWindowToken(), 0);
            }
        });

        mTemperatureGroup = (RadioGroup) v.findViewById(R.id.temperature_radioGroup);

        int checkedTempUnits = mTemperatureGroup.getCheckedRadioButtonId();

        mTemperatureGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch (checkedId) {
                    case R.id.radioButton_celsius:

                }
            }
        });

        Log.i(TAG, "onCreateView!!!");
        return v;
    }

    private class CityAdapter extends ArrayAdapter<CitySearch> implements Filterable
    {
        private Context mContext;
        private List<CitySearch> listCity = new ArrayList<CitySearch>();

        public CityAdapter(Context context, List<CitySearch> list)
        {
            super(context, R.layout.result_search_city_layout, list);
            mContext = context;
            listCity = list;
        }

        @Override
        public int getCount()
        {
            if (listCity != null)
                return listCity.size();

            return 0;
        }

        @Override
        public CitySearch getItem(int index)
        {
            if (listCity != null)
                return listCity.get(index);

            return null;
        }

        @Override
        public long getItemId(int position)
        {
            if (listCity != null) {
                return position;
            }

            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(R.layout.result_search_city_layout, parent, false);
            }

            TextView resultSearch = (TextView) convertView.findViewById(R.id.result_search);
            resultSearch.setText(listCity.get(position).getCityName()
                    + ", "
                    + listCity.get(position).getCountry());


            return convertView;
        }

        @Override
        public Filter getFilter()
        {
            Filter cityFilter = new Filter()
            {
                @Override
                protected FilterResults performFiltering(CharSequence constraint)
                {
                    FilterResults filterResults = new FilterResults();
                    if (constraint == null || constraint.length() < 3)
                        return filterResults;

                    List<CitySearch> citySearchList = YahooParser.getCity(constraint.toString());
                    filterResults.values = citySearchList;
                    filterResults.count = citySearchList.size();

                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results)
                {
                    listCity = (List) results.values;
                    notifyDataSetChanged();
                }
            };

            return cityFilter;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(getActivity()) != null)
                {
                    NavUtils.navigateUpFromSameTask(getActivity());
                    return true;
                }
        }

        return super.onOptionsItemSelected(item);
    }
}
