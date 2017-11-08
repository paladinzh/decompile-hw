package com.huawei.keyguard.view.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;

public class ArrowLayout extends LinearLayout {
    private boolean mAnimLTR = false;
    private Animation[] mArrowAnimation;
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            int sz = ArrowLayout.this.getChildCount();
            switch (msg.what) {
                case 1:
                    if (msg.arg1 >= sz) {
                        msg.arg1 = 0;
                    }
                    int idx = ArrowLayout.this.mAnimLTR ? msg.arg1 : (sz - 1) - msg.arg1;
                    ArrowLayout.this.getChildAt(idx).startAnimation(ArrowLayout.this.mArrowAnimation[idx]);
                    sendMessageDelayed(ArrowLayout.this.mHandler.obtainMessage(1, msg.arg1 + 1, 0), (long) (msg.arg1 == sz + -1 ? 1000 : 1000 / sz));
                    break;
                case 2:
                    for (int i = 0; i < sz; i++) {
                        View v = ArrowLayout.this.getChildAt(i);
                        v.setVisibility(4);
                        v.clearAnimation();
                    }
                    removeMessages(1);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public ArrowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        if ("right".equalsIgnoreCase(getTag().toString())) {
            this.mAnimLTR = true;
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        int sz = getChildCount();
        this.mArrowAnimation = new Animation[sz];
        for (int i = 0; i < sz; i++) {
            int idx;
            if (this.mAnimLTR) {
                idx = i;
            } else {
                idx = (sz - 1) - i;
            }
            getChildAt(idx).setVisibility(4);
            this.mArrowAnimation[idx] = new AlphaAnimation(1.0f, 0.2f);
            this.mArrowAnimation[idx].setInterpolator(new AccelerateInterpolator());
            this.mArrowAnimation[idx].setDuration(1000);
            this.mArrowAnimation[idx].setFillAfter(true);
        }
    }

    public void stopAnimation() {
        if (getVisibility() != 8) {
            this.mHandler.sendEmptyMessage(2);
        }
    }
}
