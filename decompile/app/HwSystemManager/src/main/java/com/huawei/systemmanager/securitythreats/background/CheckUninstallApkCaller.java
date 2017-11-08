package com.huawei.systemmanager.securitythreats.background;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import com.huawei.systemmanager.antivirus.engine.tencent.TencentAntiVirusEngine;
import com.huawei.systemmanager.antivirus.utils.AntiVirusTools;
import com.huawei.systemmanager.comm.concurrent.HsmExecutor;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.sdk.tmsdk.TMSEngineFeature;
import com.huawei.systemmanager.securitythreats.comm.SecurityThreatsConst;
import com.huawei.systemmanager.service.CustomCaller;
import com.huawei.systemmanager.useragreement.UserAgreementHelper;
import com.huawei.systemmanager.util.HwLog;
import java.util.concurrent.atomic.AtomicBoolean;

public class CheckUninstallApkCaller extends CustomCaller {
    private static final int RESULT_CODE_ERROR_PARAM = 1;
    private static final int RESULT_CODE_ERROR_TIMEOUT = 3;
    private static final int RESULT_CODE_OK = 0;
    private static final int RESULT_TYPE_UNKOWN = -1;
    private static final String TAG = "CheckUninstallApkCaller";
    private static final long TIME_OUT = 5000;
    private TencentAntiVirusEngine mEngine;

    private static class CheckUninstallApkRequest {
        AtomicBoolean mExpired = new AtomicBoolean(false);
        Object mLock = new Object();
        int mResult = -1;

        public synchronized int getResult() {
            return this.mResult;
        }

        public synchronized void setResult(int result) {
            this.mResult = result;
        }

        public void waitRequestResult(long timeout) {
            synchronized (this.mLock) {
                long begin;
                do {
                    try {
                        if (-1 != getResult()) {
                            break;
                        }
                        begin = SystemClock.elapsedRealtime();
                        this.mLock.wait(timeout);
                    } catch (InterruptedException e) {
                        HwLog.w(CheckUninstallApkCaller.TAG, "waitRequestResult InterruptedException", e);
                    }
                } while (SystemClock.elapsedRealtime() - begin < timeout);
                this.mExpired.set(true);
            }
        }

        public void releaseLock() {
            synchronized (this.mLock) {
                this.mLock.notifyAll();
            }
        }

        public boolean isExpired() {
            return this.mExpired.get();
        }
    }

    public String getMethodName() {
        return SecurityThreatsConst.METHOD_CHECK_UNINSTALL_APK;
    }

    public Bundle call(Bundle params) {
        if (params == null) {
            HwLog.w(TAG, "checkUninstallApk params is null, return error");
            return generateResult(1, -1);
        }
        String name = params.getString("name", "");
        String path = params.getString(SecurityThreatsConst.CHECK_UNINSTALL_PKG_PATH, "");
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(path)) {
            HwLog.w(TAG, "checkUninstallApk name or path is empty, return error");
            return generateResult(1, -1);
        }
        String source = params.getString(SecurityThreatsConst.CHECK_UNINSTALL_PKG_SOURCE, "");
        HwLog.i(TAG, "checkUninstallApk name=" + name + ", source=" + source);
        return check(name, path, source, new CheckUninstallApkRequest());
    }

    private Bundle generateResult(int resultCode, int virusType) {
        Bundle result = new Bundle();
        result.putInt(SecurityThreatsConst.CHECK_UNINSTALL_RESULT_CODE, resultCode);
        result.putInt(SecurityThreatsConst.CHECK_UNINSTALL_VIRUS_TYPE, virusType);
        return result;
    }

    private boolean initCheckManager() {
        if (TMSEngineFeature.isSupportTMS()) {
            synchronized (this) {
                if (this.mEngine == null) {
                    this.mEngine = new TencentAntiVirusEngine();
                    this.mEngine.onInit(GlobalContext.getContext());
                }
            }
            return true;
        }
        HwLog.w(TAG, "initCheckManager: TMS feature is not supported");
        return false;
    }

    private Bundle check(String name, String path, String source, CheckUninstallApkRequest request) {
        Utility.initSDK(GlobalContext.getContext());
        if (initCheckManager()) {
            long begin = SystemClock.elapsedRealtime();
            checkAsync(name, path, source, request);
            request.waitRequestResult(TIME_OUT);
            HwLog.i(TAG, "check time=" + (SystemClock.elapsedRealtime() - begin) + ", type=" + request.getResult());
            if (request.isExpired()) {
                return generateResult(3, -1);
            }
            return generateResult(0, request.getResult());
        }
        HwLog.e(TAG, "check: Invalid TMS check url manager");
        return generateResult(0, -1);
    }

    private void checkAsync(String name, String path, String source, CheckUninstallApkRequest request) {
        final String str = name;
        final String str2 = path;
        final String str3 = source;
        final CheckUninstallApkRequest checkUninstallApkRequest = request;
        HsmExecutor.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            public void run() {
                int virusType = -1;
                synchronized (CheckUninstallApkCaller.this) {
                    try {
                        Context context = GlobalContext.getContext();
                        virusType = CheckUninstallApkCaller.this.mEngine.onCheckUninstalledApk(context, str, UserAgreementHelper.getUserAgreementState(context) ? AntiVirusTools.isCloudScanSwitchOn(context) : false, str2, str3);
                    } catch (Exception e) {
                        HwLog.w(CheckUninstallApkCaller.TAG, "checkAsync Exception", e);
                    }
                }
                checkUninstallApkRequest.setResult(virusType);
                checkUninstallApkRequest.releaseLock();
            }
        });
    }
}
