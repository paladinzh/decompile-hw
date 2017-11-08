package com.huawei.systemmanager.rainbow.client.connect.result;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.systemmanager.rainbow.client.base.ClientConstant.CloudTimeConst;
import com.huawei.systemmanager.rainbow.client.helper.LocalSharedPrefrenceHelper;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AllCheckVersionConfigs implements IOutputItf<AllCheckVersionConfigs> {
    private static final String TAG = "AllCheckVersionConfigs";
    private List<CheckVersionConfig> mCheckVersionConfigList = new ArrayList();
    private long mIinternalTime = ClientServerSync.getIntervalTimeFromServer();

    public void parseAndUpdate(Context ctx, JSONObject obj) throws JSONException {
        parseJSONObject(obj);
        updateLocalData(ctx);
    }

    private AllCheckVersionConfigs parseJSONObject(JSONObject jsonObject) throws JSONException {
        String str_checkVersionArr;
        JSONObject root = jsonObject;
        if (jsonObject.has("pollingCycle")) {
            int intervalDays = Integer.parseInt(jsonObject.getString("pollingCycle"));
            if (1 <= intervalDays && 1000 >= intervalDays) {
                this.mIinternalTime = ((long) intervalDays) * 86400000;
            }
        }
        JSONArray jSONArray = null;
        if (jsonObject.has("components")) {
            str_checkVersionArr = jsonObject.getString("components");
        } else {
            str_checkVersionArr = null;
        }
        if (!TextUtils.isEmpty(str_checkVersionArr)) {
            jSONArray = new JSONArray(str_checkVersionArr);
        }
        if (jSONArray != null) {
            int appCount = jSONArray.length();
            for (int i = 0; i < appCount; i++) {
                JSONObject checkVersionObj = jSONArray.getJSONObject(i);
                String versionName = checkVersionObj.getString("name");
                Long versionCode = Long.valueOf(Long.parseLong(checkVersionObj.getString("version")));
                this.mCheckVersionConfigList.add(new CheckVersionConfig(versionName, versionCode.longValue(), checkVersionObj.getString("url")));
            }
        }
        return this;
    }

    private void updateLocalData(Context ctx) {
        new LocalSharedPrefrenceHelper(ctx).putLong(CloudTimeConst.CHECKVERSION_CYCLE_SPF, this.mIinternalTime);
        ClientServerSync.setIntervalTimeFromServer(this.mIinternalTime);
        List<CheckVersionConfig> checkVersionConfigList = this.mCheckVersionConfigList;
        if (checkVersionConfigList == null || checkVersionConfigList.isEmpty()) {
            HwLog.d(TAG, "No need change the url or localVersion!");
            return;
        }
        for (CheckVersionConfig currentConfig : checkVersionConfigList) {
            ClientServerSync.setVersionAndUrl(currentConfig);
        }
    }
}
