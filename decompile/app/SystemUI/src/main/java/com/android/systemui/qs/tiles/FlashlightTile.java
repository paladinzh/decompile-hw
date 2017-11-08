package com.android.systemui.qs.tiles;

import android.app.ActivityManager;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Switch;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R;
import com.android.systemui.SysUIToast;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSTile.DrawableIcon;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.statusbar.phone.HwPhoneStatusBar;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.huawei.keyguard.inf.IFlashlightController.FlashlightListener;

public class FlashlightTile extends QSTile<BooleanState> implements FlashlightListener {
    private Context mContext;
    private final AnimationIcon mDisable = new AnimationIcon(R.drawable.ic_flashlight_on2off, R.drawable.ic_flashlight_tile_off);
    private final AnimationIcon mEnable = new AnimationIcon(R.drawable.ic_flashlight_off2on, R.drawable.ic_flashlight_tile_on);
    private final FlashlightController mFlashlightController;

    public FlashlightTile(Host host) {
        super(host);
        this.mFlashlightController = host.getFlashlightController();
        this.mFlashlightController.addListener(this);
        this.mContext = host.getContext();
        this.mFlashlightController.addRegister(this.mContext);
    }

    protected void handleDestroy() {
        super.handleDestroy();
        this.mFlashlightController.removeListener(this);
        this.mFlashlightController.removeRegister(this.mContext);
    }

    public BooleanState newTileState() {
        return new BooleanState();
    }

    public void setListening(boolean listening) {
    }

    protected void handleUserSwitch(int newUserId) {
    }

    public Intent getLongClickIntent() {
        return null;
    }

    public boolean isAvailable() {
        return this.mFlashlightController.hasFlashlight();
    }

    protected void handleClick() {
        if (!ActivityManager.isUserAMonkey()) {
            if (!this.mFlashlightController.isAvailable()) {
                HwLog.i(this.TAG, "flashlight not available, do not allowed click event");
            } else if (((BooleanState) this.mState).value || HwPhoneStatusBar.getInstance().canOpenFlashLight()) {
                boolean z;
                boolean newState;
                Context context = this.mContext;
                int metricsCategory = getMetricsCategory();
                if (((BooleanState) this.mState).value) {
                    z = false;
                } else {
                    z = true;
                }
                MetricsLogger.action(context, metricsCategory, z);
                if (((BooleanState) this.mState).value) {
                    newState = false;
                } else {
                    newState = true;
                }
                refreshState(Boolean.valueOf(newState));
                SystemUIThread.runAsync(new SimpleAsyncTask() {
                    public boolean runInThread() {
                        FlashlightTile.this.mFlashlightController.setFlashlight(newState);
                        return false;
                    }
                });
            } else {
                ((StatusBarManager) this.mContext.getSystemService("statusbar")).collapsePanels();
                SysUIToast.makeText(this.mContext, this.mContext.getString(R.string.systemui_flashlight_not_open), 0).show();
            }
        }
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_flashlight_label);
    }

    protected void handleLongClick() {
    }

    protected void handleUpdateState(BooleanState state, Object arg) {
        state.label = this.mHost.getContext().getString(R.string.quick_settings_flashlight_label);
        if (this.mFlashlightController.isAvailable()) {
            if (arg instanceof Boolean) {
                boolean value = ((Boolean) arg).booleanValue();
                if (value != state.value) {
                    state.value = value;
                } else {
                    return;
                }
            }
            state.value = this.mFlashlightController.isEnabled();
            state.icon = state.value ? this.mEnable : this.mDisable;
            state.labelTint = state.value ? 1 : 0;
            state.textChangedDelay = (long) (state.value ? 217 : 83);
            state.contentDescription = this.mContext.getString(R.string.quick_settings_flashlight_label);
            String name = Switch.class.getName();
            state.expandedAccessibilityClassName = name;
            state.minimalAccessibilityClassName = name;
            return;
        }
        state.icon = new DrawableIcon(this.mContext.getDrawable(R.drawable.ic_flashlight_tile_disable));
        state.contentDescription = this.mContext.getString(R.string.accessibility_quick_settings_flashlight_unavailable);
        state.labelTint = 2;
    }

    public int getMetricsCategory() {
        return 119;
    }

    protected String composeChangeAnnouncement() {
        if (((BooleanState) this.mState).value) {
            return this.mContext.getString(R.string.accessibility_quick_settings_flashlight_changed_on);
        }
        return this.mContext.getString(R.string.accessibility_quick_settings_flashlight_changed_off);
    }

    public void onFlashlightChanged(boolean enabled) {
        refreshState(Boolean.valueOf(enabled));
    }

    public void onFlashlightError() {
        refreshState(Boolean.valueOf(false));
    }

    public void onFlashlightAvailabilityChanged(boolean available) {
        refreshState();
    }
}
