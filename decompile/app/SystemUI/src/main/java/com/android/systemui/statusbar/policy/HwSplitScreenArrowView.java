package com.android.systemui.statusbar.policy;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.systemui.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.statusbar.phone.HwPhoneStatusBar;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUiUtil;
import com.android.systemui.utils.analyze.BDReporter;

public class HwSplitScreenArrowView extends RelativeLayout {
    private boolean mAdded;
    private float mAnimStartPos;
    private ImageView mArrowIcon;
    private int mDistanceThreshold;
    private float[] mDownPoint = new float[2];
    private float[] mFingerPoint1 = new float[2];
    private float[] mFingerPoint2 = new float[2];
    private TextView mInfoText;
    private boolean mIsDisabled;
    private boolean mIsMoving;
    private boolean mIsScreenLarge;
    private boolean mIsshownToast;
    private boolean mLaunchSplitScreen;
    private int mNavBarHeight;
    private int mOrientation;
    private LayoutParams mParams;
    private Point mScreenDims = new Point();
    private WindowManager mWindowMgr;

    public HwSplitScreenArrowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mArrowIcon = (ImageView) findViewById(R.id.arrow_icon);
        this.mInfoText = (TextView) findViewById(R.id.prompt_info);
        Resources res = this.mContext.getResources();
        this.mOrientation = res.getConfiguration().orientation;
        this.mNavBarHeight = res.getDimensionPixelSize(17104920);
        this.mWindowMgr = (WindowManager) this.mContext.getSystemService("window");
        setWindowParam();
        this.mIsScreenLarge = isScreenLarge();
        if (this.mOrientation != 2 || this.mIsScreenLarge) {
            this.mDistanceThreshold = this.mScreenDims.y / 8;
        } else {
            this.mDistanceThreshold = this.mScreenDims.x / 8;
        }
    }

    private boolean isScreenLarge() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.mWindowMgr.getDefaultDisplay().getMetrics(displayMetrics);
        return ((float) (this.mScreenDims.x < this.mScreenDims.y ? this.mScreenDims.x : this.mScreenDims.y)) / displayMetrics.density >= 600.0f;
    }

    public void addViewToWindow() {
        try {
            if (!this.mAdded) {
                this.mWindowMgr.addView(this, this.mParams);
                this.mAdded = true;
            }
        } catch (RuntimeException e) {
            HwLog.e("HwSplitScreenArrowView", " from addViewToWindow() " + e.getMessage());
        }
    }

    public void removeViewToWindow() {
        try {
            if (this.mAdded) {
                this.mWindowMgr.removeView(this);
                this.mAdded = false;
            }
        } catch (RuntimeException e) {
            HwLog.e("HwSplitScreenArrowView", " from removeViewToWindow() " + e.getMessage());
        }
    }

    private void moveToPositionY(float aPosY) {
        if (aPosY > ((float) (this.mNavBarHeight * 4))) {
            this.mParams.y = ((int) aPosY) - (this.mNavBarHeight * 3);
            this.mWindowMgr.updateViewLayout(this, this.mParams);
        }
    }

    private void moveToPositionX(float aPosX) {
        if (aPosX > ((float) (this.mNavBarHeight * 4))) {
            this.mParams.x = ((int) aPosX) - (this.mNavBarHeight * 3);
            this.mWindowMgr.updateViewLayout(this, this.mParams);
        }
    }

    public void handleSplitScreenGesture(MotionEvent event) {
        if (event.getAction() == 0) {
            this.mArrowIcon.setVisibility(0);
            this.mDownPoint[0] = event.getX();
            this.mDownPoint[1] = event.getY();
        }
        if (event.getActionMasked() == 0) {
            this.mFingerPoint1[0] = event.getX(event.getActionIndex());
            this.mFingerPoint1[1] = event.getY(event.getActionIndex());
        }
        if (event.getActionMasked() == 5) {
            this.mFingerPoint2[0] = event.getX(event.getActionIndex());
            this.mFingerPoint2[1] = event.getY(event.getActionIndex());
        }
        if (event.getAction() == 2 && event.getPointerCount() == 2) {
            if (isInSlideRegion()) {
                if (!this.mIsMoving) {
                    if (HwPhoneStatusBar.getInstance().getNavigationBarView().isDockedStackExists()) {
                        this.mIsDisabled = true;
                    } else if (HwPhoneStatusBar.getInstance().isTopTaskApp()) {
                        this.mIsDisabled = false;
                    } else {
                        this.mIsDisabled = true;
                        SystemUiUtil.showToastForAllUser(this.mContext, R.string.split_app_finger_slide_message);
                    }
                    this.mInfoText.setText(R.string.split_screen_slide_help_text);
                    this.mIsMoving = true;
                }
                if (!this.mIsDisabled) {
                    float lDistance;
                    if (this.mIsScreenLarge || this.mOrientation != 2) {
                        lDistance = this.mDownPoint[1] - event.getY();
                    } else {
                        lDistance = this.mDownPoint[0] - event.getX();
                    }
                    if (lDistance >= ((float) this.mNavBarHeight)) {
                        if (isTopTaskSupportMultiWindow()) {
                            if (lDistance > ((float) this.mDistanceThreshold)) {
                                this.mInfoText.setVisibility(0);
                                this.mLaunchSplitScreen = true;
                            } else {
                                this.mInfoText.setVisibility(4);
                                this.mLaunchSplitScreen = false;
                            }
                            if (1 == this.mOrientation || !this.mIsScreenLarge) {
                                setVisibility(0);
                            }
                            if (this.mIsScreenLarge || this.mOrientation != 2) {
                                this.mAnimStartPos = event.getRawY();
                                moveToPositionY(this.mAnimStartPos);
                            } else {
                                this.mAnimStartPos = event.getRawX();
                                moveToPositionX(this.mAnimStartPos);
                            }
                        } else {
                            showToast();
                        }
                    }
                }
            }
        } else if (event.getAction() == 1) {
            if (getVisibility() == 0) {
                animateView(this.mAnimStartPos, this.mLaunchSplitScreen);
            }
            if (this.mLaunchSplitScreen) {
                BDReporter.c(getContext(), 335);
                HwPhoneStatusBar.getInstance().launchSplitScreenMode();
            }
            reset();
        } else if (event.getAction() == 3) {
            reset();
        }
    }

    public void reset() {
        this.mLaunchSplitScreen = false;
        this.mIsshownToast = false;
        this.mIsMoving = false;
        setVisibility(8);
    }

    private void setWindowParam() {
        this.mWindowMgr.getDefaultDisplay().getRealSize(this.mScreenDims);
        this.mParams = new LayoutParams(-1, -1, 2014, 16777736, -3);
        if (this.mOrientation == 2) {
            this.mParams.gravity = 48;
        } else {
            this.mParams.gravity = 1;
        }
        this.mParams.width = this.mScreenDims.x;
        this.mParams.height = this.mScreenDims.y;
    }

    private void showToast() {
        if (!this.mIsshownToast) {
            SystemUiUtil.showToastForAllUser(this.mContext, R.string.recents_incompatible_app_message);
            BDReporter.e(this.mContext, 336, "status : false");
            this.mIsshownToast = true;
        }
    }

    private boolean isInSlideRegion() {
        int canNotSlideAreaWidth = this.mContext.getResources().getDimensionPixelSize(R.dimen.fling_gesture_ignore_region);
        if (this.mIsScreenLarge || this.mOrientation != 2) {
            int minX = canNotSlideAreaWidth;
            int maxX = this.mScreenDims.x - canNotSlideAreaWidth;
            if (this.mFingerPoint1[0] < ((float) canNotSlideAreaWidth) || this.mFingerPoint1[0] > ((float) maxX) || this.mFingerPoint2[0] < ((float) canNotSlideAreaWidth) || this.mFingerPoint2[0] > ((float) maxX)) {
                return false;
            }
            return true;
        }
        int minY = canNotSlideAreaWidth;
        int maxY = this.mScreenDims.y - canNotSlideAreaWidth;
        if (this.mFingerPoint1[1] < ((float) canNotSlideAreaWidth) || this.mFingerPoint1[1] > ((float) maxY) || this.mFingerPoint2[1] < ((float) canNotSlideAreaWidth) || this.mFingerPoint2[1] > ((float) maxY)) {
            return false;
        }
        return true;
    }

    private void updateViewLayout() {
        try {
            this.mWindowMgr.updateViewLayout(this, this.mParams);
        } catch (IllegalArgumentException iex) {
            HwLog.e("HwSplitScreenArrowView", " onAnimationUpdate() " + iex.getMessage());
        }
    }

    private void animateView(float aPos, boolean islaunchingMultiWindow) {
        if (aPos <= 0.0f) {
            setVisibility(8);
            return;
        }
        float aMin = (float) (this.mNavBarHeight * 4);
        float aMax = (float) (this.mOrientation == 2 ? this.mScreenDims.x : this.mScreenDims.y);
        int dividerHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.split_screen_divider_height);
        if (islaunchingMultiWindow) {
            if (this.mOrientation == 2) {
                aMax = (((float) (this.mScreenDims.x - dividerHeight)) / 2.0f) + ((float) this.mNavBarHeight);
            } else {
                aMax = (((float) (this.mScreenDims.y - dividerHeight)) / 2.0f) + ((float) this.mNavBarHeight);
            }
        }
        if (aPos <= aMin) {
            aPos = aMin;
        }
        PropertyValuesHolder animValue = PropertyValuesHolder.ofFloat("value", new float[]{aPos, aMax});
        ValueAnimator animation = ValueAnimator.ofPropertyValuesHolder(new PropertyValuesHolder[]{animValue});
        int duration = (int) (400.0f * (Math.abs(aMax - aPos) / (aMax / 2.0f)));
        if (duration <= 200) {
            duration = 200;
        }
        animation.setDuration((long) duration);
        animation.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                if (animation != null && animation.getAnimatedValue("value") != null) {
                    float lValue = ((Float) animation.getAnimatedValue("value")).floatValue();
                    if (HwSplitScreenArrowView.this.mOrientation == 2) {
                        HwSplitScreenArrowView.this.mParams.x = ((int) lValue) - (HwSplitScreenArrowView.this.mNavBarHeight * 3);
                    } else {
                        HwSplitScreenArrowView.this.mParams.y = ((int) lValue) - (HwSplitScreenArrowView.this.mNavBarHeight * 3);
                    }
                    HwSplitScreenArrowView.this.updateViewLayout();
                }
            }
        });
        animation.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator animation) {
                HwSplitScreenArrowView.this.mArrowIcon.setVisibility(4);
                HwSplitScreenArrowView.this.mInfoText.setVisibility(4);
            }

            public void onAnimationRepeat(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
                HwSplitScreenArrowView.this.setVisibility(8);
            }

            public void onAnimationCancel(Animator animation) {
                HwSplitScreenArrowView.this.setVisibility(8);
            }
        });
        animation.start();
    }

    private boolean isTopTaskSupportMultiWindow() {
        SystemServicesProxy ssp = Recents.getSystemServices();
        RunningTaskInfo topTask = ssp.getRunningTask();
        boolean screenPinningActive = ssp.isScreenPinningActive();
        boolean isHomeStack = topTask != null ? SystemServicesProxy.isHomeStack(topTask.stackId) : false;
        if (topTask == null || isHomeStack || screenPinningActive || !topTask.isDockable) {
            return false;
        }
        return true;
    }
}
