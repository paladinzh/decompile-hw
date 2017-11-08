package com.huawei.harassmentinterception.blackwhitelist;

import com.huawei.harassmentinterception.callback.LoadDataCallBack;
import com.huawei.harassmentinterception.common.CommonObject.WhitelistInfo;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;
import java.util.Collections;
import java.util.List;

public class WhiteListDataLoader extends Thread {
    private String TAG = WhiteListDataLoader.class.getSimpleName();
    private LoadDataCallBack mCallBack = null;

    public WhiteListDataLoader(String name, LoadDataCallBack callback) {
        super(name);
        this.mCallBack = callback;
    }

    public void run() {
        try {
            List<WhitelistInfo> latestWhiteList = DBAdapter.getWhitelist(GlobalContext.getContext());
            if (this.mCallBack != null) {
                Collections.sort(latestWhiteList, WhitelistInfo.WHITELIST_ALP_COMPARATOR);
                this.mCallBack.onCompletedDataLoad(latestWhiteList);
            }
        } catch (Exception e) {
            HwLog.e(this.TAG, "DataLoadingThread-run: Exception", e);
        }
    }
}
