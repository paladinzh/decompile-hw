package com.huawei.systemmanager.spacecleanner.engine.base;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.SparseArray;
import com.google.common.collect.Lists;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.spacecleanner.engine.trash.IAppTrashInfo;
import com.huawei.systemmanager.spacecleanner.engine.trash.LargeFileTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.engine.trash.TrashGroup;
import com.huawei.systemmanager.spacecleanner.engine.trash.UnusedAppTrash;
import com.huawei.systemmanager.spacecleanner.statistics.FileAnalysisCleanInfo;
import com.huawei.systemmanager.spacecleanner.statistics.SpaceStatsUtils;
import com.huawei.systemmanager.spacecleanner.utils.FileTypeHelper;
import com.huawei.systemmanager.spacecleanner.utils.TrashUtils;
import com.huawei.systemmanager.util.HwLog;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;

public class CleanTask {
    private static final long CLEAN_INTERVAL_TIME = 2000;
    private static final long NOTIFY_INTERVAL = 100;
    private static final String TAG = "CleanTask";
    private volatile boolean mCanceled;
    private CleanParam mCleanParam;
    private int mCleanedTrashType;
    private volatile boolean mIsEnd;
    private long mLastNotifyTime;
    private ICleanListener mListener;

    public static class CleanParam {
        private static final SparseArray<Boolean> mCleanTrashIfAppAlive = new SparseArray();
        private boolean backGroundClean;
        private Context ctx;
        private List<Trash> list;
        private ICleanListener listener;
        private Executor mExecutor;
        private boolean needRefereshOther;
        private TrashScanHandler scanHandler;
        private boolean useInterval = false;

        static {
            mCleanTrashIfAppAlive.put(16384, Boolean.valueOf(false));
            mCleanTrashIfAppAlive.put(1, Boolean.valueOf(false));
            mCleanTrashIfAppAlive.put(8192, Boolean.valueOf(true));
            mCleanTrashIfAppAlive.put(81920, Boolean.valueOf(false));
        }

        public boolean canCleanIsAppAlive(int trashType) {
            Boolean canClean = Boolean.valueOf(true);
            if (this.backGroundClean) {
                canClean = (Boolean) mCleanTrashIfAppAlive.get(trashType);
                if (canClean == null) {
                    canClean = Boolean.valueOf(false);
                }
            }
            return canClean.booleanValue();
        }

        public CleanParam setContext(Context ctx) {
            this.ctx = ctx;
            return this;
        }

        public CleanParam setTrashList(List<Trash> list) {
            this.list = list;
            return this;
        }

        public CleanParam setListener(ICleanListener l) {
            this.listener = l;
            return this;
        }

        public CleanParam setInerveral(boolean useInterval) {
            this.useInterval = useInterval;
            return this;
        }

        public CleanParam setNeedRefersh(boolean needRefresh) {
            this.needRefereshOther = needRefresh;
            return this;
        }

        public CleanParam setScanHandler(TrashScanHandler scanHandler) {
            this.scanHandler = scanHandler;
            return this;
        }

        public CleanParam setExecutor(Executor executor) {
            this.mExecutor = executor;
            return this;
        }

        public CleanParam setBackGroundClean(boolean backGroundClean) {
            this.backGroundClean = backGroundClean;
            return this;
        }

        public Map<String, List<Trash>> getPathMap() {
            if (this.scanHandler == null) {
                return null;
            }
            return this.scanHandler.getPathMap();
        }
    }

    private CleanTask() {
    }

    private void start(final CleanParam param) {
        if (param.ctx == null) {
            HwLog.i(TAG, "param ctx is null, use globel context");
            param.ctx = GlobalContext.getContext();
        }
        this.mListener = param.listener;
        Executor executor = param.mExecutor;
        if (executor == null) {
            HwLog.i(TAG, "no executor set, use current thread");
            doTask(param);
            return;
        }
        executor.execute(new Runnable() {
            public void run() {
                CleanTask.this.doTask(param);
            }
        });
    }

