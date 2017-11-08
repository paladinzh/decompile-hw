package com.huawei.systemmanager.adblock.ui.apkdlcheck;

import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.text.TextUtils;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.adblock.comm.AdConst;
import com.huawei.systemmanager.adblock.comm.AdDispatcher;
import com.huawei.systemmanager.adblock.comm.AdUtils;
import com.huawei.systemmanager.adblock.ui.apkdlcheck.DlBlockManager.Record;
import com.huawei.systemmanager.adblock.ui.connect.result.AdCheckUrlResult;
import com.huawei.systemmanager.adblock.ui.connect.result.AdCheckUrlResult.UpdateTimestampRunnable;
import com.huawei.systemmanager.adblock.ui.view.DlChoiceDialog;
import com.huawei.systemmanager.adblock.ui.view.InstallAppmarketDialog;
import com.huawei.systemmanager.adblock.ui.view.dlblock.DlBlockRecordListActivity;
import com.huawei.systemmanager.comm.concurrent.HsmExecutor;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.NotificationID;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;

class DlUrlCheckTask implements IDlUrlCheckTask {
    private static final long COUNT_DOWN_INTERVAL = 1000;
    private static final String TAG = "AdBlock_DlUrlCheckTask";
    private static final long TIME_OUT = 10900;
    private boolean isFinished;
    private AdCountDownTimer mAdCountDownTimer;
    private DlChoiceDialog mAdDlChoiceDialog;
    private final Context mAppContext;
    private final Callback mCallback;
    private DlAppIconTask mDlAppIconTask;
    private String mDownloadId;
    private String mDownloaderPkgName = "";
    private AdCheckUrlResult mResult;
    private long mStartTime = 0;
    private int mUid = -1;
    private final String mUrl;

    public interface Callback {
        void onTaskFinish(IDlUrlCheckTask iDlUrlCheckTask);
    }

    private class AdCountDownTimer extends CountDownTimer {
        private final AdCheckUrlResult mResult;

        public AdCountDownTimer(long millisInFuture, long countDownInterval, AdCheckUrlResult result) {
            super(millisInFuture, countDownInterval);
            this.mResult = result;
        }

        public void onTick(long millisUntilFinished) {
            if (DlUrlCheckTask.this.mAdDlChoiceDialog != null) {
                DlUrlCheckTask.this.mAdDlChoiceDialog.setRemainingTime((int) (millisUntilFinished / 1000));
            }
        }

        public void onFinish() {
            HwLog.i(DlUrlCheckTask.TAG, "time out consume=" + (SystemClock.elapsedRealtime() - DlUrlCheckTask.this.mStartTime));
            if (DlUrlCheckTask.this.mDlAppIconTask != null) {
                DlUrlCheckTask.this.mDlAppIconTask.cancel(true);
            }
            if (DlUrlCheckTask.this.mAdDlChoiceDialog != null && DlUrlCheckTask.this.mAdDlChoiceDialog.isShowing()) {
                try {
                    if (this.mResult.getOptPolicy() == 1 || this.mResult.getOptPolicy() == 2) {
                        this.mResult.setOptPolicy(4);
                    } else {
                        this.mResult.setOptPolicy(0);
                    }
                    DlUrlCheckTask.this.mAdDlChoiceDialog.cancel();
                } catch (RuntimeException e) {
                    HwLog.w(DlUrlCheckTask.TAG, "onFinish RuntimeException", e);
                }
            }
            if (this.mResult.getOptPolicy() == 1 || this.mResult.getOptPolicy() == 2) {
                DlUrlCheckTask.this.setResult(false);
            } else {
                DlUrlCheckTask.this.setResult(true);
            }
            DlUrlCheckTask.this.record(this.mResult);
            DlUrlCheckTask.this.statDlBlocked(this.mResult, 9);
            DlUrlCheckTask.this.onTaskFinish();
        }
    }

    private static class SetResultRunnable implements Runnable {
        private final boolean mContinue;
        private final String mDlId;

        SetResultRunnable(String downloadId, boolean isContinue) {
            this.mDlId = downloadId;
            this.mContinue = isContinue;
        }

        public void run() {
            AdDispatcher.setApkDlUrlUserResult(this.mDlId, this.mContinue);
        }
    }

    public DlUrlCheckTask(Context context, Callback callback, Bundle bundle) {
        this.mAppContext = context;
        this.mCallback = callback;
        this.mDownloadId = bundle.getString(AdConst.BUNDLE_KEY_DOWNLOADID);
        this.mUrl = bundle.getString("url");
        this.mStartTime = bundle.getLong(AdConst.BUNDLE_KEY_STARTTIME, 0);
        String uidStr = bundle.getString("uid");
        getDownloader(context, uidStr);
        HwLog.i(TAG, "new downloadId=" + this.mDownloadId + ", startTime=" + this.mStartTime + ", uid=" + uidStr + ", url=" + this.mUrl + ",mDownloaderPkgName=" + this.mDownloaderPkgName);
    }

