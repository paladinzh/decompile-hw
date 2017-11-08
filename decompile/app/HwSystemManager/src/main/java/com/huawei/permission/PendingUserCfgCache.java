package com.huawei.permission;

import android.util.Log;
import com.huawei.permissionmanager.db.DBHelper;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public class PendingUserCfgCache {
    private static final String TAG = "PendingUserCfgCache";
    private List<UserCfg> mPendingCfg = new ArrayList();

    public static class UserCfg {
        private int operationType;
        private int permissionType;
        private String pkg;
        private long timeStamp;
        private int uid;

        public UserCfg(int uid, String pkg, int type, int op) {
            this.uid = uid;
            this.pkg = pkg;
            this.permissionType = type;
            this.operationType = op;
        }

        public long getPendingTime() {
            return this.timeStamp;
        }

        public void setPendingTime(long l) {
            this.timeStamp = l;
        }

        public int getUid() {
            return this.uid;
        }

        public String getPkg() {
            return this.pkg;
        }

        public int getOperationType() {
            return this.operationType;
        }

        public int getPermissionType() {
            return this.permissionType;
        }

        public int hashCode() {
            return ((((this.operationType + 31) * 31) + this.permissionType) * 31) + this.uid;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            UserCfg other = (UserCfg) obj;
            return this.permissionType == other.permissionType && this.uid == other.uid;
        }

        public String toString() {
            return "UserCfg [uid=" + this.uid + ", operationType=" + this.operationType + ", permissionType=" + this.permissionType + ", timeStamp=" + this.timeStamp + ", pkg=" + this.pkg + "]";
        }
    }

    public void addPendingCfg(int uid, String pkg, int type, int operation) {
        UserCfg cfg = new UserCfg(uid, pkg, type, operation);
        synchronized (this.mPendingCfg) {
            if (this.mPendingCfg.contains(cfg)) {
                HwLog.w(TAG, "repeated choice:" + cfg);
                return;
            }
            cfg.setPendingTime(System.currentTimeMillis());
            if (Log.HWINFO) {
                HwLog.i(TAG, "add pending user choice:" + cfg + ", current size:" + this.mPendingCfg.size());
            }
            this.mPendingCfg.add(cfg);
        }
    }

    public void removePendingCfg(int uid, String pkg, int type, int operation) {
        UserCfg cfg = new UserCfg(uid, pkg, type, operation);
        synchronized (this.mPendingCfg) {
            if (Log.HWINFO) {
                HwLog.i(TAG, "remove pending user choice:" + cfg + ", current size:" + this.mPendingCfg.size());
            }
            if (this.mPendingCfg.contains(cfg)) {
                this.mPendingCfg.remove(cfg);
                return;
            }
            HwLog.w(TAG, "remove not exist choice:" + cfg);
        }
    }

    public int getPendingCfg(int uid, String pkg, int type) {
        UserCfg cfg = new UserCfg(uid, pkg, type, 0);
        if (Log.HWINFO) {
            HwLog.i(TAG, "query pending user choice:" + cfg);
        }
        synchronized (this.mPendingCfg) {
            Object expired = null;
            for (UserCfg pendingCfg : this.mPendingCfg) {
                if (pendingCfg.equals(cfg)) {
                    if (System.currentTimeMillis() - pendingCfg.getPendingTime() > DBHelper.HISTORY_MAX_SIZE) {
                        if (Log.HWINFO) {
                            HwLog.i(TAG, "this choice is to expired, don't use it any more:" + pendingCfg);
                        }
                        expired = pendingCfg;
                        this.mPendingCfg.remove(expired);
                        return 3;
                    }
                    int operationType = pendingCfg.getOperationType();
                    return operationType;
                }
            }
            this.mPendingCfg.remove(expired);
            return 3;
        }
    }
}
