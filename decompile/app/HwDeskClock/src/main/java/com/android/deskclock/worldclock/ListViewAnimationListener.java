package com.android.deskclock.worldclock;

import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import com.android.deskclock.ViewHolder$WorldClockViewHolder;

class ListViewAnimationListener implements AnimationListener {
    private ViewHolder$WorldClockViewHolder mViewHolder;

    ListViewAnimationListener(ViewHolder$WorldClockViewHolder viewHolder) {
        this.mViewHolder = viewHolder;
    }

    public void onAnimationStart(Animation animation) {
        if (WorldClockPage.getmShowMode() == 0) {
            this.mViewHolder.mDigitalClock.setVisibility(0);
        } else {
            this.mViewHolder.mAnalogClock.setVisibility(0);
        }
    }

    public void onAnimationRepeat(Animation animation) {
    }

    public void onAnimationEnd(Animation animation) {
        WorldClockPage.setmInvalidateViewsReason(false);
        if (WorldClockPage.getmShowMode() == 0) {
            this.mViewHolder.mAnalogClock.setVisibility(8);
        } else {
            this.mViewHolder.mDigitalClock.setVisibility(4);
        }
    }
}
