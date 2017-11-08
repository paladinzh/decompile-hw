package com.android.contacts.util;

import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

public class SchedulingUtils {
    public static void doAfterLayout(final View view, final Runnable runnable) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                runnable.run();
            }
        });
    }
}
