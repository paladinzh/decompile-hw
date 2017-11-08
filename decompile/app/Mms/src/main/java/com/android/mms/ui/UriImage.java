package com.android.mms.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.android.mms.MmsConfig;
import com.android.mms.exif.ExifInterface;
import com.android.mms.model.MediaModel;
import com.android.mms.util.ShareUtils;
import com.google.android.mms.pdu.PduPart;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Locale;

public class UriImage {
    private static final UriMatcher sURLMatcher = new UriMatcher(-1);
    private String mContentType;
    private final Context mContext;
    private int mHeight;
    private boolean mIsFileProvider = false;
    private String mKeyforImagecache;
    private String mPath;
    private String mSrc;
    private String mSuffix = ".jpeg";
    private final Uri mUri;
    private int mWidth;

    static {
        sURLMatcher.addURI("mms", "part/#", 12);
    }

    public UriImage(Context context, Uri uri) {
        if (context == null || uri == null) {
            throw new IllegalArgumentException();
        }
        if (uri.getScheme().equals("content")) {
            initFromContentUri(context, uri);
        } else if (uri.getScheme().equals("file")) {
            initFromFile(context, uri);
        }
        if (!(!MmsConfig.getIsRenameAttachmentName() || this.mSrc == null || MessageUtils.isNormalASCII(this.mSrc))) {
            if (this.mSrc.lastIndexOf(46) > 0) {
                this.mSrc = System.currentTimeMillis() + this.mSrc.substring(this.mSrc.lastIndexOf(46));
            } else {
                this.mSrc = "" + System.currentTimeMillis() + this.mSuffix;
            }
        }
        this.mContext = context;
        this.mUri = uri;
        decodeBoundsInfo();
    }

    private void initFromFile(Context context, Uri uri) {
        this.mPath = uri.getPath();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String extension = MimeTypeMap.getFileExtensionFromUrl(this.mPath);
        if (TextUtils.isEmpty(extension)) {
            int dotPos = this.mPath.lastIndexOf(46);
            if (dotPos >= 0) {
                extension = this.mPath.substring(dotPos + 1);
            }
        }
        this.mContentType = mimeTypeMap.getMimeTypeFromExtension(extension.toLowerCase());
        buildSrcFromPath();
    }

    private void buildSrcFromPath() {
        if (this.mPath != null) {
            this.mSrc = this.mPath.substring(this.mPath.lastIndexOf(47) + 1);
            if (this.mSrc.lastIndexOf(46) > 0) {
                MLog.v("Mms/image", "mSrc is a normal path having a postfix");
            } else {
                this.mSrc += this.mSuffix;
            }
            if (this.mSrc.startsWith(".") && this.mSrc.length() > 1) {
                this.mSrc = this.mSrc.substring(1);
                if (this.mSrc.lastIndexOf(46) > 0) {
                    MLog.v("Mms/image", "mSrc is a normal path having a postfix");
                } else {
                    this.mSrc += this.mSuffix;
                }
            }
            this.mSrc = this.mSrc.replace(' ', '_');
        }
    }

