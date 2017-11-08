package com.huawei.systemmanager.startupmgr.db;

import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.database.gfeature.AbsFeatureView;
import com.huawei.systemmanager.comm.database.gfeature.FeatureToColumn;
import com.huawei.systemmanager.startupmgr.comm.StartupDBConst;
import com.huawei.systemmanager.startupmgr.comm.StartupDBConst.NormalViewKeys;
import java.util.List;

class StartupNormalFeatureView extends AbsFeatureView {
    StartupNormalFeatureView() {
    }

    public String getTempViewPrefix() {
        return NormalViewKeys.NORMAL_TMP_VIEW_PREFIX;
    }

    public String getLinkedRealTablePrefix() {
        return StartupDBConst.SHARED_REAL_TABLE;
    }

    public String getQueryViewName() {
        return NormalViewKeys.NORMAL_STARTUP_INFO_VIEW;
    }

    public List<FeatureToColumn> getViewColumnFeatureList() {
        List<FeatureToColumn> result = Lists.newArrayList();
        result.add(new FeatureToColumn("status", NormalViewKeys.STATUS_STORE, "0"));
        result.add(new FeatureToColumn("type", NormalViewKeys.TYPE_STORE, String.valueOf(2)));
        result.add(new FeatureToColumn(NormalViewKeys.ACTIONS_COL, NormalViewKeys.ACTIONS_STORE));
        result.add(new FeatureToColumn("userchanged", NormalViewKeys.USER_CHANGED_STORE, "0"));
        return result;
    }
}
