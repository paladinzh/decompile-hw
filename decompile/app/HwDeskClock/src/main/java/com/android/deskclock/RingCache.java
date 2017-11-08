package com.android.deskclock;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.os.UserManagerCompat;
import android.text.TextUtils;
import com.android.deskclock.alarmclock.Alarm;
import com.android.deskclock.alarmclock.Alarm.Columns;
import com.android.deskclock.alarmclock.Alarms;
import com.android.util.HwLog;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import libcore.io.Streams;

public class RingCache {
    private static RingCache sInstance;
    private Context mContext;
    private Handler mHandler = null;
    private HandlerThread mHandlerThread = new HandlerThread("RingCache");
    private int mIsCredentialProtected = -1;

    private static class RingHander extends Handler {
        public RingHander(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            boolean z = false;
            super.handleMessage(msg);
            switch (msg.arg1) {
                case 1:
                    RingCache.getInstance().addRingCacheInner(msg.obj);
                    return;
                case 2:
                    Uri uri = (Uri) msg.obj;
                    int status = msg.arg2;
                    RingCache instance = RingCache.getInstance();
                    if (status == 0) {
                        z = true;
                    }
                    instance.deleteRingCacheInner(uri, z);
                    return;
                case 3:
                    RingCache.getInstance().checkRingCacheInner(((Boolean) msg.obj).booleanValue());
                    return;
                default:
                    return;
            }
        }
    }

    private RingCache() {
        Handler ringHander;
        if (!this.mHandlerThread.isAlive()) {
            this.mHandlerThread.start();
        }
        Looper looper = this.mHandlerThread.getLooper();
        if (looper == null) {
            ringHander = new RingHander();
        } else {
            ringHander = new RingHander(looper);
        }
        this.mHandler = ringHander;
    }

    public static synchronized RingCache getInstance() {
        RingCache ringCache;
        synchronized (RingCache.class) {
            if (sInstance == null) {
                sInstance = new RingCache();
            }
            ringCache = sInstance;
        }
        return ringCache;
    }

    private String getCachePath(Context context) {
        if (context == null) {
            return "";
        }
        File file = context.createDeviceProtectedStorageContext().getFilesDir();
        if (file == null) {
            return "";
        }
        return file.getAbsolutePath() + "/ring/";
    }

    private int getCacheFileCount(Context context) {
        File file = new File(getCachePath(context));
        if (!file.exists()) {
            HwLog.i("RingCache", "mkdir result = " + file.mkdir());
        }
        if (!file.isDirectory()) {
            return -1;
        }
        String[] list = file.list();
        return list == null ? 0 : list.length;
    }

    private String getCacheFileName(String filePath) throws Exception {
        if (TextUtils.isEmpty(filePath)) {
            return "";
        }
        int index = filePath.lastIndexOf(File.separator);
        if (index == -1) {
            HwLog.w("RingCache", "file path index error.");
            return "";
        }
        return URLEncoder.encode(filePath.substring(0, index + 1), "UTF-8") + filePath.substring(index + 1);
    }

    public String getSourceFilePath(String cacheFile) throws Exception {
        if (TextUtils.isEmpty(cacheFile)) {
            return "";
        }
        String splitStr = URLEncoder.encode(File.separator, "UTF-8");
        int index = cacheFile.lastIndexOf(splitStr);
        if (index == -1) {
            HwLog.w("RingCache", "cacheFile path index error.");
            return "";
        }
        String encoderPath = cacheFile.substring(0, splitStr.length() + index);
        return URLDecoder.decode(encoderPath, "UTF-8") + cacheFile.substring(splitStr.length() + index);
    }

