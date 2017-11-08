package com.huawei.systemmanager.spacecleanner.engine;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.SparseArray;
import com.google.android.collect.Maps;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.spacecleanner.engine.base.CombineTask;
import com.huawei.systemmanager.spacecleanner.engine.base.Task;
import com.huawei.systemmanager.spacecleanner.engine.trash.AppCustomTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.engine.trash.TrashGroup;
import com.huawei.systemmanager.spacecleanner.statistics.SpaceStatsUtils;
import com.huawei.systemmanager.spacecleanner.ui.spacemanager.item.VideoDeepItem;
import com.huawei.systemmanager.util.HwLog;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class TotalScanTask extends CombineTask {
    private static final int MSG_NORMAL_TASK_CANCELED = 11;
    private static final int MSG_TASK_END = 10;
    private static final int MSG_UPDATE_ITEM = 5;
    private static final String TAG = "TotalScanTask";
    private AtomicBoolean isDesdroyed = new AtomicBoolean(false);
    private int mCurrentTrashNum = 0;
    private Set<Integer> mDeepPreparedType = Sets.newHashSet();
    private int mFinishedType = 0;
    private Set<Integer> mFinishedTypeSet = Sets.newHashSet();
    private boolean mFlagNormalTaskCanceled;
    private InnerHandler mHander;
    private ITrashScanListener mListener = ITrashScanListener.EMPTY_LISTENER;
    private int mMaxTrashNum = -1;
    private Set<Integer> mNormalPreparedType = Sets.newHashSet();
    private int mNormalTaskSize;
    private final Map<Integer, TrashGroup> mNormalTrashMap = Maps.newHashMap();
    private long mNormalTrashSize = 0;
    private final Map<String, List<Trash>> mPathMap = Maps.newHashMap();
    private final ProgressInfoFilter mProgressInfoFilter = new ProgressInfoFilter();
    private List<Task> mRestNormalTask = Lists.newArrayList();
    private List<Task> mRestTask = Lists.newArrayList();
    private String mScanningTrashInfo;
    private SparseArray<List<Task>> mSupportTypeMap;
    private final Map<Integer, TrashGroup> mTotalTrashMap = Maps.newHashMap();
    private List<ITrashFilter> mTrashFilters = Lists.newArrayList();

    private class InnerHandler extends Handler {
        public InnerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 5:
                    TotalScanTask.this.handleItemUpdate(msg.obj, msg.arg2);
                    return;
                case 10:
                    TotalScanTask.this.handlerTaskEnd(msg.obj);
                    return;
                case 11:
                    HwLog.e(TotalScanTask.TAG, "receive MSG_NORMAL_TASK_CANCELED");
                    TotalScanTask.this.mFlagNormalTaskCanceled = true;
                    if (TotalScanTask.this.isEnd()) {
                        HwLog.e(TotalScanTask.TAG, "but task is already end, ignore it");
                        return;
                    }
                    TotalScanTask.this.prepareNormalTrash(true);
                    TotalScanTask.this.mListener.onScanEnd(50, 0, true);
                    return;
                default:
                    return;
            }
        }
    }

    private static class ProgressInfoFilter {
        private static final long PROGRESS_INTERVAL = 150;
        private long mLastDeepInfoTime;
        private long mLastNormlInfoTime;

        private ProgressInfoFilter() {
        }

        private boolean filter(boolean normal, int progressInt, String info) {
            long current = SystemClock.elapsedRealtime();
            if (normal) {
                if (current - this.mLastNormlInfoTime <= PROGRESS_INTERVAL) {
                    return false;
                }
                this.mLastNormlInfoTime = current;
                return true;
            } else if (current - this.mLastDeepInfoTime <= PROGRESS_INTERVAL) {
                return false;
            } else {
                this.mLastDeepInfoTime = current;
                return true;
            }
        }
    }

    public TotalScanTask(Context ctx, Task... scanTasks) {
        super(ctx, TAG, scanTasks);
    }

    public void onSingleProgressUpdate(Task task, int progressInt, String info) {
        boolean isNormal = task.isNormal();
        if (this.mProgressInfoFilter.filter(isNormal, progressInt, info)) {
            int adjustProgress = getProgress();
            this.mScanningTrashInfo = info;
            this.mListener.onScanProgressChange(isNormal ? 50 : 100, adjustProgress, this.mScanningTrashInfo, this.mNormalTrashSize, getNormalTaskProgress());
        }
    }

    public void onSingleItemUpdate(Task task, Trash trash) {
        HwLog.d(TAG, "onSingleItemUpdate, task:" + task.getTaskName() + "trash type: " + trash.getType());
        if (!this.isDesdroyed.get()) {
            this.mHander.obtainMessage(5, 0, task.getType(), trash).sendToTarget();
        }
    }

    protected void onSingleTaskEnd(Task task, boolean canceled) {
        HwLog.i(TAG, "onSingleTaskEnd, task:" + task.getTaskName() + ", canceled:" + canceled);
        if (!this.isDesdroyed.get()) {
            this.mHander.obtainMessage(10, task).sendToTarget();
        }
    }

    protected void startWork(ScanParams p) {
        this.mMaxTrashNum = p.getMaxTrashNum();
        for (Task task : getTasks()) {
            if (task.isNormal()) {
                this.mRestNormalTask.add(task);
            }
        }
        this.mNormalTaskSize = this.mRestNormalTask.size();
        this.mRestTask.addAll(getTasks());
        HandlerThread handlerThread = new HandlerThread("TotalScanTask_handlerThread");
        handlerThread.start();
        this.mHander = new InnerHandler(handlerThread.getLooper());
        this.mTrashFilters.add(new CustProtectFilter(p));
        this.mTrashFilters.add(new VedioFilter());
        this.mTrashFilters.add(new SystemPathFilter());
        super.startWork(p);
    }

    public void destory() {
        this.isDesdroyed.set(true);
        for (Task task : this.mTasks) {
            task.cancel();
        }
        this.mHander.getLooper().quit();
    }

    public void setScanListener(ITrashScanListener l) {
        this.mListener = l;
    }

    public List<Trash> getResult() {
        return null;
    }

    public void cancelNormalScan() {
        HwLog.i(TAG, "cancelNormalScan called");
        this.mHander.sendMessageAtFrontOfQueue(this.mHander.obtainMessage(11));
    }

    public void cancel() {
        HwLog.i(TAG, "cancel called!");
        setCanceled(true);
        for (Task task : this.mTasks) {
            String taskName = task.getTaskName();
            HwLog.i(TAG, "task cancel start! " + taskName);
            task.cancel();
            HwLog.i(TAG, "task cancel end!!" + taskName);
        }
    }

    public Map<Integer, TrashGroup> getNormalTrash() {
        return Collections.unmodifiableMap(this.mNormalTrashMap);
    }

    public Map<Integer, TrashGroup> getAllTrashes() {
        return Collections.unmodifiableMap(this.mTotalTrashMap);
    }

    public Map<String, List<Trash>> getPathMap() {
        return Collections.unmodifiableMap(this.mPathMap);
    }

    public TrashGroup getNormalTrashByType(int trashType) {
        return (TrashGroup) this.mNormalTrashMap.get(Integer.valueOf(trashType));
    }

    public TrashGroup getTrashByType(int trashType) {
        return (TrashGroup) this.mTotalTrashMap.get(Integer.valueOf(trashType));
    }

    public List<TrashGroup> getTrashByMixType(int trashType) {
        List<TrashGroup> groupList = Lists.newArrayList();
        for (TrashGroup group : this.mTotalTrashMap.values()) {
            if ((group.getType() & trashType) != 0) {
                groupList.add(group);
            }
        }
        return groupList;
    }

    private void handleItemUpdate(Trash trash, int taskType) {
        for (ITrashFilter filter : this.mTrashFilters) {
            filter.filter(trash);
        }
        if (trash.isValidate()) {
            this.mCurrentTrashNum++;
            if (this.mMaxTrashNum <= 0 || this.mCurrentTrashNum <= this.mMaxTrashNum) {
                trash.getTrashSize();
                int trashType = trash.getType();
                if ((this.mFinishedType & trashType) != 0) {
                    HwLog.e(TAG, "Trash task is complete, but still trash upadate!!!! trashType:" + trashType);
                    return;
                }
                putIntoNormalMap(trash);
                putIntoDeepMap(trash);
                for (String path : trash.getFiles()) {
                    String lowCasePath = path.toLowerCase(Locale.ENGLISH);
                    List<Trash> trashlist = (List) this.mPathMap.get(lowCasePath);
                    if (trashlist == null) {
                        trashlist = new ArrayList();
                        trashlist.add(trash);
                        this.mPathMap.put(lowCasePath, trashlist);
                    } else {
                        trashlist.add(trash);
                    }
                }
                return;
            }
            HwLog.w(TAG, "current trash size beyond limit! drop new trash and cancel the task.currentTrashSize:" + this.mCurrentTrashNum + ", Trash type:" + trash.getType() + ", task type:" + taskType);
            Task task = getTaskByTaskType(taskType);
            SpaceStatsUtils.reportTrashScanMaxFileLimit();
            if (task == null) {
                HwLog.e(TAG, "could not found task, type:" + taskType);
            } else {
                task.cancel();
            }
        }
    }

    private void putIntoNormalMap(Trash trash) {
        if (!this.mFlagNormalTaskCanceled) {
            Trash trashTobeAdd = null;
            if (trash.isNormal()) {
                trashTobeAdd = trash;
            } else if (trash instanceof AppCustomTrash) {
                AppCustomTrash normal = ((AppCustomTrash) trash).splitNormalTrash();
                if (normal != null) {
                    Object trashTobeAdd2 = normal;
                }
            }
            if (trashTobeAdd != null) {
                int trashType = trash.getType();
                if ((81920 & trashType) != 0) {
                    trashType = 81920;
                }
                TrashGroup group = (TrashGroup) this.mNormalTrashMap.get(Integer.valueOf(trashType));
                if (group == null) {
                    group = new TrashGroup(trashType, true);
                    this.mNormalTrashMap.put(Integer.valueOf(trashType), group);
                }
                group.addChild(trashTobeAdd);
                this.mNormalTrashSize += trashTobeAdd.getTrashSize();
                this.mListener.onScanProgressChange(50, getProgress(), this.mScanningTrashInfo, this.mNormalTrashSize, getNormalTaskProgress());
            }
        }
    }

    private void putIntoDeepMap(Trash trash) {
        int trashType = trash.getType();
        if (trashType == 65536) {
            putIntoMap(this.mTotalTrashMap, trash, trashType, false);
        }
        if ((81920 & trashType) != 0) {
            trashType = 81920;
        }
        putIntoMap(this.mTotalTrashMap, trash, trashType, false);
    }

    private void putIntoMap(Map<Integer, TrashGroup> map, Trash trash, int trashType, boolean isNormal) {
        TrashGroup group = (TrashGroup) map.get(Integer.valueOf(trashType));
        if (group == null) {
            group = new TrashGroup(trashType, isNormal);
            map.put(Integer.valueOf(trashType), group);
        }
        group.addChild(trash);
    }

    private int getNormalTaskProgress() {
        if (this.mNormalTaskSize <= 0) {
            return 0;
        }
        double progressValue = 1.0d - (((double) this.mRestNormalTask.size()) / ((double) this.mNormalTaskSize));
        if (progressValue <= 0.0d) {
            HwLog.i(TAG, "result is 0");
            return 0;
        } else if (progressValue >= 1.0d) {
            HwLog.i(TAG, "result is 100");
            return 100;
        } else {
            double result = new BigDecimal(progressValue).setScale(2, 4).doubleValue();
            HwLog.i(TAG, "result:  " + result);
            return (int) (100.0d * result);
        }
    }

    private void handlerTaskEnd(Task task) {
        if (isEnd()) {
            HwLog.e(TAG, "handlerTaskEnd, but total task is already end, ignore, task:" + task.getTaskName());
            return;
        }
        task.setNotifyEnd(true);
        HwLog.i(TAG, "prepareTrash, task:" + task.getTaskName() + ", endTrashTypeList:" + checkTrashTypeScanEnd(task).toString());
        prepareNormalTrash(false);
        prepareDeepTrash();
        int taskType = task.getType();
        this.mListener.onScanEnd(taskType, this.mFinishedType, task.isCanceled());
        this.mRestTask.remove(task);
        this.mRestNormalTask.remove(task);
        this.mListener.onScanProgressChange(50, getProgress(), this.mScanningTrashInfo, this.mNormalTrashSize, getNormalTaskProgress());
        if (taskType < 50 && checkTaskEnd(true)) {
            boolean canceled = checkTaskCanceled(50);
            HwLog.i(TAG, "normal task end, canceled:" + canceled);
            this.mListener.onScanEnd(50, this.mFinishedType, canceled);
        }
        if (taskType < 100 && checkTaskEnd(false)) {
            canceled = checkTaskCanceled(100);
            HwLog.i(TAG, "deep task end, canceled:" + canceled);
            onPublishEnd();
            this.mListener.onScanEnd(100, this.mFinishedType, canceled);
        }
    }

    private void prepareNormalTrash(boolean isCancel) {
        for (TrashGroup normal : this.mNormalTrashMap.values()) {
            int type = normal.getType();
            if (!this.mNormalPreparedType.contains(Integer.valueOf(type)) && (isCancel || (this.mFinishedType & type) == type)) {
                normal.prepare();
                this.mNormalPreparedType.add(Integer.valueOf(type));
            }
        }
    }

    private void prepareDeepTrash() {
        for (TrashGroup deep : this.mTotalTrashMap.values()) {
            int type = deep.getType();
            if (!this.mDeepPreparedType.contains(Integer.valueOf(type))) {
                if (!(type == 256 || type == 65536)) {
                    if (type == 81920) {
                    }
                    if ((this.mFinishedType & type) == type) {
                        deep.prepare();
                        this.mDeepPreparedType.add(Integer.valueOf(type));
                    }
                }
                if ((this.mFinishedType & VideoDeepItem.TRASH_TYPE) != VideoDeepItem.TRASH_TYPE) {
                }
                if ((this.mFinishedType & type) == type) {
                    deep.prepare();
                    this.mDeepPreparedType.add(Integer.valueOf(type));
                }
            }
        }
    }

    private boolean checkTaskEnd(boolean normal) {
        if (!normal) {
            return this.mRestTask.isEmpty();
        }
        for (Task task : this.mRestTask) {
            if (task.isNormal()) {
                return false;
            }
        }
        return true;
    }

    public int getFinishedType() {
        return this.mFinishedType;
    }

    private void ensureSupportType() {
        if (this.mSupportTypeMap == null) {
            this.mSupportTypeMap = new SparseArray();
            for (Task t : getTasks()) {
                for (Integer intValue : t.getSupportTrashType()) {
                    int trashType = intValue.intValue();
                    List<Task> list = (List) this.mSupportTypeMap.get(trashType);
                    if (list == null) {
                        list = Lists.newArrayListWithCapacity(1);
                        this.mSupportTypeMap.put(trashType, list);
                    }
                    list.add(t);
                }
            }
        }
    }

    private List<Integer> checkTrashTypeScanEnd(Task task) {
        ensureSupportType();
        List<Integer> supportList = task.getSupportTrashType();
        if (HsmCollections.isEmpty(supportList)) {
            HwLog.w(TAG, "task has not support list!, task:" + task.getTaskName());
            return Lists.newArrayList();
        }
        List<Integer> endTrashTypeList = Lists.newArrayList();
        for (Integer intValue : supportList) {
            int trashType = intValue.intValue();
            if ((this.mFinishedType & trashType) == 0) {
                List<Task> list = (List) this.mSupportTypeMap.get(trashType);
                if (list == null) {
                    endTrashTypeList.add(Integer.valueOf(trashType));
                } else {
                    boolean allTaskEnd = true;
                    for (Task t : list) {
                        if (!t.isNotifyEnd()) {
                            allTaskEnd = false;
                            break;
                        }
                    }
                    if (allTaskEnd) {
                        endTrashTypeList.add(Integer.valueOf(trashType));
                    }
                }
            }
        }
        for (Integer intValue2 : endTrashTypeList) {
            trashType = intValue2.intValue();
            this.mFinishedType |= trashType;
            this.mFinishedTypeSet.add(Integer.valueOf(trashType));
        }
        return endTrashTypeList;
    }
}
