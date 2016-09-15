package org.asdtm.goodweather.task;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.asdtm.goodweather.R;
import org.asdtm.goodweather.utils.ApiKeys;
import org.asdtm.goodweather.utils.AppPreference;
import org.asdtm.goodweather.utils.Constants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.asdtm.goodweather.task.TaskOutput.ParseResult;
import static org.asdtm.goodweather.task.TaskOutput.TaskResult;

public abstract class AsyncTaskBase extends AsyncTask<String, String, TaskOutput> {

    private static final String TAG = "AsyncTaskBase";

    private static final String APPID = ApiKeys.OPEN_WEATHER_MAP_API_KEY;

    private Context mContext;

    protected abstract ParseResult parseResponse(String response);

    protected abstract void updateUI();

    public AsyncTaskBase(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected TaskOutput doInBackground(String... strings) {
        TaskOutput taskOutput = new TaskOutput();

        String requestResult = "";
        HttpURLConnection connection = null;
        try {
            URL url = getUrl(strings[0], strings[1], strings[2], strings[3]);
            connection = (HttpURLConnection) url.openConnection();

            switch (connection.getResponseCode()) {
                case HttpURLConnection.HTTP_OK:
                    ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                    InputStream inputStream = connection.getInputStream();

                    int bytesRead;
                    byte[] buffer = new byte[1024];
                    while ((bytesRead = inputStream.read(buffer)) > 0) {
                        byteArray.write(buffer, 0, bytesRead);
                    }
                    byteArray.close();
                    requestResult = byteArray.toString();
                    AppPreference.saveLastUpdateTimeMillis(mContext);
                    taskOutput.taskResult = TaskResult.SUCCESS;

                    break;
                case HttpURLConnection.HTTP_MOVED_TEMP:
                    Log.e(TAG, "302 FOUND");
                    taskOutput.taskResult = TaskResult.ERROR;
                    break;
                default:
                    Log.e(TAG, "Bad Response");
                    taskOutput.taskResult = TaskResult.ERROR;
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + requestResult);
            taskOutput.taskResult = TaskResult.ERROR;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        if (TaskOutput.TaskResult.SUCCESS.equals(taskOutput.taskResult)) {
            taskOutput.parseResult = parseResponse(requestResult);
        }

        return taskOutput;
    }

    @Override
    protected void onPostExecute(TaskOutput result) {
        analysisTaskResult(result);
        updateUI();
    }

    private void analysisTaskResult(TaskOutput result) {
        String errorMessage = mContext.getString(R.string.toast_parse_error);
        switch (result.taskResult) {
            case SUCCESS:
                ParseResult parseResult = result.parseResult;
                if (ParseResult.PARSE_JSON_EXCEPTION.equals(parseResult)) {
                    Toast.makeText(mContext, errorMessage, Toast.LENGTH_SHORT).show();
                }
                break;
            case ERROR:
                Toast.makeText(mContext, errorMessage, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private URL getUrl(String lat, String lon, String units, String lang) throws IOException {
        String url = Uri.parse(Constants.WEATHER_ENDPOINT).buildUpon()
                        .appendQueryParameter("lat", lat)
                        .appendQueryParameter("lon", lon)
                        .appendQueryParameter("APPID", APPID)
                        .appendQueryParameter("units", units)
                        .appendQueryParameter("lang", lang)
                        .build()
                        .toString();
        return new URL(url);
    }
}
