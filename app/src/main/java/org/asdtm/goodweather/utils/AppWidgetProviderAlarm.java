package org.asdtm.goodweather.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import org.asdtm.goodweather.service.LocationUpdateService;
import org.asdtm.goodweather.widget.LessWidgetProvider;
import org.asdtm.goodweather.widget.MoreWidgetProvider;

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
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
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
        if(AppPreference.isUpdateLocationEnabled(mContext)) {
            Intent intent = new Intent(mContext, LocationUpdateService.class);
            if (cls.getCanonicalName().equals(LessWidgetProvider.class.getCanonicalName())) {
                intent.putExtra("updateSource", "LESS_WIDGET");
            } else if (cls.getCanonicalName().equals(MoreWidgetProvider.class.getCanonicalName())) {
                intent.putExtra("updateSource", "MORE_WIDGET");
            } else {
                intent.putExtra("updateSource", "EXT_LOC_WIDGET");
            }
            return PendingIntent.getService(mContext,
                                            0,
                                            intent,
                                            PendingIntent.FLAG_CANCEL_CURRENT);
        } else {
            Intent intent = new Intent(mContext, cls);
            intent.setAction(Constants.ACTION_FORCED_APPWIDGET_UPDATE);
            return PendingIntent.getBroadcast(mContext,
                                              0,
                                              intent,
                                              PendingIntent.FLAG_CANCEL_CURRENT);
        }
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
