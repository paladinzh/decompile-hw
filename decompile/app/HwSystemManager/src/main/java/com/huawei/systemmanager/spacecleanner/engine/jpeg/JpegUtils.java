package com.huawei.systemmanager.spacecleanner.engine.jpeg;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.os.SystemProperties;
import android.provider.MediaStore.Images.Media;
import android.text.TextUtils;
import com.huawei.systemmanager.comm.Storage.StorageHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class JpegUtils {
    private static final String CAMERA_IMAGE_BUCKET_NAME = (Environment.getExternalStorageDirectory().toString() + CAMERA_PATH);
    public static final String CAMERA_PATH = SystemProperties.get("ro.hwcamera.directory", "/DCIM/Camera");
    private static String EXTERNAL_CAMERA_PATH_ID;
    private static final String INTERNAL_CAMERA_PATH_ID = getBucketId(CAMERA_IMAGE_BUCKET_NAME);

    public static synchronized void getExternalPath() {
        synchronized (JpegUtils.class) {
            if (StorageHelper.getStorage().isSdcardaviliable()) {
                for (String str : StorageHelper.getStorage().getSdcardRootPath()) {
                    if (!TextUtils.isEmpty(str)) {
                        EXTERNAL_CAMERA_PATH_ID = getBucketId(str + CAMERA_PATH);
                    }
                }
            }
        }
    }

    public static String getBucketId(String path) {
        return String.valueOf(path.toLowerCase(Locale.ENGLISH).hashCode());
    }

    private static List<String> queryPhoto(Context context, String id) {
        ArrayList<String> result = new ArrayList();
        if (TextUtils.isEmpty(id)) {
            return result;
        }
        String selection = "bucket_id = ?";
        Cursor cursor = context.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, new String[]{"_data"}, "bucket_id = ?", new String[]{id}, null);
        if (cursor == null) {
            return result;
        }
        if (cursor.moveToFirst()) {
            int dataColumn = cursor.getColumnIndexOrThrow("_data");
            do {
                result.add(cursor.getString(dataColumn));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    private static List<String> getInternalCameraPhoto(Context context) {
        return queryPhoto(context, INTERNAL_CAMERA_PATH_ID);
    }

    private static List<String> getExternalCameraPhoto(Context context) {
        getExternalPath();
        return queryPhoto(context, EXTERNAL_CAMERA_PATH_ID);
    }

    public static List<String> getCameraPhoto(Context context) {
        List<String> result = new ArrayList();
        result.addAll(getInternalCameraPhoto(context));
        if (StorageHelper.getStorage().isSdcardaviliable()) {
            result.addAll(getExternalCameraPhoto(context));
        }
        return result;
    }
}
