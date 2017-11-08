package com.android.systemui.statusbar.phone;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.SignalClusterView;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.tuner.TunerService.Tunable;
import com.android.systemui.utils.HwLog;
import java.io.PrintWriter;
import java.util.ArrayList;

public class StatusBarIconController extends StatusBarIconList implements Tunable {
    private static final int[] sTmpInt2 = new int[2];
    private static final Rect sTmpRect = new Rect();
    private TextView mClock;
    private Context mContext;
    private float mDarkIntensity;
    private int mDarkModeIconColorSingleTone;
    private DemoStatusIcons mDemoStatusIcons;
    private final Handler mHandler;
    private final ArraySet<String> mIconBlacklist = new ArraySet();
    private int mIconHPadding;
    private int mIconSize;
    protected int mIconTint = -1;
    private int mLightModeIconColorSingleTone;
    private NotificationIconAreaController mNotificationIconAreaController;
    private View mNotificationIconAreaInner;
    private View mOperatorNameParentView;
    private float mPendingDarkIntensity;
    private PhoneStatusBar mPhoneStatusBar;
    private SignalClusterView mSignalCluster;
    private LinearLayout mStatusIcons;
    private LinearLayout mStatusIconsCover;
    private LinearLayout mStatusIconsKeyguard;
    private LinearLayout mSystemIconArea;
    private ValueAnimator mTintAnimator;
    private final Rect mTintArea = new Rect();
    private boolean mTintChangePending;
    private boolean mTransitionDeferring;
    private final Runnable mTransitionDeferringDoneRunnable = new Runnable() {
        public void run() {
            StatusBarIconController.this.mTransitionDeferring = false;
        }
    };
    private long mTransitionDeferringDuration;
    private long mTransitionDeferringStartTime;
    private boolean mTransitionPending;

    public StatusBarIconController(Context context, View statusBar, View keyguardStatusBar, PhoneStatusBar phoneStatusBar) {
        this.mContext = context;
        this.mPhoneStatusBar = phoneStatusBar;
        this.mSystemIconArea = (LinearLayout) statusBar.findViewById(R.id.system_icon_area);
        this.mStatusIcons = (LinearLayout) statusBar.findViewById(R.id.statusIcons);
        this.mSignalCluster = (SignalClusterView) statusBar.findViewById(R.id.signal_cluster);
        this.mNotificationIconAreaController = new HwNotificationIconAreaController(context, phoneStatusBar);
        this.mNotificationIconAreaInner = this.mNotificationIconAreaController.getNotificationInnerAreaView();
        this.mOperatorNameParentView = statusBar.findViewById(R.id.operator_name_container);
        ((ViewGroup) statusBar.findViewById(R.id.notification_icon_area)).addView(this.mNotificationIconAreaInner);
        this.mStatusIconsKeyguard = (LinearLayout) keyguardStatusBar.findViewById(R.id.statusIcons);
        this.mStatusIconsCover = (LinearLayout) HwPhoneStatusBar.getInstance().getCoverStatusBarView().findViewById(R.id.statusIcons);
        scaleBatteryMeterViews(context);
        this.mClock = (TextView) statusBar.findViewById(R.id.clock);
        this.mDarkModeIconColorSingleTone = context.getColor(R.color.dark_mode_icon_color_single_tone);
        this.mLightModeIconColorSingleTone = context.getColor(R.color.light_mode_icon_color_single_tone);
        this.mHandler = new Handler();
        defineSlots();
        loadDimens();
        TunerService.get(this.mContext).addTunable((Tunable) this, "icon_blacklist");
    }

    public void setSignalCluster(SignalClusterView signalCluster) {
        if (signalCluster == null) {
            HwLog.w("StatusBarIconController", "setSignalCluster is null");
        }
        this.mSignalCluster = signalCluster;
    }

    private void scaleBatteryMeterViews(Context context) {
        Resources res = context.getResources();
        TypedValue typedValue = new TypedValue();
        res.getValue(R.dimen.status_bar_icon_scale_factor, typedValue, true);
        float iconScaleFactor = typedValue.getFloat();
        int batteryHeight = res.getDimensionPixelSize(R.dimen.status_bar_battery_icon_height);
        int batteryWidth = res.getDimensionPixelSize(R.dimen.status_bar_battery_icon_width);
        new LayoutParams((int) (((float) batteryWidth) * iconScaleFactor), (int) (((float) batteryHeight) * iconScaleFactor)).setMarginsRelative(0, 0, 0, res.getDimensionPixelSize(R.dimen.battery_margin_bottom));
    }

