package com.huawei.powergenie.debugtest;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.content.Context;
import android.os.SystemProperties;
import android.widget.RemoteViews;
import com.huawei.powergenie.R;
import com.huawei.powergenie.api.Utils;

public final class DbgUtils {
    public static final boolean DBG_TIPS = SystemProperties.getBoolean("persist.sys.pg_tips_debug", false);
    public static final boolean DBG_USB = SystemProperties.getBoolean("persist.sys.pg_usb_debug", false);
    private static int NOTIFICATION_ID = 1000;
    public static final boolean TEST = SystemProperties.getBoolean("persist.sys.pg_test_debug", false);
    protected static final boolean UPLOAD_LOG_ON;
    private static Context mContext;

    static {
        boolean z = false;
        if ("1".equals(SystemProperties.get("persist.sys.powersystem.enable", "0"))) {
            z = true;
        }
        UPLOAD_LOG_ON = z;
    }

    public static void init(Context context) {
        mContext = context;
    }

    public static void sendNotification(CharSequence title, CharSequence details) {
        if (DBG_TIPS && mContext != null) {
            String strDate = Utils.formatDate(System.currentTimeMillis());
            RemoteViews notificationView = new RemoteViews(mContext.getPackageName(), R.layout.tips_notification_layout);
            notificationView.setTextViewText(R.id.now_date, strDate);
            notificationView.setTextViewText(R.id.details, title + " " + details);
            Notification notification = new Builder(mContext).setSmallIcon(17301642).setCustomContentView(notificationView).setAutoCancel(true).build();
            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService("notification");
            int i = NOTIFICATION_ID;
            NOTIFICATION_ID = i + 1;
            notificationManager.notify(i, notification);
        }
    }
}
