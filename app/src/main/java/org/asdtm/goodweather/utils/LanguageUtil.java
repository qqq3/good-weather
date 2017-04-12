package org.asdtm.goodweather.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;

import java.util.Locale;

public class LanguageUtil {

    private static final String TAG = LanguageUtil.class.getSimpleName();

    @TargetApi(17)
    @SuppressWarnings("deprecation")
    public static void setLanguage(final ContextWrapper contextWrapper, String locale) {
        Locale sLocale;
        if (TextUtils.isEmpty(locale)) {
            sLocale = Locale.getDefault();
        } else {
            String[] localeParts = locale.split("_");
            if (localeParts.length > 1) {
                sLocale = new Locale(localeParts[0], localeParts[1]);
            } else {
                sLocale = new Locale(locale);
            }
        }

        Resources resources = contextWrapper.getBaseContext().getResources();
        Configuration configuration = resources.getConfiguration();
        Locale.setDefault(sLocale);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(sLocale);
        } else {
            configuration.locale = sLocale;
        }

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

    public static void forceChangeLanguage(Activity activity) {
        Intent intent = activity.getIntent();
        if (intent == null) {
            return;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.finish();
        activity.overridePendingTransition(0, 15);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

    public static String getLanguageName(String locale) {
        if (TextUtils.isEmpty(locale)) {
            locale = Locale.getDefault().toString();
        }
        if (locale.contains("_")) {
            return locale.split("_")[0];
        }
        return locale;
    }
}
