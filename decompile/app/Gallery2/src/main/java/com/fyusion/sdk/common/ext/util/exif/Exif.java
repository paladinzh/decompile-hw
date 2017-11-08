package com.fyusion.sdk.common.ext.util.exif;

import java.io.IOException;
import java.io.InputStream;

/* compiled from: Unknown */
public class Exif {
    public static ExifInterface getExif(byte[] bArr) {
        ExifInterface exifInterface = new ExifInterface();
        try {
            exifInterface.readExif(bArr);
        } catch (IOException e) {
        }
        return exifInterface;
    }

    public static int getOrientation(ExifInterface exifInterface) {
        Integer tagIntValue = exifInterface.getTagIntValue(ExifInterface.TAG_ORIENTATION);
        return tagIntValue != null ? ExifInterface.getRotationForOrientationValue(tagIntValue.shortValue()) : 0;
    }

    public static int getOrientation(InputStream inputStream) {
        if (inputStream == null) {
            return 0;
        }
        ExifInterface exifInterface = new ExifInterface();
        try {
            exifInterface.readExif(inputStream);
            Integer tagIntValue = exifInterface.getTagIntValue(ExifInterface.TAG_ORIENTATION);
            return tagIntValue != null ? ExifInterface.getRotationForOrientationValue(tagIntValue.shortValue()) : 0;
        } catch (IOException e) {
            return 0;
        }
    }

    public static int getOrientation(byte[] bArr) {
        return bArr != null ? getOrientation(getExif(bArr)) : 0;
    }
}
