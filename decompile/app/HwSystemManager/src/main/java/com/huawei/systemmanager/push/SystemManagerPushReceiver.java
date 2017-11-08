package com.huawei.systemmanager.push;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import com.huawei.android.pushagent.PushReceiver;
import com.huawei.systemmanager.antimal.MalwareConst;
import com.huawei.systemmanager.comm.misc.StringUtils;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.rainbow.client.connect.RequestMgr;
import com.huawei.systemmanager.securitythreats.comm.SecurityThreatsConst;
import com.huawei.systemmanager.useragreement.UserAgreementHelper;
import com.huawei.systemmanager.util.HwLog;
import java.io.UnsupportedEncodingException;
import org.json.JSONException;
import org.json.JSONObject;

public class SystemManagerPushReceiver extends PushReceiver {
    private static final String SEPARATOR = "$$";
    private static final String TAG = "SystemManagerPushReceiver";
    private PushResponse mPushResponse;

    public void onPushMsg(Context context, byte[] msg, String token) {
        try {
            JSONObject jsonResponse = new JSONObject(new String(msg, "UTF-8"));
            this.mPushResponse = parseMsg(jsonResponse);
            HwLog.i(TAG, "json " + jsonResponse);
            boolean isAntiMalFeature = false;
            if (this.mPushResponse != null) {
                HwLog.d(TAG, "msg " + this.mPushResponse.toString());
                isAntiMalFeature = TextUtils.equals(MalwareConst.ANTI_MAL_MODULE, this.mPushResponse.module);
            }
            if (isAntiMalFeature || UserAgreementHelper.getUserAgreementState(context)) {
                if (this.mPushResponse != null) {
                    if (StringUtils.isEmpty(this.mPushResponse.fileId)) {
                        this.mPushResponse.sendNormalIntent(context);
                    } else {
                        this.mPushResponse.sendFileIntent(context);
                    }
                }
                return;
            }
            HwLog.w(TAG, "user has not agree, return.");
        } catch (UnsupportedEncodingException e) {
            HwLog.e(TAG, "onPushMsg error.", e);
        } catch (JSONException ex) {
            HwLog.e(TAG, "onPushMsg error.", ex);
        }
    }

    private PushResponse parseMsg(JSONObject jsonResponse) {
        PushResponse response = new PushResponse();
        try {
            response.pushType = jsonResponse.getString(PushResponse.PUSH_TYPE_FIELD);
            response.packageName = jsonResponse.getString("packageName");
            response.module = jsonResponse.getString(PushResponse.MODULE_FIELD);
            response.action = jsonResponse.getString("action");
            response.romVersion = Build.DISPLAY;
            if (jsonResponse.has(PushResponse.FILE_NAME_FIELD)) {
                response.fileName = jsonResponse.getString(PushResponse.FILE_NAME_FIELD);
                if (!StringUtils.isEmpty(response.fileName)) {
                    String romVersion;
                    if ("com.huawei.systemmanager".equals(response.packageName) && SecurityThreatsConst.PUSH_FILE_MODULE.equals(response.module) && SecurityThreatsConst.PUSH_FILE_FILE_NAME.equals(response.fileName)) {
                        romVersion = SecurityThreatsConst.PUSH_FILE_ROM;
                    } else {
                        romVersion = response.romVersion;
                    }
                    response.fileId = response.packageName + "$$" + romVersion + "$$" + response.module + "$$" + response.fileName;
                }
            }
            if (jsonResponse.has(PushResponse.DATA_FIELD)) {
                response.data = jsonResponse.getString(PushResponse.DATA_FIELD);
            }
            return response;
        } catch (JSONException ex) {
            HwLog.e(TAG, "parseMsg : " + jsonResponse, ex);
            return null;
        } catch (Exception e) {
            HwLog.e(TAG, "parseMsg : " + jsonResponse, e);
            return null;
        }
    }

    public void onToken(Context context, String token) {
        Utility.setTokenRegistered(context, false);
        Utility.setRegisteToken(context, token);
        RequestMgr.generateDeviceTokenRequest(context, token).processRequest(context);
        HwLog.i(TAG, "token processRequest end");
    }
}
