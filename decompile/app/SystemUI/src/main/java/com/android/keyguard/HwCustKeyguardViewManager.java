package com.android.keyguard;

import android.content.Context;
import android.view.MotionEvent;

public class HwCustKeyguardViewManager {
    public boolean getBTwoFingerDown() {
        return false;
    }

    public void setBTwoFingerDown(boolean value) {
    }

    public boolean getBCaught() {
        return false;
    }

    public void dealDispatchTouchEvent(MotionEvent ev, Context context) {
    }

    public void setValue(KeyguardSecurityModel securityModel, int maxVelocity) {
    }
}
