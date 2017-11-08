package com.huawei.keyguard.cover;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import com.huawei.keyguard.util.HwLog;

public class CoverHome extends RelativeLayout {
    private CoverScreen mCoverUnlockScreen;

    public CoverHome(Context context) {
        this(context, null);
    }

    public CoverHome(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        try {
            Drawable coverDrawable = CoverCfg.getCoverWallpaper();
            if (coverDrawable != null) {
                setBackground(coverDrawable);
            } else {
                setBackgroundResource(33751074);
            }
        } catch (Exception e) {
            HwLog.w("CoverHome", "framework resource not found");
            setBackgroundColor(17170444);
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mCoverUnlockScreen = null;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case 0:
                if (this.mCoverUnlockScreen != null) {
                    this.mCoverUnlockScreen.onGrabbedStateChange();
                    break;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public void setCallback(CoverScreen coverUnlockScreen) {
        this.mCoverUnlockScreen = coverUnlockScreen;
    }
}
