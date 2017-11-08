package com.huawei.keyguard.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.android.keyguard.R$dimen;
import com.android.keyguard.R$id;
import com.android.keyguard.R$string;
import com.huawei.hwtransition.control.LiftTransition;
import com.huawei.hwtransition.control.LiftTransition.LiftListener;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.KeyguardCfg;
import com.huawei.keyguard.monitor.HwLockScreenReporter;
import com.huawei.keyguard.theme.KeyguardTheme;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.HwUnlockUtils;
import com.huawei.openalliance.ad.inter.constant.EventType;
import java.util.HashSet;

public class CameraLayout extends RelativeLayout implements LiftListener {
    private int mDeltaY;
    private boolean mInit;
    private LiftTransition mLiftTransition;
    private int mOrientation;
    private HashSet<View> mTranslateViews;
    private UnderLayer mUnderLayer;

    public void setLift(float deltaY) {
        HwLog.d("CameraLayout", "setLift " + deltaY);
        if (this.mUnderLayer != null) {
            this.mUnderLayer.setClipY((int) deltaY);
        }
        int newDelta = (int) deltaY;
        if (this.mDeltaY != newDelta) {
            for (View v : this.mTranslateViews) {
                v.setTranslationY((float) ((int) deltaY));
            }
            this.mDeltaY = newDelta;
        }
    }

    public void addTransalteView(View view) {
        this.mTranslateViews.add(view);
    }

    public void setLift(float deltaX, float deltaY) {
    }

    public void onLiftAnimationEnd() {
        HwLog.d("CameraLayout", "onLiftAnimationEnd");
        boolean succGo = false;
        if (Math.abs(this.mDeltaY) > 100) {
            succGo = HwKeyguardUpdateMonitor.getInstance(this.mContext).transitionToCamera();
        }
        if (succGo) {
            HwLog.w("CameraLayout", "unlock to camera");
            HwUnlockUtils.vibrate(this.mContext);
            HwLockScreenReporter.reportPicInfoAdEvent(this.mContext, EventType.SHOWEND, 1002, 0);
            return;
        }
        HwLog.w("CameraLayout", "unlock to camera fail.");
        setLift(0.0f);
    }

    public void setLiftMode(int liftMode) {
    }

    public CameraLayout(Context context) {
        this(context, null);
    }

    public CameraLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mInit = false;
        this.mOrientation = -1;
        this.mTranslateViews = new HashSet();
    }

    protected void onAttachedToWindow() {
        HwLog.i("CameraLayout", "onAttachedToWindow ");
        if (KeyguardCfg.isExtremePowerSavingMode()) {
            setVisibility(8);
        }
        super.onAttachedToWindow();
    }

    public void setVisibility(int visibility) {
        boolean disabled = false;
        HwKeyguardUpdateMonitor updMonitor = HwKeyguardUpdateMonitor.getInstance(this.mContext);
        int style = KeyguardTheme.getInst().getLockStyle();
        if (visibility == 0) {
            if (style == 3 || style == 4) {
                disabled = true;
            } else if (style == 5) {
                disabled = true;
            }
            if (disabled || !updMonitor.isShowing()) {
                disabled = true;
            } else {
                disabled = updMonitor.isCameraDisable();
            }
            if (disabled) {
                HwLog.w("CameraLayout", "Cameralayout disabled. " + disabled + " " + updMonitor.isShowing() + "  " + style);
            }
            if (disabled) {
                visibility = 8;
            }
        }
        super.setVisibility(visibility);
        if (visibility == 0) {
            RelativeLayout container = (RelativeLayout) findViewById(R$id.camera_container);
            if (container != null) {
                LayoutParams pa = (LayoutParams) container.getLayoutParams();
                pa.height = getResources().getDimensionPixelSize(R$dimen.camera_container_width2);
                pa.width = getResources().getDimensionPixelSize(R$dimen.camera_container_width2);
                container.setLayoutParams(pa);
            }
            ImageView cameraImageView = (ImageView) findViewById(R$id.camera_imageview);
            if (cameraImageView != null) {
                cameraImageView.setContentDescription(getResources().getString(R$string.camera_app_accessibility_description));
            }
        }
    }

    protected void onDetachedFromWindow() {
        HwLog.i("CameraLayout", "onDetachedFromWindow");
        reset();
        if (this.mUnderLayer != null) {
            ViewParent parent = this.mUnderLayer.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(this.mUnderLayer);
            }
            this.mUnderLayer = null;
        }
        super.onDetachedFromWindow();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mLiftTransition = new LiftTransition(this.mContext, this);
    }

    public boolean onDragEvent(MotionEvent event) {
        if (!this.mInit) {
            this.mInit = true;
            int height = getRootView().getHeight();
            this.mLiftTransition.setLiftRange((float) (-height), (float) height);
            this.mLiftTransition.setHeight((float) height, 2);
            HwLog.d("CameraLayout", "init transition : " + height);
        }
        return this.mLiftTransition.onTouchEvent(event);
    }

    public void reset() {
        setLift(0.0f);
        if (this.mLiftTransition != null) {
            this.mLiftTransition.reset();
        }
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus) {
            reset();
        }
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    public boolean isInterestEvent(MotionEvent event) {
        boolean interest = KgViewUtils.isTouchInView(event, this, 10);
        int orientation = getContext().getResources().getConfiguration().orientation;
        if (interest) {
            ViewGroup root;
            if (this.mUnderLayer == null) {
                root = (ViewGroup) getRootView();
                if (root != null) {
                    this.mOrientation = orientation;
                    addUnderLayer(root);
                }
            } else if (orientation != this.mOrientation) {
                root = (ViewGroup) getRootView();
                if (root != null) {
                    View view = root.findViewWithTag("UnderLayer");
                    if (view != null) {
                        root.removeView(view);
                    }
                    this.mInit = false;
                    this.mOrientation = orientation;
                    addUnderLayer(root);
                }
            }
        }
        return interest;
    }

    private void addUnderLayer(ViewGroup root) {
        this.mUnderLayer = new UnderLayer(getContext());
        this.mUnderLayer.setTag("UnderLayer");
        root.addView(this.mUnderLayer, -1, -1);
    }
}
