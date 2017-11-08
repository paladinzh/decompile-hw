package com.huawei.systemmanager.netassistant.traffic.appdetail.appdetailinfo;

import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.netassistant.netapp.control.AppPermissionController;
import com.huawei.systemmanager.netassistant.netapp.control.NetAppPermissionExcutor;
import com.huawei.systemmanager.netassistant.netapp.datasource.NetAppManager.UidDetail;
import com.huawei.systemmanager.netassistant.traffic.appdetail.appdetailinfo.AppDetailInfo.BaseInfo;
import com.huawei.systemmanager.netassistant.traffic.appinfo.SpecialUid;
import com.huawei.systemmanager.util.HwLog;

public class MobileDataInfo implements BaseInfo {
    private static final String TAG = "MobileDataInfo";
    private boolean isChecked;
    private boolean isEnable;
    private UidDetail mAbsInfo;
    private int mUid;

    public MobileDataInfo(int uid) {
        this.mUid = uid;
        initData();
    }

    private void initData() {
        this.mAbsInfo = UidDetail.create(this.mUid);
        this.isChecked = this.mAbsInfo.isMobileAccess();
        this.isEnable = !SpecialUid.isWhiteListUid(this.mUid);
    }

    public String getTitle() {
        return GlobalContext.getString(R.string.mobile_data_status);
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
        return "5";
    }

    public void setChecked(boolean value) {
        this.isChecked = value;
        HwLog.i(TAG, "setChecked ,value is " + value);
        if (this.mAbsInfo == null) {
            HwLog.i(TAG, "setChecked , arg is wrong return");
            return;
        }
        int i;
        this.mAbsInfo.setMobileAccess(value);
        if (this.mAbsInfo.isMobileAccess()) {
            i = 0;
        } else {
            i = 1;
        }
        AppPermissionController holder = new AppPermissionController(i, 0, this.mUid, 0);
        NetAppPermissionExcutor.execute(holder);
    }

    public boolean isChecked() {
        return this.isChecked;
    }

    public boolean isEnable() {
        return this.isEnable;
    }
}
