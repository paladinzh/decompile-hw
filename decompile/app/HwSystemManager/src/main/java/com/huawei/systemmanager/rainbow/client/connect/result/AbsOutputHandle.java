package com.huawei.systemmanager.rainbow.client.connect.result;

import android.content.Context;
import com.huawei.systemmanager.rainbow.client.helper.LocalSharedPrefrenceHelper;
import com.huawei.systemmanager.util.HwLog;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbsOutputHandle<T> implements IOutputItf<T> {
    private static final String TAG = "AbsOutputHandle";
    private long mLocalVersion = 0;

    abstract String getLocalVersionKey();

    abstract T parseJSONObject(JSONObject jSONObject) throws JSONException;

    abstract void updateDatabase(Context context);

    abstract boolean validOutputData();

    public void parseAndUpdate(Context ctx, JSONObject jsonObject) throws JSONException {
        parseJSONObject(jsonObject);
        if (validOutputData()) {
            new LocalSharedPrefrenceHelper(ctx).putLong(getLocalVersionKey(), this.mLocalVersion);
            updateDatabase(ctx);
            return;
        }
        HwLog.w(TAG, "AbsOutputHandle invalid output data!");
    }

    protected long getLocalVersion() {
        return this.mLocalVersion;
    }

    protected void setLocalVersion(long version) {
        this.mLocalVersion = version;
    }
}
