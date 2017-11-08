package com.android.systemui.statusbar.stack;

import android.view.View;

public interface ScrollContainer {
    void lockScrollTo(View view);

    void requestDisallowDismiss();

    void requestDisallowLongPress();
}
