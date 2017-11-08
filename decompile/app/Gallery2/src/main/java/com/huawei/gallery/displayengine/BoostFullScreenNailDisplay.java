package com.huawei.gallery.displayengine;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.DecodeUtils;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.util.DisplayEngineUtils;
import com.android.gallery3d.util.DrmUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.android.gallery3d.util.TraceController;
import com.huawei.watermark.manager.parse.WMElement;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class BoostFullScreenNailDisplay {
    private static ScreenNailCommonDisplayEnginePool sCommonDisplayEnginePool = new ScreenNailCommonDisplayEnginePool();
    private static Bitmap sFullScreenNailBitmap = null;
    private static GenerateFullScreenNailThread sGenerateFullScreenNailThread = null;
    private static MediaItem sItem = null;

    private static class GenerateFullScreenNailThread extends Thread {
        private JobContext mJobContext;

        public GenerateFullScreenNailThread(JobContext jc) {
            this.mJobContext = jc;
        }

        public void run() {
            TraceController.traceBegin("BoostFullScreenNailDisplay.GenerateFullScreenNailThread.run");
            BoostFullScreenNailDisplay.sFullScreenNailBitmap = BoostFullScreenNailDisplay.generateFullScreenNail(this.mJobContext, BoostFullScreenNailDisplay.sItem, BoostFullScreenNailDisplay.sCommonDisplayEnginePool);
            TraceController.traceEnd();
        }
    }

    private static class ScreenNailScaleDisplayEngineCreateThread extends Thread {
        private float mMaxScale = 0.0f;
        private ScreenNailScaleDisplayEngine snScaleDisplayEngine = null;
        private int srcHeight = 0;
        private int srcWidth = 0;

        public ScreenNailScaleDisplayEngineCreateThread(int inputWidth, int inputHeight, float maxScale) {
            this.srcWidth = inputWidth;
            this.srcHeight = inputHeight;
            this.mMaxScale = maxScale;
        }

        public void run() {
            TraceController.traceBegin("BoostFullScreenNailDisplay.ScaleCreateThread.run srcWidth=" + this.srcWidth + ", srcHeight=" + this.srcHeight);
            if (this.snScaleDisplayEngine != null) {
                this.snScaleDisplayEngine.destroy();
                this.snScaleDisplayEngine = null;
            }
            if (this.srcWidth > 0 && this.srcHeight > 0) {
                this.snScaleDisplayEngine = (ScreenNailScaleDisplayEngine) DisplayEngineFactory.buildDisplayEngine(this.srcWidth, this.srcHeight, 0, 6, this.mMaxScale);
                if (this.snScaleDisplayEngine == null) {
                    GalleryLog.e("BoostFullScreenNailDisplay", "scaleDisplayEngine create failed ");
                }
            }
            TraceController.traceEnd();
        }

        public ScreenNailScaleDisplayEngine getScreenNailScaleDisplayEngine() {
            return this.snScaleDisplayEngine;
        }
    }

    private static Bitmap generateFullScreenNail(JobContext jc, MediaItem mediaItem, ScreenNailCommonDisplayEnginePool displayEnginePool) {
        IOException e;
        Throwable th;
        int tImageWidth = 0;
        int tImageHeight = 0;
        int tLevel = 0;
        float tScaleLevelDownScaleImg2Dst = 0.0f;
        ScreenNailScaleDisplayEngineCreateThread tSnScaleCreateThread = null;
        if (DisplayEngineUtils.isDisplayEngineEnable()) {
            String filePath = mediaItem.getFilePath();
            GalleryLog.d("BoostFullScreenNailDisplay", "CurrentMediaItem filePath = " + filePath);
            Options options = new Options();
            options.inPreferredConfig = Config.ARGB_8888;
            TraceController.traceBegin("BoostFullScreenNailDisplay.generateFullScreenNail decodeFileDescriptor filePath = " + filePath);
            options.inJustDecodeBounds = true;
            if (DrmUtils.isDrmFile(filePath)) {
                Closeable closeable = null;
                try {
                    Closeable fileInputStream = new FileInputStream(filePath);
                    try {
                        BitmapFactory.decodeFileDescriptor(fileInputStream.getFD(), null, options);
                        Utils.closeSilently(fileInputStream);
                    } catch (FileNotFoundException e2) {
                        closeable = fileInputStream;
                        GalleryLog.d("BoostFullScreenNailDisplay", "Engine decode drm file error, file not found, file path:" + filePath);
                        Utils.closeSilently(closeable);
                        tImageWidth = options.outWidth;
                        tImageHeight = options.outHeight;
                        if (tImageWidth > 0) {
                        }
                        return null;
                    } catch (IOException e3) {
                        e = e3;
                        closeable = fileInputStream;
                        try {
                            GalleryLog.d("BoostFullScreenNailDisplay", "Engine decode drm file error, IO exception, message:" + e.getMessage() + ". file path:" + filePath);
                            Utils.closeSilently(closeable);
                            tImageWidth = options.outWidth;
                            tImageHeight = options.outHeight;
                            if (tImageWidth > 0) {
                            }
                            return null;
                        } catch (Throwable th2) {
                            th = th2;
                            Utils.closeSilently(closeable);
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        closeable = fileInputStream;
                        Utils.closeSilently(closeable);
                        throw th;
                    }
                } catch (FileNotFoundException e4) {
                    GalleryLog.d("BoostFullScreenNailDisplay", "Engine decode drm file error, file not found, file path:" + filePath);
                    Utils.closeSilently(closeable);
                    tImageWidth = options.outWidth;
                    tImageHeight = options.outHeight;
                    if (tImageWidth > 0) {
                    }
                    return null;
                } catch (IOException e5) {
                    e = e5;
                    GalleryLog.d("BoostFullScreenNailDisplay", "Engine decode drm file error, IO exception, message:" + e.getMessage() + ". file path:" + filePath);
                    Utils.closeSilently(closeable);
                    tImageWidth = options.outWidth;
                    tImageHeight = options.outHeight;
                    if (tImageWidth > 0) {
                    }
                    return null;
                }
            }
            BitmapFactory.decodeFile(filePath, options);
            tImageWidth = options.outWidth;
            tImageHeight = options.outHeight;
            if (tImageWidth > 0 || tImageHeight <= 0) {
                return null;
            }
            float tScaleFullImg2Dst = DecodeUtils.getFullScreenNailScale(tImageWidth, tImageHeight);
            tLevel = Math.max(0, Utils.floorLog2(WMElement.CAMERASIZEVALUE1B1 / tScaleFullImg2Dst));
            tScaleLevelDownScaleImg2Dst = ((float) (1 << tLevel)) * tScaleFullImg2Dst;
            ScreenNailScaleDisplayEngineCreateThread screenNailScaleDisplayEngineCreateThread = new ScreenNailScaleDisplayEngineCreateThread(tImageWidth / (1 << tLevel), tImageHeight / (1 << tLevel), tScaleLevelDownScaleImg2Dst);
            screenNailScaleDisplayEngineCreateThread.start();
            TraceController.traceEnd();
        }
        Bitmap bitmap = (Bitmap) mediaItem.requestImage(16).run(jc);
        if (!(DrmUtils.isDrmFile(mediaItem.getFilePath()) ? DrmUtils.haveCountConstraints(mediaItem.getFilePath(), 7) : false)) {
            mediaItem.requestImage(1).run(jc);
        }
        if (DisplayEngineUtils.isDisplayEngineEnable()) {
            ScreenNailCommonDisplayEngine commonDisplayEngine = DisplayEngineUtils.obtainScreenNailCommon(bitmap, mediaItem, displayEnginePool);
            if (commonDisplayEngine != null) {
                displayEnginePool.add(mediaItem, commonDisplayEngine);
                DisplayEngineUtils.updateEffectImageReview(mediaItem, commonDisplayEngine);
            }
            if (tSnScaleCreateThread != null) {
                try {
                    tSnScaleCreateThread.join();
                } catch (InterruptedException e6) {
                }
                ScreenNailScaleDisplayEngine tSnScaleDisplayEngine = tSnScaleCreateThread.getScreenNailScaleDisplayEngine();
                if (tSnScaleDisplayEngine != null && tImageWidth > 0 && tImageHeight > 0 && tScaleLevelDownScaleImg2Dst > 0.0f) {
                    TraceController.traceBegin("BoostFullScreenNailDisplay.generateFullScreenNail processScreenNailScale");
                    bitmap = DisplayEngineUtils.processScreenNailScale(bitmap, mediaItem, displayEnginePool, tImageWidth, tImageHeight, tLevel, tScaleLevelDownScaleImg2Dst, tSnScaleDisplayEngine);
                    TraceController.traceEnd();
                    tSnScaleDisplayEngine.destroy();
                }
            }
            bitmap = DisplayEngineUtils.processScreenNailACE(bitmap, mediaItem, displayEnginePool);
        }
        return bitmap;
    }

    private static void waitGenerateFullScreenNailThreadFinish() {
        if (sGenerateFullScreenNailThread != null) {
            try {
                sGenerateFullScreenNailThread.join();
            } catch (InterruptedException e) {
            }
        }
        sGenerateFullScreenNailThread = null;
    }

    private static ScreenNailCommonDisplayEngine getPreparedFullScreenNailCommon(MediaItem mediaItem) {
        waitGenerateFullScreenNailThreadFinish();
        if (mediaItem == null || mediaItem != sItem) {
            return null;
        }
        return sCommonDisplayEnginePool.get(mediaItem);
    }

    public static synchronized Bitmap getPreparedFullScreenNailBitmap(JobContext jc, MediaItem mediaItem, ScreenNailCommonDisplayEnginePool displayEnginePool) {
        synchronized (BoostFullScreenNailDisplay.class) {
            if (mediaItem == sItem) {
                waitGenerateFullScreenNailThreadFinish();
                displayEnginePool.add(mediaItem, getPreparedFullScreenNailCommon(mediaItem));
                Bitmap bitmap = sFullScreenNailBitmap;
                return bitmap;
            }
            bitmap = generateFullScreenNail(jc, mediaItem, displayEnginePool);
            return bitmap;
        }
    }

    public static synchronized void preGenerateFullScreenNailAsync(JobContext jc, MediaItem mediaItem) {
        synchronized (BoostFullScreenNailDisplay.class) {
            recycleAll();
            sItem = mediaItem;
            if (sGenerateFullScreenNailThread == null) {
                TraceController.traceBegin("BoostFullScreenNailDisplay.preDecodeFullScreenNailAsync new GenerateFullScreenNailThread and start run");
                sGenerateFullScreenNailThread = new GenerateFullScreenNailThread(jc);
                sGenerateFullScreenNailThread.start();
                TraceController.traceEnd();
            }
        }
    }

    public static synchronized void recycleAll() {
        synchronized (BoostFullScreenNailDisplay.class) {
            waitGenerateFullScreenNailThreadFinish();
            sCommonDisplayEnginePool.clear();
            if (sFullScreenNailBitmap != null) {
                sFullScreenNailBitmap.recycle();
                sFullScreenNailBitmap = null;
            }
            sItem = null;
        }
    }
}
