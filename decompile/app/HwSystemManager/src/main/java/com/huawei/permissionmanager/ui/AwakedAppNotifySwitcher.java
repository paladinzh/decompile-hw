package com.huawei.permissionmanager.ui;

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
import com.huawei.systemmanager.util.HwLog;

public class AwakedAppNotifySwitcher extends SimpleSwitcher implements OnClickListener {
    private static final String TAG = "AwakedAppNotifySwitcher";
    private final View mContainerLayout;
    private Switch mToastSwitch;
    private OnCheckedChangeListener mToastSwitchChangeListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            DBAdapter.setAwakedAppNotifySwitchStatus(AwakedAppNotifySwitcher.this.getContext(), isChecked ? 0 : 1);
        }
    };

    public AwakedAppNotifySwitcher(View container) {
        super(container.getContext());
        this.mContainerLayout = container;
    }

    public void init() {
        if (this.mContainerLayout == null) {
            HwLog.e(TAG, "AwakedAppNotifySwitcher mContainerLayout is null!");
            return;
        }
        ((TextView) this.mContainerLayout.findViewById(ViewUtil.HWID_TEXT_1)).setText(R.string.startupmgr_awaked_remind);
        TextView toastDescription = (TextView) this.mContainerLayout.findViewById(ViewUtil.HWID_TEXT_2);
        toastDescription.setSingleLine(false);
        toastDescription.setText(R.string.startupmgr_awaked_forbidden_tips);
        this.mToastSwitch = (Switch) this.mContainerLayout.findViewById(R.id.switcher);
        this.mContainerLayout.setOnClickListener(this);
        this.mToastSwitch.setOnCheckedChangeListener(this.mToastSwitchChangeListener);
    }

    public void onClick(View v) {
        this.mToastSwitch.performClick();
    }

    public void refreshState() {
        this.mToastSwitch.setOnClickListener(null);
        this.mToastSwitch.setChecked(DBAdapter.getAwakedAppNotifySwitchOpenStatus(getContext()));
        this.mToastSwitch.setOnCheckedChangeListener(this.mToastSwitchChangeListener);
    }
}
