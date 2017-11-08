package com.android.gallery3d.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.KeyguardManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.os.StatFs;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Video;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.WeightedLatLng;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.FilePreference;
import com.android.gallery3d.data.GalleryMediaItem;
import com.android.gallery3d.data.LocalImage;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.settings.GallerySettings;
import com.android.gallery3d.ui.TiledScreenNail;
import com.android.internal.view.RotationPolicy;
import com.huawei.cust.HwCfgFilePolicy;
import com.huawei.gallery.app.AbstractGalleryActivity;
import com.huawei.gallery.app.AbstractGalleryFragment;
import com.huawei.gallery.app.CreateAlbumDialog.CallBackListner;
import com.huawei.gallery.app.SinglePhotoActivity;
import com.huawei.gallery.map.app.MapAlbumActivity;
import com.huawei.gallery.map.app.MapUtils;
import com.huawei.gallery.media.GalleryMedia;
import com.huawei.gallery.print.DocumentPrintHelper;
import com.huawei.gallery.service.ScannerFavoriteService;
import com.huawei.gallery.storage.GalleryStorage;
import com.huawei.gallery.storage.GalleryStorageManager;
import com.huawei.gallery.storage.StorageUtils;
import com.huawei.gallery.util.BurstUtils;
import com.huawei.watermark.manager.parse.WMConfig;
import com.huawei.watermark.manager.parse.WMElement;
import huawei.android.widget.DialogContentHelper;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.module.update.UpdateConfig;

