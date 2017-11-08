package com.huawei.systemmanager.power.data.stats;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.BatteryStats.Uid;
import android.os.BatteryStats.Uid.Proc;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.ArrayMap;
import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatteryStatsHelper;
import com.android.internal.os.PowerProfile;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hsm.power.M2NAdapter;
import com.huawei.permissionmanager.db.DBHelper;
import com.huawei.systemmanager.optimize.process.Predicate.InputMethodPredicate;
import com.huawei.systemmanager.power.data.stats.UidAndPower.Cmp;
import com.huawei.systemmanager.power.data.xml.PowerWarningParam;
import com.huawei.systemmanager.power.util.AppRangeWrapper;
import com.huawei.systemmanager.power.util.L2MAdapter;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class PowerStatsHelper {
    private static final int BACKGROUND_CONSUME_STATSTYPE = 2;
    private static final int STATSTYPE = 0;
    public static final long STEP_LEVEL_TIME_MASK = 1099511627775L;
    private static final String TAG = PowerStatsHelper.class.getSimpleName();
    private static final int UNITPOWERINMAH = 3600000;
    private static BatteryStatsHelper mBatteryStatsHelper;

    public static PowerStatsHelper newInstance(Context ctx, boolean initBatteryHelper) {
        if (initBatteryHelper) {
            initAndCreateBatteryHelper(ctx);
        }
        return new PowerStatsHelper();
    }

    public static void reloadHelperStats() throws PowerStatsException {
        checkHelperValid();
        mBatteryStatsHelper.clearStats();
        mBatteryStatsHelper.getStats();
    }

    public long computeBatteryRealtime() throws PowerStatsException {
        checkHelperValid();
        return mBatteryStatsHelper.getStats().computeBatteryRealtime(SystemClock.elapsedRealtime() * 1000, 0);
    }

    public long computeBatteryRealtimeSinceUnplugged() throws PowerStatsException {
        checkHelperValid();
        return mBatteryStatsHelper.getStats().computeBatteryRealtime(SystemClock.elapsedRealtime() * 1000, 2);
    }

    public long computeTimePerLevel(Context ctx) {
        if (mBatteryStatsHelper == null) {
            return -1;
        }
        int numSteps = L2MAdapter.getDischargeStepDurations(mBatteryStatsHelper.getStats());
        long[] steps = L2MAdapter.getDischargeStepDurationsArray(mBatteryStatsHelper.getStats());
        if (numSteps <= 0) {
            return -1;
        }
        long total;
        if (numSteps == 1) {
            total = steps[0] & STEP_LEVEL_TIME_MASK;
        } else if (numSteps == 2) {
            total = ((steps[0] & STEP_LEVEL_TIME_MASK) + (steps[1] & STEP_LEVEL_TIME_MASK)) / 2;
        } else {
            int former = numSteps / 3;
            int middle = (numSteps * 2) / 3;
            long total_former = 0;
            long total_middle = 0;
            long total_latter = 0;
            for (int i = 0; i < former; i++) {
                total_former += steps[i] & STEP_LEVEL_TIME_MASK;
            }
            for (int j = former; j < middle; j++) {
                total_middle += steps[j] & STEP_LEVEL_TIME_MASK;
            }
            for (int k = middle; k < numSteps; k++) {
                total_latter += steps[k] & STEP_LEVEL_TIME_MASK;
            }
            total = ((total_former / (((long) former) * 2)) + ((3 * total_middle) / (((long) (middle - former)) * 10))) + (total_latter / (((long) (numSteps - middle)) * 5));
        }
        return total;
    }

    public List<UidAndPower> computerAppConsumption(Context ctx, boolean reload) throws PowerStatsException {
        Set<Integer> possibleIds = AppRangeWrapper.getAppThirdUidSet(ctx);
        List<BatterySipper> sipperList = getBatterySipperList(ctx, reload);
        List<UidAndPower> result = Lists.newArrayList();
        for (BatterySipper sipper : sipperList) {
            if (possibleIds.contains(Integer.valueOf(sipper.getUid()))) {
                result.add(new UidAndPower(sipper.getUid(), L2MAdapter.value(sipper), sipper));
                possibleIds.remove(Integer.valueOf(sipper.getUid()));
            }
        }
        for (Integer uid : possibleIds) {
            result.add(new UidAndPower(uid.intValue(), 0.0d, null));
        }
        adjustBackupgroundResult(result);
        return result;
    }

    public List<UidAndPower> computerBackgroundConsumption(Context ctx, boolean reload) throws PowerStatsException {
        Set<Integer> possibleIds = AppRangeWrapper.getRunningThirdUidSet(ctx);
        List<BatterySipper> sipperList = getBatterySipperList(ctx, reload, 2);
        List<UidAndPower> result = Lists.newArrayList();
        for (BatterySipper sipper : sipperList) {
            if (possibleIds.contains(Integer.valueOf(sipper.getUid()))) {
                result.add(new UidAndPower(sipper.getUid(), L2MAdapter.value(sipper), sipper));
                possibleIds.remove(Integer.valueOf(sipper.getUid()));
            }
        }
        for (Integer uid : possibleIds) {
            result.add(new UidAndPower(uid.intValue(), 0.0d, null));
        }
        adjustBackupgroundResult(result);
        return result;
    }

    private void adjustBackupgroundResult(List<UidAndPower> uidPowers) {
        for (UidAndPower uidPower : uidPowers) {
            if (uidPower.getSipper() != null) {
                int cluster;
                int speed;
                Uid u = uidPower.getSipper().uidObj;
                long totalTime = 0;
                int numClusters = M2NAdapter.getNumCpuClusters(mBatteryStatsHelper.getPowerProfile());
                for (cluster = 0; cluster < numClusters; cluster++) {
                    for (speed = 0; speed < M2NAdapter.getNumSpeedStepsInCpuCluster(mBatteryStatsHelper.getPowerProfile(), cluster); speed++) {
                        totalTime += u.getTimeAtCpuSpeed(cluster, speed, 2);
                    }
                }
                totalTime = Math.max(totalTime, 1);
                long cpuTimeMs = (u.getUserCpuTimeUs(2) + u.getSystemCpuTimeUs(2)) / 1000;
                double cpuPowerMaMs = 0.0d;
                double minAveragePowerForCpu = 0.0d;
                for (cluster = 0; cluster < numClusters; cluster++) {
                    int speedsForCluster = M2NAdapter.getNumSpeedStepsInCpuCluster(mBatteryStatsHelper.getPowerProfile(), cluster);
                    speed = 0;
                    while (speed < speedsForCluster) {
                        double cpuSpeedStepPower = (((double) cpuTimeMs) * (((double) u.getTimeAtCpuSpeed(cluster, speed, 2)) / ((double) totalTime))) * M2NAdapter.getAveragePowerForCpu(mBatteryStatsHelper.getPowerProfile(), cluster, speed);
                        if (cluster == 0 && speed == 0) {
                            minAveragePowerForCpu = M2NAdapter.getAveragePowerForCpu(mBatteryStatsHelper.getPowerProfile(), cluster, speed);
                            HwLog.i(TAG, "minAveragePowerForCpu= " + minAveragePowerForCpu);
                        }
                        cpuPowerMaMs += cpuSpeedStepPower;
                        speed++;
                    }
                }
                double cpuPowerMah = cpuPowerMaMs / 3600000.0d;
                if (!(cpuTimeMs == 0 && cpuPowerMah == 0.0d)) {
                    HwLog.i(TAG, "UID " + u.getUid() + ": CPU time=" + cpuTimeMs + " ms power=" + makemAh(cpuPowerMah));
                }
                long cpuFgTimeMs = 0;
                ArrayMap<String, ? extends Proc> processStats = u.getProcessStats();
                for (int i = 0; i < processStats.size(); i++) {
                    cpuFgTimeMs += ((Proc) processStats.valueAt(i)).getForegroundTime(2);
                }
                if (cpuFgTimeMs > cpuTimeMs) {
                    if (cpuFgTimeMs > DBHelper.HISTORY_MAX_SIZE + cpuTimeMs) {
                        HwLog.e(TAG, "WARNING! Cputime is more than 10 seconds behind Foreground time");
                    }
                    cpuTimeMs = cpuFgTimeMs;
                }
                double bgcpuPowerMah = (((double) (cpuTimeMs - cpuFgTimeMs)) * minAveragePowerForCpu) / 3600000.0d;
                HwLog.i(TAG, " adjustBackupgroundResult before origin uid: " + uidPower.getUid() + ", power: " + uidPower.getPower() + ", in sipper: " + L2MAdapter.value(uidPower.getSipper()) + ", cpuPowerMah: " + cpuPowerMah + ", bgcpuPowerMah: " + bgcpuPowerMah + ", cpuTime: " + L2MAdapter.cpuTime(uidPower.getSipper()) + ", fgCpuTime: " + L2MAdapter.cpuFgTime(uidPower.getSipper()));
                uidPower.setPower(uidPower.getPower() - (cpuPowerMah - bgcpuPowerMah));
                L2MAdapter.setValue(uidPower.getSipper(), uidPower.getPower());
                L2MAdapter.setCpuTime(uidPower.getSipper(), L2MAdapter.cpuTime(uidPower.getSipper()) - L2MAdapter.cpuFgTime(uidPower.getSipper()));
                L2MAdapter.setCpuFgTime(uidPower.getSipper(), 0);
                HwLog.i(TAG, " adjustBackupgroundResult after origin uid: " + uidPower.getUid() + ", power: " + uidPower.getPower() + ", in sipper: " + L2MAdapter.value(uidPower.getSipper()) + ", cpuPowerMah: " + cpuPowerMah + ", bgcpuPowerMah: " + bgcpuPowerMah + ", cpuTime: " + L2MAdapter.cpuTime(uidPower.getSipper()) + ", fgCpuTime: " + L2MAdapter.cpuFgTime(uidPower.getSipper()));
            }
        }
    }

    public List<UidAndPower> getPowerAppList(Context ctx, boolean reload) {
        try {
            long appShowLevel = (long) PowerWarningParam.getApp_show_level(ctx);
            List<UidAndPower> result = computerBackgroundConsumption(ctx, reload);
            Set<Integer> filteroutUids = getFilteroutUids(ctx);
            Iterator<UidAndPower> iterator = result.iterator();
            while (iterator.hasNext()) {
                UidAndPower uap = (UidAndPower) iterator.next();
                if (uap.getPower() < ((double) appShowLevel) || filteroutUids.contains(Integer.valueOf(uap.getUid()))) {
                    iterator.remove();
                }
            }
            return result;
        } catch (PowerStatsException ex) {
            HwLog.e(TAG, "getPowerAppList catch PowerStatsException: " + ex.getMessage());
            ex.printStackTrace();
            return Lists.newArrayList();
        } catch (Exception ex2) {
            HwLog.e(TAG, "getPowerAppList catch Exception: " + ex2.getMessage());
            ex2.printStackTrace();
            return Lists.newArrayList();
        }
    }

    public List<BatterySipper> computeSwAndHwConsumption(Context ctx, boolean reload) throws PowerStatsException {
        return getBatterySipperList(ctx, reload);
    }

    public long getScreenOnTime(long rawRealTime) {
        return (mBatteryStatsHelper.getStats().getScreenOnTime(rawRealTime, 0) / 1000) / 1000;
    }

    public long getPhoneOnTime(long rawRealTime) {
        return (mBatteryStatsHelper.getStats().getPhoneOnTime(rawRealTime, 0) / 1000) / 1000;
    }

    public long getIdleTime(long rawRealTime) {
        return ((mBatteryStatsHelper.getStats().computeBatteryRealtime(rawRealTime, 0) - mBatteryStatsHelper.getStats().getScreenOnTime(rawRealTime, 0)) / 1000) / 1000;
    }

    public long getRadioTime(long rawRealTime) {
        long signalTimeMs = 0;
        for (int i = 0; i < 5; i++) {
            signalTimeMs += mBatteryStatsHelper.getStats().getPhoneSignalStrengthTime(i, rawRealTime, 0) / 1000;
        }
        return signalTimeMs / 1000;
    }

    public long getRadioNoSignleTime(long rawRealTime) {
        return (mBatteryStatsHelper.getStats().getPhoneSignalStrengthTime(0, rawRealTime, 0) / 1000) / 1000;
    }

    public long getRadioScanningTime(long rawRealTime) {
        return (mBatteryStatsHelper.getStats().getPhoneSignalScanningTime(rawRealTime, 0) / 1000) / 1000;
    }

    public long getWifiOnTime(long rawRealTime) {
        return (mBatteryStatsHelper.getStats().getWifiOnTime(rawRealTime, 0) / 1000) / 1000;
    }

    public long getBluetoothOnTime(long rawRealTime) {
        return (L2MAdapter.getBluetoothOnTime(mBatteryStatsHelper.getStats(), rawRealTime, 0) / 1000) / 1000;
    }

    public Map<String, Double> getPackageNameAndPower(Context ctx, Uid u) {
        Map<String, Double> procPowerMap = new HashMap();
        Map<String, ? extends Proc> processStats = u.getProcessStats();
        if (processStats.size() > 0) {
            int cluster;
            int speed;
            long totalTime = 0;
            PowerProfile powerProfile = new PowerProfile(ctx);
            int numClusters = M2NAdapter.getNumCpuClusters(powerProfile);
            for (cluster = 0; cluster < numClusters; cluster++) {
                for (speed = 0; speed < M2NAdapter.getNumSpeedStepsInCpuCluster(powerProfile, cluster); speed++) {
                    totalTime += u.getTimeAtCpuSpeed(cluster, speed, 0);
                }
            }
            totalTime = Math.max(totalTime, 1);
            long cpuTime = 0;
            for (Entry<String, ? extends Proc> ent : processStats.entrySet()) {
                Proc ps = (Proc) ent.getValue();
                long tmpCpuTime = ps.getUserTime(0) + ps.getSystemTime(0);
                double processPower = 0.0d;
                for (cluster = 0; cluster < numClusters; cluster++) {
                    int speedsForCluster = M2NAdapter.getNumSpeedStepsInCpuCluster(powerProfile, cluster);
                    for (speed = 0; speed < speedsForCluster; speed++) {
                        double ratio = ((double) u.getTimeAtCpuSpeed(cluster, speed, 0)) / ((double) totalTime);
                        double cpuSpeedStepPower = (((double) tmpCpuTime) * ratio) * M2NAdapter.getAveragePowerForCpu(powerProfile, cluster, speed);
                        if (ratio != 0.0d) {
                            HwLog.i(TAG, "UID " + u.getUid() + ": CPU cluster #" + cluster + " step #" + speed + " ratio=" + makemAh(ratio) + " power=" + makemAh(cpuSpeedStepPower / 3600000.0d));
                        }
                        processPower += cpuSpeedStepPower;
                    }
                }
                cpuTime += tmpCpuTime;
                if (processPower != 0.0d) {
                    processPower /= 3600000.0d;
                }
                String procName = (String) ent.getKey();
                if (procName.equals("surfaceflinger") || procName.equals("servicemanager")) {
                    procName = "android:" + procName;
                } else if ("system".equals(procName)) {
                    procName = "android";
                }
                HwLog.i(TAG, "SharedUid: procName = " + procName + " processPower = " + processPower + "cputime = " + cpuTime);
                procPowerMap.put(procName, Double.valueOf(processPower));
            }
            statisticSystemUsage(procPowerMap);
        }
        return procPowerMap;
    }

    public Map<String, Double> getBackgroundPackageNameAndPower(Context ctx, Uid u) {
        Map<String, Double> procPowerMap = new HashMap();
        Map<String, ? extends Proc> processStats = u.getProcessStats();
        if (processStats.size() > 0) {
            int cluster;
            int speed;
            long totalTime = 0;
            int numClusters = M2NAdapter.getNumCpuClusters(mBatteryStatsHelper.getPowerProfile());
            for (cluster = 0; cluster < numClusters; cluster++) {
                for (speed = 0; speed < M2NAdapter.getNumSpeedStepsInCpuCluster(mBatteryStatsHelper.getPowerProfile(), cluster); speed++) {
                    totalTime += u.getTimeAtCpuSpeed(cluster, speed, 2);
                }
            }
            totalTime = Math.max(totalTime, 1);
            long cpuTime = 0;
            for (Entry<String, ? extends Proc> ent : processStats.entrySet()) {
                Proc ps = (Proc) ent.getValue();
                long userTime = ps.getUserTime(2);
                long systemTime = ps.getSystemTime(2);
                long cpuFgTimeMs = ps.getForegroundTime(2);
                long tmpCpuTime = userTime + systemTime;
                long cpuTimeMs = (u.getUserCpuTimeUs(2) + u.getSystemCpuTimeUs(2)) / 1000;
                double processPower = 0.0d;
                double minAveragePowerForCpu = 0.0d;
                for (cluster = 0; cluster < numClusters; cluster++) {
                    int speedsForCluster = M2NAdapter.getNumSpeedStepsInCpuCluster(mBatteryStatsHelper.getPowerProfile(), cluster);
                    speed = 0;
                    while (speed < speedsForCluster) {
                        double ratio = ((double) u.getTimeAtCpuSpeed(cluster, speed, 2)) / ((double) totalTime);
                        double cpuSpeedStepPower = (((double) cpuTimeMs) * ratio) * M2NAdapter.getAveragePowerForCpu(mBatteryStatsHelper.getPowerProfile(), cluster, speed);
                        if (cluster == 0 && speed == 0) {
                            minAveragePowerForCpu = M2NAdapter.getAveragePowerForCpu(mBatteryStatsHelper.getPowerProfile(), cluster, speed);
                            HwLog.i(TAG, "Bg minAveragePowerForCpu= " + minAveragePowerForCpu);
                        }
                        if (ratio != 0.0d) {
                            HwLog.i(TAG, "UID " + u.getUid() + ": CPU cluster #" + cluster + " step #" + speed + " ratio=" + makemAh(ratio) + " power=" + makemAh(cpuSpeedStepPower / 3600000.0d));
                        }
                        processPower += cpuSpeedStepPower;
                        speed++;
                    }
                }
                if (cpuFgTimeMs > tmpCpuTime) {
                    if (cpuFgTimeMs > DBHelper.HISTORY_MAX_SIZE + tmpCpuTime) {
                        HwLog.i(TAG, " WARNING! Bg this app's Cputime is more than 10 seconds behind Foreground time");
                    }
                    tmpCpuTime = cpuFgTimeMs;
                }
                long cpuBgTimeMs = tmpCpuTime - cpuFgTimeMs;
                if (cpuBgTimeMs > 0) {
                    HwLog.i(TAG, " UID " + u.getUid() + "CBG " + cpuBgTimeMs + " ms");
                }
                processPower /= 3600000.0d;
                processPower -= processPower - ((((double) cpuBgTimeMs) * minAveragePowerForCpu) / 3600000.0d);
                cpuTime += tmpCpuTime;
                String procName = (String) ent.getKey();
                if (procName.equals("surfaceflinger") || procName.equals("servicemanager")) {
                    procName = "android:" + procName;
                } else if ("system".equals(procName)) {
                    procName = "android";
                }
                HwLog.i(TAG, "Background SharedUid: procName = " + procName + " processPower = " + processPower + "cputime = " + cpuTime + " tmpCpuTime = " + tmpCpuTime + " bgcputime = " + cpuBgTimeMs);
                procPowerMap.put(procName, Double.valueOf(processPower));
            }
            statisticSystemUsage(procPowerMap);
        }
        return procPowerMap;
    }

    private void statisticSystemUsage(Map<String, Double> procPowerMap) {
        Iterator<Entry<String, Double>> iter = procPowerMap.entrySet().iterator();
        HwLog.d(TAG, "procPowerMap below = " + procPowerMap);
        while (iter.hasNext()) {
            Entry ent = (Entry) iter.next();
            String proc = (String) ent.getKey();
            Double bsm = (Double) ent.getValue();
            if (proc != null) {
                String pkg = proc.split(":")[0];
                if (pkg.equals(proc) || !procPowerMap.containsKey(pkg)) {
                    HwLog.d(TAG, "shared uid: name = " + proc + " power = " + bsm);
                } else {
                    HwLog.d(TAG, "bs = " + Double.valueOf(((Double) procPowerMap.get(pkg)).doubleValue() + bsm.doubleValue()));
                    iter.remove();
                }
            }
        }
        HwLog.d(TAG, "procPowerMap" + procPowerMap);
    }

    private static String makemAh(double power) {
        if (power < 1.0E-5d) {
            return String.format("%.8f", new Object[]{Double.valueOf(power)});
        } else if (power < 1.0E-4d) {
            return String.format("%.7f", new Object[]{Double.valueOf(power)});
        } else if (power < 0.001d) {
            return String.format("%.6f", new Object[]{Double.valueOf(power)});
        } else if (power < 0.01d) {
            return String.format("%.5f", new Object[]{Double.valueOf(power)});
        } else if (power < 0.1d) {
            return String.format("%.4f", new Object[]{Double.valueOf(power)});
        } else if (power < 1.0d) {
            return String.format("%.3f", new Object[]{Double.valueOf(power)});
        } else if (power < 10.0d) {
            return String.format("%.2f", new Object[]{Double.valueOf(power)});
        } else if (power < 100.0d) {
            return String.format("%.1f", new Object[]{Double.valueOf(power)});
        } else {
            return String.format("%.0f", new Object[]{Double.valueOf(power)});
        }
    }

    private List<BatterySipper> getBatterySipperList(Context ctx, boolean reload) throws PowerStatsException {
        checkHelperValid();
        synchronized (mBatteryStatsHelper) {
            if (reload) {
                mBatteryStatsHelper.clearStats();
            }
            List<UserHandle> userProfiles = getUserProfiles(ctx);
            if (userProfiles == null) {
                List arrayList = new ArrayList();
                return arrayList;
            }
            mBatteryStatsHelper.refreshStats(0, userProfiles);
            arrayList = Lists.newArrayList(mBatteryStatsHelper.getUsageList());
            return arrayList;
        }
    }

    private List<BatterySipper> getBatterySipperList(Context ctx, boolean reload, int statstype) throws PowerStatsException {
        checkHelperValid();
        synchronized (mBatteryStatsHelper) {
            if (reload) {
                mBatteryStatsHelper.clearStats();
            }
            List<UserHandle> userProfiles = getUserProfiles(ctx);
            if (userProfiles == null) {
                List arrayList = new ArrayList();
                return arrayList;
            }
            mBatteryStatsHelper.refreshStats(statstype, userProfiles);
            arrayList = Lists.newArrayList(mBatteryStatsHelper.getUsageList());
            return arrayList;
        }
    }

    private static void initAndCreateBatteryHelper(Context ctx) {
        synchronized (PowerStatsHelper.class) {
            if (mBatteryStatsHelper == null) {
                mBatteryStatsHelper = new BatteryStatsHelper(ctx, true);
                mBatteryStatsHelper.create((Bundle) null);
            }
        }
    }

    private static void checkHelperValid() throws PowerStatsException {
        if (mBatteryStatsHelper == null) {
            throw new PowerStatsException("Call newBatteryStatsHelper before calling other functions!");
        }
    }

    private static List<UserHandle> getUserProfiles(Context ctx) {
        return ((UserManager) ctx.getSystemService("user")).getUserProfiles();
    }

    private Set<Integer> getFilteroutUids(Context ctx) {
        Set<Integer> uids = Sets.newHashSet();
        List<String> inputPkgs = InputMethodPredicate.getInputMethod(ctx);
        PackageManager pm = ctx.getPackageManager();
        for (String pkg : inputPkgs) {
            try {
                ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
                if (info != null) {
                    uids.add(Integer.valueOf(info.uid));
                }
            } catch (NameNotFoundException ex) {
                HwLog.e(TAG, "getFilteroutUids catch NameNotFoundException: " + ex.getMessage());
            }
        }
        HwLog.d(TAG, "getFilteroutUids pkgs: " + inputPkgs + ", uids: " + uids);
        return uids;
    }

    public List<UidAndPower> getTopHighPowerApps(Context ctx, boolean reload) throws PowerStatsException {
        Set<Integer> possibleIds = AppRangeWrapper.getAppThirdUidSet(ctx);
        List<BatterySipper> sipperList = getBatterySipperList(ctx, reload);
        List<UidAndPower> result = Lists.newArrayList();
        for (BatterySipper sipper : sipperList) {
            if (possibleIds.contains(Integer.valueOf(sipper.getUid()))) {
                result.add(new UidAndPower(sipper.getUid(), L2MAdapter.value(sipper), sipper));
                possibleIds.remove(Integer.valueOf(sipper.getUid()));
            }
        }
        for (Integer uid : possibleIds) {
            result.add(new UidAndPower(uid.intValue(), 0.0d, null));
        }
        Collections.sort(result, new Cmp());
        return result;
    }
}
