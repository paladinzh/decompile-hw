package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.statusbar.policy.SoundVibrationController;
import com.android.systemui.statusbar.policy.SoundVibrationController.RingModeChangeCallback;

public class VibrationTile extends QSTile<BooleanState> implements RingModeChangeCallback {
    private final AnimationIcon mDisable = new AnimationIcon(R.drawable.ic_vibration_on2off, R.drawable.ic_vibration_tile_off);
    private final AnimationIcon mEnable = new AnimationIcon(R.drawable.ic_vibration_off2on, R.drawable.ic_vibration_tile_on);
    private SoundVibrationController mRingModeController;

    public VibrationTile(Host host) {
        super(host);
        this.mRingModeController = host.getSoundVibrationController();
    }

    protected void handleUpdateState(BooleanState state, Object arg) {
        int i;
        boolean vibration = arg != null ? ((Boolean) arg).booleanValue() : this.mRingModeController.isVibrationEnable();
        state.value = vibration;
        state.label = this.mContext.getString(R.string.vibration_widget_name);
        state.labelTint = vibration ? 1 : 0;
        state.icon = vibration ? this.mEnable : this.mDisable;
        if (vibration) {
            i = 400;
        } else {
            i = 83;
        }
        state.textChangedDelay = (long) i;
    }

    public int getMetricsCategory() {
        return 268;
    }

    public Intent getLongClickIntent() {
        return new Intent("com.android.settings.SOUND_SETTINGS").setPackage("com.android.settings");
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.vibration_widget_name);
    }

    public BooleanState newTileState() {
        return new BooleanState();
    }

    public void setListening(boolean listening) {
        if (listening) {
            this.mRingModeController.addRingModeChangedCallback(this);
        } else {
            this.mRingModeController.removeRingModeChangedCallback(this);
        }
    }

    protected void handleClick() {
        boolean z;
        Context context = this.mContext;
        int metricsCategory = getMetricsCategory();
        if (((BooleanState) this.mState).value) {
            z = false;
        } else {
            z = true;
        }
        MetricsLogger.action(context, metricsCategory, z);
        boolean newState = !((BooleanState) this.mState).value;
        this.mRingModeController.setVibrationState(newState);
        int ringMode = this.mRingModeController.getRingMode();
        if (newState) {
            if (ringMode == 0) {
                this.mRingModeController.setRingMode(1);
            }
        } else if (1 == ringMode) {
            this.mRingModeController.setRingMode(0);
        }
        refreshState(Boolean.valueOf(newState));
    }

    public void onRingModeChanged(int ringMode, boolean vibrationEnable) {
        refreshState(Boolean.valueOf(vibrationEnable));
    }

    public boolean isAvailable() {
        return false;
    }
}