public class GalleryUtils {
    public static final Uri EXTERNAL_FILE_URI = Files.getContentUri("external");
    public static final boolean FIXED_WALLPAPER_ENANBLED = SystemProperties.getBoolean("ro.config.enable_fixedwallpaper", true);
    public static final boolean IS_BETA_VERSION;
    public static final boolean IS_CHINESE_VERSION = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    public static final boolean IS_DEBUG;
    public static final boolean IS_DEBUG_GALLERY_HPROF = SystemProperties.getBoolean("debug_hw_gallery_hprof", false);
    public static final boolean IS_STORY_ENABLE;
    public static final boolean IS_SUPPORT_HW_ANIMATION = SystemProperties.getBoolean("ro.config.gallery_hw_animation", true);
    public static final boolean PRODUCT_LITE = SystemProperties.getBoolean("ro.build.hw_emui_lite.enable", false);
    private static OnKeyListener sBackKeyListener = new OnKeyListener() {
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode == 4 && event.getAction() == 1 && !event.isCanceled()) {
                GalleryUtils.setDialogDismissable(dialog, true);
            }
            return false;
        }
    };
    private static boolean sCVAAMode;
    private static Context sContext = null;
    private static volatile Thread sCurrentThread;
    private static Typeface sFontTypeSlim = null;
    private static int sHeightPixels;
    private static boolean sIsLanguageChanged = false;
    private static Boolean sIsSupportPressureFeature;
    private static Float sIsSupportPressureLimit;
    private static boolean sIsSupportRotation = false;
    private static String sLanguage = null;
    private static boolean sLayoutRTL = false;
    private static float sPixelDensity = GroundOverlayOptions.NO_DIMENSION;
    private static String sPreloadMediaDirectory;
    private static boolean sScreenRecorderExist;
    private static StorageManager sStorageManager;
    private static boolean sUserFontDirEmpty = true;
    private static String[] sVolumePaths;
    private static volatile boolean sWarned = true;
    private static int sWidthPixels;

    static {
        boolean z;
        boolean z2 = false;
        if (SystemProperties.getInt("ro.debuggable", 0) == 1) {
            z = true;
        } else {
            z = false;
        }
        IS_DEBUG = z;
        if (PRODUCT_LITE || SystemProperties.getBoolean("ro.config.gallery_story_disable", false)) {
            z = false;
        } else {
            z = true;
        }
        IS_STORY_ENABLE = z;
        if (SystemProperties.getInt("ro.logsystem.usertype", 0) == 3) {
            z2 = true;
        }
        IS_BETA_VERSION = z2;
    }

    public static void initialize(Context context) {
        sContext = context;
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getRealMetrics(metrics);
        sPixelDensity = metrics.density;
        if (metrics.heightPixels > metrics.widthPixels) {
            sHeightPixels = metrics.heightPixels;
            sWidthPixels = metrics.widthPixels;
        } else {
            sHeightPixels = metrics.widthPixels;
            sWidthPixels = metrics.heightPixels;
        }
        Resources r = context.getResources();
        TiledScreenNail.setPlaceholderColor(r.getColor(R.color.bitmap_screennail_placeholder));
        initializeThumbnailSizes(metrics, r);
        initializeStorageVolume(context);
        checkUserFontDir();
        sCVAAMode = ((AccessibilityManager) context.getSystemService("accessibility")).isEnabled();
        sFontTypeSlim = getTypeFaceSlim();
        if (ApiHelper.HAS_SUPPORT_ROTATION_POLICY) {
            sIsSupportRotation = RotationPolicy.isRotationSupported(context);
        }
        initializeScreenshotsRecoder(context);
        processPreloadMediaDirs();
    }

    public static boolean checkLayoutRTL(Context context) {
        sLayoutRTL = 128 == (context.getResources().getConfiguration().screenLayout & SmsCheckResult.ESCT_192);
        GalleryLog.i("GalleryUtils", "Current is Rtl or not:" + sLayoutRTL);
        return sLayoutRTL;
    }

    public static boolean isLayoutRTL() {
        return sLayoutRTL;
    }

    public static boolean isSupportSlimType() {
        return isChineseLanguage();
    }

    public static boolean isChineseLanguage() {
        if (sLanguage == null || !sLanguage.contains(WMConfig.SUPPORTZH)) {
            return false;
        }
        return true;
    }

    public static void initializeScreenshotsRecoder(Context context) {
        sScreenRecorderExist = isScreenRecorderAvailable(context);
    }

    public static void initializeStorageVolume(Context context) {
        sStorageManager = (StorageManager) context.getSystemService("storage");
        sVolumePaths = StorageUtils.getVolumePaths(context, sStorageManager);
        StorageUtils.updateStorageVolume(context, sStorageManager);
    }

    private static void initializeThumbnailSizes(DisplayMetrics metrics, Resources r) {
        int maxPixels = Math.max(metrics.heightPixels, metrics.widthPixels);
        MediaItem.setThumbnailSizes(maxPixels / 2, maxPixels / 10, maxPixels / 5);
        MediaItem.setFullScreenThumbnailSizes(maxPixels);
        TiledScreenNail.setMaxSide(maxPixels / 2);
    }

    public static boolean isHighResolution(Context context) {
        return sHeightPixels > 2048 || sWidthPixels > 2048;
    }

    public static float[] intColorToFloatARGBArray(int from) {
        return new float[]{((float) Color.alpha(from)) / 255.0f, ((float) Color.red(from)) / 255.0f, ((float) Color.green(from)) / 255.0f, ((float) Color.blue(from)) / 255.0f};
    }

    public static float dpToPixel(float dp) {
        return sPixelDensity * dp;
    }

    public static int dpToPixel(int dp) {
        return Math.round(dpToPixel((float) dp));
    }

    public static int meterToPixel(float meter) {
        return Math.round(dpToPixel((39.37f * meter) * 160.0f));
    }

    public static AbstractGalleryFragment getContentFragment(FragmentManager fragmentManager, String tag) {
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        if (fragment instanceof AbstractGalleryFragment) {
            return (AbstractGalleryFragment) fragment;
        }
        return null;
    }

    public static byte[] getBytes(String in) {
        byte[] result = new byte[(in.length() * 2)];
        int output = 0;
        for (char ch : in.toCharArray()) {
            int i = output + 1;
            result[output] = (byte) (ch & 255);
            output = i + 1;
            result[i] = (byte) (ch >> 8);
        }
        return result;
    }

    public static void setRenderThread() {
        sCurrentThread = Thread.currentThread();
    }

    public static void assertNotInRenderThread() {
        if (!sWarned && Thread.currentThread() == sCurrentThread) {
            sWarned = true;
            GalleryLog.w("GalleryUtils", new Throwable("Should not do this in render thread"));
        }
    }

    public static void startActivitySafe(Context context) {
        try {
            context.startActivity(new Intent("com.huawei.hicloud.action.GALLERY_LOGIN"));
        } catch (ActivityNotFoundException e) {
            GalleryLog.w("GalleryUtils", "Cann't find activity");
        } catch (SecurityException e2) {
            GalleryLog.w("GalleryUtils", "start cloud but find security exception.");
        } catch (Exception e3) {
            GalleryLog.w("GalleryUtils", "start cloud failed", e3);
        }
    }

    public static double fastDistanceMeters(double latRad1, double lngRad1, double latRad2, double lngRad2) {
        if (Math.abs(latRad1 - latRad2) > 0.017453292519943295d || Math.abs(lngRad1 - lngRad2) > 0.017453292519943295d) {
            return accurateDistanceMeters(latRad1, lngRad1, latRad2, lngRad2);
        }
        double sineLat = latRad1 - latRad2;
        double sineLng = lngRad1 - lngRad2;
        double cosTerms = Math.cos((latRad1 + latRad2) / 2.0d);
        return 6367000.0d * Math.sqrt((sineLat * sineLat) + (((cosTerms * cosTerms) * sineLng) * sineLng));
    }

    public static double accurateDistanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double dlat = Math.sin((lat2 - lat1) * 0.5d);
        double dlng = Math.sin((lng2 - lng1) * 0.5d);
        double x = (dlat * dlat) + (((dlng * dlng) * Math.cos(lat1)) * Math.cos(lat2));
        return (Math.atan2(Math.sqrt(x), Math.sqrt(Math.max(0.0d, WeightedLatLng.DEFAULT_INTENSITY - x))) * 2.0d) * 6367000.0d;
    }

    public static final double toMile(double meter) {
        return meter / 1609.0d;
    }

    public static boolean isEditorAvailable(Context context, String mimeType) {
        boolean z = false;
        int version = getPackagesVersion(context);
        String updateKey = "editor-update-" + mimeType;
        String hasKey = "has-editor-" + mimeType;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getInt(updateKey, 0) != version) {
            List<ResolveInfo> infos = context.getPackageManager().queryIntentActivities(new Intent("android.intent.action.EDIT").setType(mimeType), 0);
            Editor putInt = prefs.edit().putInt(updateKey, version);
            if (!infos.isEmpty()) {
                z = true;
            }
            putInt.putBoolean(hasKey, z).commit();
        }
        return prefs.getBoolean(hasKey, true);
    }

    private static boolean isScreenRecorderAvailable(Context context) {
        boolean isExistScreenRecorder = false;
        try {
            isExistScreenRecorder = MapUtils.isPackagesExist(context, "com.huawei.screenrecorder");
        } catch (RuntimeException e) {
            GalleryLog.d("GalleryUtils", "find Screen Recorder exception:" + e);
        }
        return isExistScreenRecorder;
    }

    public static boolean isScreenRecorderExist() {
        return sScreenRecorderExist;
    }

    public static synchronized boolean isSupportPressurePreview(Context context) {
        boolean z = false;
        synchronized (GalleryUtils.class) {
            if (sIsSupportPressureFeature == null || sIsSupportPressureLimit == null) {
                TouchForceManagerWrapper tfm = new TouchForceManagerWrapper(context);
                sIsSupportPressureFeature = Boolean.valueOf(tfm.isSupportForce());
                sIsSupportPressureLimit = Float.valueOf(tfm.getPressureLimit());
            }
            if (Float.compare(sIsSupportPressureLimit.floatValue(), 0.0f) != 0 && sIsSupportPressureFeature.booleanValue() && 1 == System.getInt(context.getContentResolver(), "pressure_preview_picture", 1) && !MultiWindowStatusHolder.isInMultiMaintained()) {
                if (!isLazyMode()) {
                    z = ActivityExWrapper.IS_PRESS_SUPPORT;
                }
            }
        }
        return z;
    }

    public static void updateSupportPressurePreview(Context context) {
        sIsSupportPressureLimit = Float.valueOf(new TouchForceManagerWrapper(context).getPressureLimit());
        GalleryLog.d("GalleryUtils", "first level press value = " + sIsSupportPressureLimit);
    }

    private static boolean isLazyMode() {
        int lazyMode = 0;
        try {
            IWindowManager wm = Stub.asInterface(ServiceManager.getService("window"));
            Object obj = wm.getClass().getMethod("getLazyMode", new Class[0]).invoke(wm, new Object[0]);
            if (obj instanceof Integer) {
                lazyMode = ((Integer) obj).intValue();
            }
        } catch (Exception ex) {
            GalleryLog.d("GalleryUtils", "isLazyMode ex=" + ex);
        } catch (Error err) {
            GalleryLog.d("GalleryUtils", "isLazyMode Error=" + err);
        }
        if (lazyMode != 0) {
            return true;
        }
        return false;
    }

    public static float getPressureLimit() {
        return sIsSupportPressureLimit.floatValue();
    }

    public static synchronized boolean isSupportPressureResponseMagnifier(Context context) {
        boolean z = true;
        synchronized (GalleryUtils.class) {
            if (sIsSupportPressureFeature == null) {
                sIsSupportPressureFeature = Boolean.valueOf(new TouchForceManagerWrapper(context).isSupportForce());
            }
            if (!sIsSupportPressureFeature.booleanValue()) {
                z = false;
            } else if (1 != System.getInt(context.getContentResolver(), "picture_largen_type", 1)) {
                z = false;
            }
        }
        return z;
    }

    public static synchronized boolean isSupportPressureResponse(Context context) {
        boolean z = true;
        synchronized (GalleryUtils.class) {
            if (sIsSupportPressureFeature == null) {
                sIsSupportPressureFeature = Boolean.valueOf(new TouchForceManagerWrapper(context).isSupportForce());
            }
            if (!sIsSupportPressureFeature.booleanValue() || 1 != System.getInt(context.getContentResolver(), "picture_largen_type", 1)) {
                z = false;
            } else if (1 == System.getInt(context.getContentResolver(), "pressure_preview_picture", 1)) {
                z = false;
            }
        }
        return z;
    }

    public static float getPressureResponseThreshold(Context context) {
        return System.getFloat(context.getContentResolver(), "pressure_habit_threshold", 0.2f);
    }

    public static void startActivityWithChooser(GalleryContext context, Intent target, CharSequence title) {
        if (context == null) {
            GalleryLog.d("GalleryUtils", "start activity failed, context is null !!!");
        } else {
            startActivityWithChooser(context.getActivityContext(), target, title);
        }
    }

    private static void startActivityWithChooser(Context context, Intent target, CharSequence title) {
        Intent intent = Intent.createChooser(target, title);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            String action = intent.getAction();
            GalleryLog.d("GalleryUtils", "start activity with '" + action + "' failed !!! " + e.getMessage());
            if (!"android.intent.action.CHOOSER".equals(action)) {
                try {
                    intent.setAction("android.intent.action.CHOOSER");
                    context.startActivity(intent);
                } catch (Exception ex) {
                    GalleryLog.d("GalleryUtils", "start activity with 'android.intent.action.CHOOSER' failed. " + ex.getMessage());
                }
            }
        }
    }

    public static void startMapAlbum(Activity activity, Bundle data) {
        Intent intent = new Intent();
        intent.setClass(activity, MapAlbumActivity.class);
        if (data != null) {
            intent.putExtras(data);
        }
        activity.startActivity(intent);
    }

    public static boolean isValidLocation(double latitude, double longitude) {
        return (latitude == 0.0d && longitude == 0.0d) ? false : true;
    }

    public static String formatLatitudeLongitude(String format, double latitude, double longitude) {
        return String.format(Locale.ENGLISH, format, new Object[]{Double.valueOf(latitude), Double.valueOf(longitude)});
    }

    public static boolean hasMoreEditorForPic(Context context) {
        if (context.getPackageManager().queryIntentActivities(new Intent("android.intent.action.EDIT").setType("image/*"), 0).size() > 1) {
            return true;
        }
        return false;
    }

    public static boolean isAnyMapAvailable(Context context) {
        if (context.getPackageManager().queryIntentActivities(new Intent("android.intent.action.VIEW", Uri.parse("geo:0,0")), 0).isEmpty()) {
            return false;
        }
        return true;
    }

    public static boolean isActivityAvailable(Intent intent) {
        if (sContext.getPackageManager().queryIntentActivities(intent, 0).isEmpty()) {
            return false;
        }
        return true;
    }

    private static Intent createNormalMapIntent(double latitude, double longitude) {
        return new Intent("android.intent.action.VIEW", Uri.parse(String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f", new Object[]{Double.valueOf(latitude), Double.valueOf(longitude), Double.valueOf(latitude), Double.valueOf(longitude)})));
    }

    public static void showOnMap(Context context, double latitude, double longitude) {
        context.startActivity(createNormalMapIntent(latitude, longitude));
    }

    public static void setViewPointMatrix(float[] matrix, float x, float y, float z) {
        Arrays.fill(matrix, 0, 16, 0.0f);
        float f = -z;
        matrix[15] = f;
        matrix[5] = f;
        matrix[0] = f;
        matrix[8] = x;
        matrix[9] = y;
        matrix[11] = WMElement.CAMERASIZEVALUE1B1;
        matrix[10] = WMElement.CAMERASIZEVALUE1B1;
    }

    public static int getBucketId(String path) {
        if (path == null) {
            return 0;
        }
        return path.toLowerCase(Locale.US).hashCode();
    }

    public static String formatDuration(Context context, int duration) {
        int h = duration / 3600;
        int s = duration - ((h * 3600) + (((duration - (h * 3600)) / 60) * 60));
        if (h == 0) {
            return String.format(context.getString(R.string.details_ms), new Object[]{Integer.valueOf(m), Integer.valueOf(s)});
        }
        return String.format(context.getString(R.string.details_hms), new Object[]{Integer.valueOf(h), Integer.valueOf(m), Integer.valueOf(s)});
    }

    @TargetApi(11)
    public static int determineTypeBits(Context context, Intent intent) {
        int typeBits;
        String type = intent.resolveType(context);
        if ("*/*".equals(type)) {
            typeBits = 2097152;
        } else if ("image/*".equals(type) || "vnd.android.cursor.dir/image".equals(type)) {
            typeBits = 524288;
        } else if ("video/*".equals(type) || "vnd.android.cursor.dir/video".equals(type)) {
            typeBits = 1048576;
        } else {
            typeBits = 2097152;
        }
        if (ApiHelper.HAS_INTENT_EXTRA_LOCAL_ONLY && intent.getBooleanExtra("android.intent.extra.LOCAL_ONLY", false)) {
            return typeBits | 4;
        }
        return typeBits;
    }

    public static int getSelectionModePrompt(int typeBits) {
        int i = R.string.select_photo_video;
        if ((1048576 & typeBits) != 0) {
            if ((524288 & typeBits) == 0) {
                i = R.string.select_video;
            }
            return i;
        } else if ((2097152 & typeBits) != 0) {
            return R.string.select_photo_video;
        } else {
            return R.string.widget_type;
        }
    }

    public static boolean hasSpaceForSize(long size) {
        if ("mounted".equals(Environment.getExternalStorageState())) {
            return hasSpaceForSize(size, Environment.getExternalStorageDirectory().getPath());
        }
        return false;
    }

    public static String[] getVolumePaths() {
        return (String[]) sVolumePaths.clone();
    }

    public static boolean hasSpaceForSize(long size, String dataPath) {
        boolean z = false;
        if (TextUtils.isEmpty(dataPath)) {
            return false;
        }
        for (String path : sVolumePaths) {
            if (dataPath.startsWith(path)) {
                if (getAvailableSpace(path) > size) {
                    z = true;
                }
                return z;
            }
        }
        return false;
    }

    public static File ensureExternalCacheDir(Context context) {
        File file = null;
        for (String path : sVolumePaths) {
            if (checkDiskSpace(path, UpdateConfig.UPDATE_FLAG_DEEPCLEAN_SOFT_PATH_LIST)) {
                file = new File(path, "/Android/data/" + context.getPackageName() + "/cache");
                break;
            }
        }
        return createDirIfNeed(file);
    }

    public static File ensureCacherDirOnlyInner(Context context) {
        GalleryStorage innerGalleryStorage = GalleryStorageManager.getInstance().getInnerGalleryStorage();
        if (innerGalleryStorage == null) {
            return null;
        }
        File file = null;
        if (checkDiskSpace(innerGalleryStorage.getPath(), UpdateConfig.UPDATE_FLAG_DEEPCLEAN_SOFT_PATH_LIST)) {
            file = new File(innerGalleryStorage.getPath(), "/Android/data/" + context.getPackageName() + "/cache");
        }
        return createDirIfNeed(file);
    }

    public static File createDirIfNeed(File dir) {
        if (!(dir == null || dir.exists())) {
            if (mkdirs(dir) == null) {
                return null;
            }
            try {
                if (!new File(dir, ".nomedia").createNewFile()) {
                    GalleryLog.i("GalleryUtils", "createNewFile failure.");
                }
            } catch (IOException e) {
                GalleryLog.w("GalleryUtils", "Unable to create .nomedia file");
            }
        }
        return dir;
    }

    public static File createEmptyDir(File dir) {
        if (dir == null || dir.exists() || mkdirs(dir) != null) {
            return dir;
        }
        return null;
    }

    public static File mkdirs(File dir) {
        if (dir.mkdirs()) {
            return dir;
        }
        GalleryLog.w("GalleryUtils", "Unable to create external cache directory");
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String convertUriToPath(Context context, Uri uri) {
        String path = null;
        if (uri == null) {
            return null;
        }
        String scheme = uri.getScheme();
        if (scheme == null || scheme.equals("") || scheme.equals("file")) {
            return uri.getPath();
        }
        if (scheme.equals("http") || scheme.equals("rtsp")) {
            return uri.toString();
        }
        if (!scheme.equals("content")) {
            return null;
        }
        try {
            Closeable cursor = context.getContentResolver().query(uri, new String[]{"_data"}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
            }
            Utils.closeSilently(cursor);
            return path;
        } catch (SQLiteException e) {
            GalleryLog.w("GalleryUtils", "Given Uri is not formatted in a way so that it can be found in media store.");
            return null;
        } catch (IllegalArgumentException e2) {
            GalleryLog.w("GalleryUtils", "Illegal Uri " + uri);
            return null;
        } catch (Exception e3) {
            GalleryLog.w("GalleryUtils", "error . " + uri + ". " + e3.getMessage());
            return null;
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
    }

    public static int getDelayTime(int time) {
        int times = SystemProperties.getInt("ro.autotest.delaytimes", 1);
        if (sCVAAMode) {
            times *= 3;
        }
        return time * times;
    }

    public static int getAlertDialogThemeID(Context context) {
        return getThemeIdByType(context, 3);
    }

    private static int getThemeIdByType(Context context, int type) {
        switch (type) {
            case 1:
                return context.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
            case 2:
                return context.getResources().getIdentifier("androidhwext:style/Theme.Emui.NoTitleBar", null, null);
            case 3:
                return context.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
            default:
                return 0;
        }
    }

    public static String getFileSizeString(long fileSizeByte) {
        if (fileSizeByte < 1024) {
            return String.format("%d B", new Object[]{Long.valueOf(fileSizeByte)});
        } else if (fileSizeByte < 1048576) {
            return String.format("%.1f KB", new Object[]{Double.valueOf(((double) fileSizeByte) / 1024.0d)});
        } else {
            return String.format("%.1f MB", new Object[]{Double.valueOf(((double) fileSizeByte) / 1048576.0d)});
        }
    }

    public static String getSettingFormatShortDate(Context context, long mills) {
        return DateFormat.getDateFormat(context).format(Long.valueOf(mills));
    }

    public static String getSettingFormatShortDateDependLocal(Context context, long mills) {
        return DateUtils.formatDateTime(context, mills, 131092);
    }

    public static String getFormatDateRangeString(Context context, Formatter formatter, long minMills, long maxMills) {
        return DateUtils.formatDateRange(context, formatter, minMills, maxMills, 131092).toString();
    }

    public static String getSettingFormatDateTime(Context context, long mills) {
        String date = DateFormat.getDateFormat(context).format(Long.valueOf(mills));
        return date + " " + DateFormat.getTimeFormat(context).format(Long.valueOf(mills));
    }

    public static String getFomattedDateTime(Context context, long mills) {
        return DateUtils.formatDateTime(context, mills, 68117);
    }

    public static String getlocalizedDateTime(Context context, long mills) {
        return getSettingFormatShortDate(context, mills) + " " + DateFormat.getTimeFormat(context).format(Long.valueOf(mills));
    }

    public static void dismissDialogSafely(DialogInterface dialog, Activity activity) {
        if (dialog == null) {
            if (Constant.DBG) {
                GalleryLog.e("GalleryUtils", "try to dismiss a null dialog");
            }
            return;
        }
        try {
            dialog.dismiss();
        } catch (IllegalArgumentException e) {
            if (Constant.DBG) {
                String message = "IllegalArgumentException when dismissDialog : " + dialog;
                if (activity != null && ApiHelper.API_VERSION_MIN_17) {
                    message = message + ", current activity distroyed? " + activity.isDestroyed();
                }
                GalleryLog.e("GalleryUtils", message + "." + e.getMessage());
            }
        }
    }

    public static boolean isFileNameValid(Context context, String fileName) {
        if (fileName == null || fileName.length() == 0) {
            ContextedUtils.showToastQuickly(context, (int) R.string.photoshare_name_no_content_Toast, 0);
            return false;
        } else if (85 < fileName.length()) {
            ContextedUtils.showToastQuickly(context, (int) R.string.photoshare_name_reached_max_length_Toast, 0);
            return false;
        } else if (Pattern.matches("^[^\\.\\\\/:*?<>\"|\\[\\]\\{\\}]+$", fileName)) {
            return true;
        } else {
            ContextedUtils.showToastQuickly(context, (int) R.string.photoshare_name_invalid_new, 0);
            return false;
        }
    }

    public static void showLimitExceedDialog(Context context) {
        String message = context.getResources().getQuantityString(R.plurals.share_limit_exceed_msg, 500, new Object[]{Integer.valueOf(500)});
        String confim = context.getString(R.string.ok);
        Builder limitExceedDialog = new Builder(context).setTitle(message);
        limitExceedDialog.setPositiveButton(confim, null);
        limitExceedDialog.show();
    }

    public static void setHorizontalFadeEdge(TextView tv) {
        if (tv != null) {
            tv.setSingleLine(true);
            tv.setEllipsize(TruncateAt.MARQUEE);
            tv.setHorizontalFadingEdgeEnabled(true);
        }
    }

    public static void setTextColor(TextView tv, Resources res) {
        if (tv != null) {
            tv.setTextColor(res.getColor(R.color.delete_textcolor));
        }
    }

    public static void checkLanguageChanged(GalleryContext activity, boolean isSelectOutSide) {
        String language = activity.getResources().getConfiguration().locale.toString();
        if (!(sLanguage == null || sLanguage.equals(language))) {
            sIsLanguageChanged = true;
        }
        sLanguage = language;
    }

    public static boolean isFrenchLanguage() {
        if (sLanguage == null || !sLanguage.contains("fr")) {
            return false;
        }
        return true;
    }

    public static void setTitleAndMessage(AlertDialog alertDialog, String msg, String title, boolean transformTitleAndMsg) {
        if (title != null || msg != null) {
            if (msg == null && transformTitleAndMsg) {
                msg = title;
                title = null;
            }
            alertDialog.setTitle(title);
            alertDialog.setMessage(msg);
        }
    }

    public static void setTitleAndMessage(AlertDialog alertDialog, String msg, String title) {
        setTitleAndMessage(alertDialog, msg, title, true);
    }

    public static boolean isSupportMyFavorite() {
        return true;
    }

    public static int getHeightPixels() {
        return sHeightPixels;
    }

    public static int getWidthPixels() {
        return sWidthPixels;
    }

    public static boolean getLanguageChanged() {
        return sIsLanguageChanged;
    }

    public static void setLanguageChangedFalse() {
        sIsLanguageChanged = false;
    }

    public static String getDefualtAlbumName(Context context, String albumPath) {
        String fileName = context.getString(R.string.new_album);
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            if (i != 0) {
                fileName = String.format("%s %d", new Object[]{defaultName, Integer.valueOf(i)});
            }
            File albumFileDir = new File(albumPath, fileName);
            if (!albumFileDir.exists() || !isDirContainMultimedia(context, albumFileDir.toString())) {
                return fileName;
            }
        }
        return fileName;
    }

    public static boolean isDirContainMultimedia(Context context, String path) {
        Uri uri = Files.getContentUri("external");
        Closeable closeable = null;
        try {
            closeable = context.getContentResolver().query(uri, new String[]{"_data", "count(_data)"}, "bucket_id = ? and media_type in (1,3)", new String[]{String.valueOf(getBucketId(path))}, null);
            if (closeable != null && closeable.moveToFirst() && closeable.getInt(1) > 0) {
                return true;
            }
            Utils.closeSilently(closeable);
            return false;
        } catch (RuntimeException e) {
            GalleryLog.i("GalleryUtils", "A RuntimeException has occurred in isDirContainMultimedia() method.");
        } catch (Exception e2) {
            GalleryLog.i("GalleryUtils", "An exception has occurred in isDirContainMultimedia() method.");
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    public static boolean setDialogDismissable(DialogInterface dialog, boolean dismissalbe) {
        try {
            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
            field.setAccessible(true);
            field.setBoolean(dialog, dismissalbe);
            return true;
        } catch (Exception e) {
            GalleryLog.d("GalleryUtils", "set dialog dismissable fail." + e.getMessage());
            return false;
        }
    }

    public static AlertDialog createDialog(Context context, String defaultName, int titleID, OnClickListener clickListener, CallBackListner dismiss, EditText setNameTextView) {
        return createDialog(context, defaultName, titleID, clickListener, dismiss, null, setNameTextView);
    }

    public static AlertDialog createDialog(final Context context, String defaultName, int titleID, OnClickListener clickListener, final CallBackListner dismiss, View customView, EditText setNameTextView) {
        String cancelString = context.getString(R.string.cancel);
        String confirmString = context.getString(R.string.ok);
        DialogContentHelper dialogContentHelper = new DialogContentHelper(true, true, context);
        final AlertDialog createDialog = new Builder(context).setOnKeyListener(sBackKeyListener).setTitle(titleID).create();
        if (customView == null) {
            int padding = context.getResources().getDimensionPixelSize(R.dimen.alter_dialog_padding_left_right);
            createDialog.setView(dialogContentHelper.beginLayout().insertView(setNameTextView, null).endLayout(), padding, 0, padding, 0);
        } else {
            createDialog.setView(dialogContentHelper.beginLayout().insertView(customView, null).endLayout());
        }
        createDialog.setButton(-2, cancelString, clickListener);
        createDialog.setButton(-1, confirmString, clickListener);
        createDialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                if (dismiss != null) {
                    dismiss.dialogDismiss();
                }
            }
        });
        setNameTextView.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                Button confirmBtn = createDialog.getButton(-1);
                if (s == null || s.length() != 0) {
                    confirmBtn.setClickable(true);
                    confirmBtn.setAlpha(WMElement.CAMERASIZEVALUE1B1);
                } else {
                    confirmBtn.setClickable(false);
                    confirmBtn.setAlpha(0.3f);
                }
                if (s != null && s.length() >= 85) {
                    ContextedUtils.showToastQuickly(context, (int) R.string.exceed_max_length_Toast, 0);
                }
            }
        });
        createDialog.show();
        setNameTextView.setText(defaultName);
        setNameTextView.selectAll();
        setNameTextView.setFilters(new InputFilter[]{new LengthFilter(85)});
        return createDialog;
    }

    public static Typeface getTypeFaceSlim() {
        Typeface tp = null;
        try {
            tp = Typeface.createFromFile("/system/fonts/slim.ttf");
        } catch (Exception e) {
            GalleryLog.w("GalleryUtils", "the font Slim is not exist!");
        }
        return tp;
    }

    public static void checkUserFontDir() {
        File userFontPath = new File("/data/skin/fonts");
        if (userFontPath.exists() && userFontPath.isDirectory() && userFontPath.listFiles().length > 0) {
            sUserFontDirEmpty = false;
        } else {
            sUserFontDirEmpty = true;
        }
    }

    public static void setTypeFaceAsSlim(TextView tv) {
        if (tv != null && sUserFontDirEmpty && sFontTypeSlim != null && isSupportSlimType()) {
            tv.setTypeface(sFontTypeSlim);
        }
    }

    public static void setTypeFaceAsSlim(Paint paint) {
        if (paint != null && sUserFontDirEmpty && sFontTypeSlim != null && isSupportSlimType()) {
            paint.setTypeface(sFontTypeSlim);
        }
    }

    public static int getFontHeightOfPaint(TextPaint textPaint) {
        if (textPaint == null) {
            return 0;
        }
        FontMetrics fontMetrics = textPaint.getFontMetrics();
        return (int) (fontMetrics.bottom + Math.abs(fontMetrics.top));
    }

    public static String getProcessName() {
        Closeable closeable = null;
        try {
            Closeable reader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/self/cmdline"), "utf-8"));
            try {
                String cmdline = reader.readLine();
                if (cmdline != null) {
                    cmdline = cmdline.trim();
                }
                Utils.closeSilently(reader);
                return cmdline;
            } catch (FileNotFoundException e) {
                closeable = reader;
                GalleryLog.i("GalleryUtils", "new FileInputStream() failed, reason: FileNotFoundException.");
                Utils.closeSilently(closeable);
                return null;
            } catch (IOException e2) {
                closeable = reader;
                try {
                    GalleryLog.i("GalleryUtils", "An IOException has occurred in getProcessName() method, reason: IOException.");
                    Utils.closeSilently(closeable);
                    return null;
                } catch (Throwable th) {
                    Utils.closeSilently(closeable);
                    return null;
                }
            } catch (Throwable th2) {
                closeable = reader;
                Utils.closeSilently(closeable);
                return null;
            }
        } catch (FileNotFoundException e3) {
            GalleryLog.i("GalleryUtils", "new FileInputStream() failed, reason: FileNotFoundException.");
            Utils.closeSilently(closeable);
            return null;
        } catch (IOException e4) {
            GalleryLog.i("GalleryUtils", "An IOException has occurred in getProcessName() method, reason: IOException.");
            Utils.closeSilently(closeable);
            return null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Uri convertFileUriToContentUri(Context context, Uri fileUri) {
        if (fileUri == null || !"file".equals(fileUri.getScheme())) {
            return fileUri;
        }
        Closeable closeable = null;
        Uri contentUri = fileUri;
        try {
            closeable = context.getContentResolver().query(Files.getContentUri("external"), new String[]{"_id", "media_type"}, "_data = ?", new String[]{fileUri.getPath()}, null);
            if (closeable != null && closeable.moveToFirst()) {
                switch (closeable.getInt(1)) {
                    case 1:
                        contentUri = Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(closeable.getString(0)).build();
                        break;
                    case 3:
                        contentUri = Video.Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(closeable.getString(0)).build();
                        break;
                }
            }
            Utils.closeSilently(closeable);
        } catch (SecurityException e) {
            GalleryLog.noPermissionForMediaProviderLog("GalleryUtils");
        } catch (SQLiteException e2) {
            GalleryLog.w("GalleryUtils", "Given Uri is not formatted in a way so that it can be found in media store.");
        } catch (IllegalArgumentException e3) {
            GalleryLog.w("GalleryUtils", "Illegal Uri " + fileUri);
        } catch (Throwable th) {
            Utils.closeSilently(closeable);
        }
        return contentUri;
    }

    public static boolean isSupportRotation() {
        return sIsSupportRotation;
    }

    public static boolean isNameUsed(String oldPath, String newName) {
        if (oldPath == null || newName == null) {
            return true;
        }
        File oldFile = new File(oldPath);
        return new File(oldFile.getParent(), newName + oldFile.toString().substring(oldFile.toString().lastIndexOf("."))).exists();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isNewFileNameLegal(Context context, MediaItem photo, DialogInterface dialog, String newFileName) {
        if (context == null || photo == null || dialog == null || newFileName == null || photo.getName() == null) {
            return false;
        }
        if (isFileNameValid(context, newFileName)) {
            boolean isNameUsed;
            if (photo instanceof GalleryMediaItem) {
                isNameUsed = isNameUsed((GalleryMediaItem) photo, newFileName);
            } else {
                isNameUsed = isNameUsed(photo.getFilePath(), newFileName);
            }
            if (!isNameUsed) {
                return true;
            }
            setDialogDismissable(dialog, false);
            ContextedUtils.showToastQuickly(context, (int) R.string.create_album_file_exist_Toast, 0);
            return false;
        }
        setDialogDismissable(dialog, false);
        return false;
    }

    private static boolean isNameUsed(GalleryMediaItem item, String newFileName) {
        Closeable closeable = null;
        try {
            closeable = getContext().getContentResolver().query(GalleryMedia.URI, new String[]{"count(*)"}, "bucket_id =? and title = ?", new String[]{String.valueOf(item.bucketId), newFileName}, null);
            if (closeable == null || !closeable.moveToNext()) {
                Utils.closeSilently(closeable);
                return true;
            } else if (closeable.getInt(0) > 0) {
                return true;
            } else {
                Utils.closeSilently(closeable);
                return false;
            }
        } catch (RuntimeException e) {
            GalleryLog.v("photoshareLogTag", "query name used faied");
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    public static String getContentType(Context context, Intent intent) {
        String type = intent.getType();
        if (type != null) {
            if ("application/vnd.google.panorama360+jpg".equals(type)) {
                type = "image/jpeg";
            }
            return type;
        }
        Uri uri = intent.getData();
        if (uri == null) {
            return null;
        }
        try {
            return context.getContentResolver().getType(uri);
        } catch (Throwable t) {
            GalleryLog.w("GalleryUtils", "get type fail." + t.getMessage());
            return null;
        }
    }

    public static boolean isInnerVolumeBucketId(String bucketPath) {
        boolean isInnerBucketId = false;
        GalleryStorage galleryStorage = GalleryStorageManager.getInstance().getInnerGalleryStorage();
        if (!(galleryStorage == null || bucketPath == null)) {
            isInnerBucketId = bucketPath.startsWith(galleryStorage.getPath());
        }
        if (isInnerBucketId) {
            return isInnerBucketId;
        }
        galleryStorage = GalleryStorageManager.getInstance().getSubUserGalleryStorage();
        if (galleryStorage == null || bucketPath == null) {
            return isInnerBucketId;
        }
        return bucketPath.startsWith(galleryStorage.getPath());
    }

    public static boolean isOuterVolumeBucketId(String bucketPath) {
        if (bucketPath == null) {
            return false;
        }
        ArrayList<GalleryStorage> outerGalleryStorageList = GalleryStorageManager.getInstance().getOuterGalleryStorageListMountedOnCurrentUser();
        int size = outerGalleryStorageList.size();
        for (int i = 0; i < size; i++) {
            if (bucketPath.startsWith(((GalleryStorage) outerGalleryStorageList.get(i)).getPath())) {
                return true;
            }
        }
        return false;
    }

    public static void updatesCVAAMode(Context context) {
        try {
            if (Secure.getInt(context.getContentResolver(), "touch_exploration_enabled", 0) == 0) {
                sCVAAMode = false;
            } else {
                sCVAAMode = true;
            }
        } catch (Exception e) {
            sCVAAMode = false;
        }
    }

    public static boolean isCVAAMode() {
        return sCVAAMode;
    }

    public static Path updatePathForBurst(AbstractGalleryActivity activity, Path itemPath) {
        if (itemPath == null || !MediaObject.isImageTypeFromPath(itemPath)) {
            return itemPath;
        }
        ContentResolver resolvers = activity.getContentResolver();
        Closeable closeable = null;
        try {
            MediaItem item = (MediaItem) activity.getDataManager().getMediaObject(itemPath);
            if (item == null) {
                return itemPath;
            }
            if (item.isBurstCover() || item.isRefocusPhoto() || item.isDrm() || item.isVoiceImage()) {
                Utils.closeSilently(closeable);
                return itemPath;
            }
            if (BurstUtils.BURST_PATTERN_OTHERS.matcher(item.getName().toUpperCase(Locale.US)).find()) {
                String[] projection = new String[]{"_id"};
                int bucketId = getBucketId(item.getFilePath().substring(0, item.getFilePath().lastIndexOf("/")));
                closeable = resolvers.query(Media.EXTERNAL_CONTENT_URI, projection, "bucket_id = ? AND _data LIKE '%'||?||'_BURST____COVER.JPG' ", new String[]{String.valueOf(bucketId), matchedCover.group(2)}, null);
                if (closeable == null) {
                    Utils.closeSilently(closeable);
                    return itemPath;
                } else if (closeable.moveToNext()) {
                    Path childPath = LocalImage.ITEM_PATH.getChild(closeable.getInt(0));
                    Utils.closeSilently(closeable);
                    return childPath;
                } else {
                    Utils.closeSilently(closeable);
                    return itemPath;
                }
            }
            Utils.closeSilently(closeable);
            return itemPath;
        } catch (RuntimeException e) {
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    public static String getSpecialHideQueryClause(Context context) {
        if (context == null) {
            return "";
        }
        StringBuffer specialWhereClause = new StringBuffer();
        specialWhereClause.append(" AND bucket_id NOT IN (");
        List<String> hideItem = FilePreference.getAll(context);
        if (hideItem.size() == 0) {
            return "";
        }
        int len = hideItem.size();
        int last = len - 1;
        for (int i = 0; i < len; i++) {
            specialWhereClause.append((String) hideItem.get(i));
            if (i != last) {
                specialWhereClause.append(", ");
            }
        }
        specialWhereClause.append(")");
        return specialWhereClause.toString();
    }

    public static ContextThemeWrapper getHwThemeContext(Context context, String theme) {
        if (context == null) {
            GalleryLog.e("GalleryUtils", "error getHwThemeContext context is null");
            return null;
        } else if (TextUtils.isEmpty(theme)) {
            return (ContextThemeWrapper) context;
        } else {
            Context context2 = null;
            int themeID = context.getResources().getIdentifier(theme, null, null);
            if (themeID > 0) {
                context2 = new ContextThemeWrapper(context, themeID);
            }
            return (ContextThemeWrapper) (context2 == null ? context : context2);
        }
    }

    public static String getBurstQueryClause() {
        if (ApiHelper.HAS_MEDIA_COLUMNS_IS_HW_BURST) {
            return "is_hw_burst=1";
        }
        return "_data LIKE '%BURST____COVER.JPG'";
    }

    public static boolean isPathSuffixInteger(String pathSuffix) {
        if (pathSuffix.matches("^-?[0-9]+$")) {
            return true;
        }
        return false;
    }

    public static NumberFormat getPercentFormat(int fraction) {
        NumberFormat pnf = NumberFormat.getPercentInstance();
        pnf.setMinimumFractionDigits(fraction);
        return pnf;
    }

    public static String getPercentString(float pValue, int fraction) {
        return getPercentFormat(fraction).format((double) (pValue / 100.0f));
    }

    public static String getValueFormat(long angle) {
        return NumberFormat.getInstance().format(angle);
    }

    public static void makeOutsideFileForNewAlbum(Activity activity, String bucketPath) {
        makeOutsideFileForNewAlbum(activity.getContentResolver(), (GalleryApp) activity.getApplication(), bucketPath, true);
    }

    public static void makeOutsideFileForNewAlbum(ContentResolver resolver, GalleryApp app, String bucketPath, boolean needEmptyShow) {
        try {
            deleteExtraFile(resolver, bucketPath, ".hidden", ".inside", ".outside", ".empty_out", ".empty_in");
            createExtraFileAndInsertValues(resolver, bucketPath, ".outside");
            if (needEmptyShow) {
                createExtraFileAndInsertValues(resolver, bucketPath, ".empty_out");
            }
            app.getGalleryData().addMaxAlbumIndex(getBucketId(bucketPath), bucketPath);
        } catch (RuntimeException e) {
            GalleryLog.i("GalleryUtils", "Catch a RuntimeException in makeOutsideFileForNewAlbum() method.");
        } catch (Exception e2) {
            GalleryLog.i("GalleryUtils", "insert database error!");
        }
    }

    public static void createExtraFileAndInsertValues(ContentResolver resolver, String bucketPath, String extraSuffix) {
        if (resolver != null) {
            try {
                File extraFile = new File(bucketPath, extraSuffix);
                if (!(extraFile.exists() || extraFile.createNewFile())) {
                    GalleryLog.i("GalleryUtils", "Extra file " + extraSuffix + " create failed ...");
                }
                int bucketId = getBucketId(bucketPath);
                ContentValues values = new ContentValues();
                values.put("_data", bucketPath + File.separator + extraSuffix);
                values.put("bucket_id", String.valueOf(bucketId));
                values.put("media_type", String.valueOf(0));
                values.put("title", extraSuffix);
                resolver.insert(EXTERNAL_FILE_URI, values);
            } catch (Exception e) {
                GalleryLog.i("GalleryUtils", "createNewFile() failed in createExtraFileAndInsertValues() method." + e.getMessage());
            }
        }
    }

    public static void renameExtraFileAndUpdateValues(ContentResolver resolver, String bucketPath, String oldSuffix, String newSuffix) {
        if (resolver != null) {
            try {
                File oldFile = new File(bucketPath, oldSuffix);
                File newFile = new File(bucketPath, newSuffix);
                if (oldFile.exists()) {
                    if (!oldFile.renameTo(newFile)) {
                        GalleryLog.i("GalleryUtils", " renamed extra file failed ...");
                    }
                    ContentValues values = new ContentValues();
                    values.put("_data", newFile.getPath());
                    values.put("title", newFile.getName());
                    resolver.update(EXTERNAL_FILE_URI, values, "_data = ? ", new String[]{oldFile.getPath()});
                }
            } catch (Exception e) {
                GalleryLog.i("GalleryUtils", "An Exception has occurred in renameExtraFileAndUpdateValues() method." + e.getMessage());
            }
        }
    }

    public static void deleteExtraFile(ContentResolver resolver, String bucketPath, String... extraSuffixes) {
        if (extraSuffixes != null) {
            for (String extraSuffix : extraSuffixes) {
                try {
                    resolver.delete(EXTERNAL_FILE_URI, "_data = ? ", new String[]{new File(bucketPath, extraSuffix).toString()});
                    if (!new File(bucketPath, extraSuffix).delete()) {
                        GalleryLog.i("GalleryUtils", extraSuffix + " file delete failed ...");
                    }
                } catch (Exception e) {
                    GalleryLog.i("GalleryUtils", "An Exception has occurred in deleteExtraFile() method." + e.getMessage());
                }
            }
        }
    }

    public static boolean hasSpecialExtraFile(Context context, String albumPath) {
        int bucketId = getBucketId(albumPath);
        String[] projection = new String[]{"count(*)"};
        String whereClause = "bucket_id = ? AND (title='.empty_out' OR title='.empty_in')";
        Closeable closeable = null;
        try {
            closeable = context.getContentResolver().query(EXTERNAL_FILE_URI, projection, whereClause, new String[]{String.valueOf(bucketId)}, null);
            if (closeable != null && closeable.moveToFirst() && closeable.getInt(0) > 0) {
                return true;
            }
            Utils.closeSilently(closeable);
            return false;
        } catch (RuntimeException e) {
            GalleryLog.i("GalleryUtils", "A RuntimeException has occurred in hasSpecialExtraFile() method.");
            return false;
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    public static void printSelectedImage(Context context, MediaItem mediaItem) {
        if (mediaItem != null) {
            List items = new ArrayList(1);
            items.add(mediaItem);
            printSelectedImage(context, items);
        }
    }

    public static void printSelectedImage(Context context, List<MediaItem> mediaItems) {
        if (mediaItems != null && !mediaItems.isEmpty()) {
            List<MediaItem> images = mediaItems;
            Collections.sort(mediaItems, new Comparator<MediaItem>() {
                public int compare(MediaItem lhs, MediaItem rhs) {
                    long left = lhs.getDateInMs();
                    long right = rhs.getDateInMs();
                    if (left < right) {
                        return -1;
                    }
                    if (left > right) {
                        return 1;
                    }
                    return 0;
                }
            });
            try {
                new DocumentPrintHelper(context).printBitmap("gallery pages", mediaItems);
            } catch (FileNotFoundException fnfe) {
                GalleryLog.e("GalleryUtils", "Error printing an image." + fnfe.getMessage());
            } catch (Exception e) {
                GalleryLog.e("GalleryUtils", "Error printing an image" + e.getMessage());
            }
        }
    }

    public static boolean isPrivilegedApp(String className) {
        return Prop4g.sSharePrivilege.contains(className);
    }

    public static boolean checkDiskSpace(String path, long minSize) {
        boolean z = false;
        if (path == null) {
            return false;
        }
        if (getAvailableSpace(path) > minSize) {
            z = true;
        }
        return z;
    }

    public static long getAvailableSpace(String storagePath) {
        try {
            StatFs fs = new StatFs(storagePath);
            return ((long) fs.getAvailableBlocks()) * ((long) fs.getBlockSize());
        } catch (Exception e) {
            GalleryLog.i("GalleryUtils", "Fail to access external storage." + e.getMessage());
            return 0;
        }
    }

    private static String getFileTitle(String path) {
        int lastSlash = path.lastIndexOf(47);
        if (lastSlash >= 0) {
            lastSlash++;
            if (lastSlash < path.length()) {
                path = path.substring(lastSlash);
            }
        }
        int lastDot = path.lastIndexOf(46);
        if (lastDot > 0) {
            return path.substring(0, lastDot);
        }
        return path;
    }

    public static String getMediaItemName(MediaItem item) {
        String filePath = item.getFilePath();
        if (!TextUtils.isEmpty(filePath) && !(item instanceof GalleryMediaItem)) {
            return getFileTitle(filePath);
        }
        String name = item.getName();
        if (TextUtils.isEmpty(name)) {
            return name;
        }
        int lastDot = name.lastIndexOf(46);
        if (lastDot > 0) {
            return name.substring(0, lastDot);
        }
        return name;
    }

    private static String getMimeTypeForUriImage(GalleryApp application, Uri uri) {
        String type;
        if ("file".equals(uri.getScheme())) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()).toLowerCase(Locale.US));
            if (type != null) {
                return type;
            }
        }
        type = application.getContentResolver().getType(uri);
        if (type == null) {
            type = "image/*";
        }
        return type;
    }

    public static Path findPathByUriForUriImage(GalleryApp application, Uri uri, String type) {
        String mimeType = getMimeTypeForUriImage(application, uri);
        if (type == null || ("image/*".equals(type) && mimeType.startsWith("image/"))) {
            type = mimeType;
        }
        if (!type.startsWith("image/")) {
            return null;
        }
        try {
            return Path.fromString("/uri/" + URLEncoder.encode(uri.toString(), "utf-8") + "/" + URLEncoder.encode(type, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    public static void startScanFavoriteService(Context context) {
        context.startService(new Intent(context, ScannerFavoriteService.class));
    }

    public static synchronized int getPackagesVersion(Context context) {
        int i;
        synchronized (GalleryUtils.class) {
            i = PreferenceManager.getDefaultSharedPreferences(context).getInt("packages-version", 1);
        }
        return i;
    }

    public static void playMovieUseHwVPlayer(Activity activity, Uri uri, boolean isSecureCameraAlbum) {
        String[] videoPackage = Constant.getPlayPackageName();
        int i = 0;
        while (i < videoPackage.length) {
            try {
                playMovieUserHwVPlayerInner(activity, uri, isSecureCameraAlbum, videoPackage[i]);
                return;
            } catch (ActivityNotFoundException e) {
                GalleryLog.d("playMovieUseHwVPlayer", "can't find activity. " + videoPackage[i]);
                i++;
            }
        }
        throw new RuntimeException("Original video player not exist.");
    }

    public static Intent getPeekAcitivtyIntent(Context context, MediaItem item, Path mediaSetPath) {
        Intent intent = new Intent(context, SinglePhotoActivity.class);
        intent.setAction("android.intent.action.VIEW");
        intent.setDataAndType(item.getContentUri(), item.getMimeType());
        intent.putExtra("android.intent.action.START_PEEK_ACTIVITY", "startPeekActivity");
        int w = item.getWidth();
        int h = item.getHeight();
        if (w == 0 || h == 0) {
            Options options = getJustDecodeBounds(context, item.getContentUri());
            if (options != null) {
                w = options.outWidth;
                h = options.outHeight;
            }
        }
        if (item.getRotation() % 90 != 0 || item.getRotation() % 180 == 0) {
            intent.putExtra("PeekViewWidth", w);
            intent.putExtra("PeekViewHeight", h);
        } else {
            intent.putExtra("PeekViewWidth", h);
            intent.putExtra("PeekViewHeight", w);
        }
        intent.putExtra("key_item_rotate", item.getRotation());
        intent.putExtra("is_my_favorite", item.isMyFavorite());
        intent.putExtra("media-set-path", mediaSetPath.toString());
        intent.putExtra("is_gif", "image/gif".equals(item.getMimeType()));
        return intent;
    }

    private static void playMovieUserHwVPlayerInner(Activity activity, Uri uri, boolean isSecureCameraAlbum, String packageName) {
        activity.startActivity(new Intent("android.intent.action.VIEW").setDataAndType(uri, "video/*").setComponent(new ComponentName(packageName, "com.huawei.hwvplayer.service.player.FullscreenActivity")).putExtra("is-secure-camera-album", isSecureCameraAlbum));
    }

    public static void playVideoFromCandidate(Activity activity, Uri uri, String title, boolean isSecureCameraAlbum) {
        try {
            activity.startActivity(new Intent().setAction("android.intent.action.VIEW").setDataAndType(uri, "video/*").putExtra("android.intent.extra.TITLE", title).putExtra("is-secure-camera-album", isSecureCameraAlbum));
        } catch (ActivityNotFoundException e) {
            ContextedUtils.showToastQuickly((Context) activity, (int) R.string.video_err, 0);
        }
    }

    public static boolean findString(String match, String targetString) {
        if (match == null || targetString == null) {
            return false;
        }
        return targetString.toUpperCase(Locale.US).contains(match.toUpperCase(Locale.US));
    }

    public static int getAbsoluteLeft(View view) {
        if (view == null) {
            return 0;
        }
        int left = view.getLeft();
        for (ViewParent parent = view.getParent(); parent instanceof View; parent = parent.getParent()) {
            left += ((View) parent).getLeft();
        }
        return left;
    }

    public static boolean supportSetas(MediaItem item) {
        return (item == null || (item.getSupportedOperations() & 32) == 0) ? false : true;
    }

    public static boolean isKeyguardLocked(Context context) {
        boolean z = false;
        if (context == null) {
            return false;
        }
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService("keyguard");
        if (keyguardManager != null) {
            z = keyguardManager.isKeyguardLocked();
        }
        return z;
    }

    public static boolean isScreenOff(Context context) {
        boolean z = false;
        if (context == null) {
            return false;
        }
        PowerManager powerManager = (PowerManager) context.getSystemService("power");
        if (!(powerManager == null || powerManager.isScreenOn())) {
            z = true;
        }
        return z;
    }

    public static int littleEdianByteArrayToInt(byte[] in, int offset, int bytesNum) {
        int value = 0;
        if (bytesNum < 1 || bytesNum > 4) {
            GalleryLog.e("GalleryUtils", "bytesNum must be bigger than 0 and less than 5");
            return 0;
        }
        for (int i = 0; i < bytesNum; i++) {
            value += (in[((bytesNum - 1) - i) + offset] & 255) << (((bytesNum - 1) - i) * 8);
        }
        return value;
    }

    public static Context getContext() {
        return sContext;
    }

    public static boolean isTabletProduct(Context context) {
        return context.getResources().getBoolean(R.bool.is_tablet_product);
    }

    public static Options getJustDecodeBounds(Context context, Uri mSourceUri) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        Options options = new Options();
        try {
            options.inJustDecodeBounds = true;
            parcelFileDescriptor = context.getContentResolver().openFileDescriptor(mSourceUri, "r");
            if (parcelFileDescriptor == null) {
                if (parcelFileDescriptor != null) {
                    try {
                        parcelFileDescriptor.close();
                    } catch (Exception e) {
                    }
                }
                return null;
            }
            BitmapFactory.decodeFileDescriptor(parcelFileDescriptor.getFileDescriptor(), null, options);
            if (parcelFileDescriptor != null) {
                try {
                    parcelFileDescriptor.close();
                } catch (Exception e2) {
                }
            }
            return options;
        } catch (Exception e3) {
            GalleryLog.i("GalleryUtils", "getJustDecodeBounds() failed." + e3.getMessage());
            if (parcelFileDescriptor != null) {
                try {
                    parcelFileDescriptor.close();
                } catch (Exception e4) {
                }
            }
        } catch (Throwable th) {
            if (parcelFileDescriptor != null) {
                try {
                    parcelFileDescriptor.close();
                } catch (Exception e5) {
                }
            }
        }
    }

    public static Bitmap blurBitmap(Context context, Bitmap sourceMap, float radius, float scale) {
        try {
            int postWidth = Math.round(((float) sourceMap.getWidth()) / scale);
            int postHeight = Math.round(((float) sourceMap.getHeight()) / scale);
            Bitmap inBitmap = Bitmap.createScaledBitmap(sourceMap, postWidth, postHeight, false);
            Bitmap outBitmap = Bitmap.createBitmap(inBitmap);
            RenderScript rs = RenderScript.create(context);
            if (rs == null) {
                return null;
            }
            ScriptIntrinsicBlur intrinsicBlur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            Allocation tmpIn = Allocation.createFromBitmap(rs, inBitmap);
            Allocation tmpOut = Allocation.createFromBitmap(rs, outBitmap);
            intrinsicBlur.setRadius(radius);
            intrinsicBlur.setInput(tmpIn);
            intrinsicBlur.forEach(tmpOut);
            tmpOut.copyTo(outBitmap);
            rs.destroy();
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            Bitmap result = Bitmap.createBitmap(outBitmap, 0, 0, postWidth, postHeight, matrix, true);
            inBitmap.recycle();
            outBitmap.recycle();
            return result;
        } catch (Throwable th) {
            return null;
        }
    }

    public static Intent getStartCameraIntent(Activity activity) {
        if (activity == null) {
            return null;
        }
        Intent intent = activity.getPackageManager().getLaunchIntentForPackage("com.huawei.camera");
        if (intent == null) {
            intent = new Intent();
            intent.setAction("android.media.action.STILL_IMAGE_CAMERA");
        } else {
            intent.setFlags(335544320);
        }
        return intent;
    }

    public static boolean forbidWithNetwork() {
        boolean useNetwork = GallerySettings.getBoolean(sContext, GallerySettings.KEY_USE_NETWORK, false);
        if (!IS_CHINESE_VERSION || useNetwork) {
            return false;
        }
        return true;
    }

    public static void resolveWidthAndHeight(ContentValues contentValues, String filePath) {
        try {
            Options opts = new Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, opts);
            if (opts.outWidth > 0 && opts.outHeight > 0) {
                contentValues.put("width", Integer.valueOf(opts.outWidth));
                contentValues.put("height", Integer.valueOf(opts.outHeight));
            }
        } catch (Exception e) {
            GalleryLog.w("photoshareLogTag", "updateWidthAndHeight." + e.getMessage());
        }
    }

    private static void processPreloadMediaDirs() {
        ArrayList<String> allDirectories = new ArrayList();
        String[] hwCfgMediaTypeDirs = HwCfgFilePolicy.getCfgPolicyDir(1);
        sPreloadMediaDirectory = "/system/media/Pre-loaded";
        if (hwCfgMediaTypeDirs != null) {
            String absPath = "";
            for (String hwCfgMediaTypeDir : hwCfgMediaTypeDirs) {
                try {
                    absPath = new File(hwCfgMediaTypeDir).getCanonicalPath();
                } catch (IOException e) {
                    GalleryLog.e("GalleryUtils", "Exception: " + e);
                }
                allDirectories.add(absPath + "/media");
                allDirectories.add("/data/hw_init" + absPath + "/media");
            }
            allDirectories.add(Environment.getRootDirectory() + "/media");
            for (String dir : allDirectories) {
                File file = new File(dir + "/Pre-loaded");
                if (file.exists() && file.isDirectory()) {
                    sPreloadMediaDirectory = dir + "/Pre-loaded";
                    WhiteList.updatePreloadedPathsForWhiteList(sPreloadMediaDirectory);
                    return;
                }
            }
        }
    }

    public static Point decodeBounds(String filePath) {
        try {
            Options opts = new Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, opts);
            if (opts.outWidth > 0 && opts.outHeight > 0) {
                return new Point(opts.outWidth, opts.outHeight);
            }
        } catch (Exception e) {
            GalleryLog.w("GalleryUtils", "updateWidthAndHeight. " + e.getMessage());
        }
        return null;
    }

    public static String getPreloadMediaDirectory() {
        if (sPreloadMediaDirectory == null) {
            processPreloadMediaDirs();
        }
        return sPreloadMediaDirectory;
    }

    public static void startActivityCatchSecurityEx(Context activity, Intent intent) {
        if (activity != null && intent != null) {
            try {
                activity.startActivity(intent);
            } catch (SecurityException e) {
                GalleryLog.w("GalleryUtils", "the target app has no permission of media");
            } catch (ActivityNotFoundException e2) {
                GalleryLog.w("GalleryUtils", "the target activity is not found: " + e2.getMessage());
            } catch (Exception e3) {
                GalleryLog.w("GalleryUtils", "start activity failed, message: " + e3.getMessage());
            }
        }
    }

    public static void startActivityForResultCatchSecurityEx(Activity activity, Intent intent, int requestCode) {
        if (activity != null && intent != null) {
            try {
                activity.startActivityForResult(intent, requestCode);
            } catch (SecurityException e) {
                GalleryLog.w("GalleryUtils", "the target app has no permission of media");
            } catch (ActivityNotFoundException e2) {
                GalleryLog.w("GalleryUtils", "the target activity is not found: " + e2.getMessage());
            } catch (Exception e3) {
                GalleryLog.w("GalleryUtils", "start activity failed, message: " + e3.getMessage());
            }
        }
    }

    public static Path getCleanPath(String path) {
        Path p = Path.fromString(path);
        p.clearObject();
        return p;
    }

    public static <T> T[] arraysCombine(T[] first, T[] second) {
        if (first == null || first.length == 0) {
            return second;
        }
        if (second == null || second.length == 0) {
            return first;
        }
        int offset = first.length;
        T[] result = Arrays.copyOf(first, second.length + offset);
        System.arraycopy(second, 0, result, offset, second.length);
        return result;
    }
}
