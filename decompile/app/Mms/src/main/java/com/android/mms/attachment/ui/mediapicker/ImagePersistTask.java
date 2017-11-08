package com.android.mms.attachment.ui.mediapicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.android.mms.attachment.utils.SafeAsyncTask;
import com.android.mms.exif.ExifInterface;
import com.android.mms.exif.ExifTag;
import com.android.mms.ui.MessageUtils;
import com.huawei.cspcommon.MLog;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Random;

public class ImagePersistTask extends SafeAsyncTask<Void, Void, Void> {
    private static final Random RANDOM_ID = new Random();
    private static final String externalDir = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/MmsCamera/");
    private final byte[] mBytes;
    private final MediaCallback mCallback;
    private final Context mContext;
    private Exception mException;
    private int mHeight;
    private final float mHeightPercent;
    private Uri mOutputUri = createOutputUri();
    private int mWidth;

    public ImagePersistTask(int width, int height, float heightPercent, byte[] bytes, Context context, MediaCallback callback) {
        this.mWidth = width;
        this.mHeight = height;
        this.mHeightPercent = heightPercent;
        this.mBytes = (byte[]) bytes.clone();
        this.mContext = context;
        this.mCallback = callback;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected Void doInBackgroundTimed(Void... params) {
        OutputStream outputStream = null;
        Bitmap bitmap = null;
        Bitmap clippedBitmap = null;
        if (this.mOutputUri == null) {
            MLog.w("ImagePersistTask", "createOutputUri is null,can't save picture.");
            return null;
        }
        try {
            outputStream = this.mContext.getContentResolver().openOutputStream(this.mOutputUri);
            if (this.mHeightPercent != ContentUtil.FONT_SIZE_NORMAL) {
                int clippedWidth;
                int clippedHeight;
                int orientation = 0;
                ExifInterface exifInterface = new ExifInterface();
                try {
                    exifInterface.readExif(this.mBytes);
                    Integer orientationValue = exifInterface.getTagIntValue(ExifInterface.TAG_ORIENTATION);
                    if (orientationValue != null) {
                        orientation = orientationValue.intValue();
                    }
                    exifInterface.setCompressedThumbnail((byte[]) null);
                } catch (IOException e) {
                    MLog.e("ImagePersistTask", "doInBackgroundTimed: IOException: Couldn't get exif tags, not the end of the world");
                }
                bitmap = BitmapFactory.decodeByteArray(this.mBytes, 0, this.mBytes.length);
                if (ExifInterface.getOrientationParams(orientation).invertDimensions) {
                    clippedWidth = (int) (((float) this.mHeight) * this.mHeightPercent);
                    clippedHeight = this.mWidth;
                } else {
                    clippedWidth = this.mWidth;
                    clippedHeight = (int) (((float) this.mHeight) * this.mHeightPercent);
                }
                int offsetTop = (bitmap.getHeight() - clippedHeight) / 2;
                int offsetLeft = (bitmap.getWidth() - clippedWidth) / 2;
                this.mWidth = clippedWidth;
                this.mHeight = clippedHeight;
                clippedBitmap = Bitmap.createBitmap(clippedWidth, clippedHeight, Config.ARGB_8888);
                clippedBitmap.setDensity(bitmap.getDensity());
                Canvas clippedBitmapCanvas = new Canvas(clippedBitmap);
                Matrix matrix = new Matrix();
                matrix.postTranslate((float) (-offsetLeft), (float) (-offsetTop));
                clippedBitmapCanvas.drawBitmap(bitmap, matrix, null);
                clippedBitmapCanvas.save();
                ExifTag orientationTag = exifInterface.getTag(ExifInterface.TAG_ORIENTATION);
                exifInterface.clearExif();
                exifInterface.setTag(orientationTag);
                exifInterface.writeExif(clippedBitmap, outputStream);
            } else if (outputStream != null) {
                outputStream.write(this.mBytes);
            }
            if (bitmap != null) {
                bitmap.recycle();
            }
            if (clippedBitmap != null) {
                clippedBitmap.recycle();
            }
            if (outputStream != null) {
                try {
                    outputStream.flush();
                    try {
                        outputStream.close();
                    } catch (IOException e2) {
                        MLog.e("ImagePersistTask", "Error, trying close OutputStream," + e2);
                    }
                } catch (IOException e22) {
                    this.mOutputUri = null;
                    this.mException = e22;
                    MLog.e("ImagePersistTask", "error trying to flush and close the outputStream" + e22);
                } catch (Throwable th) {
                    try {
                        outputStream.close();
                    } catch (IOException e222) {
                        MLog.e("ImagePersistTask", "Error, trying close OutputStream," + e222);
                    }
                }
            }
        } catch (IOException e2222) {
            this.mOutputUri = null;
            this.mException = e2222;
            MLog.e("ImagePersistTask", "Unable to persist image to temp storage " + e2222);
            if (bitmap != null) {
                bitmap.recycle();
            }
            if (clippedBitmap != null) {
                clippedBitmap.recycle();
            }
            if (outputStream != null) {
                try {
                    outputStream.flush();
                    try {
                        outputStream.close();
                    } catch (IOException e22222) {
                        MLog.e("ImagePersistTask", "Error, trying close OutputStream," + e22222);
                    }
                } catch (IOException e222222) {
                    this.mOutputUri = null;
                    this.mException = e222222;
                    MLog.e("ImagePersistTask", "error trying to flush and close the outputStream" + e222222);
                } catch (Throwable th2) {
                    try {
                        outputStream.close();
                    } catch (IOException e2222222) {
                        MLog.e("ImagePersistTask", "Error, trying close OutputStream," + e2222222);
                    }
                }
            }
        } catch (Throwable th3) {
            if (bitmap != null) {
                bitmap.recycle();
            }
            if (clippedBitmap != null) {
                clippedBitmap.recycle();
            }
            if (outputStream != null) {
                try {
                    outputStream.flush();
                    try {
                        outputStream.close();
                    } catch (IOException e22222222) {
                        MLog.e("ImagePersistTask", "Error, trying close OutputStream," + e22222222);
                    }
                } catch (IOException e222222222) {
                    this.mOutputUri = null;
                    this.mException = e222222222;
                    MLog.e("ImagePersistTask", "error trying to flush and close the outputStream" + e222222222);
                } catch (Throwable th4) {
                    try {
                        outputStream.close();
                    } catch (IOException e2222222222) {
                        MLog.e("ImagePersistTask", "Error, trying close OutputStream," + e2222222222);
                    }
                }
            }
        }
        return null;
    }

    protected void onPostExecute(Void aVoid) {
        if (this.mOutputUri != null) {
            this.mCallback.onMediaReady(this.mOutputUri, "image/jpeg", this.mWidth, this.mHeight);
            MessageUtils.addFileToIndex(this.mContext, this.mOutputUri.getPath());
            return;
        }
        this.mCallback.onMediaFailed(this.mException);
    }

    private boolean checkFileDir() {
        try {
            File dirFile = new File(externalDir);
            if (dirFile.exists()) {
                return true;
            }
            return dirFile.mkdirs();
        } catch (Exception e) {
            MLog.e("ImagePersistTask", "checkFileDir failed," + e);
            return false;
        }
    }

    private Uri createOutputUri() {
        if (!checkFileDir()) {
            return null;
        }
        String imageDate = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Long.valueOf(System.currentTimeMillis()));
        return Uri.parse("file://" + externalDir + String.format("MmsCamera_%s.jpg", new Object[]{imageDate}));
    }
}
