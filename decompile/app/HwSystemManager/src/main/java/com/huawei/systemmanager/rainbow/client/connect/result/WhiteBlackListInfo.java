package com.huawei.systemmanager.rainbow.client.connect.result;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.SparseArray;
import com.huawei.systemmanager.rainbow.client.background.service.IntelligentCompleteService;
import com.huawei.systemmanager.rainbow.client.base.CloudSpfKeys;
import com.huawei.systemmanager.rainbow.client.base.GetAppListBasic;
import com.huawei.systemmanager.rainbow.db.CloudDBAdapter;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WhiteBlackListInfo extends AbsOutputHandle<WhiteBlackListInfo> {
    private static final String TAG = "WhiteBlackListInfo";
    protected ArrayList<ContentValues> mContValuesList = new ArrayList();
    private List<String> mPkgList = new ArrayList();
    private int mType = 0;

    public WhiteBlackListInfo(int type) {
        this.mType = type;
    }

    public void addPkg(String pkg) {
        this.mPkgList.add(pkg);
    }

    boolean validOutputData() {
        return !this.mPkgList.isEmpty();
    }

    String getLocalVersionKey() {
        SparseArray<String> blackWhiteMap = GetAppListBasic.getBlackWhiteMaps();
        HwLog.d(TAG, "SharePreference is: " + ((String) blackWhiteMap.get(this.mType)));
        return (String) blackWhiteMap.get(this.mType);
    }

    public void updateContentValues(JSONObject permissionObj) throws JSONException {
        ContentValues contentValues = new ContentValues();
        ArrayList<JsonColMap> jsonMaps = (ArrayList) JsonColMap.getJsonColMaps().get(Integer.valueOf(this.mType));
        if (permissionObj.has("aPN")) {
            String packageName = permissionObj.getString("aPN");
            addPkg(packageName);
            contentValues.put("packageName", packageName);
        }
        if (jsonMaps != null) {
            for (JsonColMap jsonColMap : jsonMaps) {
                if (permissionObj.has(jsonColMap.mJsonField)) {
                    contentValues.put(jsonColMap.mColumnField, permissionObj.getString(jsonColMap.mJsonField));
                }
            }
        }
        this.mContValuesList.add(contentValues);
    }

    public WhiteBlackListInfo parseJSONObject(JSONObject obj) throws JSONException {
        String appListString = null;
        JSONObject root = obj;
        HwLog.i(TAG, "WhiteBlackListInfo***parseJSONObject***root==" + obj);
        if (obj.has("blVer")) {
            setLocalVersion(Long.parseLong(obj.getString("blVer")));
        }
        JSONArray jSONArray = null;
        if (obj.has("blackList")) {
            appListString = obj.getString("blackList");
        }
        if (!TextUtils.isEmpty(appListString)) {
            jSONArray = new JSONArray(appListString);
        }
        if (jSONArray != null) {
            int permissionCount = jSONArray.length();
            for (int j = 0; j < permissionCount; j++) {
                updateContentValues(jSONArray.getJSONObject(j));
            }
        }
        return this;
    }

    public void updateDatabase(Context ctx) {
        HwLog.d(TAG, "updateDatabase of packages:" + this.mPkgList);
        updateAppLists(ctx, this.mType, this.mPkgList);
    }

    public String toString() {
        StringBuffer strBuf = new StringBuffer();
        strBuf.append("mLocalVersion = " + getLocalVersion());
        if (this.mPkgList != null) {
            for (String name : this.mPkgList) {
                strBuf.append(" name = " + name);
            }
        }
        return strBuf.toString();
    }

    private void updateAppLists(Context ctx, int listType, List<String> list) {
        try {
            SparseArray<Uri> uriMap = GetAppListBasic.getBlackWhiteUriMaps();
            HwLog.e(TAG, ((Uri) uriMap.get(listType)).toString());
            CloudDBAdapter.getInstance(ctx);
            if (GetAppListBasic.isAllDataUpdated(listType)) {
                ctx.getContentResolver().delete((Uri) uriMap.get(listType), null, null);
            }
            ctx.getContentResolver().bulkInsert((Uri) uriMap.get(listType), (ContentValues[]) this.mContValuesList.toArray(new ContentValues[this.mContValuesList.size()]));
            String action = (String) GetAppListBasic.getActionMaps().get(listType);
            HwLog.i(TAG, "updateAppLists***action===" + action + "****listType==" + listType);
            if (action != null) {
                ctx.sendBroadcastAsUser(new Intent(action), UserHandle.ALL, "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
                sendCloudDataSyncCompleteNotification(ctx);
            }
        } catch (Exception e) {
            HwLog.e(TAG, e.getMessage());
        }
    }

    private void sendCloudDataSyncCompleteNotification(Context context) {
        if (context.getSharedPreferences(CloudSpfKeys.FILE_NAME, 0).getBoolean(CloudSpfKeys.NEED_CLOUD_SYNC_COMPLETE_NOTIFIED, true)) {
            sendCompleteNotification(context);
        }
    }

    public void sendCompleteNotification(Context context) {
        context.startService(new Intent(context, IntelligentCompleteService.class));
    }
}
