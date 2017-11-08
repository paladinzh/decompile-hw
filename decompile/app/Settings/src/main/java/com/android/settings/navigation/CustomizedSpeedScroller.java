package com.android.settings.navigation;

import android.content.Context;
import android.widget.Scroller;

public class CustomizedSpeedScroller extends Scroller {
    private int mDuration = 1000;

    public CustomizedSpeedScroller(Context context, int duration) {
        super(context);
        this.mDuration = duration;
    }

    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        super.startScroll(startX, startY, dx, dy, this.mDuration);
    }

    public void startScroll(int startX, int startY, int dx, int dy) {
        super.startScroll(startX, startY, dx, dy, this.mDuration);
    }
}
