package com.huawei.systemmanager.adblock.ui.connect.request;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.systemmanager.adblock.comm.AdConst.CloudResult;
import com.huawei.systemmanager.adblock.comm.AdUtils;
import com.huawei.systemmanager.adblock.ui.connect.result.AdCheckUrlResult;
import com.huawei.systemmanager.rainbow.comm.request.AbsAdRequest;
import com.huawei.systemmanager.rainbow.comm.request.ICommonRequest.RequestType;
import com.huawei.systemmanager.util.HwLog;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class DlUrlCheckRequest extends AbsAdRequest {
    private static final int CONNECTED_TIME_OUT = 3000;
    private static final int READ_TIME_OUT = 4000;
    private static final String TAG = "AdBlock_DlUrlCheckRequest";
    private AdCheckUrlResult mCheckResult = new AdCheckUrlResult();
    private Context mContext;
    private final String mDownloaderPkgName;
    private final int mUid;
    private final String mUrl;

    public DlUrlCheckRequest(Context context, String url, int uid, String downloaderPkgName) {
        setTimeout(3000, READ_TIME_OUT);
        this.mContext = context;
        this.mUrl = url;
        this.mUid = uid;
        this.mDownloaderPkgName = downloaderPkgName;
    }

    protected boolean shouldRun(Context ctx) {
        boolean run = !TextUtils.isEmpty(this.mUrl);
        if (!run) {
            HwLog.e(TAG, "shouldRun url is empty");
        }
        return run;
    }

    protected String getRequestUrl(RequestType type) {
        return "https://appsec.hicloud.com/hwmarket/installmgr/client/api";
    }

    protected void addExtPostRequestParam(Context ctx, Map<String, String> map) {
        addAdBlockExtParam(ctx, map);
    }

    protected int checkResponseCode(Context ctx, int resultCode) {
        return 0;
    }

    protected void parseResponseAndPost(Context ctx, JSONObject jsonResponse) throws JSONException {
        HwLog.i(TAG, "parseResponseAndPost:" + jsonResponse.toString());
        setResult(jsonResponse);
    }

    private void addAdBlockExtParam(Context ctx, Map<String, String> map) {
        map.put("method", "store.urlCheck");
        map.put("url", getUrl());
    }

    private String getUrl() {
        if (this.mUrl == null) {
            return "";
        }
        return this.mUrl.replaceAll("\\r", "").replaceAll("\\n", "");
    }

    public void setResult(JSONObject jsonResponse) {
        if (jsonResponse == null) {
            HwLog.e(TAG, "handleResult jsonResponse is null");
            return;
        }
        try {
            int rtnCode = jsonResponse.getInt(CloudResult.AD_RESULT_RTNCODE);
            if (rtnCode != 0) {
                HwLog.w(TAG, "handleResult rtnCode is not ok:" + rtnCode);
                return;
            }
            JSONObject jSONObject = jsonResponse;
            this.mCheckResult.init(jSONObject, this.mDownloaderPkgName, AdUtils.getAppName(this.mContext, this.mDownloaderPkgName), Integer.toString(this.mUid), this.mUrl);
        } catch (JSONException e) {
            HwLog.w(TAG, "setResult JSONException", e);
            this.mCheckResult.setOptPolicy(0);
        } catch (RuntimeException e2) {
            HwLog.w(TAG, "setResult RuntimeException", e2);
            this.mCheckResult.setOptPolicy(0);
        }
    }

    public AdCheckUrlResult getCheckResult() {
        return this.mCheckResult;
    }
}
