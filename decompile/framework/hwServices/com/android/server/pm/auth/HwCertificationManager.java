package com.android.server.pm.auth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageParser.Package;
import android.os.Process;
import com.android.server.HwNetworkPropertyChecker;
import com.android.server.pfw.autostartup.comm.XmlConst.ControlScope;
import com.android.server.pm.auth.processor.HwCertificationProcessor;
import com.android.server.pm.auth.util.HwAuthLogger;
import com.android.server.pm.auth.util.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class HwCertificationManager {
    public static final String TAG = "HwCertificationManager";
    private static Context mContext = null;
    public static final boolean mHasFeature = true;
    private static HwCertificationManager mInstance;
    private ConcurrentHashMap<String, HwCertification> mCertMap = new ConcurrentHashMap();
    private Object mLock = new Object();
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                HwCertificationManager.this.handlePackagesChanged(intent);
            }
        }
    };
    private boolean mSystemReady = false;
    private final Runnable mWriteStateRunnable = new Runnable() {
        public void run() {
            List<HwCertification> data = new ArrayList();
            data.addAll(HwCertificationManager.this.mCertMap.values());
            new HwCertXmlHandler().updateHwCert(data);
        }
    };
    private boolean mloaded = false;

    public synchronized boolean checkHwCertification(android.content.pm.PackageParser.Package r15) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x013c in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r14 = this;
        r12 = 0;
        monitor-enter(r14);
        r8 = r14.mLock;
        monitor-enter(r8);
        r6 = java.lang.System.currentTimeMillis();	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r0 = 0;
        r0 = r14.parseAndVerify(r15);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        if (r0 == 0) goto L_0x0061;	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
    L_0x0010:
        r5 = r14.mCertMap;	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r9 = r0.getPackageName();	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r9 = r9.trim();	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r5.put(r9, r0);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r14.addHwPermission(r15, r0);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r14.scheduleWriteStateLocked();	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r2 = java.lang.System.currentTimeMillis();	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r5 = com.android.server.pm.auth.util.HwAuthLogger.getHWFLOW();	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        if (r5 == 0) goto L_0x005d;	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
    L_0x002d:
        r5 = "HwCertificationManager";	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r9 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r9.<init>();	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r10 = "check";	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r9 = r9.append(r10);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r10 = r15.packageName;	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r9 = r9.append(r10);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r10 = "HwCertification spend time:";	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r9 = r9.append(r10);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r10 = r2 - r6;	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r9 = r9.append(r10);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r10 = " ms";	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r9 = r9.append(r10);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r9 = r9.toString();	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        com.android.server.pm.auth.util.HwAuthLogger.i(r5, r9);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
    L_0x005d:
        monitor-exit(r8);
        r5 = 1;
        monitor-exit(r14);
        return r5;
    L_0x0061:
        r5 = "HwCertificationManager";	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r9 = "check HwCertification error, cert is null!";	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        com.android.server.pm.auth.util.HwAuthLogger.e(r5, r9);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r2 = java.lang.System.currentTimeMillis();	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r5 = com.android.server.pm.auth.util.HwAuthLogger.getHWFLOW();	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        if (r5 == 0) goto L_0x00a4;	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
    L_0x0074:
        r5 = "HwCertificationManager";	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r9 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r9.<init>();	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r10 = "check";	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r9 = r9.append(r10);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r10 = r15.packageName;	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r9 = r9.append(r10);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r10 = "HwCertification spend time:";	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r9 = r9.append(r10);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r10 = r2 - r6;	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r9 = r9.append(r10);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r10 = " ms";	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r9 = r9.append(r10);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r9 = r9.toString();	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        com.android.server.pm.auth.util.HwAuthLogger.i(r5, r9);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
    L_0x00a4:
        monitor-exit(r8);
        monitor-exit(r14);
        return r12;
    L_0x00a7:
        r4 = move-exception;
        if (r0 == 0) goto L_0x00b2;
    L_0x00aa:
        r5 = new java.util.ArrayList;	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r5.<init>();	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r0.setPermissionList(r5);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
    L_0x00b2:
        r5 = "HwCertificationManager";	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r9 = "check HwCertification error!";	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        com.android.server.pm.auth.util.HwAuthLogger.e(r5, r9);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r2 = java.lang.System.currentTimeMillis();	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r5 = com.android.server.pm.auth.util.HwAuthLogger.getHWFLOW();	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        if (r5 == 0) goto L_0x00f5;	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
    L_0x00c5:
        r5 = "HwCertificationManager";	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r9 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r9.<init>();	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r10 = "check";	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r9 = r9.append(r10);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r10 = r15.packageName;	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r9 = r9.append(r10);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r10 = "HwCertification spend time:";	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r9 = r9.append(r10);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r10 = r2 - r6;	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r9 = r9.append(r10);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r10 = " ms";	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r9 = r9.append(r10);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r9 = r9.toString();	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        com.android.server.pm.auth.util.HwAuthLogger.i(r5, r9);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
    L_0x00f5:
        monitor-exit(r8);
        monitor-exit(r14);
        return r12;
    L_0x00f8:
        r1 = move-exception;
        r5 = "HwCertificationManager";	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r9 = "check HwCertification error: RuntimeException!";	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        com.android.server.pm.auth.util.HwAuthLogger.e(r5, r9);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r2 = java.lang.System.currentTimeMillis();
        r5 = com.android.server.pm.auth.util.HwAuthLogger.getHWFLOW();
        if (r5 == 0) goto L_0x013c;
    L_0x010c:
        r5 = "HwCertificationManager";
        r9 = new java.lang.StringBuilder;
        r9.<init>();
        r10 = "check";
        r9 = r9.append(r10);
        r10 = r15.packageName;
        r9 = r9.append(r10);
        r10 = "HwCertification spend time:";
        r9 = r9.append(r10);
        r10 = r2 - r6;
        r9 = r9.append(r10);
        r10 = " ms";
        r9 = r9.append(r10);
        r9 = r9.toString();
        com.android.server.pm.auth.util.HwAuthLogger.i(r5, r9);
    L_0x013c:
        monitor-exit(r8);
        monitor-exit(r14);
        return r12;
    L_0x013f:
        r5 = move-exception;
        r2 = java.lang.System.currentTimeMillis();	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r9 = com.android.server.pm.auth.util.HwAuthLogger.getHWFLOW();	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        if (r9 == 0) goto L_0x017a;	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
    L_0x014a:
        r9 = "HwCertificationManager";	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r10 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r10.<init>();	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r11 = "check";	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r10 = r10.append(r11);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r11 = r15.packageName;	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r10 = r10.append(r11);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r11 = "HwCertification spend time:";	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r10 = r10.append(r11);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r12 = r2 - r6;	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r10 = r10.append(r12);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r11 = " ms";	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r10 = r10.append(r11);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        r10 = r10.toString();	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
        com.android.server.pm.auth.util.HwAuthLogger.i(r9, r10);	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
    L_0x017a:
        throw r5;	 Catch:{ RuntimeException -> 0x00f8, Exception -> 0x00a7, all -> 0x013f }
    L_0x017b:
        r5 = move-exception;
        monitor-exit(r8);
        throw r5;
    L_0x017e:
        r5 = move-exception;
        monitor-exit(r14);
        throw r5;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.auth.HwCertificationManager.checkHwCertification(android.content.pm.PackageParser$Package):boolean");
    }

    private HwCertificationManager() {
        readHwCertXml();
    }

    private synchronized void readHwCertXml() {
        if (!this.mloaded) {
            long start = System.currentTimeMillis();
            HwCertXmlHandler handler = new HwCertXmlHandler();
            this.mCertMap.clear();
            handler.readHwCertXml(this.mCertMap);
            this.mloaded = true;
            long end = System.currentTimeMillis();
            if (HwAuthLogger.getHWFLOW()) {
                HwAuthLogger.i("HwCertificationManager", "readHwCertXml  spend time:" + (end - start) + " ms");
            }
            if (HwAuthLogger.getHWFLOW()) {
                HwAuthLogger.i("HwCertificationManager", "readHwCertXml  mCertMap size:" + this.mCertMap.size());
            }
        }
    }

    private void scheduleWriteStateLocked() {
        new Thread(this.mWriteStateRunnable).start();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized HwCertification parseAndVerify(Package pkg) {
        HwCertification cert = new HwCertification();
        if (HwAuthLogger.getHWFLOW()) {
            HwAuthLogger.i("HwCertificationManager", "basecodePath:" + pkg.baseCodePath);
        }
        HwCertificationProcessor hwCertificationProcessor = new HwCertificationProcessor();
        try {
            hwCertificationProcessor.createZipFile(pkg.baseCodePath);
            if (!hwCertificationProcessor.readCert(pkg.baseCodePath, cert)) {
                HwAuthLogger.e("HwCertificationManager", "read cert failed");
            } else if (!hwCertificationProcessor.parserCert(cert)) {
                HwAuthLogger.e("HwCertificationManager", "parse cert failed");
                cert.resetZipFile();
                hwCertificationProcessor.releaseZipFileResource();
                return null;
            } else if (hwCertificationProcessor.verifyCert(pkg, cert)) {
                cert.resetZipFile();
                hwCertificationProcessor.releaseZipFileResource();
                return cert;
            } else {
                HwAuthLogger.e("HwCertificationManager", "verify cert failed");
                cert.resetZipFile();
                hwCertificationProcessor.releaseZipFileResource();
                return null;
            }
        } finally {
            cert.resetZipFile();
            hwCertificationProcessor.releaseZipFileResource();
        }
    }

    private void addHwPermission(Package pkg, HwCertification cert) {
        if (pkg.requestedPermissions != null) {
            for (String perm : cert.getPermissionList()) {
                if (!pkg.requestedPermissions.contains(perm)) {
                    pkg.requestedPermissions.add(perm);
                }
            }
        }
    }

    public static void initialize(Context ctx) {
        mContext = ctx;
    }

    public static boolean isInitialized() {
        return mContext != null;
    }

    public static synchronized HwCertificationManager getIntance() {
        synchronized (HwCertificationManager.class) {
            if (mContext == null) {
                throw new IllegalArgumentException("Impossible to get the instance. This class must be initialized before");
            }
            int uid = Process.myUid();
            if (uid == 1000 || uid == HwNetworkPropertyChecker.HW_DEFAULT_REEVALUATE_DELAY_MS || uid == 0) {
                if (mInstance == null) {
                    mInstance = new HwCertificationManager();
                }
                HwCertificationManager hwCertificationManager = mInstance;
                return hwCertificationManager;
            }
            HwAuthLogger.e("HwCertificationManager", "getIntance from uid:" + uid + ",not system.return null");
            return null;
        }
    }

    public Context getContext() {
        return mContext;
    }

    public static boolean isSupportHwCertification(Package pkg) {
        if (pkg == null || pkg.requestedPermissions == null) {
            return false;
        }
        return !pkg.requestedPermissions.contains("com.huawei.permission.sec.MDM") ? pkg.requestedPermissions.contains("com.huawei.permission.sec.MDM.v2") : true;
    }

    public boolean isSystemReady() {
        return this.mSystemReady;
    }

    public static boolean hasFeature() {
        return true;
    }

    public void systemReady() {
        this.mSystemReady = true;
        try {
            removeNotExist();
        } catch (Exception e) {
        }
        resigterBroadcastReceiver();
    }

    private void removeNotExist() {
        List<String> pkgNameList = new ArrayList();
        for (String pkgName : this.mCertMap.keySet()) {
            if (!Utils.isPackageInstalled(pkgName, mContext)) {
                pkgNameList.add(pkgName);
            }
        }
        for (int i = 0; i < pkgNameList.size(); i++) {
            if (this.mCertMap.get(pkgNameList.get(i)) != null) {
                this.mCertMap.remove(pkgNameList.get(i));
                if (HwAuthLogger.getHWDEBUG()) {
                    HwAuthLogger.d("HwCertificationManager", "package:" + ((String) pkgNameList.get(i)) + " not installed,removed from the cert list xml");
                }
            }
        }
        scheduleWriteStateLocked();
    }

    private void removeExistedCert(Package pkg) {
        if (HwAuthLogger.getHWDEBUG()) {
            HwAuthLogger.d("HwCertificationManager", "removeExistedCert" + pkg.packageName);
        }
        if (this.mCertMap.get(pkg.packageName) != null) {
            this.mCertMap.remove(pkg.packageName);
            if (HwAuthLogger.getHWDEBUG()) {
                HwAuthLogger.d("HwCertificationManager", "package:" + pkg.packageName + " installed,removed from the cert list xml");
            }
            scheduleWriteStateLocked();
        }
    }

    public void cleanUp(Package pkg) {
        if (HwAuthLogger.getHWFLOW()) {
            HwAuthLogger.i("HwCertificationManager", "clean up the cert list xml");
        }
        removeExistedCert(pkg);
    }

    public void cleanUp() {
        if (HwAuthLogger.getHWFLOW()) {
            HwAuthLogger.i("HwCertificationManager", "removeNotExist,clean up the cert list xml");
        }
        removeNotExist();
    }

    private void resigterBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addDataScheme(ControlScope.PACKAGE_ELEMENT_KEY);
        mContext.registerReceiver(this.mReceiver, filter);
    }

    private void handlePackagesChanged(Intent intent) {
        if (intent.getData() != null && intent.getAction() != null) {
            String action = intent.getAction();
            String packageName = intent.getData().getSchemeSpecificPart();
            if ("android.intent.action.PACKAGE_REMOVED".equals(action) && !intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                onPackageRemoved(packageName);
            }
        }
    }

    private void onPackageRemoved(String packageName) {
        synchronized (this.mLock) {
            if (packageName != null) {
                if (this.mCertMap.containsKey(packageName)) {
                    try {
                        if (Utils.isPackageInstalled(packageName, mContext)) {
                            HwAuthLogger.w("HwCertificationManager", "[package]:" + packageName + " is exist in the package");
                            return;
                        }
                        this.mCertMap.remove(packageName);
                        scheduleWriteStateLocked();
                        if (HwAuthLogger.getHWFLOW()) {
                            HwAuthLogger.i("HwCertificationManager", "[package]:" + packageName + ",remove from the cert list xml");
                        }
                    } catch (Exception ex) {
                        HwAuthLogger.e("HwCertificationManager", "onPackageRemoved error!", ex);
                    }
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean getHwCertificationPermission(boolean allowed, Package pkg, String perm) {
        if (allowed || pkg == null || !this.mCertMap.containsKey(pkg.packageName)) {
            return allowed;
        }
        List<String> permissions = ((HwCertification) this.mCertMap.get(pkg.packageName)).getPermissionList();
        if (permissions == null || !permissions.contains(perm) || pkg.requestedPermissions == null || !pkg.requestedPermissions.contains(perm)) {
            return allowed;
        }
        if (HwAuthLogger.getHWDEBUG()) {
            HwAuthLogger.i("HwCertificationManager", "[package]:" + pkg.packageName + ",perm:" + perm);
        }
        return true;
    }

    public int getHwCertificateType(String packageName) {
        HwCertification cert = (HwCertification) this.mCertMap.get(packageName);
        if (cert == null) {
            if (HwAuthLogger.getHWDEBUG()) {
                HwAuthLogger.i("HwCertificationManager", "getHwCertificateType: cert is null, and pkg name is " + packageName);
            }
            return 5;
        }
        String certificate = cert.getCertificate();
        if (certificate == null) {
            return 6;
        }
        if (certificate.equals(HwCertification.SIGNATURE_PLATFORM)) {
            return 1;
        }
        if (certificate.equals(HwCertification.SIGNATURE_TESTKEY)) {
            return 2;
        }
        if (certificate.equals(HwCertification.SIGNATURE_SHARED)) {
            return 3;
        }
        if (certificate.equals(HwCertification.SIGNATURE_MEDIA)) {
            return 4;
        }
        if (certificate.equals("null")) {
            return 0;
        }
        return -1;
    }

    public boolean isContainHwCertification(String packageName) {
        return this.mCertMap.get(packageName) != null;
    }

    public int getHwCertificateTypeNotMDM() {
        return 5;
    }
}
