package com.huawei.systemmanager.rainbow.client.connect.request;

import android.content.Context;
import com.huawei.systemmanager.rainbow.client.helper.ServerRequestHelper;
import com.huawei.systemmanager.rainbow.comm.request.AbsServerRequest;
import com.huawei.systemmanager.rainbow.comm.request.ICommonRequest.RequestType;
import com.huawei.systemmanager.util.HwLog;
import org.json.JSONException;
import org.json.JSONObject;

public class GetFileVersionRequest extends AbsServerRequest {
    private static final String RESPONSE_SIGNATURE_FIELD = "key";
    private static final String RESPONSE_URL_FIELD = "downloadUrl";
    private static final String RESPONSE_VER_FIELD = "ver";
    private static final String TAG = "GetFileVersionRequest";
    private static final String URL = "https://configserver.hicloud.com/servicesupport/updateserver/getLatestVersion";
    private boolean mCanDownload = false;
    private String mDownloadUrl = null;
    private String mFileId = null;
    private String mLocalVer = null;
    private String mServerVer = null;
    private String mSignature = null;

    public GetFileVersionRequest(String fileId, String ver) {
        this.mFileId = fileId;
        this.mLocalVer = ver;
    }

    public boolean canDownload() {
        return this.mCanDownload;
    }

    public String getDownloadUrl() {
        return this.mDownloadUrl;
    }

    public String getLocalVer() {
        return this.mLocalVer;
    }

    public String getServerVer() {
        return this.mServerVer;
    }

    public String getSignature() {
        return this.mSignature;
    }

    protected String getRequestUrl(RequestType type) {
        return URL;
    }

    protected void addExtPostRequestParam(Context ctx, JSONObject param) {
        try {
            HwLog.d(TAG, "mFileId is " + this.mFileId);
            param.put("fileId", this.mFileId);
            param.put("ver", this.mLocalVer);
        } catch (JSONException ex) {
            HwLog.e(TAG, "addExtPostRequestParam catch JSONException: " + ex.getMessage());
        }
    }

    protected int checkResponseCode(Context ctx, int resultCode) {
        if (resultCode == 200000) {
            return 0;
        }
        return ServerRequestHelper.checkServerResponseCode(resultCode);
    }

    protected void parseResponseAndPost(Context ctx, JSONObject jsonResponse) {
        HwLog.d(TAG, "parseResponseAndPost" + jsonResponse);
        this.mCanDownload = false;
        try {
            if (jsonResponse.has("ver") && jsonResponse.has("downloadUrl")) {
                Long localVersion = Long.valueOf(Long.parseLong(this.mLocalVer));
                this.mServerVer = jsonResponse.getString("ver");
                if (jsonResponse.has("key")) {
                    this.mSignature = jsonResponse.getString("key");
                }
                Long serverVersion = Long.valueOf(Long.parseLong(this.mServerVer));
                HwLog.d(TAG, "localver " + this.mLocalVer + ", serverVer " + this.mServerVer + ", mSignature " + this.mSignature);
                if (serverVersion.longValue() > localVersion.longValue()) {
                    this.mDownloadUrl = jsonResponse.getString("downloadUrl");
                    this.mCanDownload = true;
                    return;
                }
                return;
            }
            HwLog.e(TAG, "error, field ver or downloadUrl does not exist");
        } catch (JSONException jex) {
            HwLog.e(TAG, "error.", jex);
        } catch (Exception ex) {
            HwLog.e(TAG, "error.", ex);
        }
    }

    protected String getResultCodeFiled() {
        return "resultCode";
    }
}
