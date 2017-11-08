package com.huawei.gallery.extfile;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemProperties;
import android.os.UserHandle;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.gadget.XmlUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.fyusion.sdk.common.ext.util.FyuseUtils;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class FyuseFile {
    private static byte[] mFyuseTag;
    private static int mFyuseTagLen;
    private static FileChecker<FuseFileData> sChecker = new FileChecker<FuseFileData>() {
        protected FuseFileData onFileChecked(String filePath, FuseFileData inout) throws FileNotFoundException, IOException {
            Throwable th;
            RandomAccessFile randomAccessFile = null;
            try {
                FuseFileData retData = new FuseFileData();
                RandomAccessFile file = new RandomAccessFile(filePath, "r");
                try {
                    long length = file.length();
                    file.seek((length - ((long) FyuseFile.mFyuseTagLen)) - 4);
                    byte[] tagIdLengthByte = new byte[4];
                    byte[] tagByte = new byte[FyuseFile.mFyuseTagLen];
                    if (file.read(tagIdLengthByte) != 4) {
                        FileChecker.close(file);
                        return null;
                    }
                    int tagIdLength = GalleryUtils.littleEdianByteArrayToInt(tagIdLengthByte, 0, 4);
                    if (file.read(tagByte) == FyuseFile.mFyuseTagLen && Arrays.equals(tagByte, FyuseFile.mFyuseTag)) {
                        retData.tag = new String(FyuseFile.mFyuseTag, XmlUtils.INPUT_ENCODING);
                        GalleryLog.d("FyuseFile", "tagID length: " + tagIdLength);
                        if (tagIdLength > 0) {
                            byte[] tagId = new byte[tagIdLength];
                            file.seek(((length - ((long) FyuseFile.mFyuseTagLen)) - 4) - ((long) tagIdLength));
                            if (file.read(tagId) == tagIdLength) {
                                retData.id = new String(tagId, XmlUtils.INPUT_ENCODING);
                            }
                        }
                        GalleryLog.d("FyuseFile", " (method one) find fuse file, tag id : " + retData.id);
                    } else {
                        GalleryLog.d("FyuseFile", "cann't find tag: " + new String(tagByte, XmlUtils.INPUT_ENCODING));
                    }
                    FileChecker.close(file);
                    return retData;
                } catch (Throwable th2) {
                    th = th2;
                    randomAccessFile = file;
                    FileChecker.close(randomAccessFile);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                FileChecker.close(randomAccessFile);
                throw th;
            }
        }
    };
    private static boolean sExistFyuse = false;
    private static boolean sIsFyuseSDKSupport;

    private static class FuseFileData {
        String id;
        String tag;

        private FuseFileData() {
        }
    }

    static {
        mFyuseTag = null;
        mFyuseTagLen = 6;
        sIsFyuseSDKSupport = false;
        try {
            mFyuseTag = "#FYUSE".getBytes(XmlUtils.INPUT_ENCODING);
            mFyuseTagLen = mFyuseTag.length;
        } catch (UnsupportedEncodingException e) {
            GalleryLog.e("FyuseFile", "UnsupportedEncodingException: " + e);
        }
        try {
            System.loadLibrary("vislib_jni");
            sIsFyuseSDKSupport = SystemProperties.getBoolean("ro.config.hw_fyuse_enable", false);
        } catch (UnsatisfiedLinkError e2) {
            sIsFyuseSDKSupport = false;
        }
    }

    public static boolean startViewFyuseFile(Context context, String filePath) {
        Intent intent = new Intent("android.intent.action.VIEW");
        if (makeIntentCommentIntent(intent, filePath, "")) {
            return startActivity(context, intent);
        }
        return false;
    }

    public static boolean startEditFyuseFile(Context context, String filePath) {
        Intent intent = new Intent("android.intent.action.EDIT");
        if (makeIntentCommentIntent(intent, filePath, "")) {
            return startActivity(context, intent);
        }
        return false;
    }

    public static boolean startShareFyuseFile(Context context, String filePath, String mode) {
        Intent intent = new Intent("android.intent.action.SEND");
        if (!makeIntentCommentIntent(intent, filePath, mode)) {
            return false;
        }
        intent.setComponent(new ComponentName("com.fyusion.sdk", "com.fyusion.sdk.ShareActivity"));
        return startActivity(context, intent);
    }

    public static boolean startDeleteFyuseFile(ContentResolver resolver, String filePath, int fileType) {
        if (fileType == 20) {
            FyuseUtils.delete(new File(filePath));
            return true;
        }
        if (fileType == 11) {
            if (isSupport3DPanoramaAPK()) {
                String tagId = getTagId(filePath);
                if (!(resolver == null || tagId == null)) {
                    try {
                        String[] args = new String[]{filePath};
                        resolver.delete(Uri.parse("content://com.fyusion.fyuse.contentprovider"), tagId, args);
                        GalleryLog.d("FyuseFile", "startDeleteFyuseFile,args[0]=" + args[0]);
                        return true;
                    } catch (RuntimeException e) {
                        GalleryLog.e("FyuseFile", "delete fyuse files failed");
                        return false;
                    }
                }
            }
            GalleryLog.d("FyuseFile", "when delete Fyuse APK does not exist");
            return false;
        }
        return false;
    }

    private static String getTagId(String filePath) {
        if (!sExistFyuse) {
            return null;
        }
        FuseFileData ret = (FuseFileData) sChecker.checkFile(filePath, null);
        if (ret == null || ret.tag == null) {
            return null;
        }
        return ret.id;
    }

    private static boolean startActivity(Context context, Intent intent) {
        try {
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            GalleryLog.d("FyuseFile", "startActivity failed");
            return false;
        }
    }

    public static void checkFusePackage(Context context) {
        boolean z = false;
        if (context != null) {
            if (!context.getPackageManager().queryIntentActivities(new Intent().setPackage("com.fyusion.sdk"), 0).isEmpty()) {
                z = true;
            }
            sExistFyuse = z;
        }
        GalleryLog.d("FyuseFile", "sExistFyuse " + sExistFyuse);
    }

    public static boolean isFyuseInstalled() {
        return sExistFyuse;
    }

    private static boolean makeIntentCommentIntent(Intent intent, String filePath, String mode) {
        String tagId = getTagId(filePath);
        GalleryLog.d("FyuseFile", "makeIntentCommentIntent,tagId=" + tagId + ",filePath=" + filePath);
        if (tagId == null) {
            return false;
        }
        GalleryLog.d("FyuseFile", "view fuse tag id: " + tagId);
        intent.putExtra("fyuse_id", tagId);
        intent.putExtra("THUMBNAIL_PATHS", filePath);
        intent.setPackage("com.fyusion.sdk");
        intent.putExtra("creator", "FyuseSDK");
        intent.putExtra("MODE", mode);
        return true;
    }

    public static boolean queryFyuseData(Context context, String filePath) {
        boolean z;
        Closeable closeable = null;
        try {
            String tagId = getTagId(filePath);
            if (tagId != null) {
                closeable = context.getContentResolver().query(Uri.parse("content://com.fyusion.fyuse.contentprovider"), null, tagId, null, null, null);
                if (closeable != null && closeable.getCount() > 0) {
                    GalleryLog.d("FyuseFile", "cursor.getCount()=" + closeable.getCount());
                    z = true;
                    return z;
                }
            }
            Utils.closeSilently(closeable);
        } catch (Exception e) {
            z = "FyuseFile";
            GalleryLog.e(z, "queryFyuseData,e = " + e);
        } finally {
            Utils.closeSilently(closeable);
        }
        return false;
    }

    public static void updateFyusePath(ContentResolver resolver, String filePath) {
        if (isSupport3DPanoramaAPK()) {
            try {
                String tagId = getTagId(filePath);
                GalleryLog.d("FyuseFile", "updateFyuseData, filePath = " + filePath + ",tagId = " + tagId);
                if (tagId != null) {
                    ContentValues values = new ContentValues();
                    values.put("THUMBNAIL_PATHS", filePath);
                    resolver.update(Uri.parse("content://com.fyusion.fyuse.contentprovider"), values, tagId, null);
                }
            } catch (RuntimeException e) {
                GalleryLog.e("FyuseFile", "updateFyuseData fail,filePath = " + filePath + ",tagId = " + "");
            }
            return;
        }
        GalleryLog.d("FyuseFile", "when update Fyuse APK does not exist");
    }

    public static void updateRenamedPath(ContentResolver resolver, File oldFile, File newFile, int fileType) {
        updateFyusePath(resolver, newFile.getAbsolutePath());
        startDeleteFyuseFile(resolver, oldFile.getAbsolutePath(), fileType);
    }

    public static boolean isSupport3DPanorama() {
        return !isSupport3DPanoramaSDK() ? isSupport3DPanoramaAPK() : true;
    }

    public static boolean isSupport3DPanoramaSDK() {
        return sIsFyuseSDKSupport;
    }

    public static boolean isSupport3DPanoramaAPK() {
        if (ApiHelper.HAS_MEDIA_COLUMNS_SPECIAL_FILE_TYPE && isFyuseInstalled() && UserHandle.myUserId() == 0) {
            return true;
        }
        return false;
    }

    public static boolean isProcessedFyuseFile(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        return FyuseUtils.isFyuseProcessed(file);
    }
}
