package com.huawei.systemmanager.power.provider;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.RemoteException;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.power.comm.ApplicationConstant;
import com.huawei.systemmanager.power.model.UnifiedPowerAppControl;
import com.huawei.systemmanager.power.model.UnifiedPowerBean;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SmartProviderHelper {
    private static final String TAG = "SmartProviderHelper";
    public static final Uri UNIFIED_POWER_APP_DEFAULT_VALUE_TABLE_URI = Uri.parse("content://com.huawei.android.smartpowerprovider/unifiedpowerappsdefaultvalue");
    public static final Uri UNIFIED_POWER_APP_TABLE_URI = Uri.parse("content://com.huawei.android.smartpowerprovider/unifiedpowerapps");

    public static java.util.Map<java.lang.String, java.util.ArrayList<java.lang.String>> getProtectAppFromDbForPowerGenie(android.content.Context r16, java.lang.String r17, android.os.Bundle r18) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x00a6 in list []
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
        r12 = com.huawei.systemmanager.comm.collections.HsmCollections.newArrayMap();
        r11 = com.google.common.collect.Lists.newArrayList();
        r14 = com.google.common.collect.Lists.newArrayList();
        r7 = 0;
        r13 = 0;
        r4 = "is_protected=?";
        r1 = 1;
        r5 = new java.lang.String[r1];
        r1 = "all";
        r0 = r17;
        r1 = r0.equals(r1);
        if (r1 == 0) goto L_0x0049;
    L_0x001f:
        r4 = 0;
        r5 = 0;
    L_0x0021:
        r1 = r16.getContentResolver();	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
        r2 = UNIFIED_POWER_APP_TABLE_URI;	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
        r3 = 3;	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
        r3 = new java.lang.String[r3];	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
        r6 = "pkg_name";	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
        r15 = 0;	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
        r3[r15] = r6;	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
        r6 = "is_protected";	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
        r15 = 1;	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
        r3[r15] = r6;	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
        r6 = "is_show";	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
        r15 = 2;	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
        r3[r15] = r6;	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
        r6 = 0;	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
        r7 = r1.query(r2, r3, r4, r5, r6);	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
        if (r7 != 0) goto L_0x0077;
    L_0x0043:
        if (r7 == 0) goto L_0x0048;
    L_0x0045:
        r7.close();
    L_0x0048:
        return r12;
    L_0x0049:
        r1 = "protect";
        r0 = r17;
        r1 = r0.equals(r1);
        if (r1 == 0) goto L_0x005b;
    L_0x0054:
        r1 = "1";
        r2 = 0;
        r5[r2] = r1;
        goto L_0x0021;
    L_0x005b:
        r1 = "unprotect";
        r0 = r17;
        r1 = r0.equals(r1);
        if (r1 == 0) goto L_0x006d;
    L_0x0066:
        r1 = "0";
        r2 = 0;
        r5[r2] = r1;
        goto L_0x0021;
    L_0x006d:
        r1 = "SmartProviderHelper";
        r2 = "getProtectAppFromDbForPowerGenie Illegal parameter!";
        com.huawei.systemmanager.util.HwLog.i(r1, r2);
        return r12;
    L_0x0077:
        r1 = r7.moveToNext();	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
        if (r1 == 0) goto L_0x00b4;	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
    L_0x007d:
        r1 = 0;	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
        r10 = r7.getString(r1);	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
        r1 = 1;	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
        r1 = r7.getInt(r1);	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
        r2 = 1;	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
        if (r1 != r2) goto L_0x00a7;	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
    L_0x008a:
        r9 = 1;	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
    L_0x008b:
        if (r9 == 0) goto L_0x00a9;	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
    L_0x008d:
        r11.add(r10);	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
    L_0x0090:
        r1 = "frz_protect";	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
        r12.put(r1, r11);	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
        r1 = "frz_unprotect";	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
        r12.put(r1, r14);	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
        goto L_0x0077;
    L_0x009d:
        r8 = move-exception;
        r8.printStackTrace();	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
        if (r7 == 0) goto L_0x00a6;
    L_0x00a3:
        r7.close();
    L_0x00a6:
        return r12;
    L_0x00a7:
        r9 = 0;
        goto L_0x008b;
    L_0x00a9:
        r14.add(r10);	 Catch:{ Exception -> 0x009d, all -> 0x00ad }
        goto L_0x0090;
    L_0x00ad:
        r1 = move-exception;
        if (r7 == 0) goto L_0x00b3;
    L_0x00b0:
        r7.close();
    L_0x00b3:
        throw r1;
    L_0x00b4:
        if (r7 == 0) goto L_0x00a6;
    L_0x00b6:
        r7.close();
        goto L_0x00a6;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.systemmanager.power.provider.SmartProviderHelper.getProtectAppFromDbForPowerGenie(android.content.Context, java.lang.String, android.os.Bundle):java.util.Map<java.lang.String, java.util.ArrayList<java.lang.String>>");
    }

    public static java.util.List<com.huawei.systemmanager.power.model.UnifiedPowerBean> getUserUnChangedUnifiedPowerList(android.content.Context r15) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0073 in list []
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
        r14 = 1;
        r13 = 0;
        r8 = com.google.common.collect.Lists.newArrayList();
        r6 = 0;
        r0 = r15.getContentResolver();	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        r1 = UNIFIED_POWER_APP_TABLE_URI;	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        r2 = 3;	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        r2 = new java.lang.String[r2];	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        r3 = "pkg_name";	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        r4 = 0;	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        r2[r4] = r3;	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        r3 = "is_protected";	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        r4 = 1;	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        r2[r4] = r3;	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        r3 = "is_show";	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        r4 = 2;	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        r2[r4] = r3;	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        r3 = "is_changed=?";	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        r4 = 1;	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        r4 = new java.lang.String[r4];	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        r5 = 0;	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        r5 = java.lang.String.valueOf(r5);	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        r12 = 0;	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        r4[r12] = r5;	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        r5 = 0;	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        if (r6 != 0) goto L_0x003d;
    L_0x0037:
        if (r6 == 0) goto L_0x003c;
    L_0x0039:
        r6.close();
    L_0x003c:
        return r13;
    L_0x003d:
        r0 = r6.moveToNext();	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        if (r0 == 0) goto L_0x0078;	 Catch:{ Exception -> 0x006a, all -> 0x007e }
    L_0x0043:
        r11 = new com.huawei.systemmanager.power.model.UnifiedPowerBean;	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        r11.<init>();	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        r0 = 0;	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        r0 = r6.getString(r0);	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        r11.setPkg_name(r0);	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        r0 = 1;	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        r0 = r6.getInt(r0);	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        if (r0 != r14) goto L_0x0074;	 Catch:{ Exception -> 0x006a, all -> 0x007e }
    L_0x0057:
        r9 = 1;	 Catch:{ Exception -> 0x006a, all -> 0x007e }
    L_0x0058:
        r0 = 2;	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        r0 = r6.getInt(r0);	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        if (r0 != r14) goto L_0x0076;	 Catch:{ Exception -> 0x006a, all -> 0x007e }
    L_0x005f:
        r10 = 1;	 Catch:{ Exception -> 0x006a, all -> 0x007e }
    L_0x0060:
        r11.setIs_protected(r9);	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        r11.setIs_show(r10);	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        r8.add(r11);	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        goto L_0x003d;
    L_0x006a:
        r7 = move-exception;
        r7.printStackTrace();	 Catch:{ Exception -> 0x006a, all -> 0x007e }
        if (r6 == 0) goto L_0x0073;
    L_0x0070:
        r6.close();
    L_0x0073:
        return r8;
    L_0x0074:
        r9 = 0;
        goto L_0x0058;
    L_0x0076:
        r10 = 0;
        goto L_0x0060;
    L_0x0078:
        if (r6 == 0) goto L_0x0073;
    L_0x007a:
        r6.close();
        goto L_0x0073;
    L_0x007e:
        r0 = move-exception;
        if (r6 == 0) goto L_0x0084;
    L_0x0081:
        r6.close();
    L_0x0084:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.systemmanager.power.provider.SmartProviderHelper.getUserUnChangedUnifiedPowerList(android.content.Context):java.util.List<com.huawei.systemmanager.power.model.UnifiedPowerBean>");
    }

    public static Map<String, UnifiedPowerBean> getProtectAppFromDb(Context context, Map<String, UnifiedPowerBean> out) {
        UnifiedPowerBean unifiedPowerBean;
        Exception e;
        Map<String, UnifiedPowerBean> res = HsmCollections.newArrayMap();
        if (out != null) {
            res = out;
        }
        Cursor cursor = null;
        UnifiedPowerBean unifiedPowerBean2 = null;
        try {
            cursor = context.getContentResolver().query(UNIFIED_POWER_APP_TABLE_URI, new String[]{"pkg_name", ApplicationConstant.UNIFIED_POWER_APP_CHECK, ApplicationConstant.UNIFIED_POWER_APP_SHOW}, null, null, null);
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return res;
            }
            while (true) {
                unifiedPowerBean = unifiedPowerBean2;
                try {
                    if (!cursor.moveToNext()) {
                        break;
                    }
                    unifiedPowerBean2 = new UnifiedPowerBean();
                    String pkg = cursor.getString(0);
                    boolean protect = cursor.getInt(1) == 1;
                    boolean show = cursor.getInt(2) == 1;
                    unifiedPowerBean2.setIs_protected(protect);
                    unifiedPowerBean2.setIs_show(show);
                    unifiedPowerBean2.setPkg_name(pkg);
                    res.put(pkg, unifiedPowerBean2);
                } catch (Exception e2) {
                    e = e2;
                    unifiedPowerBean2 = unifiedPowerBean;
                } catch (Throwable th) {
                    Throwable th2 = th;
                    unifiedPowerBean2 = unifiedPowerBean;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            unifiedPowerBean2 = unifiedPowerBean;
            return res;
        } catch (Exception e3) {
            e = e3;
        }
        try {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
            return res;
        } catch (Throwable th3) {
            th2 = th3;
            if (cursor != null) {
                cursor.close();
            }
            throw th2;
        }
    }

    public static void initUnifiedPowerAppTable(List<UnifiedPowerBean> list, Context context, SQLiteDatabase db) {
        Uri uri = UNIFIED_POWER_APP_TABLE_URI;
        int count = list.size();
        db.beginTransaction();
        int i = 0;
        while (i < count) {
            try {
                int i2;
                UnifiedPowerBean upb = (UnifiedPowerBean) list.get(i);
                ContentValues values = new ContentValues();
                values.put("pkg_name", upb.getPkg_name());
                String str = ApplicationConstant.UNIFIED_POWER_APP_CHECK;
                if (upb.is_protected()) {
                    i2 = 1;
                } else {
                    i2 = 0;
                }
                values.put(str, Integer.valueOf(i2));
                str = ApplicationConstant.UNIFIED_POWER_APP_SHOW;
                if (upb.is_show()) {
                    i2 = 1;
                } else {
                    i2 = 0;
                }
                values.put(str, Integer.valueOf(i2));
                db.insert(SmartProvider.UNIFIED_POWER_APP_TABLE, null, values);
                i++;
            } catch (Throwable th) {
                db.endTransaction();
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        HwLog.i(TAG, "bulk initUnifiedPowerAppTable " + count);
        context.getContentResolver().notifyChange(uri, null);
    }

    public static void updateUnifiedPowerAppListForDB(ArrayList<String> list, int isWhite, Context context) {
        Uri uri = UNIFIED_POWER_APP_TABLE_URI;
        ArrayList<ContentProviderOperation> ops = new ArrayList(list.size());
        String selection = "pkg_name = ? ";
        for (String pkg : list) {
            ContentValues values = new ContentValues();
            values.put(ApplicationConstant.UNIFIED_POWER_APP_CHECK, Integer.valueOf(isWhite));
            Builder buider = ContentProviderOperation.newUpdate(uri);
            buider.withValues(values);
            buider.withSelection(selection, new String[]{pkg});
            ops.add(buider.build());
        }
        try {
            context.getContentResolver().applyBatch(SmartProvider.AUTH, ops);
            HwLog.i(TAG, "bulk updateUnifiedPowerAppListForDB " + list.toString() + ", protect:" + isWhite);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e2) {
            e2.printStackTrace();
        }
    }

    public static void insertUnifiedPowerAppListForDB(String pkg, boolean protect, boolean show, Context context) {
        int i;
        int i2 = 1;
        Uri uri = UNIFIED_POWER_APP_TABLE_URI;
        ContentValues values = new ContentValues();
        values.put("pkg_name", pkg);
        String str = ApplicationConstant.UNIFIED_POWER_APP_CHECK;
        if (protect) {
            i = 1;
        } else {
            i = 0;
        }
        values.put(str, Integer.valueOf(i));
        String str2 = ApplicationConstant.UNIFIED_POWER_APP_SHOW;
        if (!show) {
            i2 = 0;
        }
        values.put(str2, Integer.valueOf(i2));
        context.getContentResolver().insert(uri, values);
        HwLog.i(TAG, "insertUnifiedPowerAppListForDB  type:" + protect + " pkg=" + pkg + " show=" + show);
        context.getContentResolver().notifyChange(uri, null);
    }

    public static void insertUnifiedPowerAppListForDB(String pkg, boolean protect, boolean show, boolean isChanged, Context context) {
        int i;
        int i2 = 1;
        Uri uri = UNIFIED_POWER_APP_TABLE_URI;
        ContentValues values = new ContentValues();
        values.put("pkg_name", pkg);
        String str = ApplicationConstant.UNIFIED_POWER_APP_CHECK;
        if (protect) {
            i = 1;
        } else {
            i = 0;
        }
        values.put(str, Integer.valueOf(i));
        str = ApplicationConstant.UNIFIED_POWER_APP_SHOW;
        if (show) {
            i = 1;
        } else {
            i = 0;
        }
        values.put(str, Integer.valueOf(i));
        String str2 = "is_changed";
        if (!isChanged) {
            i2 = 0;
        }
        values.put(str2, Integer.valueOf(i2));
        context.getContentResolver().insert(uri, values);
        HwLog.i(TAG, "insertUnifiedPowerAppListForDB  type:" + protect + " pkg=" + pkg + " show=" + show + " ischanged=" + isChanged);
        context.getContentResolver().notifyChange(uri, null);
    }

    public static void deleteUnifiedPowerAppListForDB(String packageName, Context context) {
        context.getContentResolver().delete(UNIFIED_POWER_APP_TABLE_URI, "pkg_name = ? ", new String[]{packageName});
        context.getContentResolver().notifyChange(UNIFIED_POWER_APP_TABLE_URI, null);
    }

    public static UnifiedPowerBean getProtectAppDefaultValueFromDbByPKG(Context context, String packageName) {
        Exception e;
        Throwable th;
        Cursor cursor = null;
        UnifiedPowerBean unifiedPowerBean = null;
        try {
            cursor = context.getContentResolver().query(UNIFIED_POWER_APP_DEFAULT_VALUE_TABLE_URI, new String[]{"pkg_name", ApplicationConstant.UNIFIED_POWER_APP_CHECK, ApplicationConstant.UNIFIED_POWER_APP_SHOW}, "pkg_name=?", new String[]{packageName}, null);
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            if (cursor.moveToNext()) {
                UnifiedPowerBean unifiedPowerBean2 = new UnifiedPowerBean();
                try {
                    unifiedPowerBean2.setPkg_name(cursor.getString(0));
                    boolean protect = cursor.getInt(1) == 1;
                    boolean show = cursor.getInt(2) == 1;
                    unifiedPowerBean2.setIs_protected(protect);
                    unifiedPowerBean2.setIs_show(show);
                    unifiedPowerBean = unifiedPowerBean2;
                } catch (Exception e2) {
                    e = e2;
                    unifiedPowerBean = unifiedPowerBean2;
                    try {
                        e.printStackTrace();
                        if (cursor != null) {
                            cursor.close();
                        }
                        return unifiedPowerBean;
                    } catch (Throwable th2) {
                        th = th2;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return unifiedPowerBean;
        } catch (Exception e3) {
            e = e3;
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
            return unifiedPowerBean;
        }
    }

    public static UnifiedPowerBean getProtectAppFromDbByPKG(Context context, String packageName) {
        Exception e;
        Throwable th;
        boolean show = true;
        Cursor cursor = null;
        UnifiedPowerBean unifiedPowerBean = null;
        try {
            cursor = context.getContentResolver().query(UNIFIED_POWER_APP_TABLE_URI, new String[]{"pkg_name", ApplicationConstant.UNIFIED_POWER_APP_CHECK, ApplicationConstant.UNIFIED_POWER_APP_SHOW}, "pkg_name=?", new String[]{packageName}, null);
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            if (cursor.moveToNext()) {
                UnifiedPowerBean unifiedPowerBean2 = new UnifiedPowerBean();
                try {
                    boolean protect;
                    unifiedPowerBean2.setPkg_name(cursor.getString(0));
                    if (cursor.getInt(1) == 1) {
                        protect = true;
                    } else {
                        protect = false;
                    }
                    unifiedPowerBean2.setIs_protected(protect);
                    if (cursor.getInt(2) != 1) {
                        show = false;
                    }
                    unifiedPowerBean2.setIs_show(show);
                    unifiedPowerBean = unifiedPowerBean2;
                } catch (Exception e2) {
                    e = e2;
                    unifiedPowerBean = unifiedPowerBean2;
                    try {
                        e.printStackTrace();
                        if (cursor != null) {
                            cursor.close();
                        }
                        return unifiedPowerBean;
                    } catch (Throwable th2) {
                        th = th2;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return unifiedPowerBean;
        } catch (Exception e3) {
            e = e3;
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
            return unifiedPowerBean;
        }
    }

    public static void updateUnifiedPowerAppListForDB(String pkg, boolean protect, boolean show, Context context) {
        int i;
        Uri uri = UNIFIED_POWER_APP_TABLE_URI;
        ContentValues values = new ContentValues();
        String str = ApplicationConstant.UNIFIED_POWER_APP_CHECK;
        if (protect) {
            i = 1;
        } else {
            i = 0;
        }
        values.put(str, Integer.valueOf(i));
        str = ApplicationConstant.UNIFIED_POWER_APP_SHOW;
        if (show) {
            i = 1;
        } else {
            i = 0;
        }
        values.put(str, Integer.valueOf(i));
        context.getContentResolver().update(uri, values, "pkg_name= ?", new String[]{pkg});
        HwLog.i(TAG, "updateUnifiedPowerAppListForDB  protect=" + protect + " pkg=" + pkg + " show=" + show);
        if (protect) {
            UnifiedPowerAppControl.getInstance(GlobalContext.getContext()).addAppToFWKForDOZEAndAppStandby(pkg);
        } else {
            UnifiedPowerAppControl.getInstance(GlobalContext.getContext()).removeAppToFWKForDOZEAndAppStandby(pkg);
        }
    }

    public static void updateUnifiedPowerAppForChangedColumns(String pkg, boolean changed, Context context) {
        int i;
        Uri uri = UNIFIED_POWER_APP_TABLE_URI;
        ContentValues values = new ContentValues();
        String str = "is_changed";
        if (changed) {
            i = 1;
        } else {
            i = 0;
        }
        values.put(str, Integer.valueOf(i));
        context.getContentResolver().update(uri, values, "pkg_name= ?", new String[]{pkg});
    }

    public static int getUnifiedPowerAppChangedStatusByPKG(Context context, String packageName) {
        Cursor cursor = null;
        int temChanged = -1;
        try {
            cursor = context.getContentResolver().query(UNIFIED_POWER_APP_TABLE_URI, new String[]{"is_changed"}, "pkg_name=?", new String[]{packageName}, null);
            if (cursor == null) {
                temChanged = -1;
            } else if (cursor.moveToNext()) {
                temChanged = cursor.getInt(0);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return temChanged;
    }
}
