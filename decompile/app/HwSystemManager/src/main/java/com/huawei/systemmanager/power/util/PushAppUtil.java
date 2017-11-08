package com.huawei.systemmanager.power.util;

import android.content.Context;
import android.provider.Settings.Secure;
import android.util.ArraySet;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.comm.xml.XmlParsers;
import com.huawei.systemmanager.comm.xml.base.SimpleXmlRow;
import com.huawei.systemmanager.customize.CustomizeManager;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PushAppUtil {
    private static final String ATTR_NAME = "name";
    private static final Object DEVIDER = SqlMarker.SQL_END;
    private static final String PKG_HIDISK = "com.huawei.hidisk";
    private static final String PKG_PARENTCONTROL = "com.huawei.parentcontrol";
    private static final String PKG_PARENTCONTROL_PARENT = "com.huawei.parentcontrol.parent";
    private static final String PKG_QINQINGUANHUAI = "com.huawei.android.remotecontrol";
    private static final String PKG_SHOUJIZHAOHUI = "com.huawei.android.ds";
    private static final String PUSH_CUST_FILE = CustomizeManager.composeCustFileName("xml/hsm/power/hsm_power_push_whiteapps.xml");
    private static final String PUSH_WHITE_APP = "push_white_apps";
    public static final String TAG = "PushAppUtil";

    private static java.util.List<java.lang.String> getWhiteAppsInCloud(android.content.Context r11) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x004f in list []
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
        r0 = r11.getContentResolver();	 Catch:{ Exception -> 0x0046, all -> 0x0056 }
        r1 = com.huawei.systemmanager.rainbow.db.base.CloudConst.PushBlackList.CONTENT_OUTERTABLE_URI;	 Catch:{ Exception -> 0x0046, all -> 0x0056 }
        r2 = 1;	 Catch:{ Exception -> 0x0046, all -> 0x0056 }
        r2 = new java.lang.String[r2];	 Catch:{ Exception -> 0x0046, all -> 0x0056 }
        r3 = "packageName";	 Catch:{ Exception -> 0x0046, all -> 0x0056 }
        r4 = 0;	 Catch:{ Exception -> 0x0046, all -> 0x0056 }
        r2[r4] = r3;	 Catch:{ Exception -> 0x0046, all -> 0x0056 }
        r3 = 0;	 Catch:{ Exception -> 0x0046, all -> 0x0056 }
        r4 = 0;	 Catch:{ Exception -> 0x0046, all -> 0x0056 }
        r5 = 0;	 Catch:{ Exception -> 0x0046, all -> 0x0056 }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x0046, all -> 0x0056 }
        if (r6 != 0) goto L_0x0023;
    L_0x001d:
        if (r6 == 0) goto L_0x0022;
    L_0x001f:
        r6.close();
    L_0x0022:
        return r10;
    L_0x0023:
        r0 = r6.moveToNext();	 Catch:{ Exception -> 0x0046, all -> 0x0056 }
        if (r0 == 0) goto L_0x0050;	 Catch:{ Exception -> 0x0046, all -> 0x0056 }
    L_0x0029:
        r0 = 0;	 Catch:{ Exception -> 0x0046, all -> 0x0056 }
        r9 = r6.getString(r0);	 Catch:{ Exception -> 0x0046, all -> 0x0056 }
        r0 = android.text.TextUtils.isEmpty(r9);	 Catch:{ Exception -> 0x0046, all -> 0x0056 }
        if (r0 != 0) goto L_0x0023;	 Catch:{ Exception -> 0x0046, all -> 0x0056 }
    L_0x0034:
        r0 = ":1";	 Catch:{ Exception -> 0x0046, all -> 0x0056 }
        r8 = r9.indexOf(r0);	 Catch:{ Exception -> 0x0046, all -> 0x0056 }
        if (r8 < 0) goto L_0x0023;	 Catch:{ Exception -> 0x0046, all -> 0x0056 }
    L_0x003d:
        r0 = 0;	 Catch:{ Exception -> 0x0046, all -> 0x0056 }
        r9 = r9.substring(r0, r8);	 Catch:{ Exception -> 0x0046, all -> 0x0056 }
        r10.add(r9);	 Catch:{ Exception -> 0x0046, all -> 0x0056 }
        goto L_0x0023;
    L_0x0046:
        r7 = move-exception;
        r7.printStackTrace();	 Catch:{ Exception -> 0x0046, all -> 0x0056 }
        if (r6 == 0) goto L_0x004f;
    L_0x004c:
        r6.close();
    L_0x004f:
        return r10;
    L_0x0050:
        if (r6 == 0) goto L_0x004f;
    L_0x0052:
        r6.close();
        goto L_0x004f;
    L_0x0056:
        r0 = move-exception;
        if (r6 == 0) goto L_0x005c;
    L_0x0059:
        r6.close();
    L_0x005c:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.systemmanager.power.util.PushAppUtil.getWhiteAppsInCloud(android.content.Context):java.util.List<java.lang.String>");
    }

    public static void initPushWhiteApps(Context context) {
        if (context == null) {
            HwLog.w(TAG, "context is null.");
            return;
        }
        List<String> apps = getConfiguredWhiteApps(PUSH_CUST_FILE);
        List<String> appsInCloudDb = getWhiteAppsInCloud(context);
        ArraySet<String> set = HsmCollections.newArraySet();
        set.addAll(apps);
        set.addAll(appsInCloudDb);
        setPushWhiteApps(context, set);
    }

    private static void setPushWhiteApps(Context context, Collection<String> pkgs) {
        if (pkgs == null) {
            HwLog.w(TAG, "white list of push apps is null.");
            return;
        }
        String list = listToString(pkgs);
        HwLog.i(TAG, "setPushWhiteApps:" + list);
        Secure.putString(context.getContentResolver(), PUSH_WHITE_APP, list);
    }

    private static String listToString(Collection<String> pkgs) {
        StringBuffer strBuf = new StringBuffer();
        for (String pkgName : pkgs) {
            strBuf.append(pkgName);
            strBuf.append(DEVIDER);
        }
        return strBuf.toString();
    }

    private static List<String> getConfiguredWhiteApps(String filename) {
        ArrayList<String> result = new ArrayList();
        try {
            for (SimpleXmlRow row : XmlParsers.diskSimpleXmlRows(filename)) {
                result.add(row.getAttrValue("name"));
            }
            HwLog.i(TAG, "push white apps from config:" + result);
        } catch (Exception e) {
            result.add(PKG_QINQINGUANHUAI);
            result.add(PKG_SHOUJIZHAOHUI);
            result.add(PKG_PARENTCONTROL);
            result.add(PKG_PARENTCONTROL_PARENT);
            result.add(PKG_HIDISK);
            HwLog.i(TAG, "push white apps defualt:" + result);
        }
        return result;
    }
}
