package com.huawei.systemmanager.secpatch.net;

import android.content.Context;
import com.google.javax.annotation.Nonnull;
import com.huawei.systemmanager.rainbow.comm.request.AbsServerRequest;
import com.huawei.systemmanager.rainbow.comm.request.ICommonRequest.RequestType;
import com.huawei.systemmanager.secpatch.common.ConstValues;
import com.huawei.systemmanager.secpatch.common.SecPatchCheckResult;
import com.huawei.systemmanager.secpatch.util.SecPatchHelper;
import com.huawei.systemmanager.util.HwLog;
import org.json.JSONException;
import org.json.JSONObject;

public class SecPatchCheckRequest extends AbsServerRequest {
    private static final String TAG = "SecPatchCheckRequest";
    private SecPatchCheckResult mCheckResult = null;
    private String mPver = "";

    public SecPatchCheckRequest(String pver) {
        this.mPver = pver;
    }

    public void setPver(String pver) {
        this.mPver = pver;
    }

    public SecPatchCheckResult getRequestResult() {
        return this.mCheckResult;
    }

    protected RequestType getRequestType() {
        return RequestType.REQUEST_POST;
    }

    protected String getRequestUrl(RequestType type) {
        return ConstValues.URL_CHECK;
    }

    protected void addExtPostRequestParam(@Nonnull Context ctx, @Nonnull JSONObject param) {
        try {
            param.put("pver", this.mPver);
        } catch (JSONException ex) {
            HwLog.e(TAG, "addExtPostRequestParam catch JSONException: " + ex.getMessage());
        }
    }

    protected int checkResponseCode(Context ctx, int resultCode) {
        return SecPatchHelper.checkServerResponseCode(resultCode);
    }

    protected void parseResponseAndPost(@Nonnull Context ctx, JSONObject jsonResponse) throws JSONException {
        this.mCheckResult = SecPatchParser.parseCheckResult(jsonResponse);
    }
}
