package com.android.gallery3d.data;

import android.annotation.TargetApi;
import android.mtp.MtpDevice;
import android.mtp.MtpDeviceInfo;
import android.net.Uri;
import android.os.Handler;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.MediaSetUtils;
import com.android.gallery3d.util.ThreadPool.JobContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@TargetApi(12)
public class MtpDeviceSet extends MediaSet implements FutureListener<ArrayList<MediaSet>> {
    private GalleryApp mApplication;
    private ArrayList<MediaSet> mDeviceSet = new ArrayList();
    private final Handler mHandler;
    private boolean mIsLoading;
    private ArrayList<MediaSet> mLoadBuffer;
    private Future<ArrayList<MediaSet>> mLoadTask;
    private final MtpContext mMtpContext;
    private final String mName;
    private final ChangeNotifier mNotifier;

    private class DevicesLoader extends BaseJob<ArrayList<MediaSet>> {
        private DevicesLoader() {
        }

        public ArrayList<MediaSet> run(JobContext jc) {
            DataManager dataManager = MtpDeviceSet.this.mApplication.getDataManager();
            ArrayList<MediaSet> result = new ArrayList();
            List<MtpDevice> devices = MtpDeviceSet.this.mMtpContext.getMtpClient().getDeviceList();
            GalleryLog.v("MtpDeviceSet", "loadDevices: " + devices + ", size=" + devices.size());
            for (MtpDevice mtpDevice : devices) {
                synchronized (DataManager.LOCK) {
                    int deviceId = mtpDevice.getDeviceId();
                    Path childPath = MtpDeviceSet.this.mPath.getChild(deviceId);
                    MtpDevice device = (MtpDevice) dataManager.peekMediaObject(childPath);
                    if (device == null) {
                        device = new MtpDevice(childPath, MtpDeviceSet.this.mApplication, deviceId, MtpDeviceSet.this.mMtpContext);
                    }
                    GalleryLog.d("MtpDeviceSet", "add device " + device);
                    result.add(device);
                }
            }
            Collections.sort(result, MediaSetUtils.NAME_COMPARATOR);
            return result;
        }

        public String workContent() {
            return "load mtp devices.";
        }
    }

    public MtpDeviceSet(Path path, GalleryApp application, MtpContext mtpContext) {
        super(path, MediaObject.nextVersionNumber());
        this.mApplication = application;
        this.mNotifier = new ChangeNotifier((MediaSet) this, Uri.parse("mtp://"), application);
        this.mMtpContext = mtpContext;
        this.mName = application.getResources().getString(R.string.set_label_mtp_devices);
        this.mHandler = new Handler(this.mApplication.getMainLooper());
    }

    public static String getDeviceName(MtpContext mtpContext, int deviceId) {
        MtpDevice device = mtpContext.getMtpClient().getDevice(deviceId);
        if (device == null) {
            return "";
        }
        MtpDeviceInfo info = device.getDeviceInfo();
        if (info == null) {
            return "";
        }
        return info.getModel().trim();
    }

    public MediaSet getSubMediaSet(int index) {
        return index < this.mDeviceSet.size() ? (MediaSet) this.mDeviceSet.get(index) : null;
    }

    public int getSubMediaSetCount() {
        return this.mDeviceSet.size();
    }

    public String getName() {
        return this.mName;
    }

    public synchronized boolean isLoading() {
        return this.mIsLoading;
    }

    public synchronized long reload() {
        if (this.mNotifier.isDirty()) {
            if (this.mLoadTask != null) {
                this.mLoadTask.cancel();
            }
            this.mIsLoading = true;
            this.mLoadTask = this.mApplication.getThreadPool().submit(new DevicesLoader(), this);
        }
        if (this.mLoadBuffer != null) {
            this.mDeviceSet = this.mLoadBuffer;
            this.mLoadBuffer = null;
            for (MediaSet device : this.mDeviceSet) {
                device.reload();
            }
            this.mDataVersion = MediaObject.nextVersionNumber();
        }
        return this.mDataVersion;
    }

    public synchronized void onFutureDone(Future<ArrayList<MediaSet>> future) {
        if (future == this.mLoadTask) {
            this.mLoadBuffer = (ArrayList) future.get();
            this.mIsLoading = false;
            if (this.mLoadBuffer == null) {
                this.mLoadBuffer = new ArrayList();
            }
            this.mHandler.post(new Runnable() {
                public void run() {
                    MtpDeviceSet.this.notifyContentChanged();
                }
            });
        }
    }
}
