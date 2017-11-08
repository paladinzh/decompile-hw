package com.android.gallery3d.data;

import com.android.gallery3d.exif.ExifInterface;
import com.android.gallery3d.util.GalleryLog;
import java.io.IOException;
import java.io.InputStream;

public class Exif {
    public static int getOrientation(InputStream is) {
        if (is == null) {
            return 0;
        }
        ExifInterface exif = new ExifInterface();
        try {
            exif.readExif(is);
            Integer val = exif.getTagIntValue(ExifInterface.TAG_ORIENTATION);
            if (val == null) {
                return 0;
            }
            return ExifInterface.getRotationForOrientationValue(val.shortValue());
        } catch (IOException e) {
            GalleryLog.w("GalleryExif", "Failed to read EXIF orientation." + e.getMessage());
            return 0;
        } catch (Throwable t) {
            GalleryLog.w("GalleryExif", "Could not read exif from input stream" + t.getMessage());
            return 0;
        }
    }
}
