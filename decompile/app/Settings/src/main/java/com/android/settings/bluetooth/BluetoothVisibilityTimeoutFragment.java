package com.android.settings.bluetooth;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import com.android.settings.ItemUseStat;
import com.android.settings.MLog;
import com.android.settingslib.bluetooth.LocalBluetoothManager;

public final class BluetoothVisibilityTimeoutFragment extends DialogFragment implements OnClickListener {
    LocalBluetoothManager mBluetoothManager = Utils.getLocalBtManager(getActivity());
    private BluetoothDiscoverableEnabler mDiscoverableEnabler;

    public BluetoothVisibilityTimeoutFragment() {
        if (this.mBluetoothManager != null) {
            this.mDiscoverableEnabler = (BluetoothDiscoverableEnabler) this.mBluetoothManager.getDiscoverableEnabler();
        } else {
            MLog.e("BluetoothVisibilityTimeoutFragment", "get LocalBluetoothManager return null!");
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getActivity() == null) {
            MLog.e("BluetoothVisibilityTimeoutFragment", "getActivity return null!");
            return null;
        } else if (this.mBluetoothManager == null || this.mDiscoverableEnabler == null) {
            return new Builder(getActivity()).create();
        } else {
            items = new String[4];
            items[0] = getResources().getString(2131628286, new Object[]{Integer.valueOf(2)});
            items[1] = getResources().getString(2131628286, new Object[]{Integer.valueOf(5)});
            items[2] = getResources().getString(2131628287, new Object[]{Integer.valueOf(1)});
            items[3] = getResources().getString(2131628288);
            return new Builder(getActivity()).setTitle(2131624424).setSingleChoiceItems(items, this.mDiscoverableEnabler.getDiscoverableTimeoutIndex(), this).setNegativeButton(17039360, new OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    ItemUseStat.getInstance().handleClick(BluetoothVisibilityTimeoutFragment.this.getActivity(), 2, "detect_timeout_cancel");
                }
            }).create();
        }
    }

    public void onResume() {
        super.onResume();
        if (this.mBluetoothManager == null && getDialog() != null) {
            getDialog().dismiss();
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        String reportStr = null;
        switch (which) {
            case 0:
                reportStr = "detect_timeout_2_minutes";
                break;
            case 1:
                reportStr = "detect_timeout_5_minutes";
                break;
            case 2:
                reportStr = "detect_timeout_1_hours";
                break;
            case 3:
                reportStr = "detect_timeout_never";
                break;
        }
        ItemUseStat.getInstance().handleClick(getActivity(), 1, reportStr);
        this.mDiscoverableEnabler.setDiscoverableTimeout(which);
        dismiss();
    }

    public void onPause() {
        super.onPause();
        ItemUseStat.getInstance().cacheData(getActivity());
    }
}
