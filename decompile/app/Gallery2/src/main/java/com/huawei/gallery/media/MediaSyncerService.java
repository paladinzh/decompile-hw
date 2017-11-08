package com.huawei.gallery.media;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.os.Build.VERSION;
import android.os.Message;
import com.android.gallery3d.common.Utils;
import com.huawei.gallery.media.database.MergedMedia;
import com.huawei.gallery.service.AsyncService;
import com.huawei.gallery.util.MediaSyncerHelper;
import com.huawei.gallery.util.MyPrinter;
import java.util.List;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class MediaSyncerService extends AsyncService {
    private static MyPrinter LOG = new MyPrinter("MediaSyncerService");
    private volatile boolean mIsSyncing = false;

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (this.mIsSyncing || MediaSyncerHelper.isMediaSyncerTerminated()) {
            return 2;
        }
        this.mIsSyncing = true;
        LOG.d("[onStartCommand] start to sync.");
        CloudLocalSyncService.startLocalSync(this);
        return super.onStartCommand(intent, flags, startId);
    }

    public boolean handleMessage(Message msg) {
        if (hasPermission()) {
            this.mIsSyncing = false;
            long start = System.currentTimeMillis();
            try {
                ContentResolver resolver = getContentResolver();
                List<MediaOperation> operations = MediaOperation.queryBatch(resolver, 0, 1);
                int maxIdSynced = LocalSyncToken.queryMaxSyncId(resolver);
                if (!operations.isEmpty() && ((MediaOperation) operations.get(0)).isStartToken()) {
                    LOG.d("media provider is cleaned.");
                    if (maxIdSynced > 0) {
                        LocalSyncToken.deleteAll(resolver, 300);
                    } else {
                        LOG.d("gallery has no record, maybe cleaned.");
                    }
                    MediaOperation.deleteBatch(resolver, operations);
                }
                resolver.insert(MergedMedia.SYNC_URI.buildUpon().appendPath("1").build(), null);
                LocalSyncToken.syncAll(resolver, maxIdSynced, 300);
                syncWithOperation(resolver);
                LOG.d("sync done with operations. time cost(include sleep time): " + (System.currentTimeMillis() - start));
                resolver.insert(MergedMedia.SYNC_URI.buildUpon().appendPath("0").build(), null);
            } catch (SQLiteDatabaseCorruptException e) {
                LOG.d("SQLiteDatabaseCorruptException e");
            } catch (Exception e2) {
                LOG.d("Exception, msg: " + e2.getMessage());
            }
            synchronized (this) {
                Utils.waitWithoutInterrupt(this, 800);
            }
            if (!this.mIsSyncing) {
                CloudLocalSyncService.stopLocalSync(this);
                stopSelf();
            }
            return true;
        }
        LOG.d("gallery has no permission [android.permission.WRITE_EXTERNAL_STORAGE] ignore.");
        Message newMsg = this.mServiceHandler.obtainMessage(msg.what, msg.arg1, msg.arg2, msg.obj);
        newMsg.getTarget().sendMessageDelayed(newMsg, 800);
        return true;
    }

    private void syncWithOperation(ContentResolver resolver) {
        long startTime = System.currentTimeMillis();
        List<MediaOperation> operations = MediaOperation.queryBatch(resolver, 0, 300);
        int totalCount = 0;
        int sleepTimes = 0;
        while (!operations.isEmpty()) {
            totalCount += operations.size();
            if (LocalSyncToken.syncBatch(resolver, operations)) {
                MediaOperation.deleteBatch(resolver, operations);
            }
            synchronized (this) {
                Utils.waitWithoutInterrupt(this, 200);
                sleepTimes++;
            }
            operations = MediaOperation.queryBatch(resolver, 0, 300);
        }
        LOG.d("sleep time(ms): " + (sleepTimes * SmsCheckResult.ESCT_200));
        LOG.d("sync with operation record[" + totalCount + "] cost time: " + (System.currentTimeMillis() - startTime));
    }

    private boolean hasPermission() {
        boolean z = true;
        if (VERSION.SDK_INT < 23) {
            return true;
        }
        try {
            if (checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
                z = false;
            }
            return z;
        } catch (Exception e) {
            LOG.d("No checkSelfPermission method!");
            return true;
        }
    }

    protected String getServiceTag() {
        return "MediaSyncerService thread";
    }

    protected void decorateMsg(Message message, Intent intent, int startId) {
        message.arg1 = startId;
        if (intent != null) {
            message.obj = intent.getExtras();
        }
    }
}
