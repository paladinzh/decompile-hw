package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.widget.Switch;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSTile.DrawableIcon;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.statusbar.phone.ManagedProfileController;
import com.android.systemui.statusbar.phone.ManagedProfileController.Callback;

public class WorkModeTile extends QSTile<BooleanState> implements Callback {
    private final AnimationIcon mDisable = new AnimationIcon(R.drawable.ic_signal_workmode_disable_animation, R.drawable.ic_signal_workmode_enable);
    private final AnimationIcon mEnable = new AnimationIcon(R.drawable.ic_signal_workmode_enable_animation, R.drawable.ic_signal_workmode_disable);
    private final ManagedProfileController mProfileController;

    public WorkModeTile(Host host) {
        super(host);
        this.mProfileController = host.getManagedProfileController();
    }

    public BooleanState newTileState() {
        return new BooleanState();
    }

    public void setListening(boolean listening) {
        if (listening) {
            this.mProfileController.addCallback(this);
        } else {
            this.mProfileController.removeCallback(this);
        }
    }

    public Intent getLongClickIntent() {
        return new Intent("android.settings.SYNC_SETTINGS");
    }

    public void handleClick() {
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
        ManagedProfileController managedProfileController = this.mProfileController;
        if (((BooleanState) this.mState).value) {
            z = false;
        } else {
            z = true;
        }
        managedProfileController.setWorkModeEnabled(z);
        if (((BooleanState) this.mState).value) {
            newState = false;
        } else {
            newState = true;
        }
        refreshState(Boolean.valueOf(newState));
    }

    public boolean isAvailable() {
        return this.mProfileController.hasActiveProfile();
    }

    public void onManagedProfileChanged() {
        refreshState(Boolean.valueOf(this.mProfileController.isWorkModeEnabled()));
    }

    public void onManagedProfileRemoved() {
        this.mHost.removeTile(getTileSpec());
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_work_mode_label);
    }

    protected void handleUpdateState(BooleanState state, Object arg) {
        if (arg instanceof Boolean) {
            state.value = ((Boolean) arg).booleanValue();
        } else {
            state.value = this.mProfileController.isWorkModeEnabled();
        }
        state.label = this.mContext.getString(R.string.quick_settings_work_mode_label);
        state.labelTint = state.value ? 1 : 0;
        Drawable icon = this.mContext.getDrawable(R.drawable.ic_workmode_tile);
        if (state.value) {
            icon.setTint(this.mContext.getColor(R.color.qs_tile_tint_on));
            state.contentDescription = this.mContext.getString(R.string.accessibility_quick_settings_work_mode_on);
        } else {
            icon.setTint(this.mContext.getColor(R.color.qs_tile_tint_off));
            state.contentDescription = this.mContext.getString(R.string.accessibility_quick_settings_work_mode_off);
        }
        state.icon = new DrawableIcon(icon);
        String name = Switch.class.getName();
        state.expandedAccessibilityClassName = name;
        state.minimalAccessibilityClassName = name;
    }

    public int getMetricsCategory() {
        return 257;
    }

    protected String composeChangeAnnouncement() {
        if (((BooleanState) this.mState).value) {
            return this.mContext.getString(R.string.accessibility_quick_settings_work_mode_changed_on);
        }
        return this.mContext.getString(R.string.accessibility_quick_settings_work_mode_changed_off);
    }
}
