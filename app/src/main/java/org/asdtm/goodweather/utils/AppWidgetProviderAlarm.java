package org.asdtm.goodweather.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class AppWidgetProviderAlarm {

    private Context mContext;

    public AppWidgetProviderAlarm(Context context) {
        mContext = context;
    }

    public void setAlarm(Class<?> cls) {
        String updatePeriodStr = AppPreference.getWidgetUpdatePeriod(mContext);
        long updatePeriodMills = Utils.intervalMillisForAlarm(updatePeriodStr);
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                                         SystemClock.elapsedRealtime() + updatePeriodMills,
                                         updatePeriodMills,
                                         getPendingIntent(cls));
    }

    public void cancelAlarm(Class<?> cls) {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(getPendingIntent(cls));
        getPendingIntent(cls).cancel();
    }

    private PendingIntent getPendingIntent(Class<?> cls) {
        Intent forcedWidgetUpdateIntent = new Intent(mContext, cls);
        forcedWidgetUpdateIntent.setAction(Constants.ACTION_FORCED_APPWIDGET_UPDATE);
        return PendingIntent.getBroadcast(mContext,
                                          0,
                                          forcedWidgetUpdateIntent,
                                          PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
