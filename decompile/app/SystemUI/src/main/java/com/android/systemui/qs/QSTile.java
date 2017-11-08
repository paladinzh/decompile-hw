package com.android.systemui.qs;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.logging.MetricsLogger;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.systemui.qs.external.TileServices;
import com.android.systemui.statusbar.phone.ManagedProfileController;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.CastController;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.NightModeController;
import com.android.systemui.statusbar.policy.RotationLockController;
import com.android.systemui.statusbar.policy.SoundVibrationController;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.UserSwitchUtils;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.Objects;

public abstract class QSTile<TState extends State> {
    protected static final boolean DEBUG = Log.isLoggable("Tile", 3);
    protected final String TAG = ("Tile." + getClass().getSimpleName());
    private boolean mAnnounceNextStateChange;
    private final ArrayList<Callback> mCallbacks = new ArrayList();
    protected final Context mContext;
    protected final H mHandler;
    protected final Host mHost;
    protected HwCustQSTile mHwCustQSTile = ((HwCustQSTile) HwCustUtils.createObj(HwCustQSTile.class, new Object[]{this}));
    protected boolean mLastState;
    private boolean mListening = false;
    protected boolean mProcessingState;
    protected TState mState = newTileState();
    private String mTileSpec;
    private TState mTmpState = newTileState();
    protected final Handler mUiHandler = new Handler(Looper.getMainLooper());

    public interface Callback {
        void onAnnouncementRequested(CharSequence charSequence);

        void onScanStateChanged(boolean z);

        void onShowDetail(boolean z);

        void onStateChanged(State state);

        void onToggleStateChanged(boolean z);
    }

    public static class State {
        public boolean autoMirrorDrawable = false;
        public CharSequence contentDescription;
        public boolean disabledByPolicy;
        public CharSequence dualLabelContentDescription;
        public EnforcedAdmin enforcedAdmin;
        public String expandedAccessibilityClassName;
        public Icon icon;
        public CharSequence label;
        public int labelTint = -1;
        public String minimalAccessibilityClassName;
        public CharSequence minimalContentDescription;
        public long textChangedDelay = 0;

        public boolean copyTo(State other) {
            if (other == null) {
                throw new IllegalArgumentException();
            } else if (other.getClass().equals(getClass())) {
                boolean changed = (Objects.equals(other.icon, this.icon) && Objects.equals(other.label, this.label) && Objects.equals(Integer.valueOf(other.labelTint), Integer.valueOf(this.labelTint)) && Objects.equals(other.contentDescription, this.contentDescription) && Objects.equals(Boolean.valueOf(other.autoMirrorDrawable), Boolean.valueOf(this.autoMirrorDrawable)) && Objects.equals(other.dualLabelContentDescription, this.dualLabelContentDescription) && Objects.equals(other.minimalContentDescription, this.minimalContentDescription) && Objects.equals(other.minimalAccessibilityClassName, this.minimalAccessibilityClassName) && Objects.equals(other.expandedAccessibilityClassName, this.expandedAccessibilityClassName) && Objects.equals(Boolean.valueOf(other.disabledByPolicy), Boolean.valueOf(this.disabledByPolicy))) ? !Objects.equals(other.enforcedAdmin, this.enforcedAdmin) : true;
                other.icon = this.icon;
                other.label = this.label;
                other.labelTint = this.labelTint;
                other.textChangedDelay = this.textChangedDelay;
                other.contentDescription = this.contentDescription;
                other.dualLabelContentDescription = this.dualLabelContentDescription;
                other.minimalContentDescription = this.minimalContentDescription;
                other.minimalAccessibilityClassName = this.minimalAccessibilityClassName;
                other.expandedAccessibilityClassName = this.expandedAccessibilityClassName;
                other.autoMirrorDrawable = this.autoMirrorDrawable;
                other.disabledByPolicy = this.disabledByPolicy;
                if (this.enforcedAdmin == null) {
                    other.enforcedAdmin = null;
                } else if (other.enforcedAdmin == null) {
                    other.enforcedAdmin = new EnforcedAdmin(this.enforcedAdmin);
                } else {
                    this.enforcedAdmin.copyTo(other.enforcedAdmin);
                }
                return changed;
            } else {
                throw new IllegalArgumentException();
            }
        }

