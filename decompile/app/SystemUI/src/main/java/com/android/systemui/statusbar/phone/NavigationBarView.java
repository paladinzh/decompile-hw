package com.android.systemui.statusbar.phone;

import android.animation.LayoutTransition;
import android.animation.LayoutTransition.TransitionListener;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.IDockedStackListener.Stub;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerGlobal;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import com.android.systemui.R;
import com.android.systemui.RecentsComponent;
import com.android.systemui.compat.ActivityInfoWrapper;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.policy.DeadZone;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.huawei.cust.HwCustUtils;
import fyusion.vislib.BuildConfig;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class NavigationBarView extends LinearLayout {
    private Drawable mBackAltCarModeIcon;
    private Drawable mBackAltIcon;
    private Drawable mBackAltLandCarModeIcon;
    private Drawable mBackAltLandIcon;
    private Drawable mBackCarModeIcon;
    private Drawable mBackIcon;
    private Drawable mBackLandCarModeIcon;
    private Drawable mBackLandIcon;
    private final NavigationBarTransitions mBarTransitions;
    protected final SparseArray<ButtonDispatcher> mButtonDisatchers = new SparseArray();
    private boolean mCarMode = false;
    private Configuration mConfiguration;
    View mCurrentView = null;
    HwCustNavigationBarView mCust = null;
    private DeadZone mDeadZone;
    int mDisabledFlags = 0;
    final Display mDisplay;
    private Drawable mDockedIcon;
    private boolean mDockedStackExists;
    private NavigationBarGestureHelper mGestureHelper;
    private H mHandler = new H();
    private Drawable mHomeCarModeIcon;
    private Drawable mHomeDefaultIcon;
    private Drawable mImeIcon;
    private final OnClickListener mImeSwitcherClickListener = new OnClickListener() {
        public void onClick(View view) {
            ((InputMethodManager) NavigationBarView.this.mContext.getSystemService(InputMethodManager.class)).showInputMethodPicker(true);
        }
    };
    private Configuration mLastConfig = new Configuration();
    private boolean mLayoutTransitionsEnabled = false;
    private Drawable mMenuIcon;
    int mNavigationIconHints = 0;
    private OnVerticalChangedListener mOnVerticalChangedListener;
    private Drawable mRecentIcon;
    View[] mRotatedViews = new View[4];
    boolean mScreenOn;
    boolean mShowMenu;
    private final NavTransitionListener mTransitionListener = new NavTransitionListener();
    boolean mVertical;
    private boolean mWakeAndUnlocking;

    private class H extends Handler {
        private H() {
        }

        public void handleMessage(Message m) {
            switch (m.what) {
                case 8686:
                    String how = BuildConfig.FLAVOR + m.obj;
                    int w = NavigationBarView.this.getWidth();
                    int h = NavigationBarView.this.getHeight();
                    int vw = NavigationBarView.this.getCurrentView().getWidth();
                    if (h != NavigationBarView.this.getCurrentView().getHeight() || w != vw) {
                        Log.w("PhoneStatusBar/NavigationBarView", String.format("*** Invalid layout in navigation bar (%s this=%dx%d cur=%dx%d)", new Object[]{how, Integer.valueOf(w), Integer.valueOf(h), Integer.valueOf(vw), Integer.valueOf(vh)}));
                        NavigationBarView.this.requestLayout();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private class NavTransitionListener implements TransitionListener {
        private boolean mBackTransitioning;
        private long mDuration;
        private boolean mHomeAppearing;
        private TimeInterpolator mInterpolator;
        private long mStartDelay;

        private NavTransitionListener() {
        }

        public void startTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
            if (view.getId() == R.id.back) {
                this.mBackTransitioning = true;
            } else if (view.getId() == R.id.home && transitionType == 2) {
                this.mHomeAppearing = true;
                this.mStartDelay = transition.getStartDelay(transitionType);
                this.mDuration = transition.getDuration(transitionType);
                this.mInterpolator = transition.getInterpolator(transitionType);
            }
        }

        public void endTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
            if (view.getId() == R.id.back) {
                this.mBackTransitioning = false;
            } else if (view.getId() == R.id.home && transitionType == 2) {
                this.mHomeAppearing = false;
            }
        }

        public void onBackAltCleared() {
            ButtonDispatcher backButton = NavigationBarView.this.getBackButton();
            if (!this.mBackTransitioning && backButton.getVisibility() == 0 && this.mHomeAppearing && NavigationBarView.this.getHomeButton().getAlpha() == 0.0f) {
                NavigationBarView.this.getBackButton().setAlpha(0);
                ValueAnimator a = ObjectAnimator.ofFloat(backButton, "alpha", new float[]{0.0f, 1.0f});
                a.setStartDelay(this.mStartDelay);
                a.setDuration(this.mDuration);
                a.setInterpolator(this.mInterpolator);
                a.start();
            }
        }
    }

    public interface OnVerticalChangedListener {
        void onVerticalChanged(boolean z);
    }

    public void onThemeChanged(Configuration newConfig) {
        onConfigurationChanged(newConfig);
    }

    public NavigationBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        this.mVertical = false;
        this.mShowMenu = false;
        this.mGestureHelper = new NavigationBarGestureHelper(context);
        this.mConfiguration = new Configuration();
        this.mConfiguration.updateFrom(context.getResources().getConfiguration());
        updateIcons(context, Configuration.EMPTY, this.mConfiguration, true);
        this.mBarTransitions = new HwNavigationBarTransitions(this);
        this.mButtonDisatchers.put(R.id.back, new ButtonDispatcher(R.id.back));
        this.mButtonDisatchers.put(R.id.home, new ButtonDispatcher(R.id.home));
        this.mButtonDisatchers.put(R.id.recent_apps, new ButtonDispatcher(R.id.recent_apps));
        this.mButtonDisatchers.put(R.id.menu, new ButtonDispatcher(R.id.menu));
        this.mButtonDisatchers.put(R.id.ime_switcher, new ButtonDispatcher(R.id.ime_switcher));
    }

    public BarTransitions getBarTransitions() {
        return this.mBarTransitions;
    }

    public void setComponents(RecentsComponent recentsComponent, Divider divider) {
        this.mGestureHelper.setComponents(recentsComponent, divider, this);
    }

    public void setOnVerticalChangedListener(OnVerticalChangedListener onVerticalChangedListener) {
        this.mOnVerticalChangedListener = onVerticalChangedListener;
        notifyVerticalChangedListener(this.mVertical);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mGestureHelper.onTouchEvent(event)) {
            return true;
        }
        if (this.mDeadZone != null && event.getAction() == 4) {
            this.mDeadZone.poke(event);
        }
        return super.onTouchEvent(event);
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        return this.mGestureHelper.onInterceptTouchEvent(event);
    }

    public void abortCurrentGesture() {
        getHomeButton().abortCurrentGesture();
    }

    public View getCurrentView() {
        return this.mCurrentView;
    }

    public View getCurrentViewStub() {
        return this.mCurrentView;
    }

    public View[] getAllViews() {
        return this.mRotatedViews;
    }

    public ButtonDispatcher getRecentsButton() {
        return (ButtonDispatcher) this.mButtonDisatchers.get(R.id.recent_apps);
    }

    public ButtonDispatcher getMenuButton() {
        return (ButtonDispatcher) this.mButtonDisatchers.get(R.id.menu);
    }

    public ButtonDispatcher getBackButton() {
        return (ButtonDispatcher) this.mButtonDisatchers.get(R.id.back);
    }

    public ButtonDispatcher getHomeButton() {
        return (ButtonDispatcher) this.mButtonDisatchers.get(R.id.home);
    }

    public ButtonDispatcher getImeSwitchButton() {
        return (ButtonDispatcher) this.mButtonDisatchers.get(R.id.ime_switcher);
    }

    private void updateCarModeIcons(Context ctx) {
        this.mBackCarModeIcon = ctx.getDrawable(R.drawable.ic_sysbar_back_carmode);
        this.mBackLandCarModeIcon = this.mBackCarModeIcon;
        this.mBackAltCarModeIcon = ctx.getDrawable(R.drawable.ic_sysbar_back_ime_carmode);
        this.mBackAltLandCarModeIcon = this.mBackAltCarModeIcon;
        this.mHomeCarModeIcon = ctx.getDrawable(R.drawable.ic_sysbar_home_carmode);
    }

    protected void updateIcons(Context ctx, Configuration oldConfig, Configuration newConfig, boolean isThemeChanged) {
        if (oldConfig.orientation == newConfig.orientation && oldConfig.densityDpi == newConfig.densityDpi) {
            if (isThemeChanged) {
            }
            if (oldConfig.densityDpi == newConfig.densityDpi || isThemeChanged) {
                this.mBackIcon = ctx.getDrawable(R.drawable.ic_sysbar_back);
                this.mBackLandIcon = this.mBackIcon;
                this.mBackAltIcon = ctx.getDrawable(R.drawable.ic_sysbar_back_ime);
                this.mBackAltLandIcon = this.mBackAltIcon;
                this.mHomeDefaultIcon = ctx.getDrawable(R.drawable.ic_sysbar_home);
                this.mRecentIcon = ctx.getDrawable(R.drawable.ic_sysbar_recent);
                this.mMenuIcon = ctx.getDrawable(R.drawable.ic_sysbar_menu);
                this.mImeIcon = ctx.getDrawable(R.drawable.ic_ime_switcher_default);
                updateCarModeIcons(ctx);
            }
            return;
        }
        if (newConfig.orientation == 2) {
            this.mDockedIcon = ctx.getDrawable(R.drawable.ic_sysbar_docked_land);
        } else {
            this.mDockedIcon = ctx.getDrawable(R.drawable.ic_sysbar_docked);
        }
        if (oldConfig.densityDpi == newConfig.densityDpi) {
        }
        this.mBackIcon = ctx.getDrawable(R.drawable.ic_sysbar_back);
        this.mBackLandIcon = this.mBackIcon;
        this.mBackAltIcon = ctx.getDrawable(R.drawable.ic_sysbar_back_ime);
        this.mBackAltLandIcon = this.mBackAltIcon;
        this.mHomeDefaultIcon = ctx.getDrawable(R.drawable.ic_sysbar_home);
        this.mRecentIcon = ctx.getDrawable(R.drawable.ic_sysbar_recent);
        this.mMenuIcon = ctx.getDrawable(R.drawable.ic_sysbar_menu);
        this.mImeIcon = ctx.getDrawable(R.drawable.ic_ime_switcher_default);
        updateCarModeIcons(ctx);
    }

    public void setLayoutDirection(int layoutDirection) {
        updateIcons(getContext(), Configuration.EMPTY, this.mConfiguration, true);
        super.setLayoutDirection(layoutDirection);
    }

    public void notifyScreenOn(boolean screenOn) {
        this.mScreenOn = screenOn;
        setDisabledFlags(this.mDisabledFlags, true);
    }

    public void setNavigationIconHints(int hints) {
        setNavigationIconHints(hints, false);
    }

    private Drawable getBackIconWithAlt(boolean carMode, boolean landscape) {
        return landscape ? carMode ? this.mBackAltLandCarModeIcon : this.mBackAltLandIcon : carMode ? this.mBackAltCarModeIcon : this.mBackAltIcon;
    }

    private Drawable getBackIcon(boolean carMode, boolean landscape) {
        return landscape ? carMode ? this.mBackLandCarModeIcon : this.mBackLandIcon : carMode ? this.mBackCarModeIcon : this.mBackIcon;
    }

    public void setNavigationIconHints(int hints, boolean force) {
        int i = 0;
        if (force || hints != this.mNavigationIconHints) {
            Drawable backIcon;
            boolean backAlt = (hints & 1) != 0;
            if (!((this.mNavigationIconHints & 1) == 0 || backAlt)) {
                this.mTransitionListener.onBackAltCleared();
            }
            this.mNavigationIconHints = hints;
            if (backAlt) {
                backIcon = getBackIconWithAlt(this.mCarMode, this.mVertical);
            } else {
                backIcon = getBackIcon(this.mCarMode, this.mVertical);
            }
            getBackButton().setImageDrawable(backIcon);
            updateRecentsIcon();
            if (this.mCarMode) {
                getHomeButton().setImageDrawable(this.mHomeCarModeIcon);
            } else {
                getHomeButton().setImageDrawable(this.mHomeDefaultIcon);
            }
            boolean showImeButton = (hints & 2) != 0;
            ButtonDispatcher imeSwitchButton = getImeSwitchButton();
            if (!showImeButton) {
                i = 4;
            }
            imeSwitchButton.setVisibility(i);
            getImeSwitchButton().setImageDrawable(this.mImeIcon);
            setMenuVisibility(this.mShowMenu, true);
            getMenuButton().setImageDrawable(this.mMenuIcon);
            setDisabledFlags(this.mDisabledFlags, true);
        }
    }

    public void setDisabledFlags(int disabledFlags) {
        setDisabledFlags(disabledFlags, false);
    }

    public void setDisabledFlags(int disabledFlags, boolean force) {
        if (force || this.mDisabledFlags != disabledFlags) {
            this.mDisabledFlags = disabledFlags;
            final boolean disableHome = (2097152 & disabledFlags) != 0;
            boolean disableRecent = !this.mCarMode ? (16777216 & disabledFlags) != 0 : true;
            boolean disableBack = (4194304 & disabledFlags) != 0 ? (this.mNavigationIconHints & 1) == 0 : false;
            boolean disableSearch = (33554432 & disabledFlags) != 0;
            if (!(disableHome && disableRecent && disableBack)) {
                disableSearch = false;
            }
            setSlippery(disableSearch);
            ViewGroup navButtons = (ViewGroup) getCurrentView().findViewById(R.id.nav_buttons);
            if (navButtons != null) {
                LayoutTransition lt = navButtons.getLayoutTransition();
                if (!(lt == null || lt.getTransitionListeners().contains(this.mTransitionListener))) {
                    lt.addTransitionListener(this.mTransitionListener);
                }
            }
            SystemUIThread.runAsync(new SimpleAsyncTask() {
                boolean isDisableRecent = disableRecent;
                boolean isInLockTask = false;

                public boolean runInThread() {
                    this.isInLockTask = NavigationBarView.this.inLockTask();
                    return super.runInThread();
                }

                public void runInUI() {
                    int i;
                    int i2 = 4;
                    if (this.isInLockTask && this.isDisableRecent && !disableHome) {
                        this.isDisableRecent = false;
                    }
                    HwLog.i("PhoneStatusBar/NavigationBarView", "disableBack: " + disableBack + ", disableHome: " + disableHome + ", isDisableRecent: " + this.isDisableRecent);
                    ButtonDispatcher backButton = NavigationBarView.this.getBackButton();
                    if (disableBack) {
                        i = 4;
                    } else {
                        i = 0;
                    }
                    backButton.setVisibility(i);
                    backButton = NavigationBarView.this.getHomeButton();
                    if (disableHome) {
                        i = 4;
                    } else {
                        i = 0;
                    }
                    backButton.setVisibility(i);
                    ButtonDispatcher recentsButton = NavigationBarView.this.getRecentsButton();
                    if (!this.isDisableRecent) {
                        i2 = 0;
                    }
                    recentsButton.setVisibility(i2);
                    super.runInUI();
                }
            });
        }
    }

    private boolean inLockTask() {
        try {
            return ActivityManagerNative.getDefault().isInLockTaskMode();
        } catch (RemoteException e) {
            return false;
        }
    }

    public void setLayoutTransitionsEnabled(boolean enabled) {
        this.mLayoutTransitionsEnabled = enabled;
        updateLayoutTransitionsEnabled();
    }

    public void setWakeAndUnlocking(boolean wakeAndUnlocking) {
        setUseFadingAnimations(wakeAndUnlocking);
        this.mWakeAndUnlocking = wakeAndUnlocking;
        updateLayoutTransitionsEnabled();
    }

    private void updateLayoutTransitionsEnabled() {
        LayoutTransition lt = null;
        boolean z = !this.mWakeAndUnlocking ? this.mLayoutTransitionsEnabled : false;
        ViewGroup navButtons = (ViewGroup) getCurrentView().findViewById(R.id.nav_buttons);
        if (navButtons != null) {
            lt = navButtons.getLayoutTransition();
        }
        if (lt == null) {
            return;
        }
        if (z) {
            lt.enableTransitionType(2);
            lt.enableTransitionType(3);
            lt.enableTransitionType(0);
            lt.enableTransitionType(1);
            return;
        }
        lt.disableTransitionType(2);
        lt.disableTransitionType(3);
        lt.disableTransitionType(0);
        lt.disableTransitionType(1);
    }

    private void setUseFadingAnimations(boolean useFadingAnimations) {
        LayoutParams lp = (LayoutParams) getLayoutParams();
        if (lp != null) {
            boolean old;
            if (lp.windowAnimations != 0) {
                old = true;
            } else {
                old = false;
            }
            if (!old && useFadingAnimations) {
                lp.windowAnimations = R.style.Animation.NavigationBarFadeIn;
            } else if (old && !useFadingAnimations) {
                lp.windowAnimations = 0;
            } else {
                return;
            }
            ((WindowManager) getContext().getSystemService("window")).updateViewLayout(this, lp);
        }
    }

    public void setSlippery(boolean newSlippery) {
        boolean oldSlippery = false;
        LayoutParams lp = (LayoutParams) getLayoutParams();
        if (lp != null) {
            if ((lp.flags & 536870912) != 0) {
                oldSlippery = true;
            }
            if (!oldSlippery && newSlippery) {
                lp.flags |= 536870912;
            } else if (oldSlippery && !newSlippery) {
                lp.flags &= -536870913;
            } else {
                return;
            }
            ((WindowManager) getContext().getSystemService("window")).updateViewLayout(this, lp);
        }
    }

    public void setMenuVisibility(boolean show) {
        setMenuVisibility(show, false);
    }

    public void setMenuVisibility(boolean show, boolean force) {
        int i = 0;
        if (force || this.mShowMenu != show) {
            this.mShowMenu = show;
            boolean shouldShow = this.mShowMenu ? (this.mNavigationIconHints & 2) == 0 : false;
            ButtonDispatcher menuButton = getMenuButton();
            if (!shouldShow) {
                i = 4;
            }
            menuButton.setVisibility(i);
        }
    }

    public void updateResources() {
        if (this.mCust != null && this.mCust.supportDebugInfo()) {
            this.mCust.toggle();
        }
    }

    public void onFinishInflate() {
        HwLog.i("PhoneStatusBar/NavigationBarView", "NavigationBarView::onFinishInflate()");
        updateRotatedViews();
        this.mCust = (HwCustNavigationBarView) HwCustUtils.createObj(HwCustNavigationBarView.class, new Object[]{getAllViews(), Looper.getMainLooper(), this.mContext});
        getImeSwitchButton().setOnClickListener(this.mImeSwitcherClickListener);
        try {
            WindowManagerGlobal.getWindowManagerService().registerDockedStackListener(new Stub() {
                public void onDividerVisibilityChanged(boolean visible) throws RemoteException {
                }

                public void onDockedStackExistsChanged(final boolean exists) throws RemoteException {
                    HwLog.i("PhoneStatusBar/NavigationBarView", "onDockedStackExistsChanged exists= " + exists);
                    NavigationBarView.this.mHandler.post(new Runnable() {
                        public void run() {
                            NavigationBarView.this.mDockedStackExists = exists;
                            NavigationBarView.this.updateRecentsIcon();
                        }
                    });
                }

                public void onDockedStackMinimizedChanged(boolean minimized, long animDuration) throws RemoteException {
                }

                public void onAdjustedForImeChanged(boolean adjustedForIme, long animDuration) throws RemoteException {
                }

                public void onDockSideChanged(int newDockSide) throws RemoteException {
                }
            });
        } catch (RemoteException e) {
            Log.e("PhoneStatusBar/NavigationBarView", "Failed registering docked stack exists listener", e);
        } catch (IllegalStateException e2) {
            Log.e("PhoneStatusBar/NavigationBarView", "Failed registering docked stack exists listener", e2);
        } catch (Exception e3) {
            Log.e("PhoneStatusBar/NavigationBarView", "Failed registering docked stack exists listener", e3);
        }
    }

    void updateRotatedViews() {
        View[] viewArr = this.mRotatedViews;
        View findViewById = findViewById(R.id.rot0);
        this.mRotatedViews[2] = findViewById;
        viewArr[0] = findViewById;
        viewArr = this.mRotatedViews;
        findViewById = findViewById(R.id.rot90);
        this.mRotatedViews[1] = findViewById;
        viewArr[3] = findViewById;
        updateCurrentView();
    }

    private void updateCurrentView() {
        int i;
        int rot = this.mDisplay.getRotation();
        for (i = 0; i < 4; i++) {
            this.mRotatedViews[i].setVisibility(8);
        }
        this.mCurrentView = this.mRotatedViews[rot];
        this.mCurrentView.setVisibility(0);
        for (i = 0; i < this.mButtonDisatchers.size(); i++) {
            ((ButtonDispatcher) this.mButtonDisatchers.valueAt(i)).setCurrentView(getCurrentViewStub());
        }
        updateLayoutTransitionsEnabled();
    }

    private void updateRecentsIcon() {
        getRecentsButton().setImageDrawable(this.mDockedStackExists ? this.mDockedIcon : this.mRecentIcon);
    }

    public void reorient() {
        updateCurrentView();
        getImeSwitchButton().setOnClickListener(this.mImeSwitcherClickListener);
        this.mDeadZone = (DeadZone) this.mCurrentView.findViewById(R.id.deadzone);
        this.mBarTransitions.init();
        setDisabledFlags(this.mDisabledFlags, true);
        setMenuVisibility(this.mShowMenu, true);
        updateTaskSwitchHelper();
        setNavigationIconHints(this.mNavigationIconHints, true);
    }

    private void updateTaskSwitchHelper() {
        this.mGestureHelper.setBarState(this.mVertical, getLayoutDirection() == 1);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        boolean newVertical = w > 0 && h > w;
        if (newVertical != this.mVertical) {
            this.mVertical = newVertical;
            reorient();
            notifyVerticalChangedListener(newVertical);
        }
        postCheckForInvalidLayout("sizeChanged");
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void notifyVerticalChangedListener(boolean newVertical) {
        if (this.mOnVerticalChangedListener != null) {
            this.mOnVerticalChangedListener.onVerticalChanged(newVertical);
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean uiCarModeChanged = updateCarMode(newConfig);
        boolean isThemeChanged = ActivityInfoWrapper.isThemeChanged(this.mLastConfig.updateFrom(newConfig));
        HwLog.i("PhoneStatusBar/NavigationBarView", "onConfigurationChanged::theme changed, " + isThemeChanged);
        updateTaskSwitchHelper();
        updateIcons(getContext(), this.mConfiguration, newConfig, isThemeChanged);
        updateRecentsIcon();
        if (!uiCarModeChanged && this.mConfiguration.densityDpi == newConfig.densityDpi) {
            if (isThemeChanged) {
            }
            this.mConfiguration.updateFrom(newConfig);
        }
        setNavigationIconHints(this.mNavigationIconHints, true);
        this.mConfiguration.updateFrom(newConfig);
    }

    private boolean updateCarMode(Configuration newConfig) {
        if (newConfig == null) {
            return false;
        }
        int uiMode = newConfig.uiMode & 15;
        if (this.mCarMode && uiMode != 3) {
            this.mCarMode = false;
            return true;
        } else if (uiMode != 3) {
            return false;
        } else {
            this.mCarMode = true;
            return true;
        }
    }

    private String getResourceName(int resId) {
        if (resId == 0) {
            return "(null)";
        }
        try {
            return getContext().getResources().getResourceName(resId);
        } catch (NotFoundException e) {
            return "(unknown)";
        }
    }

    private void postCheckForInvalidLayout(String how) {
        this.mHandler.obtainMessage(8686, 0, 0, how).sendToTarget();
    }

    private static String visibilityToString(int vis) {
        switch (vis) {
            case 4:
                return "INVISIBLE";
            case 8:
                return "GONE";
            default:
                return "VISIBLE";
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("NavigationBarView {");
        Rect r = new Rect();
        Point size = new Point();
        this.mDisplay.getRealSize(size);
        pw.println(String.format("      this: " + PhoneStatusBar.viewInfo(this) + " " + visibilityToString(getVisibility()), new Object[0]));
        getWindowVisibleDisplayFrame(r);
        boolean offscreen = r.right > size.x || r.bottom > size.y;
        pw.println("      window: " + r.toShortString() + " " + visibilityToString(getWindowVisibility()) + (offscreen ? " OFFSCREEN!" : BuildConfig.FLAVOR));
        pw.println(String.format("      mCurrentView: id=%s (%dx%d) %s", new Object[]{getResourceName(getCurrentView().getId()), Integer.valueOf(getCurrentView().getWidth()), Integer.valueOf(getCurrentView().getHeight()), visibilityToString(getCurrentView().getVisibility())}));
        String str = "      disabled=0x%08x vertical=%s menu=%s";
        Object[] objArr = new Object[3];
        objArr[0] = Integer.valueOf(this.mDisabledFlags);
        objArr[1] = this.mVertical ? "true" : "false";
        objArr[2] = this.mShowMenu ? "true" : "false";
        pw.println(String.format(str, objArr));
        dumpButton(pw, "back", getBackButton());
        dumpButton(pw, "home", getHomeButton());
        dumpButton(pw, "rcnt", getRecentsButton());
        dumpButton(pw, "menu", getMenuButton());
        pw.println("    }");
    }

    private static void dumpButton(PrintWriter pw, String caption, ButtonDispatcher button) {
        pw.print("      " + caption + ": ");
        if (button == null) {
            pw.print("null");
        } else {
            pw.print(visibilityToString(button.getVisibility()) + " alpha=" + button.getAlpha());
        }
        pw.println();
    }

    public boolean isDockedStackExists() {
        return this.mDockedStackExists;
    }
}
