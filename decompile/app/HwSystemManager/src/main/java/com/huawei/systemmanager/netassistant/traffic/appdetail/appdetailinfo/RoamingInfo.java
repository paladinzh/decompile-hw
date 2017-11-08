package com.huawei.systemmanager.netassistant.traffic.appdetail.appdetailinfo;

import android.net.HwNetworkPolicyManager;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.netassistant.traffic.appdetail.appdetailinfo.AppDetailInfo.BaseInfo;
import com.huawei.systemmanager.netassistant.traffic.appinfo.NetAppInfo;
import com.huawei.systemmanager.netassistant.traffic.appinfo.SpecialUid;
import com.huawei.systemmanager.netassistant.traffic.roamingtraffic.RoamingAppInfo;
import com.huawei.systemmanager.util.HwLog;

public class RoamingInfo implements BaseInfo {
    private static final String TAG = "RoamingInfo";
    private boolean isChecked;
    private boolean isEnable;
    HwNetworkPolicyManager mNetworkPolicy;
    private RoamingAppInfo mRoamingAppInfo;
    private int mUid;

    public RoamingInfo(int uid) {
        this.mUid = uid;
        initData();
    }

    private void initData() {
        boolean z = false;
        this.mNetworkPolicy = HwNetworkPolicyManager.from(GlobalContext.getContext());
        this.mRoamingAppInfo = new RoamingAppInfo(NetAppInfo.buildInfo(this.mUid), (this.mNetworkPolicy.getHwUidPolicy(this.mUid) & 4) == 0);
        this.isChecked = this.mRoamingAppInfo.getNetAccess();
        if (!SpecialUid.isWhiteListUid(this.mUid)) {
            z = true;
        }
        this.isEnable = z;
    }

    public String getTitle() {
        return GlobalContext.getString(R.string.app_detail_roaming_title);
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
        return "4";
    }

    public void setChecked(boolean access) {
        this.isChecked = access;
        HwLog.i(TAG, "setChecked ,value is " + access);
        if (access) {
            this.mNetworkPolicy.removeHwUidPolicy(this.mUid, 4);
        } else {
            this.mNetworkPolicy.addHwUidPolicy(this.mUid, 4);
        }
    }

    public boolean isEnable() {
        return this.isEnable;
    }

    public boolean isChecked() {
        return this.isChecked;
    }
}
