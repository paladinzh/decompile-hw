package com.huawei.gallery.photoshare.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import com.huawei.android.cg.vo.ShareReceiver;
import com.huawei.gallery.media.database.MergedMedia;
import java.util.ArrayList;

public class PhotoShareNoHwAccount {
    public static final Uri URI = MergedMedia.URI.buildUpon().appendPath("t_local_no_HwAccount").build();
    private Context mContext;

    public PhotoShareNoHwAccount(Context context) {
        this.mContext = context;
    }

    public static void createOrUpdateTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS t_local_no_HwAccount");
        db.execSQL(new StringBuffer("CREATE TABLE IF NOT EXISTS ").append("t_local_no_HwAccount").append(" (").append("owner_account").append(" TEXT NOT NULL,").append("album_ID").append(" TEXT NOT NULL,").append("account").append(" TEXT NOT NULL,").append("nickname").append(" TEXT,").append(" PRIMARY KEY (").append("owner_account").append(",").append("album_ID").append(",").append("account").append(")").append(");").toString());
    }

    public void insert(String owner, String albumId, String account, String nickname) {
        ContentValues values = new ContentValues();
        values.put("owner_account", owner);
        values.put("album_ID", albumId);
        values.put("account", account);
        values.put("nickname", nickname);
        this.mContext.getContentResolver().insert(URI, values);
    }

    public ArrayList<ShareReceiver> query(String owner, String albumId) {
        ArrayList<ShareReceiver> result = new ArrayList();
        Cursor cursor = this.mContext.getContentResolver().query(URI, new String[]{"account", "nickname"}, "owner_account=? AND album_ID=?", new String[]{owner, albumId}, "rowid");
        if (cursor == null) {
            return result;
        }
        while (cursor.moveToNext()) {
            try {
                ShareReceiver receiver = new ShareReceiver();
                receiver.setReceiverAcc(cursor.getString(0));
                receiver.setReceiverName(cursor.getString(1));
                receiver.setStatus(-1);
                result.add(receiver);
            } finally {
                cursor.close();
            }
        }
        return result;
    }

    public void delete(ArrayList<ShareReceiver> list, String owner, String albumId) {
        if (list.size() != 0) {
            StringBuilder idSetBuilder = new StringBuilder();
            boolean first = true;
            String[] whereArgs = new String[(list.size() + 2)];
            whereArgs[0] = owner;
            whereArgs[1] = albumId;
            int position = 2;
            for (ShareReceiver receiver : list) {
                if (first) {
                    first = false;
                    idSetBuilder.append("?");
                } else {
                    idSetBuilder.append(",?");
                }
                int position2 = position + 1;
                whereArgs[position] = receiver.getReceiverAcc();
                position = position2;
            }
            this.mContext.getContentResolver().delete(URI, "owner_account = ? AND album_ID = ? AND account IN(" + idSetBuilder.toString() + ")", whereArgs);
        }
    }

    public void delete(String owner, String albumId) {
        String[] whereArgs = new String[]{owner, albumId};
        this.mContext.getContentResolver().delete(URI, "owner_account = ? AND album_ID = ? ", whereArgs);
    }
}