    private String checkSourceFileUri(String cacheFileName) {
        try {
            String sourcePath = getSourceFilePath(cacheFileName);
            File sourceFile = new File(sourcePath);
            File cacheFile = new File(getCachePath(this.mContext) + cacheFileName);
            if (!sourceFile.exists()) {
                HwLog.d("RingCache", "sourceFile not exist.");
                deleteCacheFile(cacheFile);
                return "";
            } else if (cacheFile.length() == sourceFile.length()) {
                return sourcePath;
            } else {
                HwLog.w("RingCache", "souce file is not same as cache file");
                deleteCacheFile(cacheFile);
                addRingCache(this.mContext, Uri.parse(sourcePath));
                return "";
            }
        } catch (Exception e) {
            HwLog.e("RingCache", e.getMessage());
            return "";
        }
    }

    public String getCacheFilePath(Context context, String sourcePath) {
        try {
            return getCachePath(context) + getCacheFileName(sourcePath);
        } catch (Exception e) {
            HwLog.e("RingCache", "getCacheFilePath " + e.getMessage());
            return "";
        }
    }

    public void addRingCache(Context context, Uri uri) {
        HwLog.i("RingCache", "addRingCache");
        if (context == null || uri == null) {
            HwLog.w("RingCache", "context or uri is null");
            return;
        }
        this.mContext = context.getApplicationContext();
        Message msg = Message.obtain();
        msg.arg1 = 1;
        msg.what = uri.toString().hashCode();
        msg.obj = uri;
        this.mHandler.removeMessages(msg.what);
        this.mHandler.sendMessageDelayed(msg, 100);
    }

    private void addRingCacheInner(Uri uri) {
        HwLog.i("RingCache", "addRingCacheInner");
        Context context = this.mContext;
        if (context == null || uri == null) {
            HwLog.w("RingCache", "context or uri is null");
            return;
        }
        long start = System.currentTimeMillis();
        if (!isUserUnlocked(context)) {
            HwLog.i("RingCache", "not protected");
        } else if (isSdCardRing(uri)) {
            addLocalRingCache(context, uri);
            HwLog.i("RingCache", "cost time = " + (System.currentTimeMillis() - start) + " ms");
        } else {
            HwLog.i("RingCache", "not sdcard ring.");
        }
    }

    private void addLocalRingCache(Context context, Uri uri) {
        Exception e;
        Throwable th;
        int cacheCount = getCacheFileCount(context);
        if (cacheCount == -1) {
            HwLog.w("RingCache", "cache dir error !");
        } else if (cacheCount >= 10) {
            HwLog.w("RingCache", "cache full.");
        } else {
            long fileSize = new File(uri.toString()).length();
            long dirSize = calculateDirectorySize(context);
            if (fileSize > 52428800 || fileSize + dirSize > 209715200) {
                HwLog.w("RingCache", " too large not cache");
                return;
            }
            File file = null;
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                ContentResolver resolver = context.getContentResolver();
                File cacheFile = new File(getCachePath(context) + getCacheFileName(uri.toString()));
                try {
                    if (cacheFile.exists()) {
                        HwLog.i("RingCache", "cache file is exist !");
                    } else {
                        HwLog.i("RingCache", "create file result = " + cacheFile.createNewFile());
                        inputStream = resolver.openInputStream(buildFileUri(uri));
                        outputStream = resolver.openOutputStream(Uri.fromFile(cacheFile));
                        if (inputStream == null || outputStream == null) {
                            deleteCacheFile(cacheFile);
                        } else {
                            Streams.copy(inputStream, outputStream);
                        }
                    }
                    closeStream(r9);
                    closeStream(r12);
                    file = cacheFile;
                } catch (Exception e2) {
                    e = e2;
                    file = cacheFile;
                    try {
                        HwLog.e("RingCache", "cacheRing " + e.getMessage());
                        deleteCacheFile(file);
                        closeStream(inputStream);
                        closeStream(outputStream);
                    } catch (Throwable th2) {
                        th = th2;
                        closeStream(inputStream);
                        closeStream(outputStream);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    file = cacheFile;
                    closeStream(inputStream);
                    closeStream(outputStream);
                    throw th;
                }
            } catch (Exception e3) {
                e = e3;
                HwLog.e("RingCache", "cacheRing " + e.getMessage());
                deleteCacheFile(file);
                closeStream(inputStream);
                closeStream(outputStream);
            }
        }
    }

