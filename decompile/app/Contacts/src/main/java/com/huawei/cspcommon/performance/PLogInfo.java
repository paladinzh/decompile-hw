package com.huawei.cspcommon.performance;

import com.android.contacts.util.HwLog;
import com.autonavi.amap.mapcore.VTMCDataCache;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PLogInfo {
    private long mCreationSystemTime = System.currentTimeMillis();
    private long mCreationTime = System.nanoTime();
    private ConcurrentLinkedQueue<PLogNode> mInfoQueue = new ConcurrentLinkedQueue();
    private long mLastTime = this.mCreationTime;
    private int mSceneId;

    static class PLogNode {
        private long mCostTime;
        private String mMsg;
        private int mTagId;

        public PLogNode(int tagId, String msg, long costTime) {
            this.mTagId = tagId;
            this.mMsg = msg;
            this.mCostTime = costTime;
        }

        public int getTagId() {
            return this.mTagId;
        }

        public String getMsg() {
            return this.mMsg;
        }

        public long getCostTimeMs() {
            return this.mCostTime / 1000000;
        }
    }

    public PLogInfo(int sceneId) {
        this.mSceneId = sceneId;
    }

    public void insert(int tagId, String msg) {
        this.mLastTime = System.nanoTime();
        this.mInfoQueue.add(new PLogNode(tagId, msg, this.mLastTime - this.mCreationTime));
    }

    public boolean checkLimit() {
        long interval = (System.nanoTime() - this.mLastTime) / 1000000;
        if (interval > 10000) {
            if (HwLog.HWDBG) {
                HwLog.d("PLogInfo", "invalid: interval time is " + interval);
            }
            return false;
        } else if (this.mInfoQueue.size() <= VTMCDataCache.MAXSIZE) {
            return true;
        } else {
            if (HwLog.HWDBG) {
                HwLog.d("PLogInfo", "invalid: mInfoQueue.size() is " + this.mInfoQueue.size());
            }
            return false;
        }
    }

    public void print() {
        PLogPrinter.print(this);
    }

    public long getCreationSystemTime() {
        return this.mCreationSystemTime;
    }

    public int getSceneId() {
        return this.mSceneId;
    }

    public ConcurrentLinkedQueue<PLogNode> getInfoQueue() {
        return this.mInfoQueue;
    }

    public boolean containsExcludeStarting() {
        if ((System.nanoTime() - this.mCreationTime) / 1000000 < 2000) {
            for (PLogNode node : this.mInfoQueue) {
                if (PLogTable.isExcludeStarting(node.mTagId, this.mSceneId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean containsAllIncluding() {
        int[] including = PLogTable.getIncluding(this.mSceneId);
        if (including != null) {
            for (int tagId : including) {
                boolean bInclude = false;
                for (PLogNode node : this.mInfoQueue) {
                    if (tagId == node.getTagId()) {
                        bInclude = true;
                        break;
                    }
                }
                if (!bInclude) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isVaildFollow() {
        if ((System.nanoTime() - this.mCreationTime) / 1000000 < 1000) {
            return true;
        }
        return false;
    }

    public boolean isVaild() {
        int[] excludeStarting = PLogTable.getExcludeStarting(this.mSceneId);
        if (excludeStarting != null) {
            for (PLogNode node : this.mInfoQueue) {
                int curTagId = node.getTagId();
                for (int tagId : excludeStarting) {
                    if (curTagId == tagId) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
