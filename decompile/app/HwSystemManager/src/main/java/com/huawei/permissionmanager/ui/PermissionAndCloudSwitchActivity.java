package com.huawei.permissionmanager.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import com.huawei.permissionmanager.db.RecommendDBHelper;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.CommonSwitcher;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.rainbow.CloudClientOperation;
import com.huawei.systemmanager.rainbow.CloudSwitchHelper;

public class PermissionAndCloudSwitchActivity extends HsmActivity implements OnClickListener {
    private View mCloudPackageInstallLayout;
    private Switch mCloudPackageInstallSwitch;
    OnCheckedChangeListener mCloudPackageInstallSwitchCheckedChangeListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            RecommendDBHelper.getInstance(PermissionAndCloudSwitchActivity.this.mContext).setRecommendPackageInstallSwitch(isChecked);
        }
    };
    OnCheckedChangeListener mCloudSwitchCheckedChangeListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                CloudClientOperation.openSystemManageClouds(PermissionAndCloudSwitchActivity.this.mContext);
            } else {
                CloudClientOperation.closeSystemManageClouds(PermissionAndCloudSwitchActivity.this.mContext);
            }
            String[] strArr = new String[2];
            strArr[0] = HsmStatConst.PARAM_OP;
            strArr[1] = isChecked ? "1" : "0";
            HsmStat.statE(38, HsmStatConst.constructJsonParams(strArr));
        }
    };
    private View mCloudSwitchLayout;
    private Switch mCloundSwitch;
    private Context mContext;
    private View mGroupSendMonitorLayout;
    private Switch mGroupSendMonitorSwitch;
    OnCheckedChangeListener mGroupSendMonitorSwitchCheckedChangeListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            RecommendDBHelper.getInstance(PermissionAndCloudSwitchActivity.this.mContext).setSendGroupSmsSwitch(isChecked);
        }
    };
    private CommonSwitcher mNotifySwitcher;
    private View mPermissionSwitchLayout;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permission_cloud_switch);
        setTitle(R.string.menu_settings);
        this.mContext = getApplicationContext();
        this.mCloudSwitchLayout = findViewById(R.id.cloud_switch_layout);
        ((TextView) this.mCloudSwitchLayout.findViewById(ViewUtil.HWID_TEXT_1)).setText(R.string.ActionBar_AutoUpdate_PermissionConfig);
        TextView mCloudSwitchLayoutTips = (TextView) this.mCloudSwitchLayout.findViewById(ViewUtil.HWID_TEXT_2);
        mCloudSwitchLayoutTips.setSingleLine(false);
        mCloudSwitchLayoutTips.setText(R.string.ListViewSecendLine_PermissionManager_Recommend);
        this.mCloudSwitchLayout.setOnClickListener(this);
        this.mCloundSwitch = (Switch) this.mCloudSwitchLayout.findViewById(R.id.switcher);
        this.mCloudPackageInstallLayout = findViewById(R.id.cloud_packageinstall_switch_layout);
        this.mCloudPackageInstallLayout.setOnClickListener(this);
        this.mCloudPackageInstallSwitch = (Switch) this.mCloudPackageInstallLayout.findViewById(R.id.switcher);
        if (!CloudSwitchHelper.isCloudEnabled()) {
            this.mCloudSwitchLayout.setVisibility(8);
            this.mCloudPackageInstallLayout.setVisibility(8);
        }
        ((TextView) this.mCloudPackageInstallLayout.findViewById(ViewUtil.HWID_TEXT_1)).setText(R.string.Recommend_Setting_title);
        TextView packageInstallDescription = (TextView) this.mCloudPackageInstallLayout.findViewById(ViewUtil.HWID_TEXT_2);
        packageInstallDescription.setSingleLine(false);
        packageInstallDescription.setText(R.string.Recommend_Setting_description);
        this.mPermissionSwitchLayout = findViewById(R.id.permission_switch_layout);
        this.mPermissionSwitchLayout.setOnClickListener(this);
        this.mPermissionSwitchLayout.setVisibility(8);
        ((TextView) this.mPermissionSwitchLayout.findViewById(ViewUtil.HWID_TEXT_1)).setText(R.string.systemmanager_module_title_permissions);
        TextView permissionDescription = (TextView) this.mPermissionSwitchLayout.findViewById(ViewUtil.HWID_TEXT_2);
        permissionDescription.setSingleLine(false);
        permissionDescription.setText(R.string.permission_setting_description);
        this.mGroupSendMonitorLayout = findViewById(R.id.group_send_monitor_layout);
        this.mGroupSendMonitorLayout.setOnClickListener(this);
        this.mGroupSendMonitorSwitch = (Switch) this.mGroupSendMonitorLayout.findViewById(R.id.switcher);
        if (Utility.isWifiOnlyMode()) {
            this.mGroupSendMonitorLayout.setVisibility(8);
        }
        ((TextView) this.mGroupSendMonitorLayout.findViewById(ViewUtil.HWID_TEXT_1)).setText(R.string.SMS_Group_minitor);
        TextView groupSendMonitorDescription = (TextView) this.mGroupSendMonitorLayout.findViewById(ViewUtil.HWID_TEXT_2);
        groupSendMonitorDescription.setSingleLine(false);
        groupSendMonitorDescription.setText(R.string.SMS_Group_minitor_description);
        this.mNotifySwitcher = new ForbiddenNotifySwitcher(findViewById(R.id.toast_switch_layout));
        this.mNotifySwitcher.init();
    }

    protected void onResume() {
        super.onResume();
        this.mCloundSwitch.setChecked(CloudClientOperation.getSystemManageCloudsStatus(getApplicationContext()));
        this.mCloundSwitch.setOnCheckedChangeListener(this.mCloudSwitchCheckedChangeListener);
        this.mCloudPackageInstallSwitch.setOnCheckedChangeListener(null);
        this.mCloudPackageInstallSwitch.setChecked(RecommendDBHelper.getInstance(this.mContext).getRecommendPackageInstallSwitchStatus());
        this.mCloudPackageInstallSwitch.setOnCheckedChangeListener(this.mCloudPackageInstallSwitchCheckedChangeListener);
        this.mGroupSendMonitorSwitch.setOnCheckedChangeListener(null);
        this.mGroupSendMonitorSwitch.setChecked(RecommendDBHelper.getInstance(this.mContext).getSendGroupSmsSwitchStatus());
        this.mGroupSendMonitorSwitch.setOnCheckedChangeListener(this.mGroupSendMonitorSwitchCheckedChangeListener);
        this.mCloudSwitchLayout.setVisibility(8);
        this.mNotifySwitcher.refreshState();
    }

    public void onClick(View v) {
        Switch view = (Switch) v.findViewById(R.id.switcher);
        if (view != null) {
            view.performClick();
        }
    }
}