    private void initFromContentUri(Context context, Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        Cursor c = SqliteWrapper.query(context, resolver, uri, null, null, null, null);
        this.mSrc = null;
        if (c == null) {
            throw new IllegalArgumentException("Query on " + uri + " returns null result.");
        }
        try {
            if (c.getCount() == 1 && c.moveToFirst()) {
                String filePath = null;
                if (MediaModel.isMmsUri(uri)) {
                    filePath = c.getString(c.getColumnIndexOrThrow("fn"));
                    if (TextUtils.isEmpty(filePath)) {
                        filePath = c.getString(c.getColumnIndexOrThrow("_data"));
                    }
                    this.mKeyforImagecache = c.getString(c.getColumnIndexOrThrow("_data"));
                    this.mContentType = c.getString(c.getColumnIndexOrThrow("ct"));
                } else {
                    if (c.getColumnIndex("_data") != -1) {
                        filePath = uri.getPath();
                        try {
                            this.mContentType = c.getString(c.getColumnIndexOrThrow("mime_type"));
                        } catch (IllegalArgumentException e) {
                            try {
                                this.mContentType = c.getString(c.getColumnIndexOrThrow("mimetype"));
                            } catch (IllegalArgumentException e2) {
                                this.mContentType = resolver.getType(uri);
                                MLog.v("Mms/image", "initFromContentUri: " + uri + ", getType => " + this.mContentType);
                            }
                        }
                    } else {
                        this.mIsFileProvider = false;
                        if (ShareUtils.isFileProviderImageType(uri.toString())) {
                            this.mIsFileProvider = true;
                            File outputFile = ShareUtils.fileProvideUriCopy(context, uri, false);
                            if (outputFile != null) {
                                filePath = outputFile.getAbsolutePath();
                                this.mSrc = outputFile.getName();
                            }
                            this.mContentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()).toLowerCase(Locale.getDefault()));
                        } else {
                            filePath = c.getString(c.getColumnIndexOrThrow("_display_name"));
                            this.mContentType = c.getString(c.getColumnIndexOrThrow("mime_type"));
                        }
                    }
                    int nameIndex = c.getColumnIndex("_display_name");
                    if (nameIndex != -1) {
                        this.mSrc = c.getString(nameIndex);
                        if (TextUtils.isEmpty(this.mSrc)) {
                            this.mSrc = null;
                        } else {
                            this.mSrc = this.mSrc.replace(' ', '_');
                        }
                    }
                }
                this.mPath = filePath;
                if (this.mPath != null) {
                    if (this.mSrc == null) {
                        buildSrcFromPath();
                    }
                    c.close();
                    return;
                }
                return;
            }
            throw new IllegalArgumentException("Query on " + uri + " returns 0 or multiple rows.");
        } catch (IllegalArgumentException e3) {
            MLog.e("Mms/image", "initFromContentUri couldn't load image uri ", (Throwable) e3);
        } finally {
            c.close();
        }
    }

    private void decodeBoundsInfo() {
        InputStream inputStream = null;
        try {
            inputStream = this.mContext.getContentResolver().openInputStream(this.mUri);
            Options opt = new Options();
            opt.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, opt);
            this.mWidth = opt.outWidth;
            this.mHeight = opt.outHeight;
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    MLog.e("Mms/image", "IOException caught while closing stream", (Throwable) e);
                }
            }
        } catch (FileNotFoundException e2) {
            MLog.e("Mms/image", "IOException caught while opening stream", (Throwable) e2);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                    MLog.e("Mms/image", "IOException caught while closing stream", (Throwable) e3);
                }
            }
        } catch (IllegalStateException e4) {
            MLog.e("Mms/image", "Cannot perform this operation because the connection pool has been closed.");
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e32) {
                    MLog.e("Mms/image", "IOException caught while closing stream", (Throwable) e32);
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e322) {
                    MLog.e("Mms/image", "IOException caught while closing stream", (Throwable) e322);
                }
            }
        }
    }

    public String getContentType() {
        return this.mContentType;
    }

    public boolean isGifImage() {
        return "image/gif".equalsIgnoreCase(this.mContentType);
    }

    public String getSrc() {
        return this.mSrc;
    }

    public String getFullpath() {
        if (TextUtils.isEmpty(this.mKeyforImagecache)) {
            return this.mPath;
        }
        return this.mKeyforImagecache;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public boolean isLtUriImage() {
        return this.mHeight > this.mWidth;
    }

    public PduPart getResizedImageAsPart(int widthLimit, int heightLimit, int byteLimit) {
        PduPart part = new PduPart();
        byte[] data = getResizedImageData(this.mWidth, this.mHeight, widthLimit, heightLimit, byteLimit, this.mUri, this.mContext, this.mIsFileProvider);
        if (data == null) {
            return null;
        }
        part.setData(data);
        part.setContentType("image/jpeg".getBytes(Charset.defaultCharset()));
        return part;
    }

    public static byte[] getResizedImageData(int width, int height, int widthLimit, int heightLimit, int byteLimit, Uri uri, Context context) {
        return getResizedImageData(width, height, widthLimit, heightLimit, byteLimit, uri, context, false);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static byte[] getResizedImageData(int width, int height, int widthLimit, int heightLimit, int byteLimit, Uri uri, Context context, boolean isFileProvider) {
        Throwable th;
        boolean resultTooBig;
        int outWidth = width;
        int outHeight = height;
        float scaleFactor = ContentUtil.FONT_SIZE_NORMAL;
        while (true) {
            if (((float) width) * scaleFactor <= ((float) widthLimit) && ((float) height) * scaleFactor <= ((float) heightLimit)) {
                break;
            }
            scaleFactor *= 0.75f;
        }
        int orientation = 0;
        if (!isFileProvider) {
            orientation = getOrientation(context, uri);
        }
        if (MLog.isLoggable("Mms_app", 2)) {
            MLog.v("Mms/image", "getResizedBitmap: wlimit=" + widthLimit + ", hlimit=" + heightLimit + ", sizeLimit=" + byteLimit + ", width=" + width + ", height=" + height + ", initialScaleFactor=" + scaleFactor + ", orientation=" + orientation);
        }
        InputStream inputStream = null;
        ByteArrayOutputStream os = null;
        int attempts = 1;
        int sampleSize = 1;
        OutOfMemoryError e;
        try {
            FileNotFoundException e2;
            Options options = new Options();
            int quality = 95;
            Bitmap b = null;
            InputStream input = null;
            while (true) {
                if (!isFileProvider) {
                    inputStream = context.getContentResolver().openInputStream(uri);
                } else if (context == null) {
                    MLog.e("Mms/image", "context is null");
                    cleanInputStream(input);
                    cleanByteArrayOutputStream(null);
                    return null;
                } else {
                    try {
                        inputStream = new FileInputStream(new File(context.getCacheDir(), "shared_image_file"));
                    } catch (FileNotFoundException e3) {
                        e2 = e3;
                        inputStream = input;
                    } catch (OutOfMemoryError e4) {
                        e = e4;
                        inputStream = input;
                    } catch (Throwable th2) {
                        th = th2;
                        inputStream = input;
                    }
                }
                byte[] toByteArray;
                try {
                    options.inSampleSize = sampleSize;
                    try {
                        b = BitmapFactory.decodeStream(inputStream, null, options);
                        if (b == null) {
                            break;
                        }
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                                inputStream = null;
                            } catch (IOException e5) {
                                MLog.e("Mms/image", e5.getMessage(), (Throwable) e5);
                            }
                        }
                        if (b == null && attempts < 4) {
                            input = inputStream;
                        } else if (b == null) {
                            if (MLog.isLoggable("Mms_app", 2) && attempts >= 4) {
                                MLog.v("Mms/image", "getResizedImageData: gave up after too many attempts to resize");
                            }
                            cleanInputStream(inputStream);
                            cleanByteArrayOutputStream(null);
                            return null;
                        } else {
                            attempts = 1;
                            ByteArrayOutputStream os2 = null;
                            do {
                                try {
                                    int jpgFileSize;
                                    if (options.outWidth <= widthLimit && options.outHeight <= heightLimit) {
                                        if (os2 != null) {
                                        }
                                        cleanByteArrayOutputStream(os2);
                                        os = new ByteArrayOutputStream();
                                        b.compress(CompressFormat.JPEG, quality, os);
                                        jpgFileSize = os.size();
                                        if (jpgFileSize > byteLimit) {
                                            quality = (quality * byteLimit) / jpgFileSize;
                                            if (quality < 50) {
                                                quality = 50;
                                            }
                                            if (MLog.isLoggable("Mms_app", 2)) {
                                                MLog.v("Mms/image", "getResizedImageData: compress(2) w/ quality=" + quality);
                                            }
                                            try {
                                                os.close();
                                            } catch (IOException e52) {
                                                MLog.e("Mms/image", e52.getMessage(), (Throwable) e52);
                                            }
                                            os2 = new ByteArrayOutputStream();
                                            b.compress(CompressFormat.JPEG, quality, os2);
                                            os = os2;
                                        }
                                        os2 = os;
                                        if (MLog.isLoggable("Mms_app", 2)) {
                                            MLog.v("Mms/image", "attempt=" + attempts + " size=" + (os2 != null ? 0 : os2.size()) + " width=" + (((float) width) * scaleFactor) + " height=" + (((float) height) * scaleFactor) + " scaleFactor=" + scaleFactor + " quality=" + quality);
                                        }
                                        scaleFactor *= 0.75f;
                                        attempts++;
                                        resultTooBig = os2 != null || os2.size() > byteLimit;
                                        if (resultTooBig) {
                                            break;
                                        }
                                    }
                                    Bitmap ret = getScaleBitMap(width, height, scaleFactor, b);
                                    if (ret == null) {
                                        cleanInputStream(inputStream);
                                        cleanByteArrayOutputStream(os2);
                                        return null;
                                    }
                                    if (ret != b) {
                                        b.recycle();
                                    }
                                    b = ret;
                                    cleanByteArrayOutputStream(os2);
                                    os = new ByteArrayOutputStream();
                                    b.compress(CompressFormat.JPEG, quality, os);
                                    jpgFileSize = os.size();
                                    if (jpgFileSize > byteLimit) {
                                        quality = (quality * byteLimit) / jpgFileSize;
                                        if (quality < 50) {
                                            quality = 50;
                                        }
                                        if (MLog.isLoggable("Mms_app", 2)) {
                                            MLog.v("Mms/image", "getResizedImageData: compress(2) w/ quality=" + quality);
                                        }
                                        os.close();
                                        os2 = new ByteArrayOutputStream();
                                        b.compress(CompressFormat.JPEG, quality, os2);
                                        os = os2;
                                    }
                                    os2 = os;
                                    if (MLog.isLoggable("Mms_app", 2)) {
                                        if (os2 != null) {
                                        }
                                        MLog.v("Mms/image", "attempt=" + attempts + " size=" + (os2 != null ? 0 : os2.size()) + " width=" + (((float) width) * scaleFactor) + " height=" + (((float) height) * scaleFactor) + " scaleFactor=" + scaleFactor + " quality=" + quality);
                                    }
                                    scaleFactor *= 0.75f;
                                    attempts++;
                                    if (os2 != null) {
                                    }
                                    if (resultTooBig) {
                                        break;
                                    }
                                } catch (OutOfMemoryError e6) {
                                    os = os2;
                                    MLog.w("Mms/image", "getResizedImageData - image too big (OutOfMemoryError), will try  with smaller scale factor, cur scale factor: " + scaleFactor);
                                    os2 = os;
                                    if (MLog.isLoggable("Mms_app", 2)) {
                                        if (os2 != null) {
                                        }
                                        MLog.v("Mms/image", "attempt=" + attempts + " size=" + (os2 != null ? 0 : os2.size()) + " width=" + (((float) width) * scaleFactor) + " height=" + (((float) height) * scaleFactor) + " scaleFactor=" + scaleFactor + " quality=" + quality);
                                    }
                                    scaleFactor *= 0.75f;
                                    attempts++;
                                    if (os2 != null) {
                                    }
                                    if (resultTooBig) {
                                        break;
                                    } else if (attempts >= 4) {
                                    }
                                    if (!resultTooBig) {
                                    }
                                    os = os2;
                                    b.recycle();
                                    if (os == null) {
                                        MLog.v("Mms/image", "getResizedImageData returning NULL because the result is too big:  requested max: " + byteLimit + " actual: " + os.size());
                                        toByteArray = resultTooBig ? os.toByteArray() : null;
                                        cleanInputStream(inputStream);
                                        cleanByteArrayOutputStream(os);
                                        return toByteArray;
                                    }
                                    cleanInputStream(inputStream);
                                    cleanByteArrayOutputStream(os);
                                    return null;
                                } catch (FileNotFoundException e7) {
                                    e2 = e7;
                                    os = os2;
                                } catch (Throwable th3) {
                                    th = th3;
                                    os = os2;
                                }
                            } while (attempts >= 4);
                            if (resultTooBig || orientation == 0) {
                                os = os2;
                            } else {
                                cleanByteArrayOutputStream(os2);
                                b = rotateBitmap(b, orientation);
                                os = new ByteArrayOutputStream();
                                b.compress(CompressFormat.JPEG, quality, os);
                                resultTooBig = os.size() > byteLimit;
                            }
                            b.recycle();
                            if (os == null) {
                                cleanInputStream(inputStream);
                                cleanByteArrayOutputStream(os);
                                return null;
                            }
                            if (MLog.isLoggable("Mms_app", 2) && resultTooBig) {
                                MLog.v("Mms/image", "getResizedImageData returning NULL because the result is too big:  requested max: " + byteLimit + " actual: " + os.size());
                            }
                            if (resultTooBig) {
                            }
                            cleanInputStream(inputStream);
                            cleanByteArrayOutputStream(os);
                            return toByteArray;
                        }
                    } catch (OutOfMemoryError e8) {
                        MLog.w("Mms/image", "getResizedBitmap: img too large to decode (OutOfMemoryError), may try with larger sampleSize. Curr sampleSize=" + sampleSize);
                        sampleSize *= 2;
                        attempts++;
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                                inputStream = null;
                            } catch (IOException e522) {
                                MLog.e("Mms/image", e522.getMessage(), (Throwable) e522);
                            }
                        }
                    } catch (Throwable th4) {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                                inputStream = null;
                            } catch (IOException e5222) {
                                MLog.e("Mms/image", e5222.getMessage(), (Throwable) e5222);
                            }
                        }
                    }
                } catch (OutOfMemoryError e9) {
                    MLog.w("Mms/image", "getResizedImageData - image too big (OutOfMemoryError)");
                    if (os == null) {
                        cleanInputStream(inputStream);
                        cleanByteArrayOutputStream(os);
                        return null;
                    }
                    b.recycle();
                    if (os == null) {
                        cleanInputStream(inputStream);
                        cleanByteArrayOutputStream(os);
                        return null;
                    }
                    MLog.v("Mms/image", "getResizedImageData returning NULL because the result is too big:  requested max: " + byteLimit + " actual: " + os.size());
                    if (resultTooBig) {
                    }
                    cleanInputStream(inputStream);
                    cleanByteArrayOutputStream(os);
                    return toByteArray;
                } catch (FileNotFoundException e10) {
                    e2 = e10;
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                    inputStream = null;
                } catch (IOException e52222) {
                    MLog.e("Mms/image", e52222.getMessage(), (Throwable) e52222);
                }
            }
            cleanInputStream(inputStream);
            cleanByteArrayOutputStream(null);
            return null;
            MLog.e("Mms/image", e.getMessage(), (Throwable) e);
            cleanInputStream(inputStream);
            cleanByteArrayOutputStream(os);
            return null;
            try {
                MLog.e("Mms/image", e2.getMessage(), (Throwable) e2);
                cleanInputStream(inputStream);
                cleanByteArrayOutputStream(os);
                return null;
            } catch (Throwable th5) {
                th = th5;
                cleanInputStream(inputStream);
                cleanByteArrayOutputStream(os);
                throw th;
            }
        } catch (FileNotFoundException e102) {
            e2 = e102;
        } catch (OutOfMemoryError e11) {
            e = e11;
        }
    }

    private static void cleanInputStream(InputStream input) {
        if (input != null) {
            try {
                input.close();
            } catch (IOException e) {
                MLog.e("Mms/image", e.getMessage(), (Throwable) e);
            }
        }
    }

    private static void cleanByteArrayOutputStream(ByteArrayOutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                MLog.e("Mms/image", e.getMessage(), (Throwable) e);
            }
        }
    }

    private static Bitmap getScaleBitMap(int outWidth, int outHeight, float scaleFactor, Bitmap b) {
        int scaledWidth = (int) (((float) outWidth) * scaleFactor);
        int scaledHeight = (int) (((float) outHeight) * scaleFactor);
        if (MLog.isLoggable("Mms_app", 2)) {
            MLog.v("Mms/image", "getResizedImageData: retry scaling using Bitmap.createScaledBitmap: w=" + scaledWidth + ", h=" + scaledHeight);
        }
        Bitmap ret = Bitmap.createScaledBitmap(b, scaledWidth, scaledHeight, false);
        if (ret != null) {
            return ret;
        }
        if (MLog.isLoggable("Mms_app", 2)) {
            MLog.v("Mms/image", "Bitmap.createScaledBitmap returned NULL!");
        }
        return null;
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        if (degrees == 0 || bitmap == null) {
            return bitmap;
        }
        Matrix m = new Matrix();
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        m.setRotate((float) degrees, ((float) w) / 2.0f, ((float) h) / 2.0f);
        try {
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, m, true);
            if (bitmap == rotatedBitmap || rotatedBitmap == null) {
                return bitmap;
            }
            bitmap.recycle();
            return rotatedBitmap;
        } catch (OutOfMemoryError ex) {
            MLog.e("Mms/image", "OOM in rotateBitmap", (Throwable) ex);
            return bitmap;
        }
    }

    public static int getOrientation(Context context, Uri uri) {
        long dur = System.currentTimeMillis();
        if ("file".equals(uri.getScheme()) || sURLMatcher.match(uri) == 12) {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                ExifInterface exif = new ExifInterface();
                try {
                    exif.readExif(inputStream);
                    Integer val = exif.getTagIntValue(ExifInterface.TAG_ORIENTATION);
                    if (val == null) {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e) {
                            }
                        }
                        dur = System.currentTimeMillis() - dur;
                        if (MLog.isLoggable("Mms_app", 2)) {
                            MLog.v("Mms/image", "UriImage.getOrientation (exif path) took: " + dur + " ms");
                        }
                        return 0;
                    }
                    int orientation = ExifInterface.getRotationForOrientationValue(val.shortValue());
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e2) {
                        }
                    }
                    dur = System.currentTimeMillis() - dur;
                    if (MLog.isLoggable("Mms_app", 2)) {
                        MLog.v("Mms/image", "UriImage.getOrientation (exif path) took: " + dur + " ms");
                    }
                    return orientation;
                } catch (IOException e3) {
                    MLog.w("Mms/image", "Failed to read EXIF orientation", e3);
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e4) {
                        }
                    }
                } catch (IllegalArgumentException e5) {
                    MLog.w("Mms/image", "Failed to read EXIF orientation with: ", e5);
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e6) {
                        }
                    }
                } catch (Throwable e7) {
                    MLog.w("Mms/image", "Failed to read EXIF orientation, ", e7);
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e8) {
                        }
                    }
                } catch (Throwable th) {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e9) {
                        }
                    }
                }
            } catch (FileNotFoundException e10) {
                try {
                    MLog.e("Mms/image", "Can't open uri: " + uri, (Throwable) e10);
                    dur = System.currentTimeMillis() - dur;
                    if (MLog.isLoggable("Mms_app", 2)) {
                        MLog.v("Mms/image", "UriImage.getOrientation (exif path) took: " + dur + " ms");
                    }
                } catch (Throwable th2) {
                    dur = System.currentTimeMillis() - dur;
                    if (MLog.isLoggable("Mms_app", 2)) {
                        MLog.v("Mms/image", "UriImage.getOrientation (exif path) took: " + dur + " ms");
                    }
                }
            } catch (IllegalStateException e11) {
                MLog.e("Mms/image", "Cannot perform this operation because the connection pool has been closed.");
                dur = System.currentTimeMillis() - dur;
                if (MLog.isLoggable("Mms_app", 2)) {
                    MLog.v("Mms/image", "UriImage.getOrientation (exif path) took: " + dur + " ms");
                }
            }
        } else {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, new String[]{"orientation"}, null, null, null);
                if (cursor == null || !cursor.moveToNext()) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    dur = System.currentTimeMillis() - dur;
                    if (MLog.isLoggable("Mms_app", 2)) {
                        MLog.v("Mms/image", "UriImage.getOrientation (db column path) took: " + dur + " ms");
                    }
                    return 0;
                }
                int ori = cursor.getInt(0);
                if (cursor != null) {
                    cursor.close();
                }
                dur = System.currentTimeMillis() - dur;
                if (MLog.isLoggable("Mms_app", 2)) {
                    MLog.v("Mms/image", "UriImage.getOrientation (db column path) took: " + dur + " ms");
                }
                return ori;
            } catch (SQLiteException e12) {
                if (cursor != null) {
                    cursor.close();
                }
                dur = System.currentTimeMillis() - dur;
                if (MLog.isLoggable("Mms_app", 2)) {
                    MLog.v("Mms/image", "UriImage.getOrientation (db column path) took: " + dur + " ms");
                }
            } catch (IllegalArgumentException e13) {
                if (cursor != null) {
                    cursor.close();
                }
                dur = System.currentTimeMillis() - dur;
                if (MLog.isLoggable("Mms_app", 2)) {
                    MLog.v("Mms/image", "UriImage.getOrientation (db column path) took: " + dur + " ms");
                }
            } catch (Throwable th3) {
                if (cursor != null) {
                    cursor.close();
                }
                dur = System.currentTimeMillis() - dur;
                if (MLog.isLoggable("Mms_app", 2)) {
                    MLog.v("Mms/image", "UriImage.getOrientation (db column path) took: " + dur + " ms");
                }
            }
        }
        dur = System.currentTimeMillis() - dur;
        if (MLog.isLoggable("Mms_app", 2)) {
            MLog.v("Mms/image", "UriImage.getOrientation (exif path) took: " + dur + " ms");
        }
        return 0;
    }
}
