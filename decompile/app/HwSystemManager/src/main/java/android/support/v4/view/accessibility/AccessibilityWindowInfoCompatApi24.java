package android.support.v4.view.accessibility;

import android.view.accessibility.AccessibilityWindowInfo;

class AccessibilityWindowInfoCompatApi24 {
    AccessibilityWindowInfoCompatApi24() {
    }

    public static CharSequence getTitle(Object info) {
        return ((AccessibilityWindowInfo) info).getTitle();
    }

    public static Object getAnchor(Object info) {
        return ((AccessibilityWindowInfo) info).getAnchor();
    }
}
