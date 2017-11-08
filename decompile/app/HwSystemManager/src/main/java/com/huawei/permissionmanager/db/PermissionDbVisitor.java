package com.huawei.permissionmanager.db;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;

public class PermissionDbVisitor {
    public static final int HISTORY_START_DAY = 29;
    public static final Uri HISTORY_URI = DBHelper.HISTORY_URI;
    public static final String TAG = "PermissionDbVisitor";

    public static java.util.ArrayList<com.huawei.permissionmanager.db.HistoryRecord> getHistoryRecord(android.content.Context r24, int r25) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x00b8 in list []
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
        r12 = com.huawei.systemmanager.comm.misc.TimeUtil.getTodayStartCalendar();
        r3 = 5;
        r4 = -29;
        r12.add(r3, r4);
        r22 = r12.getTimeInMillis();
        r3 = 5;
        r4 = 30;
        r12.add(r3, r4);
        r20 = r12.getTimeInMillis();
        r18 = com.google.common.collect.Lists.newArrayList();
        r14 = 0;
        r2 = r24.getContentResolver();	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r3 = HISTORY_URI;	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r4 = 4;	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r4 = new java.lang.String[r4];	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r5 = "packageName";	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r6 = 0;	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r4[r6] = r5;	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r5 = "permission_type";	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r6 = 1;	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r4[r6] = r5;	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r5 = "count";	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r6 = 2;	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r4[r6] = r5;	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r5 = "time_stamp";	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r6 = 3;	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r4[r6] = r5;	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r5 = "action=? AND time_stamp>=?  AND time_stamp<=?";	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r6 = 3;	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r6 = new java.lang.String[r6];	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r7 = java.lang.String.valueOf(r25);	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r8 = 0;	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r6[r8] = r7;	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r7 = java.lang.String.valueOf(r22);	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r8 = 1;	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r6[r8] = r7;	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r7 = java.lang.String.valueOf(r20);	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r8 = 2;	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r6[r8] = r7;	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r7 = "time_stamp DESC";	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r14 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        if (r14 != 0) goto L_0x0068;
    L_0x0062:
        if (r14 == 0) goto L_0x0067;
    L_0x0064:
        r14.close();
    L_0x0067:
        return r18;
    L_0x0068:
        r3 = "packageName";	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r17 = r14.getColumnIndex(r3);	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r3 = "permission_type";	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r16 = r14.getColumnIndex(r3);	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r3 = "count";	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r13 = r14.getColumnIndex(r3);	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r3 = "time_stamp";	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r19 = r14.getColumnIndex(r3);	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
    L_0x0084:
        r3 = r14.moveToNext();	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        if (r3 == 0) goto L_0x00b9;	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
    L_0x008a:
        r2 = new com.huawei.permissionmanager.db.HistoryRecord;	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r0 = r17;	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r3 = r14.getString(r0);	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r0 = r16;	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r4 = r14.getInt(r0);	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r6 = r14.getLong(r13);	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r8 = 0;	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r0 = r19;	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r10 = r14.getLong(r0);	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r5 = r25;	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r2.<init>(r3, r4, r5, r6, r8, r10);	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r0 = r18;	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        r0.add(r2);	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        goto L_0x0084;
    L_0x00af:
        r15 = move-exception;
        r15.printStackTrace();	 Catch:{ Exception -> 0x00af, all -> 0x00bf }
        if (r14 == 0) goto L_0x00b8;
    L_0x00b5:
        r14.close();
    L_0x00b8:
        return r18;
    L_0x00b9:
        if (r14 == 0) goto L_0x00b8;
    L_0x00bb:
        r14.close();
        goto L_0x00b8;
    L_0x00bf:
        r3 = move-exception;
        if (r14 == 0) goto L_0x00c5;
    L_0x00c2:
        r14.close();
    L_0x00c5:
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.permissionmanager.db.PermissionDbVisitor.getHistoryRecord(android.content.Context, int):java.util.ArrayList<com.huawei.permissionmanager.db.HistoryRecord>");
    }

    public static void insertHistoryRecord(Context ctx, HistoryRecord record) {
        if (ctx == null || record == null) {
            HwLog.e(TAG, "insertHistoryRecord failed, params is null!!");
            return;
        }
        record.resetDateStartTime();
        HwLog.i(TAG, "insertHistoryRecord:" + record.toString());
        ContentValues values = record.getContentValues();
        Bundle bundle = new Bundle();
        bundle.putParcelable(DBHelper.KEY_CONTENT_VALUE, values);
        try {
            ctx.getContentResolver().call(HISTORY_URI, DBHelper.METHOD_RECORD_HISTORY, null, bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<HistoryRecord> getForbiddenHistoryRecord(Context ctx) {
        return getHistoryRecord(ctx, 10);
    }

    public static ArrayList<HistoryRecord> getAllowedHistoryRecord(Context ctx) {
        return getHistoryRecord(ctx, 11);
    }

    public static void removeHistoryRecord(Context ctx, String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            HwLog.e(TAG, "removeHistoryRecord pkgName is empty");
            return;
        }
        HwLog.i(TAG, "removeHistoryRecord,pkgName:" + pkgName + ", delete count:" + ctx.getContentResolver().delete(HISTORY_URI, "packageName=?", new String[]{pkgName}));
    }
}
