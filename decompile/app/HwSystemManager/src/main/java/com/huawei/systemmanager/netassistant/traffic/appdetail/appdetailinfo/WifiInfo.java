package com.huawei.systemmanager.netassistant.traffic.appdetail.appdetailinfo;

import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.netassistant.netapp.control.AppPermissionController;
import com.huawei.systemmanager.netassistant.netapp.control.NetAppPermissionExcutor;
import com.huawei.systemmanager.netassistant.netapp.datasource.NetAppManager.UidDetail;
import com.huawei.systemmanager.netassistant.traffic.appdetail.appdetailinfo.AppDetailInfo.BaseInfo;
import com.huawei.systemmanager.netassistant.traffic.appinfo.SpecialUid;
import com.huawei.systemmanager.util.HwLog;

public class WifiInfo implements BaseInfo {
    private static final String TAG = "WifiInfo";
    private boolean isChecked;
    private boolean isEnable;
    private UidDetail mAbsInfo;
    private int mUid;

    public WifiInfo(int uid) {
        this.mUid = uid;
        initData();
    }

    private void initData() {
        this.mAbsInfo = UidDetail.create(this.mUid);
        this.isChecked = this.mAbsInfo.isWifiAccess();
        this.isEnable = !SpecialUid.isWhiteListUid(this.mUid);
    }

    public String getTitle() {
        return GlobalContext.getString(R.string.WIFI);
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
        return "6";
    }

    public void setChecked(boolean value) {
        this.isChecked = value;
        HwLog.i(TAG, "setChecked ,value is " + value);
        if (this.mAbsInfo == null) {
            HwLog.i(TAG, "setChecked , arg is wrong return");
            return;
        }
        int i;
        this.mAbsInfo.setWifiAccess(value);
        if (this.mAbsInfo.isWifiAccess()) {
            i = 0;
        } else {
            i = 1;
        }
        AppPermissionController holder = new AppPermissionController(i, 1, this.mUid, 0);
        NetAppPermissionExcutor.execute(holder);
    }

    public boolean isChecked() {
        return this.isChecked;
    }

    public boolean isEnable() {
        return this.isEnable;
    }
}
