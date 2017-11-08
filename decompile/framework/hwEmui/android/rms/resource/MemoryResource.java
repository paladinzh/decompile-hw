package android.rms.resource;

import android.os.Bundle;
import android.rms.HwSysResImpl;
import android.rms.HwSysResManager;
import android.util.Log;

public final class MemoryResource extends HwSysResImpl {
    public static final String APP_MEMSIZE = "MemorySize";
    public static final String APP_UID = "Uid";
    private static final boolean DEBUG;
    private static final String TAG = "RMS.MemoryResource";
    private static MemoryResource mMemoryResource;
    private HwSysResManager mResourceManger = HwSysResManager.getInstance();

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        DEBUG = isLoggable;
    }

    public static synchronized MemoryResource getInstance() {
        MemoryResource memoryResource;
        synchronized (MemoryResource.class) {
            if (mMemoryResource == null) {
                mMemoryResource = new MemoryResource();
                if (DEBUG) {
                    Log.d(TAG, "getInstance create new MemoryResource");
                }
            }
            memoryResource = mMemoryResource;
        }
        return memoryResource;
    }

    public int acquire(int callingUid, String pkg, int processTpye, int count) {
        Bundle bd = new Bundle();
        long size = ((long) count) * 1024;
        bd.putInt(APP_UID, callingUid);
        bd.putLong(APP_MEMSIZE, size);
        return this.mResourceManger.acquireSysRes(20, null, null, bd);
    }
}
