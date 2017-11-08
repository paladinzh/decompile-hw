package com.android.keyguard.hwlockscreen.magazine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import com.huawei.keyguard.util.HwUnlockUtils;
import com.huawei.keyguard.util.WallpaperUtils;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MagazineWaterMark {
    public static final String WATER_MARK_DIR = (Environment.getExternalStorageDirectory().getPath() + "/MagazineUnlock/");

    public static boolean generateWatermarkBitmapFile(Context context, String path, String des, String pathSave) {
        if (context == null || TextUtils.isEmpty(path) || TextUtils.isEmpty(des) || TextUtils.isEmpty(pathSave)) {
            Log.e("WaterMarkUtils", "generateWatermarkBitmapFile param is empty, return false");
            return false;
        }
        Point outSize = new Point();
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getRealSize(outSize);
        Bitmap src = WallpaperUtils.clipWallpaperDrawable(Drawable.createFromPath(path), outSize);
        if (src == null) {
            Log.e("WaterMarkUtils", "generateWatermarkBitmapFile bitmap src is null, return false");
            return false;
        }
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        Log.v("WaterMarkUtils", "srcWidth:" + srcWidth + ", srcHeight:" + srcHeight);
        float zoom = ((float) srcWidth) / 1080.0f;
        if (HwUnlockUtils.isLandscape(context)) {
            zoom = 1.0f;
        }
        int wmTextSize = (int) (39.0f * zoom);
        int wmTextMarginLeft = (int) (48.0f * zoom);
        int wmTextMarginBottom = (int) (36.0f * zoom);
        int wmMarginBottom = (int) (210.0f * zoom);
        Bitmap wmBmp = Bitmap.createBitmap(srcWidth, srcHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(wmBmp);
        canvas.drawBitmap(src, 0.0f, 0.0f, null);
        src.recycle();
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(-1);
        textPaint.setTextSize((float) wmTextSize);
        textPaint.setAntiAlias(true);
        StaticLayout layout = new StaticLayout(des, textPaint, srcWidth - (wmTextMarginLeft * 2), Alignment.ALIGN_NORMAL, 1.2f, 0.0f, true);
        int textHeight = layout.getHeight();
        Paint bgPaint = new Paint();
        bgPaint.setColor(-16777216);
        bgPaint.setStyle(Style.FILL);
        canvas.drawRect(0.0f, (float) (((srcHeight - textHeight) - (wmTextMarginBottom * 2)) - wmMarginBottom), (float) srcWidth, (float) srcHeight, bgPaint);
        canvas.translate((float) wmTextMarginLeft, (float) (((srcHeight - textHeight) - wmTextMarginBottom) - wmMarginBottom));
        layout.draw(canvas);
        canvas.save(31);
        canvas.restore();
        if (saveWaterMarkBitmap(wmBmp, pathSave)) {
            wmBmp.recycle();
            Log.i("WaterMarkUtils", "generateWatermarkBitmapFile succ, return true");
            return true;
        }
        wmBmp.recycle();
        Log.i("WaterMarkUtils", "generateWatermarkBitmapFile saveWaterMarkBitmap fail, return false");
        return false;
    }

    private static boolean saveWaterMarkBitmap(Bitmap bm, String path) {
        Throwable th;
        File wmDir = new File(WATER_MARK_DIR);
        if (wmDir.exists()) {
            if (!(wmDir.isDirectory() || (wmDir.delete() && wmDir.mkdir()))) {
                return false;
            }
        } else if (!wmDir.mkdir()) {
            return false;
        }
        File f = new File(path);
        if (f.exists() && !f.delete()) {
            return false;
        }
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream out = new FileOutputStream(f);
            try {
                bm.compress(CompressFormat.JPEG, 90, out);
                out.flush();
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        Log.e("WaterMarkUtils", "saveWaterMarkBitmap out.close fail");
                    }
                }
                return true;
            } catch (FileNotFoundException e2) {
                fileOutputStream = out;
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e3) {
                        Log.e("WaterMarkUtils", "saveWaterMarkBitmap out.close fail");
                    }
                }
                return false;
            } catch (IOException e4) {
                fileOutputStream = out;
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e5) {
                        Log.e("WaterMarkUtils", "saveWaterMarkBitmap out.close fail");
                    }
                }
                return false;
            } catch (Throwable th2) {
                th = th2;
                fileOutputStream = out;
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e6) {
                        Log.e("WaterMarkUtils", "saveWaterMarkBitmap out.close fail");
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e7) {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            return false;
        } catch (IOException e8) {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            return false;
        } catch (Throwable th3) {
            th = th3;
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            throw th;
        }
    }

    public static String getWaterMarkFileSavePath(String path) {
        if (!TextUtils.isEmpty(path) && path.contains("/")) {
            String srcFileName = path.substring(path.lastIndexOf("/") + 1);
            if (srcFileName.contains(".")) {
                return WATER_MARK_DIR + "hw_watermark_" + srcFileName.substring(0, srcFileName.lastIndexOf(".")) + ".jpg";
            }
        }
        return null;
    }

    public static void deleteNotUsedWaterMarkFiles() {
        File waterMarkDir = new File(WATER_MARK_DIR);
        if (waterMarkDir.exists() && waterMarkDir.isDirectory()) {
            File[] waterMarkFiles = waterMarkDir.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    if (pathname.isFile() && pathname.getName().contains("hw_watermark_")) {
                        return true;
                    }
                    return false;
                }
            });
            if (waterMarkFiles != null && waterMarkFiles.length != 0) {
                for (File file : waterMarkFiles) {
                    if (!file.delete()) {
                        Log.e("WaterMarkUtils", "delete temp wmbmp file fail");
                    }
                }
            }
        }
    }
}
