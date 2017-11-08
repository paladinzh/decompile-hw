package com.huawei.systemmanager.spacecleanner.engine.base;

import com.huawei.systemmanager.spacecleanner.engine.ScanParams;

public interface ITrashEngine {

    public interface IUpdateListener {
        public static final int ERROR_CODE_NO_NETWORK = 300;
        public static final int ERROR_CODE_UPDATE_FAILED = 301;

        void onError(int i);

        void onUpdateFinished();

        void onUpdateStarted();
    }

    void destory();

    Task getScanner(ScanParams scanParams);

    boolean init();

    void update(IUpdateListener iUpdateListener);
}
