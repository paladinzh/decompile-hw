package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.widget.Switch;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.DrawableIcon;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.qs.QSTile.RingModeState;
import com.android.systemui.statusbar.policy.SoundVibrationController;
import com.android.systemui.statusbar.policy.SoundVibrationController.RingModeChangeCallback;
import com.android.systemui.utils.analyze.BDReporter;

public class SoundSilentTile extends QSTile<RingModeState> implements RingModeChangeCallback {
    private SoundVibrationController mRingModeController;

    public SoundSilentTile(Host host) {
        super(host);
        this.mRingModeController = host.getSoundVibrationController();
    }

    protected void handleUpdateState(RingModeState state, Object arg) {
        int ringMode = arg != null ? ((Integer) arg).intValue() : this.mRingModeController.getRingMode();
        state.label = getLabelString(ringMode);
        state.value = true;
        state.labelTint = 1;
        state.mode = ringMode;
        state.icon = new DrawableIcon(getIconDrawable(ringMode));
        state.textChangedDelay = 0;
        state.contentDescription = state.label;
        String name = Switch.class.getName();
        state.expandedAccessibilityClassName = name;
        state.minimalAccessibilityClassName = name;
    }

    public int getMetricsCategory() {
        return 268;
    }

    public Intent getLongClickIntent() {
        return new Intent("com.android.settings.SOUND_SETTINGS").setPackage("com.android.settings");
    }

    public RingModeState newTileState() {
        return new RingModeState();
    }

    public void setListening(boolean listening) {
        if (listening) {
            this.mRingModeController.addRingModeChangedCallback(this);
        } else {
            this.mRingModeController.removeRingModeChangedCallback(this);
        }
    }

    public CharSequence getTileLabel() {
        return ((RingModeState) getState()).label;
    }

    protected void handleClick() {
        boolean z;
        int newState;
        Context context = this.mContext;
        int metricsCategory = getMetricsCategory();
        if (((RingModeState) this.mState).value) {
            z = false;
        } else {
            z = true;
        }
        MetricsLogger.action(context, metricsCategory, z);
        if (2 == ((RingModeState) this.mState).mode) {
            newState = 1;
        } else if (1 == ((RingModeState) this.mState).mode) {
            newState = 0;
        } else {
            newState = 2;
        }
        ((RingModeState) this.mState).mode = newState;
        BDReporter.e(this.mContext, 370, "newState=" + newState);
        this.mRingModeController.setRingMode(newState);
        refreshState(Integer.valueOf(newState));
    }

    public void onRingModeChanged(int ringMode, boolean vibrationEnable) {
        ((RingModeState) this.mState).mode = ringMode;
        refreshState(Integer.valueOf(ringMode));
    }

    private String getLabelString(int ringMode) {
        switch (ringMode) {
            case 0:
                return this.mContext.getString(R.string.silent_widget_name);
            case 1:
                return this.mContext.getString(R.string.vibration_widget_name);
            case 2:
                return this.mContext.getString(R.string.sound_widget_name);
            default:
                return this.mContext.getString(R.string.sound_widget_name);
        }
    }

    private Drawable getIconDrawable(int ringMode) {
        switch (ringMode) {
            case 0:
                return this.mContext.getDrawable(R.drawable.ic_soundsilent_tile_silent);
            case 1:
                return this.mContext.getDrawable(R.drawable.ic_soundsilent_tile_vibration);
            case 2:
                return this.mContext.getDrawable(R.drawable.ic_soundsilent_tile_normal);
            default:
                return this.mContext.getDrawable(R.drawable.ic_soundsilent_tile_normal);
        }
    }

    protected String composeChangeAnnouncement() {
        RingModeState state = this.mState;
        if (2 == state.mode) {
            return this.mContext.getString(R.string.accessibility_quick_settings_ringer_mode_normal_changed_on);
        }
        if (1 == state.mode) {
            return this.mContext.getString(R.string.accessibility_quick_settings_ringer_mode_vibrate_changed_on);
        }
        return this.mContext.getString(R.string.accessibility_quick_settings_ringer_mode_silent_changed_on);
    }
}
