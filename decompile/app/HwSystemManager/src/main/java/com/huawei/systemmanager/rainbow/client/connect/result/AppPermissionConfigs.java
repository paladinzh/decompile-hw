package com.huawei.systemmanager.rainbow.client.connect.result;

import android.content.ContentValues;
import android.text.TextUtils;
import com.huawei.systemmanager.comm.database.gfeature.GFeatureCvt;
import com.huawei.systemmanager.rainbow.vaguerule.VagueNameMatchUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class AppPermissionConfigs {
    private List<PermissionInfo> mPermissionList = new ArrayList();
    private String mPkgName = null;

    AppPermissionConfigs() {
    }

    public void setPackageName(String pkgName) {
        this.mPkgName = pkgName;
    }

    public void addPermission(PermissionInfo permInfo) {
        this.mPermissionList.add(permInfo);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void appendUpdateParam(Map<String, String> featurePermissionTypeMap, List<ContentValues> contentValuesList, List<ContentValues> vagueContentValuesList, List<String> pkgNameList) {
        if (this.mPermissionList != null && !this.mPermissionList.isEmpty() && !TextUtils.isEmpty(this.mPkgName)) {
            List<ContentValues> def = new ArrayList();
            def.add(GFeatureCvt.cvtToStdContentValue(this.mPkgName, "31", String.valueOf(1)));
            if (VagueNameMatchUtil.isVaguePkgName(this.mPkgName)) {
                for (PermissionInfo permission : this.mPermissionList) {
                    String vagueFeatureName = (String) featurePermissionTypeMap.get(permission.mPermission);
                    if (!TextUtils.isEmpty(vagueFeatureName)) {
                        vagueContentValuesList.add(GFeatureCvt.cvtToStdContentValue(this.mPkgName, vagueFeatureName, String.valueOf(permission.mPermissionPolicy)));
                    }
                }
                List<ContentValues> r = GFeatureCvt.getDefaultContentValues(vagueContentValuesList, def);
                if (r != null && r.size() > 0) {
                    vagueContentValuesList.addAll(r);
                }
            } else {
                pkgNameList.add(this.mPkgName);
                for (PermissionInfo permission2 : this.mPermissionList) {
                    String featureName = (String) featurePermissionTypeMap.get(permission2.mPermission);
                    if (!TextUtils.isEmpty(featureName)) {
                        contentValuesList.add(GFeatureCvt.cvtToStdContentValue(this.mPkgName, featureName, String.valueOf(permission2.mPermissionPolicy)));
                    }
                }
                List<ContentValues> re = GFeatureCvt.getDefaultContentValues(contentValuesList, def);
                if (re != null && re.size() > 0) {
                    contentValuesList.addAll(re);
                }
            }
        }
    }
}
