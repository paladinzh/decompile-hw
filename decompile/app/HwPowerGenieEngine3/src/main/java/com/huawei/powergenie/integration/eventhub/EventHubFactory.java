package com.huawei.powergenie.integration.eventhub;

import android.content.Context;

public final class EventHubFactory {
    private static EventHub makeEventHubByDescriptor(Context context, String descriptor) {
        if ("msghub".equals(descriptor)) {
            return MsgHub.getInstance(context);
        }
        if ("hookhub".equals(descriptor)) {
            return HookHub.getInstance();
        }
        return null;
    }

    public static boolean startAllEventHubs(Context context, EventListener listener) {
        if (context == null || listener == null) {
            return false;
        }
        EventHub inst = makeEventHubByDescriptor(context, "msghub");
        inst.addEventListener(listener);
        inst.start();
        inst = makeEventHubByDescriptor(context, "hookhub");
        inst.addEventListener(listener);
        inst.start();
        return true;
    }
}
