package com.android.gallery3d.data;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.mtp.MtpDevice;
import android.mtp.MtpObjectInfo;
import android.net.Uri;
import android.os.Environment;
import com.android.gallery3d.data.MtpClient.Listener;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@TargetApi(12)
public class MtpContext implements Listener {
    private MtpClient mClient = new MtpClient(this.mContext);
    private Context mContext;
    private ScannerClient mScannerClient;

    private static final class ScannerClient implements MediaScannerConnectionClient {
        boolean mConnected;
        Object mLock = new Object();
        ArrayList<String> mPaths = new ArrayList();
        MediaScannerConnection mScannerConnection;

        public ScannerClient(Context context) {
            this.mScannerConnection = new MediaScannerConnection(context, this);
        }

        public void scanPath(String path) {
            synchronized (this.mLock) {
                if (this.mConnected) {
                    this.mScannerConnection.scanFile(path, null);
                } else {
                    this.mPaths.add(path);
                    this.mScannerConnection.connect();
                }
            }
        }

        public void onMediaScannerConnected() {
            synchronized (this.mLock) {
                this.mConnected = true;
                if (!this.mPaths.isEmpty()) {
                    for (String path : this.mPaths) {
                        this.mScannerConnection.scanFile(path, null);
                    }
                    this.mPaths.clear();
                }
            }
        }

        public void onScanCompleted(String path, Uri uri) {
        }
    }

    public MtpContext(Context context) {
        this.mContext = context;
        this.mScannerClient = new ScannerClient(context);
    }

    public void pause() {
        this.mClient.removeListener(this);
    }

    public void resume() {
        this.mClient.addListener(this);
        notifyDirty();
    }

    public void deviceAdded(MtpDevice device) {
        notifyDirty();
    }

    public void deviceRemoved(MtpDevice device) {
        notifyDirty();
    }

    private void notifyDirty() {
        this.mContext.getContentResolver().notifyChange(Uri.parse("mtp://"), null);
    }

    public MtpClient getMtpClient() {
        return this.mClient;
    }

    public boolean copyFile(String deviceName, MtpObjectInfo objInfo) {
        if (GalleryUtils.hasSpaceForSize((long) objInfo.getCompressedSize())) {
            File dest = new File(Environment.getExternalStorageDirectory(), "Imported");
            dest.mkdirs();
            String destPath = new File(dest, objInfo.getName()).getAbsolutePath();
            if (this.mClient.importFile(deviceName, objInfo.getObjectHandle(), destPath)) {
                this.mScannerClient.scanPath(destPath);
                return true;
            }
        }
        GalleryLog.w("MtpContext", "No space to import " + objInfo.getName() + " whose size = " + objInfo.getCompressedSize());
        return false;
    }

    public boolean copyAlbum(String deviceName, String albumName, List<MtpObjectInfo> children) {
        File dest = new File(Environment.getExternalStorageDirectory(), albumName);
        dest.mkdirs();
        int success = 0;
        for (MtpObjectInfo child : children) {
            if (GalleryUtils.hasSpaceForSize((long) child.getCompressedSize())) {
                String path = new File(dest, child.getName()).getAbsolutePath();
                if (this.mClient.importFile(deviceName, child.getObjectHandle(), path)) {
                    this.mScannerClient.scanPath(path);
                    success++;
                }
            }
        }
        return success == children.size();
    }
}
