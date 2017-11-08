package com.huawei.keyguard.support.magazine;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.OperationCanceledException;
import android.os.SystemProperties;
import android.text.TextUtils;
import com.fyusion.sdk.common.FyuseSDK;
import com.fyusion.sdk.common.ext.ProcessError;
import com.fyusion.sdk.common.ext.ProcessItem;
import com.fyusion.sdk.common.ext.ProcessorListener;
import com.fyusion.sdk.common.ext.util.FyuseUtils;
import com.fyusion.sdk.common.ext.util.FyuseUtils.FyuseContainerVersion;
import com.fyusion.sdk.common.ext.util.exif.ExifInterface;
import com.fyusion.sdk.processor.FyuseProcessor;
import com.fyusion.sdk.viewer.FyuseViewer;
import com.fyusion.sdk.viewer.ext.localfyuse.module.LocalFyuseModule;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.support.magazine.BigPictureInfo.DescriptionInfo;
import com.huawei.keyguard.util.HwLog;
import fyusion.vislib.BuildConfig;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

public class HwFyuseUtils {
    private static final Uri CONTENT_URI_PICTURES = Uri.parse("content://com.android.huawei.magazineunlock/pictures");
    private static int mCount = 0;
    private static Runnable mFyuseSystemGC = new Runnable() {
        public void run() {
            FyuseViewer.get(GlobalContext.getContext()).clearMemory();
            System.gc();
        }
    };
    private static Runnable mInitFyuseSDKRunner = new Runnable() {
        public void run() {
            Context context = GlobalContext.getContext();
            HwFyuseUtils.recordMagazineEnableStatus(HwFyuseUtils.isMagazineSwitchEnale(context));
            try {
                FyuseViewer.get(context.getApplicationContext()).registerModule(new LocalFyuseModule());
            } catch (NullPointerException e) {
                HwFyuseUtils.sIsFyuseSDKSupport = false;
                HwLog.e("HwFyuseUtils", "registerModule got NullPointerException >>>>> " + e.toString());
            } catch (Exception e2) {
                HwFyuseUtils.sIsFyuseSDKSupport = false;
                HwLog.e("HwFyuseUtils", "registerModule got Exception >>>>> " + e2.toString());
            }
            if (HwFyuseUtils.sIsFyuseSDKSupport) {
                HwFyuseUtils.updateAllPicFormatDataUntilSDCardMounted(context);
            }
        }
    };
    private static boolean mIsMagazineEnable = true;
    private static HashMap<String, Boolean> mMagazineFileNameMap = new HashMap();
    private static Object object = new Object();
    private static boolean sIsChinaVersion;
    private static boolean sIsFyuseSDKSupport;

    static {
        boolean z = true;
        sIsFyuseSDKSupport = false;
        sIsChinaVersion = false;
        try {
            if (!SystemProperties.getBoolean("ro.config.hw_kg_fyuse_enable", false)) {
                z = false;
            } else if (SystemProperties.getBoolean("ro.build.hw_emui_lite.enable", false)) {
                z = false;
            }
            sIsFyuseSDKSupport = z;
            if (sIsFyuseSDKSupport) {
                System.loadLibrary("pdqimg");
            }
            HwLog.i("HwFyuseUtils", "sIsFyuseSDKSupport:" + sIsFyuseSDKSupport);
            String language = SystemProperties.get("ro.product.locale.language");
            String region = SystemProperties.get("ro.product.locale.region");
            if ("zh".equals(language)) {
                z = "CN".equals(region);
            } else {
                z = false;
            }
            sIsChinaVersion = z;
        } catch (UnsatisfiedLinkError e) {
            sIsFyuseSDKSupport = false;
            HwLog.e("HwFyuseUtils", "Load libarary failed >>>>>" + e.toString());
        } catch (Error e2) {
            sIsFyuseSDKSupport = false;
            HwLog.e("HwFyuseUtils", "Load libarary failed >>>>>" + e2.toString());
        }
    }

    public static boolean isSupport3DFyuse() {
        return sIsFyuseSDKSupport;
    }

