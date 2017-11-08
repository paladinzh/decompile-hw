package com.android.systemui.statusbar.policy;

import android.view.View;

public interface ScrollAdapter {
    View getHostView();

    boolean isScrolledToTop();
}
