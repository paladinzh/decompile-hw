package com.huawei.gallery.photorectify;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.provider.MediaStore.Images.Media;
import android.text.TextUtils;
import com.android.gallery3d.R;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.exif.ExifInterface;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.media.GalleryMedia;
import com.huawei.gallery.photoshare.utils.MD5Utils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class RectifyUtils {
    private static String TAG = "RectifyUtils";
    private static boolean sIsRectifyNativeSupport;
    private File mCacheDir;
    private long mDateTakenInMs;
    private RandomAccessFile mFile;
    private String mFilePath;
    private RectifyImageListener mListener;
    private String mOriginTmpFilePath;
    private int mRectifyOffset;

    public interface RectifyImageListener {
        void prepareCompleted();

        void refreshImage(byte[] bArr, int i, int i2);
    }

    static {
        sIsRectifyNativeSupport = false;
        try {
            System.loadLibrary("scanner");
            sIsRectifyNativeSupport = true;
        } catch (UnsatisfiedLinkError e) {
            sIsRectifyNativeSupport = false;
        }
    }

    public RectifyUtils(Context context, MediaItem mediaItem, RectifyImageListener listener) {
        this.mFilePath = mediaItem.getFilePath();
        this.mRectifyOffset = mediaItem.getRectifyOffset();
        this.mDateTakenInMs = mediaItem.getDateInMs();
        this.mListener = listener;
        this.mCacheDir = ensureExternalCacheDirByItem(context, mediaItem);
    }

    private boolean openFile() {
        try {
            if (this.mFile != null) {
                closeFile();
            }
            this.mFile = new RandomAccessFile(this.mFilePath, "rws");
        } catch (FileNotFoundException e) {
            GalleryLog.i(TAG, "new RandomAccessFile() failed in openFile() method.");
        }
        return this.mFile != null;
    }

    private void closeFile() {
        if (this.mFile != null) {
            try {
                this.mFile.close();
                this.mFile = null;
            } catch (IOException e) {
                GalleryLog.i(TAG, "RandomAccessFile.close() failed in closeFile() method.");
            }
        }
    }

    public boolean prepare() {
        if (openFile()) {
            try {
                if (this.mRectifyOffset <= 0) {
                    this.mListener.refreshImage(null, 0, 0);
                    this.mListener.prepareCompleted();
                    return false;
                }
                this.mFile.seek((long) ((((int) this.mFile.length()) - this.mRectifyOffset) - 20));
                byte[] imageData = new byte[this.mRectifyOffset];
                this.mFile.readFully(imageData);
                if (simplyCheckIsJpegOrPngImage(imageData)) {
                    this.mListener.refreshImage(imageData, 0, imageData.length);
                    this.mOriginTmpFilePath = saveOriginTmpFile(imageData);
                    this.mListener.prepareCompleted();
                    closeFile();
                    return true;
                }
                closeFile();
                return false;
            } catch (IOException e) {
                GalleryLog.i(TAG, "prepare() failed, reason: IOException." + e.getMessage());
            } catch (Exception e2) {
                GalleryLog.i(TAG, "prepare() failed." + e2.getMessage());
            } finally {
                closeFile();
            }
        }
        return false;
    }

    private String saveOriginTmpFile(byte[] originData) {
        Throwable th;
        if (originData == null || this.mCacheDir == null) {
            return null;
        }
        Closeable closeable = null;
        try {
            File tmpFile = new File(this.mCacheDir.getAbsolutePath(), "origin.tmp");
            if (!tmpFile.createNewFile()) {
                GalleryLog.w(TAG, "create tmpFile fail...tmp file deleted success" + (tmpFile.delete() ? "success" : "fail"));
            }
            Closeable tmpOs = new BufferedOutputStream(new FileOutputStream(tmpFile));
            try {
                tmpOs.write(originData);
                tmpOs.flush();
                String absolutePath = tmpFile.getAbsolutePath();
                Utils.closeSilently(tmpOs);
                return absolutePath;
            } catch (FileNotFoundException e) {
                closeable = tmpOs;
                GalleryLog.i(TAG, "new FileOutputStream() failed in saveOriginTmpFile, reason: FileNotFoundException.");
                Utils.closeSilently(closeable);
                return null;
            } catch (IOException e2) {
                closeable = tmpOs;
                try {
                    GalleryLog.i(TAG, "saveOriginTmpFile() failed, reason: FileNotFoundException.");
                    Utils.closeSilently(closeable);
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    Utils.closeSilently(closeable);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                closeable = tmpOs;
                Utils.closeSilently(closeable);
                throw th;
            }
        } catch (FileNotFoundException e3) {
            GalleryLog.i(TAG, "new FileOutputStream() failed in saveOriginTmpFile, reason: FileNotFoundException.");
            Utils.closeSilently(closeable);
            return null;
        } catch (IOException e4) {
            GalleryLog.i(TAG, "saveOriginTmpFile() failed, reason: FileNotFoundException.");
            Utils.closeSilently(closeable);
            return null;
        }
    }

    public int getRectifyOffset() {
        return this.mRectifyOffset;
    }

    public String getOriginTmpFilePath() {
        return this.mOriginTmpFilePath;
    }

    public String getTrimTmpFilePath() {
        if (this.mCacheDir != null) {
            return this.mCacheDir.getAbsolutePath() + File.separator + "trim.tmp";
        }
        return null;
    }

    public void deleteTmpFile() {
        if (!(this.mOriginTmpFilePath == null || new File(this.mOriginTmpFilePath).delete())) {
            GalleryLog.w(TAG, "origin tmp file delete fail.");
        }
        String trimPath = getTrimTmpFilePath();
        if (trimPath != null && !new File(trimPath).delete()) {
            GalleryLog.w(TAG, "trim tmp file delete fail.");
        }
    }

    private static String createRectifyTAG(int length) {
        String tag = "RECTIFY_" + Integer.toString(length);
        StringBuffer buffer = new StringBuffer(20);
        buffer.append(tag);
        int left = 20 - buffer.length();
        for (int i = 0; i < left; i++) {
            buffer.append(' ');
        }
        return buffer.toString();
    }

    private static void updateRectifyImage(Context context, String targetPath, String rectifyPath, int offset, long dateTaken) {
        File jpegFile = new File(targetPath);
        String whereClause = "_data = ?";
        int[] widthAndHeight = getImageSizeBound(rectifyPath);
        ContentValues mediaValues = new ContentValues();
        mediaValues.put("_size", Long.valueOf(jpegFile.length()));
        mediaValues.put("date_modified", Long.valueOf(jpegFile.lastModified() / 1000));
        mediaValues.put("datetaken", Long.valueOf(dateTaken));
        mediaValues.put("hw_rectify_offset", Integer.valueOf(offset));
        mediaValues.put("width", Integer.valueOf(widthAndHeight[0]));
        mediaValues.put("height", Integer.valueOf(widthAndHeight[1]));
        ContentValues galleryValues = new ContentValues();
        galleryValues.put("_size", Long.valueOf(jpegFile.length()));
        galleryValues.put("date_modified", Long.valueOf(jpegFile.lastModified() / 1000));
        galleryValues.put("showDateToken", Long.valueOf(dateTaken));
        galleryValues.put("datetaken", Long.valueOf(dateTaken));
        galleryValues.put("hw_rectify_offset", Integer.valueOf(offset));
        galleryValues.put("width", Integer.valueOf(widthAndHeight[0]));
        galleryValues.put("height", Integer.valueOf(widthAndHeight[1]));
        galleryValues.put("hash", MD5Utils.getMD5(jpegFile));
        try {
            context.getContentResolver().update(GalleryMedia.URI, galleryValues, whereClause, new String[]{targetPath});
            context.getContentResolver().update(Media.EXTERNAL_CONTENT_URI, mediaValues, whereClause, new String[]{targetPath});
        } catch (SecurityException e) {
            GalleryLog.noPermissionForMediaProviderLog(TAG);
        }
    }

    protected static boolean simplyCheckIsJpegOrPngImage(byte[] imageData) {
        if (imageData != null && imageData.length >= 4) {
            return (imageData[0] == (byte) -1 && imageData[1] == (byte) -40) || (imageData[0] == (byte) -119 && imageData[1] == (byte) 80);
        } else {
            return false;
        }
    }

    public static int[] getImageSizeBound(String pathName) {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;
        BitmapFactory.decodeFile(pathName, options);
        int[] wh = new int[2];
        if (options.mCancel || options.outWidth == -1 || options.outHeight == -1) {
            GalleryLog.d(TAG, "getImageBound error " + pathName);
        } else {
            wh[0] = options.outWidth;
            wh[1] = options.outHeight;
        }
        return wh;
    }

    public static float[] getScanBoundF(int[] size, int[] borders) {
        if (size == null) {
            return null;
        }
        if (borders == null) {
            GalleryLog.d(TAG, "did not found bound");
            return new float[]{0.0f, 0.0f, (float) size[0], 0.0f, (float) size[0], (float) size[1], 0.0f, (float) size[1]};
        }
        float[] bound = new float[8];
        for (int j = 0; j < bound.length; j++) {
            bound[j] = (float) borders[j];
        }
        for (int i = 0; i < 4; i++) {
            if (bound[i * 2] < 0.0f) {
                bound[i * 2] = 0.0f;
            }
            if (bound[(i * 2) + 1] < 0.0f) {
                bound[(i * 2) + 1] = 0.0f;
            }
            if (bound[i * 2] > ((float) size[0])) {
                bound[i * 2] = (float) size[0];
            }
            if (bound[(i * 2) + 1] > ((float) size[1])) {
                bound[(i * 2) + 1] = (float) size[1];
            }
        }
        return bound;
    }

    public void processImageData(Context context, String mimeType, String sourcePath, String rectifyPath, String targetPath) {
        IOException ioe;
        Exception e;
        Throwable th;
        Closeable rectifyBis;
        if (TextUtils.isEmpty(sourcePath) || TextUtils.isEmpty(rectifyPath) || TextUtils.isEmpty(targetPath) || this.mCacheDir == null) {
            GalleryLog.w(TAG, "cannot processImageData because of the sourcePath is null");
            return;
        }
        File file = new File(this.mCacheDir, "target.tmp");
        Closeable closeable = null;
        Closeable closeable2 = null;
        Closeable closeable3 = null;
        Closeable closeable4 = null;
        int offset = (int) new File(sourcePath).length();
        try {
            int readCount;
            byte[] buffer = new byte[1048576];
            Closeable bufferedInputStream = new BufferedInputStream(new FileInputStream(sourcePath));
            try {
                bufferedInputStream = new BufferedOutputStream(new FileOutputStream(rectifyPath, true));
                while (true) {
                    try {
                        readCount = bufferedInputStream.read(buffer);
                        if (readCount == -1) {
                            break;
                        }
                        bufferedInputStream.write(buffer, 0, readCount);
                    } catch (IOException e2) {
                        ioe = e2;
                        closeable2 = bufferedInputStream;
                        closeable = bufferedInputStream;
                    } catch (Exception e3) {
                        e = e3;
                        closeable2 = bufferedInputStream;
                        closeable = bufferedInputStream;
                    } catch (Throwable th2) {
                        th = th2;
                        closeable2 = bufferedInputStream;
                        closeable = bufferedInputStream;
                    }
                }
                bufferedInputStream.write(createRectifyTAG(offset).getBytes("ISO-8859-1"));
                bufferedInputStream.flush();
                rectifyBis = new BufferedInputStream(new FileInputStream(rectifyPath));
            } catch (IOException e4) {
                ioe = e4;
                closeable = bufferedInputStream;
                try {
                    GalleryLog.w(TAG, "Failed to processImageData." + ioe.getMessage());
                    Utils.closeSilently(closeable);
                    Utils.closeSilently(closeable2);
                    Utils.closeSilently(closeable3);
                    Utils.closeSilently(closeable4);
                } catch (Throwable th3) {
                    th = th3;
                    Utils.closeSilently(closeable);
                    Utils.closeSilently(closeable2);
                    Utils.closeSilently(closeable3);
                    Utils.closeSilently(closeable4);
                    throw th;
                }
            } catch (Exception e5) {
                e = e5;
                closeable = bufferedInputStream;
                GalleryLog.w(TAG, "Fail processImageData." + e.getMessage());
                Utils.closeSilently(closeable);
                Utils.closeSilently(closeable2);
                Utils.closeSilently(closeable3);
                Utils.closeSilently(closeable4);
            } catch (Throwable th4) {
                th = th4;
                closeable = bufferedInputStream;
                Utils.closeSilently(closeable);
                Utils.closeSilently(closeable2);
                Utils.closeSilently(closeable3);
                Utils.closeSilently(closeable4);
                throw th;
            }
            try {
                if ("image/jpeg".equals(mimeType)) {
                    int[] widthAndHeight = getImageSizeBound(rectifyPath);
                    ExifInterface exif = getExifData(targetPath, mimeType);
                    updateExifData(exif, widthAndHeight[0], widthAndHeight[1]);
                    putExifData(exif, rectifyBis, file.getPath());
                } else {
                    Object targetBos;
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
                    while (true) {
                        try {
                            readCount = rectifyBis.read(buffer);
                            if (readCount == -1) {
                                break;
                            }
                            bufferedOutputStream.write(buffer, 0, readCount);
                        } catch (IOException e6) {
                            ioe = e6;
                            targetBos = bufferedOutputStream;
                            closeable3 = rectifyBis;
                            closeable2 = bufferedInputStream;
                            closeable = bufferedInputStream;
                        } catch (Exception e7) {
                            e = e7;
                            targetBos = bufferedOutputStream;
                            closeable3 = rectifyBis;
                            closeable2 = bufferedInputStream;
                            closeable = bufferedInputStream;
                        } catch (Throwable th5) {
                            th = th5;
                            targetBos = bufferedOutputStream;
                            closeable3 = rectifyBis;
                            closeable2 = bufferedInputStream;
                            closeable = bufferedInputStream;
                        }
                    }
                    bufferedOutputStream.flush();
                    targetBos = bufferedOutputStream;
                }
                if (!file.renameTo(new File(targetPath))) {
                    GalleryLog.w(TAG, "rename tmpFile to target failed...tmp file deleted success" + (file.delete() ? "success" : "fail"));
                }
                updateRectifyImage(context, targetPath, rectifyPath, offset, this.mDateTakenInMs);
                Utils.closeSilently(bufferedInputStream);
                Utils.closeSilently(bufferedInputStream);
                Utils.closeSilently(rectifyBis);
                Utils.closeSilently(closeable4);
                closeable2 = bufferedInputStream;
                closeable = bufferedInputStream;
            } catch (IOException e8) {
                ioe = e8;
                closeable3 = rectifyBis;
                closeable2 = bufferedInputStream;
                closeable = bufferedInputStream;
                GalleryLog.w(TAG, "Failed to processImageData." + ioe.getMessage());
                Utils.closeSilently(closeable);
                Utils.closeSilently(closeable2);
                Utils.closeSilently(closeable3);
                Utils.closeSilently(closeable4);
            } catch (Exception e9) {
                e = e9;
                closeable3 = rectifyBis;
                closeable2 = bufferedInputStream;
                closeable = bufferedInputStream;
                GalleryLog.w(TAG, "Fail processImageData." + e.getMessage());
                Utils.closeSilently(closeable);
                Utils.closeSilently(closeable2);
                Utils.closeSilently(closeable3);
                Utils.closeSilently(closeable4);
            } catch (Throwable th6) {
                th = th6;
                closeable3 = rectifyBis;
                closeable2 = bufferedInputStream;
                closeable = bufferedInputStream;
                Utils.closeSilently(closeable);
                Utils.closeSilently(closeable2);
                Utils.closeSilently(closeable3);
                Utils.closeSilently(closeable4);
                throw th;
            }
        } catch (IOException e10) {
            ioe = e10;
            GalleryLog.w(TAG, "Failed to processImageData." + ioe.getMessage());
            Utils.closeSilently(closeable);
            Utils.closeSilently(closeable2);
            Utils.closeSilently(closeable3);
            Utils.closeSilently(closeable4);
        } catch (Exception e11) {
            e = e11;
            GalleryLog.w(TAG, "Fail processImageData." + e.getMessage());
            Utils.closeSilently(closeable);
            Utils.closeSilently(closeable2);
            Utils.closeSilently(closeable3);
            Utils.closeSilently(closeable4);
        }
    }

    public static ExifInterface getExifData(String filePath, String mimeType) {
        FileNotFoundException e;
        IOException e2;
        Throwable t;
        Throwable th;
        ExifInterface exif = new ExifInterface();
        Closeable closeable = null;
        try {
            Closeable inStream = new FileInputStream(filePath);
            try {
                exif.readExif((InputStream) inStream);
                Utils.closeSilently(inStream);
                closeable = inStream;
            } catch (FileNotFoundException e3) {
                e = e3;
                closeable = inStream;
                GalleryLog.w(TAG, "Cannot find file: " + filePath + "." + e.getMessage());
                Utils.closeSilently(closeable);
                return exif;
            } catch (IOException e4) {
                e2 = e4;
                closeable = inStream;
                GalleryLog.w(TAG, "Cannot read exif for: " + filePath + "." + e2.getMessage());
                Utils.closeSilently(closeable);
                return exif;
            } catch (Throwable th2) {
                th = th2;
                closeable = inStream;
                Utils.closeSilently(closeable);
                throw th;
            }
        } catch (FileNotFoundException e5) {
            e = e5;
            GalleryLog.w(TAG, "Cannot find file: " + filePath + "." + e.getMessage());
            Utils.closeSilently(closeable);
            return exif;
        } catch (IOException e6) {
            e2 = e6;
            GalleryLog.w(TAG, "Cannot read exif for: " + filePath + "." + e2.getMessage());
            Utils.closeSilently(closeable);
            return exif;
        } catch (Throwable th3) {
            t = th3;
            GalleryLog.w(TAG, "fail to operate exif file:" + filePath + "." + t.getMessage());
            Utils.closeSilently(closeable);
            return exif;
        }
        return exif;
    }

    public static void updateExifData(ExifInterface exif, int width, int height) {
        exif.setTag(exif.buildTag(ExifInterface.TAG_IMAGE_WIDTH, Integer.valueOf(width)));
        exif.setTag(exif.buildTag(ExifInterface.TAG_IMAGE_LENGTH, Integer.valueOf(height)));
        exif.setTag(exif.buildTag(ExifInterface.TAG_PIXEL_X_DIMENSION, Integer.valueOf(width)));
        exif.setTag(exif.buildTag(ExifInterface.TAG_PIXEL_Y_DIMENSION, Integer.valueOf(height)));
        exif.removeCompressedThumbnail();
    }

    private static void putExifData(ExifInterface exif, BufferedInputStream inputStream, String targetPath) {
        byte[] buffer = new byte[1048576];
        Closeable closeable = null;
        try {
            closeable = exif.getExifWriterStream(targetPath);
            while (true) {
                int readCount = inputStream.read(buffer);
                if (readCount == -1) {
                    break;
                }
                closeable.write(buffer, 0, readCount);
            }
            closeable.flush();
        } catch (FileNotFoundException e) {
            GalleryLog.w(TAG, "File not found: " + targetPath + "." + e.getMessage());
        } catch (IOException e2) {
            GalleryLog.w(TAG, "Could not write exif. " + e2.getMessage());
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    public static boolean isRectifyNativeSupport() {
        return sIsRectifyNativeSupport;
    }

    private static File ensureExternalCacheDirByItem(Context context, MediaItem mediaItem) {
        if (context == null || mediaItem == null || mediaItem.getFilePath() == null) {
            return null;
        }
        long minSize = 3 * mediaItem.getSize();
        File file = null;
        for (String path : GalleryUtils.getVolumePaths()) {
            if (mediaItem.getFilePath().startsWith(path)) {
                if (GalleryUtils.checkDiskSpace(path, minSize)) {
                    file = new File(path, "/Android/data/" + context.getPackageName() + "/cache");
                } else {
                    ContextedUtils.showToastQuickly(context, (int) R.string.insufficient_storage_space, 0);
                }
                return GalleryUtils.createDirIfNeed(file);
            }
        }
        return GalleryUtils.createDirIfNeed(file);
    }

    public boolean isCacheDirInvalid() {
        return this.mCacheDir == null;
    }

    public static int getRectifyOffset(String path) {
        IOException ex;
        NullPointerException ex2;
        IllegalArgumentException ex3;
        Throwable th;
        Closeable closeable = null;
        try {
            Closeable randomFile = new RandomAccessFile(path, "r");
            try {
                long fileLength = randomFile.length();
                if (fileLength < 20) {
                    Utils.closeSilently(randomFile);
                    return -1;
                }
                randomFile.seek(fileLength - 20);
                byte[] buffer = new byte[20];
                if (randomFile.read(buffer) != 20) {
                    Utils.closeSilently(randomFile);
                    return -1;
                }
                String tag = new String(buffer, "ISO-8859-1").trim();
                if (tag.startsWith("RECTIFY_")) {
                    int parseInt = Integer.parseInt(tag.split("_")[1]);
                    Utils.closeSilently(randomFile);
                    return parseInt;
                }
                Utils.closeSilently(randomFile);
                closeable = randomFile;
                return -1;
            } catch (IOException e) {
                ex = e;
                closeable = randomFile;
                GalleryLog.w(TAG, "fail to getRectifyOffset IOException" + ex.getMessage());
                Utils.closeSilently(closeable);
                return -1;
            } catch (NullPointerException e2) {
                ex2 = e2;
                closeable = randomFile;
                GalleryLog.w(TAG, "fail to getRectifyOffset NullPointerException" + ex2.getMessage());
                Utils.closeSilently(closeable);
                return -1;
            } catch (IllegalArgumentException e3) {
                ex3 = e3;
                closeable = randomFile;
                try {
                    GalleryLog.w(TAG, "fail to getRectifyOffset IllegalArgumentException" + ex3.getMessage());
                    Utils.closeSilently(closeable);
                    return -1;
                } catch (Throwable th2) {
                    th = th2;
                    Utils.closeSilently(closeable);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                closeable = randomFile;
                Utils.closeSilently(closeable);
                throw th;
            }
        } catch (IOException e4) {
            ex = e4;
            GalleryLog.w(TAG, "fail to getRectifyOffset IOException" + ex.getMessage());
            Utils.closeSilently(closeable);
            return -1;
        } catch (NullPointerException e5) {
            ex2 = e5;
            GalleryLog.w(TAG, "fail to getRectifyOffset NullPointerException" + ex2.getMessage());
            Utils.closeSilently(closeable);
            return -1;
        } catch (IllegalArgumentException e6) {
            ex3 = e6;
            GalleryLog.w(TAG, "fail to getRectifyOffset IllegalArgumentException" + ex3.getMessage());
            Utils.closeSilently(closeable);
            return -1;
        }
    }
}
