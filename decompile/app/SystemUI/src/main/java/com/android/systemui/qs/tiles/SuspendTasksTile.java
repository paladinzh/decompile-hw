package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Switch;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSTile.DrawableIcon;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUiUtil;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;

public class SuspendTasksTile extends QSTile<BooleanState> {
    public static final Uri URI = Uri.parse("content://com.huawei.android.FloatTasksContentProvider");
    private final AnimationIcon mDisable = new AnimationIcon(R.drawable.ic_suspendtasks_on2off, R.drawable.ic_suspendtasks_tile_off);
    private final AnimationIcon mEnable = new AnimationIcon(R.drawable.ic_suspendtasks_off2on, R.drawable.ic_suspendtasks_tile_on);

    public SuspendTasksTile(Host host) {
        super(host);
    }

    public BooleanState newTileState() {
        return new BooleanState();
    }

    protected void handleClick() {
        if (!isTileDisabled()) {
            boolean newState = !((BooleanState) this.mState).value;
            try {
                SystemUiUtil.getContextCurrentUser(this.mContext).getContentResolver().call(URI, "set", String.valueOf(newState ? 1 : 0), null);
            } catch (Exception e) {
                HwLog.e("SuspendTasksTile", "handleClick::call method set exception:" + e);
            }
            refreshState(Boolean.valueOf(newState));
        }
    }

    protected void handleUpdateState(BooleanState state, Object arg) {
        state.label = this.mContext.getString(R.string.suspend_button_widget_name);
        if (isTileDisabled()) {
            state.labelTint = 2;
            state.icon = new DrawableIcon(this.mContext.getResources().getDrawable(R.drawable.ic_suspendtasks_tile_disable));
            state.textChangedDelay = 0;
            return;
        }
        boolean isSuspendOn = arg != null ? ((Boolean) arg).booleanValue() : isSuspendOn();
        state.label = this.mContext.getString(R.string.suspend_button_widget_name);
        state.labelTint = isSuspendOn ? 1 : 0;
        state.value = isSuspendOn;
        state.icon = isSuspendOn ? this.mEnable : this.mDisable;
        state.textChangedDelay = (long) (isSuspendOn ? 250 : 83);
        state.contentDescription = this.mContext.getString(R.string.suspend_button_widget_name);
        String name = Switch.class.getName();
        state.expandedAccessibilityClassName = name;
        state.minimalAccessibilityClassName = name;
    }

    public int getMetricsCategory() {
        return 268;
    }

    public Intent getLongClickIntent() {
        if (isTileDisabled()) {
            return null;
        }
        return new Intent("com.android.settings.SUSPEND_BUTTON_SETTINGS").setPackage("com.android.settings");
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.suspend_button_widget_name);
    }

    public void setListening(boolean listening) {
    }

    public boolean isAvailable() {
        boolean packExist = SystemUiUtil.isPackageExist(this.mContext, "com.huawei.android.FloatTasks");
        HwLog.i("SuspendTasksTile", "isAvailable:: suspend is available=" + packExist);
        return packExist;
    }

    private boolean isSuspendOn() {
        boolean z = true;
        Bundle bundle = SystemUiUtil.getContextCurrentUser(this.mContext).getContentResolver().call(URI, "get", null, null);
        if (bundle != null) {
            int state = bundle.getInt("float_task_state", -1);
            HwLog.i("SuspendTasksTile", "isSuspendOn::get current state, state=" + state);
            if (1 != state) {
                z = false;
            }
            return z;
        }
        HwLog.i("SuspendTasksTile", "isSuspendOn::get current state bundle is null");
        return false;
    }

    private boolean isTileDisabled() {
        if (!HwKeyguardUpdateMonitor.getInstance(this.mContext).isRestrictAsEncrypt()) {
            return false;
        }
        HwLog.i("SuspendTasksTile", "now isRestrictAsEncrypt, suspend tile disabled");
        return true;
    }

    protected String composeChangeAnnouncement() {
        if (this.mState.value) {
            return this.mContext.getString(R.string.accessibility_quick_settings_suspend_task_changed_on);
        }
        return this.mContext.getString(R.string.accessibility_quick_settings_suspend_task_changed_off);
    }
}
