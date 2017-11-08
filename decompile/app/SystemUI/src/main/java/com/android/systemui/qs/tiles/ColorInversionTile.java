package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.widget.Switch;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.qs.SecureSetting;

public class ColorInversionTile extends QSTile<BooleanState> {
    private final AnimationIcon mDisable = new AnimationIcon(R.drawable.ic_invert_colors_disable_animation, R.drawable.ic_invert_colors_enable);
    private final AnimationIcon mEnable = new AnimationIcon(R.drawable.ic_invert_colors_enable_animation, R.drawable.ic_invert_colors_disable);
    private final SecureSetting mSetting = new SecureSetting(this.mContext, this.mHandler, "accessibility_display_inversion_enabled") {
        protected void handleValueChanged(int value, boolean observedChange) {
            ColorInversionTile.this.handleRefreshState(Integer.valueOf(value));
        }
    };

    public ColorInversionTile(Host host) {
        super(host);
    }

    protected void handleDestroy() {
        super.handleDestroy();
        this.mSetting.setListening(false);
    }

    public BooleanState newTileState() {
        return new BooleanState();
    }

    public void setListening(boolean listening) {
        this.mSetting.setListening(listening);
    }

    protected void handleUserSwitch(int newUserId) {
        this.mSetting.setUserId(newUserId);
        handleRefreshState(Integer.valueOf(this.mSetting.getValue()));
    }

    public Intent getLongClickIntent() {
        return new Intent("android.settings.ACCESSIBILITY_SETTINGS");
    }

    protected void handleClick() {
        boolean z;
        int i = 0;
        Context context = this.mContext;
        int metricsCategory = getMetricsCategory();
        if (((BooleanState) this.mState).value) {
            z = false;
        } else {
            z = true;
        }
        MetricsLogger.action(context, metricsCategory, z);
        SecureSetting secureSetting = this.mSetting;
        if (!((BooleanState) this.mState).value) {
            i = 1;
        }
        secureSetting.setValue(i);
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_inversion_label);
    }

    protected void handleUpdateState(BooleanState state, Object arg) {
        boolean enabled = false;
        if ((arg instanceof Integer ? ((Integer) arg).intValue() : this.mSetting.getValue()) != 0) {
            enabled = true;
        }
        state.value = enabled;
        state.label = this.mContext.getString(R.string.quick_settings_inversion_label);
        state.icon = enabled ? this.mEnable : this.mDisable;
        String name = Switch.class.getName();
        state.expandedAccessibilityClassName = name;
        state.minimalAccessibilityClassName = name;
        state.contentDescription = state.label;
    }

    public int getMetricsCategory() {
        return 116;
    }

    protected String composeChangeAnnouncement() {
        if (((BooleanState) this.mState).value) {
            return this.mContext.getString(R.string.accessibility_quick_settings_color_inversion_changed_on);
        }
        return this.mContext.getString(R.string.accessibility_quick_settings_color_inversion_changed_off);
    }

    public boolean isAvailable() {
        return false;
    }
}
