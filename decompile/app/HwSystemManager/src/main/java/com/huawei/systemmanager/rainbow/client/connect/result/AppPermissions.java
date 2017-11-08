package com.huawei.systemmanager.rainbow.client.connect.result;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.systemmanager.rainbow.client.base.CloudSpfKeys.CloudReqVerSpfKeys;
import com.huawei.systemmanager.rainbow.client.util.UpdateOuterTableUtil;
import com.huawei.systemmanager.rainbow.comm.base.CloudProviderConst.RealFeatureCallMethod;
import com.huawei.systemmanager.rainbow.comm.misc.PermissionDefine;
import com.huawei.systemmanager.rainbow.db.base.CloudConst;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.CloudVagueValues;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.PermissionValues;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AppPermissions extends AbsOutputHandle<AppPermissions> {
    private List<String> mDeleteList = new ArrayList();
    private List<AppPermissionConfigs> mPermissionConfigsList = new ArrayList();

    boolean validOutputData() {
        return (this.mPermissionConfigsList.isEmpty() && this.mDeleteList.isEmpty()) ? false : true;
    }

    String getLocalVersionKey() {
        return CloudReqVerSpfKeys.RIGHT_LIST_VERSION_SPF;
    }

    public AppPermissions parseJSONObject(JSONObject obj) throws JSONException {
        String str_packageInfoArr;
        String str_delArr;
        JSONObject root = obj;
        if (obj.has("rlVer")) {
            setLocalVersion(Long.parseLong(obj.getString("rlVer")));
        }
        JSONArray jSONArray = null;
        if (obj.has("arList")) {
            str_packageInfoArr = obj.getString("arList");
        } else {
            str_packageInfoArr = null;
        }
        if (!TextUtils.isEmpty(str_packageInfoArr)) {
            jSONArray = new JSONArray(str_packageInfoArr);
        }
        if (jSONArray != null) {
            int appCount = jSONArray.length();
            for (int i = 0; i < appCount; i++) {
                String str_permissionArr;
                AppPermissionConfigs appConfig = new AppPermissionConfigs();
                JSONObject appObj = jSONArray.getJSONObject(i);
                appConfig.setPackageName(appObj.getString("aPN"));
                PermissionInfo permissionTrustInfo = new PermissionInfo();
                permissionTrustInfo.mPermission = PermissionDefine.APP_TRUST;
                permissionTrustInfo.mPermissionPolicy = appObj.getInt(PermissionDefine.APP_TRUST);
                appConfig.addPermission(permissionTrustInfo);
                JSONArray jSONArray2 = null;
                if (appObj.has("rL")) {
                    str_permissionArr = appObj.getString("rL");
                } else {
                    str_permissionArr = null;
                }
                if (!TextUtils.isEmpty(str_permissionArr)) {
                    jSONArray2 = new JSONArray(str_permissionArr);
                }
                if (jSONArray2 != null) {
                    int rightCount = jSONArray2.length();
                    for (int j = 0; j < rightCount; j++) {
                        PermissionInfo permissionInfo = new PermissionInfo();
                        JSONObject permissionObj = jSONArray2.getJSONObject(j);
                        permissionInfo.mPermission = permissionObj.getString("rID");
                        permissionInfo.mPermissionPolicy = permissionObj.getInt("rT");
                        appConfig.addPermission(permissionInfo);
                    }
                }
                this.mPermissionConfigsList.add(appConfig);
            }
        }
        JSONArray jSONArray3 = null;
        if (obj.has("apDelList")) {
            str_delArr = obj.getString("apDelList");
        } else {
            str_delArr = null;
        }
        if (!TextUtils.isEmpty(str_delArr)) {
            jSONArray3 = new JSONArray(str_delArr);
        }
        if (jSONArray3 != null) {
            int delCount = jSONArray3.length();
            for (int delIndex = 0; delIndex < delCount; delIndex++) {
                this.mDeleteList.add(jSONArray3.getString(delIndex));
            }
        }
        return this;
    }

    public void updateDatabase(Context ctx) {
        deleteAppPermissions(ctx, this.mDeleteList);
        updateAppPermissions(ctx, this.mPermissionConfigsList);
    }

    private void deleteAppPermissions(Context ctx, List<String> delList) {
        if (delList != null && !delList.isEmpty()) {
            Bundle bundle = new Bundle();
            bundle.putStringArrayList(RealFeatureCallMethod.EXTRA_PKG_NAME_LIST_KEY, new ArrayList(delList));
            ctx.getContentResolver().call(CloudConst.AUTHORITY_URI, RealFeatureCallMethod.CALL_METHOD_DELETE_GFEATURE, "CloudPermission", bundle);
            ctx.getContentResolver().call(CloudConst.AUTHORITY_URI, RealFeatureCallMethod.CALL_METHOD_DELETE_GFEATURE, "CloudVaguePermission", bundle);
        }
    }

    private void updateAppPermissions(Context ctx, List<AppPermissionConfigs> appPermissionCfgList) {
        if (appPermissionCfgList != null && !appPermissionCfgList.isEmpty()) {
            Map<String, String> featurePermissionTypeMap = PermissionDefine.getFeaturePermissionMaps();
            ArrayList<ContentValues> contentValuesList = new ArrayList();
            ArrayList<ContentValues> vagueContentValuesList = new ArrayList();
            ArrayList<String> pkgNameList = new ArrayList();
            for (AppPermissionConfigs appPermission : appPermissionCfgList) {
                appPermission.appendUpdateParam(featurePermissionTypeMap, contentValuesList, vagueContentValuesList, pkgNameList);
            }
            updatePermissionFeatureTable(ctx, contentValuesList);
            updateVagueFeatureTable(ctx, vagueContentValuesList);
            updateOuterTables(ctx, pkgNameList);
        }
    }

    private void updateVagueFeatureTable(Context ctx, ArrayList<ContentValues> valuesList) {
        if (!valuesList.isEmpty()) {
            ctx.getContentResolver().bulkInsert(CloudVagueValues.PERMISSION_FEATURE_CONTENT_URI, (ContentValues[]) valuesList.toArray(new ContentValues[valuesList.size()]));
        }
    }

    private void updatePermissionFeatureTable(Context ctx, ArrayList<ContentValues> valuesList) {
        if (!valuesList.isEmpty()) {
            ctx.getContentResolver().bulkInsert(PermissionValues.PERMISSION_FEATURE_CONTENT_URI, (ContentValues[]) valuesList.toArray(new ContentValues[valuesList.size()]));
        }
    }

    private void updateOuterTables(Context ctx, ArrayList<String> pkgNameList) {
        if (!pkgNameList.isEmpty()) {
            UpdateOuterTableUtil.updateOuterTable(ctx);
        }
    }
}
