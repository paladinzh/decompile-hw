package com.android.server.policy;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Flog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.internal.app.AssistUtils;
import com.android.server.LocalServices;
import com.android.server.input.HwCircleAnimation;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.statusbar.StatusBarManagerInternal;
import java.lang.reflect.Method;

public class SearchPanelView extends FrameLayout {
    public static final Interpolator ALPHA_OUT = new PathInterpolator(0.0f, 0.0f, 0.8f, HwCircleAnimation.SMALL_ALPHA);
    private static final String ASSIST_ICON_METADATA_NAME = "com.android.systemui.action_assist_icon";
    private static final String FINGER_PRINT_ACTION_KEYEVENT = "com.android.huawei.FINGER_PRINT_ACTION_KEYEVENT";
    protected static final String Hw_SEARCH_NAME = "com.huawei.vassistant";
    public static final String INTENT_KEY = "keycode";
    private static final int KEYCODE_START_VASSISTANT = 9999;
    private static final int MSG_HIDE_SEARCH_VIEW = 2;
    private static final int MSG_MOVE_EVENT = 5;
    private static final int MSG_SHOW_SEARCH_VIEW = 1;
    private static final int MSG_START_ABORT_ANIMATION = 3;
    private static final int MSG_START_EXIT_ANIMATION = 4;
    private static final String TAG = SearchPanelView.class.getSimpleName();
    private static String VOICE_ASSISTANT_ACTION = "com.huawei.action.VOICE_ASSISTANT";
    private static final boolean mIsChina = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", AppHibernateCst.INVALID_PKG));
    private Boolean isLandScapeProduct;
    private boolean mAdded;
    private AssistUtils mAssistUtils;
    private SearchPanelCircleView mCircle;
    private Context mContext;
    private float[] mDownPoint;
    private boolean mDraggedFarEnough;
    private boolean mDragging;
    private float mEffctiveStartTouch;
    private Handler mHandler;
    private Boolean mHasHwAssist;
    private boolean mIsCollapseStatusBar;
    private boolean mLaunchPending;
    private boolean mLaunching;
    private ImageView mLogo;
    private int mNavigationBarHeight;
    private View mScrim;
    private boolean mShowing;
    private float mStartDrag;
    private float mStartTouch;
    private int mThreshold;

    public SearchPanelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchPanelView(Context context, AttributeSet attrs, int defStyle) {
        boolean z;
        super(context, attrs, defStyle);
        this.mHasHwAssist = null;
        this.mDownPoint = new float[2];
        if (SystemProperties.getInt("ro.panel.hw_orientation", 0) == 90) {
            z = true;
        } else {
            z = false;
        }
        this.isLandScapeProduct = Boolean.valueOf(z);
        this.mIsCollapseStatusBar = false;
        this.mContext = context;
        this.mThreshold = context.getResources().getDimensionPixelSize(34472153);
        this.mNavigationBarHeight = context.getResources().getDimensionPixelSize(17104920);
        this.mAssistUtils = new AssistUtils(context);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mCircle = (SearchPanelCircleView) findViewById(34603182);
        this.mLogo = (ImageView) findViewById(34603183);
        this.mScrim = findViewById(34603181);
        maybeSwapSearchIcon();
    }

    private ComponentName getAssistInfo() {
        if (this.mAssistUtils != null) {
            return this.mAssistUtils.getAssistComponentForUser(-2);
        }
        return null;
    }

