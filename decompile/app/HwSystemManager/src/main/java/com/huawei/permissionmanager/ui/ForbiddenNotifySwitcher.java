package com.huawei.permissionmanager.ui;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import com.huawei.permissionmanager.db.DBAdapter;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.widget.CommonSwitcher.SimpleSwitcher;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.util.HwLog;

public class ForbiddenNotifySwitcher extends SimpleSwitcher implements OnClickListener {
    private static final String TAG = "ForbiddenNotifySwitcher";
    private final View mContainerLayout;
    private Switch mToastSwitch;
    private OnCheckedChangeListener mToastSwitchChangeListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                DBAdapter.setToastSwitchStatus(ForbiddenNotifySwitcher.this.getContext(), 0);
                String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, "1");
                HsmStat.statE(41, statParam);
                return;
            }
            DialogListener listener = new DialogListener();
            new Builder(ForbiddenNotifySwitcher.this.getContext()).setTitle(R.string.Title_PermissionManager_Remind).setMessage(R.string.Other_PermissionManager_Remind).setNegativeButton(R.string.cancel, listener).setPositiveButton(R.string.confirm, listener).setOnDismissListener(listener).create().show();
        }
    };

    private class DialogListener implements DialogInterface.OnClickListener, OnDismissListener {
        private int mClick;

        private DialogListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            this.mClick = which;
            if (which == -1) {
                DBAdapter.setToastSwitchStatus(ForbiddenNotifySwitcher.this.getContext(), 1);
                String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, "0");
                HsmStat.statE(41, statParam);
            }
        }

        public void onDismiss(DialogInterface dialog) {
            if (this.mClick != -1) {
                ForbiddenNotifySwitcher.this.mToastSwitch.setOnCheckedChangeListener(null);
                ForbiddenNotifySwitcher.this.mToastSwitch.setChecked(true);
                ForbiddenNotifySwitcher.this.mToastSwitch.setOnCheckedChangeListener(ForbiddenNotifySwitcher.this.mToastSwitchChangeListener);
            }
        }
    }

    public ForbiddenNotifySwitcher(View container) {
        super(container.getContext());
        this.mContainerLayout = container;
    }

    public void init() {
        if (this.mContainerLayout == null) {
            HwLog.e(TAG, "ForbiddenNotifySwitcher mContainerLayout is null!");
            return;
        }
        ((TextView) this.mContainerLayout.findViewById(ViewUtil.HWID_TEXT_1)).setText(R.string.ListViewFirstLine_PermissionManager_Remind);
        TextView toastDescription = (TextView) this.mContainerLayout.findViewById(ViewUtil.HWID_TEXT_2);
        toastDescription.setSingleLine(false);
        toastDescription.setText(R.string.ListViewSecondLine_AppPermissionForbidden_Tips);
        this.mToastSwitch = (Switch) this.mContainerLayout.findViewById(R.id.switcher);
        this.mContainerLayout.setOnClickListener(this);
        this.mToastSwitch.setOnCheckedChangeListener(this.mToastSwitchChangeListener);
    }

    public void onClick(View v) {
        this.mToastSwitch.performClick();
    }

    public void refreshState() {
        this.mToastSwitch.setOnClickListener(null);
        this.mToastSwitch.setChecked(DBAdapter.getToastSwitchOpenStatus(getContext()));
        this.mToastSwitch.setOnCheckedChangeListener(this.mToastSwitchChangeListener);
    }
}
