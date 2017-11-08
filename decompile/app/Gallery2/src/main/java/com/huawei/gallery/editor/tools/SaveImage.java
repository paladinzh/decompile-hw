package com.huawei.gallery.editor.tools;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore.Images.Media;
import com.android.gallery3d.R;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.exif.ExifInterface;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.FileUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.editor.cache.ImageLoader;
import com.huawei.gallery.editor.pipeline.CachingPipeline;
import com.huawei.gallery.editor.pipeline.ImagePreset;
import com.huawei.gallery.editor.pipeline.SimpleEditorManager;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import tmsdk.common.module.update.UpdateConfig;

public class SaveImage {
    private final Context mContext;
    private final File mDestinationDirectory;
    private final Bitmap mPreviewImage;
    private final Uri mSelectedImageUri;
    private final Uri mSourceUri;

    public interface WaitForDataLoad {
        boolean waitForDataLoad(Uri uri, Bitmap bitmap);
    }

    public interface ContentResolverQueryCallback {
        void onCursorResult(Cursor cursor);
    }

    private long getFileSizeByFd(android.content.Context r7, android.net.Uri r8) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x002f in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r6 = this;
        r4 = 0;
        r1 = 0;
        r2 = r7.getContentResolver();	 Catch:{ Exception -> 0x0020, all -> 0x0030 }
        r3 = "r";	 Catch:{ Exception -> 0x0020, all -> 0x0030 }
        r1 = r2.openFileDescriptor(r8, r3);	 Catch:{ Exception -> 0x0020, all -> 0x0030 }
        if (r1 != 0) goto L_0x0016;
    L_0x0010:
        if (r1 == 0) goto L_0x0015;
    L_0x0012:
        com.android.gallery3d.common.Utils.closeSilently(r1);
    L_0x0015:
        return r4;
    L_0x0016:
        r2 = r1.getStatSize();	 Catch:{ Exception -> 0x0020, all -> 0x0030 }
        if (r1 == 0) goto L_0x001f;
    L_0x001c:
        com.android.gallery3d.common.Utils.closeSilently(r1);
    L_0x001f:
        return r2;
    L_0x0020:
        r0 = move-exception;
        r2 = "SaveImage";	 Catch:{ Exception -> 0x0020, all -> 0x0030 }
        r3 = "get file size by fd error!";	 Catch:{ Exception -> 0x0020, all -> 0x0030 }
        com.android.gallery3d.util.GalleryLog.d(r2, r3);	 Catch:{ Exception -> 0x0020, all -> 0x0030 }
        if (r1 == 0) goto L_0x002f;
    L_0x002c:
        com.android.gallery3d.common.Utils.closeSilently(r1);
    L_0x002f:
        return r4;
    L_0x0030:
        r2 = move-exception;
        if (r1 == 0) goto L_0x0036;
    L_0x0033:
        com.android.gallery3d.common.Utils.closeSilently(r1);
    L_0x0036:
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.gallery.editor.tools.SaveImage.getFileSizeByFd(android.content.Context, android.net.Uri):long");
    }

    private static void querySourceFromContentResolver(android.content.ContentResolver r8, android.net.Uri r9, java.lang.String[] r10, com.huawei.gallery.editor.tools.SaveImage.ContentResolverQueryCallback r11) {
        /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:80)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r6 = 0;
        r3 = 0;
        r4 = 0;
        r5 = 0;
        r0 = r8;
        r1 = r9;
        r2 = r10;
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x001c, all -> 0x0023 }
        if (r6 == 0) goto L_0x0016;	 Catch:{ Exception -> 0x001c, all -> 0x0023 }
    L_0x000d:
        r0 = r6.moveToNext();	 Catch:{ Exception -> 0x001c, all -> 0x0023 }
        if (r0 == 0) goto L_0x0016;	 Catch:{ Exception -> 0x001c, all -> 0x0023 }
    L_0x0013:
        r11.onCursorResult(r6);	 Catch:{ Exception -> 0x001c, all -> 0x0023 }
    L_0x0016:
        if (r6 == 0) goto L_0x001b;
    L_0x0018:
        r6.close();
    L_0x001b:
        return;
    L_0x001c:
        r7 = move-exception;
        if (r6 == 0) goto L_0x001b;
    L_0x001f:
        r6.close();
        goto L_0x001b;
    L_0x0023:
        r0 = move-exception;
        if (r6 == 0) goto L_0x0029;
    L_0x0026:
        r6.close();
    L_0x0029:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.gallery.editor.tools.SaveImage.querySourceFromContentResolver(android.content.ContentResolver, android.net.Uri, java.lang.String[], com.huawei.gallery.editor.tools.SaveImage$ContentResolverQueryCallback):void");
    }

    public SaveImage(Context context, Uri sourceUri, Uri selectedImageUri, File destinationDirectory, Bitmap previewImage) {
        this.mContext = context;
        this.mSourceUri = sourceUri;
        this.mPreviewImage = previewImage;
        this.mDestinationDirectory = destinationDirectory;
        this.mSelectedImageUri = selectedImageUri;
    }

    private static File getFinalSaveDirectory(Context context, Uri sourceUri) {
        File saveDirectory = getSaveDirectory(context, sourceUri);
        if (saveDirectory != null && saveDirectory.toString().startsWith("/system/")) {
            saveDirectory = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_PICTURES);
        }
        if (saveDirectory == null || !saveDirectory.canWrite()) {
            saveDirectory = new File(Environment.getExternalStorageDirectory(), "EditedOnlinePhotos");
        }
        if (!saveDirectory.exists()) {
            GalleryLog.i("SaveImage", "saveDirectory mkdirs:" + saveDirectory.mkdirs());
        }
        return saveDirectory;
    }

    private ExifInterface getExifData(Uri source, String mimeType) {
        ExifInterface exif = new ExifInterface();
        if ("image/jpeg".equals(mimeType)) {
            Closeable closeable = null;
            try {
                closeable = this.mContext.getContentResolver().openInputStream(source);
                exif.readExif((InputStream) closeable);
            } catch (FileNotFoundException e) {
                GalleryLog.w("SaveImage", "Cannot find file: " + source + "." + e.getMessage());
            } catch (IOException e2) {
                GalleryLog.w("SaveImage", "Cannot read exif for: " + source + "." + e2.getMessage());
            } catch (Throwable t) {
                GalleryLog.w("SaveImage", "fail to operate exif file:" + source + "." + t.getMessage());
            } finally {
                Utils.closeSilently(closeable);
            }
        }
        return exif;
    }

    private boolean putExifData(File file, ExifInterface exif, Bitmap image, int jpegCompressQuality) {
        boolean ret = false;
        Closeable closeable = null;
        try {
            closeable = exif.getExifWriterStream(file.getAbsolutePath());
            EditorUtils.compressToJpeg(image, jpegCompressQuality, closeable);
            closeable.flush();
            closeable.close();
            closeable = null;
            ret = true;
        } catch (FileNotFoundException e) {
            GalleryLog.w("SaveImage", "File not found: " + file.getAbsolutePath() + "." + e.getMessage());
        } catch (IOException e2) {
            GalleryLog.w("SaveImage", "Could not write exif. " + e2.getMessage());
        } finally {
            Utils.closeSilently(closeable);
        }
        return ret;
    }

    private void updateExifData(ExifInterface exif, long time, int width, int height) {
        exif.addDateTimeStampTag(ExifInterface.TAG_DATE_TIME, time, TimeZone.getDefault());
        exif.setTag(exif.buildTag(ExifInterface.TAG_ORIENTATION, Short.valueOf((short) 1)));
        exif.setTag(exif.buildTag(ExifInterface.TAG_IMAGE_WIDTH, Integer.valueOf(width)));
        exif.setTag(exif.buildTag(ExifInterface.TAG_IMAGE_LENGTH, Integer.valueOf(height)));
        exif.setTag(exif.buildTag(ExifInterface.TAG_PIXEL_X_DIMENSION, Integer.valueOf(width)));
        exif.setTag(exif.buildTag(ExifInterface.TAG_PIXEL_Y_DIMENSION, Integer.valueOf(height)));
        exif.removeCompressedThumbnail();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Uri processAndSaveImage(ImagePreset preset, int quality, CachingPipeline pipeline, WaitForDataLoad service) {
        Uri uri = null;
        try {
            if (isStorageSpaceNotEnough()) {
                if (this.mPreviewImage != null) {
                    this.mPreviewImage.recycle();
                }
                return null;
            }
            Bitmap bitmap = loadMutableBitmap();
            if (bitmap == null) {
                if (this.mPreviewImage != null) {
                    this.mPreviewImage.recycle();
                }
                return null;
            }
            boolean save;
            if (bitmap.getConfig() == null) {
                bitmap.recycle();
                bitmap = this.mPreviewImage;
            } else {
                bitmap = pipeline.renderFinalImage(bitmap, preset);
            }
            long time = System.currentTimeMillis();
            String mimeType = this.mContext.getContentResolver().getType(this.mSelectedImageUri);
            if (mimeType == null) {
                mimeType = ImageLoader.getMimeType(this.mSelectedImageUri);
            }
            File destinationFile = getDestinationFile(this.mDestinationDirectory, this.mSelectedImageUri, mimeType, this.mContext);
            if ("image/png".equalsIgnoreCase(mimeType)) {
                save = EditorUtils.writeToPNGFile(bitmap, destinationFile.getAbsolutePath());
            } else {
                ExifInterface exif = getExifData(this.mSourceUri, mimeType);
                updateExifData(exif, time, bitmap.getWidth(), bitmap.getHeight());
                save = putExifData(destinationFile, exif, bitmap, quality);
            }
            if (save) {
                ContentValues values = getContentValues(this.mContext, this.mSelectedImageUri, destinationFile, time, mimeType);
                values.put("width", Integer.valueOf(bitmap.getWidth()));
                values.put("height", Integer.valueOf(bitmap.getHeight()));
                uri = this.mContext.getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
                if (this.mPreviewImage != null) {
                    service.waitForDataLoad(uri, this.mPreviewImage.copy(this.mPreviewImage.getConfig(), true));
                }
                putVoiceData(uri, destinationFile);
            }
            if (bitmap != this.mPreviewImage) {
                bitmap.recycle();
            }
            if (this.mPreviewImage != null) {
                this.mPreviewImage.recycle();
            }
            return uri;
        } catch (FileNotFoundException ex) {
            GalleryLog.w("SaveImage", "Failed to save image!" + ex.getMessage());
            if (this.mPreviewImage != null) {
                this.mPreviewImage.recycle();
            }
        } catch (SecurityException e) {
            GalleryLog.noPermissionForMediaProviderLog("SaveImage");
            if (this.mPreviewImage != null) {
                this.mPreviewImage.recycle();
            }
        } catch (Throwable th) {
            if (this.mPreviewImage != null) {
                this.mPreviewImage.recycle();
            }
        }
    }

    public static void saveImage(ImagePreset preset, SimpleEditorManager editorManager) {
        editorManager.getProcessingService().saveImage(editorManager.getUri(), new ImagePreset(preset));
    }

    private static void querySource(Context context, Uri sourceUri, String[] projection, ContentResolverQueryCallback callback) {
        querySourceFromContentResolver(context.getContentResolver(), sourceUri, projection, callback);
    }

    private static File getSaveDirectory(Context context, Uri sourceUri) {
        File file = getLocalFileFromUri(context, sourceUri);
        if (file != null) {
            return file.getParentFile();
        }
        return null;
    }

    private static File getLocalFileFromUri(Context context, Uri srcUri) {
        if (srcUri == null) {
            GalleryLog.e("SaveImage", "srcUri is null.");
            return null;
        }
        String scheme = srcUri.getScheme();
        if (scheme == null) {
            GalleryLog.e("SaveImage", "scheme is null.");
            return null;
        }
        final File[] file = new File[1];
        if (scheme.equals("content")) {
            if ("media".equals(srcUri.getAuthority())) {
                querySource(context, srcUri, new String[]{"_data"}, new ContentResolverQueryCallback() {
                    public void onCursorResult(Cursor cursor) {
                        file[0] = new File(cursor.getString(0));
                    }
                });
            }
        } else if (scheme.equals("file")) {
            file[0] = new File(srcUri.getPath());
        }
        return file[0];
    }

    private static ContentValues getContentValues(Context context, Uri sourceUri, File file, long time, String mimeType) {
        final ContentValues values = new ContentValues();
        String fileTitle = file.getName();
        boolean isPng = "image/png".equalsIgnoreCase(mimeType);
        values.put("title", fileTitle.substring(0, fileTitle.lastIndexOf(isPng ? ".png" : ".jpg")));
        values.put("_display_name", file.getName());
        String str = "mime_type";
        if (!isPng) {
            mimeType = "image/jpeg";
        }
        values.put(str, mimeType);
        values.put("datetaken", Long.valueOf(time));
        time /= 1000;
        values.put("date_modified", Long.valueOf(time));
        values.put("date_added", Long.valueOf(time));
        values.put("orientation", Integer.valueOf(0));
        values.put("_data", file.getAbsolutePath());
        values.put("_size", Long.valueOf(file.length()));
        values.put("mini_thumb_magic", Integer.valueOf(0));
        querySource(context, sourceUri, new String[]{"datetaken", "latitude", "longitude"}, new ContentResolverQueryCallback() {
            public void onCursorResult(Cursor cursor) {
                values.put("datetaken", Long.valueOf(cursor.getLong(0)));
                double latitude = cursor.getDouble(1);
                double longitude = cursor.getDouble(2);
                if (latitude != 0.0d || longitude != 0.0d) {
                    values.put("latitude", Double.valueOf(latitude));
                    values.put("longitude", Double.valueOf(longitude));
                }
            }
        });
        return values;
    }

    private Bitmap loadMutableBitmap() throws FileNotFoundException {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            Options options = new Options();
            options.inJustDecodeBounds = true;
            parcelFileDescriptor = this.mContext.getContentResolver().openFileDescriptor(this.mSourceUri, "r");
            if (parcelFileDescriptor == null) {
                if (parcelFileDescriptor != null) {
                    try {
                        parcelFileDescriptor.close();
                    } catch (Exception e) {
                    }
                }
                return null;
            }
            FileDescriptor fd = parcelFileDescriptor.getFileDescriptor();
            BitmapFactory.decodeFileDescriptor(fd, null, options);
            int w = options.outWidth;
            int h = options.outHeight;
            if (w * h > 13000000) {
                options.inSampleSize = BitmapUtils.computeSampleSize((float) Math.sqrt(1.3E7d / ((double) (w * h))));
            }
            options.inJustDecodeBounds = false;
            options.inMutable = true;
            Bitmap bitmap = ImageLoader.orientBitmap(BitmapFactory.decodeFileDescriptor(fd, null, options), ImageLoader.getMetadataOrientation(this.mContext, this.mSourceUri), false);
            if (parcelFileDescriptor != null) {
                try {
                    parcelFileDescriptor.close();
                } catch (Exception e2) {
                }
            }
            return bitmap;
        } catch (Exception e3) {
            GalleryLog.i("SaveImage", "loadMutableBitmap() failed." + e3.getMessage());
            if (parcelFileDescriptor != null) {
                try {
                    parcelFileDescriptor.close();
                } catch (Exception e4) {
                }
            }
            return null;
        } catch (Throwable th) {
            if (parcelFileDescriptor != null) {
                try {
                    parcelFileDescriptor.close();
                } catch (Exception e5) {
                }
            }
        }
    }

    private void putVoiceData(Uri uri, File destinationFile) {
        if (ApiHelper.HAS_MEDIA_COLUMNS_HW_VOICE_OFFSET) {
            File sourceFile = getLocalFileFromUri(this.mContext, this.mSourceUri);
            if (sourceFile != null) {
                long offset = Utils.getVoiceOffset(sourceFile.getAbsolutePath());
                if (offset > 0 && copyVoice(sourceFile.getAbsolutePath(), destinationFile.getAbsolutePath(), offset)) {
                    ContentValues values = new ContentValues();
                    values.put("hw_voice_offset", Long.valueOf(offset));
                    try {
                        this.mContext.getContentResolver().update(uri, values, null, null);
                    } catch (SecurityException e) {
                        GalleryLog.noPermissionForMediaProviderLog("SaveImage");
                    }
                }
            }
        }
    }

    private boolean copyVoice(String sourcePath, String destPath, long offset) {
        IOException e;
        Throwable th;
        Exception e2;
        Closeable closeable = null;
        Closeable closeable2 = null;
        try {
            Closeable os;
            Closeable is = new BufferedInputStream(new FileInputStream(sourcePath));
            try {
                os = new BufferedOutputStream(new FileOutputStream(destPath, true));
            } catch (IOException e3) {
                e = e3;
                closeable = is;
                try {
                    GalleryLog.w("SaveImage", "Failed to copy Voice ,for IOException. " + e.getMessage());
                    Utils.closeSilently(closeable);
                    Utils.closeSilently(closeable2);
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    Utils.closeSilently(closeable);
                    Utils.closeSilently(closeable2);
                    throw th;
                }
            } catch (Exception e4) {
                e2 = e4;
                closeable = is;
                GalleryLog.w("SaveImage", "Failed to copy Voice ,for other Exception. " + e2.getMessage());
                Utils.closeSilently(closeable);
                Utils.closeSilently(closeable2);
                return false;
            } catch (Throwable th3) {
                th = th3;
                closeable = is;
                Utils.closeSilently(closeable);
                Utils.closeSilently(closeable2);
                throw th;
            }
            try {
                FileUtils.skip(is, (new File(sourcePath).length() - offset) - 20);
                byte[] b = new byte[1048576];
                while (true) {
                    int len = is.read(b);
                    if (len != -1) {
                        os.write(b, 0, len);
                    } else {
                        Utils.closeSilently(is);
                        Utils.closeSilently(os);
                        return true;
                    }
                }
            } catch (IOException e5) {
                e = e5;
                closeable2 = os;
                closeable = is;
                GalleryLog.w("SaveImage", "Failed to copy Voice ,for IOException. " + e.getMessage());
                Utils.closeSilently(closeable);
                Utils.closeSilently(closeable2);
                return false;
            } catch (Exception e6) {
                e2 = e6;
                closeable2 = os;
                closeable = is;
                GalleryLog.w("SaveImage", "Failed to copy Voice ,for other Exception. " + e2.getMessage());
                Utils.closeSilently(closeable);
                Utils.closeSilently(closeable2);
                return false;
            } catch (Throwable th4) {
                th = th4;
                closeable2 = os;
                closeable = is;
                Utils.closeSilently(closeable);
                Utils.closeSilently(closeable2);
                throw th;
            }
        } catch (IOException e7) {
            e = e7;
            GalleryLog.w("SaveImage", "Failed to copy Voice ,for IOException. " + e.getMessage());
            Utils.closeSilently(closeable);
            Utils.closeSilently(closeable2);
            return false;
        } catch (Exception e8) {
            e2 = e8;
            GalleryLog.w("SaveImage", "Failed to copy Voice ,for other Exception. " + e2.getMessage());
            Utils.closeSilently(closeable);
            Utils.closeSilently(closeable2);
            return false;
        }
    }

    private String getMimeTypeByUri(Context context, Uri imageUri) {
        String mimeType = context.getContentResolver().getType(imageUri);
        if (mimeType == null) {
            return ImageLoader.getMimeType(imageUri);
        }
        return mimeType;
    }

    private boolean isStorageSpaceNotEnough() {
        long sourceFileSize = getFileSizeByFd(this.mContext, this.mSourceUri);
        String mimeType = getMimeTypeByUri(this.mContext, this.mSourceUri);
        String dataPath = getFinalSaveDirectory(this.mContext, this.mSourceUri).toString();
        long size = UpdateConfig.UPDATE_FLAG_DEEPCLEAN_SOFT_PATH_LIST;
        if (!"image/bmp".equals(mimeType)) {
            size = UpdateConfig.UPDATE_FLAG_DEEPCLEAN_SOFT_PATH_LIST + sourceFileSize;
        }
        if (GalleryUtils.hasSpaceForSize(size + 10485760, dataPath)) {
            return false;
        }
        new Handler(this.mContext.getMainLooper()).post(new Runnable() {
            public void run() {
                ContextedUtils.showToastQuickly(SaveImage.this.mContext, (int) R.string.insufficient_storage_space, 0);
            }
        });
        return true;
    }

    private File getDestinationFile(File destinationDirectory, Uri selectedImageUri, String mimeType, Context context) {
        if (destinationDirectory == null) {
            destinationDirectory = getFinalSaveDirectory(context, selectedImageUri);
        } else if (!destinationDirectory.exists()) {
            GalleryLog.i("SaveImage", "destinationDirectory mkdirs:" + destinationDirectory.mkdirs());
        }
        return new File(destinationDirectory, "IMG" + new SimpleDateFormat("_yyyyMMdd_HHmmss").format(new Date(System.currentTimeMillis())) + ("image/png".equalsIgnoreCase(mimeType) ? ".png" : ".jpg"));
    }
}
