package com.huawei.cspcommon.performance;

import android.util.SparseArray;
import com.android.contacts.util.HwLog;

public class PLogManager {
    private static SparseArray<PLogInfo> mInfoCache = null;

    public static synchronized void init() {
        synchronized (PLogManager.class) {
            if (mInfoCache == null) {
                mInfoCache = new SparseArray();
                PLogPrinter.init();
            }
        }
    }

    public static void addLog(int tagId, String msg) {
        if (mInfoCache != null) {
            for (int sceneId = 0; sceneId < PLogTable.getSize(); sceneId++) {
                PLogInfo info = (PLogInfo) mInfoCache.get(sceneId);
                if (PLogTable.isStarting(tagId, sceneId)) {
                    if (info == null || !info.containsExcludeStarting()) {
                        info = new PLogInfo(sceneId);
                        info.insert(tagId, msg);
                        mInfoCache.put(sceneId, info);
                    } else {
                        HwLog.d("PLogManager", "remove scene which has excludeStarting: " + sceneId);
                        mInfoCache.remove(sceneId);
                    }
                } else if (info != null) {
                    if (PLogTable.isEnding(tagId, sceneId)) {
                        if (info.isVaild()) {
                            info.insert(tagId, msg);
                            if (info.containsAllIncluding()) {
                                info.print();
                                mInfoCache.remove(sceneId);
                            }
                        } else {
                            HwLog.d("PLogManager", "info not vaild, remove this scene " + sceneId);
                            mInfoCache.remove(sceneId);
                        }
                    } else if (PLogTable.isExcluding(tagId, sceneId)) {
                        mInfoCache.remove(sceneId);
                    } else if (PLogTable.isFollowStarting(tagId, sceneId) && !info.isVaildFollow()) {
                        HwLog.d("PLogManager", "info not vaild, remove this scene " + sceneId);
                        mInfoCache.remove(sceneId);
                    } else if (info.checkLimit()) {
                        info.insert(tagId, msg);
                    } else {
                        HwLog.d("PLogManager", "info reach limit, remove this scene " + sceneId);
                        mInfoCache.remove(sceneId);
                    }
                } else if (PLogTable.isExcludeStarting(tagId, sceneId)) {
                    info = new PLogInfo(sceneId);
                    info.insert(tagId, msg);
                    mInfoCache.put(sceneId, info);
                }
            }
        }
    }
}
