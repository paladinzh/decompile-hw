package com.android.systemui.tuner;

import android.content.Intent;
import com.android.systemui.Prefs;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.qs.QSTile.ResourceIcon;
import com.android.systemui.qs.QSTile.State;
import com.android.systemui.statusbar.policy.NightModeController;
import com.android.systemui.statusbar.policy.NightModeController.Listener;

public class NightModeTile extends QSTile<State> implements Listener {
    private final NightModeController mNightModeController;

    public NightModeTile(Host host) {
        super(host);
        this.mNightModeController = host.getNightModeController();
    }

    public boolean isAvailable() {
        if (Prefs.getBoolean(this.mContext, "QsNightAdded", false)) {
            return TunerService.isTunerEnabled(this.mContext);
        }
        return false;
    }

    public void setListening(boolean listening) {
        if (listening) {
            this.mNightModeController.addListener(this);
            refreshState();
            return;
        }
        this.mNightModeController.removeListener(this);
    }

    public State newTileState() {
        return new State();
    }

    public Intent getLongClickIntent() {
        return new Intent(this.mContext, TunerActivity.class).putExtra("show_night_mode", true);
    }

    protected void handleClick() {
        this.mNightModeController.setNightMode(!this.mNightModeController.isEnabled());
        refreshState();
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.night_mode);
    }

    protected void handleUpdateState(State state, Object arg) {
        int i;
        if (this.mNightModeController.isEnabled()) {
            i = R.drawable.ic_night_mode;
        } else {
            i = R.drawable.ic_night_mode_disabled;
        }
        state.icon = ResourceIcon.get(i);
        state.label = this.mContext.getString(R.string.night_mode);
        state.contentDescription = this.mContext.getString(R.string.night_mode);
    }

    public void onNightModeChanged() {
        refreshState();
    }

    public int getMetricsCategory() {
        return 267;
    }
}
