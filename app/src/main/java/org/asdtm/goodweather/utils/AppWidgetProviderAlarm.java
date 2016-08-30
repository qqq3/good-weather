package org.asdtm.goodweather.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class AppWidgetProviderAlarm {

    private static final String TAG = "AppWidgetProviderAlarm";

    private Context mContext;
    private Class<?> mCls;

    public AppWidgetProviderAlarm(Context context, Class<?> cls) {
        this.mContext = context;
        this.mCls = cls;
    }

    public void setAlarm() {
        String updatePeriodStr = AppPreference.getWidgetUpdatePeriod(mContext);
        long updatePeriodMills = Utils.intervalMillisForAlarm(updatePeriodStr);
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                                         SystemClock.elapsedRealtime() + updatePeriodMills,
                                         updatePeriodMills,
                                         getPendingIntent(mCls));
    }

    public void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(getPendingIntent(mCls));
        getPendingIntent(mCls).cancel();
    }

    private PendingIntent getPendingIntent(Class<?> cls) {
        Intent intent = new Intent(mContext, cls);
        intent.setAction(Constants.ACTION_FORCED_APPWIDGET_UPDATE);
        return PendingIntent.getBroadcast(mContext,
                                          0,
                                          intent,
                                          PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public boolean isAlarmOff() {
        Intent intent = new Intent(mContext, mCls);
        intent.setAction(Constants.ACTION_FORCED_APPWIDGET_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,
                                                                 0,
                                                                 intent,
                                                                 PendingIntent.FLAG_NO_CREATE);
        return pendingIntent == null;
    }
}
