package com.huawei.systemmanager.startupmgr.service;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import com.google.android.collect.Maps;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.huawei.systemmanager.rainbow.db.CloudDBAdapter;
import com.huawei.systemmanager.rainbow.db.bean.StartupConfigBean;
import com.huawei.systemmanager.startupmgr.comm.AwakedStartupInfo;
import com.huawei.systemmanager.startupmgr.comm.MonitorActionsAssist;
import com.huawei.systemmanager.startupmgr.comm.MonitorActionsAssist.ActionCfg;
import com.huawei.systemmanager.startupmgr.comm.NormalStartupInfo;
import com.huawei.systemmanager.startupmgr.comm.StartupBinderAccess;
import com.huawei.systemmanager.startupmgr.confdata.StartupConfData;
import com.huawei.systemmanager.startupmgr.db.StartupDataMgrHelper;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import huawei.android.pfw.HwPFWStartupSetting;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class StartupConsistency {
    private static final String TAG = "StartupConsistencyChecker";
    private Map<String, AwakedStartupInfo> mAwakedStartupMap = Maps.newHashMap();
    private Map<String, NormalStartupInfo> mNormalStartupMap = Maps.newHashMap();
    private Set<String> mPengdingDeletePkgs = Sets.newHashSet();
    StartupPreSettingData mPreData;

    public StartupConsistency(StartupPreSettingData preData) {
        this.mPreData = preData;
    }

    public void checkConsistency(Context ctx) {
        resetMember();
        checkNormalStartupConsistency(ctx);
        checkAwakedStartupConsistency(ctx);
        deleteUnusedData(ctx);
        persistSettingToFwkAndDB(ctx);
    }

    public void loadNewPackageDefaultData(Context ctx, String pkgName) {
        NormalStartupInfo startupInfo = loadSingleNormalStartInfoFromPm(ctx, pkgName);
        if (startupInfo == null || !startupInfo.validInfo()) {
            HwLog.w(TAG, "loadNewPackageDefaultData not valid startupInfo of " + pkgName);
        } else {
            startupInfo.persistFullData(ctx, true);
        }
    }

    public void loadAndUpdateExistPackageData(Context ctx, String pkgName) {
        NormalStartupInfo startupInfoPM = loadSingleNormalStartInfoFromPm(ctx, pkgName);
        if (startupInfoPM != null) {
            boolean validInfo = startupInfoPM.validInfo();
            NormalStartupInfo startupInfoDB = StartupDataMgrHelper.querySingleNormalStartupInfo(ctx, pkgName);
            if (startupInfoDB != null) {
                startupInfoPM.setStatus(startupInfoDB.getStatus());
            }
            startupInfoPM.persistFullData(ctx, true);
            HwLog.i(TAG, "loadAndUpdateExistPackageData, pkageName:" + pkgName + ", validInfo:" + validInfo + ", state:" + startupInfoPM.getStatus());
            return;
        }
        HwLog.w(TAG, "loadAndUpdateExistPackageData not valid startupInfo of " + pkgName);
    }

    public void handleCloudDataUpdate(Context ctx) {
        HwLog.i(TAG, "handleCloudDataUpdate");
        handleNormalStartupCloudUpdate(ctx);
        handleAwakedStartupCloudUpdate(ctx);
    }

    private NormalStartupInfo loadSingleNormalStartInfoFromPm(Context ctx, String pkgName) {
        NormalStartupInfo normalStartupInfo = null;
        try {
            PackageInfo pkgInfo = PackageManagerWrapper.getPackageInfo(ctx.getPackageManager(), pkgName, 8706);
            if (this.mPreData.isUnderControlledApp(pkgInfo.applicationInfo)) {
                NormalStartupInfo startupInfo = new NormalStartupInfo(pkgName);
                try {
                    if (hasAnyReceiver(pkgInfo)) {
                        startupInfo.setHasAny();
                    }
                    startupInfo.setStatus(StartupConfData.getDftReceiverCfg(ctx, pkgInfo.packageName));
                    for (ActionCfg cfg : MonitorActionsAssist.copyOfMonitorActions()) {
                        if (isReceiverExist(ctx, pkgName, cfg.getSearchIntent())) {
                            int i;
                            startupInfo.appendAction(cfg.actionName());
                            if (cfg.isBootAction()) {
                                i = 1;
                            } else {
                                i = 2;
                            }
                            startupInfo.appendStartType(i);
                        }
                    }
                    return startupInfo;
                } catch (NameNotFoundException e) {
                    normalStartupInfo = startupInfo;
                    HwLog.e(TAG, "loadSingleNormalStartInfoFromPm " + pkgName + " not found");
                    return normalStartupInfo;
                }
            }
            HwLog.w(TAG, "loadSingleNormalStartInfoFromPm " + pkgName + " is not under control");
            return null;
        } catch (NameNotFoundException e2) {
            HwLog.e(TAG, "loadSingleNormalStartInfoFromPm " + pkgName + " not found");
            return normalStartupInfo;
        }
    }

    private void resetMember() {
        this.mNormalStartupMap.clear();
        this.mAwakedStartupMap.clear();
        this.mPengdingDeletePkgs.clear();
    }

    private void checkNormalStartupConsistency(Context ctx) {
        loadNormalStartupInfoFromPM(ctx);
        for (NormalStartupInfo info : StartupDataMgrHelper.queryNormalStartupInfoList(ctx)) {
            NormalStartupInfo pmInfo = (NormalStartupInfo) this.mNormalStartupMap.get(info.getPackageName());
            if (pmInfo == null) {
                this.mPengdingDeletePkgs.add(info.getPackageName());
            } else {
                pmInfo.setStatus(info.getStatus());
            }
        }
    }

    private void checkAwakedStartupConsistency(Context ctx) {
        for (AwakedStartupInfo info : StartupDataMgrHelper.queryAwakedStartupInfoList(ctx)) {
            if (!this.mPengdingDeletePkgs.contains(info.getPackageName())) {
                this.mAwakedStartupMap.put(info.getPackageName(), info);
            }
        }
    }

    private void deleteUnusedData(Context ctx) {
        for (String pkgName : this.mPengdingDeletePkgs) {
            StartupDataMgrHelper.deleteNotExistPackageData(ctx, pkgName);
        }
    }

    private void persistSettingToFwkAndDB(Context ctx) {
        List<HwPFWStartupSetting> settingList = Lists.newArrayList();
        for (Entry<String, NormalStartupInfo> entry : this.mNormalStartupMap.entrySet()) {
            NormalStartupInfo info = (NormalStartupInfo) entry.getValue();
            if (info.validInfo()) {
                info.persistFullData(ctx, false);
                settingList.add(info.getPFWStartupSetting());
            }
        }
        for (Entry<String, AwakedStartupInfo> entry2 : this.mAwakedStartupMap.entrySet()) {
            AwakedStartupInfo info2 = (AwakedStartupInfo) entry2.getValue();
            if (info2.validInfo()) {
                info2.persistFullData(ctx, false);
                settingList.add(info2.getPFWStartupSetting());
            }
        }
        StartupBinderAccess.writeStartupSettingList(settingList, true);
    }

    private void loadNormalStartupInfoFromPM(Context ctx) {
        loadDefaultList(ctx);
        for (ActionCfg cfg : MonitorActionsAssist.copyOfMonitorActions()) {
            getResolveInfoList(ctx, cfg.getSearchIntent(), cfg.isBootAction());
        }
    }

    private void loadDefaultList(Context ctx) {
        for (PackageInfo pkgInfo : PackageManagerWrapper.getInstalledPackages(ctx.getPackageManager(), 8706)) {
            if (this.mPreData.isUnderControlledApp(pkgInfo.applicationInfo) && ((NormalStartupInfo) this.mNormalStartupMap.get(pkgInfo.packageName)) == null) {
                NormalStartupInfo startupInfo = new NormalStartupInfo(pkgInfo.packageName);
                if (hasAnyReceiver(pkgInfo)) {
                    startupInfo.setHasAny();
                }
                startupInfo.setStatus(StartupConfData.getDftReceiverCfg(ctx, pkgInfo.packageName));
                if (startupInfo.validInfo()) {
                    this.mNormalStartupMap.put(pkgInfo.packageName, startupInfo);
                }
            }
        }
    }

    boolean hasAnyReceiver(PackageInfo pkgInfo) {
        return pkgInfo.receivers != null && pkgInfo.receivers.length > 0;
    }

    private void getResolveInfoList(Context ctx, Intent intent, boolean hasBootComp) {
        try {
            for (ResolveInfo info : PackageManagerWrapper.queryBroadcastReceivers(ctx.getPackageManager(), intent, 514)) {
                ComponentInfo componentInfo = info.activityInfo != null ? info.activityInfo : info.serviceInfo;
                if (componentInfo != null) {
                    findAndUpdateStartupInfo(componentInfo.applicationInfo, intent.getAction(), hasBootComp);
                }
            }
        } catch (Exception e) {
            HwLog.d(TAG, "getResolveInfoList for action: " + intent.getAction() + " catch exception", e);
        }
    }

    private boolean isReceiverExist(Context ctx, String pkgName, Intent intent) {
        try {
            PackageManager packageManager = ctx.getPackageManager();
            intent.setPackage(pkgName);
            List<ResolveInfo> result = PackageManagerWrapper.queryBroadcastReceivers(packageManager, intent, 514);
            HwLog.d(TAG, "isReceiverExist result length of action " + intent.getAction() + " is " + result.size());
            for (ResolveInfo info : result) {
                if (pkgName.equals((info.activityInfo != null ? info.activityInfo : info.serviceInfo).applicationInfo.packageName)) {
                    return true;
                }
            }
        } catch (Exception e) {
            HwLog.d(TAG, "isReceiverExist for action: " + intent.getAction() + " catch exception", e);
        }
        HwLog.d(TAG, "isReceiverExist can't find receiver for action: " + intent.getAction());
        return false;
    }

    private void findAndUpdateStartupInfo(ApplicationInfo appInfo, String action, boolean hasBootComp) {
        if (appInfo != null && this.mPreData.isUnderControlledApp(appInfo)) {
            NormalStartupInfo startupInfo = (NormalStartupInfo) this.mNormalStartupMap.get(appInfo.packageName);
            if (startupInfo != null) {
                int i;
                startupInfo.appendAction(action);
                if (hasBootComp) {
                    i = 1;
                } else {
                    i = 2;
                }
                startupInfo.appendStartType(i);
            }
        }
    }

    private void handleNormalStartupCloudUpdate(Context ctx) {
        for (NormalStartupInfo info : StartupDataMgrHelper.queryNormalStartupInfoList(ctx)) {
            if (info.isUserChanged()) {
                HwLog.d(TAG, "handleNormalStartupCloudUpdate " + info.getPackageName() + " already changed!");
            } else {
                StartupConfigBean bean = CloudDBAdapter.getInstance(ctx).getSingleStartupConfig(info.getPackageName());
                if (!(bean == null || info.getStatus() == bean.isReceiverAllowStart())) {
                    HwLog.i(TAG, "handleNormalStartupCloudUpdate update " + info.getPackageName() + " to " + bean.isReceiverAllowStart());
                    info.setStatus(bean.isReceiverAllowStart());
                    info.persistStatusData(ctx, true);
                }
            }
        }
    }

    private void handleAwakedStartupCloudUpdate(Context ctx) {
        for (AwakedStartupInfo info : StartupDataMgrHelper.queryAwakedStartupInfoList(ctx)) {
            if (info.isUserChanged()) {
                HwLog.d(TAG, "handleAwakedStartupCloudUpdate " + info.getPackageName() + " already changed!");
            } else {
                StartupConfigBean bean = CloudDBAdapter.getInstance(ctx).getSingleStartupConfig(info.getPackageName());
                if (!(bean == null || info.getStatus() == bean.isProviderServiceAllowStart())) {
                    HwLog.i(TAG, "handleAwakedStartupCloudUpdate update " + info.getPackageName() + " to " + bean.isProviderServiceAllowStart());
                    info.setStatus(bean.isProviderServiceAllowStart());
                    info.persistStatusData(ctx, true);
                }
            }
        }
    }
}
