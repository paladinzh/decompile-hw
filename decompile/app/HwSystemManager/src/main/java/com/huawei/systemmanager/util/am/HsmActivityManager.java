package com.huawei.systemmanager.util.am;

import com.huawei.android.smcs.STProcessRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HsmActivityManager {
    private static final int AMS_GET_RUNNING_PROCESS = 1;
    public static final int AMS_PROCESS_PERCEPTIBLE_ADJ = 2;
    private static final String TAG = "HsmActivityManager";
    private static HsmActivityManager mInstance = null;

    public static class ProcessWrapper {
        private final STProcessRecord mRecord;

        private ProcessWrapper(STProcessRecord record) {
            this.mRecord = record;
        }

        public boolean pkgContain(String pkgName) {
            return this.mRecord.pkgList.contains(pkgName);
        }

        public Set<String> getPkgs() {
            return this.mRecord.pkgList;
        }
    }

    public java.util.ArrayList<com.huawei.android.smcs.STProcessRecord> getRunningList() {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x008d in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r10 = this;
        r9 = 0;
        r1 = 0;
        r0 = 0;
        r5 = 0;
        r4 = 0;
        r6 = "activity";	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        r0 = android.os.ServiceManager.getService(r6);	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        if (r0 != 0) goto L_0x0018;	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
    L_0x000e:
        r6 = "HsmActivityManager";	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        r7 = "HsmActivityManager.getRunningList: invalid AMS binder";	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        com.huawei.systemmanager.util.HwLog.e(r6, r7);	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        return r9;	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
    L_0x0018:
        r1 = android.os.Parcel.obtain();	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        r6 = 1;	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        r1.writeInt(r6);	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        r5 = android.os.Parcel.obtain();	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        r6 = 1599294787; // 0x5f534d43 float:1.52259E19 double:7.90156612E-315;	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        r7 = 0;	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        r0.transact(r6, r1, r5, r7);	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        r6 = com.huawei.android.smcs.STProcessRecord.CREATOR;	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        r4 = r5.createTypedArrayList(r6);	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        if (r1 == 0) goto L_0x0036;
    L_0x0033:
        r1.recycle();
    L_0x0036:
        if (r5 == 0) goto L_0x003b;
    L_0x0038:
        r5.recycle();
    L_0x003b:
        return r4;
    L_0x003c:
        r3 = move-exception;
        r6 = "HsmActivityManager";	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        r7 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        r7.<init>();	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        r8 = "HsmActivityManager.getRunningList: catch exception: ";	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        r7 = r7.append(r8);	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        r8 = r3.toString();	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        r7 = r7.append(r8);	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        r7 = r7.toString();	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        com.huawei.systemmanager.util.HwLog.e(r6, r7);	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        r3.printStackTrace();	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        if (r1 == 0) goto L_0x0063;
    L_0x0060:
        r1.recycle();
    L_0x0063:
        if (r5 == 0) goto L_0x0068;
    L_0x0065:
        r5.recycle();
    L_0x0068:
        return r9;
    L_0x0069:
        r2 = move-exception;
        r6 = "HsmActivityManager";	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        r7 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        r7.<init>();	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        r8 = "HsmActivityManager.getRunningList: transact caught remote exception: ";	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        r7 = r7.append(r8);	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        r8 = r2.toString();	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        r7 = r7.append(r8);	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        r7 = r7.toString();	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        com.huawei.systemmanager.util.HwLog.e(r6, r7);	 Catch:{ RemoteException -> 0x0069, Exception -> 0x003c, all -> 0x0093 }
        if (r1 == 0) goto L_0x008d;
    L_0x008a:
        r1.recycle();
    L_0x008d:
        if (r5 == 0) goto L_0x0092;
    L_0x008f:
        r5.recycle();
    L_0x0092:
        return r9;
    L_0x0093:
        r6 = move-exception;
        if (r1 == 0) goto L_0x0099;
    L_0x0096:
        r1.recycle();
    L_0x0099:
        if (r5 == 0) goto L_0x009e;
    L_0x009b:
        r5.recycle();
    L_0x009e:
        throw r6;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.systemmanager.util.am.HsmActivityManager.getRunningList():java.util.ArrayList<com.huawei.android.smcs.STProcessRecord>");
    }

    private HsmActivityManager() {
    }

    public static synchronized HsmActivityManager getInstance() {
        HsmActivityManager hsmActivityManager;
        synchronized (HsmActivityManager.class) {
            if (mInstance == null) {
                mInstance = new HsmActivityManager();
            }
            hsmActivityManager = mInstance;
        }
        return hsmActivityManager;
    }

    public static List<ProcessWrapper> getRunningProcess() {
        ArrayList<STProcessRecord> records = getInstance().getRunningList();
        if (records == null) {
            return new ArrayList();
        }
        List<ProcessWrapper> result = new ArrayList(records.size());
        for (STProcessRecord record : records) {
            result.add(new ProcessWrapper(record));
        }
        return result;
    }
}
