package com.huawei.hwid.c.a;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import com.android.mms.transaction.HwCustHttpUtilsImpl;
import com.huawei.hwid.core.encrypt.e;

/* compiled from: VIPDatabase */
public class a extends SQLiteOpenHelper {
    public static boolean a(android.content.Context r8) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0048 in list []
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
        r7 = 1;
        r6 = 0;
        r1 = 0;
        r0 = "VIPDatabase";
        r2 = "enter VIPDatabase::clearCurUserInfo()";
        com.huawei.hwid.core.c.b.a.a(r0, r2);
        r0 = new com.huawei.hwid.c.a.a;	 Catch:{ Exception -> 0x003a, all -> 0x004c }
        r0.<init>(r8);	 Catch:{ Exception -> 0x003a, all -> 0x004c }
        r1 = r0.getWritableDatabase();	 Catch:{ Exception -> 0x003a, all -> 0x004c }
        r0 = "vip_config";	 Catch:{ Exception -> 0x003a, all -> 0x004c }
        r2 = "name in (?, ?, ?)";	 Catch:{ Exception -> 0x003a, all -> 0x004c }
        r3 = 3;	 Catch:{ Exception -> 0x003a, all -> 0x004c }
        r3 = new java.lang.String[r3];	 Catch:{ Exception -> 0x003a, all -> 0x004c }
        r4 = 0;	 Catch:{ Exception -> 0x003a, all -> 0x004c }
        r5 = "curUserId";	 Catch:{ Exception -> 0x003a, all -> 0x004c }
        r3[r4] = r5;	 Catch:{ Exception -> 0x003a, all -> 0x004c }
        r4 = 1;	 Catch:{ Exception -> 0x003a, all -> 0x004c }
        r5 = "curUserRightId";	 Catch:{ Exception -> 0x003a, all -> 0x004c }
        r3[r4] = r5;	 Catch:{ Exception -> 0x003a, all -> 0x004c }
        r4 = 2;	 Catch:{ Exception -> 0x003a, all -> 0x004c }
        r5 = "curUserQueryRightUrl";	 Catch:{ Exception -> 0x003a, all -> 0x004c }
        r3[r4] = r5;	 Catch:{ Exception -> 0x003a, all -> 0x004c }
        r1.delete(r0, r2, r3);	 Catch:{ Exception -> 0x003a, all -> 0x004c }
        if (r1 != 0) goto L_0x0036;
    L_0x0035:
        return r7;
    L_0x0036:
        r1.close();
        goto L_0x0035;
    L_0x003a:
        r0 = move-exception;
        r2 = "VIPDatabase";	 Catch:{ Exception -> 0x003a, all -> 0x004c }
        r3 = r0.toString();	 Catch:{ Exception -> 0x003a, all -> 0x004c }
        com.huawei.hwid.core.c.b.a.d(r2, r3, r0);	 Catch:{ Exception -> 0x003a, all -> 0x004c }
        if (r1 != 0) goto L_0x0048;
    L_0x0047:
        return r6;
    L_0x0048:
        r1.close();
        goto L_0x0047;
    L_0x004c:
        r0 = move-exception;
        if (r1 != 0) goto L_0x0050;
    L_0x004f:
        throw r0;
    L_0x0050:
        r1.close();
        goto L_0x004f;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.hwid.c.a.a.a(android.content.Context):boolean");
    }

    public static boolean a(android.content.Context r8, java.lang.String r9, int r10, java.lang.String r11) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0081 in list []
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
        r7 = 1;
        r6 = 0;
        r1 = 0;
        r0 = "VIPDatabase";
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "enter VIPDatabase::updateCurUserInfo(userId:";
        r2 = r2.append(r3);
        r3 = com.huawei.hwid.core.encrypt.f.a(r9);
        r2 = r2.append(r3);
        r3 = " rights:";
        r2 = r2.append(r3);
        r2 = r2.append(r10);
        r3 = " expireDate:";
        r2 = r2.append(r3);
        r2 = r2.append(r11);
        r2 = r2.toString();
        com.huawei.hwid.core.c.b.a.a(r0, r2);
        r0 = new com.huawei.hwid.c.a.a;	 Catch:{ Exception -> 0x0073, all -> 0x0085 }
        r0.<init>(r8);	 Catch:{ Exception -> 0x0073, all -> 0x0085 }
        r1 = r0.getWritableDatabase();	 Catch:{ Exception -> 0x0073, all -> 0x0085 }
        r0 = "vip_config";	 Catch:{ Exception -> 0x0073, all -> 0x0085 }
        r2 = "name in (?, ?)";	 Catch:{ Exception -> 0x0073, all -> 0x0085 }
        r3 = 2;	 Catch:{ Exception -> 0x0073, all -> 0x0085 }
        r3 = new java.lang.String[r3];	 Catch:{ Exception -> 0x0073, all -> 0x0085 }
        r4 = 0;	 Catch:{ Exception -> 0x0073, all -> 0x0085 }
        r5 = "curUserId";	 Catch:{ Exception -> 0x0073, all -> 0x0085 }
        r3[r4] = r5;	 Catch:{ Exception -> 0x0073, all -> 0x0085 }
        r4 = 1;	 Catch:{ Exception -> 0x0073, all -> 0x0085 }
        r5 = "curUserRightId";	 Catch:{ Exception -> 0x0073, all -> 0x0085 }
        r3[r4] = r5;	 Catch:{ Exception -> 0x0073, all -> 0x0085 }
        r1.delete(r0, r2, r3);	 Catch:{ Exception -> 0x0073, all -> 0x0085 }
        r0 = "curUserId";	 Catch:{ Exception -> 0x0073, all -> 0x0085 }
        r2 = com.huawei.hwid.core.encrypt.e.b(r8, r9);	 Catch:{ Exception -> 0x0073, all -> 0x0085 }
        a(r1, r0, r2);	 Catch:{ Exception -> 0x0073, all -> 0x0085 }
        r0 = "curUserRightId";	 Catch:{ Exception -> 0x0073, all -> 0x0085 }
        r2 = java.lang.String.valueOf(r10);	 Catch:{ Exception -> 0x0073, all -> 0x0085 }
        a(r1, r0, r2);	 Catch:{ Exception -> 0x0073, all -> 0x0085 }
        if (r1 != 0) goto L_0x006f;
    L_0x006e:
        return r7;
    L_0x006f:
        r1.close();
        goto L_0x006e;
    L_0x0073:
        r0 = move-exception;
        r2 = "VIPDatabase";	 Catch:{ Exception -> 0x0073, all -> 0x0085 }
        r3 = r0.toString();	 Catch:{ Exception -> 0x0073, all -> 0x0085 }
        com.huawei.hwid.core.c.b.a.d(r2, r3, r0);	 Catch:{ Exception -> 0x0073, all -> 0x0085 }
        if (r1 != 0) goto L_0x0081;
    L_0x0080:
        return r6;
    L_0x0081:
        r1.close();
        goto L_0x0080;
    L_0x0085:
        r0 = move-exception;
        if (r1 != 0) goto L_0x0089;
    L_0x0088:
        throw r0;
    L_0x0089:
        r1.close();
        goto L_0x0088;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.hwid.c.a.a.a(android.content.Context, java.lang.String, int, java.lang.String):boolean");
    }

    public static boolean b(android.content.Context r8, java.lang.String r9, int r10, java.lang.String r11) {
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
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r7 = 1;
        r6 = 0;
        r1 = 0;
        r0 = "VIPDatabase";
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "enter VIPDatabase::updateDeviceBindleInfo(userId:";
        r2 = r2.append(r3);
        r3 = com.huawei.hwid.core.encrypt.f.a(r9);
        r2 = r2.append(r3);
        r3 = " rights:";
        r2 = r2.append(r3);
        r2 = r2.append(r10);
        r3 = " expireDate:";
        r2 = r2.append(r3);
        r2 = r2.append(r11);
        r2 = r2.toString();
        com.huawei.hwid.core.c.b.a.a(r0, r2);
        r0 = new com.huawei.hwid.c.a.a;	 Catch:{ Exception -> 0x007f, all -> 0x0091 }
        r0.<init>(r8);	 Catch:{ Exception -> 0x007f, all -> 0x0091 }
        r1 = r0.getWritableDatabase();	 Catch:{ Exception -> 0x007f, all -> 0x0091 }
        r0 = "vip_config";	 Catch:{ Exception -> 0x007f, all -> 0x0091 }
        r2 = "name in (?, ?, ?)";	 Catch:{ Exception -> 0x007f, all -> 0x0091 }
        r3 = 3;	 Catch:{ Exception -> 0x007f, all -> 0x0091 }
        r3 = new java.lang.String[r3];	 Catch:{ Exception -> 0x007f, all -> 0x0091 }
        r4 = 0;	 Catch:{ Exception -> 0x007f, all -> 0x0091 }
        r5 = "deviceVipUserId";	 Catch:{ Exception -> 0x007f, all -> 0x0091 }
        r3[r4] = r5;	 Catch:{ Exception -> 0x007f, all -> 0x0091 }
        r4 = 1;	 Catch:{ Exception -> 0x007f, all -> 0x0091 }
        r5 = "deviceVipRightId";	 Catch:{ Exception -> 0x007f, all -> 0x0091 }
        r3[r4] = r5;	 Catch:{ Exception -> 0x007f, all -> 0x0091 }
        r4 = 2;	 Catch:{ Exception -> 0x007f, all -> 0x0091 }
        r5 = "deviceVipExpireDate";	 Catch:{ Exception -> 0x007f, all -> 0x0091 }
        r3[r4] = r5;	 Catch:{ Exception -> 0x007f, all -> 0x0091 }
        r1.delete(r0, r2, r3);	 Catch:{ Exception -> 0x007f, all -> 0x0091 }
        r0 = "deviceVipUserId";	 Catch:{ Exception -> 0x007f, all -> 0x0091 }
        r2 = com.huawei.hwid.core.encrypt.e.b(r8, r9);	 Catch:{ Exception -> 0x007f, all -> 0x0091 }
        a(r1, r0, r2);	 Catch:{ Exception -> 0x007f, all -> 0x0091 }
        r0 = "deviceVipRightId";	 Catch:{ Exception -> 0x007f, all -> 0x0091 }
        r2 = java.lang.String.valueOf(r10);	 Catch:{ Exception -> 0x007f, all -> 0x0091 }
        a(r1, r0, r2);	 Catch:{ Exception -> 0x007f, all -> 0x0091 }
        r0 = "deviceVipExpireDate";	 Catch:{ Exception -> 0x007f, all -> 0x0091 }
        a(r1, r0, r11);	 Catch:{ Exception -> 0x007f, all -> 0x0091 }
        if (r1 != 0) goto L_0x007b;
    L_0x007a:
        return r7;
    L_0x007b:
        r1.close();
        goto L_0x007a;
    L_0x007f:
        r0 = move-exception;
        r2 = "VIPDatabase";	 Catch:{ Exception -> 0x007f, all -> 0x0091 }
        r3 = r0.toString();	 Catch:{ Exception -> 0x007f, all -> 0x0091 }
        com.huawei.hwid.core.c.b.a.d(r2, r3, r0);	 Catch:{ Exception -> 0x007f, all -> 0x0091 }
        if (r1 != 0) goto L_0x008d;
    L_0x008c:
        return r6;
    L_0x008d:
        r1.close();
        goto L_0x008c;
    L_0x0091:
        r0 = move-exception;
        if (r1 != 0) goto L_0x0095;
    L_0x0094:
        throw r0;
    L_0x0095:
        r1.close();
        goto L_0x0094;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.hwid.c.a.a.b(android.content.Context, java.lang.String, int, java.lang.String):boolean");
    }

    public a(Context context) {
        super(context, "vipdatabase.db", null, 1);
    }

    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("Create table vip_config( _id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, value TEXT);");
    }

    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        com.huawei.hwid.core.c.b.a.a("VIPDatabase", "onUpgradeoldVersion=" + i + "newVersion=" + i2);
    }

    public void onDowngrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        com.huawei.hwid.core.c.b.a.a("VIPDatabase", "onDowngradeoldVersion=" + i + "newVersion=" + i2);
    }

    public static String b(Context context) {
        return b(context, "deviceVipUserId");
    }

    public static String c(Context context) {
        return b(context, "curUserId");
    }

    public static boolean a(Context context, String str, String str2) {
        Cursor cursor;
        Throwable th;
        boolean z = true;
        SQLiteDatabase sQLiteDatabase = null;
        Cursor query;
        try {
            SQLiteDatabase writableDatabase = new a(context).getWritableDatabase();
            try {
                query = writableDatabase.query("vip_config", new String[]{HwCustHttpUtilsImpl.CHAMELEON_COLUMNS_VALUE}, "name = ?", new String[]{str}, null, null, null);
            } catch (Throwable e) {
                Throwable th2 = e;
                cursor = null;
                sQLiteDatabase = writableDatabase;
                th = th2;
                try {
                    com.huawei.hwid.core.c.b.a.d("VIPDatabase", th.toString(), th);
                    if (cursor != null) {
                        cursor.close();
                    }
                    if (sQLiteDatabase != null) {
                        sQLiteDatabase.close();
                    }
                    return false;
                } catch (Throwable th3) {
                    th = th3;
                    query = cursor;
                    if (query != null) {
                        query.close();
                    }
                    if (sQLiteDatabase != null) {
                        sQLiteDatabase.close();
                    }
                    throw th;
                }
            } catch (Throwable e2) {
                query = null;
                sQLiteDatabase = writableDatabase;
                th = e2;
                if (query != null) {
                    query.close();
                }
                if (sQLiteDatabase != null) {
                    sQLiteDatabase.close();
                }
                throw th;
            }
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put("name", str);
                contentValues.put(HwCustHttpUtilsImpl.CHAMELEON_COLUMNS_VALUE, str2);
                if (query != null && query.moveToNext()) {
                    boolean z2 = writableDatabase.update("vip_config", contentValues, "name = ?", new String[]{str}) > 0;
                    if (query != null) {
                        query.close();
                    }
                    if (writableDatabase != null) {
                        writableDatabase.close();
                    }
                    return z2;
                }
                if (writableDatabase.insert("vip_config", "", contentValues) <= 0) {
                    z = false;
                }
                if (query != null) {
                    query.close();
                }
                if (writableDatabase != null) {
                    writableDatabase.close();
                }
                return z;
            } catch (Throwable e22) {
                sQLiteDatabase = writableDatabase;
                th = e22;
                cursor = query;
                com.huawei.hwid.core.c.b.a.d("VIPDatabase", th.toString(), th);
                if (cursor != null) {
                    cursor.close();
                }
                if (sQLiteDatabase != null) {
                    sQLiteDatabase.close();
                }
                return false;
            } catch (Throwable e222) {
                sQLiteDatabase = writableDatabase;
                th = e222;
                if (query != null) {
                    query.close();
                }
                if (sQLiteDatabase != null) {
                    sQLiteDatabase.close();
                }
                throw th;
            }
        } catch (Exception e3) {
            th = e3;
            cursor = null;
            com.huawei.hwid.core.c.b.a.d("VIPDatabase", th.toString(), th);
            if (cursor != null) {
                cursor.close();
            }
            if (sQLiteDatabase != null) {
                sQLiteDatabase.close();
            }
            return false;
        } catch (Throwable th4) {
            th = th4;
            query = null;
            if (query != null) {
                query.close();
            }
            if (sQLiteDatabase != null) {
                sQLiteDatabase.close();
            }
            throw th;
        }
    }

    public static String a(Context context, String str) {
        Cursor query;
        Cursor cursor;
        SQLiteDatabase sQLiteDatabase;
        Throwable th;
        SQLiteDatabase sQLiteDatabase2 = null;
        try {
            SQLiteDatabase writableDatabase = new a(context).getWritableDatabase();
            try {
                query = writableDatabase.query("vip_config", new String[]{HwCustHttpUtilsImpl.CHAMELEON_COLUMNS_VALUE}, "name = ?", new String[]{str}, null, null, null);
                if (query != null && query.moveToNext()) {
                    String string = query.getString(0);
                    if (query != null) {
                        query.close();
                    }
                    if (writableDatabase != null) {
                        writableDatabase.close();
                    }
                    return string;
                }
                try {
                    com.huawei.hwid.core.c.b.a.b("VIPDatabase", str + " is not exist in " + "vip_config");
                    if (query != null) {
                        query.close();
                    }
                    if (writableDatabase != null) {
                        writableDatabase.close();
                    }
                    return null;
                } catch (Throwable e) {
                    Throwable th2 = e;
                    cursor = query;
                    sQLiteDatabase = writableDatabase;
                    th = th2;
                    try {
                        com.huawei.hwid.core.c.b.a.d("VIPDatabase", th.toString(), th);
                        if (cursor != null) {
                            cursor.close();
                        }
                        if (sQLiteDatabase != null) {
                            sQLiteDatabase.close();
                        }
                        return null;
                    } catch (Throwable th3) {
                        th = th3;
                        sQLiteDatabase2 = sQLiteDatabase;
                        query = cursor;
                        if (query != null) {
                            query.close();
                        }
                        if (sQLiteDatabase2 != null) {
                            sQLiteDatabase2.close();
                        }
                        throw th;
                    }
                } catch (Throwable e2) {
                    sQLiteDatabase2 = writableDatabase;
                    th = e2;
                    if (query != null) {
                        query.close();
                    }
                    if (sQLiteDatabase2 != null) {
                        sQLiteDatabase2.close();
                    }
                    throw th;
                }
            } catch (Throwable e22) {
                sQLiteDatabase = writableDatabase;
                th = e22;
                cursor = null;
                com.huawei.hwid.core.c.b.a.d("VIPDatabase", th.toString(), th);
                if (cursor != null) {
                    cursor.close();
                }
                if (sQLiteDatabase != null) {
                    sQLiteDatabase.close();
                }
                return null;
            } catch (Throwable e222) {
                query = null;
                sQLiteDatabase2 = writableDatabase;
                th = e222;
                if (query != null) {
                    query.close();
                }
                if (sQLiteDatabase2 != null) {
                    sQLiteDatabase2.close();
                }
                throw th;
            }
        } catch (Exception e3) {
            th = e3;
            cursor = null;
            sQLiteDatabase = null;
            com.huawei.hwid.core.c.b.a.d("VIPDatabase", th.toString(), th);
            if (cursor != null) {
                cursor.close();
            }
            if (sQLiteDatabase != null) {
                sQLiteDatabase.close();
            }
            return null;
        } catch (Throwable th4) {
            th = th4;
            query = null;
            if (query != null) {
                query.close();
            }
            if (sQLiteDatabase2 != null) {
                sQLiteDatabase2.close();
            }
            throw th;
        }
    }

    private static String b(Context context, String str) {
        String str2 = "";
        Object a = a(context, str);
        if (TextUtils.isEmpty(a)) {
            return str2;
        }
        return e.c(context, a);
    }

    private static boolean a(SQLiteDatabase sQLiteDatabase, String str, String str2) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", str);
        contentValues.put(HwCustHttpUtilsImpl.CHAMELEON_COLUMNS_VALUE, str2);
        if (sQLiteDatabase.insert("vip_config", "", contentValues) <= 0) {
            return false;
        }
        return true;
    }
}
