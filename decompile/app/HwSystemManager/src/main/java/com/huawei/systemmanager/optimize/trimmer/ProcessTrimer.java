package com.huawei.systemmanager.optimize.trimmer;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.hsm.MediaTransactWrapper;
import android.media.AudioManager;
import android.os.Process;
import android.os.SystemClock;
import com.google.android.collect.Maps;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.reflect.ActivityManagerReflect;
import com.huawei.systemmanager.customize.AbroadUtils;
import com.huawei.systemmanager.optimize.process.HwRecentsLockUtils;
import com.huawei.systemmanager.optimize.process.ProcessFilterPolicy;
import com.huawei.systemmanager.optimize.process.SmcsDbHelper;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPkgUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ProcessTrimer {
    private static final int MAX_TASKS = 100;
    private static final String TAG = "ProcessTrimer";
    private Set<String> mSkipPkgs = Sets.newHashSet();

    public TrimResult doTrim(TrimParam param) {
        TrimResult result;
        HwLog.i(TAG, "start do trim");
        long start = SystemClock.elapsedRealtime();
        int origPri = Process.getThreadPriority(Process.myTid());
        Process.setThreadPriority(10);
        Map<String, ArrayList<Integer>> removedPkgs = doRemoveTasks(param);
        HwLog.i(TAG, "remove task end");
        List<String> pkgList = Lists.newArrayList();
        for (Entry<String, ArrayList<Integer>> entry : removedPkgs.entrySet()) {
            pkgList.add((String) entry.getKey());
        }
        if (AbroadUtils.isAbroad()) {
            result = new TrimResult(removedPkgs.size(), 0);
        } else {
            result = new TrimResult(removedPkgs.size(), forceStopApps(param, pkgList, removedPkgs).size());
        }
        HwLog.i(TAG, "do trim end, cost time:" + (SystemClock.elapsedRealtime() - start));
        Process.setThreadPriority(origPri);
        return result;
    }

    private Map<String, ArrayList<Integer>> doRemoveTasks(TrimParam param) {
        Context ctx = param.getContext();
        ActivityManager am = (ActivityManager) ctx.getSystemService("activity");
        List<RecentTaskInfo> recentTasks = am.getRecentTasks(100, 62);
        if (recentTasks == null) {
            HwLog.e(TAG, "doRemoveTasks getrecent task is null!");
            return Maps.newHashMap();
        }
        int numTasks = recentTasks.size();
        HwLog.i(TAG, "getRecentTasks list size = " + numTasks);
        Set<String> protectPkgs = Sets.newHashSet();
        if (param.isOnekeyclean() && AbroadUtils.isAbroad()) {
            protectPkgs = SmcsDbHelper.getProtectMap(ctx, true);
        }
        String foregroundApp = getForegroundApp(ctx);
        Set<String> lockedPkgs = HwRecentsLockUtils.getLockedPkgs(ctx);
        Set<Integer> musicUids = getMusicPlayingUids(ctx);
        Map<String, ArrayList<Integer>> result = Maps.newHashMap();
        for (int i = 0; i < numTasks; i++) {
            RecentTaskInfo recentInfo = (RecentTaskInfo) recentTasks.get(i);
            Intent intent = new Intent(recentInfo.baseIntent);
            if (recentInfo.origActivity != null) {
                intent.setComponent(recentInfo.origActivity);
            }
            String packageName = intent.getComponent().getPackageName();
            int userid = recentInfo.userId;
            if (lockedPkgs.contains(packageName)) {
                HwLog.i(TAG, "pkg is locked, didnot removed locked pkg:" + packageName);
            } else if (checkMusicPlay(musicUids, packageName)) {
                HwLog.i(TAG, "Pkg is playing music, didnot remove:" + packageName);
            } else {
                if (param.isOnekeyclean()) {
                    if (protectPkgs.contains(packageName)) {
                        HwLog.i(TAG, "Pkg is protected, didnot remove:" + packageName);
                    } else if (param.isKeepForeground() && Objects.equal(packageName, foregroundApp)) {
                        HwLog.i(TAG, "didnot remove foreground pkg:" + packageName);
                    }
                }
                if ("com.android.systemui".equals(packageName)) {
                    HwLog.i(TAG, "did not removetask systemui");
                } else {
                    long activeTime = recentInfo.lastActiveTime;
                    if (activeTime >= param.getStartTime() && activeTime < System.currentTimeMillis()) {
                        HwLog.i(TAG, "pkg should be skipped, its activeTime is:" + activeTime + ", pkg:" + packageName);
                        this.mSkipPkgs.add(packageName);
                    } else if (checkIfPkgSkipped(param, packageName)) {
                        HwLog.i(TAG, "pkg should be skipped, did not remove, pkg:" + packageName);
                    } else {
                        HwLog.i(TAG, "remove task, pkg:" + packageName + " userId: " + userid);
                        ActivityManagerReflect.setRemoveTask(am, recentInfo.persistentId, isSystemProtected(packageName) ? 0 : 1);
                        ArrayList<Integer> userIds = (ArrayList) result.get(packageName);
                        if (userIds == null) {
                            userIds = new ArrayList();
                            userIds.add(Integer.valueOf(userid));
                            result.put(packageName, userIds);
                        } else {
                            userIds.add(Integer.valueOf(userid));
                        }
                    }
                }
            }
        }
        return result;
    }

    private boolean isSystemProtected(String pkg) {
        return !"com.huawei.systemmanager".equals(pkg) ? "com.huawei.eassistant".equals(pkg) : true;
    }

    private Set<Integer> getMusicPlayingUids(Context ctx) {
        Set<Integer> musicUids = Sets.newHashSet();
        boolean z = false;
        try {
            AudioManager am = (AudioManager) ctx.getSystemService("audio");
            if (am == null) {
                HwLog.e(TAG, "getMusicPlayingUids am is null!");
                return musicUids;
            }
            z = am.isMusicActive();
            musicUids.addAll(MediaTransactWrapper.playingMusicUidSet());
            HwLog.i(TAG, "getMusicPlayingUids result: " + musicUids + ", active:" + z);
            return musicUids;
        } catch (Exception ex) {
            HwLog.e(TAG, "getMusicPlayingUids catch exception: " + ex.getMessage());
        }
    }

    private boolean checkMusicPlay(Set<Integer> musicUids, String pkgName) {
        if (musicUids.contains(Integer.valueOf(HsmPkgUtils.getPackageUid(pkgName)))) {
            return true;
        }
        return false;
    }

    private List<String> forceStopApps(TrimParam param, List<String> taskRemovedPkgs, Map<String, ArrayList<Integer>> pkgsAndUserIds) {
        ActivityManager activityManager = (ActivityManager) param.getContext().getSystemService("activity");
        List<String> pkgTobeKill = ProcessFilterPolicy.getOnekeycleanPkg(param, taskRemovedPkgs);
        List<String> result = Lists.newArrayList();
        for (String pkg : pkgTobeKill) {
            if (checkIfPkgSkipped(param, pkg)) {
                HwLog.i(TAG, " pkg should be skipped, did not kill, pkg:" + pkg);
            } else {
                ArrayList<Integer> userIds = (ArrayList) pkgsAndUserIds.get(pkg);
                if (userIds != null) {
                    for (int i = 0; i < userIds.size(); i++) {
                        int userid = ((Integer) userIds.get(i)).intValue();
                        HwLog.i(TAG, " forcestopApp pkgName: " + pkg + " userId: " + userid);
                        activityManager.forceStopPackageAsUser(pkg, userid);
                    }
                } else {
                    HwLog.i(TAG, "forcestopApp : " + pkg);
                    activityManager.forceStopPackage(pkg);
                }
                result.add(pkg);
            }
        }
        return result;
    }

    private boolean checkIfPkgSkipped(TrimParam param, String pkg) {
        if (param.consumeRecentTaskFlag()) {
            List<RecentTaskInfo> recentTasks = ((ActivityManager) param.getContext().getSystemService("activity")).getRecentTasks(10, 14);
            if (!HsmCollections.isEmpty(recentTasks)) {
                for (RecentTaskInfo recentInfo : recentTasks) {
                    long currentTime = System.currentTimeMillis();
                    long activeTime = recentInfo.lastActiveTime;
                    long trimStartTime = param.getStartTime();
                    if (activeTime > trimStartTime && activeTime <= currentTime) {
                        Intent intent = new Intent(recentInfo.baseIntent);
                        if (recentInfo.origActivity != null) {
                            intent.setComponent(recentInfo.origActivity);
                        }
                        String pkgName = intent.getComponent().getPackageName();
                        HwLog.i(TAG, "checkIfPkgSkipped, add launched pkg:" + pkgName + ", activeTime:" + activeTime + ", trimStartTime:" + trimStartTime + ", currentTime: " + currentTime);
                        this.mSkipPkgs.add(pkgName);
                    }
                }
            }
        }
        return this.mSkipPkgs.contains(pkg);
    }

    private String getForegroundApp(Context ctx) {
        String result = "";
        try {
            List<RunningTaskInfo> taskInfos = ((ActivityManager) ctx.getSystemService("activity")).getRunningTasks(2);
            if (taskInfos != null && taskInfos.size() == 2) {
                result = ((RunningTaskInfo) taskInfos.get(1)).baseActivity.getPackageName();
            }
        } catch (Exception ex) {
            HwLog.e(TAG, "preTopTask catch exception: " + ex.getMessage());
        }
        return result;
    }
}
