package com.huawei.permissionmanager.ui;

import com.huawei.permissionmanager.utils.IRecommendChangeListener;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

class RecommendManager {
    private static final String LOG_TAG = "RecommendManager";
    private static RecommendManager sInstance;
    private List<IRecommendChangeListener> mListeners = new ArrayList();

    public static synchronized RecommendManager getInstance() {
        RecommendManager recommendManager;
        synchronized (RecommendManager.class) {
            if (sInstance == null) {
                sInstance = new RecommendManager();
            }
            recommendManager = sInstance;
        }
        return recommendManager;
    }

    public void registerListener(IRecommendChangeListener callback) {
        synchronized (this.mListeners) {
            this.mListeners.add(callback);
            HwLog.i(LOG_TAG, "register listener:" + callback);
        }
    }

    public void unregisterListener(IRecommendChangeListener callback) {
        synchronized (this.mListeners) {
            this.mListeners.remove(callback);
            HwLog.i(LOG_TAG, "unregister listener:" + callback);
        }
    }

    public void applicationFragmentChange() {
        for (IRecommendChangeListener listener : this.mListeners) {
            listener.onApplicationFragmentRecommendAppsChange();
        }
    }

    public void permissionFragmentChange() {
        for (IRecommendChangeListener listener : this.mListeners) {
            listener.onPermissionFragmentRecommendAppsChange();
        }
    }
}