    public void onTuningChanged(String key, String newValue) {
        if ("icon_blacklist".equals(key)) {
            int i;
            this.mIconBlacklist.clear();
            this.mIconBlacklist.addAll(getIconBlacklist(newValue));
            ArrayList<StatusBarIconView> views = new ArrayList();
            for (i = 0; i < this.mStatusIcons.getChildCount(); i++) {
                views.add((StatusBarIconView) this.mStatusIcons.getChildAt(i));
            }
            for (i = views.size() - 1; i >= 0; i--) {
                removeIcon(((StatusBarIconView) views.get(i)).getSlot());
            }
            for (i = 0; i < views.size(); i++) {
                setIcon(((StatusBarIconView) views.get(i)).getSlot(), ((StatusBarIconView) views.get(i)).getStatusBarIcon());
            }
        }
    }

    private void loadDimens() {
        this.mIconSize = this.mContext.getResources().getDimensionPixelSize(17104926);
        this.mIconHPadding = this.mContext.getResources().getDimensionPixelSize(R.dimen.status_bar_icon_padding);
    }

    public void defineSlots() {
        defineSlots(this.mContext.getResources().getStringArray(17235982));
    }

    private boolean filterSystemIcon(String slot) {
        if (TextUtils.isEmpty(slot) || !"ime".equals(slot)) {
            return false;
        }
        HwLog.i("StatusBarIconController", "slot=" + slot + ",ignore this slot.");
        return true;
    }

    private void addSystemIcon(int index, StatusBarIcon icon) {
        String slot = getSlot(index);
        int viewIndex = getViewIndex(index);
        boolean blocked = this.mIconBlacklist.contains(slot);
        StatusBarIconView view = new StatusBarIconView(this.mContext, slot, null, blocked);
        view.set(icon);
        LayoutParams lp = new LayoutParams(-2, this.mIconSize);
        lp.setMargins(this.mIconHPadding, 0, this.mIconHPadding, 0);
        this.mStatusIcons.addView(view, viewIndex, lp);
        view = new StatusBarIconView(this.mContext, slot, null, blocked);
        view.set(icon);
        this.mStatusIconsKeyguard.addView(view, viewIndex, new LayoutParams(-2, this.mIconSize));
        view = new StatusBarIconView(this.mContext, slot, null, blocked);
        view.set(icon);
        this.mStatusIconsCover.addView(view, viewIndex, new LayoutParams(-2, this.mIconSize));
        applyIconTint();
    }

    public void setIcon(String slot, int resourceId, CharSequence contentDescription) {
        int index = getSlotIndex(slot);
        StatusBarIcon icon = getIcon(index);
        if (icon == null) {
            setIcon(slot, new StatusBarIcon(UserHandle.SYSTEM, this.mContext.getPackageName(), Icon.createWithResource(this.mContext, resourceId), 0, 0, contentDescription));
            return;
        }
        icon.icon = Icon.createWithResource(this.mContext, resourceId);
        icon.contentDescription = contentDescription;
        handleSet(index, icon);
    }

    public void setExternalIcon(String slot) {
        int viewIndex = getViewIndex(getSlotIndex(slot));
        int height = this.mContext.getResources().getDimensionPixelSize(R.dimen.status_bar_icon_drawing_size);
        ImageView imageView = (ImageView) this.mStatusIcons.getChildAt(viewIndex);
        imageView.setScaleType(ScaleType.FIT_CENTER);
        imageView.setAdjustViewBounds(true);
        setHeightAndCenter(imageView, height);
        imageView = (ImageView) this.mStatusIconsKeyguard.getChildAt(viewIndex);
        imageView.setScaleType(ScaleType.FIT_CENTER);
        imageView.setAdjustViewBounds(true);
        setHeightAndCenter(imageView, height);
        imageView = (ImageView) this.mStatusIconsCover.getChildAt(viewIndex);
        imageView.setScaleType(ScaleType.FIT_CENTER);
        imageView.setAdjustViewBounds(true);
        setHeightAndCenter(imageView, height);
    }

    private void setHeightAndCenter(ImageView imageView, int height) {
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        params.height = height;
        if (params instanceof LayoutParams) {
            ((LayoutParams) params).gravity = 16;
        }
        imageView.setLayoutParams(params);
    }

    public void setIcon(String slot, StatusBarIcon icon) {
        setIcon(getSlotIndex(slot), icon);
    }

    public void removeIcon(String slot) {
        removeIcon(getSlotIndex(slot));
    }

    public void setIconVisibility(String slot, boolean visibility) {
        int index = getSlotIndex(slot);
        StatusBarIcon icon = getIcon(index);
        if (icon == null || icon.visible == visibility) {
            Log.d("StatusBarIconController", "setIconVisibility icon is null or visibility not change visibility=" + visibility);
            return;
        }
        icon.visible = visibility;
        handleSet(index, icon);
    }

    public void removeIcon(int index) {
        if (getIcon(index) != null) {
            super.removeIcon(index);
            int viewIndex = getViewIndex(index);
            this.mStatusIcons.removeViewAt(viewIndex);
            this.mStatusIconsKeyguard.removeViewAt(viewIndex);
            this.mStatusIconsCover.removeViewAt(viewIndex);
        }
    }

