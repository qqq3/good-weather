package org.asdtm.goodweather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.asdtm.goodweather.service.NotificationsService;
import org.asdtm.goodweather.utils.PrefKeys;

public class StartupReceiver extends BroadcastReceiver {

    private static final String TAG = "StartupReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        PrefKeys preference = new PrefKeys(context, null);
        boolean isNotificationEnabled = preference.isNotificationEnabled();
        NotificationsService.setNotificationServiceAlarm(context, isNotificationEnabled);
    }
}
