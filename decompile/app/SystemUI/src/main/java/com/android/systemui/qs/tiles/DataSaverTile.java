package com.android.systemui.qs.tiles;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.widget.Switch;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Prefs;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.qs.QSTile.ResourceIcon;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.statusbar.policy.DataSaverController;
import com.android.systemui.statusbar.policy.DataSaverController.Listener;

public class DataSaverTile extends QSTile<BooleanState> implements Listener {
    private final DataSaverController mDataSaverController;

    public DataSaverTile(Host host) {
        super(host);
        this.mDataSaverController = host.getNetworkController().getDataSaverController();
    }

    public BooleanState newTileState() {
        return new BooleanState();
    }

    public void setListening(boolean listening) {
        if (listening) {
            this.mDataSaverController.addListener(this);
        } else {
            this.mDataSaverController.remListener(this);
        }
    }

    public Intent getLongClickIntent() {
        return CellularTile.CELLULAR_SETTINGS;
    }

    protected void handleClick() {
        if (((BooleanState) this.mState).value || Prefs.getBoolean(this.mContext, "QsDataSaverDialogShown", false)) {
            toggleDataSaver();
            return;
        }
        SystemUIDialog dialog = new SystemUIDialog(this.mContext);
        dialog.setTitle(17040887);
        dialog.setMessage(17040886);
        dialog.setPositiveButton(17040888, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                DataSaverTile.this.toggleDataSaver();
            }
        });
        dialog.setNegativeButton(17039360, null);
        dialog.setShowForAllUsers(true);
        dialog.show();
        Prefs.putBoolean(this.mContext, "QsDataSaverDialogShown", true);
    }

    private void toggleDataSaver() {
        ((BooleanState) this.mState).value = !this.mDataSaverController.isDataSaverEnabled();
        MetricsLogger.action(this.mContext, getMetricsCategory(), ((BooleanState) this.mState).value);
        this.mDataSaverController.setDataSaverEnabled(((BooleanState) this.mState).value);
        refreshState(Boolean.valueOf(((BooleanState) this.mState).value));
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.data_saver);
    }

    protected void handleUpdateState(BooleanState state, Object arg) {
        boolean booleanValue;
        int i;
        if (arg instanceof Boolean) {
            booleanValue = ((Boolean) arg).booleanValue();
        } else {
            booleanValue = this.mDataSaverController.isDataSaverEnabled();
        }
        state.value = booleanValue;
        state.label = this.mContext.getString(R.string.data_saver);
        state.contentDescription = state.label;
        if (state.value) {
            i = R.drawable.ic_data_saver;
        } else {
            i = R.drawable.ic_data_saver_off;
        }
        state.icon = ResourceIcon.get(i);
        String name = Switch.class.getName();
        state.expandedAccessibilityClassName = name;
        state.minimalAccessibilityClassName = name;
    }

    public int getMetricsCategory() {
        return 284;
    }

    protected String composeChangeAnnouncement() {
        if (((BooleanState) this.mState).value) {
            return this.mContext.getString(R.string.accessibility_quick_settings_data_saver_changed_on);
        }
        return this.mContext.getString(R.string.accessibility_quick_settings_data_saver_changed_off);
    }

    public void onDataSaverChanged(boolean isDataSaving) {
        refreshState(Boolean.valueOf(isDataSaving));
    }

    public boolean isAvailable() {
        return false;
    }
}
