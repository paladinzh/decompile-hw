package android.rms.resource;

import android.os.SystemClock;
import android.rms.HwSysResImpl;
import android.rms.HwSysResManager;
import android.rms.config.ResourceConfig;
import android.util.Log;

public final class CursorResource extends HwSysResImpl {
    private static final boolean DEBUG = false;
    private static final String TAG = "CursorResource";
    private static CursorResource mCursorResource = null;
    private int mOverloadNum = 0;
    private long mPreReportTime = 0;
    private ResourceConfig[] mResourceConfig;
    private HwSysResManager mResourceManger;

    public CursorResource() {
        getConfig(17);
    }

    public static synchronized CursorResource getInstance() {
        CursorResource cursorResource;
        synchronized (CursorResource.class) {
            if (mCursorResource == null) {
                mCursorResource = new CursorResource();
            }
            cursorResource = mCursorResource;
        }
        return cursorResource;
    }

    public int acquire(int callingUid, String pkg, int processTpye, int count) {
        int strategy = 1;
        int typeID = super.getTypeId(callingUid, null, processTpye);
        if (isResourceCountOverload(callingUid, pkg, typeID, count)) {
            strategy = this.mResourceConfig[typeID].getResourceStrategy();
            if (typeID == 2 && Log.HWINFO) {
                Log.i(TAG, "process uid " + callingUid + " open too many cursor " + pkg);
            }
        }
        return strategy;
    }

    private boolean getConfig(int resourceType) {
        if (this.mResourceConfig != null) {
            return true;
        }
        this.mResourceManger = HwSysResManager.getInstance();
        if (this.mResourceManger == null) {
            Log.w(TAG, "getConfig mResourceManger == null");
            return false;
        }
        this.mResourceConfig = this.mResourceManger.getResourceConfig(resourceType);
        return this.mResourceConfig != null;
    }

    private boolean isResourceCountOverload(int callingUid, String pkg, int typeID, int count) {
        long id = super.getResourceId(callingUid, null, typeID);
        ResourceConfig config = this.mResourceConfig[typeID];
        int threshold = config.getResourceThreshold();
        int timeInterval = config.getLoopInterval();
        long currentTime = SystemClock.uptimeMillis();
        if (count <= threshold) {
            return false;
        }
        if (Log.HWINFO) {
            Log.i(TAG, "Cursor is Overload  id=" + id + " OverloadNumber=" + count + " threshold=" + threshold);
        }
        this.mOverloadNum++;
        if (isReportTime(callingUid, typeID, currentTime, timeInterval)) {
            this.mResourceManger.recordResourceOverloadStatus(callingUid, pkg, 17, 0, 0, this.mOverloadNum);
            this.mPreReportTime = currentTime;
        }
        return true;
    }

    public boolean isReportTime(int callingUid, int typeID, long currentTime, int timeInterval) {
        if (currentTime - this.mPreReportTime > ((long) timeInterval)) {
            return true;
        }
        return false;
    }
}
