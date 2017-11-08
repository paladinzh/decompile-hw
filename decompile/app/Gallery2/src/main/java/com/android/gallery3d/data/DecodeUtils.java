package com.android.gallery3d.data;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MediaSetUtils;
import com.android.gallery3d.util.ThreadPool.CancelListener;
import com.android.gallery3d.util.ThreadPool.JobContext;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;

public class DecodeUtils {

    private static class DecodeCanceller implements CancelListener {
        Options mOptions;

        public DecodeCanceller(Options options) {
            this.mOptions = options;
        }

        public void onCancel() {
            this.mOptions.requestCancelDecode();
        }
    }

    @TargetApi(11)
    public static void setOptionsMutable(Options options) {
        if (ApiHelper.HAS_OPTIONS_IN_MUTABLE) {
            options.inMutable = true;
        }
    }

    public static void decodeBounds(JobContext jc, FileDescriptor fd, Options options) {
        boolean z;
        if (options != null) {
            z = true;
        } else {
            z = false;
        }
        Utils.assertTrue(z);
        options.inJustDecodeBounds = true;
        jc.setCancelListener(new DecodeCanceller(options));
        BitmapFactory.decodeFileDescriptor(fd, null, options);
        options.inJustDecodeBounds = false;
    }

    public static Bitmap decode(JobContext jc, byte[] bytes, Options options) {
        return decode(jc, bytes, 0, bytes.length, options);
    }

    public static Bitmap decode(JobContext jc, byte[] bytes, int offset, int length, Options options) {
        if (options == null) {
            options = new Options();
        }
        jc.setCancelListener(new DecodeCanceller(options));
        setOptionsMutable(options);
        return ensureGLCompatibleBitmap(BitmapFactory.decodeByteArray(bytes, offset, length, options));
    }

    public static void decodeBounds(JobContext jc, byte[] bytes, int offset, int length, Options options) {
        boolean z;
        if (options != null) {
            z = true;
        } else {
            z = false;
        }
        Utils.assertTrue(z);
        options.inJustDecodeBounds = true;
        jc.setCancelListener(new DecodeCanceller(options));
        BitmapFactory.decodeByteArray(bytes, offset, length, options);
        options.inJustDecodeBounds = false;
    }

    private static String getCachePath(String sourcePath) {
        int index = sourcePath.lastIndexOf(47);
        int suffixNameIndex = sourcePath.lastIndexOf(46);
        if (index < 0 || suffixNameIndex < 0 || suffixNameIndex < index) {
            return null;
        }
        return MediaSetUtils.getCameraDir().toString() + "/cache" + sourcePath.substring(index, suffixNameIndex) + ".thumbnail";
    }

    private static String getCacheLatestPath(String sourcePath) {
        int index = sourcePath.lastIndexOf(47);
        int suffixNameIndex = sourcePath.lastIndexOf(46);
        if (index < 0 || suffixNameIndex < 0 || suffixNameIndex < index) {
            return null;
        }
        return MediaSetUtils.getCameraDir().toString() + "/cache/latest" + sourcePath.substring(index, suffixNameIndex) + ".thumbnail";
    }

    public static void deleteCacheFile(String sourcePath) {
        try {
            File cacheFile = new File(getCachePath(sourcePath));
            if (cacheFile.exists()) {
                GalleryLog.d("DecodeUtils", "deleteCacheFile result :" + cacheFile.delete());
            }
        } catch (Throwable ex) {
            GalleryLog.w("DecodeUtils", ex);
        }
    }

    public static Bitmap decodeFromCacheLatest(String path) {
        String cachePath = getCacheLatestPath(path);
        if (cachePath == null) {
            return null;
        }
        return decodeThumbnailFromCache(cachePath);
    }

    public static Bitmap decodeFromCache(String path) {
        String cachePath = getCachePath(path);
        if (cachePath == null) {
            return null;
        }
        GalleryLog.d("DecodeUtils", "decodeThumbnailFromCache " + cachePath);
        return decodeThumbnailFromCache(cachePath);
    }

