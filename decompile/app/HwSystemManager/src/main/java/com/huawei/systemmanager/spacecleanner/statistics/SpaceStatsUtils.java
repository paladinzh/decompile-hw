package com.huawei.systemmanager.spacecleanner.statistics;

import android.util.SparseArray;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.systemmanager.comm.Storage.StorageHelper;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.engine.tencentadapter.TencenAppGroup;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.engine.trash.TrashGroup;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.TrashItemGroup;
import com.huawei.systemmanager.spacecleanner.ui.normalscan.NormalCovertor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpaceStatsUtils {
    public static final int FROM_CLEAN_END_ENTER = 1;
    public static final int FROM_SCAN_END_ENTER = 2;
    public static final int FROM_TITLE_BTN_ENTER = 0;
    public static final int FUNCTION_CLOSE_OP = 0;
    public static final int FUNCTION_OPEN_OP = 1;
    private static final String PARAM_COUNT = "COUNT";
    private static final String PARAM_SIZE = "SIZE";
    private static final String PARAM_TYPE = "TYPE";
    private static final String PARAM_VALUE = "VALUE";
    private static final int TYPE_APK = 4;
    private static final int TYPE_AUDIO = 3;
    private static final int TYPE_PHOTO = 1;
    private static final int TYPE_VIDEO = 2;

    public static void reportFileNumAndSize(TrashScanHandler handler) {
        Map<Integer, TrashGroup> results = handler.getAllTrashes();
        innerReportFileNumAndSize((TrashGroup) results.get(Integer.valueOf(128)), 1);
        innerReportFileNumAndSize((TrashGroup) results.get(Integer.valueOf(256)), 2);
        innerReportFileNumAndSize((TrashGroup) results.get(Integer.valueOf(512)), 3);
        innerReportFileNumAndSize((TrashGroup) results.get(Integer.valueOf(1024)), 4);
    }

    public static void reportTrashScanResult(TrashScanHandler handler, TrashInfoBuilder builder) {
        Map<Integer, TrashGroup> results = handler.getAllTrashes();
        setDeviceStorage(handler, builder);
        setTotalTrashCountAndSize(results, builder);
        setSuggestTrashCountAndSize(handler.getNormalTrashes(), builder);
        HsmStat.statE((int) Events.E_OPTMIZE_REPORT_TRASH_SCAN_RESULT, builder.toString());
    }

    private static void setSuggestTrashCountAndSize(Map<Integer, TrashGroup> results, TrashInfoBuilder builder) {
        long size = 0;
        int count = 0;
        for (TrashItemGroup<ITrashItem> itemGroup : new NormalCovertor().getScanEndList(results)) {
            for (ITrashItem child : itemGroup) {
                if (!(!child.isChecked() || child.isCleaned() || child.getTrashType() == 32768)) {
                    count += child.getTrashCount();
                    size += child.getTrashSize();
                }
            }
        }
        builder.setSuggestTrashCount(count);
        builder.setSuggestTrashSize(size);
    }

    public static void reportTrashScanMaxFileLimit() {
        HsmStat.statE(Events.E_OPTMIZE_REPORT_TRASH_SCAN_MAX_LIMIT);
    }

    public static void reportEnterDeepManagerNums(int from) {
        String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, String.valueOf(from));
        HsmStat.statE((int) Events.E_OPTMIZE_REPORT_ENTER_DEEP_MANAGER, statParam);
    }

    public static void reportFromDeepItemEnterence(int from) {
        String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, String.valueOf(from));
        HsmStat.statE((int) Events.E_OPTMIZE_REPORT_FROM_DEEPITEM_ENTERENCE, statParam);
    }

    public static void reportDeepItemEnterenceClick(int DeepItemType) {
        String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, String.valueOf(DeepItemType));
        HsmStat.statE((int) Events.E_OPTMIZE_REPORT_DEEPITEM_CLICK_OP, statParam);
    }

    public static void reportDownloadSortDialogClick(int clickOp) {
        String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, String.valueOf(clickOp));
        HsmStat.statE((int) Events.E_OPTMIZE_REPORT_DOWNLOAD_SORT_DIALOG_CLICK_OP, statParam);
    }

    public static void reportDownloadDeleteFileOp(int OrderType, HashMap<String, Integer> map) {
        if (map != null && map.size() != 0) {
            String statParam = HsmStatConst.constructJsonParams(PARAM_TYPE, String.valueOf(OrderType), PARAM_VALUE, HsmStatConst.hashMapToJson(map));
            HsmStat.statE((int) Events.E_OPTMIZE_REPORT_DOWNLOAD_DELETE_FILE_OP, statParam);
        }
    }

    public static void reportDownloadDetailDialogClick(int fileType, int clickOp) {
        String statParam = HsmStatConst.constructJsonParams(PARAM_TYPE, String.valueOf(fileType), HsmStatConst.PARAM_OP, String.valueOf(clickOp));
        HsmStat.statE((int) Events.E_OPTMIZE_REPORT_DOWNLOAD_DETAIL_OP, statParam);
    }

    public static void reportAllSelectClick(boolean selectOp) {
        String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, String.valueOf(selectOp));
        HsmStat.statE((int) Events.E_OPTMIZE_REPORT_ALL_SELECT_OP, statParam);
    }

    public static void reportInternalFreePercent(int percent) {
        String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_FREEPERCENT, String.valueOf(percent));
        HsmStat.statE((int) Events.E_OPTMIZE_REPORT_INTERNAL_FREE_PERCENT, statParam);
    }

    public static void reportLowMemFirstOP(int type) {
        String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, String.valueOf(type));
        HsmStat.statE((int) Events.E_OPTMIZE_REPORT_LOW_MEM_FIRST_OP, statParam);
    }

    public static void reportLowMemOP(int type) {
        String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, String.valueOf(type));
        HsmStat.statE((int) Events.E_OPTMIZE_REPORT_LOW_MEM_OP, statParam);
    }

    public static void reportDeepCleanTrashSize(int type, long trashSize) {
        String statParam = HsmStatConst.constructJsonParams(PARAM_TYPE, String.valueOf(type), PARAM_SIZE, String.valueOf(trashSize));
        HsmStat.statE((int) Events.E_OPTMIZE_REPORT_DEEP_CLEAN_TRASH_SIZE, statParam);
    }

    public static void reportAutoCleanTrashSize(List<Trash> list) {
        SparseArray<TrashGroup> autoCleanTrashInfo = new SparseArray();
        StringBuilder statParam = new StringBuilder();
        for (Trash trash : list) {
            int type = trash.getType();
            TrashGroup trashGroup = (TrashGroup) autoCleanTrashInfo.get(type);
            if (trashGroup == null) {
                trashGroup = new TrashGroup(type);
            }
            trashGroup.addChild(trash);
            autoCleanTrashInfo.put(type, trashGroup);
        }
        int trashQuantum = autoCleanTrashInfo.size();
        for (int index = 0; index < trashQuantum; index++) {
            if (((TrashGroup) autoCleanTrashInfo.get(autoCleanTrashInfo.keyAt(index))).getTrashSize() != 0) {
                statParam.append(HsmStatConst.constructJsonParams(PARAM_TYPE, String.valueOf(autoCleanTrashInfo.keyAt(index)), PARAM_SIZE, String.valueOf(((TrashGroup) autoCleanTrashInfo.get(autoCleanTrashInfo.keyAt(index))).getTrashSize()))).append(ConstValues.SEPARATOR_KEYWORDS_EN);
            }
        }
        if (statParam.toString().lastIndexOf(ConstValues.SEPARATOR_KEYWORDS_EN) > 0) {
            HsmStat.statE((int) Events.E_OPTMIZE_REPORT_AUTO_CLEAN_TRASH_SIZE, statParam.toString().substring(0, statParam.toString().lastIndexOf(ConstValues.SEPARATOR_KEYWORDS_EN)));
        }
    }

    public static void reportAutoCleanServiceTriggedOp() {
        HsmStat.statE(Events.E_OPTMIZE_REPORT_AUTO_CLEAN_SERVICE_TRIGGED);
    }

    public static void reportAutoCleanTaskStartOp() {
        HsmStat.statE(Events.E_OPTMIZE_REPORT_AUTO_CLEAN_TASK_START);
    }

    public static void reportLowerMemTipsEntranceOp() {
        HsmStat.statE(Events.E_OPTMIZE_REPORT_LOW_MEM_TIPS_ENTRANCE_OP);
    }

    public static void reportSpaceManagerEntranceFromLowMemTipsOp() {
        HsmStat.statE(Events.E_OPTMIZE_REPORT_ENTER_SPACE_MANAGER_FROM_LOW_MEM_TIPS);
    }

    public static void reportSpaceManagerTrashSizeFromLowMemTips(SparseArray<Long> trashInfo) {
        StringBuilder statParam = new StringBuilder();
        int trashQuantum = trashInfo.size();
        for (int index = 0; index < trashQuantum; index++) {
            if (((Long) trashInfo.get(trashInfo.keyAt(index))).longValue() != 0) {
                statParam.append(HsmStatConst.constructJsonParams(PARAM_TYPE, String.valueOf(trashInfo.keyAt(index)), PARAM_SIZE, String.valueOf(((Long) trashInfo.get(trashInfo.keyAt(index))).longValue()))).append(ConstValues.SEPARATOR_KEYWORDS_EN);
            }
        }
        String statParamString = statParam.toString();
        statParamString = statParamString.substring(0, statParamString.lastIndexOf(ConstValues.SEPARATOR_KEYWORDS_EN));
        HsmStat.statE((int) Events.E_OPTMIZE_REPORT_SPACE_MANAGER_TRASH_SIZE_FROM_LOW_MEM_TIPS, statParamString);
    }

    public static void reportAutoCleanRunResult(AutoCleanInfo info) {
        HsmStat.statE((int) Events.E_OPTMIZE_REPORT_AUTO_CLEAN_RESULT, info.toString());
    }

    public static void reportMaxStorageTopApp(TrashScanHandler handler) {
        TrashGroup group = (TrashGroup) handler.getAllTrashes().get(Integer.valueOf(81920));
        if (group != null) {
            TencenAppGroup maxStorageApp = null;
            for (Trash trash : group.getTrashList()) {
                if (trash instanceof TencenAppGroup) {
                    TencenAppGroup app = (TencenAppGroup) trash;
                    if (maxStorageApp == null) {
                        maxStorageApp = app;
                    } else if (maxStorageApp.getTrashSize() < app.getTrashSize()) {
                        maxStorageApp = app;
                    }
                }
            }
            if (maxStorageApp != null) {
                String pkg = maxStorageApp.getPackageName();
                String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_PKG, pkg);
                HsmStat.statE((int) Events.E_OPTMIZE_REPORT_MAX_STORAGE_APP, statParam);
            }
        }
    }

    public static void reportOneKeyCleanTrash(List<Trash> trashs) {
        long innerCache = 0;
        long uninstallData = 0;
        long apkFileData = 0;
        long apkCustomData = 0;
        for (Trash trash : trashs) {
            int type = trash.getType();
            if (1 == type) {
                innerCache += trash.getTrashSize();
            } else if (8192 == type) {
                uninstallData += trash.getTrashSize();
            } else if (1024 == type) {
                apkFileData += trash.getTrashSize();
            } else if (16384 == type) {
                apkCustomData += trash.getTrashSize();
            }
        }
        innerReportOneKeyCleanTrash(innerCache, 0);
        innerReportOneKeyCleanTrash(uninstallData, 1);
        innerReportOneKeyCleanTrash(apkFileData, 2);
        innerReportOneKeyCleanTrash(apkCustomData, 3);
    }

    public static void reportStopSpaceScanOp(String path) {
        String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, path);
        HsmStat.statE((int) Events.E_OPTMIZE_REPORT_STOP_SPACE_SCAN_OP, statParam);
    }

    public static void reportExpandSpaceScanItemOp(int itemTrashType, String expandedFlag) {
        String statParam = HsmStatConst.constructJsonParams(PARAM_TYPE, String.valueOf(itemTrashType), HsmStatConst.PARAM_OP, expandedFlag);
        HsmStat.statE((int) Events.E_OPTMIZE_REPORT_EXPAND_SPACE_SCAN_ITEM_OP, statParam);
    }

    public static void reportOneKeyCleanPkgInfo(int trashType, String pkgName, boolean isChecked) {
        String statParam = HsmStatConst.constructJsonParams(PARAM_TYPE, String.valueOf(trashType), HsmStatConst.PARAM_PKG, pkgName, HsmStatConst.PARAM_OP, String.valueOf(isChecked));
        HsmStat.statE((int) Events.E_OPTMIZE_REPORT_ONE_KEY_CLEAN_PKG_INFO, statParam);
    }

    public static void reportOneKeyCleanTrashItem(int trashType, boolean isChecked) {
        String statParam = HsmStatConst.constructJsonParams(PARAM_TYPE, String.valueOf(trashType), HsmStatConst.PARAM_OP, String.valueOf(isChecked));
        HsmStat.statE((int) Events.E_OPTMIZE_REPORT_ONE_KEY_CLEAN_TRASH_ITEM, statParam);
    }

    public static void reportSpaceScanFileDetail(int trashType, String pkgName) {
        String statParam = HsmStatConst.constructJsonParams(PARAM_TYPE, String.valueOf(trashType), HsmStatConst.PARAM_PKG, pkgName);
        HsmStat.statE((int) Events.E_OPTMIZE_REPORT_SPACE_SCAN_FILE_DETAIL, statParam);
    }

    public static void reportOneKeyCleanFinishOp() {
        HsmStat.statE(Events.E_OPTMIZE_REPORT_ONE_KEY_CLEAN_FINISH_OP);
    }

    public static void reportMemoryAcceleratorListSelectAllOp(String selectOp) {
        String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, selectOp);
        HsmStat.statE((int) Events.E_OPTMIZE_REPORT_MEMORY_ACCELERATOR_LIST_SELECT_ALL_OP, statParam);
    }

    public static void reportFileAnalysisResult(String result) {
        HsmStat.statE((int) Events.E_OPTMIZE_REPORT_FILE_ANALYSIS_RESULT, result);
    }

    public static void reportFileAnalysisSwitchOp(int op) {
        String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, String.valueOf(op));
        HsmStat.statE((int) Events.E_OPTMIZE_REPORT_FILE_ANALYSIS_SWITCH_OP, statParam);
    }

    public static void reportFileAnalysisNotificationEnterOp() {
        HsmStat.statE(Events.E_OPTMIZE_REPORT_FILE_ANALYSIS_NOTIFICATION_OP);
    }

    public static void reportFileAnalysisCleanResult(String result) {
        HsmStat.statE((int) Events.E_OPTMIZE_REPORT_FILE_ANALYSIS_CLEAN_RESULT, result);
    }

    public static void reportDeepItemTrashSize(String result) {
        HsmStat.statE((int) Events.E_OPTMIZE_REPORT_DEEP_ITEM_TRASH_SIZE, result);
    }

    private static void innerReportOneKeyCleanTrash(long trashSize, int type) {
        if (0 != trashSize) {
            String statParam = HsmStatConst.constructJsonParams(PARAM_TYPE, String.valueOf(type), PARAM_SIZE, String.valueOf(trashSize));
            HsmStat.statE((int) Events.E_OPTMIZE_REPORT_ONE_KEY_CLEAN_TRASH, statParam);
        }
    }

    private static void setDeviceStorage(TrashScanHandler handler, TrashInfoBuilder builder) {
        StorageHelper storageHelper = StorageHelper.getStorage();
        builder.setAvaInnerStorage(storageHelper.getAvalibaleSize(0)).setInnerStorage(storageHelper.getTotalSize(0));
        if (handler.hasSdcard()) {
            builder.setAvaExternalStorage(storageHelper.getAvalibaleSize(1)).setExternalStorage(storageHelper.getTotalSize(1));
        }
    }

    private static void setTotalTrashCountAndSize(Map<Integer, TrashGroup> results, TrashInfoBuilder builder) {
        int totalCount = 0;
        long totalSize = 0;
        for (TrashGroup group : results.values()) {
            totalCount += group.getTrashCount();
            totalSize += group.getTrashSize();
        }
        builder.setTotalTrashCount(totalCount);
        builder.setTotalTrashSize(totalSize);
    }

    private static void innerReportFileNumAndSize(TrashGroup trashGroup, int reportType) {
        if (trashGroup != null) {
            String count = String.valueOf(trashGroup.getTrashCount());
            String size = String.valueOf(trashGroup.getTrashSize());
            HsmStat.statE((int) Events.E_OPTMIZE_REPORT_MEDIA_FILE_INFO, PARAM_TYPE, String.valueOf(reportType), "COUNT", count, PARAM_SIZE, size);
        }
    }
}
