package com.huawei.harassmentinterception.blackwhitelist;

import com.huawei.harassmentinterception.callback.LoadDataCallBack;
import com.huawei.harassmentinterception.common.CommonObject.BlacklistInfo;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BlackListDataLoader extends Thread {
    public static final Comparator<BlacklistInfo> BLACK_LIST_COMPARATOR = new Comparator<BlacklistInfo>() {
        public int compare(BlacklistInfo left, BlacklistInfo right) {
            int leftP = left.getType();
            int rightP = right.getType();
            if (leftP == rightP) {
                return BlacklistInfo.HARASSMENT_ALP_COMPARATOR.compare(left, right);
            }
            if (leftP < rightP) {
                return 1;
            }
            if (leftP > rightP) {
                return -1;
            }
            return 0;
        }
    };
    private String TAG = BlackListDataLoader.class.getSimpleName();
    private LoadDataCallBack mCallBack = null;

    public BlackListDataLoader(String name, LoadDataCallBack callback) {
        super(name);
        this.mCallBack = callback;
    }

    public void run() {
        try {
            List<BlacklistInfo> latestBlackList = DBAdapter.getBlacklist(GlobalContext.getContext());
            if (this.mCallBack != null) {
                Collections.sort(latestBlackList, BLACK_LIST_COMPARATOR);
                this.mCallBack.onCompletedDataLoad(latestBlackList);
            }
        } catch (Exception e) {
            HwLog.e(this.TAG, "DataLoadingThread-run: Exception", e);
        }
    }
}