        public String toString() {
            return toStringBuilder().toString();
        }

        protected StringBuilder toStringBuilder() {
            StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append('[');
            sb.append(",icon=").append(this.icon);
            sb.append(",label=").append(this.label);
            sb.append(",labelTint=").append(this.labelTint);
            sb.append(",textChangedDelay=").append(this.textChangedDelay);
            sb.append(",contentDescription=").append(this.contentDescription);
            sb.append(",dualLabelContentDescription=").append(this.dualLabelContentDescription);
            sb.append(",minimalContentDescription=").append(this.minimalContentDescription);
            sb.append(",minimalAccessibilityClassName=").append(this.minimalAccessibilityClassName);
            sb.append(",expandedAccessibilityClassName=").append(this.expandedAccessibilityClassName);
            sb.append(",autoMirrorDrawable=").append(this.autoMirrorDrawable);
            sb.append(",disabledByPolicy=").append(this.disabledByPolicy);
            sb.append(",enforcedAdmin=").append(this.enforcedAdmin);
            return sb.append(']');
        }
    }

    public static class BooleanState extends State {
        public boolean value;

        public boolean copyTo(State other) {
            BooleanState o = (BooleanState) other;
            boolean changed = super.copyTo(other) || o.value != this.value;
            o.value = this.value;
            return changed;
        }

        protected StringBuilder toStringBuilder() {
            StringBuilder rt = super.toStringBuilder();
            rt.insert(rt.length() - 1, ",value=" + this.value);
            return rt;
        }
    }

    public static class AirplaneBooleanState extends BooleanState {
        public boolean isAirplaneMode;

        public boolean copyTo(State other) {
            AirplaneBooleanState o = (AirplaneBooleanState) other;
            boolean changed = super.copyTo(other) || o.isAirplaneMode != this.isAirplaneMode;
            o.isAirplaneMode = this.isAirplaneMode;
            return changed;
        }
    }

    public static abstract class Icon {
        public abstract Drawable getDrawable(Context context);

        public Drawable getInvisibleDrawable(Context context) {
            return getDrawable(context);
        }

        public int hashCode() {
            return Icon.class.hashCode();
        }

        public int getPadding() {
            return 0;
        }
    }

    public static class ResourceIcon extends Icon {
        private static final SparseArray<Icon> ICONS = new SparseArray();
        protected final int mResId;

        private ResourceIcon(int resId) {
            this.mResId = resId;
        }

        public static Icon get(int resId) {
            Icon icon = (Icon) ICONS.get(resId);
            if (icon != null) {
                return icon;
            }
            icon = new ResourceIcon(resId);
            ICONS.put(resId, icon);
            return icon;
        }

        public Drawable getDrawable(Context context) {
            return context.getDrawable(this.mResId);
        }

        public Drawable getInvisibleDrawable(Context context) {
            return context.getDrawable(this.mResId);
        }

        public boolean equals(Object o) {
            return (o instanceof ResourceIcon) && ((ResourceIcon) o).mResId == this.mResId;
        }

        public String toString() {
            return String.format("ResourceIcon[resId=0x%08x]", new Object[]{Integer.valueOf(this.mResId)});
        }
    }

    protected static class AnimationIcon extends ResourceIcon {
        private final int mAnimatedResId;

        public AnimationIcon(int resId, int staticResId) {
            super(staticResId);
            this.mAnimatedResId = resId;
        }

        public Drawable getDrawable(Context context) {
            return context.getDrawable(this.mAnimatedResId).getConstantState().newDrawable();
        }
    }

    public interface DetailAdapter {
        View createDetailView(Context context, View view, ViewGroup viewGroup);

        int getMetricsCategory();

        Intent getSettingsIntent();

        CharSequence getTitle();

        Boolean getToggleState();

        void setToggleState(boolean z);
    }

    public static class DrawableIcon extends Icon {
        protected final Drawable mDrawable;

        public DrawableIcon(Drawable drawable) {
            this.mDrawable = drawable;
        }

        public Drawable getDrawable(Context context) {
            return this.mDrawable;
        }

        public Drawable getInvisibleDrawable(Context context) {
            return this.mDrawable;
        }
    }

