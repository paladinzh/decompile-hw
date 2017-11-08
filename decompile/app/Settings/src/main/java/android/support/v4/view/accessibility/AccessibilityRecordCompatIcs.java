package android.support.v4.view.accessibility;

import android.view.accessibility.AccessibilityRecord;
import java.util.List;

class AccessibilityRecordCompatIcs {
    AccessibilityRecordCompatIcs() {
    }

    public static List<CharSequence> getText(Object record) {
        return ((AccessibilityRecord) record).getText();
    }

    public static void setChecked(Object record, boolean isChecked) {
        ((AccessibilityRecord) record).setChecked(isChecked);
    }

    public static void setClassName(Object record, CharSequence className) {
        ((AccessibilityRecord) record).setClassName(className);
    }

    public static void setContentDescription(Object record, CharSequence contentDescription) {
        ((AccessibilityRecord) record).setContentDescription(contentDescription);
    }

    public static void setEnabled(Object record, boolean isEnabled) {
        ((AccessibilityRecord) record).setEnabled(isEnabled);
    }

    public static void setFromIndex(Object record, int fromIndex) {
        ((AccessibilityRecord) record).setFromIndex(fromIndex);
    }

    public static void setItemCount(Object record, int itemCount) {
        ((AccessibilityRecord) record).setItemCount(itemCount);
    }

    public static void setPassword(Object record, boolean isPassword) {
        ((AccessibilityRecord) record).setPassword(isPassword);
    }

    public static void setScrollX(Object record, int scrollX) {
        ((AccessibilityRecord) record).setScrollX(scrollX);
    }

    public static void setScrollY(Object record, int scrollY) {
        ((AccessibilityRecord) record).setScrollY(scrollY);
    }

    public static void setScrollable(Object record, boolean scrollable) {
        ((AccessibilityRecord) record).setScrollable(scrollable);
    }

    public static void setToIndex(Object record, int toIndex) {
        ((AccessibilityRecord) record).setToIndex(toIndex);
    }
}
