package com.android.mms.transaction;

import android.content.Context;
import android.util.Log;
import org.apache.http.HttpRequest;

public class HwCustHttpUtils {
    private static final String TAG = "HwCustHttpUtils";

    public void checkHttpHeaderUseCurrentLocale(StringBuilder buffer) {
    }

    public void addHeader(Context context, HttpRequest req, int Method) {
        Log.d(TAG, "addHeader to HttpRequest");
    }

    public String getCustomUserAgent(Context context, String userAgent) {
        return userAgent;
    }

    public String getChameleonUAprof(Context context) {
        return null;
    }
}
