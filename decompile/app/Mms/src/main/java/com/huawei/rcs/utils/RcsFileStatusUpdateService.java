package com.huawei.rcs.utils;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RcsFileStatusUpdateService extends Service {
    private static final String FT_TAG = (TAG + " FileTrans: ");
    private static final String TAG = RcsFileStatusUpdateService.class.getSimpleName();
    private ExecutorService executorService;

    private class UpdateFileStatusTask implements Runnable {
        private UpdateFileStatusTask() {
        }

        public void run() {
            MLog.i(RcsFileStatusUpdateService.FT_TAG, "update file status run");
            Context context = RcsFileStatusUpdateService.this.getApplicationContext();
            ContentResolver cr = RcsFileStatusUpdateService.this.getContentResolver();
            ContentValues cv = new ContentValues();
            cv.put("NEED_TO_NOTIFY_CHANGE", Boolean.valueOf(true));
            cv.put("transfer_status", Integer.valueOf(1001));
            Cursor c = SqliteWrapper.query(context, cr, Uri.parse("content://rcsim/file_trans"), new String[]{"msg_id", "chat_type"}, "transfer_status = 1000 OR transfer_status = 1007", null, null);
            if (c == null || !c.moveToFirst()) {
                if (c != null) {
                    c.close();
                }
                RcsFileStatusUpdateService.this.stopSelf();
                MLog.i(RcsFileStatusUpdateService.FT_TAG, "RcsFileStatusUpdateService stop");
            }
            do {
                long msg_id = c.getLong(c.getColumnIndex("msg_id"));
                int chatType = c.getInt(c.getColumnIndex("chat_type"));
                MLog.i(RcsFileStatusUpdateService.FT_TAG, "UpdateFileStatusTask msg_id = " + msg_id + ", chatType = " + chatType);
                SqliteWrapper.update(context, cr, Uri.parse("content://rcsim/file_trans"), cv, "msg_id = " + msg_id + " AND chat_type = " + chatType, null);
                ContentValues contentValue = new ContentValues();
                Context context2;
                ContentResolver contentResolver;
                switch (chatType) {
                    case 1:
                    case 3:
                        contentValue.put("status", Integer.valueOf(64));
                        context2 = context;
                        contentResolver = cr;
                        SqliteWrapper.update(context2, contentResolver, Uri.parse("content://rcsim/chat"), contentValue, "_id = ?", new String[]{String.valueOf(msg_id)});
                        break;
                    case 2:
                        contentValue.put("status", Integer.valueOf(4));
                        context2 = context;
                        contentResolver = cr;
                        SqliteWrapper.update(context2, contentResolver, Uri.parse("content://rcsim/rcs_group_message"), contentValue, "_id = ?", new String[]{String.valueOf(msg_id)});
                        break;
                }
            } while (c.moveToNext());
            if (c != null) {
                c.close();
            }
            RcsFileStatusUpdateService.this.stopSelf();
            MLog.i(RcsFileStatusUpdateService.FT_TAG, "RcsFileStatusUpdateService stop");
        }
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        this.executorService = Executors.newFixedThreadPool(5);
        if (this.executorService.submit(new UpdateFileStatusTask()) == null) {
            MLog.i(FT_TAG, "executorService.submit is null");
        }
        return 2;
    }
}
