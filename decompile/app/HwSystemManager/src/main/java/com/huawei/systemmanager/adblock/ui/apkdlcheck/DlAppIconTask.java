package com.huawei.systemmanager.adblock.ui.apkdlcheck;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import com.huawei.systemmanager.adblock.ui.connect.request.DlAppIconRequest;
import com.huawei.systemmanager.adblock.ui.view.DlChoiceDialog;
import com.huawei.systemmanager.util.HwLog;

class DlAppIconTask extends AsyncTask<Void, Void, Drawable> {
    private static final String TAG = "AdBlock_DlAppIconTask";
    private final Context mAppContext;
    private final DlChoiceDialog mDialog;
    private final String mUrl;

    public DlAppIconTask(Context context, String url, DlChoiceDialog dialog) {
        this.mAppContext = context;
        this.mUrl = url;
        this.mDialog = dialog;
    }

    protected Drawable doInBackground(Void... params) {
        DlAppIconRequest request = new DlAppIconRequest(this.mUrl);
        request.processRequest(this.mAppContext);
        return request.getIcon();
    }

    protected void onPostExecute(Drawable drawable) {
        super.onPostExecute(drawable);
        if (isCancelled()) {
            HwLog.w(TAG, "setAppIcon isCancelled");
        } else if (drawable == null) {
            HwLog.w(TAG, "setAppIcon drawable is null");
        } else {
            try {
                this.mDialog.setAppIcon(drawable);
            } catch (RuntimeException e) {
                HwLog.w(TAG, "setAppIcon RuntimeException", e);
            }
        }
    }
}
