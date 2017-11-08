package com.huawei.gallery.ui.stackblur;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.android.gallery3d.util.GalleryLog;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StackBlurUtils {
    static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(EXECUTOR_THREADS);
    static final int EXECUTOR_THREADS = Runtime.getRuntime().availableProcessors();
    private static volatile boolean hasRS = true;
    private static WeakReference<BlurProcess> sProcess;

    public static synchronized Bitmap getBlurBitmap(Context context, Bitmap image, int radius) {
        Bitmap result;
        synchronized (StackBlurUtils.class) {
            BlurProcess blurProcess = null;
            if (sProcess != null) {
                blurProcess = (BlurProcess) sProcess.get();
            }
            if (blurProcess == null) {
                blurProcess = createProcess(context);
                sProcess = new WeakReference(blurProcess);
            }
            result = blurProcess.blur(image, (float) radius);
        }
        return result;
    }

    public static Drawable getDefaultBlurBackground(Context context, Bitmap image) {
        Bitmap result = getBlurBitmap(context, image, 20);
        if (result == null) {
            return null;
        }
        new Canvas(result).drawColor(-1291845632);
        return new BitmapDrawable(context.getResources(), result);
    }

    private static BlurProcess createProcess(Context context) {
        if (!hasRS) {
            return new JavaBlurProcess();
        }
        try {
            return new RSBlurProcess(context);
        } catch (Throwable th) {
            GalleryLog.d("StackBlurUtils", "Do not support Render Script." + th.getMessage());
            BlurProcess blurProcess = new JavaBlurProcess();
            hasRS = false;
            return blurProcess;
        }
    }
}
