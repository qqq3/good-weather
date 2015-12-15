package org.asdtm.goodweather;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class AboutActivity extends AppCompatActivity
{
    private TextView mAppVersion;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        mAppVersion = (TextView) findViewById(R.id.program_version_textView);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        AdView mAdView = (AdView) findViewById(R.id.adView);

        AdRequest mAdRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("BABC04D75F37693A3A243EA37AF6DE5F")
                .build();
        mAdView.loadAd(mAdRequest);

        this.setSupportActionBar(mToolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar()
                    .setHomeAsUpIndicator(getResources()
                                            .getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha, null));
        } else {
            getSupportActionBar()
                    .setHomeAsUpIndicator(getResources()
                                            .getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha));
        }
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        /* try {
            String versionName = getPackageManager()
                    .getPackageInfo(getPackageName(), 0)
                    .versionName;
            mAppVersion.setText(getResources().getText(R.string.version_label) + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } */
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(this) != null) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
        }

        return super.onOptionsItemSelected(item);
    }
}
