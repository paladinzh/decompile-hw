package com.huawei.systemmanager.rainbow.db.featureview;

import com.huawei.systemmanager.rainbow.db.base.CloudConst.PermissionValues;

public class PermissionFeatureView extends VaguePermissionFeatureView {
    public String getTempViewPrefix() {
        return PermissionValues.PERMISSION_TEMP_FEATURE_VIEW_PREFIX;
    }

    public String getLinkedRealTablePrefix() {
        return "CloudPermission";
    }

    public String getQueryViewName() {
        return PermissionValues.PERMISSION_INNER_VIEW_NAME;
    }
}