    protected final class H extends Handler {
        private H(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            boolean z = true;
            try {
                String name;
                if (msg.what == 1) {
                    name = "handleAddCallback";
                    QSTile.this.handleAddCallback((Callback) msg.obj);
                } else if (msg.what == 12) {
                    name = "handleRemoveCallbacks";
                    QSTile.this.handleRemoveCallbacks();
                } else if (msg.what == 13) {
                    name = "handleRemoveCallback";
                    QSTile.this.handleRemoveCallback((Callback) msg.obj);
                } else if (msg.what == 2) {
                    name = "handleClick";
                    if (QSTile.this.mState.disabledByPolicy) {
                        QSTile.this.mHost.startActivityDismissingKeyguard(RestrictedLockUtils.getShowAdminSupportDetailsIntent(QSTile.this.mContext, QSTile.this.mState.enforcedAdmin));
                        return;
                    }
                    QSTile.this.mAnnounceNextStateChange = true;
                    QSTile.this.handleClick();
                } else if (msg.what == 3) {
                    name = "handleSecondaryClick";
                    QSTile.this.handleSecondaryClick();
                } else if (msg.what == 4) {
                    name = "handleLongClick";
                    QSTile.this.handleLongClick();
                } else if (msg.what == 5) {
                    name = "handleRefreshState";
                    QSTile.this.handleRefreshState(msg.obj);
                } else if (msg.what == 6) {
                    name = "handleShowDetail";
                    r6 = QSTile.this;
                    if (msg.arg1 == 0) {
                        z = false;
                    }
                    r6.handleShowDetail(z);
                } else if (msg.what == 7) {
                    name = "handleUserSwitch";
                    QSTile.this.handleUserSwitch(msg.arg1);
                } else if (msg.what == 8) {
                    name = "handleToggleStateChanged";
                    r6 = QSTile.this;
                    if (msg.arg1 == 0) {
                        z = false;
                    }
                    r6.handleToggleStateChanged(z);
                } else if (msg.what == 9) {
                    name = "handleScanStateChanged";
                    r6 = QSTile.this;
                    if (msg.arg1 == 0) {
                        z = false;
                    }
                    r6.handleScanStateChanged(z);
                } else if (msg.what == 10) {
                    name = "handleDestroy";
                    QSTile.this.handleDestroy();
                } else if (msg.what == 11) {
                    name = "handleClearState";
                    QSTile.this.handleClearState();
                } else if (msg.what == 14) {
                    name = "setListening";
                    r6 = QSTile.this;
                    if (msg.arg1 == 0) {
                        z = false;
                    }
                    r6.setListening(z);
                } else if (msg.what == 15) {
                    name = "handleRefreshStateByObserver";
                    QSTile.this.handleRefreshStateByObserver(msg.obj);
                } else {
                    throw new IllegalArgumentException("Unknown msg: " + msg.what);
                }
            } catch (Throwable t) {
                String error = "Error in " + null;
                Log.w(QSTile.this.TAG, error, t);
                QSTile.this.mHost.warn(error, t);
            }
        }
    }

    public interface Host {

        public interface Callback {
            void onTilesChanged();
        }

        void collapsePanels();

        BatteryController getBatteryController();

        BluetoothController getBluetoothController();

        CastController getCastController();

        Context getContext();

        FlashlightController getFlashlightController();

        HotspotController getHotspotController();

        KeyguardMonitor getKeyguardMonitor();

        LocationController getLocationController();

        Looper getLooper();

        ManagedProfileController getManagedProfileController();

        NetworkController getNetworkController();

        NightModeController getNightModeController();

        RotationLockController getRotationLockController();

        SoundVibrationController getSoundVibrationController();

        TileServices getTileServices();

        UserInfoController getUserInfoController();

        UserSwitcherController getUserSwitcherController();

        ZenModeController getZenModeController();

        void openPanels();

        void removeTile(String str);

        void startActivityDismissingKeyguard(PendingIntent pendingIntent);

        void startActivityDismissingKeyguard(Intent intent);

        void startRunnableDismissingKeyguard(Runnable runnable);

        void updateTileState(State state, String str);

        void warn(String str, Throwable th);
    }

    public static final class RingModeState extends BooleanState {
        public int mode;

        public boolean copyTo(State other) {
            RingModeState o = (RingModeState) other;
            boolean changed = super.copyTo(other) || o.mode != this.mode;
            o.mode = this.mode;
            return changed;
        }