    public void cancel() {
        HwLog.i(TAG, "cancel called,current is end:" + isEnd());
        this.mCanceled = true;
    }

    public boolean isEnd() {
        return this.mIsEnd;
    }

    private boolean isCanceled() {
        return this.mCanceled;
    }

    protected void doTask(CleanParam param) {
        this.mCleanParam = param;
        notifyCleanStart();
        long cleanedSize = 0;
        List<Trash> list = param.list;
        if (list == null) {
            list = Collections.emptyList();
            HwLog.e(TAG, "doTask list is null!!");
        }
        reportFileAnalysisClean(list);
        long cleanInterval = getCleanInterval(list.size());
        boolean useInterval = param.useInterval;
        int totalCount = list.size();
        for (int i = 0; i < totalCount; i++) {
            if (isCanceled()) {
                HwLog.i(TAG, "task is canceled, break");
                break;
            }
            Trash trash = (Trash) list.get(i);
            int trashType = trash.getType();
            int progress = (i * 100) / totalCount;
            cleanedSize += trash.getTrashSize();
            if (!param.backGroundClean) {
                cleanTrash(trash, progress);
            } else if (1 == trashType) {
                HwLog.i(TAG, "trashType is TYPE_APK_INNER_CACHE");
                for (Trash child : ((TrashGroup) trash).getTrashList()) {
                    cleanTrashPre(child, progress, param);
                }
            } else {
                HwLog.i(TAG, "trashType is not TYPE_APK_INNER_CACHE");
                cleanTrashPre(trash, progress, param);
            }
            trash.refreshContent();
            if (useInterval) {
                try {
                    Thread.sleep(cleanInterval);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        checkRefreshContent(param);
        notifyCleanEnd(cleanedSize);
    }

    public void reportFileAnalysisClean(List<Trash> listTrash) {
        long startTime = System.currentTimeMillis();
        FileAnalysisCleanInfo result = new FileAnalysisCleanInfo();
        result.mTime = startTime;
        StringBuilder appPkgStrBuilder = new StringBuilder();
        for (Trash trash : listTrash) {
            long trashSize = trash.getTrashSize();
            int unUsedDays;
            if (trash instanceof LargeFileTrash) {
                LargeFileTrash fileTrash = (LargeFileTrash) trash;
                unUsedDays = fileTrash.getUnUsedDays();
                result.mLargeFileSize += trashSize;
                result.mLargeFileNum++;
                switch (FileTypeHelper.getFileType(fileTrash.getPath())) {
                    case 1:
                        result.mLargeFileMusicSize += trashSize;
                        result.mMTime += unUsedDays;
                        result.mLargeFileMusicNum++;
                        break;
                    case 2:
                        result.mLargeFileVideoSize += trashSize;
                        result.mVTime += unUsedDays;
                        result.mLargeFileVideoNum++;
                        break;
                    case 3:
                        result.mLargeFilePhotoSize += trashSize;
                        result.mPTime += unUsedDays;
                        result.mLargeFilePhotoNum++;
                        break;
                    case 4:
                        result.mLargeFileApkSize += trashSize;
                        result.mATime += unUsedDays;
                        result.mLargeFileApkNum++;
                        break;
                    case 5:
                        result.mLargeFileDocSize += trashSize;
                        result.mDTime += unUsedDays;
                        result.mLargeFileDocNum++;
                        break;
                    case 6:
                        result.mLargeFileArchivesSize += trashSize;
                        result.mARTime += unUsedDays;
                        result.mLargeFileArchivesNum++;
                        break;
                    case 7:
                        result.mLargeFileOtherSize += trashSize;
                        result.mOTime += unUsedDays;
                        result.mLargeFileOtherNum++;
                        break;
                    default:
                        HwLog.e(TAG, "file type is error!");
                        break;
                }
            } else if (trash instanceof UnusedAppTrash) {
                UnusedAppTrash unusedApp = (UnusedAppTrash) trash;
                unUsedDays = unusedApp.getUnusedDay();
                result.mAppNum++;
                result.mAppSize += trashSize;
                result.mAppTime += unUsedDays;
                appPkgStrBuilder.append(unusedApp.getPackageName()).append(ConstValues.SEPARATOR_KEYWORDS_EN);
            }
        }
        result.mAppPkgNames = appPkgStrBuilder.toString();
        if (!TextUtils.isEmpty(result.mAppPkgNames)) {
            result.mAppPkgNames = "(" + result.mAppPkgNames.substring(0, result.mAppPkgNames.lastIndexOf(ConstValues.SEPARATOR_KEYWORDS_EN)) + ")";
        }
        String report = result.getReport();
        if (TextUtils.isEmpty(report)) {
            HwLog.e(TAG, "reportFileAnalysisClean report string is empty!:");
        } else {
            SpaceStatsUtils.reportFileAnalysisCleanResult(report);
        }
        HwLog.i(TAG, "reportFileAnalysisClean cost time:" + (System.currentTimeMillis() - startTime));
    }

    private void cleanTrashPre(Trash trash, int progress, CleanParam param) {
        if (!(trash instanceof IAppTrashInfo) || param.canCleanIsAppAlive(trash.getType())) {
            cleanTrash(trash, progress);
            return;
        }
        String pkgName = ((IAppTrashInfo) trash).getPackageName();
        if (Boolean.valueOf(TrashUtils.queryIfPkgAlive(pkgName)).booleanValue()) {
            HwLog.i(TAG, "pkg alive, do not clean. pkg:" + pkgName);
        } else {
            cleanTrash(trash, progress);
        }
    }

    private void checkRefreshContent(CleanParam param) {
        HwLog.i(TAG, "begint to RefreshContent");
        if (param.needRefereshOther) {
            TrashScanHandler handler = param.scanHandler;
            if (handler == null) {
                HwLog.e(TAG, "need refereshContent but scanhandler is null");
                return;
            }
            int key;
            Map<Integer, TrashGroup> normalTrashes = handler.getNormalTrashes();
            for (Integer intValue : Lists.newArrayList(normalTrashes.keySet())) {
                key = intValue.intValue();
                if ((this.mCleanedTrashType & key) != 0) {
                    HwLog.i(TAG, "refresh normal trash type:" + Integer.toBinaryString(key));
                    Trash normal = (Trash) normalTrashes.get(Integer.valueOf(key));
                    if (normal != null) {
                        normal.refreshContent();
                    }
                }
            }
            Map<Integer, TrashGroup> deepTrashes = handler.getAllTrashes();
            for (Integer intValue2 : Lists.newArrayList(deepTrashes.keySet())) {
                key = intValue2.intValue();
                if ((this.mCleanedTrashType & key) != 0) {
                    HwLog.i(TAG, "refresh deep trash type:" + Integer.toBinaryString(key));
                    Trash deep = (Trash) deepTrashes.get(Integer.valueOf(key));
                    if (deep != null) {
                        deep.refreshContent();
                    }
                }
            }
        }
    }

    private long getCleanInterval(int trashCount) {
        if (trashCount <= 0) {
            return 0;
        }
        return CLEAN_INTERVAL_TIME / ((long) Math.min(trashCount, 20));
    }

    private void cleanTrash(Trash trash, int progress) {
        CleanParam param = this.mCleanParam;
        if (param == null) {
            HwLog.e(TAG, "cleanTrash but param is null!!!");
            return;
        }
        if (trash instanceof TrashGroup) {
            for (Trash child : ((TrashGroup) trash).getTrashList()) {
                if (!isCanceled()) {
                    cleanTrash(child, progress);
                } else {
                    return;
                }
            }
        } else if (!isCanceled()) {
            notifyProgress(progress, trash);
            List<String> listPath = trash.getFiles();
            trash.clean(param.ctx);
            if (param.needRefereshOther) {
                this.mCleanedTrashType |= trash.getType();
                deleteOthersIfHasSameFile(param.getPathMap(), listPath);
            }
            notifyItemUpdate(trash);
        } else {
            return;
        }
        trash.refreshContent();
    }

    private void deleteOthersIfHasSameFile(Map<String, List<Trash>> map, List<String> listPath) {
        if (!HsmCollections.isMapEmpty(map)) {
            for (String path : listPath) {
                List<Trash> listTrash = (List) map.get(path.toLowerCase(Locale.ENGLISH));
                if (HsmCollections.isEmpty(listTrash)) {
                    HwLog.e(TAG, "File trash not add to pathMap;");
                } else {
                    for (Trash item : listTrash) {
                        if (item.cleanFile(path)) {
                            this.mCleanedTrashType |= item.getType();
                        }
                    }
                }
            }
        }
    }

    private void notifyProgress(int progress, Trash trash) {
        String name = trash.getName();
        if (TextUtils.isEmpty(name)) {
            HwLog.w(TAG, "notifyprogress, trash name is empty!!");
            return;
        }
        long currentTime = SystemClock.elapsedRealtime();
        if (currentTime - this.mLastNotifyTime > NOTIFY_INTERVAL) {
            this.mLastNotifyTime = currentTime;
            notifyProgressChanged(progress, name);
        }
    }

    private void notifyCleanStart() {
        HwLog.i(TAG, "notifyCleanStart");
        if (this.mListener != null) {
            this.mListener.onCleanStart();
        }
    }

    private void notifyProgressChanged(int progress, String info) {
        if (this.mListener != null) {
            this.mListener.onCleanProgressChange(progress, info);
        }
    }

    private void notifyItemUpdate(Trash trash) {
        if (this.mListener != null) {
            this.mListener.onItemUpdate(trash);
        }
    }

    private void notifyCleanEnd(long cleanedSize) {
        HwLog.i(TAG, "notifyCleanEnd");
        this.mIsEnd = true;
        if (this.mListener != null) {
            this.mListener.onCleanEnd(isCanceled(), cleanedSize);
        }
    }

    public static CleanTask startClean(List<Trash> list, ICleanListener l, TrashScanHandler scanHandler) {
        return startClean(GlobalContext.getContext(), list, l, scanHandler);
    }

    public static CleanTask startClean(Context ctx, List<Trash> list, ICleanListener l, TrashScanHandler scanHandler) {
        CleanParam param = buildParams();
        param.setContext(ctx);
        param.setTrashList(list);
        param.setListener(l);
        param.setScanHandler(scanHandler);
        param.setNeedRefersh(true);
        param.setExecutor(SpaceConst.sExecutor);
        return startClean(param);
    }

    public static CleanTask startCleanWithInterval(List<Trash> list, ICleanListener l, TrashScanHandler scanHandler) {
        CleanParam param = buildParams();
        param.setContext(GlobalContext.getContext());
        param.setTrashList(list);
        param.setListener(l);
        param.setScanHandler(scanHandler);
        param.setNeedRefersh(true);
        param.setExecutor(SpaceConst.sExecutor);
        param.setInerveral(true);
        return startClean(param);
    }

    public static CleanTask startAutoClean(Context ctx, List<Trash> list, ICleanListener l) {
        SpaceStatsUtils.reportAutoCleanTrashSize(list);
        CleanParam param = buildParams();
        param.setContext(ctx);
        param.setTrashList(list);
        param.setExecutor(SpaceConst.sExecutor);
        param.setNeedRefersh(false);
        param.setInerveral(false);
        param.setBackGroundClean(true);
        param.setListener(l);
        return startClean(param);
    }

    public static CleanTask startClean(CleanParam param) {
        CleanTask task = new CleanTask();
        task.start(param);
        return task;
    }

    public static CleanParam buildParams() {
        return new CleanParam();
    }
}
