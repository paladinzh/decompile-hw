package com.huawei.permission;

import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.IPackageChangeListener.DefListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TipsLifecycle extends DefListener {
    private static String TAG = TipsLifecycle.class.getSimpleName();
    private Map<String, List<String>> mTipRecords = new HashMap();

    static class TipExistException extends RuntimeException {
        TipExistException(String msg) {
            super(msg);
        }
    }

    TipsLifecycle() {
    }

    public void addTipRecord(String key, String tipOp, boolean force) {
        HwLog.d(TAG, "addTipRecord " + key + SqlMarker.COMMA_SEPARATE + tipOp);
        synchronized (this.mTipRecords) {
            addTipRecordInternal(key, tipOp, force);
        }
    }

    public void addNotExistRecord(String key, String tipOp) {
        HwLog.d(TAG, "addNotExistRecord " + key + SqlMarker.COMMA_SEPARATE + tipOp);
        synchronized (this.mTipRecords) {
            if (isTipRecordExist(key, tipOp)) {
                throw new TipExistException("Tip entry[" + key + SqlMarker.COMMA_SEPARATE + tipOp + "] already exist!");
            }
            addTipRecordInternal(key, tipOp, false);
        }
    }

    public boolean removeTipRecord(String key, String tipOp) {
        HwLog.d(TAG, "removeTipRecord " + key + SqlMarker.COMMA_SEPARATE + tipOp);
        synchronized (this.mTipRecords) {
            if (this.mTipRecords.get(key) == null || !((List) this.mTipRecords.get(key)).contains(tipOp)) {
                return false;
            }
            boolean remove = ((List) this.mTipRecords.get(key)).remove(tipOp);
            return remove;
        }
    }

    public void removeAppTipRecords(String key) {
        synchronized (this.mTipRecords) {
            this.mTipRecords.remove(key);
        }
    }

    public boolean isTipRecordExist(String key, String tipOp) {
        synchronized (this.mTipRecords) {
            if (this.mTipRecords.containsKey(key)) {
                boolean contains = ((List) this.mTipRecords.get(key)).contains(tipOp);
                return contains;
            }
            return false;
        }
    }

    public void onPackageRemoved(String pkgName) {
        super.onPackageRemoved(pkgName);
        removeAppTipRecords(pkgName);
    }

    private boolean shouldAddRecord() {
        return !ShareCfg.isControl;
    }

    private void addTipRecordInternal(String key, String tipOp, boolean force) {
        if (force || shouldAddRecord()) {
            List<String> list = (List) this.mTipRecords.get(key);
            if (list == null) {
                list = new ArrayList();
            }
            if (list.add(tipOp)) {
                this.mTipRecords.put(key, list);
            }
        }
    }
}
