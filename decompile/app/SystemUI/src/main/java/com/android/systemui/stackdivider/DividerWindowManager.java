package com.android.systemui.stackdivider;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

public class DividerWindowManager {
    private LayoutParams mLp;
    private View mView;
    private final WindowManager mWindowManager;

    public DividerWindowManager(Context ctx) {
        this.mWindowManager = (WindowManager) ctx.getSystemService(WindowManager.class);
    }

    public void add(View view, int width, int height) {
        this.mLp = new LayoutParams(width, height, 2034, 545521704, -3);
        this.mLp.setTitle("DockedStackDivider");
        LayoutParams layoutParams = this.mLp;
        layoutParams.privateFlags |= 64;
        view.setSystemUiVisibility(1792);
        this.mWindowManager.addView(view, this.mLp);
        this.mView = view;
    }

    public void remove() {
        if (this.mView != null) {
            this.mWindowManager.removeView(this.mView);
        }
        this.mView = null;
    }

    public void setSlippery(boolean slippery) {
        boolean changed = false;
        LayoutParams layoutParams;
        if (slippery && (this.mLp.flags & 536870912) == 0) {
            layoutParams = this.mLp;
            layoutParams.flags |= 536870912;
            changed = true;
        } else if (!(slippery || (this.mLp.flags & 536870912) == 0)) {
            layoutParams = this.mLp;
            layoutParams.flags &= -536870913;
            changed = true;
        }
        if (changed) {
            this.mWindowManager.updateViewLayout(this.mView, this.mLp);
        }
    }

    public void setTouchable(boolean touchable) {
        boolean changed = false;
        LayoutParams layoutParams;
        if (!touchable && (this.mLp.flags & 16) == 0) {
            layoutParams = this.mLp;
            layoutParams.flags |= 16;
            changed = true;
        } else if (touchable && (this.mLp.flags & 16) != 0) {
            layoutParams = this.mLp;
            layoutParams.flags &= -17;
            changed = true;
        }
        if (changed) {
            this.mWindowManager.updateViewLayout(this.mView, this.mLp);
        }
    }
}
