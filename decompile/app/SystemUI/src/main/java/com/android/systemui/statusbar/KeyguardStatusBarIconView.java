package com.android.systemui.statusbar;

import android.app.Notification;
import android.content.Context;
import android.graphics.ColorFilter;
import android.util.AttributeSet;
import android.util.Log;
import com.android.systemui.utils.HwLog;

public class KeyguardStatusBarIconView extends StatusBarIconView {
    public KeyguardStatusBarIconView(Context context, String slot, Notification notification) {
        super(context, slot, notification);
    }

    public KeyguardStatusBarIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void updateTint() {
    }

    public void setIsResever(boolean isResever) {
        if (DEBUG) {
            Log.d("KeyguardStatusBarIconView", "setIsResever:" + isResever + " " + this);
        }
        this.mIsResever = isResever;
    }

    public void setTint() {
    }

    public void setColorFilter(ColorFilter cf) {
        int tint = 1;
        if (this.mNotification != null) {
            tint = this.mNotification.extras.getInt("hw_small_icon_tint", 0);
            HwLog.i("KeyguardStatusBarIconView", "setColorFilter: tint=" + tint);
        }
        if (tint != 0) {
            super.setColorFilter(cf);
        }
    }

    protected void maybeUpdateIconScale() {
    }

    protected void updateIconScale() {
    }
}
