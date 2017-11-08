package com.huawei.systemmanager.optimize.process;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.text.TextUtils;
import com.huawei.systemmanager.optimize.base.Const;
import com.huawei.systemmanager.optimize.smcs.SMCSDatabaseConstant;
import com.huawei.systemmanager.rainbow.db.CloudDBAdapter;
import com.huawei.systemmanager.rainbow.db.bean.BackgroundConfigBean;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SmcsDbHelper {
    private static final String SMCS_AUTHORITY_URI = "content://smcs/";
    private static final Uri SMCS_DEFAULT_VALUE_URI = Uri.parse("content://smcs/default_value_table");
    public static final Uri SMCS_PROTECT_TABLE_URI = Uri.parse("content://smcs/st_protected_pkgs_table");
    private static final Uri SMCS_URI = Uri.parse("content://smcs/");
    private static final String TAG = "SmcsDbHelper";
    public static final int VALUE_FALE = 1;
    public static final int VALUE_NULL = 0;
    public static final int VALUE_TRUE = 2;

    public static java.util.Map<java.lang.String, java.lang.Boolean> getAllDefaultControledMap(android.content.Context r15) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x007e in list []
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
        r14 = 2;
        r13 = com.huawei.systemmanager.comm.collections.HsmCollections.newArrayMap();
        r0 = com.huawei.systemmanager.rainbow.db.CloudDBAdapter.getInstance(r15);
        r8 = r0.getAllBackgroundConfig();
        r0 = com.huawei.systemmanager.comm.misc.Utility.isNullOrEmptyList(r8);
        if (r0 != 0) goto L_0x0033;
    L_0x0013:
        r7 = r8.iterator();
    L_0x0017:
        r0 = r7.hasNext();
        if (r0 == 0) goto L_0x0033;
    L_0x001d:
        r6 = r7.next();
        r6 = (com.huawei.systemmanager.rainbow.db.bean.BackgroundConfigBean) r6;
        r0 = r6.getPkgName();
        r1 = r6.isControlled();
        r1 = java.lang.Boolean.valueOf(r1);
        r13.put(r0, r1);
        goto L_0x0017;
    L_0x0033:
        r10 = 0;
        r0 = r15.getContentResolver();	 Catch:{ Exception -> 0x0075, all -> 0x0087 }
        r1 = SMCS_DEFAULT_VALUE_URI;	 Catch:{ Exception -> 0x0075, all -> 0x0087 }
        r2 = 2;	 Catch:{ Exception -> 0x0075, all -> 0x0087 }
        r2 = new java.lang.String[r2];	 Catch:{ Exception -> 0x0075, all -> 0x0087 }
        r3 = "pkg_name";	 Catch:{ Exception -> 0x0075, all -> 0x0087 }
        r4 = 0;	 Catch:{ Exception -> 0x0075, all -> 0x0087 }
        r2[r4] = r3;	 Catch:{ Exception -> 0x0075, all -> 0x0087 }
        r3 = "control";	 Catch:{ Exception -> 0x0075, all -> 0x0087 }
        r4 = 1;	 Catch:{ Exception -> 0x0075, all -> 0x0087 }
        r2[r4] = r3;	 Catch:{ Exception -> 0x0075, all -> 0x0087 }
        r3 = "control!=0";	 Catch:{ Exception -> 0x0075, all -> 0x0087 }
        r4 = 0;	 Catch:{ Exception -> 0x0075, all -> 0x0087 }
        r5 = 0;	 Catch:{ Exception -> 0x0075, all -> 0x0087 }
        r10 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x0075, all -> 0x0087 }
        if (r10 != 0) goto L_0x005a;
    L_0x0054:
        if (r10 == 0) goto L_0x0059;
    L_0x0056:
        r10.close();
    L_0x0059:
        return r13;
    L_0x005a:
        r0 = r10.moveToNext();	 Catch:{ Exception -> 0x0075, all -> 0x0087 }
        if (r0 == 0) goto L_0x0081;	 Catch:{ Exception -> 0x0075, all -> 0x0087 }
    L_0x0060:
        r0 = 0;	 Catch:{ Exception -> 0x0075, all -> 0x0087 }
        r12 = r10.getString(r0);	 Catch:{ Exception -> 0x0075, all -> 0x0087 }
        r0 = 1;	 Catch:{ Exception -> 0x0075, all -> 0x0087 }
        r0 = r10.getInt(r0);	 Catch:{ Exception -> 0x0075, all -> 0x0087 }
        if (r0 != r14) goto L_0x007f;	 Catch:{ Exception -> 0x0075, all -> 0x0087 }
    L_0x006c:
        r9 = 1;	 Catch:{ Exception -> 0x0075, all -> 0x0087 }
    L_0x006d:
        r0 = java.lang.Boolean.valueOf(r9);	 Catch:{ Exception -> 0x0075, all -> 0x0087 }
        r13.put(r12, r0);	 Catch:{ Exception -> 0x0075, all -> 0x0087 }
        goto L_0x005a;
    L_0x0075:
        r11 = move-exception;
        r11.printStackTrace();	 Catch:{ Exception -> 0x0075, all -> 0x0087 }
        if (r10 == 0) goto L_0x007e;
    L_0x007b:
        r10.close();
    L_0x007e:
        return r13;
    L_0x007f:
        r9 = 0;
        goto L_0x006d;
    L_0x0081:
        if (r10 == 0) goto L_0x007e;
    L_0x0083:
        r10.close();
        goto L_0x007e;
    L_0x0087:
        r0 = move-exception;
        if (r10 == 0) goto L_0x008d;
    L_0x008a:
        r10.close();
    L_0x008d:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.systemmanager.optimize.process.SmcsDbHelper.getAllDefaultControledMap(android.content.Context):java.util.Map<java.lang.String, java.lang.Boolean>");
    }

    public static java.util.HashMap<java.lang.String, java.lang.Boolean> getDefaultKeyTaskPkgs(android.content.Context r12) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0056 in list []
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
        r11 = 2;
        r10 = com.google.android.collect.Maps.newHashMap();
        r6 = 0;
        r1 = SMCS_DEFAULT_VALUE_URI;	 Catch:{ Exception -> 0x0047, all -> 0x005f }
        r0 = r12.getContentResolver();	 Catch:{ Exception -> 0x0047, all -> 0x005f }
        r2 = 2;	 Catch:{ Exception -> 0x0047, all -> 0x005f }
        r2 = new java.lang.String[r2];	 Catch:{ Exception -> 0x0047, all -> 0x005f }
        r3 = "pkg_name";	 Catch:{ Exception -> 0x0047, all -> 0x005f }
        r4 = 0;	 Catch:{ Exception -> 0x0047, all -> 0x005f }
        r2[r4] = r3;	 Catch:{ Exception -> 0x0047, all -> 0x005f }
        r3 = "keytask";	 Catch:{ Exception -> 0x0047, all -> 0x005f }
        r4 = 1;	 Catch:{ Exception -> 0x0047, all -> 0x005f }
        r2[r4] = r3;	 Catch:{ Exception -> 0x0047, all -> 0x005f }
        r3 = "keytask!=0";	 Catch:{ Exception -> 0x0047, all -> 0x005f }
        r4 = 0;	 Catch:{ Exception -> 0x0047, all -> 0x005f }
        r5 = 0;	 Catch:{ Exception -> 0x0047, all -> 0x005f }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x0047, all -> 0x005f }
        if (r6 != 0) goto L_0x002c;
    L_0x0026:
        if (r6 == 0) goto L_0x002b;
    L_0x0028:
        r6.close();
    L_0x002b:
        return r10;
    L_0x002c:
        r0 = r6.moveToNext();	 Catch:{ Exception -> 0x0047, all -> 0x005f }
        if (r0 == 0) goto L_0x0059;	 Catch:{ Exception -> 0x0047, all -> 0x005f }
    L_0x0032:
        r0 = 0;	 Catch:{ Exception -> 0x0047, all -> 0x005f }
        r9 = r6.getString(r0);	 Catch:{ Exception -> 0x0047, all -> 0x005f }
        r0 = 1;	 Catch:{ Exception -> 0x0047, all -> 0x005f }
        r0 = r6.getInt(r0);	 Catch:{ Exception -> 0x0047, all -> 0x005f }
        if (r0 != r11) goto L_0x0057;	 Catch:{ Exception -> 0x0047, all -> 0x005f }
    L_0x003e:
        r8 = 1;	 Catch:{ Exception -> 0x0047, all -> 0x005f }
    L_0x003f:
        r0 = java.lang.Boolean.valueOf(r8);	 Catch:{ Exception -> 0x0047, all -> 0x005f }
        r10.put(r9, r0);	 Catch:{ Exception -> 0x0047, all -> 0x005f }
        goto L_0x002c;
    L_0x0047:
        r7 = move-exception;
        r0 = "SmcsDbHelper";	 Catch:{ Exception -> 0x0047, all -> 0x005f }
        r2 = "getKeyTaskPackages error ";	 Catch:{ Exception -> 0x0047, all -> 0x005f }
        com.huawei.systemmanager.util.HwLog.e(r0, r2, r7);	 Catch:{ Exception -> 0x0047, all -> 0x005f }
        if (r6 == 0) goto L_0x0056;
    L_0x0053:
        r6.close();
    L_0x0056:
        return r10;
    L_0x0057:
        r8 = 0;
        goto L_0x003f;
    L_0x0059:
        if (r6 == 0) goto L_0x0056;
    L_0x005b:
        r6.close();
        goto L_0x0056;
    L_0x005f:
        r0 = move-exception;
        if (r6 == 0) goto L_0x0065;
    L_0x0062:
        r6.close();
    L_0x0065:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.systemmanager.optimize.process.SmcsDbHelper.getDefaultKeyTaskPkgs(android.content.Context):java.util.HashMap<java.lang.String, java.lang.Boolean>");
    }

    public static java.util.ArrayList<java.lang.String> getProtectList(android.content.Context r11, boolean r12) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0053 in list []
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
        r10 = com.google.common.collect.Lists.newArrayList();
        r6 = 0;
        if (r12 == 0) goto L_0x0039;
    L_0x0007:
        r9 = 1;
    L_0x0008:
        r0 = r11.getContentResolver();	 Catch:{ Exception -> 0x004a, all -> 0x005a }
        r1 = SMCS_PROTECT_TABLE_URI;	 Catch:{ Exception -> 0x004a, all -> 0x005a }
        r2 = 1;	 Catch:{ Exception -> 0x004a, all -> 0x005a }
        r2 = new java.lang.String[r2];	 Catch:{ Exception -> 0x004a, all -> 0x005a }
        r3 = "pkg_name";	 Catch:{ Exception -> 0x004a, all -> 0x005a }
        r4 = 0;	 Catch:{ Exception -> 0x004a, all -> 0x005a }
        r2[r4] = r3;	 Catch:{ Exception -> 0x004a, all -> 0x005a }
        r3 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x004a, all -> 0x005a }
        r3.<init>();	 Catch:{ Exception -> 0x004a, all -> 0x005a }
        r4 = "is_checked=";	 Catch:{ Exception -> 0x004a, all -> 0x005a }
        r3 = r3.append(r4);	 Catch:{ Exception -> 0x004a, all -> 0x005a }
        r3 = r3.append(r9);	 Catch:{ Exception -> 0x004a, all -> 0x005a }
        r3 = r3.toString();	 Catch:{ Exception -> 0x004a, all -> 0x005a }
        r4 = 0;	 Catch:{ Exception -> 0x004a, all -> 0x005a }
        r5 = 0;	 Catch:{ Exception -> 0x004a, all -> 0x005a }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x004a, all -> 0x005a }
        if (r6 != 0) goto L_0x003b;
    L_0x0033:
        if (r6 == 0) goto L_0x0038;
    L_0x0035:
        r6.close();
    L_0x0038:
        return r10;
    L_0x0039:
        r9 = 0;
        goto L_0x0008;
    L_0x003b:
        r0 = r6.moveToNext();	 Catch:{ Exception -> 0x004a, all -> 0x005a }
        if (r0 == 0) goto L_0x0054;	 Catch:{ Exception -> 0x004a, all -> 0x005a }
    L_0x0041:
        r0 = 0;	 Catch:{ Exception -> 0x004a, all -> 0x005a }
        r8 = r6.getString(r0);	 Catch:{ Exception -> 0x004a, all -> 0x005a }
        r10.add(r8);	 Catch:{ Exception -> 0x004a, all -> 0x005a }
        goto L_0x003b;
    L_0x004a:
        r7 = move-exception;
        r7.printStackTrace();	 Catch:{ Exception -> 0x004a, all -> 0x005a }
        if (r6 == 0) goto L_0x0053;
    L_0x0050:
        r6.close();
    L_0x0053:
        return r10;
    L_0x0054:
        if (r6 == 0) goto L_0x0053;
    L_0x0056:
        r6.close();
        goto L_0x0053;
    L_0x005a:
        r0 = move-exception;
        if (r6 == 0) goto L_0x0060;
    L_0x005d:
        r6.close();
    L_0x0060:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.systemmanager.optimize.process.SmcsDbHelper.getProtectList(android.content.Context, boolean):java.util.ArrayList<java.lang.String>");
    }

    public static java.util.Map<java.lang.String, java.lang.Boolean> getRecordProtectAppFromDb(android.content.Context r12, java.util.Map<java.lang.String, java.lang.Boolean> r13) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0051 in list []
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
        r11 = 1;
        r10 = com.huawei.systemmanager.comm.collections.HsmCollections.newArrayMap();
        if (r13 == 0) goto L_0x0008;
    L_0x0007:
        r10 = r13;
    L_0x0008:
        r6 = 0;
        r0 = r12.getContentResolver();	 Catch:{ Exception -> 0x0048, all -> 0x005a }
        r1 = SMCS_PROTECT_TABLE_URI;	 Catch:{ Exception -> 0x0048, all -> 0x005a }
        r2 = 2;	 Catch:{ Exception -> 0x0048, all -> 0x005a }
        r2 = new java.lang.String[r2];	 Catch:{ Exception -> 0x0048, all -> 0x005a }
        r3 = "pkg_name";	 Catch:{ Exception -> 0x0048, all -> 0x005a }
        r4 = 0;	 Catch:{ Exception -> 0x0048, all -> 0x005a }
        r2[r4] = r3;	 Catch:{ Exception -> 0x0048, all -> 0x005a }
        r3 = "is_checked";	 Catch:{ Exception -> 0x0048, all -> 0x005a }
        r4 = 1;	 Catch:{ Exception -> 0x0048, all -> 0x005a }
        r2[r4] = r3;	 Catch:{ Exception -> 0x0048, all -> 0x005a }
        r3 = 0;	 Catch:{ Exception -> 0x0048, all -> 0x005a }
        r4 = 0;	 Catch:{ Exception -> 0x0048, all -> 0x005a }
        r5 = 0;	 Catch:{ Exception -> 0x0048, all -> 0x005a }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x0048, all -> 0x005a }
        if (r6 != 0) goto L_0x002d;
    L_0x0027:
        if (r6 == 0) goto L_0x002c;
    L_0x0029:
        r6.close();
    L_0x002c:
        return r10;
    L_0x002d:
        r0 = r6.moveToNext();	 Catch:{ Exception -> 0x0048, all -> 0x005a }
        if (r0 == 0) goto L_0x0054;	 Catch:{ Exception -> 0x0048, all -> 0x005a }
    L_0x0033:
        r0 = 0;	 Catch:{ Exception -> 0x0048, all -> 0x005a }
        r8 = r6.getString(r0);	 Catch:{ Exception -> 0x0048, all -> 0x005a }
        r0 = 1;	 Catch:{ Exception -> 0x0048, all -> 0x005a }
        r0 = r6.getInt(r0);	 Catch:{ Exception -> 0x0048, all -> 0x005a }
        if (r0 != r11) goto L_0x0052;	 Catch:{ Exception -> 0x0048, all -> 0x005a }
    L_0x003f:
        r9 = 1;	 Catch:{ Exception -> 0x0048, all -> 0x005a }
    L_0x0040:
        r0 = java.lang.Boolean.valueOf(r9);	 Catch:{ Exception -> 0x0048, all -> 0x005a }
        r10.put(r8, r0);	 Catch:{ Exception -> 0x0048, all -> 0x005a }
        goto L_0x002d;
    L_0x0048:
        r7 = move-exception;
        r7.printStackTrace();	 Catch:{ Exception -> 0x0048, all -> 0x005a }
        if (r6 == 0) goto L_0x0051;
    L_0x004e:
        r6.close();
    L_0x0051:
        return r10;
    L_0x0052:
        r9 = 0;
        goto L_0x0040;
    L_0x0054:
        if (r6 == 0) goto L_0x0051;
    L_0x0056:
        r6.close();
        goto L_0x0051;
    L_0x005a:
        r0 = move-exception;
        if (r6 == 0) goto L_0x0060;
    L_0x005d:
        r6.close();
    L_0x0060:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.systemmanager.optimize.process.SmcsDbHelper.getRecordProtectAppFromDb(android.content.Context, java.util.Map):java.util.Map<java.lang.String, java.lang.Boolean>");
    }

    public static java.util.Map<java.lang.String, java.lang.Boolean> getRecordProtectAppNotChangeByUsrFromDb(android.content.Context r13) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x005a in list []
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
        r12 = 1;
        r10 = com.huawei.systemmanager.comm.collections.HsmCollections.newArrayMap();
        r6 = 0;
        r0 = r13.getContentResolver();	 Catch:{ Exception -> 0x0051, all -> 0x0063 }
        r1 = SMCS_PROTECT_TABLE_URI;	 Catch:{ Exception -> 0x0051, all -> 0x0063 }
        r2 = 2;	 Catch:{ Exception -> 0x0051, all -> 0x0063 }
        r2 = new java.lang.String[r2];	 Catch:{ Exception -> 0x0051, all -> 0x0063 }
        r3 = "pkg_name";	 Catch:{ Exception -> 0x0051, all -> 0x0063 }
        r4 = 0;	 Catch:{ Exception -> 0x0051, all -> 0x0063 }
        r2[r4] = r3;	 Catch:{ Exception -> 0x0051, all -> 0x0063 }
        r3 = "is_checked";	 Catch:{ Exception -> 0x0051, all -> 0x0063 }
        r4 = 1;	 Catch:{ Exception -> 0x0051, all -> 0x0063 }
        r2[r4] = r3;	 Catch:{ Exception -> 0x0051, all -> 0x0063 }
        r3 = "userchanged=?";	 Catch:{ Exception -> 0x0051, all -> 0x0063 }
        r4 = 1;	 Catch:{ Exception -> 0x0051, all -> 0x0063 }
        r4 = new java.lang.String[r4];	 Catch:{ Exception -> 0x0051, all -> 0x0063 }
        r5 = 0;	 Catch:{ Exception -> 0x0051, all -> 0x0063 }
        r5 = java.lang.String.valueOf(r5);	 Catch:{ Exception -> 0x0051, all -> 0x0063 }
        r11 = 0;	 Catch:{ Exception -> 0x0051, all -> 0x0063 }
        r4[r11] = r5;	 Catch:{ Exception -> 0x0051, all -> 0x0063 }
        r5 = 0;	 Catch:{ Exception -> 0x0051, all -> 0x0063 }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x0051, all -> 0x0063 }
        if (r6 != 0) goto L_0x0036;
    L_0x0030:
        if (r6 == 0) goto L_0x0035;
    L_0x0032:
        r6.close();
    L_0x0035:
        return r10;
    L_0x0036:
        r0 = r6.moveToNext();	 Catch:{ Exception -> 0x0051, all -> 0x0063 }
        if (r0 == 0) goto L_0x005d;	 Catch:{ Exception -> 0x0051, all -> 0x0063 }
    L_0x003c:
        r0 = 0;	 Catch:{ Exception -> 0x0051, all -> 0x0063 }
        r8 = r6.getString(r0);	 Catch:{ Exception -> 0x0051, all -> 0x0063 }
        r0 = 1;	 Catch:{ Exception -> 0x0051, all -> 0x0063 }
        r0 = r6.getInt(r0);	 Catch:{ Exception -> 0x0051, all -> 0x0063 }
        if (r0 != r12) goto L_0x005b;	 Catch:{ Exception -> 0x0051, all -> 0x0063 }
    L_0x0048:
        r9 = 1;	 Catch:{ Exception -> 0x0051, all -> 0x0063 }
    L_0x0049:
        r0 = java.lang.Boolean.valueOf(r9);	 Catch:{ Exception -> 0x0051, all -> 0x0063 }
        r10.put(r8, r0);	 Catch:{ Exception -> 0x0051, all -> 0x0063 }
        goto L_0x0036;
    L_0x0051:
        r7 = move-exception;
        r7.printStackTrace();	 Catch:{ Exception -> 0x0051, all -> 0x0063 }
        if (r6 == 0) goto L_0x005a;
    L_0x0057:
        r6.close();
    L_0x005a:
        return r10;
    L_0x005b:
        r9 = 0;
        goto L_0x0049;
    L_0x005d:
        if (r6 == 0) goto L_0x005a;
    L_0x005f:
        r6.close();
        goto L_0x005a;
    L_0x0063:
        r0 = move-exception;
        if (r6 == 0) goto L_0x0069;
    L_0x0066:
        r6.close();
    L_0x0069:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.systemmanager.optimize.process.SmcsDbHelper.getRecordProtectAppNotChangeByUsrFromDb(android.content.Context):java.util.Map<java.lang.String, java.lang.Boolean>");
    }

    public static void addProtectRecordToDb(String packageName, int checked, Context context) {
        ContentValues values = new ContentValues();
        values.put("pkg_name", packageName);
        values.put(SMCSDatabaseConstant.ST_PROTECTED_PKG_CHECK, Integer.valueOf(checked));
        context.getContentResolver().insert(SMCS_PROTECT_TABLE_URI, values);
    }

    public static void updateProtectAppForDB(String packageName, int isWhite, Context context) {
        Uri uri = SMCS_PROTECT_TABLE_URI;
        ContentValues values = new ContentValues();
        values.put(SMCSDatabaseConstant.ST_PROTECTED_PKG_CHECK, Integer.valueOf(isWhite));
        values.put("userchanged", Integer.valueOf(1));
        context.getContentResolver().update(uri, values, "pkg_name = ? ", new String[]{packageName});
    }

    public static void updateProtectAppListForDB(ArrayList<String> list, int isWhite, Context context) {
        Uri uri = SMCS_PROTECT_TABLE_URI;
        ArrayList<ContentProviderOperation> ops = new ArrayList(list.size());
        String selection = "pkg_name = ? ";
        for (String pkg : list) {
            ContentValues values = new ContentValues();
            values.put(SMCSDatabaseConstant.ST_PROTECTED_PKG_CHECK, Integer.valueOf(isWhite));
            values.put("userchanged", Integer.valueOf(1));
            Builder buider = ContentProviderOperation.newUpdate(uri);
            buider.withValues(values);
            buider.withSelection(selection, new String[]{pkg});
            ops.add(buider.build());
        }
        try {
            context.getContentResolver().applyBatch(Const.SMCS_URI_AUTH, ops);
            HwLog.i(TAG, "bulk updateProtectAppListForDB " + list.size() + ", type:" + isWhite);
            context.getContentResolver().notifyChange(uri, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e2) {
            e2.printStackTrace();
        }
    }

    public static void updateProtectAppFromCloud(Context context, Map<String, Boolean> mapChangedConfig) {
        if (mapChangedConfig == null || mapChangedConfig.isEmpty()) {
            HwLog.w(TAG, "updateProtectAppFromCloud: Invalid configs");
            return;
        }
        Uri uri = SMCS_PROTECT_TABLE_URI;
        for (Entry<String, Boolean> entry : mapChangedConfig.entrySet()) {
            ContentValues values = new ContentValues();
            values.put(SMCSDatabaseConstant.ST_PROTECTED_PKG_CHECK, (Boolean) entry.getValue());
            context.getContentResolver().update(uri, values, "pkg_name = ? ", new String[]{(String) entry.getKey()});
        }
    }

    public static void deleteProtectRecordFromDb(String packageName, Context context) {
        context.getContentResolver().delete(SMCS_PROTECT_TABLE_URI, "pkg_name = ? ", new String[]{packageName});
    }

    public static ArrayList<String> getListFromDB(ArrayList<String> list, int iswhite, Context context) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(SMCS_PROTECT_TABLE_URI, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    if (cursor.getInt(1) == iswhite) {
                        list.add(cursor.getString(0));
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return list;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static ArrayList<String> getAllControlled(Context context) {
        Cursor cursor = null;
        ArrayList<String> list = new ArrayList();
        try {
            cursor = context.getContentResolver().query(SMCS_PROTECT_TABLE_URI, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    list.add(cursor.getString(0));
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return list;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static int getAppProtectStateFromDb(Context ctx, String pkg) {
        HwLog.i(TAG, "getAppProtectStateFromDb called, pkg is:" + pkg);
        if (TextUtils.isEmpty(pkg)) {
            return 0;
        }
        Cursor cursor = null;
        try {
            cursor = ctx.getContentResolver().query(SMCS_PROTECT_TABLE_URI, new String[]{SMCSDatabaseConstant.ST_PROTECTED_PKG_CHECK}, "pkg_name=?", new String[]{pkg}, null);
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return 0;
            } else if (!cursor.moveToNext()) {
                if (cursor != null) {
                    cursor.close();
                }
                return 0;
            } else if (cursor.getInt(0) == 1) {
                if (cursor != null) {
                    cursor.close();
                }
                return 2;
            } else {
                if (cursor != null) {
                    cursor.close();
                }
                return 1;
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
    }

    public static Set<String> getProtectMap(Context context, boolean protect) {
        ArrayList<String> protectList = getProtectList(context, protect);
        HashSet<String> result = new HashSet(protectList.size());
        for (String pkg : protectList) {
            result.add(pkg);
        }
        return result;
    }

    public static void refreshDefaultValueTable(Context context) {
        context.getContentResolver().call(SMCS_URI, SMCSDatabaseConstant.METHOD_INIT_DEFAULT_VALUE_TABLE, null, null);
    }

    public static int getSingleControlledState(Context ctx, String pkg) {
        Cursor cursor = null;
        try {
            cursor = ctx.getContentResolver().query(SMCS_DEFAULT_VALUE_URI, new String[]{SMCSDatabaseConstant.COLUMN_CONTROLL}, "pkg_name=?", new String[]{pkg}, null);
            if (cursor == null || !cursor.moveToNext()) {
                if (cursor != null) {
                    cursor.close();
                }
                BackgroundConfigBean cloudBean = CloudDBAdapter.getInstance(ctx).getSingleBackgroundConfig(pkg);
                if (cloudBean == null) {
                    return 0;
                }
                int i;
                if (cloudBean.isControlled()) {
                    i = 2;
                } else {
                    i = 1;
                }
                return i;
            }
            int controlStat = cursor.getInt(0);
            if (cursor != null) {
                cursor.close();
            }
            return controlStat;
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
    }

    public static int getSinlgeProtectState(Context ctx, String pkg) {
        Cursor cursor = null;
        try {
            cursor = ctx.getContentResolver().query(SMCS_DEFAULT_VALUE_URI, new String[]{"protect"}, "pkg_name= ?", new String[]{pkg}, null);
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return 0;
            } else if (cursor.moveToNext()) {
                int checkState = cursor.getInt(0);
                if (cursor != null) {
                    cursor.close();
                }
                return checkState;
            } else {
                if (cursor != null) {
                    cursor.close();
                }
                return 0;
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
    }

    public static Cursor getPkgChecked(Context ctx, String pkg) {
        return ctx.getContentResolver().query(SMCS_DEFAULT_VALUE_URI, new String[]{"pkg_name", "protect"}, "pkg_name= ? AND protect=2", new String[]{pkg}, null);
    }
}
