package com.android.systemui.observer;

import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.Secure;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.UserSwitchUtils;
import fyusion.vislib.BuildConfig;

public class ObserverZoomGesture extends ObserverItem<Boolean> {
    private boolean mZoomGestureEnable;

    public ObserverZoomGesture(Handler handler) {
        super(handler);
    }

    public Uri getUri() {
        return Secure.getUriFor("accessibility_display_magnification_enabled");
    }

    public void onChange() {
        boolean z = true;
        if (Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_display_magnification_enabled", 0, UserSwitchUtils.getCurrentUser()) != 1) {
            z = false;
        }
        this.mZoomGestureEnable = z;
        HwLog.i(BuildConfig.FLAVOR, "getZoomGestureEnabled(), zoomGestureEnable = " + this.mZoomGestureEnable);
    }

    public Boolean getValue() {
        return Boolean.valueOf(this.mZoomGestureEnable);
    }
}
