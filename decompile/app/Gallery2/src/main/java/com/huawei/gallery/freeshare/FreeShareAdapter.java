package com.huawei.gallery.freeshare;

import android.content.Context;
import android.os.SystemProperties;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.android.freeshare.client.Transmission;
import com.huawei.android.freeshare.client.device.DeviceChangeListener;
import com.huawei.android.freeshare.client.device.DeviceInfo;
import com.huawei.android.freeshare.client.transfer.ActionListener;
import com.huawei.android.freeshare.client.transfer.FileTransferListener;
import java.util.ArrayList;
import java.util.List;

public class FreeShareAdapter implements DeviceChangeListener, ActionListener, FileTransferListener {
    public static final boolean FREESHARE_SUPPORTED = SystemProperties.getBoolean("ro.config.hw_freeshare", true);
    private static FreeShareAdapter mInstance;
    private int initCount = 1;
    private List<Listener> mListeners = new ArrayList();
    private int mState;
    DeviceInfo mTargetDevice = null;
    Transmission mTransmission;

    public interface Listener {
        void onDeviceChange();

        void onDiscoverFinished();

        void onFinish();
    }

    private FreeShareAdapter() {
    }

    private void CountUp() {
        this.initCount++;
    }

    private boolean CountDown() {
        int i = this.initCount - 1;
        this.initCount = i;
        return i == 0;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized FreeShareAdapter getInstance(Context context) {
        synchronized (FreeShareAdapter.class) {
            if (mInstance != null) {
                mInstance.CountUp();
            } else if (FREESHARE_SUPPORTED) {
                Transmission transmission = Transmission.getTransmissionInstance(context);
                if (transmission == null || !transmission.init()) {
                } else {
                    mInstance = new FreeShareAdapter();
                    mInstance.init(transmission);
                }
            } else {
                return null;
            }
            FreeShareAdapter freeShareAdapter = mInstance;
            return freeShareAdapter;
        }
    }

    private void init(Transmission transmission) {
        GalleryLog.v("FreeShare", "DataAdapter init");
        this.mTransmission = transmission;
        this.mTransmission.addDeviceListener(this);
        this.mTransmission.setActionListener(this);
        this.mTransmission.addFileTransferListener(this);
        this.mState = 0;
    }

    public void destroy() {
        if (CountDown()) {
            GalleryLog.v("FreeShare", "DataAdapter destroy");
            this.mTransmission.removeDeviceListener(this);
            this.mTransmission.setActionListener(null);
            this.mTransmission.destroy();
            this.mTransmission = null;
            this.mTargetDevice = null;
            mInstance = null;
            this.mState = 0;
        }
    }

    public boolean hasTargetDevice() {
        return this.mTargetDevice != null;
    }

    public void addListener(Listener listener) {
        if (listener != null && !this.mListeners.contains(listener)) {
            this.mListeners.add(listener);
        }
    }

    public void removeListener(Listener listener) {
        if (this.mListeners.contains(listener)) {
            this.mListeners.remove(listener);
        }
    }

    public void onDeviceUp(DeviceInfo device) {
        if (2 == this.mState) {
            for (Listener l : this.mListeners) {
                l.onDeviceChange();
            }
        }
    }

    public void onEnabled() {
        if (1 == this.mState) {
            boolean result = this.mTransmission.discover();
            GalleryLog.d("FreeShare", "onEnabled " + result);
            if (result) {
                this.mState = 2;
                return;
            }
            this.mState = 0;
            for (Listener l : this.mListeners) {
                l.onDiscoverFinished();
            }
        }
    }

    public void onDisabled() {
        GalleryLog.d("FreeShare", "onDisabled");
        if (this.mState != 0) {
            this.mState = 0;
            for (Listener l : this.mListeners) {
                l.onDiscoverFinished();
            }
        }
        this.mTargetDevice = null;
    }

    public void onDiscoverStarted() {
    }

    public void onDiscoverFinished() {
        GalleryLog.d("FreeShare", "onDiscoverFinished");
        if (2 == this.mState) {
            for (Listener l : this.mListeners) {
                l.onDiscoverFinished();
            }
            this.mState = 0;
        }
    }

    public void onTransferFinish(String uri, boolean success) {
        if (!success) {
            this.mTargetDevice = null;
            this.mState = 0;
        }
        for (Listener l : this.mListeners) {
            l.onFinish();
        }
    }

    public void onProgressUpdate(String uri, int progress) {
    }

    public boolean discover() {
        if (2 == this.mState || 1 == this.mState) {
            return true;
        }
        boolean result;
        if (this.mTransmission.isEnabled()) {
            result = this.mTransmission.discover();
            if (result) {
                this.mState = 2;
            }
        } else {
            result = this.mTransmission.setEnabled(true);
            if (result) {
                this.mState = 1;
            }
        }
        if (!result) {
            this.mState = 0;
        }
        return result;
    }

    public void cancelDiscover() {
        if (1 == this.mState) {
            this.mTransmission.setEnabled(false);
        } else if (2 == this.mState) {
            this.mTransmission.cancelDiscover();
        }
        this.mState = 0;
    }

    public final List<DeviceInfo> getDeviceList() {
        if (this.mTransmission == null) {
            return new ArrayList();
        }
        return this.mTransmission.getDeviceList();
    }

    public void selectDevice(DeviceInfo device) {
        cancelDiscover();
        this.mTargetDevice = device;
    }

    public boolean sendMedia(String uri, String mimeType) {
        DeviceInfo device = this.mTargetDevice;
        if (device == null || uri == null || mimeType == null) {
            return false;
        }
        return this.mTransmission.startMission(this.mTransmission.createSendMission(device, uri, mimeType));
    }

    public void cancelShare() {
        this.mTransmission.cancelRestMission();
    }

    public String getTargetName() {
        if (this.mTargetDevice != null) {
            return this.mTargetDevice.getName();
        }
        return null;
    }
}
