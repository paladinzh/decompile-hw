package com.android.gallery3d.common;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.os.Build.VERSION;
import com.amap.api.maps.model.WeightedLatLng;
import com.android.gallery3d.settings.HicloudAccountReceiver;
import com.android.gallery3d.util.BusinessRadar.BugType;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ThumbnailReporter;
import com.huawei.watermark.manager.parse.WMElement;
import java.io.ByteArrayOutputStream;
import java.util.Locale;

public class BitmapUtils {
    private BitmapUtils() {
    }

    public static int computeSampleSize(int width, int height, int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(width, height, minSideLength, maxNumOfPixels);
        if (initialSize <= 8) {
            return Utils.nextPowerOf2(initialSize);
        }
        return ((initialSize + 7) / 8) * 8;
    }

    private static int computeInitialSampleSize(int w, int h, int minSideLength, int maxNumOfPixels) {
        if (maxNumOfPixels == -1 && minSideLength == -1) {
            return 1;
        }
        int lowerBound;
        if (maxNumOfPixels == -1) {
            lowerBound = 1;
        } else {
            lowerBound = (int) Math.ceil(Math.sqrt(((double) (w * h)) / ((double) maxNumOfPixels)));
        }
        if (minSideLength == -1) {
            return lowerBound;
        }
        return Math.max(Math.min(w / minSideLength, h / minSideLength), lowerBound);
    }

    public static int computeSampleSizeShorter(int w, int h, int minSideLength) {
        int initialSize = Math.min(w / minSideLength, h / minSideLength);
        if (initialSize <= 1) {
            return 1;
        }
        int prevPowerOf2;
        if (initialSize <= 8) {
            prevPowerOf2 = Utils.prevPowerOf2(initialSize);
        } else {
            prevPowerOf2 = (initialSize / 8) * 8;
        }
        return prevPowerOf2;
    }

    public static int computeSampleSizeLarger(float scale) {
        int initialSize = (int) Math.floor(WeightedLatLng.DEFAULT_INTENSITY / ((double) scale));
        if (initialSize <= 1) {
            return 1;
        }
        int prevPowerOf2;
        if (initialSize <= 8) {
            prevPowerOf2 = Utils.prevPowerOf2(initialSize);
        } else {
            prevPowerOf2 = (initialSize / 8) * 8;
        }
        return prevPowerOf2;
    }

    public static int computeSampleSize(float scale) {
        Utils.assertTrue(scale > 0.0f);
        int initialSize = Math.max(1, (int) Math.ceil((double) (WMElement.CAMERASIZEVALUE1B1 / scale)));
        if (initialSize <= 8) {
            return Utils.nextPowerOf2(initialSize);
        }
        return ((initialSize + 7) / 8) * 8;
    }

