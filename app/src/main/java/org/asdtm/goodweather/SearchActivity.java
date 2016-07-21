package org.asdtm.goodweather;

import android.app.SearchManager;
import android.content.Context;
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
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SearchView;
import android.widget.TextView;

import org.asdtm.goodweather.utils.Preferences;
import org.asdtm.goodweather.model.CitySearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    public static final String TAG = "SearchActivity";

    private final String APP_SETTINGS_NAME = "config";
    private final String APP_SETTINGS_CITY = "city";
    private final String APP_SETTINGS_COUNTRY = "country";
    private final String APP_SETTINGS_COUNTRY_CODE = "country_code";
    private final String APP_SETTINGS_LATITUDE = "latitude";
    private final String APP_SETTINGS_LONGITUDE = "longitude";

    private RecyclerView mFoundCityRecyclerView;
    private List<CitySearch> mCites;
    private SearchCityAdapter mSearchCityAdapter;
    private SharedPreferences mCityPref;
    private Preferences mPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.search_activity_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mPreferences = new Preferences(SearchActivity.this, APP_SETTINGS_NAME);
        mCityPref = getSharedPreferences(APP_SETTINGS_NAME, 0);
        setCurrentLocale();

        mFoundCityRecyclerView = (RecyclerView) findViewById(R.id.search_city_recycler_view);
        mFoundCityRecyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));

        mCites = new ArrayList<>();
        mSearchCityAdapter = new SearchCityAdapter(mCites);
        mFoundCityRecyclerView.setAdapter(mSearchCityAdapter);

        loadLastFoundCity();
    }

    private class SearchCityHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private CitySearch mCity;
        private TextView mCityName;
        private TextView mCountryName;

        public SearchCityHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mCityName = (TextView) itemView.findViewById(R.id.city_name);
            mCountryName = (TextView) itemView.findViewById(R.id.country_name);
        }

        public void bindCity(CitySearch city) {
            mCity = city;
            mCityName.setText(city.getCityName());
            mCountryName.setText(city.getCountry());
        }

        @Override
        public void onClick(View v) {
            v.setBackgroundColor(Color.rgb(227, 227, 227));

            setCity(mCity);
            finish();
        }
    }

    private class SearchCityAdapter extends RecyclerView.Adapter<SearchCityHolder> implements
            Filterable {

        private List<CitySearch> mCites;

        public SearchCityAdapter(List<CitySearch> cites) {
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

                    List<CitySearch> citySearchList = YahooParser.getCity(charSequence.toString(),
                                                                          mPreferences.getLocale());
                    filterResults.values = citySearchList;
                    filterResults.count = citySearchList.size();

                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence charSequence,
                                              FilterResults filterResults) {
                    mCites.clear();
                    mCites.addAll((ArrayList<CitySearch>) filterResults.values);
                    notifyDataSetChanged();
                }
            };
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search_city).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconified(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            searchView.onActionViewExpanded();
        }
        int searchPlateId = searchView.getContext().getResources().getIdentifier(
                "android:id/search_plate", null, null);
        View searchPlateView = searchView.findViewById(searchPlateId);
        if (searchPlateView != null) {
            int color = ContextCompat.getColor(SearchActivity.this, R.color.colorPrimary);
            searchPlateView.setBackgroundColor(color);
        }

        searchView.setOnQueryTextListener(this);

        return true;
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

    @Override
    public boolean onQueryTextSubmit(String s) {
        mSearchCityAdapter.getFilter().filter(s);

        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        mSearchCityAdapter.getFilter().filter(s);

        return true;
    }

    private void setCity(CitySearch city) {
        SharedPreferences.Editor editor = mCityPref.edit();
        editor.putString(APP_SETTINGS_CITY, city.getCityName());
        editor.putString(APP_SETTINGS_COUNTRY, city.getCountry());
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

    private void setCurrentLocale() {
        String currentLocale;
        /**
         * Check API version and based on this gets the current value of the default locale
         * with specified Category or without
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            currentLocale = Locale.getDefault(Locale.Category.DISPLAY).getLanguage();
        } else {
            currentLocale = Locale.getDefault().getLanguage();
        }
        mPreferences.setLocale(currentLocale);
    }
}