    private void maybeSwapSearchIcon() {
        ComponentName componentName = new ComponentName(Hw_SEARCH_NAME, "com.huawei.vassistant.ui.VAssistantActivity");
        if (hasHwAssist()) {
            replaceDrawable(this.mLogo, componentName, ASSIST_ICON_METADATA_NAME);
            return;
        }
        Intent intent = ((SearchManager) this.mContext.getSystemService("search")).getAssistIntent(false);
        if (intent != null) {
            replaceDrawable(this.mLogo, intent.getComponent(), ASSIST_ICON_METADATA_NAME);
        } else {
            this.mLogo.setImageDrawable(null);
        }
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
                    }
                }
            } catch (NameNotFoundException e) {
                Log.w(TAG, "Failed to swap drawable; " + component.flattenToShortString() + " not found", e);
            } catch (NotFoundException nfe) {
                Log.w(TAG, "Failed to swap drawable from " + component.flattenToShortString(), nfe);
            }
        }
    }

    public boolean dispatchHoverEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) {
            return true;
        }
        return super.dispatchHoverEvent(event);
    }

    public boolean handleGesture(MotionEvent event) {
        if (isNotReady(event)) {
            return false;
        }
        switch (event.getActionMasked()) {
            case 0:
                if (this.isLandScapeProduct.booleanValue()) {
                    this.mStartTouch = event.getY();
                } else {
                    this.mStartTouch = this.mCircle.isLand() ? event.getX() : event.getY();
                }
                this.mEffctiveStartTouch = this.mCircle.isLand() ? event.getY() : event.getX();
                this.mDragging = false;
                this.mDraggedFarEnough = false;
                this.mCircle.reset();
                this.mDownPoint[0] = event.getX();
                this.mDownPoint[1] = event.getY();
                break;
            case 1:
            case 3:
                float lastTouch = this.isLandScapeProduct.booleanValue() ? event.getY() : this.mCircle.isLand() ? event.getX() : event.getY();
                Log.d(TAG, "lastTouch is:" + lastTouch);
                if (Math.abs(this.mStartTouch - lastTouch) <= ((float) this.mThreshold) || mIsChina || !hasHwAssist()) {
                    if (this.mDraggedFarEnough) {
                        processDraggedEvent();
                    } else {
                        abortAnimation();
                    }
                    this.mIsCollapseStatusBar = false;
                    break;
                }
                startVoiceAssist();
                return true;
                break;
            case 2:
                move(event);
                break;
        }
        return true;
    }

    private boolean isNotReady(MotionEvent event) {
        return this.mLaunching || this.mLaunchPending || this.mCircle == null || event.getPointerCount() > 1;
    }

    private void processDraggedEvent() {
        if (this.mCircle.isAnimationRunning(true)) {
            this.mLaunchPending = true;
            this.mCircle.setAnimatingOut(true);
            this.mCircle.performOnAnimationFinished(new Runnable() {
                public void run() {
                    SearchPanelView.this.exitAnimation();
                }
            });
            return;
        }
        exitAnimation();
    }

    private void onMove(MotionEvent event) {
        boolean z = true;
        if (event == null) {
            Log.w(TAG, "onMove return because the event is null");
        } else if (event.getPointerCount() == 0) {
            Log.w(TAG, "onMove return because the pointer count is 0, it will crash if go on.");
        } else {
            try {
                float currentTouch = this.isLandScapeProduct.booleanValue() ? event.getY() : this.mCircle.isLand() ? event.getX() : event.getY();
                if (Math.abs(this.mStartTouch - currentTouch) > ((float) this.mNavigationBarHeight) && !isShowing()) {
                    if (!this.mIsCollapseStatusBar) {
                        collapseStatusBar(this.mContext);
                        this.mIsCollapseStatusBar = true;
                    }
                    if (mIsChina) {
                        showSearchPanelView();
                    }
                }
                if (getVisibility() == 0 && !this.mDragging && (!this.mCircle.isAnimationRunning(true) || Math.abs(this.mStartTouch - currentTouch) > ((float) this.mThreshold))) {
                    this.mStartDrag = currentTouch;
                    this.mDragging = true;
                }
                if (this.mDragging) {
                    this.mCircle.setDragDistance(Math.max(this.mStartDrag - currentTouch, 0.0f));
                    if (Math.abs(this.mStartTouch - currentTouch) <= ((float) this.mThreshold)) {
                        z = false;
                    }
                    this.mDraggedFarEnough = z;
                    this.mCircle.setDraggedFarEnough(this.mDraggedFarEnough);
                }
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "onMove return because there is an IllegalArgumentException, " + e);
            }
        }
    }

    public void show(boolean show, boolean animate) {
        this.mShowing = show;
        if (show) {
            maybeSwapSearchIcon();
            if (getVisibility() != 0) {
                setVisibility(0);
                if (animate) {
                    startEnterAnimation();
                    notifyTrikeyEvent(KEYCODE_START_VASSISTANT);
                } else {
                    this.mScrim.setAlpha(HwCircleAnimation.SMALL_ALPHA);
                }
            }
            setFocusable(true);
            setFocusableInTouchMode(true);
            requestFocus();
        } else if (animate) {
            startAbortAnimation();
        } else {
            setVisibility(4);
        }
    }

    private void notifyTrikeyEvent(int keyCode) {
        Intent intent = new Intent(FINGER_PRINT_ACTION_KEYEVENT);
        intent.putExtra("keycode", keyCode);
        intent.setPackage("com.android.systemui");
        intent.addFlags(268435456);
        this.mContext.sendBroadcast(intent, "android.permission.STATUS_BAR");
    }

    private void startEnterAnimation() {
        this.mCircle.startEnterAnimation();
        this.mScrim.setAlpha(HwCircleAnimation.SMALL_ALPHA);
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

    private void startExitAnimation() {
        this.mLaunchPending = false;
        if (!this.mLaunching && getVisibility() == 0) {
            this.mLaunching = true;
            startVoiceAssist();
            this.mCircle.setAnimatingOut(true);
            this.mCircle.startExitAnimation(new Runnable() {
                public void run() {
                    SearchPanelView.this.mLaunching = false;
                    SearchPanelView.this.mCircle.setAnimatingOut(false);
                    SearchPanelView.this.setVisibility(4);
                }
            });
            this.mScrim.animate().alpha(0.0f).setDuration(300).setStartDelay(0).setInterpolator(ALPHA_OUT);
        }
    }

    private void startVoiceAssist() {
        try {
            Flog.bdReport(this.mContext, 11);
            ((StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class)).startAssist(new Bundle());
        } catch (Exception exp) {
            Log.e(TAG, "startVoiceAssist error:" + exp.getMessage());
        }
    }

    public void hide(boolean animate) {
        if (animate) {
            startAbortAnimation();
        } else {
            setVisibility(4);
        }
    }

    public boolean isShowing() {
        return getVisibility() == 0 && !this.mCircle.isAnimatingOut();
    }

    public void initUI(Looper looper) {
        initHandler(looper);
    }

    private void initHandler(Looper looper) {
        this.mHandler = new Handler(looper) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        SearchPanelView.this.show(true, true);
                        return;
                    case 2:
                        SearchPanelView.this.hide(true);
                        return;
                    case 3:
                        SearchPanelView.this.startAbortAnimation();
                        return;
                    case 4:
                        SearchPanelView.this.startExitAnimation();
                        return;
                    case 5:
                        SearchPanelView.this.onMove((MotionEvent) msg.obj);
                        return;
                    default:
                        return;
                }
            }
        };
    }

    public void showSearchPanelView() {
        if (hasHwAssist() && this.mHandler != null) {
            this.mHandler.sendEmptyMessage(1);
        }
    }

    public void hideSearchPanelView() {
        if (this.mHandler != null) {
            this.mHandler.sendEmptyMessage(2);
        }
    }

    public boolean isInContentArea(int x, int y) {
        return true;
    }

    public void abortAnimation() {
        if (this.mHandler != null) {
            this.mHandler.sendEmptyMessage(3);
        }
    }

    public void exitAnimation() {
        if (this.mHandler != null) {
            this.mHandler.sendEmptyMessage(4);
        }
    }

    public void move(MotionEvent event) {
        if (this.mHandler != null) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = 5;
            msg.obj = event;
            this.mHandler.sendMessage(msg);
        }
    }

    public static final void collapseStatusBar(Context context) {
        Object sbservice = context.getSystemService("statusbar");
        try {
            Method collapse;
            Class<?> statusBarManager = Class.forName("android.app.StatusBarManager");
            if (VERSION.SDK_INT >= 17) {
                collapse = statusBarManager.getMethod("collapsePanels", new Class[0]);
            } else {
                collapse = statusBarManager.getMethod("collapse", new Class[0]);
            }
            collapse.invoke(sbservice, new Object[0]);
        } catch (ClassNotFoundException ce) {
            Log.w(TAG, "Class not found for StatusBarManager:" + ce.getMessage());
        } catch (NoSuchMethodException me) {
            Log.w(TAG, "Method not found for collapse:" + me.getMessage());
        } catch (RuntimeException re) {
            re.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean hasHwAssist() {
        if (mIsChina) {
            if (this.mHasHwAssist == null) {
                try {
                    this.mContext.getPackageManager().getPackageInfo(Hw_SEARCH_NAME, 128);
                    this.mHasHwAssist = Boolean.valueOf(true);
                } catch (NameNotFoundException e) {
                    this.mHasHwAssist = Boolean.valueOf(false);
                } catch (Exception exp) {
                    this.mHasHwAssist = Boolean.valueOf(false);
                    exp.printStackTrace();
                }
            }
            return this.mHasHwAssist.booleanValue();
        } else if (getAssistInfo() != null) {
            return true;
        } else {
            Log.w(TAG, "startAssist::assistComponent is null, return");
            return false;
        }
    }
}
