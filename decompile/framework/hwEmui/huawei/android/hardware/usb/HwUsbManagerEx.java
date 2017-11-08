package huawei.android.hardware.usb;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;
import huawei.android.content.HwContextEx;
import huawei.android.hardware.usb.IHwUsbManagerEx.Stub;

public class HwUsbManagerEx {
    private static final String TAG = "HwUsbManagerEx";
    private static volatile HwUsbManagerEx mInstance = null;
    IHwUsbManagerEx mService;

    public static synchronized HwUsbManagerEx getInstance() {
        HwUsbManagerEx hwUsbManagerEx;
        synchronized (HwUsbManagerEx.class) {
            if (mInstance == null) {
                mInstance = new HwUsbManagerEx();
            }
            hwUsbManagerEx = mInstance;
        }
        return hwUsbManagerEx;
    }

    private HwUsbManagerEx() {
        this.mService = null;
        this.mService = Stub.asInterface(ServiceManager.getService(HwContextEx.HW_USB_EX_SERVICE));
    }

    public void allowUsbHDB(boolean alwaysAllow, String publicKey) {
        try {
            if (this.mService != null) {
                this.mService.allowUsbHDB(alwaysAllow, publicKey);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwUsbEx service binder error!");
        }
    }

    public void denyUsbHDB() {
        try {
            if (this.mService != null) {
                this.mService.denyUsbHDB();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwUsbManagerEx service binder error!");
        }
    }

    public void clearUsbHDBKeys() {
        try {
            if (this.mService != null) {
                this.mService.clearUsbHDBKeys();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwUsbManagerEx service binder error!");
        }
    }
}