    private static Bitmap decodeThumbnailFromCache(String filePath) {
        Throwable ex;
        Throwable th;
        Closeable fis = null;
        try {
            byte[] head = new byte[16];
            Closeable fis2 = new FileInputStream(filePath);
            try {
                if (fis2.read(head, 0, 16) != 16) {
                    Utils.closeSilently(fis2);
                    return null;
                }
                int width = (Utils.byteToInt(head[9]) << 8) + Utils.byteToInt(head[8]);
                int height = (Utils.byteToInt(head[13]) << 8) + Utils.byteToInt(head[12]);
                if (width > 1024 || height > 1024) {
                    Utils.closeSilently(fis2);
                    return null;
                }
                int dataLen = (width * height) * 4;
                byte[] data = new byte[dataLen];
                if (fis2.read(data, 0, dataLen) != dataLen) {
                    Utils.closeSilently(fis2);
                    return null;
                }
                Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
                if (bitmap == null) {
                    Utils.closeSilently(fis2);
                    return null;
                }
                bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(data, 0, dataLen));
                Utils.closeSilently(fis2);
                return bitmap;
            } catch (Exception e) {
                ex = e;
                fis = fis2;
                try {
                    GalleryLog.w("DecodeUtils", ex);
                    Utils.closeSilently(fis);
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    Utils.closeSilently(fis);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fis = fis2;
                Utils.closeSilently(fis);
                throw th;
            }
        } catch (Exception e2) {
            ex = e2;
            GalleryLog.w("DecodeUtils", ex);
            Utils.closeSilently(fis);
            return null;
        }
    }

