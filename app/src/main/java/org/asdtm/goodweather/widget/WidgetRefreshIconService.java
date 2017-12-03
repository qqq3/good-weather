package org.asdtm.goodweather.widget;

import android.app.IntentService;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.PowerManager;
import android.widget.RemoteViews;

import org.asdtm.goodweather.R;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.asdtm.goodweather.utils.LogToFile.appendLog;

public class WidgetRefreshIconService extends IntentService {

    private static final String TAG = "WidgetRefreshIconService";

    private static long ROTATE_UPDATE_ICON_MILIS = 100;

    private final int[] refreshIcons = new int[8];
    private volatile int currentRotationIndex;
    public volatile static boolean isRotationActive = false;

    private final Map<ComponentName, Integer> widgetTypes = new HashMap<>();

    private PowerManager powerManager;

    public WidgetRefreshIconService() {
        super(TAG);
        refreshIcons[0] = R.drawable.ic_refresh_white_18dp;
        refreshIcons[1] = R.drawable.ic_refresh_white_18dp_1;
        refreshIcons[2] = R.drawable.ic_refresh_white_18dp_2;
        refreshIcons[3] = R.drawable.ic_refresh_white_18dp_3;
        refreshIcons[4] = R.drawable.ic_refresh_white_18dp_4;
        refreshIcons[5] = R.drawable.ic_refresh_white_18dp_5;
        refreshIcons[6] = R.drawable.ic_refresh_white_18dp_6;
        refreshIcons[7] = R.drawable.ic_refresh_white_18dp_7;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        widgetTypes.put(new ComponentName(this, ExtLocationWidgetProvider.class), R.layout.widget_ext_loc_3x3);
        widgetTypes.put(new ComponentName(this, MoreWidgetProvider.class), R.layout.widget_more_3x3);
        widgetTypes.put(new ComponentName(this, LessWidgetProvider.class), R.layout.widget_less_3x1);
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        IntentFilter filterScreenOn = new IntentFilter(Intent.ACTION_SCREEN_ON);
        getApplication().registerReceiver(screenOnReceiver, filterScreenOn);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getAction() == null) {
            return;
        }
        String action = intent.getAction();
        switch (action) {
            case "android.intent.action.START_ROTATING_UPDATE":
                startRotatingUpdateIcon();
                break;
            case "android.intent.action.STOP_ROTATING_UPDATE":
                stopRotatingUpdateIcon();
        }
    }

    private void startRotatingUpdateIcon() {
        if (isRotationActive || timerRotateIconHandler.hasMessages(0)) {
            return;
        }
        isRotationActive = true;
        currentRotationIndex = 0;
        rotateRefreshButtonOneStep();
        timerRotateIconHandler.postDelayed(timerRotateIconRunnable, ROTATE_UPDATE_ICON_MILIS);
    }

    private void stopRotatingUpdateIcon() {
        isRotationActive = false;
        timerRotateIconHandler.removeCallbacksAndMessages(null);
    }

    public boolean isRotationActive() {
        return isRotationActive;
    }

    private void rotateRefreshButtonOneStep() {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
        for (ComponentName componentName: widgetTypes.keySet()) {
            RemoteViews rv = new RemoteViews(this.getPackageName(), widgetTypes.get(componentName));

            int[] widgetIds = widgetManager.getAppWidgetIds(componentName);
            for (int appWidgetId : widgetIds) {
                rv.setImageViewResource(R.id.widget_button_refresh, refreshIcons[currentRotationIndex]);
                widgetManager.partiallyUpdateAppWidget(appWidgetId, rv);
            }
        }
        currentRotationIndex++;
        if (currentRotationIndex >= refreshIcons.length) {
            currentRotationIndex = 0;
        }
    }

    Handler timerRotateIconHandler = new Handler();
    Runnable timerRotateIconRunnable = new Runnable() {

        @Override
        public void run() {
            if (!powerManager.isScreenOn() || !isRotationActive() || timerRotateIconHandler.hasMessages(0)) {
                return;
            }
            rotateRefreshButtonOneStep();
            timerRotateIconHandler.postDelayed(timerRotateIconRunnable, ROTATE_UPDATE_ICON_MILIS);
        }


    };

    private BroadcastReceiver screenOnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isRotationActive() || timerRotateIconHandler.hasMessages(0)) {
                return;
            }
            rotateRefreshButtonOneStep();
            timerRotateIconHandler.postDelayed(timerRotateIconRunnable, ROTATE_UPDATE_ICON_MILIS);
        }
    };
}
