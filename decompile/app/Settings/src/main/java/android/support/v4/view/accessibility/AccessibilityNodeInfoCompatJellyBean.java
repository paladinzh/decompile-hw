package android.support.v4.view.accessibility;

import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;

class AccessibilityNodeInfoCompatJellyBean {
    AccessibilityNodeInfoCompatJellyBean() {
    }

    public static void addChild(Object info, View child, int virtualDescendantId) {
        ((AccessibilityNodeInfo) info).addChild(child, virtualDescendantId);
    }

    public static void setSource(Object info, View root, int virtualDescendantId) {
        ((AccessibilityNodeInfo) info).setSource(root, virtualDescendantId);
    }

    public static boolean isVisibleToUser(Object info) {
        return ((AccessibilityNodeInfo) info).isVisibleToUser();
    }

    public static void setVisibleToUser(Object info, boolean visibleToUser) {
        ((AccessibilityNodeInfo) info).setVisibleToUser(visibleToUser);
    }

    public static boolean isAccessibilityFocused(Object info) {
        return ((AccessibilityNodeInfo) info).isAccessibilityFocused();
    }

    public static void setAccesibilityFocused(Object info, boolean focused) {
        ((AccessibilityNodeInfo) info).setAccessibilityFocused(focused);
    }
}