    private void closeStream(Closeable in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                HwLog.e("RingCache", "closeStream ", e);
            }
        }
    }

    private long calculateDirectorySize(Context context) {
        File fold = new File(getCachePath(context));
        if (!fold.exists()) {
            HwLog.i("RingCache", "mkdir result = " + fold.mkdir());
        }
        long size = 0;
        if (fold.isDirectory()) {
            File[] files = fold.listFiles();
            if (files == null || files.length == 0) {
                return 0;
            }
            for (File file : files) {
                size += file.length();
            }
        }
        return size;
    }

    private Uri buildFileUri(Uri uri) {
        Uri buildUri = uri;
        if (TextUtils.isEmpty(uri.getScheme())) {
            return uri.buildUpon().scheme("file").build();
        }
        return buildUri;
    }

    private void deleteCacheFile(File file) {
        if (file != null) {
            HwLog.d("RingCache", "delete file result = " + file.delete());
        }
    }

    public void deleteRingCache(Context context, Uri uri, boolean ignoreState) {
        HwLog.i("RingCache", "delete ring cache");
        if (context == null || uri == null) {
            HwLog.w("RingCache", "context or uri is null");
            return;
        }
        this.mContext = context.getApplicationContext();
        Message msg = Message.obtain();
        msg.what = uri.toString().hashCode();
        msg.arg1 = 2;
        msg.arg2 = ignoreState ? 0 : 1;
        msg.obj = uri;
        this.mHandler.removeMessages(msg.what);
        this.mHandler.sendMessageDelayed(msg, 100);
    }

    public void updateRingCache(Context context, Uri uri, boolean enabled, boolean ignoreState) {
        if (context == null || uri == null) {
            HwLog.w("RingCache", "context or uri is null");
            return;
        }
        if (enabled) {
            getInstance().addRingCache(context, uri);
        } else {
            getInstance().deleteRingCache(context, uri, ignoreState);
        }
    }

    private void deleteRingCacheInner(Uri uri, boolean ignoreState) {
        Context context = this.mContext;
        if (context == null || uri == null) {
            HwLog.w("RingCache", "context or uri is null");
        } else if (!isSdCardRing(uri)) {
            HwLog.i("RingCache", "not sdcard ring,not delete");
        } else if (isAlertUriUsed(context.getContentResolver(), uri, ignoreState)) {
            HwLog.i("RingCache", "alert is used, not delete");
        } else {
            try {
                deleteCacheFile(new File(getCachePath(context) + getCacheFileName(uri.toString())));
            } catch (Exception e) {
                HwLog.e("RingCache", "deleteRingCache " + e.getMessage());
            }
        }
    }

    public boolean isCredentialProtected(Context context) {
        boolean z = true;
        if (context == null) {
            return false;
        }
        Context ctt = context.createCredentialProtectedStorageContext();
        if (ctt == null) {
            return false;
        }
        if (this.mIsCredentialProtected == -1) {
            int i;
            if (ctt.isCredentialProtectedStorage()) {
                i = 1;
            } else {
                i = 0;
            }
            this.mIsCredentialProtected = i;
            HwLog.w("RingCache", "isCredentialProtected sIsCredentialProtected: " + this.mIsCredentialProtected);
        }
        if (this.mIsCredentialProtected != 1) {
            z = false;
        }
        return z;
    }

    public boolean isUserUnlocked(Context context) {
        if (context == null || !isCredentialProtected(context)) {
            return true;
        }
        return UserManagerCompat.isUserUnlocked(context);
    }

    public boolean isSdCardRing(Uri uri) {
        if (uri != null) {
            return isSdCardRing(uri.toString());
        }
        return false;
    }

    private boolean isSdCardRing(String sourcePath) {
        if (TextUtils.isEmpty(sourcePath) || sourcePath.startsWith("content://settings/system") || sourcePath.startsWith("/system/")) {
            return false;
        }
        return true;
    }

    private boolean isAlertUriUsed(ContentResolver contentResolver, Uri uri, boolean ignoreState) {
        int count = 0;
        Cursor cursor = null;
        try {
            String[] ALARM_QUERY_COLUMNS = new String[]{"_id", "enabled", "alert"};
            String where = "alert=?";
            if (!ignoreState) {
                where = where + " AND enabled=1";
            }
            cursor = contentResolver.query(Columns.CONTENT_URI, ALARM_QUERY_COLUMNS, where, new String[]{uri.toString()}, null);
            count = cursor == null ? 0 : cursor.getCount();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            HwLog.e("RingCache", "isAlertUriUsed " + e.getMessage());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        HwLog.i("RingCache", "count =  " + count);
        if (count > 0) {
            return true;
        }
        return false;
    }

    public void checkRingCache(Context context, boolean isCheckDB) {
        HwLog.i("RingCache", "check ring cache");
        if (context == null) {
            HwLog.w("RingCache", "context is null");
        } else if (isUserUnlocked(context)) {
            this.mContext = context.getApplicationContext();
            Message msg = Message.obtain();
            msg.arg1 = 3;
            msg.obj = Boolean.valueOf(isCheckDB);
            this.mHandler.sendMessage(msg);
        } else {
            HwLog.w("RingCache", " phone locked.");
        }
    }

    private void checkRingCacheInner(boolean isCheckDB) {
        HwLog.i("RingCache", "checkRingCacheInner isCheckDB = " + isCheckDB);
        deleteUnusedCache();
        if (isCheckDB) {
            checkAlertUri();
        }
    }

    private void deleteUnusedCache() {
        boolean isDirectory;
        HwLog.i("RingCache", "deleteUnusedCache");
        File file = new File(getCachePath(this.mContext));
        if (file.exists()) {
            isDirectory = file.isDirectory();
        } else {
            isDirectory = false;
        }
        if (isDirectory) {
            String[] cacheFileNames = file.list();
            if (cacheFileNames == null) {
                HwLog.w("RingCache", "no cache file");
                return;
            }
            for (String fileName : cacheFileNames) {
                String uripath = checkSourceFileUri(fileName);
                if (!TextUtils.isEmpty(uripath)) {
                    deleteRingCacheInner(Uri.parse(uripath), false);
                }
            }
            return;
        }
        HwLog.w("RingCache", "cache dir not exist");
    }

    private void checkAlertUri() {
        HwLog.i("RingCache", "checkAlertUri");
        if (this.mContext != null) {
            Cursor cursor = null;
            try {
                cursor = Alarms.getAlarmsCursor(this.mContext.getContentResolver());
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        Alarm alarm = new Alarm(cursor);
                        if (alarm.enabled && isSdCardRing(alarm.alert)) {
                            addRingCache(this.mContext, alarm.alert);
                        }
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                HwLog.e("RingCache", "checkAlertUri " + e.getMessage());
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    public void clearOldCache(Context context) {
        int i = 0;
        HwLog.i("RingCache", "clearOldCache");
        if (context != null) {
            File file = context.createDeviceProtectedStorageContext().getCacheDir();
            if (file != null) {
                boolean isDirectory;
                File cacheDir = new File(file.getAbsolutePath() + "/ring/");
                if (cacheDir.exists()) {
                    isDirectory = cacheDir.isDirectory();
                } else {
                    isDirectory = false;
                }
                if (isDirectory) {
                    File[] files = cacheDir.listFiles();
                    if (files != null) {
                        int length = files.length;
                        while (i < length) {
                            deleteCacheFile(files[i]);
                            i++;
                        }
                        deleteCacheFile(cacheDir);
                        return;
                    }
                    return;
                }
                HwLog.w("RingCache", "cache dir not exist");
            }
        }
    }
}
