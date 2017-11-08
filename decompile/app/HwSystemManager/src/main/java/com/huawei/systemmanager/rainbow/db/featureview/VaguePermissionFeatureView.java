package com.huawei.systemmanager.rainbow.db.featureview;

import com.huawei.systemmanager.comm.database.gfeature.AbsFeatureView;
import com.huawei.systemmanager.comm.database.gfeature.FeatureToColumn;
import com.huawei.systemmanager.rainbow.db.base.CloudConst;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.CloudVagueValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.NetworkValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.PermissionValues;
import com.huawei.systemmanager.rainbow.db.base.CommonFeatureColumn;
import com.huawei.systemmanager.rainbow.db.base.PermissionColumn;
import java.util.ArrayList;
import java.util.List;

public class VaguePermissionFeatureView extends AbsFeatureView {
    public String getTempViewPrefix() {
        return CloudVagueValues.PERMISSION_TEMP_FEATURE_VIEW_PREFIX;
    }

    public String getLinkedRealTablePrefix() {
        return "CloudVaguePermission";
    }

    public String getQueryViewName() {
        return CloudVagueValues.PERMISSION_INNER_VIEW_NAME;
    }

    public List<FeatureToColumn> getViewColumnFeatureList() {
        List<FeatureToColumn> featureList = new ArrayList();
        List<PermissionColumn> permissionColumnList = CloudConst.getPermissionColumnList();
        if (permissionColumnList.isEmpty()) {
            return featureList;
        }
        List<CommonFeatureColumn> commonColumnList = CloudConst.getCommonFeatureColumnList();
        if (commonColumnList.isEmpty()) {
            return featureList;
        }
        for (PermissionColumn permissionColumn : permissionColumnList) {
            featureList.add(permissionColumn.genFeatureToColunm());
        }
        featureList.add(new FeatureToColumn(PermissionValues.PERMISSION_COLUMN_TRUST, PermissionValues.PERMISSION_VIEW_TRUST));
        featureList.add(new FeatureToColumn(NetworkValues.NETWORK_COLUMN_DATA, "2"));
        featureList.add(new FeatureToColumn(NetworkValues.NETWORK_COLUMN_WIFI, "1"));
        for (CommonFeatureColumn commonColumn : commonColumnList) {
            featureList.add(commonColumn.createFeatureToColumn());
        }
        return featureList;
    }
}
