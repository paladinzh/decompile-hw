package com.huawei.mms.ui;

import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import com.android.mms.MmsApp;

public class MmsClickListener implements OnClickListener, OnTouchListener {
    private static int DOUBLE_TAP_MIN_TIME = ViewConfiguration.getDoubleTapMinTime();
    private static int DOUBLE_TAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout();
    private static int sDoubleTapSlopSquare = ((int) Math.pow((double) ViewConfiguration.get(MmsApp.getApplication().getApplicationContext()).getScaledDoubleTapSlop(), 2.0d));
    private IMmsClickListener mClickListener;
    private int mDeltaPos = 0;
    private long mDownTime = 0;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 101:
                    if (MmsClickListener.this.mClickListener != null) {
                        MmsClickListener.this.mClickListener.onSingleClick(MmsClickListener.this.mView);
                        return;
                    }
                    return;
                default:
                    throw new RuntimeException("Unknown message " + msg);
            }
        }
    };
    private int mPrevPosX = -100;
    private int mPrevPosY = -100;
    private long mPrevUpTime = 0;
    private View mView;

    public interface IMmsClickListener {
        void onDoubleClick(View view);

        void onSingleClick(View view);
    }

    public MmsClickListener(IMmsClickListener doubleClickListener) {
        this.mClickListener = doubleClickListener;
    }

    public void setClickListener(View v) {
        this.mView = v;
        v.setOnClickListener(this);
        v.setOnTouchListener(this);
    }

    public void removeClickListener() {
        if (this.mView != null) {
            this.mView.setOnClickListener(null);
            this.mView.setOnTouchListener(null);
        }
    }

    public void onClick(View v) {
        boolean hadTapMessage = this.mHandler.hasMessages(101);
        if (hadTapMessage) {
            this.mHandler.removeMessages(101);
        }
        if (!hadTapMessage || this.mDeltaPos >= sDoubleTapSlopSquare) {
            if (this.mPrevUpTime - this.mDownTime < ((long) ViewConfiguration.getLongPressTimeout())) {
                this.mHandler.sendEmptyMessageDelayed(101, (long) DOUBLE_TAP_TIMEOUT);
            }
        } else if (this.mClickListener != null) {
            this.mClickListener.onDoubleClick(this.mView);
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (action == 0) {
            int posX = (int) event.getX();
            int posY = (int) event.getY();
            this.mDownTime = event.getDownTime();
            long deltaTime = this.mDownTime - this.mPrevUpTime;
            if (deltaTime >= ((long) DOUBLE_TAP_TIMEOUT) || deltaTime <= ((long) DOUBLE_TAP_MIN_TIME)) {
                this.mDeltaPos = sDoubleTapSlopSquare;
            } else {
                int deltaX = posX - this.mPrevPosX;
                int deltaY = posY - this.mPrevPosY;
                this.mDeltaPos = (deltaX * deltaX) + (deltaY * deltaY);
            }
            this.mPrevPosX = (int) event.getX();
            this.mPrevPosY = (int) event.getY();
        } else if (action == 1) {
            this.mPrevUpTime = event.getEventTime();
        }
        return false;
    }
}
