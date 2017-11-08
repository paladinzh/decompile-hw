package com.huawei.systemmanager.netassistant.traffic.appdetail.appdetailinfo;

import android.text.TextUtils;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.netassistant.traffic.appdetail.appdetailinfo.AppDetailInfo.BaseInfo;
import com.huawei.systemmanager.netassistant.traffic.appinfo.NetAppUtils;
import com.huawei.systemmanager.netassistant.traffic.notrafficapp.NoTrafficAppDbInfo;
import com.huawei.systemmanager.util.HwLog;

public class NoTrafficInfo implements BaseInfo {
    private static final String TAG = "NoTrafficInfo";
    private boolean isChecked;
    private boolean isEnable;
    private String mImsi;
    private NoTrafficAppDbInfo mNoTrafficAppDbInfo;
    private int mUid;

    public NoTrafficInfo(String imsi, int uid) {
        this.mImsi = imsi;
        this.mUid = uid;
        initData();
    }

    private void initData() {
        boolean z = false;
        this.mNoTrafficAppDbInfo = new NoTrafficAppDbInfo(this.mImsi);
        this.mNoTrafficAppDbInfo.initAllData();
        this.isChecked = this.mNoTrafficAppDbInfo.isNoTrafficApp(this.mUid);
        if (NetAppUtils.isRemovableUid(this.mUid) && !TextUtils.isEmpty(this.mImsi)) {
            z = true;
        }
        this.isEnable = z;
    }

    public String getTitle() {
        return GlobalContext.getString(R.string.no_traffic_app);
    }

    public String getSubTitle() {
        if (this.isChecked) {
            return GlobalContext.getString(R.string.app_detail_mobile_add);
        }
        return GlobalContext.getString(R.string.app_detail_mobile_minus);
    }

    public int getType() {
        return 1;
    }

    public String getTask() {
        return "2";
    }

    public void setChecked(boolean value) {
        this.isChecked = value;
        HwLog.i(TAG, "setChecked ,value is " + value);
        if (this.mNoTrafficAppDbInfo == null) {
            return;
        }
        if (value) {
            this.mNoTrafficAppDbInfo.save(this.mUid);
        } else {
            this.mNoTrafficAppDbInfo.clear(this.mUid);
        }
    }

    public boolean isEnable() {
        return this.isEnable;
    }

    public boolean isChecked() {
        return this.isChecked;
    }
}