    public void setIcon(int index, StatusBarIcon icon) {
        if (icon == null) {
            removeIcon(index);
        } else if (!filterSystemIcon(getSlot(index))) {
            boolean isNew = getIcon(index) == null;
            super.setIcon(index, icon);
            if (isNew) {
                addSystemIcon(index, icon);
            } else {
                handleSet(index, icon);
            }
        }
    }

    private void handleSet(int index, StatusBarIcon icon) {
        int viewIndex = getViewIndex(index);
        StatusBarIconView view = (StatusBarIconView) this.mStatusIcons.getChildAt(viewIndex);
        if (view != null) {
            view.set(icon);
        } else {
            HwLog.e("StatusBarIconController", "handleSet mStatusIcons view is null");
        }
        view = (StatusBarIconView) this.mStatusIconsKeyguard.getChildAt(viewIndex);
        if (view != null) {
            view.set(icon);
        } else {
            HwLog.e("StatusBarIconController", "handleSet mStatusIconsKeyguard view is null");
        }
        view = (StatusBarIconView) this.mStatusIconsCover.getChildAt(viewIndex);
        if (view != null) {
            view.set(icon);
        } else {
            HwLog.e("StatusBarIconController", "handleSet mStatusIconsCover view is null");
        }
        applyIconTint();
    }

    public void updateNotificationIcons(NotificationData notificationData) {
        this.mNotificationIconAreaController.updateNotificationIcons(notificationData);
    }

    public void hideSystemIconArea(boolean animate) {
        animateHide(this.mSystemIconArea, animate);
    }

    public void showSystemIconArea(boolean animate) {
        animateShow(this.mSystemIconArea, animate);
    }

    public void hideNotificationIconArea(boolean animate) {
        animateHide(this.mNotificationIconAreaInner, animate);
        animateHide(this.mOperatorNameParentView, animate);
    }

    public void showNotificationIconArea(boolean animate) {
        animateShow(this.mNotificationIconAreaInner, animate);
        animateShow(this.mOperatorNameParentView, animate);
    }

    public void setClockVisibility(boolean visible) {
        HwLog.i("StatusBarIconController", "setClockVisibility:" + visible);
        this.mClock.setVisibility(visible ? 0 : 8);
    }

    public void dump(PrintWriter pw) {
        int N = this.mStatusIcons.getChildCount();
        pw.println("  system icons: " + N);
        for (int i = 0; i < N; i++) {
            pw.println("    [" + i + "] icon=" + ((StatusBarIconView) this.mStatusIcons.getChildAt(i)));
        }
    }

    public void dispatchDemoCommand(String command, Bundle args) {
        if (this.mDemoStatusIcons == null) {
            this.mDemoStatusIcons = new DemoStatusIcons(this.mStatusIcons, this.mIconSize);
        }
        this.mDemoStatusIcons.dispatchDemoCommand(command, args);
    }

    private void animateHide(final View v, boolean animate) {
        v.animate().cancel();
        if (animate) {
            v.animate().alpha(0.0f).setDuration(160).setStartDelay(0).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(new Runnable() {
                public void run() {
                    v.setVisibility(4);
                }
            });
            return;
        }
        v.setAlpha(0.0f);
        v.setVisibility(4);
    }

