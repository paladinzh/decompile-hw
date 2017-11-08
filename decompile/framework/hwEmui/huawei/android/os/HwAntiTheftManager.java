package huawei.android.os;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;
import huawei.android.content.HwContextEx;
import huawei.android.os.IHwAntiTheftManager.Stub;

public class HwAntiTheftManager {
    private static final String TAG = "AntiTheftManager";
    private static volatile HwAntiTheftManager mInstance = null;
    IHwAntiTheftManager mService;

    public static synchronized HwAntiTheftManager getInstance() {
        HwAntiTheftManager hwAntiTheftManager;
        synchronized (HwAntiTheftManager.class) {
            if (mInstance == null) {
                mInstance = new HwAntiTheftManager();
            }
            hwAntiTheftManager = mInstance;
        }
        return hwAntiTheftManager;
    }

    private HwAntiTheftManager() {
        this.mService = null;
        this.mService = Stub.asInterface(ServiceManager.getService(HwContextEx.HW_ANTI_THEFT_SERVICE));
    }

    public byte[] readAntiTheftData() {
        try {
            if (this.mService != null) {
                return this.mService.readAntiTheftData();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "AntiTheft binder error!");
        }
        return null;
    }

    public int wipeAntiTheftData() {
        try {
            if (this.mService != null) {
                return this.mService.wipeAntiTheftData();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "AntiTheft binder error!");
        }
        return -1;
    }

    public int writeAntiTheftData(byte[] writeToNative) {
        try {
            if (this.mService != null) {
                return this.mService.writeAntiTheftData(writeToNative);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "AntiTheft binder error!");
        }
        return -1;
    }

    public int getAntiTheftDataBlockSize() {
        try {
            if (this.mService != null) {
                return this.mService.getAntiTheftDataBlockSize();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "AntiTheft binder error!");
        }
        return 0;
    }

    public int setAntiTheftEnabled(boolean enable) {
        try {
            if (this.mService != null) {
                return this.mService.setAntiTheftEnabled(enable);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "AntiTheft binder error!");
        }
        return -1;
    }

    public boolean getAntiTheftEnabled() {
        try {
            if (this.mService != null) {
                return this.mService.getAntiTheftEnabled();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "AntiTheft binder error!");
        }
        return false;
    }

    public boolean checkRootState() {
        try {
            if (this.mService != null) {
                return this.mService.checkRootState();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "AntiTheft binder error!");
        }
        return false;
    }

    public boolean isAntiTheftSupported() {
        try {
            if (this.mService != null) {
                return this.mService.isAntiTheftSupported();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "AntiTheft binder error!");
        }
        return false;
    }
}
