package com.huawei.rcs.ui;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import com.android.mms.MmsApp;
import com.android.mms.ui.MessageUtils;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.AvatarCache;
import com.huawei.mms.util.ResEx;
import com.huawei.rcs.media.RcsMediaFileUtils;
import com.huawei.rcs.media.RcsMediaFileUtils.MediaFileType;
import com.huawei.rcs.utils.RcsUtility;
import java.io.File;
import java.io.Serializable;

public class RcsAsyncIconLoader {
    private static RcsAsyncIconLoader mInstance = null;
    private Callback mCallback = new Callback() {
        public boolean handleMessage(Message msg) {
            if (msg != null) {
                switch (msg.what) {
                    case 16:
                        if (msg.getData() != null) {
                            RcsAsyncIconLoader.this.asyncLoadIconInternal(msg.getData().getString("msg_id"), msg.getData().getString("file_path"), (OnIconLoadedCallback) msg.getData().getSerializable("callback"));
                            break;
                        }
                        break;
                }
            }
            return true;
        }
    };
    private Handler mHandler = null;
    private HandlerThread mThread = null;

    public interface OnIconLoadedCallback extends Serializable {
        void onIconLoaded(String str, Bitmap bitmap, boolean z);
    }

    private RcsAsyncIconLoader() {
        init();
    }

    private synchronized void init() {
        MLog.i("RcsAsyncIconLoader", "init RcsAsyncIconLoader -> init.");
        if (this.mThread != null) {
            this.mThread.quit();
        }
        this.mThread = new HandlerThread("RcsAsyncIconLoader");
        this.mThread.start();
        this.mHandler = new Handler(this.mThread.getLooper(), this.mCallback);
    }

    public static RcsAsyncIconLoader getInstance() {
        synchronized (RcsAsyncIconLoader.class) {
            if (mInstance == null) {
                mInstance = new RcsAsyncIconLoader();
            }
        }
        return mInstance;
    }

    public static synchronized boolean isInstanceExist() {
        boolean z;
        synchronized (RcsAsyncIconLoader.class) {
            z = mInstance != null;
        }
        return z;
    }

    public synchronized void asyncLoadIcon(String msgId, String filePath, OnIconLoadedCallback callback) {
        MLog.i("RcsAsyncIconLoader", " asyncLoadIcon -> msgId = " + msgId + ", filePath = " + filePath);
        if (!(callback == null || this.mHandler == null)) {
            String id = msgId;
            String path = filePath;
            OnIconLoadedCallback cb = callback;
            Message msg = this.mHandler.obtainMessage(16);
            Bundle fileData = new Bundle();
            fileData.putString("msg_id", msgId);
            fileData.putString("file_path", filePath);
            fileData.putSerializable("callback", callback);
            msg.setData(fileData);
            msg.sendToTarget();
        }
    }

    private void asyncLoadIconInternal(String msgId, String filePath, OnIconLoadedCallback callback) {
        boolean z = false;
        MediaFileType fileType = RcsMediaFileUtils.getFileType(filePath);
        if (callback == null) {
            MLog.w("RcsAsyncIconLoader", " asyncLoadIconInternal -> File type is null, nothing handled.");
            return;
        }
        Bitmap fileIcon = null;
        boolean isUnHandledFileType = false;
        if (fileType != null) {
            if (RcsMediaFileUtils.isVideoFileType(fileType.fileType)) {
                fileIcon = RcsUtility.composeFileTransIcon(MmsApp.getApplication(), filePath, null, 4096);
            } else if (RcsMediaFileUtils.isImageFileType(fileType.fileType)) {
                fileIcon = RcsUtility.composeFileTransIcon(MmsApp.getApplication(), filePath, null, 8192);
            } else if (RcsMediaFileUtils.isVCardFileType(fileType.fileType)) {
                fileIcon = createVCardThumbnail(filePath);
            } else {
                isUnHandledFileType = true;
                MLog.i("RcsAsyncIconLoader", " asyncLoadIconInternal -> Not the current available type, nothing handled.");
            }
        }
        if (fileIcon != null) {
            callback.onIconLoaded(msgId, ResEx.getRoundedCornerBitmap(fileIcon, (float) MessageUtils.dipToPx(MmsApp.getApplication(), 16.0f)), false);
        } else {
            MLog.i("RcsAsyncIconLoader", " asyncLoadIconInternal -> fileIcon is null, return default icon.");
            if (!isUnHandledFileType) {
                z = true;
            }
            callback.onIconLoaded(msgId, null, z);
        }
    }

    private Bitmap createVCardThumbnail(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            Uri uri = Uri.fromFile(file);
            MLog.d("RcsAsyncIconLoader", "createVCardThumbnail uri = " + uri);
            try {
                return AvatarCache.createRoundPhoto(RcsUtility.drawableToBitmap(new RcsVCardInfo(MmsApp.getApplication(), uri).getVCardDrawable()));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        MLog.w("RcsAsyncIconLoader", "createVcardParsingModule file is not exist");
        return null;
    }

    public synchronized void quit() {
        MLog.i("RcsAsyncIconLoader", "RcsAsyncIconLoader -> quit.");
        if (this.mHandler != null) {
            this.mHandler.removeMessages(16);
            this.mHandler = null;
        }
        if (this.mThread != null) {
            this.mThread.quit();
            this.mThread = null;
        }
        this.mCallback = null;
        setInstance(null);
    }

    public static void setInstance(RcsAsyncIconLoader instance) {
        mInstance = instance;
    }
}
