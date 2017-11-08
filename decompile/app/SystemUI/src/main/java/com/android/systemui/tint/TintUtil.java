package com.android.systemui.tint;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

class TintUtil {
    TintUtil() {
    }

    static boolean isAnyParentNodeInvisible(View view) {
        ViewParent parent = view.getParent();
        while (parent != null && (parent instanceof ViewGroup)) {
            if (((ViewGroup) parent).getVisibility() != 0) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }
}
