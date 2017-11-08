package com.android.settings.deviceinfo;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.Settings.Secure;
import com.android.settings.MLog;
import com.android.settings.Utils;

public class UsbMassStorageManager {
    private static UsbMassStorageManager instance = null;
    private static final Object slock = new Object();
    private boolean isOutdated = false;
    private Context mContext;
    private Handler mHandler;
    private long mLastMsgTime = 0;
    private Thread mOldHt;
    private StorageManager mStorageManager;
    private UsbManager mUsbManager;
    private HandlerThread mht;

    private UsbMassStorageManager() {
    }

    private UsbMassStorageManager(Context context) {
        this.mContext = context;
        this.mStorageManager = (StorageManager) context.getSystemService("storage");
        this.mUsbManager = (UsbManager) context.getSystemService("usb");
    }

    public static synchronized UsbMassStorageManager getInstance(Context context) {
        UsbMassStorageManager usbMassStorageManager;
        synchronized (UsbMassStorageManager.class) {
            if (instance == null) {
                instance = new UsbMassStorageManager(context.getApplicationContext());
            }
            usbMassStorageManager = instance;
        }
        return usbMassStorageManager;
    }

    public boolean isSwitchOn() {
        if (Secure.getInt(this.mContext.getContentResolver(), "mass_storage_switch", 0) == 1) {
            return true;
        }
        return false;
    }

    private boolean isExternalSDcardShared() {
        StorageManager storageManager = (StorageManager) this.mContext.getSystemService("storage");
        for (StorageVolume storageVolume : storageManager.getVolumeList()) {
            if (Utils.isVolumeExternalSDcard(this.mContext, storageVolume)) {
                return "shared".equals(storageManager.getVolumeState(storageVolume.getPath()));
            }
        }
        return false;
    }

    public boolean hasExternalSdcard() {
        StorageManager mStorageManager = (StorageManager) this.mContext.getSystemService("storage");
        for (StorageVolume volume : mStorageManager.getVolumeList()) {
            if (volume.isRemovable()) {
                String state = mStorageManager.getVolumeState(volume.getPath());
                if ("mounted".equals(state) || "shared".equals(state)) {
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    private boolean isExternalSDcardUnShared() {
        StorageManager storageManager = (StorageManager) this.mContext.getSystemService("storage");
        for (StorageVolume storageVolume : storageManager.getVolumeList()) {
            if (Utils.isVolumeExternalSDcard(this.mContext, storageVolume)) {
                return "mounted".equals(storageManager.getVolumeState(storageVolume.getPath()));
            }
        }
        return false;
    }

    private void retrySyncEnableUsbMassStorage() {
        if (this.mHandler != null) {
            if (System.currentTimeMillis() - this.mLastMsgTime > 60000) {
                MLog.w("UsbMassStorage", "time out when retry enable usbmassstorage");
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(3), 300000);
            } else {
                MLog.v("UsbMassStorage", "retry sendMessage");
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), 4000);
            }
        }
    }

    private void syncEnableUsbMassStorage() {
        synchronized (slock) {
            boolean enable = isSwitchOn();
            MLog.v("UsbMassStorage", "syncdo begin:enable=" + enable);
            if (enable) {
                doEnable();
            } else {
                doDisable();
            }
            MLog.v("UsbMassStorage", "syncdo end:enable=" + enable);
        }
    }

    private void doEnable() {
        MLog.d("UsbMassStorage", hasExternalSdcard() + "," + this.mStorageManager.isUsbMassStorageEnabled() + isExternalSDcardUnShared());
        if (!hasExternalSdcard() || (this.mStorageManager.isUsbMassStorageEnabled() && !isExternalSDcardUnShared())) {
            MLog.d("UsbMassStorage", "syncdo enable mass storage sucess!already.");
            setChangeState(3);
            return;
        }
        setChangeState(2);
        this.mStorageManager.enableUsbMassStorage();
        if (!this.mStorageManager.isUsbMassStorageEnabled() || isExternalSDcardUnShared()) {
            MLog.e("UsbMassStorage", "syncdo enable mass storage failed!" + this.mStorageManager.isUsbMassStorageEnabled() + isExternalSDcardUnShared());
            retrySyncEnableUsbMassStorage();
            return;
        }
        MLog.d("UsbMassStorage", "syncdo enable mass storage sucess!after enabling");
        setChangeState(3);
    }

    private void doDisable() {
        if ((hasExternalSdcard() && this.mStorageManager.isUsbMassStorageEnabled()) || isExternalSDcardShared()) {
            setChangeState(0);
            this.mStorageManager.disableUsbMassStorage();
            if (this.mStorageManager.isUsbMassStorageEnabled() || isExternalSDcardShared()) {
                MLog.d("UsbMassStorage", "syncdo disalbe mass storage failed!" + this.mStorageManager.isUsbMassStorageEnabled() + isExternalSDcardUnShared());
                retrySyncEnableUsbMassStorage();
            } else {
                MLog.d("UsbMassStorage", "syncdo disable mass storage sucess!after disabling");
            }
        } else {
            MLog.d("UsbMassStorage", "syncdo disable mass storage sucess!already.");
        }
        setChangeState(1);
    }

    private void syncDisableUsbMassStorage() {
        synchronized (slock) {
            doDisable();
        }
    }

    public void updateChangingState() {
        if (isChanging()) {
            MLog.v("UsbMassStorage", "updatestate sendMessage");
            prepare();
            this.mHandler.removeMessages(3);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(3), 0);
        }
    }

