package com.huawei.systemmanager.spacecleanner.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Audio.Media;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.text.TextUtils;
import com.huawei.systemmanager.comm.misc.CursorHelper;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;
import java.io.File;

public class MediaUtil {
    public static final String APK_SELECTION = "_data LIKE '%.apk'";
    public static final Uri AUDIO_URI = Media.getContentUri(EXTERNAL);
    private static final String EXTERNAL = "external";
    public static final Uri FILE_URI = Files.getContentUri(EXTERNAL);
    public static final Uri PHOTO_RUI = Images.Media.getContentUri(EXTERNAL);
    private static final String TAG = "MediaProviderVisitor";
    public static final Uri VIDEO_RUI = Video.Media.getContentUri(EXTERNAL);

    public interface DataHandler {
        boolean handlerData(String str);
    }

    public static void queryApk(Context ctx, DataHandler dataHandler) {
        queryFile(ctx, APK_SELECTION, dataHandler);
    }

    private static void queryFile(Context ctx, String selection, DataHandler dataHandler) {
        Cursor cursor = null;
        try {
            cursor = ctx.getContentResolver().query(FILE_URI, new String[]{"title", "_data"}, selection, null, null);
            if (cursor != null) {
                HwLog.i(TAG, "total apk file number:" + cursor.getCount());
                while (cursor.moveToNext()) {
                    String path = cursor.getString(1);
                    if (!new File(path).exists()) {
                        HwLog.i(TAG, "file is not exist, ignore.");
                    } else if (!dataHandler.handlerData(path)) {
                        break;
                    }
                }
                CursorHelper.closeCursor(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CursorHelper.closeCursor(cursor);
        }
    }

    public static void deleteMediaProvider(Uri uri, String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            if (new File(filePath).exists()) {
                HwLog.i(TAG, "deleteMediaProvider failed, file is exist.");
                return;
            }
            GlobalContext.getContext().getContentResolver().delete(uri, "_data=?", new String[]{filePath});
        }
    }
}