    public static Bitmap decodeThumbnail(JobContext jc, String filePath, Options options, int targetSize, int type) {
        Throwable e;
        Throwable ex;
        Throwable th;
        if (filePath == null) {
            return null;
        }
        File tempFile = new File(filePath);
        if (!tempFile.exists() || tempFile.length() <= 0) {
            GalleryLog.i("DecodeUtils", "decodeThumbnail filePath not exist : " + filePath);
            return null;
        }
        Closeable closeable = null;
        try {
            Closeable fis = new FileInputStream(filePath);
            try {
                Bitmap decodeThumbnail = decodeThumbnail(jc, fis.getFD(), options, targetSize, type);
                Utils.closeSilently(fis);
                return decodeThumbnail;
            } catch (FileNotFoundException e2) {
                e = e2;
                closeable = fis;
                GalleryLog.w("DecodeUtils", e);
                Utils.closeSilently(closeable);
                return null;
            } catch (Exception e3) {
                ex = e3;
                closeable = fis;
                try {
                    GalleryLog.w("DecodeUtils", ex);
                    Utils.closeSilently(closeable);
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    Utils.closeSilently(closeable);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                closeable = fis;
                Utils.closeSilently(closeable);
                throw th;
            }
        } catch (FileNotFoundException e4) {
            e = e4;
            GalleryLog.w("DecodeUtils", e);
            Utils.closeSilently(closeable);
            return null;
        } catch (Exception e5) {
            ex = e5;
            GalleryLog.w("DecodeUtils", ex);
            Utils.closeSilently(closeable);
            return null;
        }
    }

    public static Bitmap decodeRectThumbnail(JobContext jc, String filePath, Options options, int targetWidthSize, int targetHeightSize) {
        Throwable e;
        Throwable ex;
        Throwable th;
        Closeable closeable = null;
        try {
            Closeable fis = new FileInputStream(filePath);
            try {
                Bitmap decodeRectThumbnail = decodeRectThumbnail(jc, fis.getFD(), options, targetWidthSize, targetHeightSize);
                Utils.closeSilently(fis);
                return decodeRectThumbnail;
            } catch (FileNotFoundException e2) {
                e = e2;
                closeable = fis;
                GalleryLog.w("DecodeUtils", e);
                Utils.closeSilently(closeable);
                return null;
            } catch (Exception e3) {
                ex = e3;
                closeable = fis;
                try {
                    GalleryLog.w("DecodeUtils", ex);
                    Utils.closeSilently(closeable);
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    Utils.closeSilently(closeable);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                closeable = fis;
                Utils.closeSilently(closeable);
                throw th;
            }
        } catch (FileNotFoundException e4) {
            e = e4;
            GalleryLog.w("DecodeUtils", e);
            Utils.closeSilently(closeable);
            return null;
        } catch (Exception e5) {
            ex = e5;
            GalleryLog.w("DecodeUtils", ex);
            Utils.closeSilently(closeable);
            return null;
        }
    }

    public static float getFullScreenNailScale(int imgWidth, int imgHeight) {
        float screenWidth = (float) GalleryUtils.getWidthPixels();
        float screenHeight = (float) GalleryUtils.getHeightPixels();
        GalleryLog.d("DecodeUtils", String.format("getFullScreenNailScale screenWidth = %f, screenHeight = %f, imgWidth = %d, imgHeight = %d, scale = %f", new Object[]{Float.valueOf(screenWidth), Float.valueOf(screenHeight), Integer.valueOf(imgWidth), Integer.valueOf(imgHeight), Float.valueOf(Math.max(Math.min(screenWidth / ((float) imgWidth), screenHeight / ((float) imgHeight)), Math.min(screenWidth / ((float) imgHeight), screenHeight / ((float) imgWidth))))}));
        return Math.max(Math.min(screenWidth / ((float) imgWidth), screenHeight / ((float) imgHeight)), Math.min(screenWidth / ((float) imgHeight), screenHeight / ((float) imgWidth)));
    }

    public static Bitmap decodeThumbnail(JobContext jc, FileDescriptor fd, Options options, int targetSize, int type) {
        if (options == null) {
            options = new Options();
        }
        jc.setCancelListener(new DecodeCanceller(options));
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, null, options);
        if (jc.isCancelled()) {
            return null;
        }
        float scale;
        int w = options.outWidth;
        int h = options.outHeight;
        if (type == 2) {
            options.inSampleSize = BitmapUtils.computeSampleSizeLarger(((float) targetSize) / ((float) Math.min(w, h)));
            if ((w / options.inSampleSize) * (h / options.inSampleSize) > 640000) {
                options.inSampleSize = BitmapUtils.computeSampleSize((float) Math.sqrt(640000.0d / ((double) (w * h))));
            }
        } else {
            scale = ((float) targetSize) / ((float) Math.max(w, h));
            if (targetSize == MediaItem.getTargetSize(16)) {
                scale = getFullScreenNailScale(w, h);
            }
            options.inSampleSize = BitmapUtils.computeSampleSizeLarger(scale);
        }
        options.inJustDecodeBounds = false;
        setOptionsMutable(options);
        Bitmap result = BitmapFactory.decodeFileDescriptor(fd, null, options);
        if (result == null) {
            return null;
        }
        int min;
        float f = (float) targetSize;
        if (type == 2) {
            min = Math.min(result.getWidth(), result.getHeight());
        } else {
            min = Math.max(result.getWidth(), result.getHeight());
        }
        scale = f / ((float) min);
        if (((double) scale) <= 0.5d) {
            result = BitmapUtils.resizeBitmapByScale(result, scale, true);
        }
        return ensureGLCompatibleBitmap(result);
    }

    public static Bitmap decodeRectThumbnail(JobContext jc, FileDescriptor fd, Options options, int targetWidthSize, int targetHeightSize) {
        if (options == null) {
            options = new Options();
        }
        jc.setCancelListener(new DecodeCanceller(options));
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, null, options);
        if (jc.isCancelled()) {
            return null;
        }
        int w = options.outWidth;
        int h = options.outHeight;
        options.inSampleSize = BitmapUtils.computeSampleSizeLarger(Math.max(((float) targetWidthSize) / ((float) w), ((float) targetHeightSize) / ((float) h)));
        if ((w / options.inSampleSize) * (h / options.inSampleSize) > 640000) {
            options.inSampleSize = BitmapUtils.computeSampleSize((float) Math.sqrt(640000.0d / ((double) (w * h))));
        }
        options.inJustDecodeBounds = false;
        setOptionsMutable(options);
        Bitmap result = BitmapFactory.decodeFileDescriptor(fd, null, options);
        if (result == null) {
            return null;
        }
        float scale = Math.max(((float) targetWidthSize) / ((float) result.getWidth()), ((float) targetHeightSize) / ((float) result.getHeight()));
        if (((double) scale) <= 0.5d) {
            result = BitmapUtils.resizeBitmapByScale(result, scale, true);
        }
        return ensureGLCompatibleBitmap(result);
    }

    public static Bitmap decodeIfBigEnough(JobContext jc, byte[] data, Options options, int targetSize) {
        if (options == null) {
            options = new Options();
        }
        jc.setCancelListener(new DecodeCanceller(options));
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        if (jc.isCancelled() || options.outWidth < targetSize || options.outHeight < targetSize) {
            return null;
        }
        options.inSampleSize = BitmapUtils.computeSampleSizeShorter(options.outWidth, options.outHeight, targetSize);
        options.inJustDecodeBounds = false;
        setOptionsMutable(options);
        return ensureGLCompatibleBitmap(BitmapFactory.decodeByteArray(data, 0, data.length, options));
    }

