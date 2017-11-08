package com.huawei.systemmanager.cvaa;

import android.content.Context;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;

public class AccessibilityEventHelper {
    private static final String PACKAGE_NAME = "com.huawei.systemmanager";
    private static final String TAG = "AccessiblityEventHelper";

    public static AccessibilityManager getAccessibilityManager() {
        Context context = GlobalContext.getContext();
        if (context != null) {
            return (AccessibilityManager) context.getSystemService("accessibility");
        }
        return null;
    }

    public static void sendAccessibilityEvent(AccessibilityManager accessibilityManager, String msg) {
        if (TextUtils.isEmpty(msg)) {
            HwLog.e(TAG, "message is null or empty");
            return;
        }
        if (accessibilityManager != null && accessibilityManager.isEnabled()) {
            AccessibilityEvent event = AccessibilityEvent.obtain(32);
            event.setPackageName("com.huawei.systemmanager");
            event.getText().add(msg);
            event.setAddedCount(msg.length());
            accessibilityManager.sendAccessibilityEvent(event);
            try {
                event.recycle();
            } catch (Exception e) {
                HwLog.e(TAG, "event has been recycled");
            }
        }
    }

    public static void sendCheckboxAccessibilityEvent(boolean isChecked) {
        AccessibilityManager am = getAccessibilityManager();
        Context context = GlobalContext.getContext();
        if (context != null) {
            String msg = context.getString(R.string.checkbox_checked);
            if (!isChecked) {
                msg = context.getString(R.string.checkbox_not_checked);
            }
            sendAccessibilityEvent(am, msg);
        }
    }
}
