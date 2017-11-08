package com.huawei.systemmanager.power.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import com.huawei.systemmanager.comm.component.SuperHighPowerBean;
import com.huawei.systemmanager.power.provider.SmartProvider;
import com.huawei.systemmanager.power.provider.SmartProvider.ROGUE_Columns;
import com.huawei.systemmanager.power.provider.SmartProvider.WakeUp_Columns;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.HashMap;

public class SavingSettingUtil {
    public static final int PACKAGE_FIELD_HIGHWAKEFREQ = 4;
    public static final int PACKAGE_FIELD_IGNORE = 1;
    public static final int PACKAGE_FIELD_IGNOREWAKEUP = 5;
    public static final int PACKAGE_FIELD_ROGUE_TYPE = 7;
    public static final int PACKAGE_FIELD_TIME_WHEN_HIGH_POWER = 6;
    public static final int PACKAGE_FIELD_TYPE = 0;
    public static final int PACKAGE_FIELD_WAKEUPNUM_ALL = 0;
    private static final String TAG = "SavingSettingUtil";

    public static java.util.HashSet<java.lang.String> getRogueAppSet(android.content.Context r10) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0046 in list []
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
        r8 = new java.util.HashSet;
        r8.<init>();
        r6 = 0;
        r0 = r10.getContentResolver();	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r1 = com.huawei.systemmanager.power.provider.SmartProvider.ROGUE_APP_RUI;	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r2 = 1;	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r2 = new java.lang.String[r2];	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r3 = "pkgname";	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r4 = 0;	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r2[r4] = r3;	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r3 = "roguetype != ?";	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r4 = 1;	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r4 = new java.lang.String[r4];	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r5 = "0";	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r9 = 0;	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r4[r9] = r5;	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r5 = 0;	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        if (r6 != 0) goto L_0x002e;
    L_0x0028:
        if (r6 == 0) goto L_0x002d;
    L_0x002a:
        r6.close();
    L_0x002d:
        return r8;
    L_0x002e:
        r0 = r6.moveToNext();	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        if (r0 == 0) goto L_0x0047;	 Catch:{ Exception -> 0x003d, all -> 0x004d }
    L_0x0034:
        r0 = 0;	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r0 = r6.getString(r0);	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r8.add(r0);	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        goto L_0x002e;
    L_0x003d:
        r7 = move-exception;
        r7.printStackTrace();	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        if (r6 == 0) goto L_0x0046;
    L_0x0043:
        r6.close();
    L_0x0046:
        return r8;
    L_0x0047:
        if (r6 == 0) goto L_0x004c;
    L_0x0049:
        r6.close();
    L_0x004c:
        return r8;
    L_0x004d:
        r0 = move-exception;
        if (r6 == 0) goto L_0x0053;
    L_0x0050:
        r6.close();
    L_0x0053:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.systemmanager.power.util.SavingSettingUtil.getRogueAppSet(android.content.Context):java.util.HashSet<java.lang.String>");
    }

    public static java.util.HashMap<java.lang.String, java.lang.Long> getRougeAppWithTimeInfo(android.content.Context r10) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x005b in list []
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
        r8 = new java.util.HashMap;
        r8.<init>();
        r6 = 0;
        r0 = r10.getContentResolver();	 Catch:{ Exception -> 0x0052, all -> 0x0062 }
        r1 = com.huawei.systemmanager.power.provider.SmartProvider.ROGUE_APP_RUI;	 Catch:{ Exception -> 0x0052, all -> 0x0062 }
        r2 = 2;	 Catch:{ Exception -> 0x0052, all -> 0x0062 }
        r2 = new java.lang.String[r2];	 Catch:{ Exception -> 0x0052, all -> 0x0062 }
        r3 = "pkgname";	 Catch:{ Exception -> 0x0052, all -> 0x0062 }
        r4 = 0;	 Catch:{ Exception -> 0x0052, all -> 0x0062 }
        r2[r4] = r3;	 Catch:{ Exception -> 0x0052, all -> 0x0062 }
        r3 = "time";	 Catch:{ Exception -> 0x0052, all -> 0x0062 }
        r4 = 1;	 Catch:{ Exception -> 0x0052, all -> 0x0062 }
        r2[r4] = r3;	 Catch:{ Exception -> 0x0052, all -> 0x0062 }
        r3 = "isrogue = ? or roguetype != ?";	 Catch:{ Exception -> 0x0052, all -> 0x0062 }
        r4 = 2;	 Catch:{ Exception -> 0x0052, all -> 0x0062 }
        r4 = new java.lang.String[r4];	 Catch:{ Exception -> 0x0052, all -> 0x0062 }
        r5 = "1";	 Catch:{ Exception -> 0x0052, all -> 0x0062 }
        r9 = 0;	 Catch:{ Exception -> 0x0052, all -> 0x0062 }
        r4[r9] = r5;	 Catch:{ Exception -> 0x0052, all -> 0x0062 }
        r5 = "0";	 Catch:{ Exception -> 0x0052, all -> 0x0062 }
        r9 = 1;	 Catch:{ Exception -> 0x0052, all -> 0x0062 }
        r4[r9] = r5;	 Catch:{ Exception -> 0x0052, all -> 0x0062 }
        r5 = 0;	 Catch:{ Exception -> 0x0052, all -> 0x0062 }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x0052, all -> 0x0062 }
        if (r6 != 0) goto L_0x003a;
    L_0x0034:
        if (r6 == 0) goto L_0x0039;
    L_0x0036:
        r6.close();
    L_0x0039:
        return r8;
    L_0x003a:
        r0 = r6.moveToNext();	 Catch:{ Exception -> 0x0052, all -> 0x0062 }
        if (r0 == 0) goto L_0x005c;	 Catch:{ Exception -> 0x0052, all -> 0x0062 }
    L_0x0040:
        r0 = 0;	 Catch:{ Exception -> 0x0052, all -> 0x0062 }
        r0 = r6.getString(r0);	 Catch:{ Exception -> 0x0052, all -> 0x0062 }
        r1 = 1;	 Catch:{ Exception -> 0x0052, all -> 0x0062 }
        r2 = r6.getLong(r1);	 Catch:{ Exception -> 0x0052, all -> 0x0062 }
        r1 = java.lang.Long.valueOf(r2);	 Catch:{ Exception -> 0x0052, all -> 0x0062 }
        r8.put(r0, r1);	 Catch:{ Exception -> 0x0052, all -> 0x0062 }
        goto L_0x003a;
    L_0x0052:
        r7 = move-exception;
        r7.printStackTrace();	 Catch:{ Exception -> 0x0052, all -> 0x0062 }
        if (r6 == 0) goto L_0x005b;
    L_0x0058:
        r6.close();
    L_0x005b:
        return r8;
    L_0x005c:
        if (r6 == 0) goto L_0x0061;
    L_0x005e:
        r6.close();
    L_0x0061:
        return r8;
    L_0x0062:
        r0 = move-exception;
        if (r6 == 0) goto L_0x0068;
    L_0x0065:
        r6.close();
    L_0x0068:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.systemmanager.power.util.SavingSettingUtil.getRougeAppWithTimeInfo(android.content.Context):java.util.HashMap<java.lang.String, java.lang.Long>");
    }

    public static java.util.ArrayList<java.lang.String> getWakeupPendingApp(android.content.Context r10) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0046 in list []
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
        r8 = new java.util.ArrayList;
        r8.<init>();
        r6 = 0;
        r0 = r10.getContentResolver();	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r1 = com.huawei.systemmanager.power.provider.SmartProvider.ROGUE_APP_RUI;	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r2 = 1;	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r2 = new java.lang.String[r2];	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r3 = "pkgname";	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r4 = 0;	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r2[r4] = r3;	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r3 = "ignorewakeup = ?";	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r4 = 1;	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r4 = new java.lang.String[r4];	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r5 = "1";	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r9 = 0;	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r4[r9] = r5;	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r5 = 0;	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        if (r6 != 0) goto L_0x002e;
    L_0x0028:
        if (r6 == 0) goto L_0x002d;
    L_0x002a:
        r6.close();
    L_0x002d:
        return r8;
    L_0x002e:
        r0 = r6.moveToNext();	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        if (r0 == 0) goto L_0x0047;	 Catch:{ Exception -> 0x003d, all -> 0x004d }
    L_0x0034:
        r0 = 0;	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r0 = r6.getString(r0);	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        r8.add(r0);	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        goto L_0x002e;
    L_0x003d:
        r7 = move-exception;
        r7.printStackTrace();	 Catch:{ Exception -> 0x003d, all -> 0x004d }
        if (r6 == 0) goto L_0x0046;
    L_0x0043:
        r6.close();
    L_0x0046:
        return r8;
    L_0x0047:
        if (r6 == 0) goto L_0x004c;
    L_0x0049:
        r6.close();
    L_0x004c:
        return r8;
    L_0x004d:
        r0 = move-exception;
        if (r6 == 0) goto L_0x0053;
    L_0x0050:
        r6.close();
    L_0x0053:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.systemmanager.power.util.SavingSettingUtil.getWakeupPendingApp(android.content.Context):java.util.ArrayList<java.lang.String>");
    }

    public static void setRogue(ContentResolver resolver, String pkgname, int flag, Object value) {
        ContentValues updateValues = new ContentValues();
        switch (flag) {
            case 0:
                updateValues.put(ROGUE_Columns.ISROGUE, Integer.valueOf(Integer.parseInt(value.toString())));
                break;
            case 1:
                updateValues.put(ROGUE_Columns.IGNORE, Integer.valueOf(Integer.parseInt(value.toString())));
                break;
            case 2:
                updateValues.put(ROGUE_Columns.CLEAR, Integer.valueOf(Integer.parseInt(value.toString())));
                break;
            case 3:
                updateValues.put(ROGUE_Columns.PRESETBLACKAPP, Integer.valueOf(Integer.parseInt(value.toString())));
                break;
            case 4:
                updateValues.put(ROGUE_Columns.HIGHWAKEUPFREQ, Integer.valueOf(Integer.parseInt(value.toString())));
                break;
            case 5:
                updateValues.put(ROGUE_Columns.IGNOREWAKEUPAPP, Integer.valueOf(Integer.parseInt(value.toString())));
                break;
            case 6:
                updateValues.put(ROGUE_Columns.TIME, value.toString());
                break;
            case 7:
                updateValues.put(ROGUE_Columns.ROGUETYPE, value.toString());
                break;
            default:
                throw new UnsupportedOperationException("Invalid segment flag = " + flag);
        }
        resolver.update(Uri.parse("content://com.huawei.android.smartpowerprovider/rogueapps"), updateValues, "pkgname = ?", new String[]{pkgname});
    }

    public static Object getRogue(ContentResolver resolver, String pkgname, int flag) {
        Object obj = null;
        String fieldName = "";
        switch (flag) {
            case 0:
                fieldName = ROGUE_Columns.ISROGUE;
                break;
            case 1:
                fieldName = ROGUE_Columns.IGNORE;
                break;
            case 2:
                fieldName = ROGUE_Columns.CLEAR;
                break;
            case 3:
                fieldName = ROGUE_Columns.PRESETBLACKAPP;
                break;
            case 4:
                fieldName = ROGUE_Columns.HIGHWAKEUPFREQ;
                break;
            case 5:
                fieldName = ROGUE_Columns.IGNOREWAKEUPAPP;
                break;
            case 6:
                fieldName = ROGUE_Columns.TIME;
                break;
            case 7:
                fieldName = ROGUE_Columns.ROGUETYPE;
                break;
            default:
                throw new UnsupportedOperationException("Invalid segment flag = " + flag);
        }
        ContentResolver contentResolver = resolver;
        Cursor c = contentResolver.query(Uri.parse("content://com.huawei.android.smartpowerprovider/rogueapps"), null, "pkgname = ?", new String[]{pkgname}, null);
        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToFirst();
                obj = Integer.valueOf(c.getInt(c.getColumnIndex(fieldName)));
            }
            c.close();
        }
        return obj;
    }

    public static void insertRogue(ContentResolver resolver, String pkgname, ContentValues values) {
        if (getRogue(resolver, pkgname, 0) == null) {
            resolver.insert(Uri.parse("content://com.huawei.android.smartpowerprovider/rogueapps"), values);
            return;
        }
        setRogue(resolver, pkgname, 0, values.get(ROGUE_Columns.ISROGUE));
        setRogue(resolver, pkgname, 1, values.get(ROGUE_Columns.IGNORE));
        setRogue(resolver, pkgname, 2, values.get(ROGUE_Columns.CLEAR));
        setRogue(resolver, pkgname, 3, values.get(ROGUE_Columns.PRESETBLACKAPP));
        setRogue(resolver, pkgname, 4, values.get(ROGUE_Columns.HIGHWAKEUPFREQ));
        setRogue(resolver, pkgname, 5, values.get(ROGUE_Columns.IGNOREWAKEUPAPP));
        setRogue(resolver, pkgname, 6, values.get(ROGUE_Columns.TIME));
        setRogue(resolver, pkgname, 7, values.get(ROGUE_Columns.ROGUETYPE));
        HwLog.i(TAG, "the pkgname = " + pkgname + " is exist!");
    }

    public static void setWakeUpApp(ContentResolver resolver, String pkgname, ContentValues values) {
        ContentValues updateValues = new ContentValues();
        updateValues.put(WakeUp_Columns.WAKEUPNUM_ALL, values.get(WakeUp_Columns.WAKEUPNUM_ALL).toString());
        updateValues.put(WakeUp_Columns.WAKEUPNUM_H, values.get(WakeUp_Columns.WAKEUPNUM_H).toString());
        resolver.update(Uri.parse("content://com.huawei.android.smartpowerprovider/wakeupapps"), updateValues, "pkgname = ?", new String[]{pkgname});
    }

    public static void insertWakeUpApp(ContentResolver resolver, String pkgname, ContentValues values) {
        if (getWakeUpApp(resolver, pkgname, 0) == null) {
            resolver.insert(Uri.parse("content://com.huawei.android.smartpowerprovider/wakeupapps"), values);
            return;
        }
        setWakeUpApp(resolver, pkgname, values);
        HwLog.i(TAG, "the pkgname = " + pkgname + " is exist!");
    }

    public static Object getWakeUpApp(ContentResolver resolver, String pkgname, int flag) {
        Object obj = null;
        String fieldName = "";
        switch (flag) {
            case 0:
                fieldName = WakeUp_Columns.WAKEUPNUM_ALL;
                break;
            case 1:
                fieldName = WakeUp_Columns.WAKEUPNUM_H;
                break;
            default:
                throw new UnsupportedOperationException("Invalid segment flag = " + flag);
        }
        ContentResolver contentResolver = resolver;
        Cursor c = contentResolver.query(Uri.parse("content://com.huawei.android.smartpowerprovider/wakeupapps"), null, "pkgname = ?", new String[]{pkgname}, null);
        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToFirst();
                obj = Integer.valueOf(c.getInt(c.getColumnIndex(fieldName)));
            }
            c.close();
        }
        return obj;
    }

    public static HashMap<String, Integer> databaseQuery(ContentResolver resolver) {
        HashMap<String, Integer> appMap = new HashMap();
        Cursor cursor = null;
        try {
            cursor = resolver.query(SmartProvider.WAKEUP_RUI, new String[]{"pkgname", WakeUp_Columns.WAKEUPNUM_ALL}, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    appMap.put(cursor.getString(0), Integer.valueOf(Integer.parseInt(cursor.getString(1))));
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException e) {
            HwLog.e(TAG, "Database exception!");
            return appMap;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return appMap;
    }

    public static ArrayList<String> rogueDBQuery(Context ctx) {
        ContentResolver resolver = ctx.getContentResolver();
        ArrayList<String> appList = new ArrayList();
        Cursor cursor = null;
        try {
            cursor = resolver.query(SmartProvider.ROGUE_APP_RUI, new String[]{"pkgname"}, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    appList.add(cursor.getString(0));
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException e) {
            HwLog.e(TAG, "Database exception!");
            return appList;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return appList;
    }

    public static ArrayList<SuperHighPowerBean> querySuperHighPowerApps(Context ctx) {
        ContentResolver resolver = ctx.getContentResolver();
        ArrayList<SuperHighPowerBean> appList = new ArrayList();
        Cursor cursor = null;
        cursor = resolver.query(SmartProvider.ROGUE_APP_RUI, new String[]{"pkgname", ROGUE_Columns.TIME}, "roguetype = 4", null, "time desc");
        if (cursor != null) {
            HwLog.i(TAG, "cursor.getCount() =" + cursor.getCount());
            PackageManager pm = ctx.getPackageManager();
            while (cursor.moveToNext()) {
                ApplicationInfo ai;
                SuperHighPowerBean superHighPowerBean = new SuperHighPowerBean();
                superHighPowerBean.setmPkg(cursor.getString(0));
                superHighPowerBean.setmTime(cursor.getLong(1));
                try {
                    ai = pm.getApplicationInfo(superHighPowerBean.getmPkg(), 8192);
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    superHighPowerBean.setmAppIcon(pm.getApplicationIcon(ai));
                    superHighPowerBean.setmLabel(pm.getApplicationLabel(ai).toString());
                    appList.add(superHighPowerBean);
                } catch (SQLiteException e2) {
                    HwLog.e(TAG, "querySuperHighPowerApps Database exception!");
                    return appList;
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return appList;
    }

    public static void deleteRogueFromSmartProviderDB(Context context, String pkgName) {
        context.getContentResolver().delete(SmartProvider.ROGUE_APP_RUI, "pkgname = ?", new String[]{pkgName});
    }

    public static void recordRogue(Context ctx, String pkgName, ArrayList<String> rogueApp, int type) {
        Object rogueType = getRogue(ctx.getContentResolver(), pkgName, 7);
        if (rogueType != null && ((Integer) rogueType).intValue() == 4) {
            HwLog.i(TAG, "recordRogue:it's a super high power app,so don't record again!");
        }
        try {
            ContentValues values = new ContentValues();
            if (rogueApp.isEmpty() || !rogueApp.contains(pkgName)) {
                HwLog.d(TAG, "--> start to execute values.put()");
                values.put("pkgname", pkgName);
                values.put(ROGUE_Columns.ISROGUE, Integer.valueOf(0));
                values.put(ROGUE_Columns.IGNORE, Integer.valueOf(0));
                values.put(ROGUE_Columns.CLEAR, Integer.valueOf(0));
                values.put(ROGUE_Columns.PRESETBLACKAPP, Integer.valueOf(0));
                if (1 == type) {
                    values.put(ROGUE_Columns.HIGHWAKEUPFREQ, Integer.valueOf(1));
                } else {
                    values.put(ROGUE_Columns.HIGHWAKEUPFREQ, Integer.valueOf(0));
                }
                values.put(ROGUE_Columns.IGNOREWAKEUPAPP, Integer.valueOf(0));
                values.put(ROGUE_Columns.ROGUETYPE, Integer.valueOf(type));
                insertRogue(ctx.getContentResolver(), pkgName, values);
                values.clear();
                setRogue(ctx.getContentResolver(), pkgName, 6, Long.valueOf(System.currentTimeMillis()));
            }
            HwLog.i(TAG, "--> start to execute SavingSettingUtil.setRogue");
            setRogue(ctx.getContentResolver(), pkgName, 7, Integer.valueOf(type));
            if (1 == type) {
                setRogue(ctx.getContentResolver(), pkgName, 4, Integer.valueOf(1));
            }
            setRogue(ctx.getContentResolver(), pkgName, 6, Long.valueOf(System.currentTimeMillis()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void recordHighPower(Context ctx, String pkgName, ArrayList<String> rogueApp) {
        Object rogueType = getRogue(ctx.getContentResolver(), pkgName, 7);
        if (rogueType != null && ((Integer) rogueType).intValue() == 4) {
            HwLog.i(TAG, "recordHighPower:it's a super high power app,so don't record again!");
        }
        try {
            ContentValues values = new ContentValues();
            if (rogueApp.isEmpty() || !rogueApp.contains(pkgName)) {
                HwLog.d(TAG, "--> start to execute values.put()");
                values.put("pkgname", pkgName);
                values.put(ROGUE_Columns.ISROGUE, Integer.valueOf(1));
                values.put(ROGUE_Columns.IGNORE, Integer.valueOf(0));
                values.put(ROGUE_Columns.CLEAR, Integer.valueOf(0));
                values.put(ROGUE_Columns.PRESETBLACKAPP, Integer.valueOf(0));
                values.put(ROGUE_Columns.HIGHWAKEUPFREQ, Integer.valueOf(0));
                values.put(ROGUE_Columns.IGNOREWAKEUPAPP, Integer.valueOf(0));
                values.put(ROGUE_Columns.ROGUETYPE, Integer.valueOf(0));
                insertRogue(ctx.getContentResolver(), pkgName, values);
                values.clear();
                setRogue(ctx.getContentResolver(), pkgName, 6, Long.valueOf(System.currentTimeMillis()));
            }
            HwLog.i(TAG, "--> start to execute SavingSettingUtil.setRogue");
            setRogue(ctx.getContentResolver(), pkgName, 0, Integer.valueOf(1));
            setRogue(ctx.getContentResolver(), pkgName, 6, Long.valueOf(System.currentTimeMillis()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void recordSuperHighPower(Context ctx, String pkgName) {
        try {
            ArrayList<String> superPowerApp = superPowerAppsDBQuery(ctx);
            Long curTimeMillis = Long.valueOf(System.currentTimeMillis());
            if (superPowerApp.isEmpty() || !superPowerApp.contains(pkgName)) {
                ContentValues values = new ContentValues();
                values.put("pkgname", pkgName);
                values.put(ROGUE_Columns.TIME, curTimeMillis);
                ctx.getContentResolver().insert(Uri.parse("content://com.huawei.android.smartpowerprovider/superpowerapps"), values);
                values.clear();
                return;
            }
            ContentValues updateValues = new ContentValues();
            updateValues.put(ROGUE_Columns.TIME, curTimeMillis);
            ctx.getContentResolver().update(Uri.parse("content://com.huawei.android.smartpowerprovider/superpowerapps"), updateValues, "pkgname = ?", new String[]{pkgName});
            updateValues.clear();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<SuperHighPowerBean> querySuperHighPowerAppsFromDB(Context ctx) {
        ContentResolver resolver = ctx.getContentResolver();
        ArrayList<SuperHighPowerBean> appList = new ArrayList();
        Cursor cursor = null;
        cursor = resolver.query(SmartProvider.SUPERPOWER_APP_URI, new String[]{"pkgname", ROGUE_Columns.TIME}, null, null, "time desc");
        if (cursor != null) {
            HwLog.i(TAG, "cursor.getCount() =" + cursor.getCount());
            PackageManager pm = ctx.getPackageManager();
            while (cursor.moveToNext()) {
                SuperHighPowerBean mSuperHighPowerBean = new SuperHighPowerBean();
                mSuperHighPowerBean.setmPkg(cursor.getString(0));
                mSuperHighPowerBean.setmTime(cursor.getLong(1));
                try {
                    ApplicationInfo ai = pm.getApplicationInfo(mSuperHighPowerBean.getmPkg(), 8192);
                    mSuperHighPowerBean.setmAppIcon(pm.getApplicationIcon(ai));
                    mSuperHighPowerBean.setmLabel(pm.getApplicationLabel(ai).toString());
                    appList.add(mSuperHighPowerBean);
                } catch (NameNotFoundException e) {
                    try {
                        e.printStackTrace();
                    } catch (SQLiteException e2) {
                        HwLog.e(TAG, "querySuperHighPowerApps Database exception!");
                        return appList;
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return appList;
    }

    public static ArrayList<String> superPowerAppsDBQuery(Context ctx) {
        ContentResolver resolver = ctx.getContentResolver();
        ArrayList<String> appList = new ArrayList();
        Cursor cursor = null;
        try {
            cursor = resolver.query(SmartProvider.SUPERPOWER_APP_URI, new String[]{"pkgname"}, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    appList.add(cursor.getString(0));
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException e) {
            HwLog.e(TAG, "Database exception!");
            return appList;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return appList;
    }

    public static boolean isPkgNameExistInRogueDB(ContentResolver resolver, String pkgname) {
        boolean IsExist = false;
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = resolver;
            cursor = contentResolver.query(Uri.parse("content://com.huawei.android.smartpowerprovider/rogueapps"), null, "pkgname = ?", new String[]{pkgname}, null);
            if (cursor != null && cursor.getCount() > 0) {
                IsExist = true;
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException e) {
            HwLog.e(TAG, "Database exception!");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return IsExist;
    }
}