    public static Bitmap ensureGLCompatibleBitmap(Bitmap bitmap) {
        if (bitmap == null || bitmap.getConfig() != null) {
            return bitmap;
        }
        Bitmap newBitmap = bitmap.copy(Config.ARGB_8888, false);
        GalleryLog.w("DecodeUtils", String.format("bitmap(%s) will be recycled[mWidth=%d, mHeight=%d]", new Object[]{bitmap, Integer.valueOf(bitmap.getWidth()), Integer.valueOf(bitmap.getHeight())}));
        bitmap.recycle();
        return newBitmap;
    }

    public static BitmapRegionDecoder createBitmapRegionDecoder(JobContext jc, byte[] bytes, int offset, int length, boolean shareable) {
        if (offset < 0 || length <= 0 || offset + length > bytes.length) {
            throw new IllegalArgumentException(String.format("offset = %s, length = %s, bytes = %s", new Object[]{Integer.valueOf(offset), Integer.valueOf(length), Integer.valueOf(bytes.length)}));
        }
        try {
            return BitmapRegionDecoder.newInstance(bytes, offset, length, shareable);
        } catch (Throwable t) {
            GalleryLog.w("DecodeUtils", t);
            return null;
        }
    }

    public static BitmapRegionDecoder createBitmapRegionDecoder(JobContext jc, String filePath, boolean shareable) {
        if (filePath == null) {
            return null;
        }
        File tempFile = new File(filePath);
        if (!tempFile.exists() || tempFile.length() <= 0) {
            GalleryLog.i("DecodeUtils", "createBitmapRegionDecoder filePath not exist : " + filePath);
            return null;
        }
        try {
            return BitmapRegionDecoder.newInstance(filePath, shareable);
        } catch (Throwable t) {
            GalleryLog.w("DecodeUtils", t);
            return null;
        }
    }

    public static BitmapRegionDecoder createBitmapRegionDecoder(JobContext jc, FileDescriptor fd, boolean shareable) {
        try {
            return BitmapRegionDecoder.newInstance(fd, shareable);
        } catch (Throwable t) {
            GalleryLog.w("DecodeUtils", t);
            return null;
        }
    }

    @TargetApi(11)
    public static Bitmap decode(JobContext jc, byte[] data, int offset, int length, Options options, BitmapPool pool) {
        if (pool == null) {
            return decode(jc, data, offset, length, options);
        }
        Bitmap findCachedBitmap;
        if (options == null) {
            options = new Options();
        }
        if (options.inSampleSize < 1) {
            options.inSampleSize = 1;
        }
        options.inPreferredConfig = Config.ARGB_8888;
        if (options.inSampleSize == 1) {
            findCachedBitmap = findCachedBitmap(pool, jc, data, offset, length, options);
        } else {
            findCachedBitmap = null;
        }
        options.inBitmap = findCachedBitmap;
        try {
            Bitmap bitmap = decode(jc, data, offset, length, options);
            if (!(options.inBitmap == null || options.inBitmap == bitmap)) {
                pool.recycle(options.inBitmap);
                options.inBitmap = null;
            }
            return bitmap;
        } catch (IllegalArgumentException e) {
            if (options.inBitmap == null) {
                throw e;
            }
            GalleryLog.w("DecodeUtils", "decode fail with a given bitmap, try decode to a new bitmap");
            pool.recycle(options.inBitmap);
            options.inBitmap = null;
            return decode(jc, data, offset, length, options);
        }
    }

    private static Bitmap findCachedBitmap(BitmapPool pool, JobContext jc, byte[] data, int offset, int length, Options options) {
        if (pool.isOneSize()) {
            return pool.getBitmap();
        }
        decodeBounds(jc, data, offset, length, options);
        return pool.getBitmap(options.outWidth, options.outHeight);
    }

    public static Bitmap findCachedBitmap(BitmapPool pool, JobContext jc, FileDescriptor fileDescriptor, Options options) {
        if (pool.isOneSize()) {
            return pool.getBitmap();
        }
        decodeBounds(jc, fileDescriptor, options);
        return pool.getBitmap(options.outWidth, options.outHeight);
    }
}
