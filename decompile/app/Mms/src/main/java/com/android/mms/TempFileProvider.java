package com.android.mms;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import com.huawei.cspcommon.MLog;
import java.io.File;
import java.io.FileNotFoundException;

public class TempFileProvider extends ContentProvider {
    public static final Uri SCRAP_CONTENT_URI = Uri.parse("content://mms_temp_file/scrapSpace");
    public static final Uri SCRAP_VCALENDAR_URI = Uri.parse("content://mms_temp_file/vcalendar_temp.vcs");
    public static final Uri SCRAP_VCARD_IMP_URI = Uri.parse("content://mms_temp_file/vcard_temp.vcf");
    public static final Uri SCRAP_VCARD_URI = Uri.parse("content://mms_temp_file/vCardSpace");
    public static final Uri SCRAP_VIDEO_URI = Uri.parse("content://mms_temp_file/scrapSpaceVideo.3gp");
    private static String TAG = "TempFileProvider";
    private static final UriMatcher sURLMatcher = new UriMatcher(-1);

    static {
        sURLMatcher.addURI("mms_temp_file", "scrapSpace", 1);
        sURLMatcher.addURI("mms_temp_file", "vCardSpace", 2);
        sURLMatcher.addURI("mms_temp_file", "scrapSpaceVideo.3gp", 3);
        sURLMatcher.addURI("mms_temp_file", "vcalendar_temp.vcs", 4);
        sURLMatcher.addURI("mms_temp_file", "vcard_temp.vcf", 5);
    }

    public boolean onCreate() {
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private ParcelFileDescriptor getTempStoreFd(String mode, String fileName) {
        ParcelFileDescriptor pfd = null;
        try {
            File file = new File(fileName);
            File parentFile = file.getParentFile();
            if (parentFile == null) {
                return null;
            }
            if (parentFile.exists() || parentFile.mkdirs()) {
                int modeFlags;
                if (mode.equals("r")) {
                    modeFlags = 268435456;
                } else {
                    modeFlags = 1006632960;
                }
                pfd = ParcelFileDescriptor.open(file, modeFlags);
                return pfd;
            }
            MLog.e(TAG, "[TempFileProvider] tempStoreFd:ParentFile Path does not exist!");
            return null;
        } catch (Exception ex) {
            MLog.e(TAG, "getTempStoreFd: error creating pfd for " + fileName, (Throwable) ex);
        }
    }

    public String getType(Uri uri) {
        return "*/*";
    }

    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        String filePath;
        switch (sURLMatcher.match(uri)) {
            case 1:
                filePath = getScrapPicPath();
                break;
            case 2:
                filePath = getvCardPath(getContext());
                break;
            case 3:
                filePath = getScrapVideoPath();
                break;
            case 4:
                filePath = getvCalendarPath(getContext());
                break;
            case 5:
                filePath = getvCardImpPath(getContext());
                break;
            default:
                return null;
        }
        if (mode.contains("w")) {
            cleanScrapFile(getContext(), filePath);
        }
        return getTempStoreFd(mode, filePath);
    }

    public static String getScrapPath(Context context, String fileName) {
        File extDir = context.getExternalCacheDir();
        if (extDir == null) {
            return context.getCacheDir().getAbsolutePath() + "/" + fileName;
        }
        return extDir.getAbsolutePath() + "/" + fileName;
    }

    public static String getScrapPicPath() {
        return Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_PICTURES + "/" + ".temp" + ".jpg";
    }

    public static String getvCardPath(Context context) {
        return getScrapPath(context, ".big.tmp.vcard");
    }

    public static String getScrapVideoPath() {
        return Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_MOVIES + "/" + ".temp" + ".3gp";
    }

    public static String getvCalendarPath(Context context) {
        return context.getFileStreamPath("vcalendar_temp.vcs").getPath();
    }

    public static String getvCardImpPath(Context context) {
        return context.getFileStreamPath("vcard_temp.vcf").getPath();
    }

    public static Uri renameScrapFile(String fileExtension, String uniqueIdentifier) {
        File newTempFile;
        File oldTempFile;
        if (fileExtension != null && fileExtension.equals(".3gp")) {
            newTempFile = new File(Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_MOVIES + "/" + uniqueIdentifier + ".3gp");
            oldTempFile = new File(Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_MOVIES + "/" + ".temp" + ".3gp");
        } else if (fileExtension == null || !fileExtension.equals(".jpg")) {
            return null;
        } else {
            newTempFile = new File(Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_PICTURES + "/" + uniqueIdentifier + ".jpg");
            oldTempFile = new File(Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_PICTURES + "/" + ".temp" + ".jpg");
        }
        if (!newTempFile.delete()) {
            MLog.e(TAG, "delete newTempFile failed!!!");
        }
        if (oldTempFile.renameTo(newTempFile)) {
            return Uri.fromFile(newTempFile);
        }
        return null;
    }

    public static boolean isTempFile(String path) {
        return path.contains(".temp");
    }

    private void cleanScrapFile(Context context, String path) {
        File file = new File(path);
        if (file.exists() && file.length() > 0) {
            if (file.delete()) {
                MLog.d(TAG, "getScrapPath, delete old file ");
            } else {
                MLog.d(TAG, "getScrapPath, failed to delete old file ");
            }
        }
    }
}
