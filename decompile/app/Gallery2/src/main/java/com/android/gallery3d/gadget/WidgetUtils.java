package com.android.gallery3d.gadget;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Process;
import android.view.View;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.MediaSetUtils;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.storage.GalleryStorageManager;
import dalvik.system.PathClassLoader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class WidgetUtils {
    private static final String CLASS_NAME = "com.huawei.hwtransition.HwTransition";
    private static final int FILE_MODE = 508;
    private static final String JAR_PAHT = "/system/framework/hwtransition.jar";
    private static final String TAG = "WidgetUtils";
    public static final String WIDGET_2X1 = "2*1";
    public static final String WIDGET_2X2 = "2*2";
    private static final String WIDGET_ID = "widget_id";
    private static final String WIDGET_INIT = "widget_init";
    public static final String WIDGET_RECORD = "gallery_widget_record";
    public static final String WIDGET_TYPE = "widget_type";
    private static Class<?> clazz = null;

    private WidgetUtils() {
    }

    public static Class<?> getHwTransitionClass(Context context) {
        if (clazz == null) {
            try {
                clazz = getPathClassLoader(context).loadClass(CLASS_NAME);
            } catch (ClassNotFoundException e) {
                GalleryLog.i(TAG, "Not found class:com.huawei.hwtransition.HwTransition");
            }
        }
        return clazz;
    }

    private static PathClassLoader getPathClassLoader(Context context) {
        return new PathClassLoader(JAR_PAHT, context.getClassLoader());
    }

    public static boolean isVisible(View view) {
        View view2 = null;
        View cellLayout = null;
        while (view != null) {
            if (!view.getClass().getName().endsWith("CellLayout")) {
                if (!(view.getParent() instanceof View)) {
                    break;
                }
                view = (View) view.getParent();
            } else {
                cellLayout = view;
                view2 = (View) view.getParent();
                break;
            }
        }
        if (!(view2 == null || cellLayout == null)) {
            int scrollX = view2.getScrollX();
            int pleft = view2.getLeft();
            int pright = view2.getRight();
            int left = cellLayout.getLeft();
            if (scrollX + pleft >= cellLayout.getRight() || scrollX + pright <= left) {
                return false;
            }
        }
        return true;
    }

    public static boolean isCutBitmap(Context context) {
        if (context.getResources().getColor(R.color.cutbitmap_tag) == -1) {
            return true;
        }
        return false;
    }

    public static boolean isScreenScroll(View targetView) {
        View view = null;
        View cellLayout = null;
        while (targetView != null) {
            if (!targetView.getClass().getName().endsWith("CellLayout")) {
                if (!(targetView.getParent() instanceof View)) {
                    break;
                }
                targetView = (View) targetView.getParent();
            } else {
                cellLayout = targetView;
                view = (View) targetView.getParent();
                break;
            }
        }
        if (!(view == null || cellLayout == null)) {
            int scrollX = view.getScrollX();
            if (scrollX == 0 || scrollX % view.getWidth() < 3) {
                return false;
            }
        }
        return true;
    }

    public static synchronized void setSharedPrefer(Context context, int widgetId, String bucket_id) {
        synchronized (WidgetUtils.class) {
            SharedPreferences sp = context.getSharedPreferences(WIDGET_RECORD, 4);
            if (!sp.contains("widget_id_" + widgetId)) {
                ReportToBigData.report(56, String.format("{WidgetAction:%s}", new Object[]{"AddWidget"}));
            }
            Editor editer = sp.edit();
            editer.putString("widget_id_" + widgetId, bucket_id);
            editer.commit();
        }
    }

    public static synchronized String getSharedPrefer(Context context, int widgetId) {
        String bucket_id;
        synchronized (WidgetUtils.class) {
            SharedPreferences sp = context.getSharedPreferences(WIDGET_RECORD, 4);
            bucket_id = null;
            if (sp.contains("widget_id_" + widgetId)) {
                bucket_id = sp.getString("widget_id_" + widgetId, null);
            }
        }
        return bucket_id;
    }

    public static synchronized void delete(Context context, int widgetId) {
        synchronized (WidgetUtils.class) {
            SharedPreferences sp = context.getSharedPreferences(WIDGET_RECORD, 4);
            if (sp.contains("widget_id_" + widgetId)) {
                Editor editor = sp.edit();
                editor.remove("widget_id_" + widgetId);
                editor.commit();
                ReportToBigData.report(56, String.format("{WidgetAction:%s}", new Object[]{"DeleteWidget"}));
            }
        }
    }

    public static void setAlbumPath(Context context, int widgetId, String albumPath) {
        Editor editer = context.getSharedPreferences(WIDGET_RECORD, 4).edit();
        editer.putString("albumpath_widget_id_" + widgetId, albumPath);
        editer.commit();
    }

    public static String getAlbumPath(Context context, int widgetId) {
        return context.getSharedPreferences(WIDGET_RECORD, 4).getString("albumpath_widget_id_" + widgetId, null);
    }

    public static void deleteAlbumPath(Context context, int widgetId) {
        SharedPreferences sp = context.getSharedPreferences(WIDGET_RECORD, 4);
        if (sp.contains("albumpath_widget_id_" + widgetId)) {
            Editor editor = sp.edit();
            editor.remove("albumpath_widget_id_" + widgetId);
            editor.commit();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean savePng(Bitmap bitmap, int widgetId) {
        BufferedOutputStream bos;
        Throwable th;
        String strFilename = getCacheFilePath(widgetId);
        FileOutputStream fileOutputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        boolean bRet = false;
        try {
            File file = new File(strFilename);
            if (!file.exists()) {
                file = new File(getCachePath());
                if (!file.exists()) {
                    if (!file.mkdirs()) {
                        return false;
                    }
                    FileUtils.setPermissions(file.getAbsolutePath(), FILE_MODE, Process.myUid(), 1023);
                }
                file = new File(strFilename);
                if (!file.createNewFile()) {
                    return false;
                }
                FileUtils.setPermissions(file.getAbsolutePath(), FILE_MODE, Process.myUid(), 1023);
            } else if (!file.delete()) {
                return false;
            }
            FileOutputStream fs = new FileOutputStream(strFilename);
            try {
                bos = new BufferedOutputStream(fs);
            } catch (IOException e) {
                fileOutputStream = fs;
                GalleryLog.i(TAG, "Catch an IOException in savePng() method.");
                if (bufferedOutputStream != null) {
                    try {
                        bufferedOutputStream.flush();
                        try {
                            bufferedOutputStream.close();
                        } catch (IOException e2) {
                            GalleryLog.i(TAG, "BufferedOutputStream.close() faield in savePng() method, reason: IOException.");
                        }
                    } catch (IOException e3) {
                        GalleryLog.i(TAG, "BufferedOutputStream.flush() faield in savePng() method, reason: IOException.");
                    } catch (Throwable th2) {
                        try {
                            bufferedOutputStream.close();
                        } catch (IOException e4) {
                            GalleryLog.i(TAG, "BufferedOutputStream.close() faield in savePng() method, reason: IOException.");
                        }
                    }
                }
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e5) {
                        GalleryLog.i(TAG, "FileOutputStream.close() failed in savePng() method, reason: IOException.");
                    }
                }
                return bRet;
            } catch (Exception e6) {
                fileOutputStream = fs;
                try {
                    GalleryLog.e(TAG, "may be bitmap Recycled");
                    if (bufferedOutputStream != null) {
                        try {
                            bufferedOutputStream.flush();
                            try {
                                bufferedOutputStream.close();
                            } catch (IOException e7) {
                                GalleryLog.i(TAG, "BufferedOutputStream.close() faield in savePng() method, reason: IOException.");
                            }
                        } catch (IOException e8) {
                            GalleryLog.i(TAG, "BufferedOutputStream.flush() faield in savePng() method, reason: IOException.");
                        } catch (Throwable th3) {
                            try {
                                bufferedOutputStream.close();
                            } catch (IOException e9) {
                                GalleryLog.i(TAG, "BufferedOutputStream.close() faield in savePng() method, reason: IOException.");
                            }
                        }
                    }
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e10) {
                            GalleryLog.i(TAG, "FileOutputStream.close() failed in savePng() method, reason: IOException.");
                        }
                    }
                    return bRet;
                } catch (Throwable th4) {
                    th = th4;
                    if (bufferedOutputStream != null) {
                        try {
                            bufferedOutputStream.flush();
                            try {
                                bufferedOutputStream.close();
                            } catch (IOException e11) {
                                GalleryLog.i(TAG, "BufferedOutputStream.close() faield in savePng() method, reason: IOException.");
                            }
                        } catch (IOException e12) {
                            GalleryLog.i(TAG, "BufferedOutputStream.flush() faield in savePng() method, reason: IOException.");
                        } catch (Throwable th5) {
                            try {
                                bufferedOutputStream.close();
                            } catch (IOException e13) {
                                GalleryLog.i(TAG, "BufferedOutputStream.close() faield in savePng() method, reason: IOException.");
                            }
                        }
                    }
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e14) {
                            GalleryLog.i(TAG, "FileOutputStream.close() failed in savePng() method, reason: IOException.");
                        }
                    }
                    throw th;
                }
            } catch (Throwable th6) {
                th = th6;
                fileOutputStream = fs;
                if (bufferedOutputStream != null) {
                    bufferedOutputStream.flush();
                    bufferedOutputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                throw th;
            }
            try {
                bRet = bitmap.compress(CompressFormat.PNG, 100, fs);
                if (bos != null) {
                    try {
                        bos.flush();
                        try {
                            bos.close();
                        } catch (IOException e15) {
                            GalleryLog.i(TAG, "BufferedOutputStream.close() faield in savePng() method, reason: IOException.");
                        }
                    } catch (IOException e16) {
                        GalleryLog.i(TAG, "BufferedOutputStream.flush() faield in savePng() method, reason: IOException.");
                    } catch (Throwable th7) {
                        try {
                            bos.close();
                        } catch (IOException e17) {
                            GalleryLog.i(TAG, "BufferedOutputStream.close() faield in savePng() method, reason: IOException.");
                        }
                    }
                }
                if (fs != null) {
                    try {
                        fs.close();
                    } catch (IOException e18) {
                        GalleryLog.i(TAG, "FileOutputStream.close() failed in savePng() method, reason: IOException.");
                    }
                }
                fileOutputStream = fs;
            } catch (IOException e19) {
                bufferedOutputStream = bos;
                fileOutputStream = fs;
                GalleryLog.i(TAG, "Catch an IOException in savePng() method.");
                if (bufferedOutputStream != null) {
                    bufferedOutputStream.flush();
                    bufferedOutputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                return bRet;
            } catch (Exception e20) {
                bufferedOutputStream = bos;
                fileOutputStream = fs;
                GalleryLog.e(TAG, "may be bitmap Recycled");
                if (bufferedOutputStream != null) {
                    bufferedOutputStream.flush();
                    bufferedOutputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                return bRet;
            } catch (Throwable th8) {
                th = th8;
                bufferedOutputStream = bos;
                fileOutputStream = fs;
                if (bufferedOutputStream != null) {
                    bufferedOutputStream.flush();
                    bufferedOutputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                throw th;
            }
        } catch (IOException e21) {
            GalleryLog.i(TAG, "Catch an IOException in savePng() method.");
            if (bufferedOutputStream != null) {
                bufferedOutputStream.flush();
                bufferedOutputStream.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            return bRet;
        } catch (Exception e22) {
            GalleryLog.e(TAG, "may be bitmap Recycled");
            if (bufferedOutputStream != null) {
                bufferedOutputStream.flush();
                bufferedOutputStream.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            return bRet;
        }
        return bRet;
    }

    private static final String getCacheFilePath(int widgetId) {
        return getCachePath() + "widget_shadow_" + widgetId + ".png";
    }

    private static final String getCachePath() {
        return Environment.getExternalStorageDirectory().getPath() + "/data/com.android.gallery3d/.cache/";
    }

    public static boolean deletePng(int widgetId) {
        File file = new File(getCacheFilePath(widgetId));
        if (!file.exists() || file.delete()) {
            return true;
        }
        return false;
    }

    public static Bitmap readPng(int widgetId) {
        BufferedInputStream bos;
        Bitmap bitmap;
        Throwable th;
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        try {
            FileInputStream fs = new FileInputStream(getCacheFilePath(widgetId));
            try {
                bos = new BufferedInputStream(fs);
            } catch (Throwable th2) {
                th = th2;
                fileInputStream = fs;
                if (bufferedInputStream != null) {
                    try {
                        bufferedInputStream.close();
                    } catch (IOException e) {
                        GalleryLog.i(TAG, "BufferedInputStream.close() failed in readPng() method, reason: IOException.");
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e2) {
                        GalleryLog.i(TAG, "FileInputStream.close() failed in readPng() method, reason: IOException.");
                    }
                }
                throw th;
            }
            try {
                bitmap = BitmapFactory.decodeStream(bos);
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e3) {
                        GalleryLog.i(TAG, "BufferedInputStream.close() failed in readPng() method, reason: IOException.");
                    }
                }
                if (fs != null) {
                    try {
                        fs.close();
                    } catch (IOException e4) {
                        GalleryLog.i(TAG, "FileInputStream.close() failed in readPng() method, reason: IOException.");
                    }
                }
                fileInputStream = fs;
            } catch (Throwable th3) {
                th = th3;
                bufferedInputStream = bos;
                fileInputStream = fs;
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            throw th;
        }
        return bitmap;
    }

    public static int getCurrentAppWidgetId(View view) {
        View parent = (View) view.getParent();
        while (parent != null) {
            if (!(parent instanceof AppWidgetHostView)) {
                if (!(parent.getParent() instanceof View)) {
                    break;
                }
                parent = (View) parent.getParent();
            } else {
                int appWidgetId = ((AppWidgetHostView) parent).getAppWidgetId();
                GalleryLog.i(TAG, "appWidgetId = " + appWidgetId);
                return appWidgetId;
            }
        }
        return 0;
    }

    public static boolean isCameraAlbum(String bucketId) {
        if (bucketId == null) {
            return false;
        }
        boolean z;
        bucketId = bucketId.split("/")[0];
        if (bucketId.startsWith(String.valueOf(MediaSetUtils.getCameraBucketId()))) {
            z = true;
        } else {
            z = GalleryStorageManager.getInstance().isOuterGalleryStorageCameraBucketID(bucketId);
        }
        return z;
    }
}
