package com.huawei.systemmanager.adblock.ui.apkdlcheck;

import android.content.Context;
import android.os.AsyncTask;
import com.huawei.systemmanager.adblock.ui.connect.request.DlUrlCheckRequest;
import com.huawei.systemmanager.adblock.ui.connect.result.AdCheckUrlResult;

class DlUrlCheckOnlineTask extends AsyncTask<Void, Void, AdCheckUrlResult> {
    private final Context mAppContext;
    private final Callback mCallback;
    private final String mDownloaderPkgName;
    private final int mUid;
    private final String mUrl;

    public interface Callback {
        void onCheckOnlineFinish(AdCheckUrlResult adCheckUrlResult);
    }

    public DlUrlCheckOnlineTask(Context context, String url, int uid, String downloaderPkgName, Callback callback) {
        this.mAppContext = context;
        this.mUrl = url;
        this.mUid = uid;
        this.mDownloaderPkgName = downloaderPkgName;
        this.mCallback = callback;
    }

    protected AdCheckUrlResult doInBackground(Void... params) {
        DlUrlCheckRequest request = new DlUrlCheckRequest(this.mAppContext, this.mUrl, this.mUid, this.mDownloaderPkgName);
        request.processRequest(this.mAppContext);
        return request.getCheckResult();
    }

    protected void onPostExecute(AdCheckUrlResult result) {
        super.onPostExecute(result);
        this.mCallback.onCheckOnlineFinish(result);
    }
}
