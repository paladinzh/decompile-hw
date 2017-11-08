package com.huawei.systemmanager.optimize.process;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.UserHandle;
import android.text.TextUtils;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.huawei.android.smcs.STProcessRecord;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.optimize.process.Predicate.CheckPredicate;
import com.huawei.systemmanager.optimize.process.Predicate.CheckProtectedAppPredicate;
import com.huawei.systemmanager.optimize.process.Predicate.CustomKeyPredicate;
import com.huawei.systemmanager.optimize.process.Predicate.InputMethodPredicate;
import com.huawei.systemmanager.optimize.process.Predicate.KeepPreTopTaskPredicate;
import com.huawei.systemmanager.optimize.process.Predicate.LauncherPredicate;
import com.huawei.systemmanager.optimize.process.Predicate.MemoryPredicate;
import com.huawei.systemmanager.optimize.process.Predicate.MusicPlayingPredicate;
import com.huawei.systemmanager.optimize.process.Predicate.PersistentFlagPerdicate;
import com.huawei.systemmanager.optimize.process.Predicate.ProtectControlledPredicate;
import com.huawei.systemmanager.optimize.process.Predicate.SuperAppPredicate;
import com.huawei.systemmanager.optimize.process.Predicate.SystemIdPredicate;
import com.huawei.systemmanager.optimize.process.Predicate.SystemUILockPredicate;
import com.huawei.systemmanager.optimize.process.Predicate.VisibleWidgetPredicate;
import com.huawei.systemmanager.optimize.process.Predicate.WallPaperPredicate;
import com.huawei.systemmanager.optimize.trimmer.TrimParam;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.am.HsmActivityManager;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ProcessFilterPolicy {
    private static final Comparator<ProcessAppItem> PROCESS_APP_COMPARATOR = new Comparator<ProcessAppItem>() {
        public int compare(ProcessAppItem lhs, ProcessAppItem rhs) {
            boolean leftKeyTask = lhs.isKeyProcess();
            if ((leftKeyTask ^ rhs.isKeyProcess()) != 0) {
                return leftKeyTask ? 1 : -1;
            } else if (leftKeyTask) {
                return ProcessAppItem.MEMORY_COPARATOR.compare(lhs, rhs);
            } else {
                int protectedCompare = ProcessAppItem.PROTECT_COMPARATOR.compare(lhs, rhs);
                if (protectedCompare == 0) {
                    return ProcessAppItem.MEMORY_COPARATOR.compare(lhs, rhs);
                }
                return protectedCompare;
            }
        }
    };
    private static final String TAG = "ProcessFilterPolicy";

    private static class TempDataHolder {
        List<Integer> pidList;
        List<ProcessAppItem> processList;

        private TempDataHolder() {
        }
    }

    private static class TransFuncPkgToItem implements Function<String, ProcessAppItem> {
        private final HsmPackageManager mPkgManager;

        private TransFuncPkgToItem() {
            this.mPkgManager = HsmPackageManager.getInstance();
        }

        public ProcessAppItem apply(String input) {
            if (TextUtils.isEmpty(input)) {
                HwLog.i(ProcessFilterPolicy.TAG, "TransPkgToItem, input is empty");
                return null;
            }
            HsmPkgInfo pkgInfo = this.mPkgManager.getPkgInfo(input);
            if (pkgInfo != null) {
                return new ProcessAppItem(pkgInfo);
            }
            HwLog.i(ProcessFilterPolicy.TAG, "TransPkgToItem, can not found pkg:" + input);
            return null;
        }
    }

    public static List<ProcessAppItem> getRunningApps(Context ctx) {
        return queryAllRunningAppInfo(ctx);
    }

    public static boolean queryIfAppAlive(Context ctx, String pkg) {
        return queryIfPkgAlive(pkg);
    }

    private static TempDataHolder getRunningAppMap() {
        List<RunningAppProcessInfo> appProcessList = ((ActivityManager) GlobalContext.getContext().getSystemService("activity")).getRunningAppProcesses();
        Map<String, ProcessAppItem> runningApps = HsmCollections.newArrayMap();
        HsmPackageManager hsmPm = HsmPackageManager.getInstance();
        List<Integer> pidList = Lists.newArrayList();
        for (RunningAppProcessInfo appProcess : appProcessList) {
            String processName = appProcess.processName;
            if (TextUtils.isEmpty(processName)) {
                HwLog.w(TAG, "processName is empty!");
            } else if (appProcess.uid > 10000) {
                if (UserHandle.myUserId() != UserHandle.getUserId(appProcess.uid)) {
                    HwLog.d(TAG, "getRunningAppMap, not current user " + appProcess.processName + "  uid:" + appProcess.uid);
                } else {
                    String mainPkg = getMainPkgName(appProcess);
                    if (TextUtils.isEmpty(mainPkg)) {
                        HwLog.i(TAG, " main pkg name is empty, processName:" + processName);
                    } else {
                        ProcessAppItem appItem = (ProcessAppItem) runningApps.get(mainPkg);
                        if (appItem == null) {
                            HsmPkgInfo pkgInfo = hsmPm.getPkgInfo(mainPkg);
                            if (pkgInfo != null) {
                                appItem = new ProcessAppItem(pkgInfo);
                                runningApps.put(mainPkg, appItem);
                            }
                        }
                        if (appItem == null) {
                            HwLog.i(TAG, "can not find pkg:" + mainPkg + ",processName:" + processName);
                        } else {
                            appItem.addPid(appProcess.pid);
                            pidList.add(Integer.valueOf(appProcess.pid));
                        }
                    }
                }
            }
        }
        TempDataHolder dataHolder = new TempDataHolder();
        dataHolder.pidList = pidList;
        dataHolder.processList = Lists.newArrayList(runningApps.values());
        return dataHolder;
    }

    private static String getMainPkgName(RunningAppProcessInfo appProcess) {
        String processName = appProcess.processName;
        String[] pkgNameList = appProcess.pkgList;
        if (pkgNameList != null && pkgNameList.length > 0) {
            return pkgNameList[0];
        }
        HwLog.w(TAG, "pkgNameList is null! processName:" + processName);
        return "";
    }

    private static List<ProcessAppItem> queryAllRunningAppInfo(Context ctx) {
        HwLog.i(TAG, "begin to queryAllRunningAppInfo");
        TempDataHolder dataHolder = getRunningAppMap();
        Collection<ProcessAppItem> appCollection = Collections2.filter(dataHolder.processList, Predicates.and(new SystemIdPredicate(), new PersistentFlagPerdicate()));
        new CheckProtectedAppPredicate(ctx, true).executeTask();
        LauncherPredicate launcherPre = new LauncherPredicate(ctx, false);
        InputMethodPredicate inputPre = new InputMethodPredicate(ctx, false);
        WallPaperPredicate wallPre = new WallPaperPredicate(ctx, false);
        CustomKeyPredicate customPre = new CustomKeyPredicate(ctx);
        appCollection = Collections2.filter(Collections2.filter(appCollection, Predicates.and(protectedAppPre, launcherPre, inputPre, wallPre, customPre)), new CheckPredicate());
        MemoryPredicate memoryPre = new MemoryPredicate(ctx, dataHolder.pidList);
        memoryPre.executeTask();
        List<ProcessAppItem> appList = Lists.newArrayList(Collections2.filter(appCollection, memoryPre));
        Collections.sort(appList, PROCESS_APP_COMPARATOR);
        return appList;
    }

    private static boolean queryIfPkgAlive(String pkg) {
        long start = System.currentTimeMillis();
        for (RunningAppProcessInfo appProcess : ((ActivityManager) GlobalContext.getContext().getSystemService("activity")).getRunningAppProcesses()) {
            if (pkg.equals(getMainPkgName(appProcess))) {
                return true;
            }
        }
        HwLog.i(TAG, "queryIfPkgAlive cost:" + (System.currentTimeMillis() - start));
        return false;
    }

    public static List<ProcessAppItem> getProcessRecordList(Collection<STProcessRecord> runningList, List<String> removedList) {
        ProcessAppItem appItem;
        HsmPkgInfo pkgInfo;
        Map<String, ProcessAppItem> runningApps = HsmCollections.newArrayMap();
        HsmPackageManager hsmPm = HsmPackageManager.getInstance();
        for (STProcessRecord item : runningList) {
            if (TextUtils.isEmpty(item.processName)) {
                HwLog.w(TAG, "processName is empty!");
            } else {
                String mainPkg = getMainPkgName(item);
                if (TextUtils.isEmpty(mainPkg)) {
                    HwLog.i(TAG, " main pkg name is empty, processName:" + item.processName);
                } else {
                    appItem = (ProcessAppItem) runningApps.get(mainPkg);
                    if (appItem == null) {
                        pkgInfo = hsmPm.getPkgInfo(mainPkg);
                        if (pkgInfo != null) {
                            appItem = new ProcessAppItem(pkgInfo);
                            runningApps.put(mainPkg, appItem);
                        }
                    }
                    if (appItem == null) {
                        HwLog.i(TAG, "can not find pkg:" + mainPkg + ",processName:" + item.processName);
                    } else {
                        if (item.curAdj < appItem.getADJ()) {
                            appItem.setADJ(item.curAdj);
                        }
                        appItem.addPid(item.pid);
                    }
                }
            }
        }
        if (removedList != null) {
            for (String pkg : removedList) {
                if (runningApps.get(pkg) == null) {
                    pkgInfo = hsmPm.getPkgInfo(pkg);
                    if (pkgInfo != null) {
                        appItem = new ProcessAppItem(pkgInfo);
                        appItem.setADJ(100);
                        runningApps.put(pkg, appItem);
                    }
                }
            }
        }
        return Lists.newArrayList(runningApps.values());
    }

    public static List<String> getOnekeycleanPkg(TrimParam param, List<String> removePkg) {
        Collection<STProcessRecord> procList = HsmActivityManager.getInstance().getRunningList();
        if (procList == null) {
            procList = Collections.emptyList();
        }
        List<String> result = conver2ListPkg(Lists.newArrayList(Collections2.filter(getProcessRecordList(procList, removePkg), getOnekeyCleanPredicate(param.getContext(), param.isKeepForeground()))));
        HwLog.i(TAG, "getOnekeycleanPkg:" + result);
        return result;
    }

    public static List<ProcessAppItem> getpkgsAfterfilter(Context ctx, List<String> removePkg) {
        return Lists.newArrayList(Collections2.filter(convertPkgToAppItem(ctx, removePkg), getClearSpecifyPkgPre(ctx)));
    }

    private static List<ProcessAppItem> convertPkgToAppItem(Context ctx, List<String> removePkgs) {
        return Lists.newArrayList(Collections2.filter(Collections2.transform(removePkgs, new TransFuncPkgToItem()), Predicates.notNull()));
    }

    private static Predicate<ProcessAppItem> getOnekeyCleanPredicate(Context ctx, boolean keepPreTopTask) {
        ProtectControlledPredicate controlledPre = ProtectControlledPredicate.create(ctx, true);
        LauncherPredicate launcherPre = new LauncherPredicate(ctx, true);
        InputMethodPredicate inputPre = new InputMethodPredicate(ctx, true);
        WallPaperPredicate wallPre = new WallPaperPredicate(ctx, true);
        new VisibleWidgetPredicate().executeTask();
        new MusicPlayingPredicate(ctx).executeTask();
        CustomKeyPredicate customPre = new CustomKeyPredicate(ctx);
        SuperAppPredicate superAppPredicate = new SuperAppPredicate();
        SystemUILockPredicate systemUiLockerPre = SystemUILockPredicate.create(ctx);
        HwLog.i(TAG, "getOnekeyCleanPredicate, current is NOT abroad");
        Predicate<ProcessAppItem> pre = Predicates.and(controlledPre, superAppPredicate, launcherPre, inputPre, wallPre, vwPredicate, musicPredicate, customPre, systemUiLockerPre);
        if (!keepPreTopTask) {
            return pre;
        }
        KeepPreTopTaskPredicate keepPrePredicate = new KeepPreTopTaskPredicate(ctx);
        keepPrePredicate.executeTask();
        return Predicates.and(pre, keepPrePredicate);
    }

    private static Predicate<ProcessAppItem> getClearSpecifyPkgPre(Context ctx) {
        ProtectControlledPredicate controlledPredicate = ProtectControlledPredicate.create(ctx, false);
        LauncherPredicate launcherPre = new LauncherPredicate(ctx, true);
        InputMethodPredicate inputPre = new InputMethodPredicate(ctx, true);
        WallPaperPredicate wallPre = new WallPaperPredicate(ctx, true);
        new VisibleWidgetPredicate().executeTask();
        SuperAppPredicate superAppPredicate = new SuperAppPredicate();
        HwLog.i(TAG, "getClearSpecifyPkgPre current is NOT abroad");
        return Predicates.and(controlledPredicate, superAppPredicate, launcherPre, inputPre, wallPre, vwPredicate);
    }

    public static List<String> conver2ListPkg(List<ProcessAppItem> listsItem) {
        List<String> lists = Lists.newArrayListWithCapacity(listsItem.size());
        for (ProcessAppItem item : listsItem) {
            lists.add(item.getPackageName());
        }
        return lists;
    }

    private static String getMainPkgName(STProcessRecord appProcess) {
        String processName = appProcess.processName;
        HashSet<String> pkgNameList = appProcess.pkgList;
        if (pkgNameList != null && pkgNameList.size() > 0) {
            return pkgNameList.toArray()[0];
        }
        HwLog.w(TAG, "pkgNameList is null! processName:" + processName);
        return "";
    }
}
