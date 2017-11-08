package com.android.systemui;

import android.util.EventLog;

public class EventLogTags {
    private EventLogTags() {
    }

    public static void writeSysuiStatusBarState(int state, int keyguardshowing, int keyguardoccluded, int bouncershowing, int secure, int currentlyinsecure) {
        EventLog.writeEvent(36004, new Object[]{Integer.valueOf(state), Integer.valueOf(keyguardshowing), Integer.valueOf(keyguardoccluded), Integer.valueOf(bouncershowing), Integer.valueOf(secure), Integer.valueOf(currentlyinsecure)});
    }

    public static void writeSysuiLockscreenGesture(int type, int lengthdp, int velocitydp) {
        EventLog.writeEvent(36021, new Object[]{Integer.valueOf(type), Integer.valueOf(lengthdp), Integer.valueOf(velocitydp)});
    }
}
