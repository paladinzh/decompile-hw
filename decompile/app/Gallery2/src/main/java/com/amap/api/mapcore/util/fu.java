package com.amap.api.mapcore.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* compiled from: DBOperation */
public class fu {
    private static Map<Class<? extends ft>, ft> d = new HashMap();
    private fz a;
    private SQLiteDatabase b;
    private ft c;

    public <T> java.util.List<T> a(java.lang.String r13, java.lang.Class<T> r14, boolean r15) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Unreachable block: B:50:0x0073
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.modifyBlocksTree(BlockProcessor.java:248)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:52)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.visit(BlockProcessor.java:38)
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
        r12 = this;
        r8 = 0;
        r9 = r12.c;
        monitor-enter(r9);
        r10 = new java.util.ArrayList;	 Catch:{ all -> 0x0022 }
        r10.<init>();	 Catch:{ all -> 0x0022 }
        r11 = r12.b(r14);	 Catch:{ all -> 0x0022 }
        r1 = r12.a(r11);	 Catch:{ all -> 0x0022 }
        r0 = r12.b;	 Catch:{ all -> 0x0022 }
        if (r0 == 0) goto L_0x001b;	 Catch:{ all -> 0x0022 }
    L_0x0015:
        r0 = r12.b;	 Catch:{ all -> 0x0022 }
        if (r0 != 0) goto L_0x0025;	 Catch:{ all -> 0x0022 }
    L_0x0019:
        monitor-exit(r9);	 Catch:{ all -> 0x0022 }
        return r10;	 Catch:{ all -> 0x0022 }
    L_0x001b:
        r0 = r12.a(r15);	 Catch:{ all -> 0x0022 }
        r12.b = r0;	 Catch:{ all -> 0x0022 }
        goto L_0x0015;	 Catch:{ all -> 0x0022 }
    L_0x0022:
        r0 = move-exception;	 Catch:{ all -> 0x0022 }
        monitor-exit(r9);	 Catch:{ all -> 0x0022 }
        throw r0;
    L_0x0025:
        r0 = android.text.TextUtils.isEmpty(r1);	 Catch:{ all -> 0x0022 }
        if (r0 != 0) goto L_0x0019;
    L_0x002b:
        if (r13 == 0) goto L_0x0019;
    L_0x002d:
        r0 = r12.b;	 Catch:{ Throwable -> 0x0121, all -> 0x011d }
        r2 = 0;	 Catch:{ Throwable -> 0x0121, all -> 0x011d }
        r4 = 0;	 Catch:{ Throwable -> 0x0121, all -> 0x011d }
        r5 = 0;	 Catch:{ Throwable -> 0x0121, all -> 0x011d }
        r6 = 0;	 Catch:{ Throwable -> 0x0121, all -> 0x011d }
        r7 = 0;	 Catch:{ Throwable -> 0x0121, all -> 0x011d }
        r3 = r13;	 Catch:{ Throwable -> 0x0121, all -> 0x011d }
        r1 = r0.query(r1, r2, r3, r4, r5, r6, r7);	 Catch:{ Throwable -> 0x0121, all -> 0x011d }
        if (r1 == 0) goto L_0x0049;
    L_0x003b:
        r0 = r1.moveToNext();	 Catch:{ Throwable -> 0x0061 }
        if (r0 != 0) goto L_0x0059;
    L_0x0041:
        if (r1 != 0) goto L_0x00b9;
    L_0x0043:
        r0 = r12.b;	 Catch:{ all -> 0x0022 }
        if (r0 != 0) goto L_0x00cb;	 Catch:{ all -> 0x0022 }
    L_0x0047:
        monitor-exit(r9);	 Catch:{ all -> 0x0022 }
        return r10;
    L_0x0049:
        r0 = r12.b;	 Catch:{ Throwable -> 0x0061 }
        r0.close();	 Catch:{ Throwable -> 0x0061 }
        r0 = 0;	 Catch:{ Throwable -> 0x0061 }
        r12.b = r0;	 Catch:{ Throwable -> 0x0061 }
        if (r1 != 0) goto L_0x0092;
    L_0x0053:
        r0 = r12.b;	 Catch:{ all -> 0x0022 }
        if (r0 != 0) goto L_0x00a3;	 Catch:{ all -> 0x0022 }
    L_0x0057:
        monitor-exit(r9);	 Catch:{ all -> 0x0022 }
        return r10;
    L_0x0059:
        r0 = r12.a(r1, r14, r11);	 Catch:{ Throwable -> 0x0061 }
        r10.add(r0);	 Catch:{ Throwable -> 0x0061 }
        goto L_0x003b;
    L_0x0061:
        r0 = move-exception;
    L_0x0062:
        if (r15 == 0) goto L_0x0080;
    L_0x0064:
        if (r1 != 0) goto L_0x00e3;
    L_0x0066:
        r0 = r12.b;	 Catch:{ all -> 0x0022 }
        if (r0 == 0) goto L_0x0047;	 Catch:{ all -> 0x0022 }
    L_0x006a:
        r0 = r12.b;	 Catch:{ all -> 0x0022 }
        r0.close();	 Catch:{ all -> 0x0022 }
        r0 = 0;	 Catch:{ all -> 0x0022 }
        r12.b = r0;	 Catch:{ all -> 0x0022 }
        goto L_0x0047;	 Catch:{ all -> 0x0022 }
        r0 = move-exception;	 Catch:{ all -> 0x0022 }
        if (r15 != 0) goto L_0x0047;	 Catch:{ all -> 0x0022 }
    L_0x0076:
        r1 = "DataBase";	 Catch:{ all -> 0x0022 }
        r2 = "searchListData";	 Catch:{ all -> 0x0022 }
        com.amap.api.mapcore.util.fl.a(r0, r1, r2);	 Catch:{ all -> 0x0022 }
        goto L_0x0047;
    L_0x0080:
        r2 = "DataBase";	 Catch:{ all -> 0x008a }
        r3 = "searchListData";	 Catch:{ all -> 0x008a }
        com.amap.api.mapcore.util.fl.a(r0, r2, r3);	 Catch:{ all -> 0x008a }
        goto L_0x0064;
    L_0x008a:
        r0 = move-exception;
    L_0x008b:
        if (r1 != 0) goto L_0x00f5;
    L_0x008d:
        r1 = r12.b;	 Catch:{ all -> 0x0022 }
        if (r1 != 0) goto L_0x0106;	 Catch:{ all -> 0x0022 }
    L_0x0091:
        throw r0;	 Catch:{ all -> 0x0022 }
    L_0x0092:
        r1.close();	 Catch:{ all -> 0x0022 }
        goto L_0x0053;	 Catch:{ all -> 0x0022 }
        r0 = move-exception;	 Catch:{ all -> 0x0022 }
        if (r15 != 0) goto L_0x0053;	 Catch:{ all -> 0x0022 }
    L_0x0099:
        r1 = "DataBase";	 Catch:{ all -> 0x0022 }
        r2 = "searchListData";	 Catch:{ all -> 0x0022 }
        com.amap.api.mapcore.util.fl.a(r0, r1, r2);	 Catch:{ all -> 0x0022 }
        goto L_0x0053;	 Catch:{ all -> 0x0022 }
    L_0x00a3:
        r0 = r12.b;	 Catch:{ all -> 0x0022 }
        r0.close();	 Catch:{ all -> 0x0022 }
        r0 = 0;	 Catch:{ all -> 0x0022 }
        r12.b = r0;	 Catch:{ all -> 0x0022 }
        goto L_0x0057;	 Catch:{ all -> 0x0022 }
        r0 = move-exception;	 Catch:{ all -> 0x0022 }
        if (r15 != 0) goto L_0x0057;	 Catch:{ all -> 0x0022 }
    L_0x00af:
        r1 = "DataBase";	 Catch:{ all -> 0x0022 }
        r2 = "searchListData";	 Catch:{ all -> 0x0022 }
        com.amap.api.mapcore.util.fl.a(r0, r1, r2);	 Catch:{ all -> 0x0022 }
        goto L_0x0057;	 Catch:{ all -> 0x0022 }
    L_0x00b9:
        r1.close();	 Catch:{ all -> 0x0022 }
        goto L_0x0043;	 Catch:{ all -> 0x0022 }
        r0 = move-exception;	 Catch:{ all -> 0x0022 }
        if (r15 != 0) goto L_0x0043;	 Catch:{ all -> 0x0022 }
    L_0x00c0:
        r1 = "DataBase";	 Catch:{ all -> 0x0022 }
        r2 = "searchListData";	 Catch:{ all -> 0x0022 }
        com.amap.api.mapcore.util.fl.a(r0, r1, r2);	 Catch:{ all -> 0x0022 }
        goto L_0x0043;	 Catch:{ all -> 0x0022 }
    L_0x00cb:
        r0 = r12.b;	 Catch:{ all -> 0x0022 }
        r0.close();	 Catch:{ all -> 0x0022 }
        r0 = 0;	 Catch:{ all -> 0x0022 }
        r12.b = r0;	 Catch:{ all -> 0x0022 }
        goto L_0x0047;	 Catch:{ all -> 0x0022 }
        r0 = move-exception;	 Catch:{ all -> 0x0022 }
        if (r15 != 0) goto L_0x0047;	 Catch:{ all -> 0x0022 }
    L_0x00d8:
        r1 = "DataBase";	 Catch:{ all -> 0x0022 }
        r2 = "searchListData";	 Catch:{ all -> 0x0022 }
        com.amap.api.mapcore.util.fl.a(r0, r1, r2);	 Catch:{ all -> 0x0022 }
        goto L_0x0047;	 Catch:{ all -> 0x0022 }
    L_0x00e3:
        r1.close();	 Catch:{ all -> 0x0022 }
        goto L_0x0066;	 Catch:{ all -> 0x0022 }
        r0 = move-exception;	 Catch:{ all -> 0x0022 }
        if (r15 != 0) goto L_0x0066;	 Catch:{ all -> 0x0022 }
    L_0x00ea:
        r1 = "DataBase";	 Catch:{ all -> 0x0022 }
        r2 = "searchListData";	 Catch:{ all -> 0x0022 }
        com.amap.api.mapcore.util.fl.a(r0, r1, r2);	 Catch:{ all -> 0x0022 }
        goto L_0x0066;	 Catch:{ all -> 0x0022 }
    L_0x00f5:
        r1.close();	 Catch:{ all -> 0x0022 }
        goto L_0x008d;	 Catch:{ all -> 0x0022 }
        r1 = move-exception;	 Catch:{ all -> 0x0022 }
        if (r15 != 0) goto L_0x008d;	 Catch:{ all -> 0x0022 }
    L_0x00fc:
        r2 = "DataBase";	 Catch:{ all -> 0x0022 }
        r3 = "searchListData";	 Catch:{ all -> 0x0022 }
        com.amap.api.mapcore.util.fl.a(r1, r2, r3);	 Catch:{ all -> 0x0022 }
        goto L_0x008d;	 Catch:{ all -> 0x0022 }
    L_0x0106:
        r1 = r12.b;	 Catch:{ all -> 0x0022 }
        r1.close();	 Catch:{ all -> 0x0022 }
        r1 = 0;	 Catch:{ all -> 0x0022 }
        r12.b = r1;	 Catch:{ all -> 0x0022 }
        goto L_0x0091;	 Catch:{ all -> 0x0022 }
        r1 = move-exception;	 Catch:{ all -> 0x0022 }
        if (r15 != 0) goto L_0x0091;	 Catch:{ all -> 0x0022 }
    L_0x0112:
        r2 = "DataBase";	 Catch:{ all -> 0x0022 }
        r3 = "searchListData";	 Catch:{ all -> 0x0022 }
        com.amap.api.mapcore.util.fl.a(r1, r2, r3);	 Catch:{ all -> 0x0022 }
        goto L_0x0091;
    L_0x011d:
        r0 = move-exception;
        r1 = r8;
        goto L_0x008b;
    L_0x0121:
        r0 = move-exception;
        r1 = r8;
        goto L_0x0062;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.amap.api.mapcore.util.fu.a(java.lang.String, java.lang.Class, boolean):java.util.List<T>");
    }

    public <T> void a(java.util.List<T> r6) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Unreachable block: B:43:0x0080
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.modifyBlocksTree(BlockProcessor.java:248)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:52)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.visit(BlockProcessor.java:38)
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
        r5 = this;
        r2 = r5.c;
        monitor-enter(r2);
        if (r6 != 0) goto L_0x0007;
    L_0x0005:
        monitor-exit(r2);	 Catch:{ all -> 0x0065 }
        return;	 Catch:{ all -> 0x0065 }
    L_0x0007:
        r0 = r6.size();	 Catch:{ all -> 0x0065 }
        if (r0 == 0) goto L_0x0005;	 Catch:{ all -> 0x0065 }
    L_0x000d:
        r0 = 0;	 Catch:{ all -> 0x0065 }
        r0 = r5.b(r0);	 Catch:{ all -> 0x0065 }
        r5.b = r0;	 Catch:{ all -> 0x0065 }
        r0 = r5.b;	 Catch:{ all -> 0x0065 }
        if (r0 == 0) goto L_0x003e;
    L_0x0018:
        r0 = r5.b;	 Catch:{ Throwable -> 0x004a }
        r0.beginTransaction();	 Catch:{ Throwable -> 0x004a }
        r0 = r6.iterator();	 Catch:{ Throwable -> 0x004a }
    L_0x0021:
        r1 = r0.hasNext();	 Catch:{ Throwable -> 0x004a }
        if (r1 != 0) goto L_0x0040;	 Catch:{ Throwable -> 0x004a }
    L_0x0027:
        r0 = r5.b;	 Catch:{ Throwable -> 0x004a }
        r0.setTransactionSuccessful();	 Catch:{ Throwable -> 0x004a }
        r0 = r5.b;	 Catch:{ all -> 0x0065 }
        r0 = r0.inTransaction();	 Catch:{ all -> 0x0065 }
        if (r0 != 0) goto L_0x007a;	 Catch:{ all -> 0x0065 }
    L_0x0034:
        r0 = r5.b;	 Catch:{ all -> 0x0065 }
        r0.close();	 Catch:{ all -> 0x0065 }
        r0 = 0;	 Catch:{ all -> 0x0065 }
        r5.b = r0;	 Catch:{ all -> 0x0065 }
    L_0x003c:
        monitor-exit(r2);	 Catch:{ all -> 0x0065 }
        return;	 Catch:{ all -> 0x0065 }
    L_0x003e:
        monitor-exit(r2);	 Catch:{ all -> 0x0065 }
        return;
    L_0x0040:
        r1 = r0.next();	 Catch:{ Throwable -> 0x004a }
        r3 = r5.b;	 Catch:{ Throwable -> 0x004a }
        r5.a(r3, r1);	 Catch:{ Throwable -> 0x004a }
        goto L_0x0021;
    L_0x004a:
        r0 = move-exception;
        r1 = "DataBase";	 Catch:{ all -> 0x0068 }
        r3 = "insertListData";	 Catch:{ all -> 0x0068 }
        com.amap.api.mapcore.util.fl.a(r0, r1, r3);	 Catch:{ all -> 0x0068 }
        r0 = r5.b;	 Catch:{ all -> 0x0065 }
        r0 = r0.inTransaction();	 Catch:{ all -> 0x0065 }
        if (r0 != 0) goto L_0x0096;	 Catch:{ all -> 0x0065 }
    L_0x005c:
        r0 = r5.b;	 Catch:{ all -> 0x0065 }
        r0.close();	 Catch:{ all -> 0x0065 }
        r0 = 0;	 Catch:{ all -> 0x0065 }
        r5.b = r0;	 Catch:{ all -> 0x0065 }
        goto L_0x003c;	 Catch:{ all -> 0x0065 }
    L_0x0065:
        r0 = move-exception;	 Catch:{ all -> 0x0065 }
        monitor-exit(r2);	 Catch:{ all -> 0x0065 }
        throw r0;
    L_0x0068:
        r0 = move-exception;
        r1 = r5.b;	 Catch:{ all -> 0x0065 }
        r1 = r1.inTransaction();	 Catch:{ all -> 0x0065 }
        if (r1 != 0) goto L_0x00b2;	 Catch:{ all -> 0x0065 }
    L_0x0071:
        r1 = r5.b;	 Catch:{ all -> 0x0065 }
        r1.close();	 Catch:{ all -> 0x0065 }
        r1 = 0;	 Catch:{ all -> 0x0065 }
        r5.b = r1;	 Catch:{ all -> 0x0065 }
    L_0x0079:
        throw r0;	 Catch:{ all -> 0x0065 }
    L_0x007a:
        r0 = r5.b;	 Catch:{ all -> 0x0065 }
        r0.endTransaction();	 Catch:{ all -> 0x0065 }
        goto L_0x0034;	 Catch:{ all -> 0x0065 }
        r0 = move-exception;	 Catch:{ all -> 0x0065 }
        r1 = "DataBase";	 Catch:{ all -> 0x0065 }
        r3 = "insertListData";	 Catch:{ all -> 0x0065 }
        com.amap.api.mapcore.util.fl.a(r0, r1, r3);	 Catch:{ all -> 0x0065 }
        goto L_0x0034;	 Catch:{ all -> 0x0065 }
        r0 = move-exception;	 Catch:{ all -> 0x0065 }
        r1 = "DataBase";	 Catch:{ all -> 0x0065 }
        r3 = "insertListData";	 Catch:{ all -> 0x0065 }
        com.amap.api.mapcore.util.fl.a(r0, r1, r3);	 Catch:{ all -> 0x0065 }
        goto L_0x003c;	 Catch:{ all -> 0x0065 }
    L_0x0096:
        r0 = r5.b;	 Catch:{ all -> 0x0065 }
        r0.endTransaction();	 Catch:{ all -> 0x0065 }
        goto L_0x005c;	 Catch:{ all -> 0x0065 }
        r0 = move-exception;	 Catch:{ all -> 0x0065 }
        r1 = "DataBase";	 Catch:{ all -> 0x0065 }
        r3 = "insertListData";	 Catch:{ all -> 0x0065 }
        com.amap.api.mapcore.util.fl.a(r0, r1, r3);	 Catch:{ all -> 0x0065 }
        goto L_0x005c;	 Catch:{ all -> 0x0065 }
        r0 = move-exception;	 Catch:{ all -> 0x0065 }
        r1 = "DataBase";	 Catch:{ all -> 0x0065 }
        r3 = "insertListData";	 Catch:{ all -> 0x0065 }
        com.amap.api.mapcore.util.fl.a(r0, r1, r3);	 Catch:{ all -> 0x0065 }
        goto L_0x003c;	 Catch:{ all -> 0x0065 }
    L_0x00b2:
        r1 = r5.b;	 Catch:{ all -> 0x0065 }
        r1.endTransaction();	 Catch:{ all -> 0x0065 }
        goto L_0x0071;	 Catch:{ all -> 0x0065 }
        r1 = move-exception;	 Catch:{ all -> 0x0065 }
        r3 = "DataBase";	 Catch:{ all -> 0x0065 }
        r4 = "insertListData";	 Catch:{ all -> 0x0065 }
        com.amap.api.mapcore.util.fl.a(r1, r3, r4);	 Catch:{ all -> 0x0065 }
        goto L_0x0071;	 Catch:{ all -> 0x0065 }
        r1 = move-exception;	 Catch:{ all -> 0x0065 }
        r3 = "DataBase";	 Catch:{ all -> 0x0065 }
        r4 = "insertListData";	 Catch:{ all -> 0x0065 }
        com.amap.api.mapcore.util.fl.a(r1, r3, r4);	 Catch:{ all -> 0x0065 }
        goto L_0x0079;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.amap.api.mapcore.util.fu.a(java.util.List):void");
    }

    public static synchronized ft a(Class<? extends ft> cls) throws IllegalAccessException, InstantiationException {
        ft ftVar;
        synchronized (fu.class) {
            if (d.get(cls) == null) {
                d.put(cls, cls.newInstance());
            }
            ftVar = (ft) d.get(cls);
        }
        return ftVar;
    }

    public fu(Context context, ft ftVar) {
        try {
            this.a = new fz(context.getApplicationContext(), ftVar.b(), null, ftVar.c(), ftVar);
        } catch (Throwable th) {
            th.printStackTrace();
        }
        this.c = ftVar;
    }

    public static String a(Map<String, String> map) {
        if (map == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        Object obj = 1;
        for (String str : map.keySet()) {
            Object obj2;
            if (obj == null) {
                stringBuilder.append(" and ").append(str).append(" = '").append((String) map.get(str)).append("'");
                obj2 = obj;
            } else {
                stringBuilder.append(str).append(" = '").append((String) map.get(str)).append("'");
                obj2 = null;
            }
            obj = obj2;
        }
        return stringBuilder.toString();
    }

    public <T> void a(String str, Class<T> cls) {
        synchronized (this.c) {
            Object a = a(b((Class) cls));
            if (TextUtils.isEmpty(a)) {
                return;
            }
            this.b = b(false);
            if (this.b != null) {
                try {
                    this.b.delete(a, str, null);
                    if (this.b != null) {
                        this.b.close();
                        this.b = null;
                    }
                } catch (Throwable th) {
                    if (this.b != null) {
                        this.b.close();
                        this.b = null;
                    }
                }
            }
        }
    }

    public <T> void a(String str, Object obj, boolean z) {
        synchronized (this.c) {
            if (obj != null) {
                fv b = b(obj.getClass());
                Object a = a(b);
                if (TextUtils.isEmpty(a)) {
                    return;
                }
                ContentValues a2 = a(obj, b);
                if (a2 != null) {
                    this.b = b(z);
                    if (this.b != null) {
                        try {
                            this.b.update(a, a2, str, null);
                            if (this.b != null) {
                                this.b.close();
                                this.b = null;
                            }
                        } catch (Throwable th) {
                            if (this.b != null) {
                                this.b.close();
                                this.b = null;
                            }
                        }
                    } else {
                        return;
                    }
                }
                return;
            }
        }
    }

    public <T> void a(String str, Object obj) {
        a(str, obj, false);
    }

    public void a(Object obj, String str) {
        synchronized (this.c) {
            List b = b(str, obj.getClass());
            if (b == null || b.size() == 0) {
                a(obj);
            } else {
                a(str, obj);
            }
        }
    }

    public <T> void a(T t) {
        a((Object) t, false);
    }

    public <T> void a(T t, boolean z) {
        synchronized (this.c) {
            this.b = b(z);
            if (this.b != null) {
                try {
                    a(this.b, (Object) t);
                    if (this.b != null) {
                        this.b.close();
                        this.b = null;
                    }
                } catch (Throwable th) {
                    if (this.b != null) {
                        this.b.close();
                        this.b = null;
                    }
                }
            }
        }
    }

    private <T> void a(SQLiteDatabase sQLiteDatabase, T t) {
        fv b = b(t.getClass());
        Object a = a(b);
        if (!TextUtils.isEmpty(a) && t != null && sQLiteDatabase != null) {
            ContentValues a2 = a((Object) t, b);
            if (a2 != null) {
                sQLiteDatabase.insert(a, null, a2);
            }
        }
    }

    public <T> List<T> b(String str, Class<T> cls) {
        return a(str, (Class) cls, false);
    }

    private <T> T a(Cursor cursor, Class<T> cls, fv fvVar) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Field[] a = a((Class) cls, fvVar.b());
        Constructor declaredConstructor = cls.getDeclaredConstructor(new Class[0]);
        declaredConstructor.setAccessible(true);
        T newInstance = declaredConstructor.newInstance(new Object[0]);
        for (Field field : a) {
            field.setAccessible(true);
            Annotation annotation = field.getAnnotation(fw.class);
            if (annotation != null) {
                fw fwVar = (fw) annotation;
                int b = fwVar.b();
                int columnIndex = cursor.getColumnIndex(fwVar.a());
                switch (b) {
                    case 1:
                        field.set(newInstance, Short.valueOf(cursor.getShort(columnIndex)));
                        break;
                    case 2:
                        field.set(newInstance, Integer.valueOf(cursor.getInt(columnIndex)));
                        break;
                    case 3:
                        field.set(newInstance, Float.valueOf(cursor.getFloat(columnIndex)));
                        break;
                    case 4:
                        field.set(newInstance, Double.valueOf(cursor.getDouble(columnIndex)));
                        break;
                    case 5:
                        field.set(newInstance, Long.valueOf(cursor.getLong(columnIndex)));
                        break;
                    case 6:
                        field.set(newInstance, cursor.getString(columnIndex));
                        break;
                    case 7:
                        field.set(newInstance, cursor.getBlob(columnIndex));
                        break;
                    default:
                        break;
                }
            }
        }
        return newInstance;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void a(Object obj, Field field, ContentValues contentValues) {
        Annotation annotation = field.getAnnotation(fw.class);
        if (annotation != null) {
            fw fwVar = (fw) annotation;
            switch (fwVar.b()) {
                case 1:
                    contentValues.put(fwVar.a(), Short.valueOf(field.getShort(obj)));
                    break;
                case 2:
                    try {
                        contentValues.put(fwVar.a(), Integer.valueOf(field.getInt(obj)));
                        break;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        break;
                    }
                case 3:
                    contentValues.put(fwVar.a(), Float.valueOf(field.getFloat(obj)));
                    break;
                case 4:
                    contentValues.put(fwVar.a(), Double.valueOf(field.getDouble(obj)));
                    break;
                case 5:
                    contentValues.put(fwVar.a(), Long.valueOf(field.getLong(obj)));
                    break;
                case 6:
                    String str = "";
                    contentValues.put(fwVar.a(), (String) field.get(obj));
                    break;
                case 7:
                    contentValues.put(fwVar.a(), (byte[]) field.get(obj));
                    break;
            }
        }
    }

    private ContentValues a(Object obj, fv fvVar) {
        ContentValues contentValues = new ContentValues();
        for (Field field : a(obj.getClass(), fvVar.b())) {
            field.setAccessible(true);
            a(obj, field, contentValues);
        }
        return contentValues;
    }

    private Field[] a(Class<?> cls, boolean z) {
        if (cls == null) {
            return null;
        }
        if (z) {
            return cls.getSuperclass().getDeclaredFields();
        }
        return cls.getDeclaredFields();
    }

    private SQLiteDatabase a(boolean z) {
        try {
            if (this.b == null) {
                this.b = this.a.getReadableDatabase();
            }
        } catch (Throwable th) {
            if (z) {
                th.printStackTrace();
            } else {
                fl.a(th, "DBOperation", "getReadAbleDataBase");
            }
        }
        return this.b;
    }

    private SQLiteDatabase b(boolean z) {
        try {
            if (this.b != null) {
                if (!this.b.isReadOnly()) {
                    return this.b;
                }
            }
            if (this.b != null) {
                this.b.close();
            }
            this.b = this.a.getWritableDatabase();
        } catch (Throwable th) {
            fl.a(th, "DBOperation", "getWriteDatabase");
        }
        return this.b;
    }

    private boolean a(Annotation annotation) {
        return annotation != null;
    }

    private <T> String a(fv fvVar) {
        if (fvVar != null) {
            return fvVar.a();
        }
        return null;
    }

    private <T> fv b(Class<T> cls) {
        Annotation annotation = cls.getAnnotation(fv.class);
        if (a(annotation)) {
            return (fv) annotation;
        }
        return null;
    }
}
