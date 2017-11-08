package com.android.systemui.assist;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.media.AudioAttributes;
import android.media.AudioAttributes.Builder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.Vibrator;
import android.provider.Settings.System;
import android.util.AttributeSet;
import android.util.Log;
import android.view.IWindowManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.internal.policy.IKeyguardService;
import com.android.internal.policy.IKeyguardService.Stub;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.HwPhoneStatusBar;
import com.android.systemui.utils.CompatUtils;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.PhoneStatusBarUtils;
import com.android.systemui.utils.SystemUiUtil;
import com.android.systemui.utils.UserSwitchUtils;
import com.android.systemui.utils.analyze.BDReporter;

public class SearchPanelView extends FrameLayout {
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new Builder().setContentType(4).setUsage(13).build();
    private SearchPanelCircleView mCircle;
    private final Context mContext;
    private boolean mDraggedFarEnough;
    private boolean mDragging;
    protected IKeyguardService mKeyguardService;
    private boolean mLaunchPending;
    private boolean mLaunching;
    private ImageView mLogo;
    private View mScrim;
    private float mStartDrag;
    private float mStartTouch;
    private int mThreshold;

    public SearchPanelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchPanelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        this.mThreshold = context.getResources().getDimensionPixelSize(R.dimen.search_panel_threshold);
        this.mKeyguardService = Stub.asInterface(getApplicationWindowToken());
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mContext.getSystemService("layout_inflater");
        this.mCircle = (SearchPanelCircleView) findViewById(R.id.search_panel_circle);
        this.mLogo = (ImageView) findViewById(R.id.search_logo);
        this.mScrim = findViewById(R.id.search_panel_scrim);
    }

    public boolean dispatchHoverEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) {
            return true;
        }
        return super.dispatchHoverEvent(event);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mLaunching || this.mLaunchPending || this.mCircle == null) {
            return false;
        }
        switch (event.getActionMasked()) {
            case 0:
                float y = (!this.mCircle.isLand() || SystemUiUtil.isDefaultLandOrientationProduct()) ? event.getY() : event.getX();
                this.mStartTouch = y;
                this.mDragging = false;
                this.mDraggedFarEnough = false;
                this.mCircle.reset();
                break;
            case 1:
            case 3:
                if (this.mDraggedFarEnough) {
                    if (!this.mCircle.isAnimationRunning(true)) {
                        startExitAnimation();
                        break;
                    }
                    this.mLaunchPending = true;
                    this.mCircle.setAnimatingOut(true);
                    this.mCircle.performOnAnimationFinished(new Runnable() {
                        public void run() {
                            SearchPanelView.this.startExitAnimation();
                        }
                    });
                    break;
                }
                startAbortAnimation();
                break;
            case 2:
                float currentTouch = (!this.mCircle.isLand() || SystemUiUtil.isDefaultLandOrientationProduct()) ? event.getY() : event.getX();
                if (getVisibility() == 0 && !this.mDragging && (!this.mCircle.isAnimationRunning(true) || Math.abs(this.mStartTouch - currentTouch) > ((float) this.mThreshold))) {
                    this.mStartDrag = currentTouch;
                    this.mDragging = true;
                }
                if (this.mDragging) {
                    boolean z;
                    this.mCircle.setDragDistance(Math.max(this.mStartDrag - currentTouch, 0.0f));
                    if (Math.abs(this.mStartTouch - currentTouch) > ((float) this.mThreshold)) {
                        z = true;
                    } else {
                        z = false;
                    }
                    this.mDraggedFarEnough = z;
                    this.mCircle.setDraggedFarEnough(this.mDraggedFarEnough);
                    break;
                }
                break;
        }
        return true;
    }

    private void maybeSwapSearchIcon() {
        replaceDrawable(this.mLogo, new ComponentName("com.huawei.vassistant", "com.huawei.vassistant.ui.VAssistantActivity"), "com.android.systemui.action_assist_icon");
    }

    public void replaceDrawable(ImageView v, ComponentName component, String name) {
        if (component != null) {
            try {
                PackageManager packageManager = this.mContext.getPackageManager();
                Bundle metaData = packageManager.getActivityInfo(component, 128).metaData;
                if (metaData != null) {
                    int iconResId = metaData.getInt(name);
                    if (iconResId != 0) {
                        v.setImageDrawable(packageManager.getResourcesForActivity(component).getDrawable(iconResId));
                        return;
                    }
                }
            } catch (NameNotFoundException e) {
                Log.w("SearchPanelView", "Failed to swap drawable; " + component.flattenToShortString() + " not found", e);
            } catch (NotFoundException nfe) {
                Log.w("SearchPanelView", "Failed to swap drawable from " + component.flattenToShortString(), nfe);
            }
        }
        v.setImageResource(R.drawable.google);
    }

    private void vibrate() {
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void[] params) {
                Context context = SearchPanelView.this.getContext();
                if (System.getIntForUser(context.getContentResolver(), "haptic_feedback_enabled", 1, UserSwitchUtils.getCurrentUser()) != 0) {
                    ((Vibrator) context.getSystemService("vibrator")).vibrate((long) context.getResources().getInteger(R.integer.config_search_panel_view_vibration_duration), SearchPanelView.VIBRATION_ATTRIBUTES);
                }
                return null;
            }
        }.execute(new Void[0]);
    }

    public void show(boolean show, boolean animate) {
        HwLog.i("SearchPanelView", "show SearchPanel show = " + show);
        if (show) {
            maybeSwapSearchIcon();
            if (getVisibility() != 0) {
                setVisibility(0);
                vibrate();
                if (animate) {
                    startEnterAnimation();
                } else {
                    this.mScrim.setAlpha(1.0f);
                }
            }
            setFocusable(true);
            setFocusableInTouchMode(true);
            requestFocus();
        } else if (getVisibility() == 4) {
        } else {
            if (animate) {
                startAbortAnimation();
            } else {
                setVisibility(4);
            }
        }
    }

    private void startEnterAnimation() {
        this.mCircle.startEnterAnimation();
        this.mScrim.setAlpha(1.0f);
    }

    private void startExitAnimation() {
        HwLog.i("SearchPanelView", "startExitAnimation()");
        this.mLaunchPending = false;
        if (!this.mLaunching && getVisibility() == 0) {
            this.mLaunching = true;
            startHwAssistActivity();
            vibrate();
            this.mCircle.setAnimatingOut(true);
            this.mCircle.startExitAnimation(new Runnable() {
                public void run() {
                    SearchPanelView.this.mLaunching = false;
                    SearchPanelView.this.mCircle.setAnimatingOut(false);
                    SearchPanelView.this.setVisibility(4);
                }
            });
            this.mScrim.animate().alpha(0.0f).setDuration(300).setStartDelay(0).setInterpolator(PhoneStatusBarUtils.ALPHA_OUT);
        }
    }

    private void startAbortAnimation() {
        this.mCircle.startAbortAnimation(new Runnable() {
            public void run() {
                SearchPanelView.this.mCircle.setAnimatingOut(false);
                SearchPanelView.this.setVisibility(4);
            }
        });
        this.mCircle.setAnimatingOut(true);
        this.mScrim.setAlpha(0.0f);
    }

    public void setHorizontal(boolean horizontal) {
        this.mCircle.setHorizontal(horizontal);
    }

    public void startHwAssistActivity() {
        if (HwPhoneStatusBar.getInstance().isDeviceProvisioned()) {
            HwPhoneStatusBar.getInstance().animateCollapsePanels(1);
            if (isKeyguardShowing()) {
                CompatUtils.useKeyguardTouchDelegateMethod(this.mKeyguardService, "showAssistant");
            } else {
                try {
                    this.mContext.startActivityAsUser(new Intent("com.huawei.action.VOICE_ASSISTANT").setPackage("com.huawei.vassistant").setFlags(268435456), new UserHandle(UserSwitchUtils.getCurrentUser()));
                    BDReporter.c(getContext(), 10);
                } catch (ActivityNotFoundException e) {
                    HwLog.e("SearchPanelView", "ActivityNotFoundException");
                }
            }
        }
    }

    private boolean isKeyguardShowing() {
        boolean isKeyguardShowing = false;
        try {
            isKeyguardShowing = IWindowManager.Stub.asInterface(ServiceManager.getService("window")).isKeyguardLocked();
        } catch (RemoteException e) {
            HwLog.e("SearchPanelView", "RemoteException");
        }
        return isKeyguardShowing;
    }
}
