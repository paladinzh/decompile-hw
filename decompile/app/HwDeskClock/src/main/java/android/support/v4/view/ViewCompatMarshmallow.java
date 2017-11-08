package android.support.v4.view;

import android.view.View;

class ViewCompatMarshmallow {
    ViewCompatMarshmallow() {
    }

    static void offsetTopAndBottom(View view, int offset) {
        view.offsetTopAndBottom(offset);
    }

    static void offsetLeftAndRight(View view, int offset) {
        view.offsetLeftAndRight(offset);
    }
}