    public static Bitmap resizeBitmapByScale(Bitmap bitmap, float scale, boolean recycle) {
        int width = Math.round(((float) bitmap.getWidth()) * scale);
        int height = Math.round(((float) bitmap.getHeight()) * scale);
        if (width == bitmap.getWidth() && height == bitmap.getHeight()) {
            return bitmap;
        }
        Bitmap target = Bitmap.createBitmap(width, height, getConfig(bitmap));
        Canvas canvas = new Canvas(target);
        canvas.scale(scale, scale);
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, new Paint(6));
        if (recycle) {
            bitmap.recycle();
        }
        return target;
    }

    private static Config getConfig(Bitmap bitmap) {
        Config config = bitmap.getConfig();
        if (config == null) {
            return Config.ARGB_8888;
        }
        return config;
    }

    public static Bitmap resizeDownBySideLength(Bitmap bitmap, int maxLength, boolean recycle) {
        float scale = Math.min(((float) maxLength) / ((float) bitmap.getWidth()), ((float) maxLength) / ((float) bitmap.getHeight()));
        if (scale >= WMElement.CAMERASIZEVALUE1B1) {
            return bitmap;
        }
        return resizeBitmapByScale(bitmap, scale, recycle);
    }

    public static Bitmap resizeAndCropCenter(Bitmap bitmap, int size, boolean recycle) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w == size && h == size) {
            return bitmap;
        }
        float scale = ((float) size) / ((float) Math.min(w, h));
        Bitmap target = Bitmap.createBitmap(size, size, getConfig(bitmap));
        int width = Math.round(((float) bitmap.getWidth()) * scale);
        int height = Math.round(((float) bitmap.getHeight()) * scale);
        Canvas canvas = new Canvas(target);
        canvas.translate(((float) (size - width)) / 2.0f, ((float) (size - height)) / 2.0f);
        canvas.scale(scale, scale);
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, new Paint(6));
        if (recycle) {
            bitmap.recycle();
        }
        return target;
    }

    public static Bitmap resizeAndCropCenter(Bitmap bitmap, int targetWidth, int targetHeight, boolean recycle) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w == targetWidth && h == targetHeight) {
            return bitmap;
        }
        float scale = Math.max(((float) targetWidth) / ((float) w), ((float) targetHeight) / ((float) h));
        Bitmap target = Bitmap.createBitmap(targetWidth, targetHeight, getConfig(bitmap));
        int width = Math.round(((float) bitmap.getWidth()) * scale);
        int height = Math.round(((float) bitmap.getHeight()) * scale);
        Canvas canvas = new Canvas(target);
        canvas.translate(((float) (targetWidth - width)) / 2.0f, ((float) (targetHeight - height)) / 2.0f);
        canvas.scale(scale, scale);
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, new Paint(6));
        if (recycle) {
            bitmap.recycle();
        }
        return target;
    }

    public static Bitmap rotateBitmap(Bitmap source, int rotation, boolean recycle) {
        if (rotation == 0 || source == null) {
            return source;
        }
        int w = source.getWidth();
        int h = source.getHeight();
        Matrix m = new Matrix();
        m.postRotate((float) rotation);
        Bitmap bitmap = Bitmap.createBitmap(source, 0, 0, w, h, m, true);
        if (recycle) {
            source.recycle();
        }
        return bitmap;
    }

    public static Bitmap createVideoThumbnail(String filePath) {
        Class cls = null;
        Object obj = null;
        Exception exception = null;
        try {
            cls = Class.forName("android.media.MediaMetadataRetriever");
            obj = cls.newInstance();
            cls.getMethod("setDataSource", new Class[]{String.class}).invoke(obj, new Object[]{filePath});
            Bitmap bitmap;
            if (VERSION.SDK_INT <= 9) {
                bitmap = (Bitmap) cls.getMethod("captureFrame", new Class[0]).invoke(obj, new Object[0]);
                if (obj != null) {
                    try {
                        cls.getMethod("release", new Class[0]).invoke(obj, new Object[0]);
                    } catch (Exception e) {
                    }
                }
                return bitmap;
            }
            byte[] data = (byte[]) cls.getMethod("getEmbeddedPicture", new Class[0]).invoke(obj, new Object[0]);
            if (data != null) {
                Bitmap bitmap2 = BitmapFactory.decodeByteArray(data, 0, data.length);
                if (bitmap2 != null) {
                    if (obj != null) {
                        try {
                            cls.getMethod("release", new Class[0]).invoke(obj, new Object[0]);
                        } catch (Exception e2) {
                        }
                    }
                    return bitmap2;
                }
            }
            int option = ApiHelper.META_DATA_RETRIEVER_ARGB8888_OPTION;
            if (option == Integer.MIN_VALUE) {
                bitmap = (Bitmap) cls.getMethod("getFrameAtTime", new Class[0]).invoke(obj, new Object[0]);
                if (obj != null) {
                    try {
                        cls.getMethod("release", new Class[0]).invoke(obj, new Object[0]);
                    } catch (Exception e3) {
                    }
                }
                return bitmap;
            }
            GalleryLog.d("BitmapUtils", "getFrameAtTime option code:" + option);
            bitmap = (Bitmap) cls.getMethod("getFrameAtTime", new Class[]{Long.TYPE, Integer.TYPE}).invoke(obj, new Object[]{Integer.valueOf(-1), Integer.valueOf(option)});
            if (obj != null) {
                try {
                    cls.getMethod("release", new Class[0]).invoke(obj, new Object[0]);
                } catch (Exception e4) {
                }
            }
            return bitmap;
        } catch (Exception ex) {
            exception = ex;
            if (ex != null) {
                ThumbnailReporter.reportThumbnailFail(BugType.DECODE_THUMB_FAILED_VIDEO, filePath, ex);
            }
            if (obj != null) {
                try {
                    cls.getMethod("release", new Class[0]).invoke(obj, new Object[0]);
                } catch (Exception e5) {
                }
            }
        } catch (Exception ex2) {
            exception = ex2;
            if (ex2 != null) {
                ThumbnailReporter.reportThumbnailFail(BugType.DECODE_THUMB_FAILED_VIDEO, filePath, ex2);
            }
            if (obj != null) {
                try {
                    cls.getMethod("release", new Class[0]).invoke(obj, new Object[0]);
                } catch (Exception e6) {
                }
            }
        } catch (Exception e7) {
            exception = e7;
            GalleryLog.e("BitmapUtils", "createVideoThumbnail " + e7.getMessage());
            if (e7 != null) {
                ThumbnailReporter.reportThumbnailFail(BugType.DECODE_THUMB_FAILED_VIDEO, filePath, e7);
            }
            if (obj != null) {
                try {
                    cls.getMethod("release", new Class[0]).invoke(obj, new Object[0]);
                } catch (Exception e8) {
                }
            }
        } catch (Exception e9) {
            exception = e9;
            GalleryLog.e("BitmapUtils", "createVideoThumbnail " + e9.getMessage());
            if (e9 != null) {
                ThumbnailReporter.reportThumbnailFail(BugType.DECODE_THUMB_FAILED_VIDEO, filePath, e9);
            }
            if (obj != null) {
                try {
                    cls.getMethod("release", new Class[0]).invoke(obj, new Object[0]);
                } catch (Exception e10) {
                }
            }
        } catch (Exception e11) {
            exception = e11;
            GalleryLog.e("BitmapUtils", "createVideoThumbnail " + e11.getMessage());
            if (e11 != null) {
                ThumbnailReporter.reportThumbnailFail(BugType.DECODE_THUMB_FAILED_VIDEO, filePath, e11);
            }
            if (obj != null) {
                try {
                    cls.getMethod("release", new Class[0]).invoke(obj, new Object[0]);
                } catch (Exception e12) {
                }
            }
        } catch (NoSuchMethodException e13) {
            GalleryLog.e("BitmapUtils", "createVideoThumbnail " + e13.getMessage());
            if (obj != null) {
                try {
                    cls.getMethod("release", new Class[0]).invoke(obj, new Object[0]);
                } catch (Exception e14) {
                }
            }
        } catch (Exception e15) {
            exception = e15;
            GalleryLog.e("BitmapUtils", "createVideoThumbnail " + e15.getMessage());
            if (e15 != null) {
                ThumbnailReporter.reportThumbnailFail(BugType.DECODE_THUMB_FAILED_VIDEO, filePath, e15);
            }
            if (obj != null) {
                try {
                    cls.getMethod("release", new Class[0]).invoke(obj, new Object[0]);
                } catch (Exception e16) {
                }
            }
        } catch (Throwable th) {
            if (exception != null) {
                ThumbnailReporter.reportThumbnailFail(BugType.DECODE_THUMB_FAILED_VIDEO, filePath, exception);
            }
            if (obj != null) {
                try {
                    cls.getMethod("release", new Class[0]).invoke(obj, new Object[0]);
                } catch (Exception e17) {
                }
            }
        }
        return null;
    }

    public static Bitmap findFace(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        Face[] faces = new Face[3];
        int width = bitmap.getWidth() & -2;
        int height = bitmap.getHeight() & -2;
        Bitmap bmp = Bitmap.createBitmap(width, height, Config.RGB_565);
        new Canvas(bmp).drawBitmap(bitmap, 0.0f, 0.0f, new Paint(2));
        GalleryLog.d("BitmapUtils", String.format("face thumbnail(%sx%s) count %s", new Object[]{Integer.valueOf(width), Integer.valueOf(height), Integer.valueOf(new FaceDetector(width, height, 3).findFaces(bmp, faces))}));
        if (new FaceDetector(width, height, 3).findFaces(bmp, faces) <= 0) {
            return null;
        }
        Face face = faces[0];
        PointF midle = new PointF();
        face.getMidPoint(midle);
        int r = Math.min(width, height) / 2;
        int cx = (int) midle.x;
        int cy = (int) midle.y;
        Rect faceRect = new Rect(cx - r, cy - r, cx + r, cy + r);
        faceRect.intersect(0, 0, width, height);
        GalleryLog.d("BitmapUtils", String.format("middle point(%sx%s), face Rect: %s", new Object[]{Integer.valueOf(cx), Integer.valueOf(cy), faceRect}));
        return Bitmap.createBitmap(bmp, faceRect.left, faceRect.top, faceRect.width(), faceRect.height());
    }

    public static byte[] compressToBytes(Bitmap bitmap) {
        return compressToBytes(bitmap, 90);
    }

    public static byte[] compressToBytes(Bitmap bitmap, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(HicloudAccountReceiver.MSG_HICLOUD_ACCOUNT_LOGOUT);
        bitmap.compress(CompressFormat.JPEG, quality, baos);
        return baos.toByteArray();
    }

    public static boolean isSupportedByRegionDecoder(String mimeType) {
        boolean z = false;
        if (mimeType == null) {
            return false;
        }
        mimeType = mimeType.toLowerCase();
        if (!(!mimeType.startsWith("image/") || mimeType.equals("image/gif") || mimeType.endsWith("bmp"))) {
            z = true;
        }
        return z;
    }

    public static boolean isRotationSupported(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        boolean z;
        mimeType = mimeType.toLowerCase();
        if (mimeType.equals("image/jpeg") || mimeType.equals("image/bmp") || mimeType.equals("image/png") || mimeType.equals("image/x-ms-bmp") || mimeType.equals("image/wbmp")) {
            z = true;
        } else {
            z = mimeType.equals("image/vnd.wap.wbmp");
        }
        return z;
    }

    public static boolean isFilterShowSupported(String mimeType) {
        boolean z = false;
        if (mimeType == null) {
            return false;
        }
        mimeType = mimeType.toLowerCase(Locale.US);
        if (!("image/gif".equals(mimeType) || "image/vnd.wap.wbmp".equals(mimeType) || "image/wbmp".equals(mimeType))) {
            z = true;
        }
        return z;
    }

    public static boolean isRectifySupported(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        boolean z;
        mimeType = mimeType.toLowerCase(Locale.US);
        if (mimeType.equals("image/jpeg")) {
            z = true;
        } else {
            z = mimeType.equals("image/png");
        }
        return z;
    }
}
