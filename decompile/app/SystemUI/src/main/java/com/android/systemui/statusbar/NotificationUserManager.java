package com.android.systemui.statusbar;

import android.content.Context;
import android.content.pm.UserInfo;
import android.util.SparseArray;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.UserSwitchUtils;

public class NotificationUserManager {
    private static NotificationUserManager sInstance = null;
    private Context mContext = null;
    private SparseArray<NotificationStateManager> mUsers = new SparseArray();

    public static synchronized NotificationUserManager getInstance(Context context) {
        NotificationUserManager notificationUserManager;
        synchronized (NotificationUserManager.class) {
            if (sInstance == null) {
                sInstance = new NotificationUserManager(context);
            }
            notificationUserManager = sInstance;
        }
        return notificationUserManager;
    }

    private NotificationUserManager(Context context) {
        this.mContext = context;
    }

    public void updateCurrentProfilesCache(SparseArray<UserInfo> currentProfiles) {
        HwLog.i("NotificationUserManager", "updateCurrentProfilesCache");
        clear();
        for (int i = 0; i < currentProfiles.size(); i++) {
            UserInfo userInfo = (UserInfo) currentProfiles.valueAt(i);
            this.mUsers.put(userInfo.id, new NotificationStateManager(this.mContext, userInfo.id));
            HwLog.i("NotificationUserManager", "add user:" + userInfo.id);
        }
    }

    public void clear() {
        for (int i = 0; i < this.mUsers.size(); i++) {
            ((NotificationStateManager) this.mUsers.valueAt(i)).clear();
            HwLog.i("NotificationUserManager", "clear user:" + this.mUsers.keyAt(i));
        }
        this.mUsers.clear();
    }

    public int getNotificationState(int userId, String packageName, String type) {
        if (userId < 0) {
            userId = UserSwitchUtils.getCurrentUser();
        }
        NotificationStateManager m = (NotificationStateManager) this.mUsers.get(userId);
        if (m == null) {
            return -1;
        }
        return m.getNotificationState(packageName, type);
    }

    public int getSoundVibrateState(int userId, String packageName) {
        if (userId < 0) {
            userId = UserSwitchUtils.getCurrentUser();
        }
        NotificationStateManager m = (NotificationStateManager) this.mUsers.get(userId);
        if (m == null) {
            return 3;
        }
        return m.getSoundVibrate(packageName);
    }

    public void setSoundVibrateState(int userId, String packageName, int state, int uid) {
        if (userId < 0) {
            userId = UserSwitchUtils.getCurrentUser();
        }
        NotificationStateManager m = (NotificationStateManager) this.mUsers.get(userId);
        if (m != null) {
            m.setSoundVibrate(packageName, state, uid);
        }
    }
}
