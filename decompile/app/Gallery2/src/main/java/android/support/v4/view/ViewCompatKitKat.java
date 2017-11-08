package android.support.v4.view;

import android.view.View;

class ViewCompatKitKat {
    ViewCompatKitKat() {
    }

    public static boolean isAttachedToWindow(View view) {
        return view.isAttachedToWindow();
    }
}
