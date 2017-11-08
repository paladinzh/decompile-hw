package com.huawei.systemmanager.netassistant.traffic.appdetail.appdetailinfo;

import android.net.NetworkPolicyManager;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.netassistant.traffic.appdetail.appdetailinfo.AppDetailInfo.BaseInfo;
import com.huawei.systemmanager.netassistant.traffic.appinfo.SpecialUid;
import com.huawei.systemmanager.util.HwLog;

public class BackgroundInfo implements BaseInfo {
    private static final String TAG = "BackgroundInfo";
    private boolean isChecked;
    private boolean isEnable;
    private NetworkPolicyManager mPolicyManager;
    private int mUid;

    public BackgroundInfo(int uid) {
        this.mUid = uid;
        initData();
    }

    private void initData() {
        boolean z;
        boolean z2 = false;
        this.mPolicyManager = NetworkPolicyManager.from(GlobalContext.getContext());
        if ((this.mPolicyManager.getUidPolicy(this.mUid) & 1) == 0) {
            z = true;
        } else {
            z = false;
        }
        this.isChecked = z;
        if (!SpecialUid.isWhiteListUid(this.mUid)) {
            z2 = true;
        }
        this.isEnable = z2;
    }

    public String getTitle() {
        return GlobalContext.getString(R.string.app_detail_background_title);
    }

    public String getSubTitle() {
        if (this.isChecked) {
            return GlobalContext.getString(R.string.app_detail_mobile_open);
        }
        return GlobalContext.getString(R.string.app_detail_mobile_close);
    }

    public int getType() {
        return 1;
    }

    public String getTask() {
        return "3";
    }

    public void setChecked(boolean value) {
        this.isChecked = value;
        HwLog.i(TAG, "setChecked ,value is " + value);
        if (this.mPolicyManager != null) {
            this.mPolicyManager.setUidPolicy(this.mUid, value ? 0 : 1);
        }
    }

    public boolean isChecked() {
        return this.isChecked;
    }

    public boolean isEnable() {
        return this.isEnable;
    }
}
