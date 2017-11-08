package com.huawei.systemmanager.mainscreen.detector;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.component.SingletonManager;
import com.huawei.systemmanager.comm.component.SingletonManager.Singletoner;
import com.huawei.systemmanager.comm.concurrent.HsmSingleExecutor;
import com.huawei.systemmanager.mainscreen.detector.item.DetectItem;
import com.huawei.systemmanager.mainscreen.detector.item.PkgDetectItem;
import com.huawei.systemmanager.mainscreen.detector.item.VirusAppItem;
import com.huawei.systemmanager.mainscreen.detector.task.DetectTask;
import com.huawei.systemmanager.mainscreen.detector.task.DetectTaskListener;
import com.huawei.systemmanager.mainscreen.detector.task.DetectTaskListener.SimpleDetectTaskListener;
import com.huawei.systemmanager.mainscreen.detector.task.DetectTaskSet;
import com.huawei.systemmanager.mainscreen.detector.task.MixDetectTask;
import com.huawei.systemmanager.mainscreen.detector.task.TrashDetectTask;
import com.huawei.systemmanager.mainscreen.detector.task.VirusDetectTask;
import com.huawei.systemmanager.util.HwLog;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

public class DetectTaskManager implements Singletoner {
    public static final int MAX_SOCRE = 100;
    public static final String TAG = "DetectTaskManager";
    private static final SingletonManager<DetectTaskManager> sSingletion = new SingletonManager<DetectTaskManager>() {
        protected DetectTaskManager onCreate(long randomId) {
            return new DetectTaskManager(randomId);
        }
    };
    private boolean mCouldRefresh;
    private Executor mExecutor;
    private long mId;
    private DetectTaskListener mInnerListener;
    private List<PkgDetectItem> mPkgItems;
    private RefreshTask mRefreshTask;
    private final Map<Integer, DetectItem> mResultItems;
    private DetectTaskListener mScanTaskListener;
    private DetectTaskSet mScanTaskSet;
    private VirusAppItem mVirusAppItem;
    private MixDetectTask mixTask;

    private class RefreshTask extends AsyncTask<List<DetectItem>, Void, Void> {
        private AtomicBoolean mWait;

        private RefreshTask() {
            this.mWait = new AtomicBoolean(true);
        }

        protected Void doInBackground(List<DetectItem>... params) {
            long startTime = SystemClock.elapsedRealtime();
            HwLog.i(DetectTaskManager.TAG, "RefreshTask begin!");
            List<DetectItem> copies = Lists.newArrayList();
            for (DetectItem item : params[0]) {
                DetectItem copy = item.copy();
                copy.refresh();
                copies.add(copy);
            }
            HwLog.i(DetectTaskManager.TAG, "RefreshTask end! cost time:" + (SystemClock.elapsedRealtime() - startTime));
            synchronized (DetectTaskManager.this.mResultItems) {
                for (DetectItem item2 : copies) {
                    DetectTaskManager.this.mResultItems.put(Integer.valueOf(item2.getItemType()), item2);
                }
            }
            DetectTaskManager.this.caculteScore();
            this.mWait.set(false);
            return null;
        }

        public boolean isWait() {
            return this.mWait.get();
        }
    }

    private DetectTaskManager(long id) {
        this.mResultItems = new HashMap();
        this.mPkgItems = Lists.newArrayList();
        this.mExecutor = new HsmSingleExecutor();
        this.mCouldRefresh = true;
        this.mInnerListener = new SimpleDetectTaskListener() {
            private static final long PROGRESS_INTERVAL = 100;
            private long lastProgressTime = 0;

            public void onStart(DetectTask task) {
                if (DetectTaskManager.this.mScanTaskListener != null) {
                    DetectTaskManager.this.mScanTaskListener.onStart(task);
                }
            }

            public void onItemFount(DetectTask task, DetectItem item) {
                synchronized (DetectTaskManager.this.mResultItems) {
                    DetectTaskManager.this.mResultItems.put(Integer.valueOf(item.getItemType()), item);
                }
                if (item instanceof PkgDetectItem) {
                    DetectTaskManager.this.mPkgItems.add((PkgDetectItem) item);
                }
                if (item instanceof VirusAppItem) {
                    DetectTaskManager.this.mVirusAppItem = (VirusAppItem) item;
                }
                if (DetectTaskManager.this.mScanTaskListener != null) {
                    DetectTaskManager.this.mScanTaskListener.onItemFount(task, item);
                }
                DetectTaskManager.this.caculteScore();
            }

            public void onProgressChange(DetectTask task, String itemName, float progress) {
                if (DetectTaskManager.this.mScanTaskSet != null) {
                    long currentTime = SystemClock.elapsedRealtime();
                    if (currentTime - this.lastProgressTime >= PROGRESS_INTERVAL) {
                        this.lastProgressTime = currentTime;
                        float fixedProgress = DetectTaskManager.this.mScanTaskSet.getProgress();
                        if (DetectTaskManager.this.mScanTaskListener != null) {
                            DetectTaskManager.this.mScanTaskListener.onProgressChange(task, itemName, fixedProgress);
                        }
                    }
                }
            }

            public void onTaskFinish(DetectTask task) {
                if (DetectTaskManager.this.mScanTaskListener != null) {
                    DetectTaskManager.this.mScanTaskListener.onTaskFinish(task);
                }
            }
        };
        this.mId = id;
    }

