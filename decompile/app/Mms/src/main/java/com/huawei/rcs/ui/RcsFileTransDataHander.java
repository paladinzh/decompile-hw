package com.huawei.rcs.ui;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.huawei.cspcommon.ex.SqliteWrapper;
import java.util.List;

public class RcsFileTransDataHander {
    static final String[] PROJECTION = new String[]{"msg_id", "thread_id", "date", "file_name", "file_size", "file_type", "file_content", "file_icon", "transfer_status", "trans_size", "chat_type", "global_trans_id"};

    private static void insertFTDB(Context context, ContentValues cvs) {
        SqliteWrapper.insert(context, context.getContentResolver(), Uri.parse("content://rcsim/file_trans"), cvs);
    }

    public static void addNewFavTransTransRecord(final Context context, final List<Long> mIds, final int chatType) {
        new Thread(new Runnable() {
            public void run() {
                if (mIds.size() > 0) {
                    for (int i = 0; i < mIds.size(); i++) {
                        Cursor cr = SqliteWrapper.query(context, Uri.parse("content://rcsim/file_trans"), RcsFileTransDataHander.PROJECTION, "msg_id=" + ((Long) mIds.get(i)).longValue() + " AND " + "chat_type" + "=" + chatType, null, null);
                        if (cr != null) {
                            try {
                                if (cr.moveToFirst()) {
                                    RcsFileTransDataHander.insertFTDB(context, RcsFileTransDataHander.copyDataToContentValues(cr, chatType));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (cr != null) {
                            cr.close();
                        }
                    }
                }
            }
        }).start();
    }

    private static ContentValues copyDataToContentValues(Cursor cursor, int chatType) {
        ContentValues cvs = new ContentValues();
        cvs.put("msg_id", Long.valueOf(cursor.getLong(cursor.getColumnIndex("msg_id"))));
        cvs.put("thread_id", Long.valueOf(cursor.getLong(cursor.getColumnIndex("thread_id"))));
        cvs.put("date", Long.valueOf(cursor.getLong(cursor.getColumnIndex("date"))));
        cvs.put("file_name", cursor.getString(cursor.getColumnIndex("file_name")));
        cvs.put("file_size", Long.valueOf(cursor.getLong(cursor.getColumnIndex("file_size"))));
        cvs.put("file_type", cursor.getString(cursor.getColumnIndex("file_type")));
        cvs.put("file_content", cursor.getString(cursor.getColumnIndex("file_content")));
        cvs.put("file_icon", cursor.getString(cursor.getColumnIndex("file_icon")));
        cvs.put("transfer_status", Integer.valueOf(cursor.getInt(cursor.getColumnIndex("transfer_status"))));
        cvs.put("trans_size", Long.valueOf(cursor.getLong(cursor.getColumnIndex("trans_size"))));
        cvs.put("global_trans_id", cursor.getString(cursor.getColumnIndex("global_trans_id")));
        if (1 == chatType) {
            chatType = 100;
        } else if (2 == chatType) {
            chatType = 101;
        } else if (3 == chatType) {
            chatType = 102;
        }
        cvs.put("chat_type", Integer.valueOf(chatType));
        return cvs;
    }
}
