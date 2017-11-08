package com.android.deskclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import com.android.util.Log;
import java.io.File;

public class AlarmDataBaseReceiver extends BroadcastReceiver {

    private static class HwRunnable implements Runnable {
        Context mContext;
        Intent mIntent;

        public HwRunnable(Context context, Intent intent) {
            this.mContext = context;
            this.mIntent = intent;
        }

        public void run() {
            AlarmDataBaseReceiver.handSwitchSuccess(this.mContext, this.mIntent);
            RingCache.getInstance().checkRingCache(this.mContext, true);
        }
    }

    private static void handSwitchSuccess(android.content.Context r14, android.content.Intent r15) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x00ae in list []
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
        r6 = r15.getAction();
        r10 = getInnerSdcardPath(r14);
        r9 = getOuterSdcardPath(r14);
        r0 = "AlarmDataBaseReceiver";
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = "onReceive : internalCard = ";
        r1 = r1.append(r2);
        r1 = r1.append(r10);
        r2 = " externalCard = ";
        r1 = r1.append(r2);
        r1 = r1.append(r9);
        r1 = r1.toString();
        com.android.util.Log.dRelease(r0, r1);
        r0 = "switch_success";
        r0 = r0.equals(r6);
        if (r0 == 0) goto L_0x00ae;
    L_0x003a:
        r0 = "AlarmDataBaseReceiver";
        r1 = "AsyncHandler : switch_success";
        com.android.util.Log.d(r0, r1);
        r11 = 0;
        r0 = r14.getContentResolver();	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        r1 = com.android.deskclock.alarmclock.Alarm.Columns.CONTENT_URI;	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        r2 = 0;	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        r3 = 0;	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        r4 = 0;	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        r5 = 0;	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        r11 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        if (r11 == 0) goto L_0x00c1;	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
    L_0x0054:
        r0 = r11.moveToNext();	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        if (r0 == 0) goto L_0x00c1;	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
    L_0x005a:
        r7 = new com.android.deskclock.alarmclock.Alarm;	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        r7.<init>(r11);	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        r0 = r7.alert;	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        if (r0 != 0) goto L_0x00af;	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
    L_0x0063:
        r12 = 0;	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
    L_0x0064:
        if (r12 == 0) goto L_0x0054;	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
    L_0x0066:
        r0 = r12.contains(r10);	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        if (r0 == 0) goto L_0x00b6;	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
    L_0x006c:
        r12 = r12.replaceFirst(r10, r9);	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
    L_0x0070:
        r13 = new android.content.ContentValues;	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        r13.<init>();	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        r0 = "alert";	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        r13.put(r0, r12);	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        r0 = r14.getContentResolver();	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        r1 = com.android.deskclock.alarmclock.Alarm.Columns.CONTENT_URI;	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        r2 = r7.id;	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        r2 = (long) r2;	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        r1 = android.content.ContentUris.withAppendedId(r1, r2);	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        r2 = 0;	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        r3 = 0;	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        r0.update(r1, r13, r2, r3);	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        goto L_0x0054;
    L_0x008e:
        r8 = move-exception;
        r0 = "AlarmDataBaseReceiver";	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        r1 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        r1.<init>();	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        r2 = " there has an error >>> ";	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        r1 = r1.append(r2);	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        r1 = r1.append(r8);	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        r1 = r1.toString();	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        com.android.util.Log.e(r0, r1);	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        if (r11 == 0) goto L_0x00ae;
    L_0x00ab:
        r11.close();
    L_0x00ae:
        return;
    L_0x00af:
        r0 = r7.alert;	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        r12 = r0.toString();	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        goto L_0x0064;	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
    L_0x00b6:
        r0 = r12.contains(r9);	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        if (r0 == 0) goto L_0x0070;	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
    L_0x00bc:
        r12 = r12.replaceFirst(r9, r10);	 Catch:{ Exception -> 0x008e, all -> 0x00c7 }
        goto L_0x0070;
    L_0x00c1:
        if (r11 == 0) goto L_0x00ae;
    L_0x00c3:
        r11.close();
        goto L_0x00ae;
    L_0x00c7:
        r0 = move-exception;
        if (r11 == 0) goto L_0x00cd;
    L_0x00ca:
        r11.close();
    L_0x00cd:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.deskclock.AlarmDataBaseReceiver.handSwitchSuccess(android.content.Context, android.content.Intent):void");
    }

    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            Log.w("AlarmDataBaseReceiver", "AlarmDataBaseReceiver->OnReceive : the intent is null or action is null.");
        } else {
            AsyncHandler.post(new HwRunnable(context, intent));
        }
    }

    public static String getInnerSdcardPath(Context context) {
        StorageVolume[] sVolumes = ((StorageManager) context.getSystemService("storage")).getVolumeList();
        if (sVolumes == null) {
            return null;
        }
        for (StorageVolume volume : sVolumes) {
            if (volume.isEmulated()) {
                return volume.getPath() + File.separator;
            }
        }
        return null;
    }

    public static String getOuterSdcardPath(Context context) {
        StorageVolume[] sVolumes = ((StorageManager) context.getSystemService("storage")).getVolumeList();
        if (sVolumes == null) {
            return null;
        }
        for (StorageVolume volume : sVolumes) {
            if (!volume.isEmulated()) {
                return volume.getPath() + File.separator;
            }
        }
        return null;
    }
}
