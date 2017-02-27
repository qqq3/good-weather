package org.asdtm.goodweather;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.asdtm.goodweather.model.CitySearch;
import org.asdtm.goodweather.utils.AppPreference;
import org.asdtm.goodweather.utils.CityParser;
import org.asdtm.goodweather.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    public static final String TAG = "SearchActivity";

    private final String APP_SETTINGS_CITY = "city";
    private final String APP_SETTINGS_COUNTRY_CODE = "country_code";
    private final String APP_SETTINGS_LATITUDE = "latitude";
    private final String APP_SETTINGS_LONGITUDE = "longitude";

    private List<CitySearch> mCites;
    private SearchCityAdapter mSearchCityAdapter;
    private SharedPreferences mCityPref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
        setContentView(R.layout.activity_search);

        setupActionBar();
        setupSearchView();

        String APP_SETTINGS_NAME = "config";
        mCityPref = getSharedPreferences(APP_SETTINGS_NAME, 0);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.search_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));

        mCites = new ArrayList<>();
        mSearchCityAdapter = new SearchCityAdapter(mCites);
        recyclerView.setAdapter(mSearchCityAdapter);

        loadLastFoundCity();
    }

    private void setupSearchView() {
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        SearchView searchView = (SearchView) findViewById(R.id.search_view);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconified(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchCityAdapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mSearchCityAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppPreference.setLocale(this, Constants.APP_SETTINGS_NAME);
    }

    private class SearchCityHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private CitySearch mCity;
        private TextView mCityName;
        private TextView mCountryName;

        SearchCityHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mCityName = (TextView) itemView.findViewById(R.id.city_name);
            mCountryName = (TextView) itemView.findViewById(R.id.country_code);
        }

        void bindCity(CitySearch city) {
            mCity = city;
            mCityName.setText(city.getCityName());
            mCountryName.setText(city.getCountryCode());
        }

        @Override
        public void onClick(View v) {
            v.setBackgroundColor(Color.rgb(227, 227, 227));
            setCity(mCity);
            sendBroadcast(new Intent(Constants.ACTION_FORCED_APPWIDGET_UPDATE));
            setResult(RESULT_OK);
            finish();
        }
    }

    private class SearchCityAdapter extends RecyclerView.Adapter<SearchCityHolder> implements
            Filterable {

        private List<CitySearch> mCites;

        SearchCityAdapter(List<CitySearch> cites) {
            mCites = cites;
        }

        @Override
        public int getItemCount() {
            if (mCites != null)
                return mCites.size();

            return 0;
        }

        @Override
        public void onBindViewHolder(SearchCityHolder holder, int position) {
            CitySearch city = mCites.get(position);
            holder.bindCity(city);
        }

        @Override
        public SearchCityHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(SearchActivity.this);
            View v = inflater.inflate(R.layout.city_item, parent, false);

            return new SearchCityHolder(v);
        }

        @Override
        public Filter getFilter() {

            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    FilterResults filterResults = new FilterResults();

                    List<CitySearch> citySearchList = CityParser.getCity(charSequence.toString());
                    filterResults.values = citySearchList;
                    filterResults.count = citySearchList != null ? citySearchList.size() : 0;

                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence charSequence,
                                              FilterResults filterResults) {
                    mCites.clear();
                    if (filterResults.values != null) {
                        mCites.addAll((ArrayList<CitySearch>) filterResults.values);
                    }
                    notifyDataSetChanged();
                }
            };
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setCity(CitySearch city) {
        SharedPreferences.Editor editor = mCityPref.edit();
        editor.putString(APP_SETTINGS_CITY, city.getCityName());
        editor.putString(APP_SETTINGS_COUNTRY_CODE, city.getCountryCode());
        editor.putString(APP_SETTINGS_LATITUDE, city.getLatitude());
        editor.putString(APP_SETTINGS_LONGITUDE, city.getLongitude());
        editor.apply();
    }

    private void loadLastFoundCity() {
        if (mCites.isEmpty()) {
            String lastCity = mCityPref.getString(APP_SETTINGS_CITY, "London");
            String lastCountry = mCityPref.getString(APP_SETTINGS_COUNTRY_CODE, "UK");
            String lastLat = mCityPref.getString(APP_SETTINGS_LATITUDE, "51.51");
            String lastLon = mCityPref.getString(APP_SETTINGS_LONGITUDE, "-0.13");
            CitySearch city = new CitySearch(lastCity, lastCountry, lastLat, lastLon);
            mCites.add(city);
        }
    }
}
