package com.android.server.rms.handler;

import android.content.Context;

public class ResourceDispatcher {
    private static final boolean DEBUG = false;
    private static final String TAG = "RMS.ResourceDispatcher";

    public static HwSysResHandler dispath(int resourceType, Context context) {
        switch (resourceType) {
            case 19:
                return AppHandler.getInstance(context);
            case 20:
                return MemoryHandler.getInstance(context);
            default:
                return null;
        }
    }
}
