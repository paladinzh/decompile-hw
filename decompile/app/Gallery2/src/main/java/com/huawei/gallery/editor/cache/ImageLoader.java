package com.huawei.gallery.editor.cache;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.webkit.MimeTypeMap;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.exif.ExifInterface;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.watermark.manager.parse.WMElement;
import com.huawei.watermark.ui.WMComponent;
import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;

public final class ImageLoader {
    private ImageLoader() {
    }

    public static String getMimeType(Uri src) {
        String postfix = MimeTypeMap.getFileExtensionFromUrl(src.toString());
        if (postfix != null) {
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(postfix);
        }
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int getMetadataOrientation(Context context, Uri uri) {
        if (uri == null || context == null) {
            throw new IllegalArgumentException("bad argument to getOrientation");
        }
        Closeable closeable = null;
        int parseExif;
        try {
            closeable = context.getContentResolver().query(uri, new String[]{"orientation"}, null, null, null);
            if (closeable == null || !closeable.moveToNext()) {
                Utils.closeSilently(closeable);
                ExifInterface exif = new ExifInterface();
                InputStream inputStream = null;
                try {
                    if ("file".equals(uri.getScheme())) {
                        if (!"image/jpeg".equals(getMimeType(uri))) {
                            return 1;
                        }
                        exif.readExif(uri.getPath());
                    } else {
                        inputStream = context.getContentResolver().openInputStream(uri);
                        exif.readExif(inputStream);
                    }
                    parseExif = parseExif(exif);
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            GalleryLog.w("ImageLoader", "Failed to close InputStream." + e.getMessage());
                        }
                    }
                    return parseExif;
                } catch (IOException e2) {
                    GalleryLog.w("ImageLoader", "Failed to read EXIF orientation." + e2.getMessage());
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e22) {
                            GalleryLog.w("ImageLoader", "Failed to close InputStream." + e22.getMessage());
                        }
                    }
                } catch (Throwable th) {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e222) {
                            GalleryLog.w("ImageLoader", "Failed to close InputStream." + e222.getMessage());
                        }
                    }
                }
            } else {
                switch (closeable.getInt(0)) {
                    case WMComponent.ORI_90 /*90*/:
                        Utils.closeSilently(closeable);
                        return 6;
                    case 180:
                        Utils.closeSilently(closeable);
                        return 3;
                    case 270:
                        Utils.closeSilently(closeable);
                        return 8;
                    default:
                        parseExif = 1;
                        break;
                }
                Utils.closeSilently(closeable);
            }
        } catch (SecurityException e3) {
            parseExif = "ImageLoader";
            GalleryLog.noPermissionForMediaProviderLog(parseExif);
        } catch (SQLiteException e4) {
        } catch (IllegalArgumentException e5) {
        } catch (IllegalStateException e6) {
        } finally {
        }
        return 1;
    }

    public static int getMetadataOrientation(int ori) {
        switch (ori) {
            case WMComponent.ORI_90 /*90*/:
                return 6;
            case 180:
                return 3;
            case 270:
                return 8;
            default:
                return 1;
        }
    }

    private static int parseExif(ExifInterface exif) {
        Integer tagval = exif.getTagIntValue(ExifInterface.TAG_ORIENTATION);
        if (tagval == null) {
            return 1;
        }
        int orientation = tagval.intValue();
        switch (orientation) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                return orientation;
            default:
                return 1;
        }
    }

    private static int getScreenSize() {
        int screenSize = GalleryUtils.getWidthPixels() * GalleryUtils.getHeightPixels();
        return screenSize > 2073600 ? 2073600 : screenSize;
    }

    public static synchronized Bitmap getEditorBitmap(Uri uri, Context context, MediaItem mediaItem) {
        synchronized (ImageLoader.class) {
            if (uri == null || context == null) {
                return null;
            }
            Bitmap bitmap;
            ParcelFileDescriptor parcelFileDescriptor = null;
            try {
                Options options = new Options();
                options.inJustDecodeBounds = true;
                parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
                if (parcelFileDescriptor == null) {
                    if (parcelFileDescriptor != null) {
                        try {
                            parcelFileDescriptor.close();
                        } catch (Exception e) {
                        }
                    }
                    return null;
                }
                int orientation;
                FileDescriptor fd = parcelFileDescriptor.getFileDescriptor();
                BitmapFactory.decodeFileDescriptor(fd, null, options);
                int w = options.outWidth;
                int h = options.outHeight;
                int screenSize = getScreenSize();
                if (w * h > screenSize) {
                    options.inSampleSize = BitmapUtils.computeSampleSizeLarger((float) Math.sqrt((double) (((float) screenSize) / ((float) (w * h)))));
                }
                options.inJustDecodeBounds = false;
                options.inMutable = true;
                Bitmap temp = BitmapFactory.decodeFileDescriptor(fd, null, options);
                if (mediaItem.getFileInfo() != null) {
                    orientation = getMetadataOrientation(mediaItem.getRotation());
                } else {
                    orientation = getMetadataOrientation(context, uri);
                }
                bitmap = orientBitmap(temp, orientation, false);
                if (bitmap != temp) {
                    temp.recycle();
                }
                float scale = (float) Math.sqrt((double) ((((float) GalleryUtils.getWidthPixels()) * ((float) GalleryUtils.getHeightPixels())) / ((float) (bitmap.getWidth() * bitmap.getHeight()))));
                int width = (int) (((float) bitmap.getWidth()) * scale);
                int height = (int) (((float) bitmap.getHeight()) * scale);
                if (width == 0 || height == 0) {
                    if (parcelFileDescriptor != null) {
                        try {
                            parcelFileDescriptor.close();
                        } catch (Exception e2) {
                        }
                    }
                    return null;
                } else if (scale < WMElement.CAMERASIZEVALUE1B1) {
                    Bitmap editorBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                    bitmap.recycle();
                    if (parcelFileDescriptor != null) {
                        try {
                            parcelFileDescriptor.close();
                        } catch (Exception e3) {
                        }
                    }
                } else if (parcelFileDescriptor != null) {
                    try {
                        parcelFileDescriptor.close();
                    } catch (Exception e4) {
                    }
                }
            } catch (Exception e5) {
                GalleryLog.e("ImageLoader", "BitmapFactory.decodeFileDescriptor failed " + uri + "." + e5.getMessage());
                if (parcelFileDescriptor != null) {
                    try {
                        parcelFileDescriptor.close();
                    } catch (Exception e6) {
                    }
                }
                return null;
            } catch (Throwable th) {
                if (parcelFileDescriptor != null) {
                    try {
                        parcelFileDescriptor.close();
                    } catch (Exception e7) {
                    }
                }
            }
        }
        return editorBitmap;
        return bitmap;
    }

    public static Bitmap orientBitmap(Bitmap bitmap, int ori, boolean alwaysCopy) {
        Matrix matrix = new Matrix();
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (!(ori == 6 || ori == 8 || ori == 5)) {
            if (ori == 7) {
            }
            switch (ori) {
                case 2:
                    matrix.preScale(GroundOverlayOptions.NO_DIMENSION, WMElement.CAMERASIZEVALUE1B1);
                    break;
                case 3:
                    matrix.setRotate(BitmapDescriptorFactory.HUE_CYAN, ((float) w) / 2.0f, ((float) h) / 2.0f);
                    break;
                case 4:
                    matrix.preScale(WMElement.CAMERASIZEVALUE1B1, GroundOverlayOptions.NO_DIMENSION);
                    break;
                case 5:
                    matrix.setRotate(90.0f, ((float) w) / 2.0f, ((float) h) / 2.0f);
                    matrix.preScale(WMElement.CAMERASIZEVALUE1B1, GroundOverlayOptions.NO_DIMENSION);
                    break;
                case 6:
                    matrix.setRotate(90.0f, ((float) w) / 2.0f, ((float) h) / 2.0f);
                    break;
                case 7:
                    matrix.setRotate(BitmapDescriptorFactory.HUE_VIOLET, ((float) w) / 2.0f, ((float) h) / 2.0f);
                    matrix.preScale(WMElement.CAMERASIZEVALUE1B1, GroundOverlayOptions.NO_DIMENSION);
                    break;
                case 8:
                    matrix.setRotate(BitmapDescriptorFactory.HUE_VIOLET, ((float) w) / 2.0f, ((float) h) / 2.0f);
                    break;
                default:
                    if (!alwaysCopy) {
                        return bitmap;
                    }
                    break;
            }
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        int tmp = w;
        w = h;
        h = tmp;
        switch (ori) {
            case 2:
                matrix.preScale(GroundOverlayOptions.NO_DIMENSION, WMElement.CAMERASIZEVALUE1B1);
                break;
            case 3:
                matrix.setRotate(BitmapDescriptorFactory.HUE_CYAN, ((float) w) / 2.0f, ((float) h) / 2.0f);
                break;
            case 4:
                matrix.preScale(WMElement.CAMERASIZEVALUE1B1, GroundOverlayOptions.NO_DIMENSION);
                break;
            case 5:
                matrix.setRotate(90.0f, ((float) w) / 2.0f, ((float) h) / 2.0f);
                matrix.preScale(WMElement.CAMERASIZEVALUE1B1, GroundOverlayOptions.NO_DIMENSION);
                break;
            case 6:
                matrix.setRotate(90.0f, ((float) w) / 2.0f, ((float) h) / 2.0f);
                break;
            case 7:
                matrix.setRotate(BitmapDescriptorFactory.HUE_VIOLET, ((float) w) / 2.0f, ((float) h) / 2.0f);
                matrix.preScale(WMElement.CAMERASIZEVALUE1B1, GroundOverlayOptions.NO_DIMENSION);
                break;
            case 8:
                matrix.setRotate(BitmapDescriptorFactory.HUE_VIOLET, ((float) w) / 2.0f, ((float) h) / 2.0f);
                break;
            default:
                if (alwaysCopy) {
                    return bitmap;
                }
                break;
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
