package com.huawei.systemmanager.spacecleanner.engine;

import android.content.Context;
import com.google.android.collect.Maps;
import com.huawei.systemmanager.comm.component.SingletonManager;
import com.huawei.systemmanager.comm.component.SingletonManager.Singletoner;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.engine.ITrashScanListener.MultiListener;
import com.huawei.systemmanager.spacecleanner.engine.base.ITrashEngine;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.spacecleanner.engine.trash.TrashGroup;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;
import java.util.Map;

public class TrashScanHandler implements Singletoner {
    private static final String TAG = "TrashScanHandler";
    private static final SingletonManager<TrashScanHandler> sSingletion = new SingletonManager<TrashScanHandler>() {
        protected TrashScanHandler onCreate(long randomId) {
            return new TrashScanHandler(GlobalContext.getContext(), randomId);
        }
    };
    private final ITrashEngine mEngine;
    private long mId;
    private ScanParams mParams;
    private MultiListener mScanListener = new MultiListener();
    private TotalScanTask mScanTask;

    TrashScanHandler(Context ctx, long id) {
        this.mEngine = new TrashEngineImpl(ctx);
        this.mId = id;
    }

    void startScan(ScanParams params) {
        this.mParams = params;
        this.mScanTask = (TotalScanTask) this.mEngine.getScanner(params);
        if (this.mScanTask == null) {
            HwLog.e(TAG, "Start scan, scanner is null!!");
            return;
        }
        this.mScanTask.setScanListener(this.mScanListener);
        this.mScanTask.start(params);
    }

    public void cancelScan(int scanType) {
        if (this.mScanTask == null) {
            HwLog.e(TAG, "cancelScan called, but mScanTask is null!");
            return;
        }
        if (scanType == 50) {
            this.mScanTask.cancelNormalScan();
        } else if (scanType == 100) {
            this.mScanTask.cancel();
        } else {
            throw new IllegalArgumentException("cancelScan, unknow scantype:" + scanType);
        }
    }

    public void cancelScan() {
        cancelScan(100);
    }

    public boolean isScanEnd() {
        if (this.mScanTask != null) {
            return this.mScanTask.isEnd();
        }
        HwLog.e(TAG, "isScanEnd called, but mScanTask is null!");
        return false;
    }

    public Map<Integer, TrashGroup> getNormalTrashes() {
        if (this.mScanTask != null) {
            return this.mScanTask.getNormalTrash();
        }
        HwLog.e(TAG, "getNormalTrashByType called, but mScanTask is null!");
        return Maps.newHashMap();
    }

    public Map<Integer, TrashGroup> getAllTrashes() {
        if (this.mScanTask != null) {
            return this.mScanTask.getAllTrashes();
        }
        HwLog.e(TAG, "getAllTrashes called, but mScanTask is null!");
        return Maps.newHashMap();
    }

    public Map<String, List<Trash>> getPathMap() {
        if (this.mScanTask != null) {
            return this.mScanTask.getPathMap();
        }
        HwLog.e(TAG, "getPathMap called, but mScanTask is null!");
        return Maps.newHashMap();
    }

    public TrashGroup getNormalTrashByType(int type) {
        if (this.mScanTask != null) {
            return this.mScanTask.getNormalTrashByType(type);
        }
        HwLog.e(TAG, "getNormalTrashByType called, but mScanTask is null!");
        return null;
    }

    public TrashGroup getTrashByType(int type) {
        if (this.mScanTask != null) {
            return this.mScanTask.getTrashByType(type);
        }
        HwLog.e(TAG, "getTrashByType  called, but mScanTask is null!");
        return null;
    }

    public List<TrashGroup> getTrashByMixType(int type) {
        if (this.mScanTask != null) {
            return this.mScanTask.getTrashByMixType(type);
        }
        HwLog.e(TAG, "getTrashByMixType  called, but mScanTask is null!");
        return null;
    }

    public int getFinishedType() {
        if (this.mScanTask != null) {
            return this.mScanTask.getFinishedType();
        }
        HwLog.e(TAG, "getTrashResultByTrashType TrashGroup called, but mScanTask is null!");
        return 0;
    }

    public long getId() {
        return this.mId;
    }

    public void addScanListener(ITrashScanListener l) {
        this.mScanListener.addListener(l);
    }

    public void removeScanListenr(ITrashScanListener l) {
        this.mScanListener.removeListener(l);
    }

    public void destory() {
        if (this.mScanTask != null) {
            this.mScanTask.destory();
        }
        this.mEngine.destory();
    }

    public boolean hasSdcard() {
        if (this.mParams != null) {
            return this.mParams.hasSdcard();
        }
        HwLog.e(TAG, "hasSdcard called, but mParams is null");
        return false;
    }

    public static TrashScanHandler create() {
        return (TrashScanHandler) sSingletion.createNewInstance();
    }

    public static TrashScanHandler getInstance(long id) {
        return (TrashScanHandler) sSingletion.getSingleton(id);
    }
}
