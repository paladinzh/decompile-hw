package com.android.systemui.observer;

import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.Secure;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.UserSwitchUtils;

public class ObserverDoubleTap extends ObserverItem<Boolean> {
    boolean mClearNotification = false;

    public ObserverDoubleTap(Handler handler) {
        super(handler);
    }

    public Uri getUri() {
        return Secure.getUriFor("fp_show_notification");
    }

    public void onChange() {
        boolean z;
        if (Secure.getIntForUser(this.mContext.getContentResolver(), "fp_show_notification", 0, UserSwitchUtils.getCurrentUser()) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mClearNotification = z;
        HwLog.i("ObserverDoubleTap", "ClearNotification=" + this.mClearNotification + ", " + Secure.getIntForUser(this.mContext.getContentResolver(), "fp_show_notification", 0, UserSwitchUtils.getCurrentUser()));
    }

    public Boolean getValue() {
        return Boolean.valueOf(this.mClearNotification);
    }
}
