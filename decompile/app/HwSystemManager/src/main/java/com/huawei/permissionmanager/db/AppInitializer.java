package com.huawei.permissionmanager.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.util.Log;
import com.huawei.permission.MPermissionUtil;
import com.huawei.permissionmanager.model.AppPermissionGroup;
import com.huawei.permissionmanager.model.AppPermissions;
import com.huawei.permissionmanager.model.HwAppPermissions;
import com.huawei.permissionmanager.model.HwAppPermissions.SysAllow2Hsm;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.addviewmonitor.AddViewAppManager;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.customize.CustomizeWrapper;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class AppInitializer {
    private static final String LOG_TAG = "AppInitializer";

    private static void changeContentValues(android.content.ContentValues r1, int r2, int r3) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.permissionmanager.db.AppInitializer.changeContentValues(android.content.ContentValues, int, int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.permissionmanager.db.AppInitializer.changeContentValues(android.content.ContentValues, int, int):void");
    }

    public static boolean updateReplaceApp(android.content.Context r19, java.lang.String r20, int r21) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0184 in list []
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
        if (r19 != 0) goto L_0x000d;
    L_0x0002:
        r3 = "AppInitializer";
        r4 = "context null in updateReplaceApp.";
        com.huawei.systemmanager.util.HwLog.w(r3, r4);
        r3 = 0;
        return r3;
    L_0x000d:
        r9 = com.huawei.permissionmanager.model.HwAppPermissions.create(r19, r20);
        r10 = 0;
        r10 = com.huawei.permissionmanager.db.DBAdapter.queryOneDataByPkgName(r19, r20);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        if (r10 == 0) goto L_0x00dc;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
    L_0x0018:
        r3 = r10.getCount();	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        if (r3 <= 0) goto L_0x0143;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
    L_0x001e:
        r7 = 0;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r6 = 0;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r17 = 0;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r10.moveToFirst();	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r3 = "trust";	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r18 = r10.getColumnIndex(r3);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r0 = r18;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r17 = r10.getInt(r0);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r3 = 1;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r0 = r17;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        if (r0 != r3) goto L_0x00e3;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
    L_0x0037:
        r8 = 1;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
    L_0x0038:
        r0 = r19;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r1 = r20;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r2 = r21;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r8 = com.huawei.permissionmanager.utils.CommonFunctionUtil.checkAppTrustStatus(r0, r1, r2, r9, r8);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r3 = "permissionCode";	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r15 = r10.getColumnIndex(r3);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r6 = r10.getInt(r15);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r11 = com.huawei.permissionmanager.db.AppInfo.getComparePermissionCode(r19, r20);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r6 = r6 & r11;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r3 = "permissionCfg";	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r14 = r10.getColumnIndex(r3);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r7 = r10.getInt(r14);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r7 = r7 & r11;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r3 = "AppInitializer";	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = new java.lang.StringBuilder;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4.<init>();	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r5 = "updateReplaceApp for ";	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.append(r5);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r0 = r20;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.append(r0);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r5 = ", trust:";	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.append(r5);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r0 = r17;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.append(r0);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r5 = ", real trust:";	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.append(r5);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.append(r8);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r5 = ", code:";	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.append(r5);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.append(r6);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r5 = ", cfg:";	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.append(r5);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.append(r7);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r5 = ", mask:";	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.append(r5);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.append(r11);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.toString();	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        com.huawei.systemmanager.util.HwLog.i(r3, r4);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        if (r8 == 0) goto L_0x00e6;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
    L_0x00b5:
        r3 = "AppInitializer";	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = new java.lang.StringBuilder;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4.<init>();	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r5 = "at init, trust app:";	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.append(r5);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r0 = r20;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.append(r0);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.toString();	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        com.huawei.systemmanager.util.HwLog.i(r3, r4);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r6 = r11;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r7 = 0;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
    L_0x00d3:
        r3 = r19;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r21;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r5 = r20;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        com.huawei.permissionmanager.db.DBAdapter.updateAppPermission(r3, r4, r5, r6, r7, r8);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
    L_0x00dc:
        if (r10 == 0) goto L_0x00e1;
    L_0x00de:
        r10.close();
    L_0x00e1:
        r3 = 1;
        return r3;
    L_0x00e3:
        r8 = 0;
        goto L_0x0038;
    L_0x00e6:
        r0 = r19;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r1 = r20;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r16 = com.huawei.systemmanager.rainbow.db.CloudDBAdapter.applyDefaultPolicy(r0, r11, r1, r6, r7);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r3 = "AppInitializer";	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = new java.lang.StringBuilder;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4.<init>();	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r5 = "updateReplaceApp for ";	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.append(r5);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r0 = r20;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.append(r0);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r5 = ", previous:";	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.append(r5);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.append(r6);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r5 = ", ";	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.append(r5);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.append(r7);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r5 = ", after:";	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.append(r5);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r5 = 0;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r5 = r16[r5];	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.append(r5);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r5 = ",";	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.append(r5);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r5 = 1;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r5 = r16[r5];	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.append(r5);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.toString();	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        com.huawei.systemmanager.util.HwLog.i(r3, r4);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r3 = 0;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r6 = r16[r3];	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r3 = 1;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r7 = r16[r3];	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        goto L_0x00d3;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
    L_0x0143:
        r3 = "AppInitializer";	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = new java.lang.StringBuilder;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4.<init>();	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r5 = "restorePermissionFromCloud, pkg: ";	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.append(r5);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r0 = r20;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.append(r0);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r5 = " does not exist in db";	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.append(r5);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = r4.toString();	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        com.huawei.systemmanager.util.HwLog.d(r3, r4);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r3 = "update";	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r0 = r19;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r1 = r20;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r2 = r21;	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        initilizeNewAppAndSyncToSys(r0, r1, r2, r3);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        goto L_0x00dc;
    L_0x0174:
        r13 = move-exception;
        r3 = "AppInitializer";	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = "updateReplaceApp permission  fail.";	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        com.huawei.systemmanager.util.HwLog.e(r3, r4, r13);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r3 = 0;
        if (r10 == 0) goto L_0x0184;
    L_0x0181:
        r10.close();
    L_0x0184:
        return r3;
    L_0x0185:
        r12 = move-exception;
        r3 = "AppInitializer";	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r4 = "updateReplaceApp permission from cloud fail.";	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        com.huawei.systemmanager.util.HwLog.e(r3, r4, r12);	 Catch:{ NullPointerException -> 0x0174, Exception -> 0x0185, all -> 0x0196 }
        r3 = 0;
        if (r10 == 0) goto L_0x0195;
    L_0x0192:
        r10.close();
    L_0x0195:
        return r3;
    L_0x0196:
        r3 = move-exception;
        if (r10 == 0) goto L_0x019c;
    L_0x0199:
        r10.close();
    L_0x019c:
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.permissionmanager.db.AppInitializer.updateReplaceApp(android.content.Context, java.lang.String, int):boolean");
    }

    public static boolean shouldInitilizePermissionData() {
        return CustomizeWrapper.isPermissionEnabled();
    }

    public static boolean initilizeNewAppAndSyncToSys(Context context, String pkgName, int uid, String reason) {
        if (!shouldInitilizePermissionData()) {
            return false;
        }
        if (context == null) {
            HwLog.w(LOG_TAG, "context null in init new apps.");
            return false;
        } else if (!initilizeNewApp(context, pkgName, uid)) {
            return false;
        } else {
            if (!Utility.isTestApp(pkgName)) {
                HwLog.i(LOG_TAG, "pkgName sync config to system group; pkg:" + pkgName);
                HwAppPermissions.create(context, pkgName).setHsmDefaultValues(reason);
            }
            return true;
        }
    }

    public static boolean initilizeNewApp(Context context, String pkgName, int uid) {
        if (context == null) {
            HwLog.w(LOG_TAG, "context null in initilizeNewApp.");
            return false;
        }
        ContentValues contentValues = DBAdapter.getInitialConfig(context, pkgName, uid, true);
        if (contentValues == null) {
            HwLog.w(LOG_TAG, "initilizeNewInstalledApp get null values.");
            return false;
        }
        try {
            Uri result = context.getContentResolver().insert(DBAdapter.BLOCK_TABLE_NAME_URI, contentValues);
            if (Log.HWINFO) {
                HwLog.i(LOG_TAG, "initilizeNewInstalledApp, insert result:" + result + ", for values:" + contentValues);
            }
            AddViewAppManager.trustIfNeeded(context, uid, pkgName, contentValues);
            return true;
        } catch (Exception e) {
            HwLog.w(LOG_TAG, "initilizeNewInstalledApp, insert fail.", e);
            return false;
        }
    }

    public static ContentValues getInitialConfigFromSystemConfig(Context context, PackageInfo pkgInfo) {
        if (pkgInfo == null || pkgInfo.applicationInfo == null) {
            HwLog.w(LOG_TAG, "getInitialConfigFromSystemConfig fail.");
            return null;
        }
        ContentValues value = DBAdapter.getInitialConfig(context, pkgInfo.packageName, pkgInfo.applicationInfo.uid, true);
        if (value == null) {
            HwLog.w(LOG_TAG, "This pkg has no permission requested:" + pkgInfo.applicationInfo.packageName);
            return null;
        }
        String[] requestedPermissions = pkgInfo.requestedPermissions;
        if (requestedPermissions == null || requestedPermissions.length == 0) {
            HwLog.w(LOG_TAG, "getInitialConfigFromSystemConfig: No permission requested in " + pkgInfo.packageName);
            return value;
        }
        int codeMask = AppInfo.getCodeMaskByRequestedPermissions(requestedPermissions);
        List<AppPermissionGroup> permissionGroupList = new AppPermissions(context, pkgInfo, null, false, null).getPermissionGroups();
        if (Utility.isNullOrEmptyList(permissionGroupList)) {
            HwLog.w(LOG_TAG, "getInitialConfigFromSystemConfig: No valid runtime permission is declared = " + pkgInfo.packageName);
            return value;
        }
        for (AppPermissionGroup group : permissionGroupList) {
            int operation = ((Integer) ShareCfg.value2UserOperation.get(HwAppPermissions.convertSysValueToHsmValue(pkgInfo.applicationInfo.targetSdkVersion <= 22, group.areRuntimePermissionsGranted(), group.isUserFixed(), SysAllow2Hsm.Allow))).intValue();
            int[] groupTypes = (int[]) MPermissionUtil.grpToTypeArray.get(group.getName());
            if (groupTypes != null) {
                for (int type : groupTypes) {
                    if ((type & codeMask) == type) {
                        changeContentValues(value, type, operation);
                    }
                }
            }
        }
        HwLog.i(LOG_TAG, "getInitialConfigFromSystemConfig, get value:" + value);
        return value;
    }

    public static void initializeRuntiemPermission(Context context, String pkgName, int uid, int type) {
        if (shouldInitilizePermissionData() && type == 67108864) {
            HwLog.i(LOG_TAG, "initializeRuntiemPermission, pkg:" + pkgName + ", type:" + type + ", config:" + 1);
            DBAdapter.setSinglePermission(context, uid, pkgName, type, 1);
        }
    }
}
