package org.asdtm.goodweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.asdtm.goodweather.service.CurrentWeatherService;
import org.asdtm.goodweather.utils.AppPreference;

public class BaseActivity extends AppCompatActivity {

    private final String TAG = "BaseActivity";
    static final int PICK_CITY = 1;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar mToolbar;
    private String[] mCityAndCode;
    private TextView mHeaderCity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        getToolbar();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupNavDrawer();
    }

    private void setupNavDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (mDrawerLayout == null) {
            return;
        }
        mDrawerToggle = new ActionBarDrawerToggle(this,
                                                  mDrawerLayout,
                                                  mToolbar,
                                                  R.string.navigation_drawer_open,
                                                  R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        if (mToolbar != null) {
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        configureNavView();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void configureNavView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(navigationViewListener);

        mCityAndCode = AppPreference.getCityAndCode(this);
        View headerLayout = navigationView.getHeaderView(0);
        mHeaderCity = (TextView) headerLayout.findViewById(R.id.nav_header_city);
        mHeaderCity.setText(mCityAndCode[0] + ", " + mCityAndCode[1]);
    }

    private NavigationView.OnNavigationItemSelectedListener navigationViewListener =
            new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.nav_menu_current_weather:
                            startActivity(new Intent(BaseActivity.this, MainActivity.class));
                            break;
                        case R.id.nav_menu_graphs:
                            createBackStack(new Intent(BaseActivity.this,
                                                       GraphsActivity.class));
                            break;
                        case R.id.nav_menu_weather_forecast:
                            createBackStack(new Intent(BaseActivity.this,
                                                       WeatherForecastActivity.class));
                            break;
                        case R.id.nav_settings:
                            createBackStack(new Intent(BaseActivity.this,
                                                       SettingsActivity.class));
                            break;
                        case R.id.nav_feedback:
                            Intent sendMessage = new Intent(Intent.ACTION_SEND);
                            sendMessage.setType("message/rfc822");
                            sendMessage.putExtra(Intent.EXTRA_EMAIL, new String[]{
                                    getResources().getString(R.string.feedback_email)});
                            try {
                                startActivity(Intent.createChooser(sendMessage, "Send feedback"));
                            } catch (android.content.ActivityNotFoundException e) {
                                Toast.makeText(BaseActivity.this, "Communication app not found",
                                               Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case R.id.nav_menu_bitcoin_donation:
                            BitcoinDonationDialog dialog = BitcoinDonationDialog.newInstance();
                            dialog.show(getFragmentManager(), "bitcoinDonationDialog");
                            break;
                    }

                    mDrawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }
            };

    private void createBackStack(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            TaskStackBuilder builder = TaskStackBuilder.create(this);
            builder.addNextIntentWithParentStack(intent);
            builder.startActivities();
        } else {
            startActivity(intent);
            finish();
        }
    }

    protected Toolbar getToolbar() {
        if (mToolbar == null) {
            mToolbar = (Toolbar) findViewById(R.id.toolbar);
            if (mToolbar != null) {
                setSupportActionBar(mToolbar);
            }
        }

        return mToolbar;
    }

    @Override
    public void onBackPressed() {
        if (isNavDrawerOpen()) {
            closeNavDraw();
        } else {
            super.onBackPressed();
        }
    }

    protected boolean isNavDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    protected void closeNavDraw() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    @NonNull
    protected ProgressDialog getProgressDialog() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.isIndeterminate();
        dialog.setMessage(getString(R.string.load_progress));
        dialog.setCancelable(false);
        return dialog;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICK_CITY:
                ConnectionDetector connectionDetector = new ConnectionDetector(this);
                if (resultCode == RESULT_OK) {
                    mCityAndCode = AppPreference.getCityAndCode(this);
                    mHeaderCity.setText(mCityAndCode[0] + ", " + mCityAndCode[1]);

                    if (connectionDetector.isNetworkAvailableAndConnected()) {
                        startService(new Intent(this, CurrentWeatherService.class));
                    }
                }
                break;
        }
    }
}
