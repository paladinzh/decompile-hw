package com.huawei.mms.util;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.cspcommon.ex.SqliteWrapper;

public class RiskUrlCheckService extends IntentService {
    public RiskUrlCheckService() {
        super("checkRiskUrlThread");
    }

    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String str = null;
            String str2 = null;
            String uriString = null;
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                str = bundle.getString("msg_address");
                str2 = bundle.getString("msg_body");
                uriString = bundle.getString("msg_uri");
            }
            String pos = HwMessageUtils.getRiskUrlPosString(this, str2);
            if (TextUtils.isEmpty(pos) || "0,".equals(pos)) {
                pos = HwMessageUtils.getUnOfficialUrlPosString(this, str, str2);
            }
            if (!TextUtils.isEmpty(pos)) {
                Uri messageUri = Uri.parse(uriString);
                ContentValues values = new ContentValues();
                values.put("risk_url_body", pos);
                SqliteWrapper.update(this, getContentResolver(), messageUri, values, null, null);
            }
        }
    }
}
