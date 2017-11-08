package com.huawei.gallery.media;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import com.android.gallery3d.common.Utils;
import com.huawei.gallery.media.database.MergedMedia;
import com.huawei.gallery.util.MediaSyncerHelper;
import com.huawei.gallery.util.MyPrinter;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MediaOperation {
    private static MyPrinter LOG = new MyPrinter("MediaOperation");
    private static final String[] PROJECTION = new String[]{"id", "_id", "_data", "type"};
    final String dbData;
    final int dbId;
    final int dbOp;
    final int keyId;

    MediaOperation(Cursor c) {
        this.keyId = c.getInt(0);
        this.dbId = c.getInt(1);
        this.dbData = c.getString(2);
        this.dbOp = c.getInt(3);
    }

    public boolean isStartToken() {
        return this.keyId == 1 && this.dbId < 0;
    }

    public static void deleteBatch(ContentResolver resolver, List<MediaOperation> operations) {
        StringBuffer args = new StringBuffer();
        for (MediaOperation op : operations) {
            args.append(",").append(op.keyId);
        }
        if (!MediaSyncerHelper.isMediaSyncerTerminated()) {
            LOG.d("deleteBatch result: " + resolver.delete(MergedMedia.OPERATION_URI, " id in ( " + args.substring(1) + " )", null));
        }
    }

    public static List<MediaOperation> queryBatch(ContentResolver resolver, int start, int count) {
        Throwable th;
        List<MediaOperation> ret = Collections.emptyList();
        Uri uri = MergedMedia.OPERATION_URI.buildUpon().appendQueryParameter("limit", start + "," + count).build();
        Closeable c = null;
        try {
            if (MediaSyncerHelper.isMediaSyncerTerminated()) {
                Utils.closeSilently(null);
                return ret;
            }
            c = resolver.query(uri, PROJECTION, null, null, null);
            if (c != null) {
                List<MediaOperation> ret2 = new ArrayList(c.getCount());
                while (c.moveToNext()) {
                    if (MediaSyncerHelper.isMediaSyncerTerminated()) {
                        List<MediaOperation> emptyList = Collections.emptyList();
                        Utils.closeSilently(c);
                        return emptyList;
                    }
                    try {
                        ret2.add(new MediaOperation(c));
                    } catch (Throwable th2) {
                        th = th2;
                        ret = ret2;
                    }
                }
                ret = ret2;
            }
            Utils.closeSilently(c);
            return ret;
        } catch (Throwable th3) {
            th = th3;
            Utils.closeSilently(c);
            throw th;
        }
    }

    public String toString() {
        return String.format("{id:%s,path:%s,op:%s}", new Object[]{Integer.valueOf(this.dbId), this.dbData, Integer.valueOf(this.dbOp)});
    }
}
