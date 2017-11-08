package com.android.systemui.qs.tiles;

import android.app.KeyguardManager;
import android.app.StatusBarManager;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.DrawableIcon;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.qs.QSTile.State;
import com.android.systemui.utils.HwLog;

public class ScreenrecorderTile extends QSTile<State> {
    public ScreenrecorderTile(Host host) {
        super(host);
    }

    public State newTileState() {
        return new State();
    }

    protected void handleClick() {
        if (((KeyguardManager) this.mContext.getSystemService("keyguard")).inKeyguardRestrictedInputMode()) {
            HwLog.i(this.TAG, "handleClick::inKeyguardRestrictedInputMode");
            return;
        }
        ((StatusBarManager) this.mContext.getSystemService("statusbar")).collapsePanels();
        Intent intent = new Intent();
        intent.setAction("com.huawei.screenrecorder.Start");
        intent.setClassName("com.huawei.screenrecorder", "com.huawei.screenrecorder.ScreenRecordService");
        try {
            HwLog.i(this.TAG, "start service");
            this.mContext.startServiceAsUser(intent, UserHandle.CURRENT_OR_SELF);
        } catch (Exception e) {
            HwLog.e(this.TAG, "handleClick:: can't start screenrecorder:" + e);
        }
    }

    protected void handleUpdateState(State state, Object arg) {
        Drawable icon = this.mContext.getDrawable(R.drawable.ic_screenrecorder_tile_on);
        state.label = this.mContext.getString(R.string.screenrecorder_start_widget_name);
        state.icon = new DrawableIcon(icon);
    }

    public int getMetricsCategory() {
        return 268;
    }

    public Intent getLongClickIntent() {
        return null;
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.screenrecorder_start_widget_name);
    }

    public void setListening(boolean listening) {
    }

    public boolean isAvailable() {
        try {
            this.mContext.getPackageManager().getPackageInfo("com.huawei.screenrecorder", 128);
            return true;
        } catch (NameNotFoundException e) {
            HwLog.e(this.TAG, "isAvailable::package name is not found in phone.");
            return false;
        }
    }
}
