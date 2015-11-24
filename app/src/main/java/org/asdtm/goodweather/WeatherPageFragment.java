package org.asdtm.goodweather;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.concurrent.TimeUnit;

public class WeatherPageFragment extends Fragment
{
    private static final String TAG = "WeatherPageFragment";

    private TextView mTextView;
    private Button mSearchButton;
    private BackgroundLoadWeather mLoadWeather;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Save fragment
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_main, parent, false);

        mTextView = (TextView) v.findViewById(R.id.textView_label);

        mSearchButton = (Button) v.findViewById(R.id.buttonSearch);
        mSearchButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mLoadWeather = new BackgroundLoadWeather();
                mLoadWeather.execute();
            }
        });

        return v;
    }

    class BackgroundLoadWeather extends AsyncTask<String, Void, String>
    {
        private ProgressDialog mProgress;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            mProgress = new ProgressDialog(getActivity());
            mProgress.setMessage(getResources().getString(R.string.progress_dialog_label));
            mProgress.setIndeterminate(true);
            mProgress.setCancelable(true);
            mProgress.show();
        }

        @Override
        protected String doInBackground(String... params)
        {
            String result;
            try {
                result = new WeatherRequest()
                        .getUrl("http://api.openweathermap.org/data/2.5/weather?q=London&APPID=7b1eaeea7795f54d52027369812383d0");
                return result;
            } catch (IOException e)
            {
                Log.e(TAG, "Fail!!!");
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
            mProgress.dismiss();
            mTextView.setText(result);
        }
    }
}