    public static void initFyuseSDK(Context context) {
        if (isSupport3DFyuse()) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("analytics.wifi-only", true);
            try {
                FyuseSDK.init(context, "rUeRdUDBXvq37qVroPBN72", "3WSRIN4pGuuX0njf6IE1rLgjRn0surmV", bundle);
            } catch (Exception e) {
                sIsFyuseSDKSupport = false;
                HwLog.e("HwFyuseUtils", "init FyuseSDK got Exception >>>>> " + e.toString());
            }
            GlobalContext.getBackgroundHandler().post(mInitFyuseSDKRunner);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isCurrent3DWallpaper(Context context) {
        if (isSupport3DFyuse() && isMagaineWallPaper(context) && getMagazineEnableStatus() && !MagazineUtils.isUserCustomedWallpaper(context)) {
            return isFyuseFormatPic(context);
        }
        return false;
    }

    public static void clearFyuseViewMemory(Context context) {
        if (isSupport3DFyuse()) {
            GlobalContext.getUIHandler().postDelayed(mFyuseSystemGC, 5000);
        }
    }

    public static void removeCallbacks() {
        GlobalContext.getUIHandler().removeCallbacks(mFyuseSystemGC);
    }

    public static boolean isFyuseTypeFile(String fileName) {
        return getFileType(fileName) == 11;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int getFileType(String fileName) {
        if (TextUtils.isEmpty(fileName) || !isSupport3DFyuse()) {
            return 1;
        }
        synchronized (object) {
            if (!mMagazineFileNameMap.containsKey(fileName)) {
                File file = new File(fileName);
                if (!file.exists()) {
                    HwLog.i("HwFyuseUtils", "file is not exist, set ");
                    return 0;
                } else if (isNewFyuseFile(fileName)) {
                    boolean isFyuseFile = false;
                    try {
                        isFyuseFile = FyuseUtils.isFyuseContainerVersion(file, FyuseContainerVersion.VERSION_3);
                    } catch (UnsatisfiedLinkError ex) {
                        sIsFyuseSDKSupport = false;
                        HwLog.e("HwFyuseUtils", "isFileType , " + ex.toString());
                    } catch (Error ex2) {
                        sIsFyuseSDKSupport = false;
                        HwLog.e("HwFyuseUtils", "isFileType , " + ex2.toString());
                    }
                    mMagazineFileNameMap.put(fileName, Boolean.valueOf(isFyuseFile));
                } else {
                    return 1;
                }
            }
            int i;
            if (((Boolean) mMagazineFileNameMap.get(fileName)).booleanValue()) {
                i = 11;
            } else {
                i = 1;
            }
        }
    }

    private static boolean isNewFyuseFile(String path) {
        if (TextUtils.isEmpty(path)) {
            HwLog.i("HwFyuseUtils", "path is null");
            return false;
        }
        byte[] fileBytes = readFileEndBytes(path);
        int tagLength = "#FYUSEv3".getBytes().length;
        if (fileBytes == null || fileBytes.length < tagLength) {
            return false;
        }
        return Arrays.equals(Arrays.copyOfRange(fileBytes, fileBytes.length - tagLength, fileBytes.length), "#FYUSEv3".getBytes(Charset.forName("UTF-8")));
    }

    private static byte[] readFileEndBytes(String path) {
        Throwable th;
        RandomAccessFile randomAccessFile = null;
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        try {
            RandomAccessFile randomFile = new RandomAccessFile(path, "r");
            try {
                long fileLength = randomFile.length();
                if (fileLength < 20) {
                    if (randomFile != null) {
                        try {
                            randomFile.close();
                        } catch (IOException e) {
                            HwLog.w("HwFyuseUtils", "fail to process custom image, readFileEndBytes close file fail");
                        }
                    }
                    return null;
                }
                byte[] tmp = new byte[20];
                randomFile.seek(fileLength - 20);
                if (randomFile.read(tmp) != 20) {
                    if (randomFile != null) {
                        try {
                            randomFile.close();
                        } catch (IOException e2) {
                            HwLog.w("HwFyuseUtils", "fail to process custom image, readFileEndBytes close file fail");
                        }
                    }
                    return null;
                }
                if (randomFile != null) {
                    try {
                        randomFile.close();
                    } catch (IOException e3) {
                        HwLog.w("HwFyuseUtils", "fail to process custom image, readFileEndBytes close file fail");
                    }
                }
                return tmp;
            } catch (IOException e4) {
                randomAccessFile = randomFile;
                HwLog.w("HwFyuseUtils", "fail to process custom image, readFileEndBytes throws IOException");
                if (randomAccessFile != null) {
                    try {
                        randomAccessFile.close();
                    } catch (IOException e5) {
                        HwLog.w("HwFyuseUtils", "fail to process custom image, readFileEndBytes close file fail");
                    }
                }
                return null;
            } catch (Exception e6) {
                randomAccessFile = randomFile;
                try {
                    HwLog.w("HwFyuseUtils", "fail to process custom image, readFileEndBytes throws Exception");
                    if (randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
                        } catch (IOException e7) {
                            HwLog.w("HwFyuseUtils", "fail to process custom image, readFileEndBytes close file fail");
                        }
                    }
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    if (randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
                        } catch (IOException e8) {
                            HwLog.w("HwFyuseUtils", "fail to process custom image, readFileEndBytes close file fail");
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                randomAccessFile = randomFile;
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
                throw th;
            }
        } catch (IOException e9) {
            HwLog.w("HwFyuseUtils", "fail to process custom image, readFileEndBytes throws IOException");
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
            return null;
        } catch (Exception e10) {
            HwLog.w("HwFyuseUtils", "fail to process custom image, readFileEndBytes throws Exception");
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
            return null;
        }
    }

    private static boolean isMagazineEnable(Context context) {
        if (context == null) {
            return false;
        }
        try {
            SharedPreferences sp = context.createPackageContext("com.android.keyguard", 0).getSharedPreferences("com.android.keyguard_preferences", 4);
            if (sp == null) {
                HwLog.w("HwFyuseUtils", "get MagazineEnable, SharedPreferences is null");
                return true;
            } else if (sp.contains("enable_magazinelock_feature") || !sp.contains("wifi_auto_update")) {
                return sp.getBoolean("enable_magazinelock_feature", true);
            } else {
                return sp.getBoolean("wifi_auto_update", true);
            }
        } catch (NameNotFoundException e) {
            HwLog.e("HwFyuseUtils", "isMagazineEnable exception: " + e.toString());
            return false;
        }
    }

    public static boolean isMagazineSwitchEnale(Context context) {
        if (sIsChinaVersion) {
            return isMagazineEnable(context);
        }
        return true;
    }

    public static void recordMagazineEnableStatus(boolean isMagazineEnable) {
        mIsMagazineEnable = isMagazineEnable;
    }

    public static boolean getMagazineEnableStatus() {
        return mIsMagazineEnable;
    }

    public static void updateAllPicFormatDataUntilSDCardMounted(final Context context) {
        if (mCount == 5 || isPicFormatChecked(context)) {
            HwLog.w("HwFyuseUtils", "pic format has already been checked");
            return;
        }
        if ("mounted".equals(Environment.getExternalStorageState())) {
            updateAllPicFormatData(context);
            setPicFormatChecked(context);
        } else {
            mCount++;
            HwLog.i("HwFyuseUtils", "updatePicFormatData sd card not mounted");
            GlobalContext.getBackgroundHandler().postDelayed(new Runnable() {
                public void run() {
                    HwFyuseUtils.updateAllPicFormatDataUntilSDCardMounted(context);
                }
            }, 3000);
        }
    }

    public static void updateSinglePicFormat(final String path, final int fileType) {
        GlobalContext.getBackgroundHandler().post(new Runnable() {
            public void run() {
                HwFyuseUtils.updateSinglePicFormatData(path, fileType);
            }
        });
    }

    private static void updateSinglePicFormatData(String path, int fileType) {
        ContentResolver resolver = GlobalContext.getContext().getContentResolver();
        ContentValues values = new ContentValues();
        values.put("picFormat", Integer.valueOf(fileType));
        resolver.update(CONTENT_URI_PICTURES, values, "path = '" + path + "'", null);
    }

    public static boolean isMagaineWallPaper(Context context) {
        return 2 == KeyguardWallpaper.getInst(context).getCurruntType();
    }

    private static boolean isFyuseFormatPic(Context context) {
        BigPictureInfo bigPictureInfo = MagazineWallpaper.getInst(context).getCurrentWallpaper();
        if (bigPictureInfo == null) {
            return false;
        }
        return bigPictureInfo.isFyuseFormatPic();
    }

    private static void updateAllPicFormatData(Context context) {
        ContentResolver resolver = GlobalContext.getContext().getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(CONTENT_URI_PICTURES, null, null, null, null);
        } catch (SQLiteException ex) {
            HwLog.e("HwFyuseUtils", "updatePicFormatData ex = " + ex.toString());
        } catch (OperationCanceledException ex2) {
            HwLog.e("HwFyuseUtils", "updatePicFormatData ex = " + ex2.toString());
        }
        if (cursor == null) {
            HwLog.w("HwFyuseUtils", "skip modify pic format as no data");
            return;
        }
        while (cursor.moveToNext()) {
            try {
                int picFormat = cursor.getInt(cursor.getColumnIndex("picFormat"));
                HwLog.i("HwFyuseUtils", "picFormat = " + picFormat);
                if (picFormat == -1) {
                    String picPath = cursor.getString(cursor.getColumnIndex("path"));
                    boolean isFyu = isFyuseTypeFile(picPath);
                    ContentValues values = new ContentValues();
                    values.put("picFormat", Integer.valueOf(isFyu ? 11 : 1));
                    resolver.update(CONTENT_URI_PICTURES, values, "path = '" + picPath + "'", null);
                }
            } finally {
                cursor.close();
            }
        }
    }

    private static boolean isPicFormatChecked(Context context) {
        return context.getSharedPreferences("magazine_preferences", 0).getBoolean("update_pic_format_checked", false);
    }

    private static void setPicFormatChecked(Context context) {
        context.getSharedPreferences("magazine_preferences", 0).edit().putBoolean("update_pic_format_checked", true).commit();
    }

    public static Bitmap getFyusePreBitmap(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        return FyuseUtils.getRectifiedPreview(new File(path));
    }

    public static DescriptionInfo getDescription(String picPath) {
        if (!isFyuseTypeFile(picPath)) {
            return null;
        }
        DescriptionInfo info = new DescriptionInfo();
        if (TextUtils.isEmpty(picPath)) {
            HwLog.w("HwFyuseUtils", "no description because of null error");
            return info;
        }
        File file = new File(picPath);
        ExifInterface sdkExif = new ExifInterface();
        try {
            sdkExif.readExif(file.getAbsolutePath());
            String title = unicodeToUtf8(sdkExif.getTagStringValue(ExifInterface.TAG_IMAGE_DESCRIPTION));
            String content = unicodeToUtf8(sdkExif.getTagStringValue(ExifInterface.TAG_USER_COMMENT));
            if (!TextUtils.isEmpty(title)) {
                info.setTitle(title);
            }
            if (!TextUtils.isEmpty(content)) {
                info.setContent(content);
            }
        } catch (IOException e) {
            HwLog.e("HwFyuseUtils", "readExif got IOException >>>>> " + e.toString());
        } catch (Exception e2) {
            HwLog.e("HwFyuseUtils", "readExif got Exception >>>>> " + e2.toString());
        }
        return info;
    }

    public static String unicodeToUtf8(String input) {
        if (TextUtils.isEmpty(input)) {
            return BuildConfig.FLAVOR;
        }
        Properties p = new Properties();
        try {
            p.load(new StringReader("key=" + input));
            return p.getProperty("key");
        } catch (IOException e) {
            HwLog.e("HwFyuseUtils", "unicodeToUtf8 got IOException >>>>> " + e.toString());
            return BuildConfig.FLAVOR;
        } catch (Exception e2) {
            HwLog.e("HwFyuseUtils", "unicodeToUtf8 got Exception >>>>> " + e2.toString());
            return BuildConfig.FLAVOR;
        }
    }

    public static void checkFileProcessStatus(final String filePath) {
        if (isFyuseTypeFile(filePath) && !TextUtils.isEmpty(filePath)) {
            GlobalContext.getBackgroundHandler().post(new Runnable() {
                public void run() {
                    final File file = new File(filePath);
                    if (!FyuseUtils.isFyuseProcessed(file)) {
                        HwLog.i("HwFyuseUtils", "checkFileProcessStatus:fyuse not processed : " + file.getName());
                        FyuseProcessor processor = FyuseProcessor.getInstance();
                        final String str = filePath;
                        processor.prepareForViewing(file, new ProcessorListener() {
                            public void onProgress(ProcessItem item, int frame, int totalFrames, Bitmap image) {
                            }

                            public void onError(ProcessItem item, ProcessError error) {
                                HwLog.i("HwFyuseUtils", "onError");
                            }

                            public void onProcessComplete(ProcessItem processItem) {
                                HwLog.i("HwFyuseUtils", "onProcessComplete: " + file.lastModified() + ", " + file.getName());
                                HwFyuseUtils.updateFileModifiedTime(str, file);
                            }

                            public void onImageDataReady(ProcessItem processItem) {
                                HwLog.i("HwFyuseUtils", "onImageDataReady");
                            }

                            public void onMetadataReady(ProcessItem processItem, int paramAnonymousInt) {
                                HwLog.i("HwFyuseUtils", "onMetadataReady");
                            }

                            public void onSliceFound(ProcessItem processItem, int paramAnonymousInt) {
                                HwLog.i("HwFyuseUtils", "onSliceFound");
                            }

                            public void onSliceReady(ProcessItem processItem, int paramAnonymousInt) {
                                HwLog.i("HwFyuseUtils", "onSliceReady");
                            }
                        });
                    }
                }
            });
        }
    }

    private static void updateFileModifiedTime(final String path, final File file) {
        GlobalContext.getContext().sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(file)));
        GlobalContext.getBackgroundHandler().post(new Runnable() {
            public void run() {
                ContentValues values = new ContentValues();
                values.put("date_modified", Long.valueOf(file.lastModified()));
                GlobalContext.getContext().getContentResolver().update(HwFyuseUtils.CONTENT_URI_PICTURES, values, "path = '" + path + "'", null);
            }
        });
    }

    public static boolean isFyuseProcessed(String path) {
        if (!isFyuseTypeFile(path)) {
            return true;
        }
        File file = new File(path);
        boolean ret = FyuseUtils.isFyuseProcessed(file);
        if (!ret) {
            HwLog.i("HwFyuseUtils", file.getName() + "is not processed");
        }
        return ret;
    }
}
