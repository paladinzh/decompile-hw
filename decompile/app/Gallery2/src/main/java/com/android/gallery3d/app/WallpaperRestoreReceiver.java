package com.android.gallery3d.app;

import android.annotation.TargetApi;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Process;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.WallpaperUtils;
import com.autonavi.amap.mapcore.ERROR_CODE;
import com.huawei.gallery.util.BundleUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class WallpaperRestoreReceiver extends BroadcastReceiver {
    private static final String PATH_WALLPAPER = (Environment.getDataDirectory() + "/skin/wallpaper");

    public void onReceive(Context context, Intent intent) {
        int actionType = BundleUtils.getInt(intent.getExtras(), "action_type", 0);
        GalleryLog.d("WallpaperRestoreReceiver", " check wallpaper. action->" + intent.getAction() + ",  action_type->" + actionType);
        if (actionType == ERROR_CODE.CONN_CREATE_FALSE) {
            Intent i = new Intent("com.huawei.wallpaper.action.CHECK");
            i.setClass(context, WallpaperService.class);
            context.startService(i);
        } else if ("android.intent.action.PRE_BOOT_COMPLETED".equals(intent.getAction())) {
            checkWallpaperForOTA(context);
        }
    }

    private void checkWallpaperForOTA(Context context) {
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        GalleryLog.d("WallpaperRestoreReceiver", "called checkWallpaperForOTA to restore wallpaper");
        File file = new File(PATH_WALLPAPER);
        file = new File(file, "gallery_home_wallpaper_0.jpg");
        WallpaperManager wallpaperManager = (WallpaperManager) context.getSystemService("wallpaper");
        if (wallpaperManager.getWallpaperInfo() != null) {
            GalleryLog.d("WallpaperRestoreReceiver", "current is live wallpaper, no need to process.");
            return;
        }
        Drawable wallpaper = wallpaperManager.getDrawable();
        if (wallpaper != null) {
            Bitmap bitmap = Bitmap.createBitmap(wallpaper.getIntrinsicWidth(), wallpaper.getIntrinsicHeight(), Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            wallpaper.setBounds(0, 0, wallpaper.getIntrinsicWidth(), wallpaper.getIntrinsicHeight());
            wallpaper.draw(canvas);
            if (bitmap == null) {
                GalleryLog.d("WallpaperRestoreReceiver", "can't decode wallpaper from WallpaperManager");
                return;
            }
            boolean isFixed = WallpaperUtils.checkBitmapLine(bitmap, bitmap.getWidth(), bitmap.getHeight());
            GalleryLog.d("WallpaperRestoreReceiver", "checkBitmapLine isFixed ? " + isFixed);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if (isFixed) {
                Point size = getDisplaySize(context);
                GalleryLog.d("WallpaperRestoreReceiver", String.format("repair wallpaper(%sx%s) to screen size:%s ", new Object[]{Integer.valueOf(width), Integer.valueOf(height), size}));
                if (Math.abs((size.y * width) - (size.x * height)) < 10) {
                    GalleryLog.d("WallpaperRestoreReceiver", "aspect is right, no need to change.");
                    return;
                }
                Bitmap result = Bitmap.createBitmap(size.x, size.y, Config.ARGB_8888);
                Canvas cropCanvas = new Canvas(result);
                Rect dst = new Rect(0, 0, size.x, size.y);
                cropCanvas.drawBitmap(bitmap, new Rect(0, 0, width / 2, height), dst, new Paint(2));
                GalleryLog.d("WallpaperRestoreReceiver", "Crop bitmap done, will call setWallpaper.");
                file = new File(file, "gallery_home_wallpaper_0.tmp");
                FileOutputStream fileOutputStream = null;
                try {
                    GalleryLog.d("WallpaperRestoreReceiver", "set wallpaper - WallpaperManager.setBitmap(result)  start");
                    WallpaperManager.getInstance(context).setBitmap(result);
                    GalleryLog.d("WallpaperRestoreReceiver", "set wallpaper - WallpaperManager.setBitmap(result)  done");
                    FileOutputStream fileOutputSteam = new FileOutputStream(file);
                    try {
                        result.compress(CompressFormat.JPEG, 90, fileOutputSteam);
                        GalleryLog.d("WallpaperRestoreReceiver", "delete old file gallery_home_wallpaper_0.jpg, result is " + file.delete());
                        GalleryLog.d("WallpaperRestoreReceiver", "rename gallery_home_wallpaper_0.tmp to gallery_home_wallpaper_0.jpg, resule is " + file.renameTo(file));
                        FileUtils.setPermissions(file.getPath(), 508, Process.myUid(), 1023);
                        if (fileOutputSteam != null) {
                            try {
                                fileOutputSteam.close();
                            } catch (IOException e3) {
                            }
                        }
                    } catch (FileNotFoundException e4) {
                        e = e4;
                        fileOutputStream = fileOutputSteam;
                        GalleryLog.d("WallpaperRestoreReceiver", "compress bitmap failed, delete temp file " + file.delete() + "." + e.getMessage());
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e5) {
                            }
                        }
                        GalleryLog.d("WallpaperRestoreReceiver", "start phone first time, and wallpaper isFixed? " + isFixed);
                    } catch (IOException e6) {
                        e2 = e6;
                        fileOutputStream = fileOutputSteam;
                        try {
                            GalleryLog.w("WallpaperRestoreReceiver", "fail to set wall paper." + e2.getMessage());
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (IOException e7) {
                                }
                            }
                            GalleryLog.d("WallpaperRestoreReceiver", "start phone first time, and wallpaper isFixed? " + isFixed);
                        } catch (Throwable th2) {
                            th = th2;
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (IOException e8) {
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        fileOutputStream = fileOutputSteam;
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e9) {
                    e = e9;
                    GalleryLog.d("WallpaperRestoreReceiver", "compress bitmap failed, delete temp file " + file.delete() + "." + e.getMessage());
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    GalleryLog.d("WallpaperRestoreReceiver", "start phone first time, and wallpaper isFixed? " + isFixed);
                } catch (IOException e10) {
                    e2 = e10;
                    GalleryLog.w("WallpaperRestoreReceiver", "fail to set wall paper." + e2.getMessage());
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    GalleryLog.d("WallpaperRestoreReceiver", "start phone first time, and wallpaper isFixed? " + isFixed);
                }
            }
            GalleryLog.d("WallpaperRestoreReceiver", "start phone first time, and wallpaper isFixed? " + isFixed);
        }
    }

    @TargetApi(13)
    public Point getDisplaySize(Context context) {
        Point size = new Point();
        Display d = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);
        size.x = metrics.widthPixels;
        size.y = metrics.heightPixels;
        try {
            if (VERSION.SDK_INT < 14 || VERSION.SDK_INT >= 17) {
                if (VERSION.SDK_INT >= 17) {
                    Display.class.getMethod("getRealSize", new Class[]{Point.class}).invoke(d, new Object[]{size});
                }
                return size;
            }
            size.x = ((Integer) Display.class.getMethod("getRawWidth", new Class[0]).invoke(d, new Object[0])).intValue();
            size.y = ((Integer) Display.class.getMethod("getRawHeight", new Class[0]).invoke(d, new Object[0])).intValue();
            return size;
        } catch (NoSuchMethodException e) {
            GalleryLog.e("WallpaperRestoreReceiver", "NoSuchMethodException when getMethod.");
        } catch (IllegalAccessException e2) {
            GalleryLog.e("WallpaperRestoreReceiver", "IllegalAccessException when invoke.");
        } catch (InvocationTargetException e3) {
            GalleryLog.e("WallpaperRestoreReceiver", "InvocationTargetException when invoke.");
        } catch (RuntimeException e4) {
            GalleryLog.d("WallpaperRestoreReceiver", "Error when get full screen height.");
        }
    }
}
