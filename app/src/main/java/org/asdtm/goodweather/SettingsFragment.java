package org.asdtm.goodweather;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import org.asdtm.goodweather.model.CitySearch;

import java.util.ArrayList;
import java.util.List;

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

        AutoCompleteTextView mSearchCity = (AutoCompleteTextView) v.findViewById(R.id.autoComplete_search_city);
        CityAdapter cityAdapter = new CityAdapter(getActivity(), null);
        mSearchCity.setAdapter(cityAdapter);

        final TextView mCurrentCity = (TextView) v.findViewById(R.id.currentCity);
        mSearchCity.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                CitySearch result = (CitySearch) parent.getItemAtPosition(position);
                mCurrentCity.setText("Current city: " + result.getCityName());
            }
        });
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