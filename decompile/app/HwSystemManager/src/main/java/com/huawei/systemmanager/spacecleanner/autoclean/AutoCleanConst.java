package com.huawei.systemmanager.spacecleanner.autoclean;

import android.content.Context;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.systemmanager.comm.Storage.StorageHelper;
import com.huawei.systemmanager.emui.activities.HsmActivityHelper;
import com.huawei.systemmanager.power.model.BatteryStatisticsHelper;
import com.huawei.systemmanager.spacecleanner.SpaceCleannerManager;
import com.huawei.systemmanager.spacecleanner.autoclean.ScanType.AutoScan;
import com.huawei.systemmanager.spacecleanner.autoclean.ScanType.FileAnalysisScan;
import com.huawei.systemmanager.spacecleanner.autoclean.ScanType.SuggestCleanTrashScan;
import com.huawei.systemmanager.spacecleanner.autoclean.TrashAnalysis.AnalysisMode;
import com.huawei.systemmanager.spacecleanner.autoclean.TrashAnalysis.LargeFileAnalysisMode;
import com.huawei.systemmanager.spacecleanner.autoclean.TrashAnalysis.UnUsedAppAnalysisMode;
import com.huawei.systemmanager.spacecleanner.engine.trash.LargeFileTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.engine.trash.UnusedAppTrash;
import com.huawei.systemmanager.spacecleanner.setting.SpaceSettingPreference;
import com.huawei.systemmanager.spacecleanner.statistics.FileAnalysisInfo;
import com.huawei.systemmanager.spacecleanner.statistics.SpaceStatsUtils;
import com.huawei.systemmanager.spacecleanner.utils.FileTypeHelper;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public class AutoCleanConst {
    public static final long ANALYSIS_NOTIFY_INTERNAL_STORAGE_PERCENT = 10;
    public static final long ANALYSIS_NOTIFY_MIN_SIZE = 104857600;
    public static final long AUTOCLEAN_INTERVAL = 7200000;
    public static final long AUTOSCAN_TIME_OUT = 300;
    public static final long AUTOSCAN_WAIT_TIME = 5;
    public static final String KEY_LAST_CLEAN_TIME = "last_autoclean_time";
    public static final String KEY_LAST_FILE_ANALYSIS_TIME = "last_file_analysis_time";
    public static final String PREFRENCE = "space_prefence";
    public static final String PREFRENCE_SCAN = "space_prefence_scan";
    private static final String TAG = "AutoCleanConst";
    public static final long TWO_GB_SIZE = 2147483648L;
    private static long mAnalysisNotifySize = TWO_GB_SIZE;
    private static List<AutoScan> mScanList = new ArrayList();
    private static final Object sMutexAutoCleanConst = new Object();
    private static AutoCleanConst sSingleton;

    public static AutoCleanConst getInstance() {
        AutoCleanConst autoCleanConst;
        synchronized (sMutexAutoCleanConst) {
            if (sSingleton == null) {
                sSingleton = new AutoCleanConst();
                initScan();
            }
            autoCleanConst = sSingleton;
        }
        return autoCleanConst;
    }

    public static void destroyInstance() {
        synchronized (sMutexAutoCleanConst) {
            sSingleton = null;
        }
    }

    public static long getAnalysisNotifySize() {
        return mAnalysisNotifySize;
    }

    public static void initScan() {
        mScanList.clear();
        mScanList.add(new SuggestCleanTrashScan());
        SpaceCleannerManager.getInstance();
        if (SpaceCleannerManager.isSupportHwFileAnalysis()) {
            HwLog.i(TAG, "Not support hwFileAnalysis! ");
            List<AnalysisMode> mAnalysisModes = new ArrayList();
            mAnalysisModes.add(new LargeFileAnalysisMode());
            mAnalysisModes.add(new UnUsedAppAnalysisMode());
            mScanList.add(new FileAnalysisScan(mAnalysisModes));
            long preAnalysisSize = (10 * StorageHelper.getStorage().getTotalSize(0)) / 100;
            if (preAnalysisSize < ANALYSIS_NOTIFY_MIN_SIZE) {
                HwLog.e(TAG, "analysis size is too small.set it as min size;");
                preAnalysisSize = ANALYSIS_NOTIFY_MIN_SIZE;
            }
            mAnalysisNotifySize = Math.min(preAnalysisSize, TWO_GB_SIZE);
            return;
        }
        HwLog.i(TAG, "Support hwFileAnalysis! ");
    }

    public List<AnalysisMode> getAnalysisModes() {
        List<AnalysisMode> analysisModes = Lists.newArrayList();
        for (AutoScan autoScan : mScanList) {
            List<AnalysisMode> modes = autoScan.getAnalysisModes();
            if (autoScan.shouldAnalysis() && modes != null && modes.size() > 0) {
                analysisModes.addAll(modes);
            }
        }
        return analysisModes;
    }

    public int getAllTrashToScan(Context ctx) {
        int trashToScan = 0;
        for (AutoScan autoScan : mScanList) {
            int trash = autoScan.trashToScan();
            if (trash > 0 && autoScan.shouldStart(ctx)) {
                trashToScan |= trash;
            }
        }
        HwLog.i(TAG, "getAllTrashToScan trash:" + trashToScan);
        return trashToScan;
    }

    public int getShouldCleanTrashType() {
        int shouldCleanTrashType = 0;
        for (AutoScan autoScan : mScanList) {
            if (autoScan.shouldClean()) {
                int trash = autoScan.trashToScan();
                if (trash > 0) {
                    shouldCleanTrashType |= trash;
                }
            }
        }
        HwLog.i(TAG, "getShouldCleanTrashType trash:" + shouldCleanTrashType);
        return shouldCleanTrashType;
    }

    public int getShouldAnalysisTrashType() {
        int shouldAnalysisTrashType = 0;
        for (AutoScan autoScan : mScanList) {
            if (autoScan.shouldAnalysis()) {
                int trash = autoScan.trashToScan();
                if (trash > 0) {
                    shouldAnalysisTrashType |= trash;
                }
            }
        }
        HwLog.i(TAG, "getShouldAnalysisTrashType trash:" + shouldAnalysisTrashType);
        return shouldAnalysisTrashType;
    }

    public boolean checkScanCondition(Context ctx, List<AutoScan> scanList) {
        if (!getInstance().checkIfScreenOff(ctx)) {
            HwLog.i(TAG, "checkScanCondition, current is not screenoff, checkCondition failed");
            return false;
        } else if (getInstance().checkIfSuperPowerMode()) {
            HwLog.i(TAG, "checkScanCondition, current is super power mode, checkCondition failed");
            return false;
        } else {
            boolean isAllScanClose = true;
            for (AutoScan autoScan : scanList) {
                if (autoScan.shouldStart(ctx)) {
                    isAllScanClose = false;
                    break;
                }
            }
            if (!isAllScanClose) {
                return true;
            }
            HwLog.i(TAG, "checkScanCondition, all scan is closed, checkCondition failed");
            return false;
        }
    }

    public boolean checkCleanCondition(Context ctx, List<AutoScan> scanList) {
        if (!getInstance().checkIfScreenOff(ctx)) {
            HwLog.i(TAG, "checkCleanCondition, current is not screenoff, checkCondition failed");
            return false;
        } else if (getInstance().checkIfSuperPowerMode()) {
            HwLog.i(TAG, "checkCleanCondition, current is super power mode, checkCondition failed");
            return false;
        } else {
            boolean isAllShouldNotClean = true;
            for (AutoScan autoScan : scanList) {
                if (autoScan.shouldStart(ctx) && autoScan.shouldClean()) {
                    isAllShouldNotClean = false;
                    break;
                }
            }
            if (!isAllShouldNotClean) {
                return true;
            }
            HwLog.i(TAG, "checkCleanCondition, all scan show not clean, checkCondition failed");
            return false;
        }
    }

    public boolean checkAnalysisCondition(Context ctx, List<AutoScan> scanList) {
        if (!getInstance().checkIfScreenOff(ctx)) {
            HwLog.i(TAG, "checkAnalysisCondition, current is not screenoff, checkCondition failed");
            return false;
        } else if (getInstance().checkIfSuperPowerMode()) {
            HwLog.i(TAG, "checkAnalysisCondition, current is super power mode, checkCondition failed");
            return false;
        } else {
            boolean isAllShouldNotAnalysis = true;
            for (AutoScan autoScan : scanList) {
                if (autoScan.shouldAnalysis() && autoScan.shouldStart(ctx)) {
                    isAllShouldNotAnalysis = false;
                    break;
                }
            }
            if (!isAllShouldNotAnalysis) {
                return true;
            }
            HwLog.i(TAG, "checkAnalysisCondition, all scan show not analysis, checkCondition failed");
            return false;
        }
    }

    public void saveAutoCleanTime(Context ctx) {
        ctx.getSharedPreferences("space_prefence", 0).edit().putLong(KEY_LAST_CLEAN_TIME, System.currentTimeMillis()).commit();
    }

    public void saveFileAnaylysisTime(Context ctx) {
        ctx.getSharedPreferences("space_prefence", 0).edit().putLong(KEY_LAST_FILE_ANALYSIS_TIME, System.currentTimeMillis()).commit();
    }

    public List<AutoScan> getScanList() {
        return mScanList;
    }

    public boolean checkIfSuperPowerMode() {
        return SystemProperties.getBoolean("sys.super_power_save", false);
    }

    public boolean checkIfScreenOff(Context ctx) {
        return !((PowerManager) ctx.getSystemService(BatteryStatisticsHelper.DB_POWER)).isInteractive();
    }

    public boolean checkIfAutoCleanSwitchOn() {
        boolean canAutoClean = SpaceSettingPreference.getDefault().getCacheCleanSetting().isSwitchOn();
        HwLog.i(TAG, "auto clean switch on state:" + canAutoClean);
        return canAutoClean;
    }

    public boolean checkIfNotCommonlyUsedSwitchOn() {
        boolean canNotifyNotCommonlyUsed = SpaceSettingPreference.getDefault().getNotCommonlyUsedSetting().isSwitchOn();
        HwLog.i(TAG, "notify not commonly switch on state:" + canNotifyNotCommonlyUsed);
        return canNotifyNotCommonlyUsed;
    }

    public boolean checkIfUserAgreement(Context ctx) {
        boolean isUserAgreement = !HsmActivityHelper.checkShouldShowUserAgreement(ctx);
        HwLog.i(TAG, "is user agreement:" + isUserAgreement);
        return isUserAgreement;
    }

    public int getFreePercentInternal() {
        int freePercent = StorageHelper.getStorage().getFreePercent(0);
        if (freePercent < 0) {
            return 0;
        }
        return freePercent;
    }

    public void reportAnalysisResult(List<Trash> trashList, boolean shouldNotify) {
        String str;
        FileAnalysisInfo result = new FileAnalysisInfo();
        result.mTime = System.currentTimeMillis();
        if (shouldNotify) {
            str = "1";
        } else {
            str = "2";
        }
        result.mIsNotify = str;
        StringBuilder appPkgStrBuilder = new StringBuilder();
        for (Trash trash : trashList) {
            long trashSize = trash.getTrashSize();
            if (trash instanceof LargeFileTrash) {
                LargeFileTrash fileTrash = (LargeFileTrash) trash;
                result.mLargeFileSize += trashSize;
                result.mLargeFileNum++;
                switch (FileTypeHelper.getFileType(fileTrash.getPath())) {
                    case 1:
                        result.mLargeFileMusicSize += trashSize;
                        break;
                    case 2:
                        result.mLargeFileVideoSize += trashSize;
                        break;
                    case 3:
                        result.mLargeFilePhotoSize += trashSize;
                        break;
                    case 4:
                        result.mLargeFileApkSize += trashSize;
                        break;
                    case 5:
                        result.mLargeFileDocSize += trashSize;
                        break;
                    case 6:
                        result.mLargeFileArchivesSize += trashSize;
                        break;
                    case 7:
                        result.mLargeFileOtherSize += trashSize;
                        break;
                    default:
                        HwLog.e(TAG, "file type is error!");
                        break;
                }
            } else if (trash instanceof UnusedAppTrash) {
                UnusedAppTrash unusedApp = (UnusedAppTrash) trash;
                result.mAppNum++;
                result.mAppSize += trashSize;
                appPkgStrBuilder.append(unusedApp.getPackageName()).append(ConstValues.SEPARATOR_KEYWORDS_EN);
            }
        }
        result.mAppPkgNames = appPkgStrBuilder.toString();
        if (!TextUtils.isEmpty(result.mAppPkgNames)) {
            result.mAppPkgNames = "(" + result.mAppPkgNames.substring(0, result.mAppPkgNames.lastIndexOf(ConstValues.SEPARATOR_KEYWORDS_EN)) + ")";
        }
        String report = result.getReport();
        if (TextUtils.isEmpty(report)) {
            HwLog.e(TAG, "reportAnalysisResult report string is empty!:");
        } else {
            SpaceStatsUtils.reportFileAnalysisResult(report);
        }
    }
}
