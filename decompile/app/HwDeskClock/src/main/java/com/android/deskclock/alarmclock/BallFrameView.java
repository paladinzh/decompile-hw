package com.android.deskclock.alarmclock;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import com.android.deskclock.DeskClockApplication;
import com.android.deskclock.R;
import com.android.deskclock.smartcover.HwCustCoverAdapter;
import com.android.util.ClockReporter;
import com.android.util.HwLog;
import com.android.util.Utils;
import com.huawei.cust.HwCustUtils;

public class BallFrameView extends FrameLayout {
    private int mAnimationHeight;
    private int mAnimationWidth;
    private BollView mBooView;
    private HwCustCoverAdapter mCover;
    private LayoutParams mFrameLayoutParams;
    private int mOffsetX;
    private float mStartX;
    private Handler mainHandler;

    public BallFrameView(Context context) {
        this(context, null);
    }

    public BallFrameView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BallFrameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mainHandler = null;
        this.mAnimationWidth = 1080;
        this.mStartX = 0.0f;
        this.mOffsetX = 0;
        this.mCover = (HwCustCoverAdapter) HwCustUtils.createObj(HwCustCoverAdapter.class, new Object[0]);
        this.mAnimationHeight = 210;
        this.mFrameLayoutParams = new LayoutParams(-1, -1);
        this.mBooView = (BollView) LayoutInflater.from(context).inflate(R.layout.boll_animation, this, true).findViewById(R.id.boll_ani);
        if (this.mCover != null && this.mCover.isEvaPortCover()) {
            this.mAnimationWidth = (int) (((float) this.mAnimationWidth) * 0.2f);
        } else if (this.mCover != null && this.mCover.isMTPortCover()) {
            this.mAnimationWidth = (int) (((float) this.mAnimationWidth) * 0.3f);
        } else if (Utils.isLandScreen(context)) {
            this.mAnimationWidth = (int) (((float) this.mAnimationWidth) * 0.29f);
        } else {
            this.mAnimationWidth = (int) (((float) this.mAnimationWidth) * 0.4f);
        }
    }

    public void startTextViewAnimal() {
        if (this.mBooView != null) {
            HwLog.i("BallFrameView", "startTextViewAnimal");
            this.mBooView.startAnim();
        }
    }

    public void stopTextViewAnimal() {
        if (this.mBooView != null) {
            HwLog.i("BallFrameView", "stopTextViewAnimal");
            this.mBooView.endAnim();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getRawX();
        float eventY = event.getY();
        if (this.mBooView != null) {
            this.mBooView.handleTouchEvent(event);
        }
        switch (event.getAction()) {
            case 0:
                this.mStartX = eventX;
                stopTextViewAnimal();
                break;
            case 1:
                handleUpEvent(eventX, eventY);
                break;
            case 2:
                this.mOffsetX = (int) (eventX - this.mStartX);
                updateStart(this.mOffsetX, 0);
                break;
            case 3:
                HwLog.i("BallFrameView", "receiver touch event = ACTION_CANCEL");
                break;
        }
        return true;
    }

    private void handleUpEvent(float eventX, float eventY) {
        this.mOffsetX = (int) (eventX - this.mStartX);
        if (Math.abs(this.mOffsetX) > this.mAnimationWidth) {
            this.mainHandler.obtainMessage(2).sendToTarget();
            ClockReporter.reportEventMessage(getContext(), 92, "");
            this.mBooView.setVisibility(4);
            return;
        }
        HwLog.i("BallFrameView", "the distance of moving is too short");
        updateStart(0, 0);
        restoreAnimal();
    }

    public void restoreAnimal() {
        HwLog.i("BallFrameView", "restoreAnimal");
        postDelayed(new Runnable() {
            public void run() {
                BallFrameView.this.startTextViewAnimal();
            }
        }, 200);
    }

    private void updateStart(int x, int y) {
        if (x > 0) {
            this.mFrameLayoutParams.setMargins(0, y, -x, 0);
        } else {
            this.mFrameLayoutParams.setMargins(x, y, 0, 0);
        }
        this.mBooView.setLayoutParams(this.mFrameLayoutParams);
    }

    public void setCoverViewWidth(int animationWidth) {
        this.mAnimationWidth = animationWidth;
        if (this.mCover != null && this.mCover.isEvaPortCover()) {
            this.mAnimationWidth = (int) (((float) this.mAnimationWidth) * 0.2f);
        } else if (this.mCover != null && this.mCover.isMTPortCover()) {
            this.mAnimationWidth = (int) (((float) this.mAnimationWidth) * 0.3f);
        } else if (Utils.isLandScreen(DeskClockApplication.getDeskClockApplication())) {
            this.mAnimationWidth = (int) (((float) this.mAnimationWidth) * 0.29f);
        } else {
            this.mAnimationWidth = (int) (((float) this.mAnimationWidth) * 0.4f);
        }
    }

    public void setMainHandler(Handler handler) {
        this.mainHandler = handler;
    }
}
