package org.asdtm.goodweather;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

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

        this.setSupportActionBar(mToolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar()
                    .setHomeAsUpIndicator(getResources()
                                            .getDrawable(R.drawable.abc_ic_ab_back_material, null));
        } else {
            getSupportActionBar()
                    .setHomeAsUpIndicator(getResources()
                                            .getDrawable(R.drawable.abc_ic_ab_back_material));
        }
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
