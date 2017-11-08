package com.huawei.keyguard.cover.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.huawei.keyguard.cover.CoverViewLoader;
import com.huawei.keyguard.util.HwLog;

public class CoverDigitalClock extends RelativeLayout {
    private View mCoverDigitalClockView = CoverViewLoader.createView(getContext(), "com.android.deskclock", "cover_digital_clock");
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    CoverDigitalClock.this.invalidate();
                    if (CoverDigitalClock.this.mHandler.hasMessages(1)) {
                        CoverDigitalClock.this.mHandler.removeMessages(1);
                    }
                    CoverDigitalClock.this.mHandler.sendEmptyMessageDelayed(1, 17);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public CoverDigitalClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (this.mCoverDigitalClockView != null) {
            addView(this.mCoverDigitalClockView);
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mCoverDigitalClockView == null) {
            HwLog.w("CoverDigitalClock", "onAttachedToWindow, mCoverDigitalClockView is null");
            return;
        }
        View v = this.mCoverDigitalClockView.findViewWithTag("full_time");
        if (v instanceof TextView) {
            TextView fullTime = (TextView) v;
            Typeface robotolight = Typeface.create("sans-serif-light", 0);
            if (robotolight != null) {
                fullTime.setTypeface(robotolight);
            }
        }
        this.mHandler.sendEmptyMessageDelayed(1, 17);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mCoverDigitalClockView = null;
        if (this.mHandler.hasMessages(1)) {
            this.mHandler.removeMessages(1);
        }
    }
}
