package com.android.pplusaudit2.AutoUpdateApk;

import android.annotation.TargetApi;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

/**
 * Created by ULTRABOOK on 5/16/2016.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NLService extends NotificationListenerService {

    private String TAG = this.getClass().getSimpleName();
    public static final String NOT_TAG = "com.chasetech.pcount.autoupdate.NOTIFICATION_LISTENER";
    public static final String NOT_POSTED = "POSTED";
    public static final String NOT_REMOVED = "REMOVED";
    public static final String NOT_EVENT_KEY = "not_key";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }


}
