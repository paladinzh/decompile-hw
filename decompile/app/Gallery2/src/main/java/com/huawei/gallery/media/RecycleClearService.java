package com.huawei.gallery.media;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.data.GalleryRecycleAlbum;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.ui.MenuExecutor;
import com.huawei.gallery.phonestatus.PhoneState;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.recycle.utils.CloudRecycleUtils;
import com.huawei.gallery.recycle.utils.RecycleUtils;
import com.huawei.gallery.service.AsyncService;
import com.huawei.gallery.util.MyPrinter;
import java.util.ArrayList;

public class RecycleClearService extends AsyncService {
    private static final MyPrinter LOG = new MyPrinter("RecycleClearService");

    protected String getServiceTag() {
        return "recycle clear service thread";
    }

    protected void decorateMsg(Message message, Intent intent, int startId) {
        message.what = startId;
        message.obj = intent;
    }

    private boolean getRunningPermission(boolean logPrint) {
        if (RecycleUtils.supportRecycle() && !MenuExecutor.isRecycleRecovering() && PhoneState.isChargeIn(this, logPrint) && PhoneState.isBatteryLevelOK(this, logPrint)) {
            return PhoneState.isScreenOff(this, logPrint);
        }
        return false;
    }

    private void sendMessage(int startId, long time, Intent intent) {
        Message msg = this.mServiceHandler.obtainMessage();
        decorateMsg(msg, intent, startId);
        if (time == 0) {
            this.mServiceHandler.sendMessage(msg);
        } else {
            this.mServiceHandler.sendMessageDelayed(msg, time);
        }
    }

    public boolean handleMessage(Message msg) {
        if (getRunningPermission(true)) {
            switch (msg.what) {
                case 0:
                    sendMessage(1, 0, null);
                    break;
                case 1:
                    long serverTime = 0;
                    if (PhotoShareUtils.getServer() != null) {
                        try {
                            serverTime = PhotoShareUtils.getServer().getServerTime();
                        } catch (RemoteException e) {
                            PhotoShareUtils.dealRemoteException(e);
                        }
                    }
                    boolean isDeletedFile = false;
                    while (true) {
                        GalleryRecycleAlbum mediaSet = (GalleryRecycleAlbum) ((GalleryApp) getApplication()).getDataManager().getMediaObject("/virtual/recycle");
                        if (mediaSet != null) {
                            ArrayList<MediaItem> items = mediaSet.getTotalWaitClearMediaItem(0, 500, serverTime);
                            int size = items.size();
                            LOG.d("clean items in recycleCleanService  size=" + size);
                            int i = 0;
                            while (i < size) {
                                MediaItem item = (MediaItem) items.get(i);
                                Bundle data = new Bundle();
                                data.putInt("recycle_flag", 3);
                                if (getRunningPermission(false)) {
                                    RecycleUtils.delete(getContentResolver(), item, data);
                                    isDeletedFile = true;
                                    i++;
                                } else {
                                    notifyDeleteStatus(isDeletedFile);
                                    sendMessage(3, 0, null);
                                    return true;
                                }
                            }
                            if (items.size() == 500) {
                            }
                        }
                        notifyDeleteStatus(isDeletedFile);
                        sendMessage(3, 0, null);
                        break;
                    }
                case 3:
                    stopRecycleClearService(this);
                    break;
            }
            return false;
        }
        stopRecycleClearService(this);
        return true;
    }

    public static void stopRecycleClearService(Context context) {
        if (context != null) {
            Intent intent = new Intent();
            intent.setClass(context, RecycleClearService.class);
            context.stopService(intent);
        }
    }

    private void notifyDeleteStatus(boolean isChanged) {
        if (isChanged) {
            ContentResolver resolver = getContentResolver();
            resolver.notifyChange(LocalRecycledFile.URI, null);
            resolver.notifyChange(CloudRecycleUtils.CLOUD_RECYCLED_FILE_TABLE_URI, null);
            RecycleUtils.startAsyncAlbumInfo();
        }
    }
}
