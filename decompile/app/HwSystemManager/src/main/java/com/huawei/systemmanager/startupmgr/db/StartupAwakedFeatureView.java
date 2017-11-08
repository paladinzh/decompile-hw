package com.huawei.systemmanager.startupmgr.db;

import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.database.gfeature.AbsFeatureView;
import com.huawei.systemmanager.comm.database.gfeature.FeatureToColumn;
import com.huawei.systemmanager.startupmgr.comm.StartupDBConst;
import com.huawei.systemmanager.startupmgr.comm.StartupDBConst.AwakedViewKeys;
import java.util.List;

class StartupAwakedFeatureView extends AbsFeatureView {
    StartupAwakedFeatureView() {
    }

    public String getTempViewPrefix() {
        return AwakedViewKeys.AWAKED_TMP_VIEW_PREFIX;
    }

    public String getLinkedRealTablePrefix() {
        return StartupDBConst.SHARED_REAL_TABLE;
    }

    public String getQueryViewName() {
        return AwakedViewKeys.AWAKED_STARTUP_INFO_VIEW;
    }

    public List<FeatureToColumn> getViewColumnFeatureList() {
        List<FeatureToColumn> result = Lists.newArrayList();
        result.add(new FeatureToColumn("status", AwakedViewKeys.STATUS_STORE, "0"));
        result.add(new FeatureToColumn(AwakedViewKeys.LAST_CALLER_PKG_COL, AwakedViewKeys.LAST_CALLER_PKG_STORE));
        result.add(new FeatureToColumn(AwakedViewKeys.CALLER_PKG_SET_COL, AwakedViewKeys.CALLER_PKG_SET_STORE));
        result.add(new FeatureToColumn("userchanged", AwakedViewKeys.USER_CHANGED_STORE, "0"));
        return result;
    }
}