        protected StringBuilder toStringBuilder() {
            StringBuilder rt = super.toStringBuilder();
            rt.insert(rt.length() - 1, ",mode=" + this.mode);
            return rt;
        }
    }

    public static final class SignalState extends BooleanState {
        public boolean activityIn;
        public boolean activityOut;
        public boolean connected;
        public boolean filter;
        public boolean isOverlayIconWide;
        public int overlayIconId;

        public boolean copyTo(State other) {
            SignalState o = (SignalState) other;
            boolean changed = (o.connected == this.connected && o.activityIn == this.activityIn && o.activityOut == this.activityOut && o.overlayIconId == this.overlayIconId) ? o.isOverlayIconWide != this.isOverlayIconWide : true;
            o.connected = this.connected;
            o.activityIn = this.activityIn;
            o.activityOut = this.activityOut;
            o.overlayIconId = this.overlayIconId;
            o.filter = this.filter;
            o.isOverlayIconWide = this.isOverlayIconWide;
            return !super.copyTo(other) ? changed : true;
        }

        protected StringBuilder toStringBuilder() {
            StringBuilder rt = super.toStringBuilder();
            rt.insert(rt.length() - 1, ",connected=" + this.connected);
            rt.insert(rt.length() - 1, ",activityIn=" + this.activityIn);
            rt.insert(rt.length() - 1, ",activityOut=" + this.activityOut);
            rt.insert(rt.length() - 1, ",overlayIconId=" + this.overlayIconId);
            rt.insert(rt.length() - 1, ",filter=" + this.filter);
            rt.insert(rt.length() - 1, ",wideOverlayIcon=" + this.isOverlayIconWide);
            return rt;
        }
    }

    public abstract Intent getLongClickIntent();

    public abstract int getMetricsCategory();

    public abstract CharSequence getTileLabel();

    protected abstract void handleClick();

    protected abstract void handleUpdateState(TState tState, Object obj);

    public abstract TState newTileState();

    protected abstract void setListening(boolean z);

    public void setNewState(boolean isEnabled) {
    }

    protected QSTile(Host host) {
        this.mHost = host;
        this.mContext = host.getContext();
        this.mHandler = new H(host.getLooper());
    }

    public void setListening(Object listener, boolean listening) {
        if (DEBUG) {
            Log.d(this.TAG, "setListening: listener=" + this + ", listening=" + listening + ", mListening=" + this.mListening);
        }
        if (this.mListening != listening) {
            this.mListening = listening;
            if (listening) {
                if (DEBUG) {
                    Log.d(this.TAG, "setListening true");
                }
                this.mHandler.obtainMessage(14, 1, 0).sendToTarget();
            } else {
                if (DEBUG) {
                    Log.d(this.TAG, "setListening false");
                }
                this.mHandler.obtainMessage(14, 0, 0).sendToTarget();
            }
        }
    }

    public String getTileSpec() {
        return this.mTileSpec;
    }

    public void setTileSpec(String tileSpec) {
        this.mTileSpec = tileSpec;
    }

    public Host getHost() {
        return this.mHost;
    }

    public QSIconView createTileView(Context context) {
        return new QSIconView(context);
    }

    public DetailAdapter getDetailAdapter() {
        return null;
    }

    public boolean isAvailable() {
        return true;
    }

    public void addCallback(Callback callback) {
        this.mHandler.obtainMessage(1, callback).sendToTarget();
    }

    public void removeCallback(Callback callback) {
        this.mHandler.obtainMessage(13, callback).sendToTarget();
    }

    public void removeCallbacks() {
        this.mHandler.sendEmptyMessage(12);
    }

    public void click() {
        this.mHandler.sendEmptyMessage(2);
    }

    public void secondaryClick() {
        this.mHandler.sendEmptyMessage(3);
    }

    public void longClick() {
        this.mHandler.sendEmptyMessage(4);
    }

    public void showDetail(boolean show) {
        int i;
        H h = this.mHandler;
        if (show) {
            i = 1;
        } else {
            i = 0;
        }
        h.obtainMessage(6, i, 0).sendToTarget();
    }

    public final void refreshState() {
        HwLog.i(this.TAG, "refreshState=" + this.mTileSpec + ", " + this.mState);
        refreshState(null);
    }

