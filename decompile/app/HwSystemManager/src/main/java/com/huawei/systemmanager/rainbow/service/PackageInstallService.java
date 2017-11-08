package com.huawei.systemmanager.rainbow.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.rainbow.client.background.handle.serv.RecommendSingleApk;
import com.huawei.systemmanager.rainbow.client.background.service.RainbowCommonService;
import com.huawei.systemmanager.rainbow.client.base.ClientConstant.CloudActions;
import com.huawei.systemmanager.rainbow.db.CloudDBAdapter;
import com.huawei.systemmanager.rainbow.service.IPackageInstallService.Stub;
import com.huawei.systemmanager.rainbow.util.PackageInfoConst;
import com.huawei.systemmanager.rainbow.vaguerule.VagueManager;
import com.huawei.systemmanager.service.MainService.HsmService;
import com.huawei.systemmanager.startupmgr.confdata.StartupConfData;
import com.huawei.systemmanager.util.HwLog;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class PackageInstallService extends Stub implements HsmService {
    private static final int MAX_FETCH_RECOMMEND_WAIT_NUM = 3;
    private static final long MAX_WAIT_FETCH_RECOMMEND_TIME = 2000;
    public static final String PACKAGE_INSTALL_SERVICE_NAME = "";
    private static final String TAG = "PackageInstallService";
    private Context mContext = null;
    private final ConcurrentHashMap<Long, ResultCountDownLatch> mLockers = new ConcurrentHashMap();
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (Utility.checkBroadcast(context, intent)) {
                if (CloudActions.ACTION_REPLY_RECOMMEND_SINGLE_APK.equals(intent.getAction())) {
                    long requestId = intent.getLongExtra(RecommendSingleApk.REQUEST_ID_KEY, -1);
                    boolean fetchResult = intent.getBooleanExtra("result", false);
                    ResultCountDownLatch locker = (ResultCountDownLatch) PackageInstallService.this.mLockers.get(Long.valueOf(requestId));
                    if (locker != null) {
                        locker.mResult = fetchResult;
                        locker.countDown();
                    }
                }
            }
        }
    };

    private static class ResultCountDownLatch extends CountDownLatch {
        public volatile boolean mResult;

        public ResultCountDownLatch(int count) {
            super(count);
        }
    }

    public com.huawei.systemmanager.rainbow.service.PermissionRecommendInfo requestPermissionRecommendInfo(java.lang.String r15) throws android.os.RemoteException {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:45:? in {5, 11, 17, 24, 30, 34, 39, 41, 42, 44, 46, 47} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r14 = this;
        r11 = "PackageInstallService";
        r12 = new java.lang.StringBuilder;
        r12.<init>();
        r13 = "requestPermissionRecommendInfo for ";
        r12 = r12.append(r13);
        r12 = r12.append(r15);
        r12 = r12.toString();
        com.huawei.systemmanager.util.HwLog.i(r11, r12);
        r11 = r14.mContext;
        r12 = "com.huawei.systemmanager.permission.ACCESS_INTERFACE";
        r13 = 0;
        r11.enforceCallingOrSelfPermission(r12, r13);
        r4 = android.os.Binder.clearCallingIdentity();
        r8 = -1;
        r11 = r14.mContext;	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r10 = com.huawei.systemmanager.rainbow.service.PIDataCvt.cvtToRecommendInfo(r11, r15);	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r11 = r10.mRecommendInfoList;	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r11 = r11.isEmpty();	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        if (r11 != 0) goto L_0x0044;
    L_0x0037:
        r11 = r14.mLockers;
        r12 = java.lang.Long.valueOf(r8);
        r11.remove(r12);
        android.os.Binder.restoreCallingIdentity(r4);
        return r10;
    L_0x0044:
        r11 = r14.mLockers;	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r3 = r11.size();	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r11 = 3;	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        if (r3 < r11) goto L_0x0074;	 Catch:{ Exception -> 0x0106, all -> 0x012b }
    L_0x004d:
        r11 = "PackageInstallService";	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r12 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r12.<init>();	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r13 = "current locker too much, size:";	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r12 = r12.append(r13);	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r12 = r12.append(r3);	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r12 = r12.toString();	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        com.huawei.systemmanager.util.HwLog.w(r11, r12);	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r11 = r14.mLockers;
        r12 = java.lang.Long.valueOf(r8);
        r11.remove(r12);
        android.os.Binder.restoreCallingIdentity(r4);
        return r10;
    L_0x0074:
        r0 = java.lang.System.currentTimeMillis();	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r6 = new com.huawei.systemmanager.rainbow.service.PackageInstallService$ResultCountDownLatch;	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r11 = 1;	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r6.<init>(r11);	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r11 = r14.mLockers;	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r12 = java.lang.Long.valueOf(r0);	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r7 = r11.putIfAbsent(r12, r6);	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r7 = (com.huawei.systemmanager.rainbow.service.PackageInstallService.ResultCountDownLatch) r7;	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        if (r7 == 0) goto L_0x00a2;	 Catch:{ Exception -> 0x0106, all -> 0x012b }
    L_0x008c:
        r11 = "PackageInstallService";	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r12 = "current locker request id exist!";	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        com.huawei.systemmanager.util.HwLog.w(r11, r12);	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r11 = r14.mLockers;
        r12 = java.lang.Long.valueOf(r8);
        r11.remove(r12);
        android.os.Binder.restoreCallingIdentity(r4);
        return r10;
    L_0x00a2:
        r8 = r0;
        r11 = "PackageInstallService";	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r12 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r12.<init>();	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r13 = "no recommend data, try fetch from server, pkg:";	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r12 = r12.append(r13);	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r12 = r12.append(r15);	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r12 = r12.toString();	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        com.huawei.systemmanager.util.HwLog.i(r11, r12);	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r14.startGetRecommdSingleApp(r0, r15);	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r11 = java.util.concurrent.TimeUnit.MILLISECONDS;	 Catch:{ Exception -> 0x00fb }
        r12 = 2000; // 0x7d0 float:2.803E-42 double:9.88E-321;	 Catch:{ Exception -> 0x00fb }
        r11 = r6.await(r12, r11);	 Catch:{ Exception -> 0x00fb }
        if (r11 != 0) goto L_0x00e4;	 Catch:{ Exception -> 0x00fb }
    L_0x00ca:
        r11 = "PackageInstallService";	 Catch:{ Exception -> 0x00fb }
        r12 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x00fb }
        r12.<init>();	 Catch:{ Exception -> 0x00fb }
        r13 = "requestPermissionRecommendInfo request timeout, pkgName:";	 Catch:{ Exception -> 0x00fb }
        r12 = r12.append(r13);	 Catch:{ Exception -> 0x00fb }
        r12 = r12.append(r15);	 Catch:{ Exception -> 0x00fb }
        r12 = r12.toString();	 Catch:{ Exception -> 0x00fb }
        com.huawei.systemmanager.util.HwLog.w(r11, r12);	 Catch:{ Exception -> 0x00fb }
    L_0x00e4:
        r11 = r6.mResult;	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        if (r11 == 0) goto L_0x011e;	 Catch:{ Exception -> 0x0106, all -> 0x012b }
    L_0x00e8:
        r11 = r14.mContext;	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r11 = com.huawei.systemmanager.rainbow.service.PIDataCvt.cvtToRecommendInfo(r11, r15);	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r12 = r14.mLockers;
        r13 = java.lang.Long.valueOf(r0);
        r12.remove(r13);
        android.os.Binder.restoreCallingIdentity(r4);
        return r11;
    L_0x00fb:
        r2 = move-exception;
        r11 = "PackageInstallService";	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r12 = "requestPermissionRecommendInfo locker catch exception,";	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        com.huawei.systemmanager.util.HwLog.i(r11, r12, r2);	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        goto L_0x00e4;
    L_0x0106:
        r2 = move-exception;
        r11 = "PackageInstallService";	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r12 = "requestPermissionRecommendInfo : Exception";	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        com.huawei.systemmanager.util.HwLog.w(r11, r12, r2);	 Catch:{ Exception -> 0x0106, all -> 0x012b }
        r11 = 0;
        r12 = r14.mLockers;
        r13 = java.lang.Long.valueOf(r8);
        r12.remove(r13);
        android.os.Binder.restoreCallingIdentity(r4);
        return r11;
    L_0x011e:
        r11 = r14.mLockers;
        r12 = java.lang.Long.valueOf(r0);
        r11.remove(r12);
        android.os.Binder.restoreCallingIdentity(r4);
        return r10;
    L_0x012b:
        r11 = move-exception;
        r12 = r14.mLockers;
        r13 = java.lang.Long.valueOf(r8);
        r12.remove(r13);
        android.os.Binder.restoreCallingIdentity(r4);
        throw r11;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.systemmanager.rainbow.service.PackageInstallService.requestPermissionRecommendInfo(java.lang.String):com.huawei.systemmanager.rainbow.service.PermissionRecommendInfo");
    }

    public PackageInstallService(Context context) {
        this.mContext = context;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public PackagePermissionInfo requestPackagePermissionInfo(String pkgName) throws RemoteException {
        int i = 1;
        HwLog.i(TAG, "requestPackagePermissionInfo for " + pkgName);
        this.mContext.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
        long identity = Binder.clearCallingIdentity();
        try {
            PackagePermissionInfo info = CloudDBAdapter.getInstance(this.mContext).getPermissionsByPkgName(pkgName);
            if (!(info == null || info.permission == null)) {
                info.permission.put(PackageInfoConst.BOOTSTARTUP_KEY, Integer.valueOf(1));
                Map map = info.permission;
                String str = PackageInfoConst.APP_STARTUP_KEY;
                if (StartupConfData.getDftReceiverCfg(this.mContext, pkgName)) {
                    i = 0;
                }
                map.put(str, Integer.valueOf(i));
            }
            if (info == null) {
                HwLog.i(TAG, "Send package permissions info:null.");
            } else if (Log.HWINFO) {
                HwLog.i(TAG, "Send package permissions info:" + info.toString());
            }
            Binder.restoreCallingIdentity(identity);
            return info;
        } catch (Exception e) {
            HwLog.e(TAG, "requestPackagePermissionInfo: Exception", e);
            return null;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public Bundle requesPermissionWithSdkVersion(Bundle args) throws RemoteException {
        Bundle result = new Bundle();
        if (args == null) {
            HwLog.i(TAG, "requestPermissionInfo args is null");
            return result;
        }
        String pkgName = args.getString("pkgName", "");
        int sdkVersion = args.getInt("sdkVersion", -1);
        HwLog.i(TAG, "requestPermissionInfo for " + pkgName + ", sdk version:" + sdkVersion);
        if (TextUtils.isEmpty(pkgName)) {
            return result;
        }
        long identity = Binder.clearCallingIdentity();
        if (sdkVersion != -1) {
            try {
                VagueManager.getInstance(this.mContext).setPkgSdkVersion(pkgName, sdkVersion);
            } catch (Exception e) {
                HwLog.e(TAG, "requesPermissionWithSdkVersion: Exception", e);
                return null;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
        PackagePermissionInfo info = requestPackagePermissionInfo(pkgName);
        HwLog.i(TAG, "requestPermissionInfo for " + pkgName + ", result:" + info);
        result.putParcelable("permissionInfo", info);
        result.setClassLoader(PackagePermissionInfo.class.getClassLoader());
        Binder.restoreCallingIdentity(identity);
        return result;
    }

    public Bundle requestInfo(String method, Bundle args) throws RemoteException {
        if ("requesPermissionWithSdkVersion".equals(method)) {
            return requesPermissionWithSdkVersion(args);
        }
        return new Bundle();
    }

    public void startGetRecommdSingleApp(long requestId, String pkg) {
        Intent recIntent = new Intent(CloudActions.INTENT_CLOUD_RECOMMEND_SINGLE_APK);
        recIntent.setClass(this.mContext, RainbowCommonService.class);
        recIntent.putExtra("packageName", pkg);
        recIntent.putExtra(RecommendSingleApk.REQUEST_ID_KEY, requestId);
        this.mContext.startService(recIntent);
    }

    public void init() {
        this.mContext.registerReceiver(this.mReceiver, new IntentFilter(CloudActions.ACTION_REPLY_RECOMMEND_SINGLE_APK), "com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
    }

    public void onDestroy() {
        this.mContext.unregisterReceiver(this.mReceiver);
    }

    public void onConfigurationChange(Configuration newConfig) {
    }

    public void onStartCommand(Intent intent, int flags, int startId) {
    }
}
