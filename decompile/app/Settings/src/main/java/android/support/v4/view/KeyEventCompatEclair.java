package android.support.v4.view;

import android.view.KeyEvent;

class KeyEventCompatEclair {
    KeyEventCompatEclair() {
    }

    public static void startTracking(KeyEvent event) {
        event.startTracking();
    }
}
