package android.support.v4.view;

import android.view.View;

class ViewCompatBase {
    ViewCompatBase() {
    }

    static boolean isAttachedToWindow(View view) {
        return view.getWindowToken() != null;
    }
}
