package com.android.contacts.util;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import com.android.common.io.MoreCloseables;
import com.google.common.io.Closeables;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ContactPhotoUtils {
    public static Uri generateTempImageUri(Context context) {
        return getUriByFileProvider(context, "com.android.contacts.files", new File(getRootFilePath(context), generateTempPhotoFileName()));
    }

    public static Uri generateTempCroppedImageUri(Context context) {
        return getUriByFileProvider(context, "com.android.contacts.files", new File(getRootFilePath(context), generateTempCroppedPhotoFileName()));
    }

    public static Uri getUriByFileProvider(Context context, String string, File file) {
        Uri uri = null;
        try {
            uri = FileProvider.getUriForFile(context, string, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uri;
    }

    public static File getRootFilePath(Context context) {
        File dir;
        if (Environment.getExternalStorageDirectory() == null || !"mounted".equals(Environment.getExternalStorageState())) {
            dir = context.getCacheDir();
        } else {
            dir = context.getExternalCacheDir();
        }
        if (!(dir == null || dir.mkdirs())) {
            dir.deleteOnExit();
        }
        return dir;
    }

    public static String pathForTempPhoto(Context context, String fileName) {
        File dir;
        if (Environment.getExternalStorageDirectory() == null || !"mounted".equals(Environment.getExternalStorageState())) {
            dir = context.getCacheDir();
        } else {
            dir = context.getExternalCacheDir();
        }
        try {
            dir.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new File(dir, fileName).getAbsolutePath();
    }

    public static String pathForTempPhoto(Context context, String path, String fileName) {
        File dir;
        if (Environment.getExternalStorageDirectory() == null || !"mounted".equals(Environment.getExternalStorageState())) {
            dir = context.getCacheDir();
        } else {
            dir = context.getExternalCacheDir();
        }
        return new File(new File(dir, path), fileName).getAbsolutePath();
    }

    private static String generateTempPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        return "ContactPhoto-" + new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss", Locale.US).format(date) + ".jpg";
    }

    private static String generateTempCroppedPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        return "ContactPhoto-" + new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss", Locale.US).format(date) + "-cropped.jpg";
    }

    public static Bitmap getBitmapFromUri(Context context, Uri uri) throws FileNotFoundException {
        if (uri == null) {
            HwLog.e("ContactPhotoUtils", "get Bitmap From Uri, the Uri is null");
            throw new FileNotFoundException();
        }
        InputStream imageStream = context.getContentResolver().openInputStream(uri);
        try {
            Bitmap decodeStream = BitmapFactory.decodeStream(imageStream);
            return decodeStream;
        } finally {
            Closeables.closeQuietly(imageStream);
        }
    }

    public static byte[] compressBitmap(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream((bitmap.getWidth() * bitmap.getHeight()) * 4);
        try {
            bitmap.compress(CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
            HwLog.w("ContactPhotoUtils", "Unable to serialize photo: " + e.toString());
            return null;
        }
    }

    public static void addCropExtras(Intent intent, int photoSize) {
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", photoSize);
        intent.putExtra("outputY", photoSize);
    }

    public static void addPhotoPickerExtras(Intent intent, Uri photoUri) {
        intent.putExtra("output", photoUri);
        intent.addFlags(3);
        intent.setClipData(ClipData.newRawUri("output", photoUri));
    }

    public static boolean savePhotoFromUriToUri(Context context, Uri inputUri, Uri outputUri, boolean deleteAfterSave) {
        Closeable closeable = null;
        InputStream inputStream = null;
        AssetFileDescriptor assetFileDescriptor = null;
        if (!(outputUri == null || inputUri == null)) {
            try {
                assetFileDescriptor = context.getContentResolver().openAssetFileDescriptor(outputUri, "rw");
                if (assetFileDescriptor != null) {
                    closeable = assetFileDescriptor.createOutputStream();
                    inputStream = context.getContentResolver().openInputStream(inputUri);
                    byte[] buffer = new byte[16384];
                    while (true) {
                        int length = inputStream.read(buffer);
                        if (length <= 0) {
                            break;
                        }
                        closeable.write(buffer, 0, length);
                    }
                    return true;
                }
            } catch (IOException e) {
                return false;
            } catch (SecurityException se) {
                HwLog.e("ContactPhotoUtils", "savePhotoFromUriToUri SecurityException", se);
                return false;
            } finally {
                Closeables.closeQuietly(inputStream);
                Closeables.closeQuietly(closeable);
                if (deleteAfterSave && inputUri != null) {
                    context.getContentResolver().delete(inputUri, null, null);
                }
                MoreCloseables.closeQuietly(assetFileDescriptor);
            }
        }
        Closeables.closeQuietly(null);
        Closeables.closeQuietly(null);
        if (deleteAfterSave && inputUri != null) {
            context.getContentResolver().delete(inputUri, null, null);
        }
        MoreCloseables.closeQuietly(assetFileDescriptor);
        return false;
    }
}
