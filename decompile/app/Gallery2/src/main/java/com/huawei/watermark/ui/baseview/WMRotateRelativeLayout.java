package com.huawei.watermark.ui.baseview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;
import com.huawei.watermark.decoratorclass.WMLog;
import com.huawei.watermark.manager.parse.WMElement;
import com.huawei.watermark.ui.WMComponent;
import com.huawei.watermark.wmutil.WMAnimationUtil;
import com.huawei.watermark.wmutil.WMResourceUtil;

public class WMRotateRelativeLayout extends RelativeLayout implements WMRotatable {
    private long mAnimationEndTime = 0;
    private long mAnimationStartTime = 0;
    private boolean mClockwise = false;
    private int mCurrentDegree = 0;
    private Interpolator mInterpolator;
    private int mMeasureHeight = 0;
    private int mMeasureWidth = 0;
    private OnTouchListener mOnTouchListener;
    private Runnable mRotateRunnable = new Runnable() {
        public void run() {
            if (WMRotateRelativeLayout.this.mCurrentDegree == WMRotateRelativeLayout.this.mTargetDegree) {
                WMRotateRelativeLayout.this.setEnabled(true);
                return;
            }
            WMRotateRelativeLayout.this.setEnabled(false);
            long time = AnimationUtils.currentAnimationTimeMillis();
            if (time < WMRotateRelativeLayout.this.mAnimationEndTime) {
                long timeDuration = WMRotateRelativeLayout.this.mAnimationEndTime - WMRotateRelativeLayout.this.mAnimationStartTime;
                float deltaTime = ((float) timeDuration) * WMRotateRelativeLayout.this.mInterpolator.getInterpolation(((((float) (time - WMRotateRelativeLayout.this.mAnimationStartTime)) * WMElement.CAMERASIZEVALUE1B1) / ((float) timeDuration)) * WMElement.CAMERASIZEVALUE1B1);
                int -get6 = WMRotateRelativeLayout.this.mStartDegree;
                if (!WMRotateRelativeLayout.this.mClockwise) {
                    deltaTime = -deltaTime;
                }
                int degree = -get6 + ((int) ((214.28572f * deltaTime) / 1000.0f));
                WMRotateRelativeLayout.this.mCurrentDegree = degree >= 0 ? degree % 360 : (degree % 360) + 360;
            } else {
                WMRotateRelativeLayout.this.mCurrentDegree = WMRotateRelativeLayout.this.mTargetDegree;
            }
            WMRotateRelativeLayout.this.setRotation((float) (-WMRotateRelativeLayout.this.mCurrentDegree));
            WMRotateRelativeLayout.this.post(WMRotateRelativeLayout.this.mRotateRunnable);
        }
    };
    private int mStartDegree = 0;
    private int mTargetDegree = 0;

