package com.huawei.systemmanager.power.notification;

import android.content.Context;
import com.google.common.collect.Sets;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Set;

public class NotificationListEditor {
    private static final String TAG = "NotificationListEditor";
    private Context mContext = null;
    private Set<Integer> mNotifyList = Sets.newHashSet();

    public NotificationListEditor(Context context, ArrayList<Integer> notifyList) {
        this.mNotifyList.addAll(notifyList);
        this.mContext = context;
    }

    public void removeNotifyList(int uid) {
        if (this.mNotifyList.contains(Integer.valueOf(uid))) {
            HwLog.d(TAG, "item == uid ");
            this.mNotifyList.remove(Integer.valueOf(uid));
            if (this.mNotifyList.size() == 0) {
                UserNotifier.destroyNotification(this.mContext);
            }
        }
    }
}
