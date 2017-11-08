package com.huawei.systemmanager.shortcut;

import android.app.ActivityManagerNative;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.res.Configuration;
import android.content.res.ConfigurationEx;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.RemoteException;
import android.widget.TextView;
import com.huawei.harassmentinterception.ui.InterceptionActivity;
import com.huawei.netassistant.ui.NetAssistantMainActivity;
import com.huawei.permissionmanager.ui.MainActivity;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.addviewmonitor.AddViewMonitorActivity;
import com.huawei.systemmanager.applock.view.AppLockEntranceActivity;
import com.huawei.systemmanager.comm.misc.Closeables;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.module.ModuleMgr;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Shortcut;
import com.huawei.systemmanager.power.ui.HwPowerManagerActivity;
import com.huawei.systemmanager.spacecleanner.SpaceCleanActivity;
import com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public class ShortCutHelper {
    public static final String CONTAINER_COLUMN_NAME = "container";
    static final String FLAG_WIDGET_IN_LAUNCHER = "-100";
    static final String FLAG_WIDGET_IN_LAUNCHER_DOWN = "-101";
    public static final String INTENT_COLUMN_NAME = "intent";
    public static final String SHORTCUT_LAUNCHER_DB_DRAWER_PREFIX = "content://com.huawei.android.launcher.settings/drawer_favorites";
    public static final String SHORTCUT_LAUNCHER_DB_PREFIX = "content://com.huawei.android.launcher.settings/favorites";
    private static final String TAG = "ShortCutHelper";
    public static final String TITLE_COLUMN_NAME = "title";
    private String hsmPkgName;
    private Context mContext;
    public String mDatabaseUri = SHORTCUT_LAUNCHER_DB_PREFIX;

    public boolean hasShortcut(android.content.Context r12, java.lang.String r13, java.lang.String r14) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x00be in list []
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
        r11 = this;
        r0 = "ShortCutHelper";
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = " hasShortcut shortCutIntentClass:";
        r2 = r2.append(r3);
        r2 = r2.append(r13);
        r2 = r2.toString();
        com.huawei.systemmanager.util.HwLog.d(r0, r2);
        r1 = r11.getLauncherUri();
        if (r1 != 0) goto L_0x0022;
    L_0x0020:
        r0 = 0;
        return r0;
    L_0x0022:
        r0 = 1;
        r4 = new java.lang.String[r0];
        r0 = 0;
        r4[r0] = r14;
        r6 = 0;
        r0 = r12.getContentResolver();	 Catch:{ SQLiteException -> 0x00a4 }
        r3 = "iconPackage=? ";	 Catch:{ SQLiteException -> 0x00a4 }
        r2 = 0;	 Catch:{ SQLiteException -> 0x00a4 }
        r5 = 0;	 Catch:{ SQLiteException -> 0x00a4 }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ SQLiteException -> 0x00a4 }
        if (r6 == 0) goto L_0x00c6;
    L_0x0038:
        r0 = r6.getCount();	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        if (r0 <= 0) goto L_0x00c6;	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
    L_0x003e:
        r0 = "ShortCutHelper";	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        r2 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        r2.<init>();	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        r3 = "c.getCount():";	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        r2 = r2.append(r3);	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        r3 = r6.getCount();	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        r2 = r2.append(r3);	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        r2 = r2.toString();	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        com.huawei.systemmanager.util.HwLog.d(r0, r2);	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        r6.moveToFirst();	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
    L_0x005f:
        r0 = r6.isAfterLast();	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        if (r0 != 0) goto L_0x00bf;	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
    L_0x0065:
        r0 = "intent";	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        r9 = r6.getColumnIndex(r0);	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        r10 = r6.getString(r9);	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        if (r10 == 0) goto L_0x00b0;	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
    L_0x0072:
        r0 = r10.contains(r13);	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        if (r0 == 0) goto L_0x00b0;	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
    L_0x0078:
        r0 = "ShortCutHelper";	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        r2 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        r2.<init>();	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        r3 = "intentString:";	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        r2 = r2.append(r3);	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        r2 = r2.append(r10);	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        r3 = " shortCutIntentClass:";	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        r2 = r2.append(r3);	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        r2 = r2.append(r13);	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        r2 = r2.toString();	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        com.huawei.systemmanager.util.HwLog.d(r0, r2);	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        r0 = 1;
        if (r6 == 0) goto L_0x00a3;
    L_0x00a0:
        r6.close();
    L_0x00a3:
        return r0;
    L_0x00a4:
        r7 = move-exception;
        r0 = "ShortCutHelper";
        r2 = "read lauuncher db error , usually is caused by the cust is not correct";
        com.huawei.systemmanager.util.HwLog.d(r0, r2);
        r0 = 0;
        return r0;
    L_0x00b0:
        r6.moveToNext();	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        goto L_0x005f;
    L_0x00b4:
        r8 = move-exception;
        r8.printStackTrace();	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        r0 = 0;
        if (r6 == 0) goto L_0x00be;
    L_0x00bb:
        r6.close();
    L_0x00be:
        return r0;
    L_0x00bf:
        r0 = 0;
        if (r6 == 0) goto L_0x00c5;
    L_0x00c2:
        r6.close();
    L_0x00c5:
        return r0;
    L_0x00c6:
        r0 = "ShortCutHelper";	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        r2 = "cursor is null or count is 0";	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        com.huawei.systemmanager.util.HwLog.d(r0, r2);	 Catch:{ Exception -> 0x00b4, all -> 0x00d6 }
        r0 = 0;
        if (r6 == 0) goto L_0x00d5;
    L_0x00d2:
        r6.close();
    L_0x00d5:
        return r0;
    L_0x00d6:
        r0 = move-exception;
        if (r6 == 0) goto L_0x00dc;
    L_0x00d9:
        r6.close();
    L_0x00dc:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.systemmanager.shortcut.ShortCutHelper.hasShortcut(android.content.Context, java.lang.String, java.lang.String):boolean");
    }

    public java.lang.String queryShortCutNameByIntent(android.content.Context r13, android.content.Intent r14) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x00a4 in list []
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
        r12 = this;
        r3 = 0;
        r11 = 0;
        if (r14 != 0) goto L_0x0005;
    L_0x0004:
        return r11;
    L_0x0005:
        r0 = r14.getComponent();
        r7 = r0.flattenToShortString();
        r0 = "ShortCutHelper";
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = "queryShortCutNameByIntent componentClassName:";
        r1 = r1.append(r2);
        r1 = r1.append(r7);
        r1 = r1.toString();
        com.huawei.systemmanager.util.HwLog.d(r0, r1);
        r0 = 1;
        r4 = new java.lang.String[r0];
        r0 = new java.lang.StringBuilder;
        r0.<init>();
        r1 = "%";
        r0 = r0.append(r1);
        r0 = r0.append(r7);
        r1 = "%";
        r0 = r0.append(r1);
        r0 = r0.toString();
        r4[r3] = r0;
        r6 = 0;
        r0 = r13.getContentResolver();	 Catch:{ Exception -> 0x009b, all -> 0x00a5 }
        r1 = r12.getLauncherUri();	 Catch:{ Exception -> 0x009b, all -> 0x00a5 }
        r3 = "intent like ?";	 Catch:{ Exception -> 0x009b, all -> 0x00a5 }
        r2 = 0;	 Catch:{ Exception -> 0x009b, all -> 0x00a5 }
        r5 = 0;	 Catch:{ Exception -> 0x009b, all -> 0x00a5 }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x009b, all -> 0x00a5 }
        if (r6 == 0) goto L_0x0095;	 Catch:{ Exception -> 0x009b, all -> 0x00a5 }
    L_0x005b:
        r0 = r6.getCount();	 Catch:{ Exception -> 0x009b, all -> 0x00a5 }
        if (r0 <= 0) goto L_0x0095;	 Catch:{ Exception -> 0x009b, all -> 0x00a5 }
    L_0x0061:
        r6.moveToFirst();	 Catch:{ Exception -> 0x009b, all -> 0x00a5 }
        r0 = r6.isAfterLast();	 Catch:{ Exception -> 0x009b, all -> 0x00a5 }
        if (r0 != 0) goto L_0x0095;	 Catch:{ Exception -> 0x009b, all -> 0x00a5 }
    L_0x006a:
        r0 = "title";	 Catch:{ Exception -> 0x009b, all -> 0x00a5 }
        r10 = r6.getColumnIndex(r0);	 Catch:{ Exception -> 0x009b, all -> 0x00a5 }
        r9 = r6.getString(r10);	 Catch:{ Exception -> 0x009b, all -> 0x00a5 }
        r0 = "ShortCutHelper";	 Catch:{ Exception -> 0x009b, all -> 0x00a5 }
        r1 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x009b, all -> 0x00a5 }
        r1.<init>();	 Catch:{ Exception -> 0x009b, all -> 0x00a5 }
        r2 = "title:";	 Catch:{ Exception -> 0x009b, all -> 0x00a5 }
        r1 = r1.append(r2);	 Catch:{ Exception -> 0x009b, all -> 0x00a5 }
        r1 = r1.append(r9);	 Catch:{ Exception -> 0x009b, all -> 0x00a5 }
        r1 = r1.toString();	 Catch:{ Exception -> 0x009b, all -> 0x00a5 }
        com.huawei.systemmanager.util.HwLog.d(r0, r1);	 Catch:{ Exception -> 0x009b, all -> 0x00a5 }
        if (r6 == 0) goto L_0x0094;
    L_0x0091:
        r6.close();
    L_0x0094:
        return r9;
    L_0x0095:
        if (r6 == 0) goto L_0x009a;
    L_0x0097:
        r6.close();
    L_0x009a:
        return r11;
    L_0x009b:
        r8 = move-exception;
        r8.printStackTrace();	 Catch:{ Exception -> 0x009b, all -> 0x00a5 }
        if (r6 == 0) goto L_0x00a4;
    L_0x00a1:
        r6.close();
    L_0x00a4:
        return r11;
    L_0x00a5:
        r0 = move-exception;
        if (r6 == 0) goto L_0x00ab;
    L_0x00a8:
        r6.close();
    L_0x00ab:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.systemmanager.shortcut.ShortCutHelper.queryShortCutNameByIntent(android.content.Context, android.content.Intent):java.lang.String");
    }

    public ShortCutHelper(Context ctx) {
        this.mContext = ctx;
        this.hsmPkgName = this.mContext.getPackageName();
    }

    public List<ShortCutInfoItem> getShortCutItemList() {
        List<ShortCutInfoItem> mShortCutList = new ArrayList();
        ShortCutInfoItem mSpaceClearItem = getShortCutInfoItem(this.mContext, R.string.systemmanager_module_title_cleanup, R.drawable.ic_desk_appclean, R.drawable.ic_launcher_spaceclean, new Intent(this.mContext, SpaceCleanActivity.class));
        Intent powerSavingIntent = new Intent(this.mContext, HwPowerManagerActivity.class);
        powerSavingIntent.putExtra(HsmStatConst.PARAM_FROM_SHORT_CUT, "sc");
        ShortCutInfoItem mPowerSavingItem = getShortCutInfoItem(this.mContext, R.string.power_management_title, R.drawable.ic_desk_powersaving, R.drawable.ic_launcher_powersaving, powerSavingIntent);
        ShortCutInfoItem mHarassmentInterceptionItem = getShortCutInfoItem(this.mContext, R.string.systemmanager_module_title_blocklist, R.drawable.ic_desk_intercept, R.drawable.ic_launcher_harrasment, new Intent(this.mContext, InterceptionActivity.class));
        ShortCutInfoItem mTrafficManagerItem = getShortCutInfoItem(this.mContext, R.string.systemmanager_module_title_mobiledata, R.drawable.ic_desk_flow, R.drawable.ic_launcher_traffic, new Intent(this.mContext, NetAssistantMainActivity.class));
        ShortCutInfoItem mPermissionManagerItem = getShortCutInfoItem(this.mContext, R.string.systemmanager_module_title_permissions, R.drawable.ic_desk_permission, R.drawable.ic_launcher_permission, new Intent(this.mContext, MainActivity.class));
        Intent antiVirusIntent = ModuleMgr.MODULE_VIRUSSCANNER.getMainEntry(this.mContext);
        antiVirusIntent.putExtra(HsmStatConst.PARAM_FROM_SHORT_CUT, "sc");
        ShortCutInfoItem mAntiVirusItem = getShortCutInfoItem(this.mContext, R.string.systemmanager_module_title_virus, R.drawable.ic_desk_viruses, R.drawable.ic_launcher_antivirus, antiVirusIntent);
        Intent intent = new Intent(this.mContext, AppLockEntranceActivity.class);
        intent.putExtra(HsmStatConst.PARAM_FROM_SHORT_CUT, "sc");
        ShortCutInfoItem mAppLockItem = getShortCutInfoItem(this.mContext, R.string.ActionBar_EnterAppLock_Title, R.drawable.ic_desk_lock, R.drawable.ic_launcher_applock, intent);
        ShortCutInfoItem mAddViewItem = getShortCutInfoItem(this.mContext, R.string.addview_activity_title, R.drawable.ic_desk_windows, R.drawable.ic_launcher_addview, new Intent(this.mContext, AddViewMonitorActivity.class));
        intent = new Intent(this.mContext, StartupNormalAppListActivity.class);
        intent.putExtra(HsmStatConst.PARAM_FROM_SHORT_CUT, "sc");
        ShortCutInfoItem mBootStartUpItem = getShortCutInfoItem(this.mContext, R.string.systemmanager_module_title_autolaunch, R.drawable.ic_desk_start, R.drawable.ic_launcher_bootup, intent);
        mShortCutList.add(mSpaceClearItem);
        mShortCutList.add(mPowerSavingItem);
        if (!(!Utility.isWifiOnlyMode() ? Utility.isDataOnlyMode() : true)) {
            mShortCutList.add(mHarassmentInterceptionItem);
        }
        if (ModuleMgr.MODULE_NETWORKAPP.entryEnabled(this.mContext) && !Utility.isWifiOnlyMode()) {
            mShortCutList.add(mTrafficManagerItem);
        }
        if (ModuleMgr.MODULE_PERMISSION.entryEnabled(this.mContext)) {
            mShortCutList.add(mPermissionManagerItem);
        }
        if (ModuleMgr.MODULE_BOOTUP.entryEnabled(this.mContext)) {
            mShortCutList.add(mBootStartUpItem);
        }
        if (ModuleMgr.MODULE_VIRUSSCANNER.entryEnabled(this.mContext)) {
            mShortCutList.add(mAntiVirusItem);
        }
        mShortCutList.add(mAppLockItem);
        mShortCutList.add(mAddViewItem);
        return mShortCutList;
    }

    private ShortCutInfoItem getShortCutInfoItem(Context ctx, int titleResId, int iconResId, int iconInDeskResId, Intent destinationIntent) {
        int shortCutInLauncherResId;
        String shortCutIntentClass = destinationIntent.getComponent().flattenToShortString();
        HwLog.d(TAG, "getShortCutInfoItem shortCutIntentClass:" + shortCutIntentClass);
        boolean isShorCutInLauncher = hasShortcut(this.mContext, shortCutIntentClass, this.hsmPkgName);
        if (isShorCutInLauncher) {
            shortCutInLauncherResId = R.string.shortcut_already_in_launcher;
        } else if (isSuggestToLauncher(titleResId)) {
            shortCutInLauncherResId = R.string.shortcut_suggest_send_to_launcher;
        } else {
            shortCutInLauncherResId = R.string.shortcut_not_in_launcher;
        }
        return new ShortCutInfoItem(titleResId, shortCutInLauncherResId, isShorCutInLauncher, iconResId, iconInDeskResId, destinationIntent);
    }

    public boolean isSuggestToLauncher(int titleResId) {
        if (titleResId == R.string.systemmanager_module_title_cleanup || titleResId == R.string.power_management_title) {
            return true;
        }
        return false;
    }

    public Uri getLauncherUri() {
        return Uri.parse(this.mDatabaseUri);
    }

    public void createShortCut(Context ctx, int shortCutNameResId, int shortCutIconDeskResId, Intent destinationIntent) {
        HwLog.d(TAG, " createShortCut ");
        if (destinationIntent != null) {
            Intent shortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            shortcutIntent.putExtra("duplicate", false);
            shortcutIntent.putExtra("android.intent.extra.shortcut.NAME", ctx.getResources().getString(shortCutNameResId));
            shortcutIntent.putExtra("android.intent.extra.shortcut.ICON_RESOURCE", ShortcutIconResource.fromContext(ctx.getApplicationContext(), shortCutIconDeskResId));
            String action = destinationIntent.getAction();
            HwLog.d(TAG, "createShortCut action:" + action);
            if (action == null) {
                destinationIntent.setAction(TAG);
            }
            shortcutIntent.putExtra("android.intent.extra.shortcut.INTENT", destinationIntent);
            ctx.sendBroadcast(shortcutIntent);
            recordShortCutEvent(destinationIntent, "c");
        }
    }

    public void delShortcut(Context ctx, int shortCutNameResId, Intent destinationIntent) {
        HwLog.d(TAG, " delShortcut ");
        if (destinationIntent == null) {
            HwLog.d(TAG, "delShortcut destinationIntent is null");
            return;
        }
        String action = destinationIntent.getAction();
        HwLog.d(TAG, "delShortcut action:" + action);
        if (action == null) {
            destinationIntent.setAction(TAG);
        }
        String shortCutName = queryShortCutNameByIntent(this.mContext, destinationIntent);
        Intent delShortcutIntent = new Intent("com.android.launcher.action.UNINSTALL_SHORTCUT");
        delShortcutIntent.putExtra("android.intent.extra.shortcut.NAME", shortCutName);
        delShortcutIntent.putExtra("android.intent.extra.shortcut.INTENT", destinationIntent);
        ctx.sendBroadcast(delShortcutIntent);
        recordShortCutEvent(destinationIntent, "d");
    }

    public boolean isOneKeyCleanWidgetInLauncher(Context ctx) {
        HwLog.d(TAG, "onekeyCleanComponentName:" + new ComponentName("com.huawei.android.launcher", "com.huawei.android.launcher.widget.OneKeyCleanWidget").flattenToShortString());
        Uri launcherUri = getLauncherUri();
        if (launcherUri == null) {
            return false;
        }
        try {
            Cursor c = ctx.getContentResolver().query(launcherUri, null, "intent = ? and ( container = ? or container = ? ) ", new String[]{"#Intent;component=" + onekeyCleanComponentName + ";end", FLAG_WIDGET_IN_LAUNCHER, FLAG_WIDGET_IN_LAUNCHER_DOWN}, null);
            if (c == null) {
                HwLog.d(TAG, "launcher cursor is null");
                return false;
            }
            try {
                if (c.getCount() > 0) {
                    HwLog.d(TAG, "launcher cursor count :" + c.getCount());
                    return true;
                }
                HwLog.d(TAG, "launcher cursor count <= 0");
                Closeables.close(c);
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                Closeables.close(c);
            }
        } catch (SQLiteException e2) {
            HwLog.e(TAG, "isOneKeyCleanWidgetInLauncher SQLiteException");
            return false;
        }
    }

    public static void recordShortCutEvent(Intent destinationIntent, String operation) {
        if (destinationIntent != null) {
            HwLog.d(TAG, "cutClassName:" + HsmStatConst.cutActivityName(destinationIntent.getComponent().getClassName()));
            HsmStat.statE(Shortcut.NAME, cutClassName, operation);
        }
    }

    public static boolean isInSimpleLauncher() {
        Configuration curConfig = new Configuration();
        try {
            curConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
            ConfigurationEx extraConfig = new com.huawei.android.content.res.ConfigurationEx(curConfig).getExtraConfig();
            if (extraConfig != null && 2 == extraConfig.simpleuiMode) {
                return true;
            }
        } catch (RemoteException e) {
            HwLog.e(TAG, "read is simple launcher error!");
        }
        return false;
    }

    public static boolean isShieldApps(int shortCutNameResId) {
        switch (shortCutNameResId) {
            case R.string.ActionBar_EnterAppLock_Title:
            case R.string.systemmanager_module_title_cleanup:
            case R.string.systemmanager_module_title_blocklist:
            case R.string.systemmanager_module_title_virus:
            case R.string.systemmanager_module_title_autolaunch:
                return true;
            default:
                return false;
        }
    }

    public static void setTextViewMultiLines(TextView text) {
        HwLog.d(TAG, "The text should be multi lines!");
        if (text != null) {
            text.setSingleLine(false);
            text.setMaxLines(2);
        }
    }
}