    public WMRotateRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mInterpolator = WMAnimationUtil.getInterpolator(context, WMResourceUtil.getAnimid(context, "wm_jar_cubic_bezier_interpolator_type_a"));
    }

    public void setOnTouchListener(OnTouchListener listener) {
        super.setOnTouchListener(listener);
        this.mOnTouchListener = listener;
    }

    public OnTouchListener getOnTouchListener() {
        return this.mOnTouchListener;
    }

    public void setOrientation(int degree, boolean animation) {
        boolean z = false;
        degree = degree >= 0 ? degree % 360 : (degree % 360) + 360;
        if (degree != this.mTargetDegree) {
            this.mTargetDegree = degree;
            if (animation) {
                this.mStartDegree = this.mCurrentDegree;
                this.mAnimationStartTime = AnimationUtils.currentAnimationTimeMillis();
                int diff = this.mTargetDegree - this.mCurrentDegree;
                if (diff < 0) {
                    diff += 360;
                }
                if (diff > 180) {
                    diff -= 360;
                }
                if (diff >= 0) {
                    z = true;
                }
                this.mClockwise = z;
                this.mAnimationEndTime = this.mAnimationStartTime + ((long) ((((float) Math.abs(diff)) * 1000.0f) / 214.28572f));
                post(this.mRotateRunnable);
            } else {
                this.mCurrentDegree = this.mTargetDegree;
                setRotation((float) (-this.mCurrentDegree));
            }
        }
    }

    public float getX() {
        switch (this.mCurrentDegree) {
            case WMComponent.ORI_90 /*90*/:
            case 270:
                return super.getX() + (((float) (getSeemWidth() - getSeemHeight())) / 2.0f);
            default:
                return super.getX();
        }
    }

    public float getY() {
        switch (this.mCurrentDegree) {
            case WMComponent.ORI_90 /*90*/:
            case 270:
                return super.getY() + (((float) (getSeemHeight() - getSeemWidth())) / 2.0f);
            default:
                return super.getY();
        }
    }

    public void setX(float x) {
        switch (this.mCurrentDegree) {
            case WMComponent.ORI_90 /*90*/:
            case 270:
                super.setX((((float) (getSeemHeight() - getSeemWidth())) / 2.0f) + x);
                return;
            default:
                super.setX(x);
                return;
        }
    }

    public void setY(float y) {
        switch (this.mCurrentDegree) {
            case WMComponent.ORI_90 /*90*/:
            case 270:
                super.setY(y - (((float) (getSeemHeight() - getSeemWidth())) / 2.0f));
                return;
            default:
                super.setY(y);
                return;
        }
    }

    public void setRotateLayoutParams(LayoutParams params) {
        if (params != null) {
            RelativeLayout.LayoutParams params_src = (RelativeLayout.LayoutParams) params;
            RelativeLayout.LayoutParams params_child = new RelativeLayout.LayoutParams(-2, -2);
            getChildWidthAndHeigth(params_src, params_child);
            View mView = findViewById(WMResourceUtil.getId(getContext(), "wm_base_relativelayout"));
            if (mView != null) {
                mView.setLayoutParams(params_child);
                RelativeLayout.LayoutParams params_rotatelayout = new RelativeLayout.LayoutParams(-2, -2);
                int[] rules = params_src.getRules();
                getRotateLayoutRulesParams(params_rotatelayout, rules);
                int w = mView.getWidth();
                int h = mView.getHeight();
                if ((this.mCurrentDegree == 90 || this.mCurrentDegree == 270) && (w == 0 || h == 0)) {
                    long time_start = System.currentTimeMillis();
                    measure(-2, -2);
                    WMLog.d("WMRotateRelativeLayout", "setRotateLayoutParams measure time = " + (System.currentTimeMillis() - time_start));
                    this.mMeasureWidth = getMeasuredWidth();
                    this.mMeasureHeight = getMeasuredHeight();
                    w = this.mMeasureWidth;
                    h = this.mMeasureHeight;
                }
                getRotateLayoutMarginParams(params_src, params_rotatelayout, rules, w, h);
                super.setLayoutParams(params_rotatelayout);
            }
        }
    }

    private void getRotateLayoutRulesParams(RelativeLayout.LayoutParams params_rotatelayout, int[] rules) {
        if (rules[12] != 0) {
            params_rotatelayout.addRule(12);
        }
        if (rules[10] != 0) {
            params_rotatelayout.addRule(10);
        }
        if (rules[9] != 0) {
            params_rotatelayout.addRule(9);
        }
        if (rules[11] != 0) {
            params_rotatelayout.addRule(11);
        }
        if (rules[13] != 0) {
            params_rotatelayout.addRule(13);
        }
        if (rules[15] != 0) {
            params_rotatelayout.addRule(15);
        }
        if (rules[14] != 0) {
            params_rotatelayout.addRule(14);
        }
    }

    private void getRotateLayoutMarginParams(RelativeLayout.LayoutParams params_src, RelativeLayout.LayoutParams params_rotatelayout, int[] rules, int w, int h) {
        switch (this.mCurrentDegree) {
            case WMComponent.ORI_90 /*90*/:
            case 270:
                if (rules[12] != 0) {
                    params_rotatelayout.bottomMargin = params_src.bottomMargin + ((w - h) / 2);
                }
                if (rules[11] != 0) {
                    params_rotatelayout.rightMargin = params_src.rightMargin + ((h - w) / 2);
                }
                if (rules[9] != 0) {
                    params_rotatelayout.leftMargin = params_src.leftMargin + ((h - w) / 2);
                }
                if (rules[10] != 0) {
                    params_rotatelayout.topMargin = params_src.topMargin + ((w - h) / 2);
                    return;
                }
                return;
            default:
                if (rules[12] != 0) {
                    params_rotatelayout.bottomMargin = params_src.bottomMargin;
                }
                if (rules[11] != 0) {
                    params_rotatelayout.rightMargin = params_src.rightMargin;
                }
                if (rules[9] != 0) {
                    params_rotatelayout.leftMargin = params_src.leftMargin;
                }
                if (rules[10] != 0) {
                    params_rotatelayout.topMargin = params_src.topMargin;
                    return;
                }
                return;
        }
    }

    private void getChildWidthAndHeigth(RelativeLayout.LayoutParams params_src, RelativeLayout.LayoutParams params_child) {
        switch (this.mCurrentDegree) {
            case WMComponent.ORI_90 /*90*/:
            case 270:
                params_child.width = params_src.height;
                params_child.height = params_src.width;
                return;
            default:
                params_child.width = params_src.width;
                params_child.height = params_src.height;
                return;
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        this.mMeasureWidth = getWidth();
        this.mMeasureHeight = getHeight();
    }

    private int getSeemWidth() {
        int w = getWidth();
        if (w == 0) {
            return this.mMeasureWidth;
        }
        return w;
    }

    private int getSeemHeight() {
        int w = getHeight();
        if (w == 0) {
            return this.mMeasureHeight;
        }
        return w;
    }
}
