package org.asdtm.goodweather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.asdtm.goodweather.service.NotificationService;
import org.asdtm.goodweather.utils.AppPreference;

public class StartupReceiver extends BroadcastReceiver {

    private static final String TAG = "StartupReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isNotificationEnabled = AppPreference.isNotificationEnabled(context);
        NotificationService.setNotificationServiceAlarm(context, isNotificationEnabled);
    }
}