    public long getId() {
        return this.mId;
    }

    public void startDetectTask(Context context, DetectTaskListener listener) {
        HwLog.i(TAG, "startDetectTask");
        this.mScanTaskListener = listener;
        List<DetectTask> tasks = Lists.newArrayList();
        this.mixTask = new MixDetectTask(context, this.mExecutor);
        tasks.add(this.mixTask);
        tasks.add(new VirusDetectTask(context));
        tasks.add(new TrashDetectTask(context));
        Iterator<DetectTask> it = tasks.iterator();
        while (it.hasNext()) {
            if (!((DetectTask) it.next()).isEnable()) {
                HwLog.i(TAG, "task is NOT enable, remove it");
                it.remove();
            }
        }
        if (tasks.isEmpty()) {
            HwLog.e(TAG, "Detect sub task is empty!!");
        }
        this.mScanTaskSet = new DetectTaskSet(context, tasks);
        this.mScanTaskSet.setListener(this.mInnerListener);
        this.mScanTaskSet.execute();
    }

    public int getScore() {
        int scoreByItems;
        synchronized (this.mResultItems) {
            scoreByItems = getScoreByItems(this.mResultItems.values());
        }
        return scoreByItems;
    }

    public int getScoreByItems(Collection<DetectItem> items) {
        StringBuilder builder = new StringBuilder("getScoreByItems,");
        int totalScore = 0;
        for (DetectItem item : items) {
            int itemSocre = item.getScore();
            if (itemSocre != 0) {
                builder.append("(");
                item.printf(builder);
                builder.append("),");
            }
            totalScore += itemSocre;
        }
        int score = 100 - totalScore;
        if (score < 0) {
            HwLog.i(TAG, "score <0, adjust to 0, score:" + score);
            score = 0;
        }
        HwLog.i(TAG, builder.toString());
        return score;
    }

    public List<DetectItem> getResult() {
        List newArrayList;
        synchronized (this.mResultItems) {
            newArrayList = Lists.newArrayList(this.mResultItems.values());
        }
        return newArrayList;
    }

    private void caculteScore() {
        int score = getScore();
        if (this.mScanTaskListener != null) {
            this.mScanTaskListener.onItemScoreChange(score);
        }
    }

    public Executor getCleanExecutor() {
        return this.mExecutor;
    }

    public void refreshItem() {
        HwLog.i(TAG, "refreshItem called");
        if (this.mixTask == null) {
            HwLog.w(TAG, "refreshItem but mix task is null!");
        } else if (!this.mixTask.isFinish()) {
            HwLog.i(TAG, "mix task not finished, need not refresh item");
        } else if (this.mRefreshTask != null && this.mRefreshTask.isWait()) {
            HwLog.i(TAG, "refresh called, but there ia a refresh task wait, do nothing");
        } else if (this.mCouldRefresh) {
            List<DetectItem> items = getResult();
            this.mRefreshTask = new RefreshTask();
            this.mRefreshTask.executeOnExecutor(this.mExecutor, new List[]{items});
        } else {
            HwLog.i(TAG, "refresh called, but mCouldRefresh is false");
        }
    }

    public void destory() {
        if (this.mScanTaskSet != null) {
            this.mScanTaskSet.destory();
        }
        this.mScanTaskListener = null;
    }

    public boolean handlerPkgRemove(String pkg) {
        boolean changed = false;
        for (PkgDetectItem item : this.mPkgItems) {
            if (!item.isOptimized()) {
                changed = item.romvePkg(pkg);
            }
        }
        return changed;
    }

    public boolean handlerReceiveVirusScanApp(Intent intent) {
        if (this.mVirusAppItem == null) {
            return false;
        }
        this.mVirusAppItem.receiveVirusscanApps(intent);
        return true;
    }

    public void refreshBluetooth() {
        refreshItem(9);
    }

    public void refreshWifi() {
        refreshItem(8);
    }

    public void refreshItem(final int type) {
        this.mExecutor.execute(new Runnable() {
            public void run() {
                synchronized (DetectTaskManager.this.mResultItems) {
                    DetectItem item = (DetectItem) DetectTaskManager.this.mResultItems.get(Integer.valueOf(type));
                    if (item != null) {
                        DetectItem copy = item.copy();
                        copy.refresh();
                        DetectTaskManager.this.mResultItems.put(Integer.valueOf(copy.getItemType()), copy);
                    }
                }
                DetectTaskManager.this.caculteScore();
            }
        });
    }

    public void setCouldRefresh(boolean couldRefresh) {
        this.mCouldRefresh = couldRefresh;
    }

    public static DetectTaskManager create() {
        return (DetectTaskManager) sSingletion.createNewInstance();
    }

    public static DetectTaskManager getDetecor(long id) {
        return (DetectTaskManager) sSingletion.getSingleton(id);
    }
}