    protected final void refreshState(Object arg) {
        this.mHandler.obtainMessage(5, arg).sendToTarget();
    }

    protected final void onObserverChanged() {
        this.mHandler.obtainMessage(15).sendToTarget();
    }

    public final void clearState() {
        this.mHandler.sendEmptyMessage(11);
    }

    public void fireToggleStateChanged(boolean state) {
        int i;
        H h = this.mHandler;
        if (state) {
            i = 1;
        } else {
            i = 0;
        }
        h.obtainMessage(8, i, 0).sendToTarget();
    }

    public void fireScanStateChanged(boolean state) {
        int i;
        H h = this.mHandler;
        if (state) {
            i = 1;
        } else {
            i = 0;
        }
        h.obtainMessage(9, i, 0).sendToTarget();
    }

    public void destroy() {
        this.mHandler.sendEmptyMessage(10);
    }

    public TState getState() {
        return this.mState;
    }

    public void setDetailListening(boolean listening) {
    }

    private void handleAddCallback(Callback callback) {
        this.mCallbacks.add(callback);
        callback.onStateChanged(this.mState);
    }

    private void handleRemoveCallback(Callback callback) {
        this.mCallbacks.remove(callback);
    }

    private void handleRemoveCallbacks() {
        this.mCallbacks.clear();
    }

    protected void handleSecondaryClick() {
        handleClick();
    }

    protected void handleLongClick() {
        MetricsLogger.action(this.mContext, 366, getTileSpec());
        Intent longClickIntent = getLongClickIntent();
        if (longClickIntent != null) {
            this.mHost.startActivityDismissingKeyguard(longClickIntent);
        }
    }

    protected void handleClearState() {
        this.mTmpState = newTileState();
        this.mState = newTileState();
    }

    protected void handleRefreshState(Object arg) {
        handleUpdateState(this.mTmpState, arg);
        if (this.mTmpState.copyTo(this.mState)) {
            handleStateChanged();
        }
    }

    protected void handleRefreshStateByObserver(Object arg) {
        this.mProcessingState = false;
        handleRefreshState(null);
    }

    private void handleStateChanged() {
        boolean delayAnnouncement = shouldAnnouncementBeDelayed();
        if (this.mCallbacks.size() != 0) {
            for (int i = 0; i < this.mCallbacks.size(); i++) {
                ((Callback) this.mCallbacks.get(i)).onStateChanged(this.mState);
            }
            if (this.mAnnounceNextStateChange && !delayAnnouncement) {
                String announcement = composeChangeAnnouncement();
                if (announcement != null) {
                    ((Callback) this.mCallbacks.get(0)).onAnnouncementRequested(announcement);
                }
            }
        }
        if (!this.mAnnounceNextStateChange) {
            delayAnnouncement = false;
        }
        this.mAnnounceNextStateChange = delayAnnouncement;
        this.mHost.updateTileState(this.mState, this.mTileSpec);
    }

    protected boolean shouldAnnouncementBeDelayed() {
        return false;
    }

    protected String composeChangeAnnouncement() {
        return null;
    }

    private void handleShowDetail(boolean show) {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            ((Callback) this.mCallbacks.get(i)).onShowDetail(show);
        }
    }

    private void handleToggleStateChanged(boolean state) {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            ((Callback) this.mCallbacks.get(i)).onToggleStateChanged(state);
        }
    }

    private void handleScanStateChanged(boolean state) {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            ((Callback) this.mCallbacks.get(i)).onScanStateChanged(state);
        }
    }

    protected void handleUserSwitch(int newUserId) {
        handleRefreshState(null);
    }

    protected void handleDestroy() {
        setListening(false);
        this.mCallbacks.clear();
    }

    protected void checkIfRestrictionEnforcedByAdminOnly(State state, String userRestriction) {
        EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(this.mContext, userRestriction, UserSwitchUtils.getCurrentUser());
        if (admin == null || RestrictedLockUtils.hasBaseUserRestriction(this.mContext, userRestriction, UserSwitchUtils.getCurrentUser())) {
            state.disabledByPolicy = false;
            state.enforcedAdmin = null;
            return;
        }
        state.disabledByPolicy = true;
        state.enforcedAdmin = admin;
    }
}