    private void syncUpdateState() {
        synchronized (slock) {
            if (isChanging()) {
                boolean isUsbMassStorageEnabled = this.mStorageManager.isUsbMassStorageEnabled();
                if (isUsbMassStorageEnabled || isExternalSDcardShared()) {
                    setChangeState(3);
                    MLog.d("UsbMassStorage", "updatestate enable:" + isExternalSDcardShared());
                } else if (!isUsbMassStorageEnabled) {
                    setChangeState(1);
                    MLog.d("UsbMassStorage", "updatestate disbale:" + isExternalSDcardUnShared());
                }
            }
        }
    }

    private void setChangeState(int state) {
        Secure.putInt(this.mContext.getContentResolver(), "mass_storage_state", state);
    }

    public int getChangeState() {
        return Secure.getInt(this.mContext.getContentResolver(), "mass_storage_state", -1);
    }

    private void clearChangeState() {
        int state = getChangeState();
        if (state == 2) {
            setChangeState(0);
        } else if (state == 0) {
            setChangeState(3);
        }
    }

    public boolean isChanging() {
        int state = getChangeState();
        if (state == 2 || state == 0) {
            return true;
        }
        return false;
    }

    private void prepare() {
        if (this.isOutdated || this.mht == null || !this.mht.isAlive()) {
            this.isOutdated = false;
            MLog.d("UsbMassStorage", "thread new mHandler and new thread");
            this.mht = new HandlerThread("mass_storage");
            this.mht.setPriority(10);
            this.mht.start();
            this.mHandler = new Handler(this.mht.getLooper()) {
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 1:
                            UsbMassStorageManager.this.syncEnableUsbMassStorage();
                            return;
                        case 2:
                            UsbMassStorageManager.this.syncDisableUsbMassStorage();
                            return;
                        case 3:
                            UsbMassStorageManager.this.syncUpdateState();
                            return;
                        default:
                            return;
                    }
                }
            };
        }
        if (this.mOldHt != null) {
            this.mOldHt.interrupt();
            this.mOldHt = null;
            clearChangeState();
        }
    }

    public void quitThread() {
        if (this.mht != null) {
            MLog.d("UsbMassStorage", "quitThread");
            this.mht.quitSafely();
            this.mOldHt = this.mht;
            this.isOutdated = true;
        }
    }
}
