package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.widget.Switch;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.statusbar.policy.RotationLockController;
import com.android.systemui.statusbar.policy.RotationLockController.RotationLockControllerCallback;

public class RotationLockTile extends QSTile<BooleanState> {
    private final RotationLockControllerCallback mCallback = new RotationLockControllerCallback() {
        public void onRotationLockStateChanged(boolean rotationLocked, boolean affordanceVisible) {
            RotationLockTile.this.refreshState(Boolean.valueOf(rotationLocked));
        }
    };
    private final RotationLockController mController;
    private final AnimationIcon mEnableRotate = new AnimationIcon(R.drawable.ic_rotation_off2on, R.drawable.ic_rotation_tile_on);
    private final AnimationIcon mPortrait = new AnimationIcon(R.drawable.ic_rotation_on2off, R.drawable.ic_rotation_tile_off);

    public RotationLockTile(Host host) {
        super(host);
        this.mController = host.getRotationLockController();
    }

    public BooleanState newTileState() {
        return new BooleanState();
    }

    public void setListening(boolean listening) {
        if (this.mController != null) {
            if (listening) {
                this.mController.addRotationLockControllerCallback(this.mCallback);
            } else {
                this.mController.removeRotationLockControllerCallback(this.mCallback);
            }
        }
    }

    public Intent getLongClickIntent() {
        return new Intent("android.settings.DISPLAY_SETTINGS");
    }

    protected void handleClick() {
        boolean z = false;
        if (this.mController != null) {
            boolean z2;
            Context context = this.mContext;
            int metricsCategory = getMetricsCategory();
            if (((BooleanState) this.mState).value) {
                z2 = false;
            } else {
                z2 = true;
            }
            MetricsLogger.action(context, metricsCategory, z2);
            boolean newState = !((BooleanState) this.mState).value;
            RotationLockController rotationLockController = this.mController;
            if (!newState) {
                z = true;
            }
            rotationLockController.setRotationLocked(z);
            refreshState(Boolean.valueOf(newState));
        }
    }

    public CharSequence getTileLabel() {
        return ((BooleanState) getState()).label;
    }

    protected void handleUpdateState(BooleanState state, Object arg) {
        int i = 1;
        if (this.mController != null) {
            boolean z;
            boolean rotationLocked = this.mController.isRotationLocked();
            if (rotationLocked) {
                z = false;
            } else {
                z = true;
            }
            state.value = z;
            state.label = this.mContext.getString(R.string.quick_settings_rotation_unlocked_label);
            state.icon = !rotationLocked ? this.mEnableRotate : this.mPortrait;
            state.textChangedDelay = 400;
            if (rotationLocked) {
                i = 0;
            }
            state.labelTint = i;
            state.contentDescription = getAccessibilityString(rotationLocked);
            String name = Switch.class.getName();
            state.expandedAccessibilityClassName = name;
            state.minimalAccessibilityClassName = name;
        }
    }

    public static boolean isCurrentOrientationLockPortrait(RotationLockController controller, Context context) {
        boolean z = true;
        int lockOrientation = controller.getRotationLockOrientation();
        if (lockOrientation == 0) {
            if (context.getResources().getConfiguration().orientation == 2) {
                z = false;
            }
            return z;
        }
        if (lockOrientation == 2) {
            z = false;
        }
        return z;
    }

    public int getMetricsCategory() {
        return 123;
    }

    private String getAccessibilityString(boolean locked) {
        if (!locked) {
            return this.mContext.getString(R.string.accessibility_quick_settings_rotation);
        }
        String string;
        StringBuilder append = new StringBuilder().append(this.mContext.getString(R.string.accessibility_quick_settings_rotation)).append(",");
        Context context = this.mContext;
        Object[] objArr = new Object[1];
        if (isCurrentOrientationLockPortrait(this.mController, this.mContext)) {
            string = this.mContext.getString(R.string.quick_settings_rotation_locked_portrait_label);
        } else {
            string = this.mContext.getString(R.string.quick_settings_rotation_locked_landscape_label);
        }
        objArr[0] = string;
        return append.append(context.getString(R.string.accessibility_quick_settings_rotation_value, objArr)).toString();
    }

    protected String composeChangeAnnouncement() {
        return getAccessibilityString(((BooleanState) this.mState).value);
    }
}
