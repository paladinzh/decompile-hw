package com.huawei.android.freeshare.client.bluetooth;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Process;
import android.text.TextUtils;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.android.freeshare.client.device.DeviceInfo;
import com.huawei.android.freeshare.client.transfer.FileTransfer;
import com.huawei.android.freeshare.client.transfer.Mission;
import com.huawei.android.freeshare.client.transfer.TransferItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class BluetoothFileTransfer extends FileTransfer {
    private Object mInsertLock = new Object();
    private int mInsertShareThreadNum;
    private ContentObserver mObserver = new BluetoothShareContentObserver();
    private boolean mPendingUpdate;
    private boolean mRegisteDataBaseObserver;
    private UpdateThread mUpdateThread;
    private Map<TransferItem, Uri> mUriNeedObsever = new HashMap(5);

    private class BluetoothShareContentObserver extends ContentObserver {
        public BluetoothShareContentObserver() {
            super(new Handler());
        }

        public void onChange(boolean selfChange) {
            GalleryLog.v("Freeshare_BluetoothFileTransfer", "ContentObserver received notification");
            BluetoothFileTransfer.this.updateFromProvider();
        }
    }

    private class InsertShareInfoThread extends Thread {
        private Mission mMission;

        public InsertShareInfoThread(Mission mission) {
            this.mMission = mission;
            synchronized (BluetoothFileTransfer.this.mInsertLock) {
                BluetoothFileTransfer.this.mInsertShareThreadNum = BluetoothFileTransfer.this.mInsertShareThreadNum + 1;
            }
            GalleryLog.v("Freeshare_BluetoothFileTransfer", "Thread id is: " + getId());
        }

        public void run() {
            Process.setThreadPriority(10);
            BluetoothFileTransfer.this.insertShare(this.mMission);
            synchronized (BluetoothFileTransfer.this.mInsertLock) {
                BluetoothFileTransfer bluetoothFileTransfer = BluetoothFileTransfer.this;
                bluetoothFileTransfer.mInsertShareThreadNum = bluetoothFileTransfer.mInsertShareThreadNum - 1;
            }
        }
    }

    private class UpdateThread extends Thread {
        private List<TransferItem> mTempList = new ArrayList(5);

        public UpdateThread() {
            super("Update bluetooth database");
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            Process.setThreadPriority(10);
            while (true) {
                synchronized (BluetoothFileTransfer.this) {
                    if (BluetoothFileTransfer.this.mUpdateThread != this) {
                        throw new IllegalStateException("multiple UpdateThreads in BluetoothFileTransfer");
                    }
                    GalleryLog.v("Freeshare_BluetoothFileTransfer", "pendingUpdate is " + BluetoothFileTransfer.this.mPendingUpdate);
                    if (!BluetoothFileTransfer.this.mPendingUpdate || BluetoothFileTransfer.this.mUriNeedObsever.isEmpty()) {
                        BluetoothFileTransfer.this.mUpdateThread = null;
                    } else {
                        BluetoothFileTransfer.this.mPendingUpdate = false;
                    }
                }
            }
            BluetoothFileTransfer.this.mUpdateThread = null;
        }
    }

    protected BluetoothFileTransfer(Context context) {
        super(context);
    }

    public boolean init() {
        registBluetoothProviderObserver();
        return super.init();
    }

    public boolean destroy() {
        super.destroy();
        synchronized (this) {
            this.mUriNeedObsever.clear();
        }
        unRegistBluetoothProviderObserver();
        return true;
    }

    public boolean start(Mission mission) {
        synchronized (this.mInsertLock) {
            if (this.mInsertShareThreadNum > 3) {
                GalleryLog.e("Freeshare_BluetoothFileTransfer", "Too many shares user triggered concurrently!");
                return false;
            }
            InsertShareInfoThread insertThread = new InsertShareInfoThread(mission);
            insertThread.start();
            return true;
        }
    }

    private void registBluetoothProviderObserver() {
        if (!this.mRegisteDataBaseObserver && this.mObserver != null) {
            getContext().getContentResolver().registerContentObserver(BluetoothShare.CONTENT_URI, true, this.mObserver);
            this.mRegisteDataBaseObserver = true;
        }
    }

    private void unRegistBluetoothProviderObserver() {
        if (this.mRegisteDataBaseObserver) {
            getContext().getContentResolver().unregisterContentObserver(this.mObserver);
            this.mRegisteDataBaseObserver = false;
        }
    }

    private void insertShare(Mission mission) {
        DeviceInfo device = mission.getTargetDevice();
        Long ts = Long.valueOf(System.currentTimeMillis());
        ArrayList<TransferItem> items = mission.getTransferItems();
        ContentResolver contentResolver = getContext().getContentResolver();
        for (TransferItem item : items) {
            String fileUri = item.mUri;
            String contentType = contentResolver.getType(Uri.parse(item.mUri));
            GalleryLog.v("Freeshare_BluetoothFileTransfer", "Got mimetype: " + contentType + "  Got uri: " + fileUri);
            if (TextUtils.isEmpty(contentType)) {
                contentType = item.mMimetype;
            }
            ContentValues values = new ContentValues();
            values.put("uri", fileUri);
            values.put("mimetype", contentType);
            values.put("destination", device.getMacAddress());
            values.put("direction", Integer.valueOf(0));
            values.put("timestamp", ts);
            values.put("NeedSaveSendingFileInfo", Boolean.valueOf(true));
            Uri contentUri = null;
            try {
                contentUri = getContext().getContentResolver().insert(BluetoothShare.CONTENT_URI, values);
            } catch (Exception e) {
                GalleryLog.e("Freeshare_BluetoothFileTransfer", "insert database failed!!");
            }
            if (contentUri == null) {
                GalleryLog.e("Freeshare_BluetoothFileTransfer", "insert database failed,content uri is null!!");
            } else {
                item.setTransferUri(contentUri);
                synchronized (this) {
                    this.mUriNeedObsever.put(item, contentUri);
                }
                GalleryLog.v("Freeshare_BluetoothFileTransfer", "Insert contentUri: " + contentUri + "  to device: " + device.getName());
            }
        }
    }

    private void updateFromProvider() {
        synchronized (this) {
            this.mPendingUpdate = true;
            if (this.mUpdateThread == null && !this.mUriNeedObsever.isEmpty()) {
                this.mUpdateThread = new UpdateThread();
                this.mUpdateThread.start();
            }
        }
    }

    private void updateTransferItem(Uri uri, TransferItem item) {
        if (uri == null || item == null) {
            synchronized (this) {
                this.mUriNeedObsever.remove(item);
            }
        } else if (!item.isComplete()) {
            Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, "_id");
            int bluetoothShareState = 491;
            int totalBytes = 0;
            int currentBytes = 0;
            boolean z = false;
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    bluetoothShareState = cursor.getInt(cursor.getColumnIndexOrThrow("status"));
                    totalBytes = cursor.getInt(cursor.getColumnIndexOrThrow("total_bytes"));
                    currentBytes = cursor.getInt(cursor.getColumnIndexOrThrow("current_bytes"));
                    z = BluetoothShare.isStatusSuccess(bluetoothShareState);
                }
                cursor.close();
            }
            item.mCurrentBytes = currentBytes;
            item.mTotalBytes = totalBytes;
            item.mSuccess = z;
            int transferState = 3;
            if (SmsCheckResult.ESCT_192 == bluetoothShareState) {
                transferState = 4;
            } else if (BluetoothShare.isStatusCompleted(bluetoothShareState)) {
                transferState = 5;
            }
            item.mStatus = transferState;
            boolean shouldNotifyObserver = true;
            if (item.isComplete()) {
                synchronized (this) {
                    shouldNotifyObserver = this.mUriNeedObsever.remove(item) != null;
                }
            }
            if (this.mTransferObserver != null && shouldNotifyObserver) {
                this.mTransferObserver.notifyChanged(item);
            }
        }
    }

    public boolean cancleTransferringMission() {
        GalleryLog.d("Freeshare_BluetoothFileTransfer", "cancleTransferringMission");
        List<TransferItem> temp = new ArrayList(1);
        synchronized (this) {
            temp.addAll(this.mUriNeedObsever.keySet());
        }
        for (TransferItem item : temp) {
            Uri uri = item.getTransferUri();
            synchronized (this) {
                boolean shouldNotifyObserver = this.mUriNeedObsever.remove(item) != null;
            }
            if (uri != null && shouldNotifyObserver) {
                getContext().getContentResolver().delete(uri, null, null);
                item.mSuccess = false;
                item.mStatus = 5;
                if (this.mTransferObserver != null) {
                    this.mTransferObserver.notifyChanged(item);
                }
            }
        }
        synchronized (this) {
            GalleryLog.d("Freeshare_BluetoothFileTransfer", "mUriNeedObsever size = " + this.mUriNeedObsever.size());
        }
        return true;
    }

    public boolean isTransferring() {
        boolean isTransferring;
        synchronized (this) {
            isTransferring = !this.mUriNeedObsever.isEmpty();
        }
        return isTransferring;
    }
}
