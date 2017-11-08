package com.huawei.systemmanager.secpatch.net;

import android.content.Context;
import com.google.javax.annotation.Nonnull;
import com.huawei.systemmanager.rainbow.comm.request.AbsServerRequest;
import com.huawei.systemmanager.rainbow.comm.request.ICommonRequest.RequestType;
import com.huawei.systemmanager.secpatch.common.ConstValues;
import com.huawei.systemmanager.secpatch.common.SecPatchQueryResult;
import com.huawei.systemmanager.secpatch.util.SecPatchHelper;
import com.huawei.systemmanager.util.HwLog;
import org.json.JSONException;
import org.json.JSONObject;

public class SecPatchQueryRequest extends AbsServerRequest {
    public static final String TAG = "SecPatchQueryRequest";
    private String mCver = "";
    private String mOpt = "all";
    private String mPver = "";
    private SecPatchQueryResult mSecPatch = null;

    public SecPatchQueryRequest(String opt, String pver, String cver) {
        this.mOpt = opt;
        this.mPver = pver;
        this.mCver = cver;
    }

    public SecPatchQueryResult getRequestResult() {
        return this.mSecPatch;
    }

    protected RequestType getRequestType() {
        return RequestType.REQUEST_POST;
    }

    protected String getRequestUrl(RequestType arg0) {
        return ConstValues.URL_QUERY;
    }

    protected void addExtPostRequestParam(@Nonnull Context ctx, JSONObject param) {
        try {
            param.put("pver", this.mPver);
            param.put(ConstValues.KEY_CVER, this.mCver);
            param.put(ConstValues.KEY_OPT, this.mOpt);
        } catch (JSONException ex) {
            HwLog.e(TAG, "addExtPostRequestParam catch JSONException: " + ex.getMessage());
        }
    }

    protected int checkResponseCode(Context ctx, int resultCode) {
        return SecPatchHelper.checkServerResponseCode(resultCode);
    }

    protected void parseResponseAndPost(@Nonnull Context ctx, JSONObject jsonResponse) throws JSONException {
        this.mSecPatch = SecPatchParser.parseSecPatchList(jsonResponse);
    }
}
