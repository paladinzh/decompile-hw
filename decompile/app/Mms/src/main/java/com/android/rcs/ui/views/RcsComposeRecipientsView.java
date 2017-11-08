package com.android.rcs.ui.views;

import android.content.Intent;
import android.os.Handler;
import com.android.rcs.RcsCommonConfig;

public class RcsComposeRecipientsView {
    boolean isRcsOn = RcsCommonConfig.isRCSSwitchOn();

    public void setContactIntent(Intent intent) {
        if (!this.isRcsOn) {
        }
    }

    public boolean isRcsSwitchOn() {
        return this.isRcsOn;
    }

    public boolean postRcsDelayed(Handler h, Runnable r) {
        if (!this.isRcsOn || h == null) {
            return false;
        }
        h.postDelayed(r, 1000);
        return true;
    }
}
