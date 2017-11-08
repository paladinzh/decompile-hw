package com.huawei.systemmanager.secpatch.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import com.google.common.base.Objects;
import com.huawei.systemmanager.comm.concurrent.HsmExecutor;
import com.huawei.systemmanager.secpatch.common.SecPatchCheckResult;
import com.huawei.systemmanager.secpatch.db.DBAdapter;
import com.huawei.systemmanager.secpatch.net.SecPatchRequester;
import com.huawei.systemmanager.secpatch.util.SecPatchHelper;
import com.huawei.systemmanager.util.HwLog;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SecPatchChecker {
    private static final long CONNECT_INTERNET_INTERVAL_TIME = 86400000;
    private static final String KEY_CHECK_VERSION = "check_sec_path_version";
    private static final String KEY_CONNET_WORK = "check_sec_patch_from_work";
    private static final String TAG = "SecPatchChecker";
    private static final long WAIT_NETWORK_MAX_TIME = 5000;
    private static final SecPatchChecker sInstance = new SecPatchChecker();
    private FutureTask<Void> mTask;

    public static int getPathItemNum(Context ctx) {
        return sInstance.refreshAndGetSecPathItem(ctx, WAIT_NETWORK_MAX_TIME);
    }

    private SecPatchChecker() {
    }

    private int refreshAndGetSecPathItem(final Context ctx, long maxWaitTime) {
        if (checkShouldConnectNetwork(ctx)) {
            if (this.mTask == null || this.mTask.isDone()) {
                this.mTask = new FutureTask(new Callable<Void>() {
                    public Void call() throws Exception {
                        SecPatchChecker.this.refreshFromNetwork(ctx);
                        return null;
                    }
                });
                HsmExecutor.THREAD_POOL_EXECUTOR.execute(this.mTask);
                HwLog.i(TAG, "refreshAndGetSecPathItem, start a new refresh task");
            } else {
                HwLog.i(TAG, "refreshAndGetSecPathItem, last refresh thread is still alive, do nothing");
            }
            try {
                this.mTask.get(WAIT_NETWORK_MAX_TIME, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e2) {
                e2.printStackTrace();
            } catch (TimeoutException e3) {
                e3.printStackTrace();
            } catch (Exception e4) {
                e4.printStackTrace();
            }
        }
        return getPathNumFromDb(ctx);
    }

    private int getPathNumFromDb(Context ctx) {
        return DBAdapter.getNeedUpdateVersionList(ctx).size();
    }

    private boolean checkShouldConnectNetwork(Context ctx) {
        SharedPreferences perfer = ctx.getSharedPreferences("systemmanagerscan", 0);
        long lastConnectNetworkTime = perfer.getLong(KEY_CONNET_WORK, 0);
        long current = System.currentTimeMillis();
        if (current < lastConnectNetworkTime) {
            HwLog.i(TAG, "checkShouldConnectNetwork, in branch current < lastConnectNetworkTime,lastConnectNetworkTime:" + lastConnectNetworkTime + ", current:" + current);
            return true;
        } else if (current - lastConnectNetworkTime >= 86400000) {
            HwLog.i(TAG, "checkShouldConnectNetwork, in branch CONNECT_INTERNET_INTERVAL_TIME,lastConnectNetworkTime:" + lastConnectNetworkTime + ", current:" + current);
            return true;
        } else {
            String lastCheckVersion = perfer.getString(KEY_CHECK_VERSION, "");
            String currentVersion = SecPatchHelper.getSystemVersionName();
            if (Objects.equal(lastCheckVersion, currentVersion)) {
                HwLog.i(TAG, "need not conntetnetwork");
                return false;
            }
            HwLog.i(TAG, "checkShouldConnectNetwork, in branch version changed,lastCheckVersion:" + lastCheckVersion + ", currentVersion:" + currentVersion);
            return true;
        }
    }

    private void saveConnetNetworkTime(Context ctx) {
        SharedPreferences perfer = ctx.getSharedPreferences("systemmanagerscan", 0);
        perfer.edit().putLong(KEY_CONNET_WORK, System.currentTimeMillis()).putString(KEY_CHECK_VERSION, SecPatchHelper.getSystemVersionName()).commit();
    }

    private boolean refreshFromNetwork(Context ctx) {
        HwLog.i(TAG, "begin to refreshFromNetwork");
        long startTime = SystemClock.elapsedRealtime();
        if (SecPatchHelper.isNetworkAvaialble(ctx)) {
            SecPatchCheckResult checkResult = SecPatchRequester.queryCheckVersion(ctx);
            saveConnetNetworkTime(ctx);
            if (!SecPatchHelper.isRomVersionChange(ctx)) {
                HwLog.d(TAG, "getSecPathItem, Rom version not change!");
                if (!checkResult.getResponseCodeValidStatus()) {
                    HwLog.e(TAG, "getSecPathItem, checkResult is invalid");
                    return false;
                }
            }
            checkResult.printVersionInfoToLog(TAG);
            boolean getAllStatus = SecPatchRequester.queryAllPatch(ctx, checkResult.getCheckAllVersion());
            boolean getUpdateStatus = SecPatchRequester.queryUpdatePatch(ctx, checkResult.getCheckAvaVersion());
            HwLog.i(TAG, "refreshSecPathItem, getAllStatus:" + getAllStatus + ", getUpdateStatus:" + getUpdateStatus + ",costTime:" + (SystemClock.elapsedRealtime() - startTime));
            if (getAllStatus) {
                getUpdateStatus = true;
            }
            return getUpdateStatus;
        }
        HwLog.i(TAG, "getSecPathItem, network avaialbe false!");
        return false;
    }
}
