package com.android.mms.transaction;

import android.content.Context;
import android.net.Uri;
import com.google.android.mms.MmsException;
import com.huawei.cspcommon.MLog;

public class StopRetrieveTransaction extends Transaction {
    private final Uri mUri;

    public StopRetrieveTransaction(Context context, int serviceId, TransactionSettings connectionSettings, String uri) throws MmsException {
        super(context, serviceId, connectionSettings);
        if (uri.startsWith("content://")) {
            this.mUri = Uri.parse(uri);
            return;
        }
        throw new IllegalArgumentException("Initializing from X-Mms-Content-Location is abandoned!");
    }

    public void run() {
        try {
            MLog.i("Mms_TXM_SRT", "start stopdownload " + this.mUri.toString());
            stopDownload(this.mUri.toString());
        } catch (Throwable t) {
            MLog.e("Mms_TXM_SRT", MLog.getStackTraceString(t));
        }
    }

    public int getType() {
        return 4;
    }

    public Uri getUri() {
        return this.mUri;
    }
}