    private void getDownloader(Context context, String uidStr) {
        try {
            this.mUid = Integer.parseInt(uidStr);
        } catch (NumberFormatException e) {
            HwLog.e(TAG, "AdDlRequest uid is error", e);
        }
        this.mDownloaderPkgName = AdUtils.getFistPackageName(context, this.mUid);
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof DlUrlCheckTask)) {
            return false;
        }
        DlUrlCheckTask other = (DlUrlCheckTask) o;
        if (TextUtils.equals(this.mUrl, other.mUrl) && this.mUid == other.mUid) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return (TextUtils.isEmpty(this.mUrl) ? 0 : this.mUrl.hashCode()) + this.mUid;
    }

    private synchronized void onTaskFinish() {
        if (!this.isFinished) {
            this.isFinished = true;
            this.mCallback.onTaskFinish(this);
        }
    }

    public boolean isValid() {
        if (TextUtils.isEmpty(this.mDownloadId) || TextUtils.isEmpty(this.mUrl) || this.mStartTime <= 0 || this.mUid < 0) {
            return false;
        }
        return true;
    }

    public String getDownloadId() {
        return this.mDownloadId;
    }

    public void execute() {
        Record record = DlBlockManager.getInstance().getTempRecord(this.mUid, this.mUrl);
        if (record == null || -1 == record.mOptPolicy) {
            new DlUrlCheckOnlineTask(this.mAppContext, this.mUrl, this.mUid, this.mDownloaderPkgName, this).executeOnExecutor(HsmExecutor.THREAD_POOL_EXECUTOR, new Void[0]);
        } else if (record.mOptPolicy == 0) {
            setResult(true);
            onTaskFinish();
        } else {
            setResult(false);
            sendNotification(record);
            AdCheckUrlResult.updateTimestamp(this.mAppContext, this.mUid, record.mPkgName);
            onTaskFinish();
        }
    }

    public void setResult(boolean isContinue) {
        HsmExecutor.THREAD_POOL_EXECUTOR.execute(new SetResultRunnable(this.mDownloadId, isContinue));
    }

    private void sendNotification(Record record) {
        String downloaderName = AdUtils.getAppName(this.mAppContext, this.mDownloaderPkgName);
        PendingIntent pi = PendingIntent.getActivity(this.mAppContext, 0, new Intent(this.mAppContext, DlBlockRecordListActivity.class), 134217728);
        Intent allowDownloadIntent = new Intent(this.mAppContext, DlAllowOrignalService.class);
        allowDownloadIntent.putExtra("url", this.mUrl);
        allowDownloadIntent.putExtra("pkg", record.mPkgName);
        allowDownloadIntent.putExtra("uid", this.mUid);
        ((NotificationManager) this.mAppContext.getSystemService("notification")).notify(NotificationID.APK_DL_BLOCK, new Builder(this.mAppContext).setSmallIcon(R.drawable.ic_flow_notifi).setContentTitle(this.mAppContext.getString(R.string.ad_dl_notification_title, new Object[]{downloaderName, record.mApkName})).setContentText(this.mAppContext.getString(R.string.roaming_traffic_notification_summary)).setAutoCancel(true).setContentIntent(pi).addAction(0, this.mAppContext.getString(R.string.ad_dl_notification_allow_btn), PendingIntent.getService(this.mAppContext, 0, allowDownloadIntent, 134217728)).build());
    }

    public void onCheckOnlineFinish(AdCheckUrlResult result) {
        HwLog.i(TAG, "onCheckOnlineFinish downloadId=" + this.mDownloadId + result.toString());
        this.mResult = result;
        long timeRemaining = (this.mStartTime + TIME_OUT) - SystemClock.elapsedRealtime();
        if (timeRemaining <= 0 || result.getOptPolicy() <= 0 || AdUtils.isPackageMatchUid(this.mAppContext, this.mUid, result.getApkPkgName()) || !AdUtils.shouldShowAppmarkedDialog(this.mAppContext)) {
            setResult(true);
            onTaskFinish();
        } else {
            if (result.getUidPkgName().length() > 0) {
                Record record = AdCheckUrlResult.getDlBlockByUIDPkg(this.mAppContext, result.getUidPkgName());
                if (!(record == null || 3 == record.mOptPolicy)) {
                    if (record.mOptPolicy == 0) {
                        setResult(true);
                    } else {
                        setResult(false);
                        sendNotification(record);
                        HsmExecutor.THREAD_POOL_EXECUTOR.execute(new UpdateTimestampRunnable(this.mAppContext, this.mUid, record.mPkgName));
                        DlBlockManager.getInstance().setTempRecord(this.mUid, this.mUrl, record);
                    }
                    onTaskFinish();
                    return;
                }
            }
            showDialog(timeRemaining, result);
        }
    }

    private void showDialog(long timeRemaining, AdCheckUrlResult result) {
        this.mAdCountDownTimer = new AdCountDownTimer(timeRemaining, 1000, result);
        this.mAdCountDownTimer.start();
        this.mAdDlChoiceDialog = new DlChoiceDialog(this.mAppContext, this.mAppContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null), AdUtils.getFistPackageLabel(this.mAppContext, this.mUid), (int) (timeRemaining / 1000), result, this);
        this.mAdDlChoiceDialog.show();
        this.mDlAppIconTask = new DlAppIconTask(this.mAppContext, result.getIcon(), this.mAdDlChoiceDialog);
        this.mDlAppIconTask.execute(new Void[0]);
    }

    public void onChoose(AdCheckUrlResult result, int choice) {
        if (this.mAdCountDownTimer != null) {
            this.mAdCountDownTimer.cancel();
        }
        long now = SystemClock.elapsedRealtime();
        long consume = now - this.mStartTime;
        StringBuilder sb = new StringBuilder();
        sb.append("onChoose result=").append(result.toString()).append(", choice=").append(choice).append(", downloadId=").append(this.mDownloadId).append(", startTime=").append(this.mStartTime).append(", now=").append(now).append(", consume=").append(consume);
        HwLog.i(TAG, sb.toString());
        switch (choice) {
            case 0:
                result.setOptPolicy(4);
                setResult(false);
                statDlBlocked(result, 0);
                break;
            case 1:
                result.setOptPolicy(4);
                setResult(false);
                downloadByAppmarket(this.mAppContext, result);
                statDlBlocked(result, 1);
                break;
            case 2:
                setResult(true);
                result.setOptPolicy(0);
                statDlBlocked(result, 2);
                break;
        }
        record(result);
        if (1 != choice) {
            onTaskFinish();
        }
    }

    private void record(AdCheckUrlResult result) {
        if (result != null) {
            try {
                DlBlockManager.getInstance().record(this.mAppContext, this.mUid, this.mUrl, result);
            } catch (Throwable e) {
                HwLog.w("record exception", e);
            }
        }
    }

    private void downloadByAppmarket(Context context, AdCheckUrlResult result) {
        ApplicationInfo info = AdUtils.getAppmarket(context);
        if (info == null) {
            showInstallAppmarketDialog();
        } else if (info.enabled) {
            AdUtils.downloadApkByAppmarket(context, result.getDetailId(), result.getApkAppName());
            onTaskFinish();
        } else {
            new InstallAppmarketTask(this.mAppContext, this).execute();
        }
    }

    private void showInstallAppmarketDialog() {
        new InstallAppmarketDialog(this.mAppContext, this.mAppContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null), this).show();
    }

    public void onChooseInstallAppmarket(boolean install) {
        HwLog.i(TAG, "onChooseInstallAppmarket install=" + install);
        statAppmarketInstall(install);
        if (install) {
            new InstallAppmarketTask(this.mAppContext, this).execute();
        } else {
            onTaskFinish();
        }
    }

    public void onInstallAppmarketFinish(boolean success) {
        HwLog.i(TAG, "onInstallAppmarketFinish onFinish=" + success);
        if (success && this.mResult != null) {
            AdUtils.downloadApkByAppmarket(this.mAppContext, this.mResult.getDetailId(), this.mResult.getApkAppName());
        }
        onTaskFinish();
    }

    private void statDlBlocked(AdCheckUrlResult result, int op) {
        HsmPkgInfo info = HsmPackageManager.getInstance().getPkgInfo(this.mDownloaderPkgName);
        if (info == null || result == null) {
            HwLog.i(TAG, "statAdViewBlocked stat info or mResult is null");
            return;
        }
        String label = info.label();
        String version = String.valueOf(info.getVersionCode());
        HwLog.i(TAG, "statAdViewBlocked HsmStat:" + HsmStatConst.constructJsonParams(HsmStatConst.PARAM_PKG, this.mDownloaderPkgName, HsmStatConst.PARAM_LABEL, label, HsmStatConst.PARAM_VERSION, version, HsmStatConst.PARAM_OP, String.valueOf(op), HsmStatConst.PARAM_PKG2, result.getApkPkgName(), HsmStatConst.PARAM_LABEL2, result.getApkAppName(), HsmStatConst.PARAM_ID, result.getDetailId()));
        HsmStat.statE((int) Events.E_APP_DOWNLOAD, statParam);
    }

    private void statAppmarketInstall(boolean install) {
        String[] strArr = new String[2];
        strArr[0] = HsmStatConst.PARAM_OP;
        strArr[1] = install ? "1" : "0";
        HwLog.i(TAG, "statAppmarketInstall HsmStat:" + HsmStatConst.constructJsonParams(strArr));
        HsmStat.statE((int) Events.E_APPMARKET_INSTALL, statParam);
    }

    public void statAdUrlBlocked() {
        HsmPkgInfo info = HsmPackageManager.getInstance().getPkgInfo(this.mDownloaderPkgName);
        if (info == null) {
            HwLog.i(TAG, "statAdViewBlocked stat info is null");
            return;
        }
        String label = info.label();
        String version = String.valueOf(info.getVersionCode());
        HwLog.i(TAG, "statAdViewBlocked HsmStat:" + HsmStatConst.constructJsonParams(HsmStatConst.PARAM_PKG, this.mDownloaderPkgName, HsmStatConst.PARAM_LABEL, label, HsmStatConst.PARAM_VERSION, version, HsmStatConst.PARAM_VAL, this.mUrl));
        HsmStat.statE((int) Events.E_ADVERTISE_URL_BLOCKED, statParam);
    }
}