    private void animateShow(View v, boolean animate) {
        v.animate().cancel();
        v.setVisibility(0);
        if (animate) {
            v.animate().alpha(1.0f).setDuration(320).setInterpolator(Interpolators.ALPHA_IN).setStartDelay(50).withEndAction(null);
            if (this.mPhoneStatusBar.isKeyguardFadingAway()) {
                v.animate().setDuration(this.mPhoneStatusBar.getKeyguardFadingAwayDuration()).setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN).setStartDelay(this.mPhoneStatusBar.getKeyguardFadingAwayDelay()).start();
            }
            return;
        }
        v.setAlpha(1.0f);
    }

    private void animateIconTint(float targetDarkIntensity, long delay, long duration) {
        if (this.mTintAnimator != null) {
            this.mTintAnimator.cancel();
        }
        if (this.mDarkIntensity != targetDarkIntensity) {
            this.mTintAnimator = ValueAnimator.ofFloat(new float[]{this.mDarkIntensity, targetDarkIntensity});
            this.mTintAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    StatusBarIconController.this.setIconTintInternal(((Float) animation.getAnimatedValue()).floatValue());
                }
            });
            this.mTintAnimator.setDuration(duration);
            this.mTintAnimator.setStartDelay(delay);
            this.mTintAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            this.mTintAnimator.start();
        }
    }

    protected void setIconTintInternal(float darkIntensity) {
        this.mDarkIntensity = darkIntensity;
        this.mIconTint = ((Integer) ArgbEvaluator.getInstance().evaluate(darkIntensity, Integer.valueOf(this.mLightModeIconColorSingleTone), Integer.valueOf(this.mDarkModeIconColorSingleTone))).intValue();
        this.mNotificationIconAreaController.setIconTint(this.mIconTint);
        applyIconTint();
    }

    public static int getTint(Rect tintArea, View view, int color) {
        if (isInArea(tintArea, view)) {
            return color;
        }
        return -1;
    }

    public static float getDarkIntensity(Rect tintArea, View view, float intensity) {
        if (isInArea(tintArea, view)) {
            return intensity;
        }
        return 0.0f;
    }

    private static boolean isInArea(Rect area, View view) {
        boolean majorityOfWidth = true;
        if (area.isEmpty()) {
            return true;
        }
        sTmpRect.set(area);
        view.getLocationOnScreen(sTmpInt2);
        int left = sTmpInt2[0];
        int intersectAmount = Math.max(0, Math.min(view.getWidth() + left, area.right) - Math.max(left, area.left));
        boolean coversFullStatusBar = area.top <= 0;
        if (intersectAmount * 2 <= view.getWidth()) {
            majorityOfWidth = false;
        }
        if (!majorityOfWidth) {
            coversFullStatusBar = false;
        }
        return coversFullStatusBar;
    }

    protected void applyIconTint() {
        for (int i = 0; i < this.mStatusIcons.getChildCount(); i++) {
            StatusBarIconView v = (StatusBarIconView) this.mStatusIcons.getChildAt(i);
            v.setImageTintList(ColorStateList.valueOf(getTint(this.mTintArea, v, this.mIconTint)));
        }
        if (this.mSignalCluster != null) {
            this.mSignalCluster.setIconTint(this.mIconTint, this.mDarkIntensity, this.mTintArea);
        }
        this.mClock.setTextColor(getTint(this.mTintArea, this.mClock, this.mIconTint));
    }

    public void appTransitionPending() {
        this.mTransitionPending = true;
    }

    public void appTransitionCancelled() {
        if (this.mTransitionPending && this.mTintChangePending) {
            this.mTintChangePending = false;
            animateIconTint(this.mPendingDarkIntensity, 0, 120);
        }
        this.mTransitionPending = false;
    }

    public void appTransitionStarting(long startTime, long duration) {
        if (this.mTransitionPending && this.mTintChangePending) {
            this.mTintChangePending = false;
            animateIconTint(this.mPendingDarkIntensity, Math.max(0, startTime - SystemClock.uptimeMillis()), duration);
        } else if (this.mTransitionPending) {
            this.mTransitionDeferring = true;
            this.mTransitionDeferringStartTime = startTime;
            this.mTransitionDeferringDuration = duration;
            this.mHandler.removeCallbacks(this.mTransitionDeferringDoneRunnable);
            this.mHandler.postAtTime(this.mTransitionDeferringDoneRunnable, startTime);
        }
        this.mTransitionPending = false;
    }

    public static ArraySet<String> getIconBlacklist(String blackListStr) {
        ArraySet<String> ret = new ArraySet();
        if (blackListStr == null) {
            blackListStr = "rotate";
        }
        for (String slot : blackListStr.split(",")) {
            if (!TextUtils.isEmpty(slot)) {
                ret.add(slot);
            }
        }
        return ret;
    }

    public void onDensityOrFontScaleChanged() {
        int i;
        loadDimens();
        this.mNotificationIconAreaController.onDensityOrFontScaleChanged(this.mContext);
        updateClock();
        for (i = 0; i < this.mStatusIcons.getChildCount(); i++) {
            View child = this.mStatusIcons.getChildAt(i);
            LayoutParams lp = new LayoutParams(-2, this.mIconSize);
            lp.setMargins(this.mIconHPadding, 0, this.mIconHPadding, 0);
            child.setLayoutParams(lp);
        }
        for (i = 0; i < this.mStatusIconsKeyguard.getChildCount(); i++) {
            this.mStatusIconsKeyguard.getChildAt(i).setLayoutParams(new LayoutParams(-2, this.mIconSize));
        }
        for (i = 0; i < this.mStatusIconsCover.getChildCount(); i++) {
            this.mStatusIconsCover.getChildAt(i).setLayoutParams(new LayoutParams(-2, this.mIconSize));
        }
        scaleBatteryMeterViews(this.mContext);
    }

    private void updateClock() {
        FontSizeUtils.updateFontSize(this.mClock, R.dimen.status_bar_clock_size);
        this.mClock.setPaddingRelative(this.mContext.getResources().getDimensionPixelSize(R.dimen.status_bar_clock_starting_padding), 0, this.mContext.getResources().getDimensionPixelSize(R.dimen.status_bar_clock_end_padding), 0);
    }
}
