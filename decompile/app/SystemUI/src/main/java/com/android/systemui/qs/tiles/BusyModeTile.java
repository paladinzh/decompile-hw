package com.android.systemui.qs.tiles;

import android.app.StatusBarManager;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.widget.Switch;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSTile.DrawableIcon;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUiUtil;
import com.android.systemui.utils.UserSwitchUtils;

public class BusyModeTile extends QSTile<BooleanState> {
    private ContentObserver mStateChangedObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            BusyModeTile.this.refreshState();
        }
    };

    public BusyModeTile(Host host) {
        super(host);
    }

    public BooleanState newTileState() {
        return new BooleanState();
    }

    protected void handleClick() {
        boolean newState;
        int i;
        if (((BooleanState) this.mState).value) {
            newState = false;
        } else {
            newState = true;
        }
        if (newState) {
            ((StatusBarManager) this.mContext.getSystemService("statusbar")).collapsePanels();
        }
        Intent intent = new Intent("com.huawei.android.preventmode.change");
        String str = "PreventModechange";
        if (newState) {
            i = 1;
        } else {
            i = 0;
        }
        intent.putExtra(str, i);
        intent.putExtra("package_name", "com.android.toolbox");
        intent.setPackage("com.android.settings");
        this.mContext.sendBroadcast(intent);
        refreshState();
    }

    protected void handleUpdateState(BooleanState state, Object arg) {
        Drawable icon;
        boolean isBusyModeOn = arg != null ? ((Boolean) arg).booleanValue() : isBusyModeOn();
        if (isBusyModeOn) {
            icon = this.mHost.getContext().getDrawable(R.drawable.ic_busymode_tile_on);
        } else {
            icon = this.mHost.getContext().getDrawable(R.drawable.ic_busymode_tile_off);
        }
        state.label = this.mContext.getString(R.string.busymode_widget_name);
        state.labelTint = isBusyModeOn ? 1 : 0;
        state.value = isBusyModeOn;
        state.icon = new DrawableIcon(icon);
        state.contentDescription = this.mContext.getString(R.string.busymode_widget_name);
        String name = Switch.class.getName();
        state.expandedAccessibilityClassName = name;
        state.minimalAccessibilityClassName = name;
    }

    public int getMetricsCategory() {
        return 268;
    }

    public Intent getLongClickIntent() {
        return new Intent("android.settings.ZEN_MODE_SETTINGS");
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.busymode_widget_name);
    }

    public void setListening(boolean listening) {
        if (listening) {
            registerObserver();
        } else {
            unregisterObserver();
        }
    }

    public boolean isAvailable() {
        if (!SystemUiUtil.isWifiOnly(this.mContext) && SystemUiUtil.isVoiceCapable(this.mContext) && UserSwitchUtils.getCurrentUser() == 0) {
            return true;
        }
        return false;
    }

    private boolean isBusyModeOn() {
        int currentMode = 0;
        try {
            currentMode = Global.getInt(this.mContext.getContentResolver(), "zen_mode");
        } catch (SettingNotFoundException e) {
            HwLog.e(this.TAG, "Get current zen mode fail, default mode is OFF");
        }
        if (currentMode != 0) {
            return true;
        }
        return false;
    }

    private void registerObserver() {
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("zen_mode"), true, this.mStateChangedObserver, -1);
    }

    private void unregisterObserver() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mStateChangedObserver);
    }

    protected String composeChangeAnnouncement() {
        if (this.mState.value) {
            return this.mContext.getString(R.string.accessibility_quick_settings_busy_mode_changed_on);
        }
        return this.mContext.getString(R.string.accessibility_quick_settings_busy_mode_changed_off);
    }
}
