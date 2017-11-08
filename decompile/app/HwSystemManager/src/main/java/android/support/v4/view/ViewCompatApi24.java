package android.support.v4.view;

import android.view.PointerIcon;
import android.view.View;

class ViewCompatApi24 {
    ViewCompatApi24() {
    }

    public static void setPointerIcon(View view, Object pointerIcon) {
        view.setPointerIcon((PointerIcon) pointerIcon);
    }
}
