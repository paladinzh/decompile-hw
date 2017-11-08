package com.huawei.systemmanager.rainbow;

import android.content.Context;
import com.huawei.systemmanager.comm.grule.GRuleManager;
import com.huawei.systemmanager.comm.misc.StringUtils;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import java.util.ArrayList;
import java.util.List;

public abstract class CloudControlChange {
    private Context mContext = null;

    protected abstract String getCurrentScenario();

    protected abstract boolean isCurrentPkgNotInited(String str);

    protected abstract void processBlackToWhiteList(List<String> list);

    protected abstract void processWhiteToBlackList(List<String> list);

    public CloudControlChange(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void handleWhiteListChange(List<String> addList, List<String> minusList) {
        handleWhiteListMinusOrBlackListAdd(minusList);
        handleWhiteListAddOrBlackListMinus(addList);
    }

    public void handleBlackListChange(List<String> addList, List<String> minusList) {
        handleWhiteListMinusOrBlackListAdd(addList);
        handleWhiteListAddOrBlackListMinus(minusList);
    }

    private void handleWhiteListAddOrBlackListMinus(List<String> initList) {
        if (initList != null && !initList.isEmpty()) {
            List<String> processList = new ArrayList();
            for (String pkgName : initList) {
                HsmPkgInfo appInfo = HsmPackageManager.getInstance().getPkgInfo(pkgName);
                if (!(appInfo == null || StringUtils.isEmpty(appInfo.mPkgName) || GRuleManager.getInstance().shouldMonitor(this.mContext, getCurrentScenario(), appInfo.mPkgName))) {
                    processList.add(appInfo.mPkgName);
                }
            }
            if (!processList.isEmpty()) {
                processBlackToWhiteList(processList);
            }
        }
    }

    private void handleWhiteListMinusOrBlackListAdd(List<String> initList) {
        if (initList != null && !initList.isEmpty()) {
            List<String> processList = new ArrayList();
            for (String pkgName : initList) {
                HsmPkgInfo appInfo = HsmPackageManager.getInstance().getPkgInfo(pkgName);
                if (appInfo != null && !StringUtils.isEmpty(appInfo.mPkgName) && GRuleManager.getInstance().shouldMonitor(this.mContext, getCurrentScenario(), appInfo.mPkgName) && isCurrentPkgNotInited(appInfo.mPkgName)) {
                    processList.add(appInfo.mPkgName);
                }
            }
            if (!processList.isEmpty()) {
                processWhiteToBlackList(processList);
            }
        }
    }
}
